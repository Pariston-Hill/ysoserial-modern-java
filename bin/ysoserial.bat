@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%.."
if not defined YSOSERIAL_JAR set "YSOSERIAL_JAR=%PROJECT_DIR%\target\ysoserial-0.0.6-SNAPSHOT-all.jar"

if not exist "%YSOSERIAL_JAR%" (
  echo ysoserial jar not found: %YSOSERIAL_JAR% 1>&2
  echo Run: mvn -DskipTests package 1>&2
  exit /b 1
)

java ^
  --add-opens java.base/sun.reflect.annotation=ALL-UNNAMED ^
  --add-opens java.base/java.lang=ALL-UNNAMED ^
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED ^
  --add-opens java.base/java.net=ALL-UNNAMED ^
  --add-opens java.base/java.util=ALL-UNNAMED ^
  --add-opens java.management/javax.management=ALL-UNNAMED ^
  --add-opens java.rmi/sun.rmi.server=ALL-UNNAMED ^
  --add-opens java.rmi/sun.rmi.transport=ALL-UNNAMED ^
  --add-opens java.rmi/sun.rmi.transport.tcp=ALL-UNNAMED ^
  --add-opens java.sql.rowset/com.sun.rowset=ALL-UNNAMED ^
  --add-opens java.xml/com.sun.org.apache.xalan.internal.xsltc=ALL-UNNAMED ^
  --add-opens java.xml/com.sun.org.apache.xalan.internal.xsltc.runtime=ALL-UNNAMED ^
  --add-opens java.xml/com.sun.org.apache.xalan.internal.xsltc.trax=ALL-UNNAMED ^
  --add-opens java.xml/com.sun.org.apache.xml.internal.dtm=ALL-UNNAMED ^
  --add-opens java.xml/com.sun.org.apache.xml.internal.serializer=ALL-UNNAMED ^
  --add-exports java.rmi/sun.rmi.server=ALL-UNNAMED ^
  --add-exports java.rmi/sun.rmi.transport=ALL-UNNAMED ^
  --add-exports java.rmi/sun.rmi.transport.tcp=ALL-UNNAMED ^
  --add-exports java.sql.rowset/com.sun.rowset=ALL-UNNAMED ^
  --add-exports java.xml/com.sun.org.apache.xalan.internal.xsltc=ALL-UNNAMED ^
  --add-exports java.xml/com.sun.org.apache.xalan.internal.xsltc.runtime=ALL-UNNAMED ^
  --add-exports java.xml/com.sun.org.apache.xalan.internal.xsltc.trax=ALL-UNNAMED ^
  --add-exports java.xml/com.sun.org.apache.xml.internal.dtm=ALL-UNNAMED ^
  --add-exports java.xml/com.sun.org.apache.xml.internal.serializer=ALL-UNNAMED ^
  -jar "%YSOSERIAL_JAR%" %*
