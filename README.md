# jNBT v1.5

<div align="center">

![Version](https://img.shields.io/badge/version-1.5-blue)
![Minecraft](https://img.shields.io/badge/minecraft-1.21.5%20(Java)-green)
![Java](https://img.shields.io/badge/java-21-red)
![License](https://img.shields.io/badge/license-MIT-brightgreen)

*A modern, efficient Java library for reading, editing, and writing Minecraft NBT files*

</div>

## ✨ Features

**jNBT** provides a complete solution for NBT file manipulation with both low-level control and high-level convenience:

- 🎯 **Complete NBT Support** - All tags from the [official specification](https://minecraft.wiki/w/NBT_format)
- 🛠️ **Fluent Builder API** - Modern builder pattern for easy structure creation
- 📦 **Smart Compression** - Automatic detection and support for GZIP, ZLIB, and uncompressed files
- 🔧 **Interface-Based Design** - Clean APIs
- ⚡ **Type Safety** - Minimal casting

## 🚀 Quick Start

**Read NBT file:**
```java
ICompoundTag root = NBTFileFactory.readNBTFile(new File("level.dat"));
String worldName = root.getString("LevelName");
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

## 📋 Status

### ✅ Supported
- All NBT tag types (Byte, Short, Int, Long, Float, Double, String, List, Compound, Arrays)
- Complete CRUD operations (Create, Read, Update, Delete)
- Compression formats: **GZIP**, **ZLIB**, **None**
- Many concenience methods

### 🔮 Future Plans
- Bedrock Edition NBT support
- SNBT (String NBT) format
- Conversion to JSON
- Graphical NBT editor (maybe)

## 📖 Documentation

Explore the `examples/` folder for comprehensive usage patterns.

## 📦 Building
```bash
mvn clean install
```

## ⚠️ Important Notice

**Always backup your files before modification.** While thoroughly tested, data corruption is always possible with file manipulation tools.

## 🔗 Related Projects

This library powers:
- [jMCX](https://github.com/paul-eff/jMCX) - Edit Minecraft world files
- [MinecraftOfflineOnlineConverter](https://github.com/paul-eff/MinecraftOfflineOnlineConverter) - Player data migration

## 📚 References

- [Official NBT Format Specification](https://minecraft.wiki/w/NBT_format)

## Remark
Minecraft is a registered trademark of Mojang AB.