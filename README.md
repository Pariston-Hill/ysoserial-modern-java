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

Release jars can be published from this fork's GitHub Releases page. Until a
release is published, build the jar locally from source.

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

Show available payloads:

```shell
java -jar target/ysoserial-0.0.6-SNAPSHOT-all.jar
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

## Available Payloads

Run the jar without arguments to print the payload list generated from the
current build:

```shell
java -jar target/ysoserial-0.0.6-SNAPSHOT-all.jar
```

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
