# jNBT v0.1
<p align="center">
  <img src="https://img.shields.io/badge/version-0.1-blue">
  <img src="https://img.shields.io/badge/minecraft-1.19 (Java)-green">
  <img src="https://img.shields.io/badge/java-11-red">
</p>
A Java library for interacting with Minecraft NBT files.

### Currently supports
- Read operation
- Java Minecraft NBT files (raw & gzip)
### WIP
- Edit and write operations
- Java Minecraft NBT files (zlib DEFLATE)
- Support for JSON (import & export)
### Future
- Bedrock Minecraft NBT files
- GUI

# Usage

- Obviously download the jar
- Add it as a dependency to your project (dependent on your IDE)
- Take a look into the `Example.java` file or try something like:
```java
// Parse and output NBT file
NBTReader reader = new NBTReader(new File("./path/to/NBT/file.dat"));

try {
    System.out.println(reader.read());
} catch (IOException e) {
    throw new RuntimeException(e);
}
```

# Sources
- https://wiki.vg/NBT
- https://minecraft.fandom.com/wiki/NBT_format#SNBT_format

# Disclaimer
Please always make a backup of your files before using this tool.
Whilst it was thoroughly tested, there is always the chance that a bug might occur!

If you need support for a specific version or a custom feature, please leave me a message or issue :)!

# Remark
Minecraft is a registered trademark of Mojang AB.

