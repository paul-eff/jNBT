# jNBT

<div align="center">

![Version](https://img.shields.io/badge/version-2.0.0-blue)
![Minecraft](https://img.shields.io/badge/minecraft-26.3%20(Java)-green)
![Java](https://img.shields.io/badge/java-25-red)
![License](https://img.shields.io/badge/license-MIT-brightgreen)

*A modern, efficient Java library for reading, editing, and writing Minecraft NBT files*

</div>

## Features

**jNBT** is a library for reading, editing, and writing Java Edition NBT files.

- **All Java Edition NBT Tag Types** - See the [Minecraft Wiki's community NBT reference](https://minecraft.wiki/w/NBT_format)
- **Fluent Builder API** - Modern builder pattern for easy structure creation
- **Smart Compression** - Automatic detection and support for GZIP, ZLIB, and uncompressed files
- **SNBT Support** - Parse and generate Java Edition Stringified NBT

## Installation

You can import the [latest release](https://github.com/paul-eff/jNBT/releases/latest) `.jar` file directly as a dependency via your IDE, or use a build system:

**Gradle:**
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("de.pauleff:jnbt:2.0.0")
}
```

**Maven:**
```xml
<dependencies>
    <dependency>
        <groupId>de.pauleff</groupId>
        <artifactId>jnbt</artifactId>
        <version>2.0.0</version>
    </dependency>
</dependencies>
```

To use the development version, build and publish it to your local Maven repository as described below,
then use `de.pauleff:jnbt:2.0.0-SNAPSHOT` with `mavenLocal()` in your consuming Gradle project.

## Quick Start

**Read NBT file:**
```java
ICompoundTag root = NBTFileFactory.readNBTFile(new File("level.dat"));
ICompoundTag data = root.getCompound("Data");
String worldName = data.getString("LevelName");
```

**Create with Builder:**
```java
ICompoundTag player = NBTBuilder.compound("Player")
    .addString("name", "Steve")
    .addInt("level", 25)
    .addCompound("inventory")
        .addString("item", "diamond_sword")
    .endCompound()
    .build();
```

**Parse SNBT:**
```java
ITag<?> tag = NBTFactory.parseFromSNBT("{display:{Name:\"Custom Item\"}}");
```

**Generate SNBT:**
```java
SNBTSerializer serializer = new SNBTSerializer();
String snbt = serializer.serialize(tag);
```

### Supported

- All Java Edition NBT tag types (Byte, Short, Int, Long, Float, Double, String, List, Compound, Arrays)
- Create, read, update, and delete tags
- Compression formats: **GZIP**, **ZLIB**, **None**
- SNBT (Stringified NBT) parsing and generation

### Future Plans
- Bedrock Edition NBT support
- Conversion to JSON

## Documentation

Explore [`src/examples`](src/examples) for some basic usage patterns.

## Building

Install JDK 25 and use the included Gradle wrapper:
```bash
./gradlew clean build
```

Run only the tests:
```bash
./gradlew test
```

Install the development version into your local Maven repository:
```bash
./gradlew publishToMavenLocal
```

## Important Notice

**Back up your files before modifying them.**

## Related Projects

This library powers:
- [jMCX](https://github.com/paul-eff/jMCX) - Edit Minecraft world files
- [MinecraftOfflineOnlineConverter](https://github.com/paul-eff/MinecraftOfflineOnlineConverter) - Player data migration

## Remark

Licensed under the [MIT License](LICENSE).

Minecraft is a registered trademark of Mojang AB.
