# jNBT v0.7
<p align="center">
  <img src="https://img.shields.io/badge/version-0.7-blue">
  <img src="https://img.shields.io/badge/minecraft-1.21.4 (Java)-green">
  <img src="https://img.shields.io/badge/java-21-red">
</p>
Currently there aren't many sophisticated methods to interact with Minecraft NBT files in Java (read, edit and write). 
And those that are around have not been updated in a while. With this library I want to provide an up to date and efficient way to interact with NBT files.

This NBT library will in the near future be the base of my Anvil library (WIP) and the [MinecraftOfflineOnlineConverter](https://github.com/paul-eff/MinecraftOfflineOnlineConverter).

### Currently supports
- Read operation
- Adding, removing, finding and editing elements in the NBT file 
- Write operation
- Java Minecraft NBT files (raw & gzip)
### WIP
- Java Minecraft NBT files (zlib DEFLATE)
- Write with GZIP
### Future
- Bedrock Minecraft NBT files
- Anvil (other repository)
- GUI

# Usage

- Obviously download the jar
- Add it as a dependency to your project (dependent on your IDE)
- Take a look into the `Example.java` file or try something like:
```java
// Parse and output NBT file
NBTReader reader = new NBTReader(new File("./path/to/NBT/file.dat"));

try {
    // Fetch and print the whole NBT file
    Collection_Tag root = reader.read();
    System.out.println(root);

    // Fetch and print the "Inventory" list
    // Currently it doesn't matter if you use Tag or Tag<?>. The latter generates less warning though.
    Tag<?> inventory = root.getTagByName("Inventory");
    System.out.println(inventory);
    
    // Add a new item to the "Inventory" list
    Tag_Compound diamondHoe = new Tag_Compound();
    diamondHoe.addTag(new Tag_Int("count", 1));
    diamondHoe.addTag(new Tag_Double("Slot", 1.0));
    diamondHoe.addTag(new Tag_String("id", "minecraft:diamond_hoe"));
    ((Collection_Tag) inventory).addTag(diamondHoe);
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

