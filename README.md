# jNBT v1.0
<p align="center">
  <img src="https://img.shields.io/badge/version-1.0-blue">
  <img src="https://img.shields.io/badge/minecraft-1.21.4 (Java)-green">
  <img src="https://img.shields.io/badge/java-21-red">
</p>
Currently there aren't any sophisticated libraries to interact with Minecraft NBT files in Java (read, edit and write). 
And those that are around haven't been updated in a while. 
With this library I want to provide an up to date and efficient (and also overengineered) way to interact with NBT files.

This NBT library will in the near future be the base of my Anvil library (WIP) and the [MinecraftOfflineOnlineConverter](https://github.com/paul-eff/MinecraftOfflineOnlineConverter).

### Supports
- All tags present in the current [NBT specification](https://minecraft.wiki/w/NBT_format)
- All common operations: reading, editing (add, remove, replace, update) and writing
- Compression types: GZIP and NONE
### WIP
- Compression types: ZLIB and LZ4
- Extract read/write methods to each tag (internal stuff, nothing functional)
### Future
- Bedrock Minecraft NBT files
- Anvil (other repository)
- SNBT support (maybe)
- GUI (BIG maybe!)

# Usage

- Obviously download the jar
- Add it as a dependency to your project (dependent on your IDE)
- Take a look into the `Example.java` file or try something like:
```java
NBTReader reader;
NBTWriter writer;
try {
    // === Creating a reader ===
    reader = new NBTReader(new File("./path/to/NBT/file.dat"));
    // Fetch and print the whole NBT file
    Tag_Compound root = reader.read();
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
    // Remove an item from the "Inventory" list - accessing the inventory's data directly (getData array)
    ((Collection_Tag) inventory).getData().remove(1);
    // Remove an item from the "Inventory" list - accessing the inventory's data directly (iterator)
    Iterator<Tag<?>> iterator = ((Collection_Tag) inventory).getData().iterator();
    while (iterator.hasNext()) {
        Tag<?> tag = iterator.next();
        if (tag instanceof Tag_Compound invItem) {
            if (invItem.getTagByName("id").getData().equals("minecraft:diamond_hoe")) {
            iterator.remove();
            break;
            }
        }
    }
    // Remove an element ("Inventory") from the root - using built in remover
    root.removeTag(root.getTagByName("Inventory"));
    // === Creating a writer ===
    // If you are overwriting a file, the writer will determine the compression type automatically
    writer = new NBTWriter(new File("./path/to/NBT/file.dat"));
    // In any other case you can specify the compression type
    writer = new NBTWriter(new File("./path/to/new/NBT/file.dat"), Compression_Types.GZIP);
    // Write the root back to the file
    writer.write(root);
} catch (IOException e) {
    throw new RuntimeException(e);
}
```

# Sources
- https://minecraft.wiki/w/NBT_format

# Disclaimer
Please always make a backup of your files before using this tool.
Whilst it was thoroughly tested, there is always the chance that a bug might occur!

If you need support for a specific version or a custom feature, please leave me a message or issue :)!

# Remark
Minecraft is a registered trademark of Mojang AB.

