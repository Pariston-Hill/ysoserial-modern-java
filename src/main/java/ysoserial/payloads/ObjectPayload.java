package ysoserial.payloads;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import ysoserial.GeneratePayload;


@SuppressWarnings ( "rawtypes" )
public interface ObjectPayload <T> {

    /*
     * return armed payload object to be serialized that will execute specified
     * command on deserialization
     */
    public T getObject ( String command ) throws Exception;

    public static class Utils {

        // get payload classes by classpath scanning
        public static Set<Class<? extends ObjectPayload>> getPayloadClasses () {
            final Set<Class<? extends ObjectPayload>> payloadTypes = new HashSet<Class<? extends ObjectPayload>>();
            final String packageName = ObjectPayload.class.getPackage().getName();
            final String packagePath = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if ( classLoader == null ) {
                classLoader = ObjectPayload.class.getClassLoader();
            }

            try {
                final Enumeration<URL> resources = classLoader.getResources(packagePath);
                while ( resources.hasMoreElements() ) {
                    final URL resource = resources.nextElement();
                    if ( "file".equals(resource.getProtocol()) ) {
                        collectFromFile(packageName, resource, payloadTypes, classLoader);
                    }
                    else if ( "jar".equals(resource.getProtocol()) ) {
                        collectFromJar(packageName, resource, payloadTypes, classLoader);
                    }
                }
            }
            catch ( IOException e ) {
                throw new IllegalStateException("Failed to scan payload classes", e);
            }
            return payloadTypes;
        }


        private static void collectFromFile ( final String packageName, final URL resource,
                final Set<Class<? extends ObjectPayload>> payloadTypes, final ClassLoader classLoader ) {
            try {
                final Path root = new File(resource.toURI()).toPath();
                try ( Stream<Path> paths = Files.walk(root) ) {
                    paths.filter(Files::isRegularFile)
                            .filter(path -> path.getFileName().toString().endsWith(".class"))
                            .forEach(path -> addPayloadClass(packageName, root, path, payloadTypes, classLoader));
                }
            }
            catch ( IOException | URISyntaxException e ) {
                throw new IllegalStateException("Failed to scan payload classes from " + resource, e);
            }
        }


        private static void collectFromJar ( final String packageName, final URL resource,
                final Set<Class<? extends ObjectPayload>> payloadTypes, final ClassLoader classLoader ) {
            try {
                final JarURLConnection connection = (JarURLConnection) resource.openConnection();
                final String packagePath = connection.getEntryName();
                final JarFile jarFile = connection.getJarFile();
                final Enumeration<JarEntry> entries = jarFile.entries();
                while ( entries.hasMoreElements() ) {
                    final JarEntry entry = entries.nextElement();
                    final String name = entry.getName();
                    if ( entry.isDirectory() || !name.startsWith(packagePath + "/") || !name.endsWith(".class") ) {
                        continue;
                    }
                    addPayloadClass(name.replace('/', '.').substring(0, name.length() - ".class".length()), payloadTypes, classLoader);
                }
            }
            catch ( IOException e ) {
                throw new IllegalStateException("Failed to scan payload classes from " + resource, e);
            }
        }


        private static void addPayloadClass ( final String packageName, final Path root, final Path classFile,
                final Set<Class<? extends ObjectPayload>> payloadTypes, final ClassLoader classLoader ) {
            final String relativeClass = root.relativize(classFile).toString()
                    .replace(File.separatorChar, '.')
                    .replaceAll("\\.class$", "");
            addPayloadClass(packageName + "." + relativeClass, payloadTypes, classLoader);
        }


        @SuppressWarnings ( "unchecked" )
        private static void addPayloadClass ( final String className, final Set<Class<? extends ObjectPayload>> payloadTypes,
                final ClassLoader classLoader ) {
            if ( className.indexOf('$') >= 0 ) {
                return;
            }
            try {
                final Class<?> clazz = Class.forName(className, false, classLoader);
                if ( ObjectPayload.class.isAssignableFrom(clazz) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()) ) {
                    payloadTypes.add((Class<? extends ObjectPayload>) clazz);
                }
            }
            catch ( ClassNotFoundException | LinkageError e ) {
                // Some legacy payloads are intentionally unavailable on newer JDKs.
            }
        }


        @SuppressWarnings ( "unchecked" )
        public static Class<? extends ObjectPayload> getPayloadClass ( final String className ) {
            Class<? extends ObjectPayload> clazz = null;
            try {
                clazz = (Class<? extends ObjectPayload>) Class.forName(className);
            }
            catch ( Exception e1 ) {}
            if ( clazz == null ) {
                try {
                    return clazz = (Class<? extends ObjectPayload>) Class
                            .forName(GeneratePayload.class.getPackage().getName() + ".payloads." + className);
                }
                catch ( Exception e2 ) {}
            }
            if ( clazz != null && !ObjectPayload.class.isAssignableFrom(clazz) ) {
                clazz = null;
            }
            return clazz;
        }


        public static Object makePayloadObject ( String payloadType, String payloadArg ) {
            final Class<? extends ObjectPayload> payloadClass = getPayloadClass(payloadType);
            if ( payloadClass == null || !ObjectPayload.class.isAssignableFrom(payloadClass) ) {
                throw new IllegalArgumentException("Invalid payload type '" + payloadType + "'");

            }

            final Object payloadObject;
            try {
                final ObjectPayload payload = payloadClass.newInstance();
                payloadObject = payload.getObject(payloadArg);
            }
            catch ( Exception e ) {
                throw new IllegalArgumentException("Failed to construct payload", e);
            }
            return payloadObject;
        }


        @SuppressWarnings ( "unchecked" )
        public static void releasePayload ( ObjectPayload payload, Object object ) throws Exception {
            if ( payload instanceof ReleaseableObjectPayload ) {
                ( (ReleaseableObjectPayload) payload ).release(object);
            }
        }


        public static void releasePayload ( String payloadType, Object payloadObject ) {
            final Class<? extends ObjectPayload> payloadClass = getPayloadClass(payloadType);
            if ( payloadClass == null || !ObjectPayload.class.isAssignableFrom(payloadClass) ) {
                throw new IllegalArgumentException("Invalid payload type '" + payloadType + "'");

            }

            try {
                final ObjectPayload payload = payloadClass.newInstance();
                releasePayload(payload, payloadObject);
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }

        }
    }
}
