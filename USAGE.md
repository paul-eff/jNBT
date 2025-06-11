# jNBT Usage Guide

A comprehensive guide to using the jNBT library for reading, editing, and writing Minecraft NBT (Named Binary Tag) files.

## Table of Contents

- [Quick Start](#quick-start)
- [Core APIs](#core-apis)
- [File Operations](#file-operations)
- [Creating NBT Data](#creating-nbt-data)
- [Builder Pattern](#builder-pattern)
- [Tag Types Reference](#tag-types-reference)
- [Compression Support](#compression-support)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)
- [Examples](#examples)

## Quick Start

### Reading an NBT File

```java
import me.paulferlitz.api.*;
import java.io.File;

// Simple one-liner to read any NBT file
ICompoundTag root = NBTFileFactory.readNBTFile(new File("level.dat"));

// Access data without casting
String worldName = root.getString("LevelName");
int spawnX = root.getInt("SpawnX");
```

### Writing an NBT File

```java
// Create data structure
ICompoundTag playerData = NBTFactory.createCompound("Player")
    .addString("Name", "Steve")
    .addInt("Level", 42)
    .addDouble("Health", 20.0);

// Write to file with automatic compression
NBTFileFactory.writeNBTFile(new File("player.dat"), playerData);
```

## Core APIs

### 1. NBTFileFactory - File I/O Operations

The primary entry point for reading and writing NBT files with automatic compression detection.

#### Key Methods

```java
// Reading
static ICompoundTag readNBTFile(File nbtFile)
static INBTReader createReader(File nbtFile)
static INBTReader createReader(DataInputStream dis)

// Writing  
static void writeNBTFile(File nbtFile, ICompoundTag root)
static void writeNBTFile(File nbtFile, ICompoundTag root, Compression_Types compression)
static INBTWriter createWriter(File nbtFile)
static INBTWriter createWriter(File nbtFile, Compression_Types compression)

// Utilities
static void copyNBTFile(File source, File destination)
static boolean isValidNBTFile(File nbtFile)
```

#### Usage Examples

```java
// Read with automatic compression detection
ICompoundTag data = NBTFileFactory.readNBTFile(new File("level.dat"));

// Write with preserved compression format
NBTFileFactory.writeNBTFile(new File("level.dat"), data);

// Write with specific compression
NBTFileFactory.writeNBTFile(new File("config.dat"), data, Compression_Types.NONE);

// Copy file preserving format
NBTFileFactory.copyNBTFile(new File("world.dat"), new File("backup.dat"));

// Validate NBT file
if (NBTFileFactory.isValidNBTFile(new File("suspicious.dat"))) {
    System.out.println("Valid NBT file");
}
```

### 2. NBTFactory - Tag Creation Factory

Creates individual NBT tags programmatically without dealing with constructors.

#### Primitive Tags

```java
// String tags
ITag<String> createString()
ITag<String> createString(String name)
ITag<String> createString(String name, String value)

// Numeric tags
ITag<Integer> createInt()
ITag<Integer> createInt(String name)
ITag<Integer> createInt(String name, int value)

ITag<Double> createDouble()
ITag<Double> createDouble(String name)
ITag<Double> createDouble(String name, double value)

ITag<Float> createFloat()
ITag<Float> createFloat(String name)  
ITag<Float> createFloat(String name, float value)

ITag<Byte> createByte()
ITag<Byte> createByte(String name)
ITag<Byte> createByte(String name, byte value)

ITag<Short> createShort()
ITag<Short> createShort(String name)
ITag<Short> createShort(String name, short value)

ITag<Long> createLong()
ITag<Long> createLong(String name)
ITag<Long> createLong(String name, long value)
```

#### Array Tags

```java
// Byte arrays
ITag<byte[]> createByteArray()
ITag<byte[]> createByteArray(String name)
ITag<byte[]> createByteArray(String name, byte[] value)

// Integer arrays
ITag<int[]> createIntArray()
ITag<int[]> createIntArray(String name)
ITag<int[]> createIntArray(String name, int[] value)

// Long arrays
ITag<long[]> createLongArray()
ITag<long[]> createLongArray(String name)
ITag<long[]> createLongArray(String name, long[] value)
```

#### Container Tags

```java
// Compound tags (key-value storage)
ICompoundTag createCompound()
ICompoundTag createCompound(String name)
ICompoundTag createCompound(String name, ArrayList<ITag<?>> data)

// List tags (homogeneous arrays)
IListTag createList(int listTypeID)
IListTag createList(String name, int listTypeID)
```

#### Convenience Methods

```java
// Quick key-value compound creation
ICompoundTag createSimpleCompound(String rootName, String... keyValuePairs)

// Example usage
ICompoundTag config = NBTFactory.createSimpleCompound("Config",
    "serverName", "My Server",
    "maxPlayers", "20", 
    "difficulty", "normal"
);
```

### 3. NBTBuilder - Fluent Construction API

Advanced builder pattern for creating complex NBT structures with method chaining.

#### Static Factory Methods

```java
// Container builders
static CompoundBuilder compound(String name)
static ListBuilder list(String name, NBTTags listType)

// Direct tag creation
static ITag<String> string(String name, String value)
static ITag<Integer> integer(String name, int value)
static ITag<Double> doubleTag(String name, double value)
static ITag<Float> floatTag(String name, float value)
static ITag<Byte> byteTag(String name, byte value)
static ITag<Short> shortTag(String name, short value)
static ITag<Long> longTag(String name, long value)
```

#### File Integration

```java
// Load existing file for modification
static CompoundBuilder fromFile(File file)

// Build and save directly
void buildAndSave(File file)
void buildAndSave(File file, Compression_Types compression)
```

## File Operations

### Reading Files

```java
// Method 1: Simple one-liner
ICompoundTag root = NBTFileFactory.readNBTFile(new File("world.dat"));

// Method 2: Manual resource management
try (INBTReader reader = NBTFileFactory.createReader(new File("world.dat"))) {
    ICompoundTag root = reader.read();
    // Process data...
}

// Method 3: From DataInputStream
try (DataInputStream dis = new DataInputStream(new FileInputStream("world.dat"))) {
    INBTReader reader = NBTFileFactory.createReader(dis);
    ICompoundTag root = reader.read();
}
```

### Writing Files

```java
// Method 1: Simple write (preserves existing compression)
NBTFileFactory.writeNBTFile(new File("world.dat"), data);

// Method 2: Specify compression
NBTFileFactory.writeNBTFile(new File("world.dat"), data, Compression_Types.GZIP);

// Method 3: Manual resource management
try (INBTWriter writer = NBTFileFactory.createWriter(new File("world.dat"), Compression_Types.GZIP)) {
    writer.write(data);
}
```

## Creating NBT Data

### Using NBTFactory

```java
// Create player data
ICompoundTag player = NBTFactory.createCompound("Player");
player.addString("Name", "Steve")
      .addInt("Level", 50)
      .addDouble("Health", 20.0)
      .addFloat("FoodLevel", 20.0f);

// Create inventory list
IListTag inventory = NBTFactory.createList("Inventory", NBTTags.Tag_Compound.getId());
inventory.addCompound("item1");

// Add inventory to player
player.addTag(inventory);
```

### Working with ICompoundTag

```java
ICompoundTag compound = NBTFactory.createCompound("Data");

// Adding values with fluent API
compound.addString("name", "example")
        .addInt("count", 42)
        .addDouble("value", 3.14);

// Checking for tags
if (compound.hasTag("name")) {
    String name = compound.getString("name");
}

// Getting child tags
ITag<?> nameTag = compound.getTag("name");
ICompoundTag nested = compound.getCompound("nestedData");
IListTag list = compound.getList("items");

// Type-safe getters with defaults
int count = compound.getInt("count"); // Returns 0 if not found
String name = compound.getString("name"); // Returns null if not found
```

### Working with IListTag

```java
// Create list for compound elements
IListTag playerList = NBTFactory.createList("Players", NBTTags.Tag_Compound.getId());

// Add elements (must match list type)
playerList.addCompound("player1");
playerList.addCompound("player2");

// Create list for primitives
IListTag scoreList = NBTFactory.createList("Scores", NBTTags.Tag_Int.getId()); 
scoreList.addInt("score1", 100);
scoreList.addInt("score2", 250);

// Get list type information
int typeId = scoreList.getListTypeID(); // Returns NBTTags.Tag_Int.getId()
```

## Builder Pattern

### Basic Compound Building

```java
// Create player with inventory
ICompoundTag player = NBTBuilder.compound("Player")
    .addString("Name", "Steve")
    .addInt("Level", 42)
    .addList("Inventory", NBTTags.Tag_Compound)
        .addCompound("sword")
            .addString("id", "minecraft:diamond_sword")
            .addInt("Count", 1)
            .addInt("Damage", 0)
        .end()
        .addCompound("apple")
            .addString("id", "minecraft:apple")
            .addInt("Count", 64)
        .end()
    .end()
    .build();
```

### File Integration with Builder

```java
// Create and save in one operation
NBTBuilder.compound("WorldData")
    .addString("LevelName", "My World")
    .addInt("SpawnX", 0)
    .addInt("SpawnY", 64)
    .addInt("SpawnZ", 0)
    .buildAndSave(new File("world.dat"), Compression_Types.GZIP);

// Load, modify, and save
NBTBuilder.fromFile(new File("player.dat"))
    .addString("LastLogin", "2024-01-01")
    .addInt("NewScore", 1000)
    .buildAndSave(new File("player.dat"));
```

### List Building

```java
// Create list of coordinates
ITag<?> coordinatesList = NBTBuilder.list("Coordinates", NBTTags.Tag_Compound)
    .addCompound("spawn")
        .addInt("x", 0)
        .addInt("y", 64) 
        .addInt("z", 0)
    .end()
    .addCompound("home")
        .addInt("x", 100)
        .addInt("y", 70)
        .addInt("z", -50) 
    .end()
    .build();
```

## Tag Types Reference

### NBTTags Enum

All supported NBT tag types with their official IDs:

```java
NBTTags.Tag_End         // 0  - End marker
NBTTags.Tag_Byte        // 1  - 8-bit signed integer
NBTTags.Tag_Short       // 2  - 16-bit signed integer
NBTTags.Tag_Int         // 3  - 32-bit signed integer
NBTTags.Tag_Long        // 4  - 64-bit signed integer
NBTTags.Tag_Float       // 5  - 32-bit floating point
NBTTags.Tag_Double      // 6  - 64-bit floating point
NBTTags.Tag_Byte_Array  // 7  - Array of unsigned bytes
NBTTags.Tag_String      // 8  - UTF-8 encoded string
NBTTags.Tag_List        // 9  - Ordered same-type collection
NBTTags.Tag_Compound    // 10 - Named heterogeneous collection
NBTTags.Tag_Int_Array   // 11 - Array of 32-bit integers
NBTTags.Tag_Long_Array  // 12 - Array of 64-bit integers
```

### Type Usage

```java
// Using enum for list creation
IListTag stringList = NBTFactory.createList("strings", NBTTags.Tag_String.getId());
IListTag compoundList = NBTFactory.createList("data", NBTTags.Tag_Compound.getId());

// Type lookup
NBTTags tagType = NBTTags.getById(3); // Returns Tag_Int
int typeId = NBTTags.Tag_String.getId(); // Returns 8
String typeName = NBTTags.Tag_Compound.getName(); // Returns "Tag_Compound"
```

## Compression Support

### Compression_Types Enum

```java
Compression_Types.NONE  // 0 - No compression
Compression_Types.GZIP  // 1 - GZIP (most common)
Compression_Types.ZLIB  // 2 - ZLIB (some MC components)
Compression_Types.LZ4   // 3 - LZ4 (planned)
```

### Usage

```java
// Automatic detection when reading
ICompoundTag data = NBTFileFactory.readNBTFile(new File("level.dat"));

// Explicit compression when writing
NBTFileFactory.writeNBTFile(new File("level.dat"), data, Compression_Types.GZIP);

// Detect compression of existing file
Compression_Types detected = NBTFileHandler.getCompressionType(new File("level.dat"));
```

## Error Handling

### Common Exceptions

```java
try {
    ICompoundTag data = NBTFileFactory.readNBTFile(new File("corrupted.dat"));
} catch (IOException e) {
    // File not found, corrupted, or unsupported format
    System.err.println("Failed to read NBT file: " + e.getMessage());
}

try {
    IListTag list = NBTFactory.createList("items", NBTTags.Tag_String.getId());
    list.addInt("invalid", 42); // Wrong type for string list
} catch (IllegalArgumentException e) {
    // Type mismatch in list operations
    System.err.println("Type error: " + e.getMessage());
}
```

### Validation

```java
// Validate file before processing
File nbtFile = new File("suspicious.dat");
if (NBTFileFactory.isValidNBTFile(nbtFile)) {
    ICompoundTag data = NBTFileFactory.readNBTFile(nbtFile);
    // Safe to process...
} else {
    System.err.println("Invalid or corrupted NBT file");
}

// Check for required tags
ICompoundTag playerData = loadPlayerData();
if (!playerData.hasTag("Name")) {
    throw new IllegalStateException("Player data missing required Name field");
}
```

## Best Practices

### Resource Management

```java
// GOOD: Use try-with-resources
try (INBTReader reader = NBTFileFactory.createReader(file)) {
    return reader.read();
}

// GOOD: Use convenience methods that handle resources
return NBTFileFactory.readNBTFile(file);

// AVOID: Manual resource management
INBTReader reader = NBTFileFactory.createReader(file);
ICompoundTag data = reader.read();
reader.close(); // Easy to forget!
```

### Type Safety

```java
// GOOD: Use type-safe getters
String name = compound.getString("name");
int level = compound.getInt("level");

// GOOD: Check before casting
ITag<?> tag = compound.getTag("data");
if (tag instanceof ICompoundTag childCompound) {
    // Safe to use childCompound
}

// AVOID: Direct casting without checks
ICompoundTag child = (ICompoundTag) compound.getTag("data"); // May throw ClassCastException
```

### List Type Consistency

```java
// GOOD: Create list with correct type
IListTag inventory = NBTFactory.createList("Inventory", NBTTags.Tag_Compound.getId());
inventory.addCompound("item1");
inventory.addCompound("item2");

// AVOID: Mixing types in lists
IListTag badList = NBTFactory.createList("Mixed", NBTTags.Tag_String.getId());
badList.addString("item1", "sword");
badList.addInt("item2", 42); // IllegalArgumentException!
```

### Builder Pattern Best Practices

```java
// GOOD: Proper nesting with end() calls
NBTBuilder.compound("root")
    .addString("name", "test")
    .addList("items", NBTTags.Tag_Compound)
        .addCompound("item1")
            .addString("type", "sword")
        .end() // Close compound
    .end() // Close list
    .build(); // Build root

// AVOID: Missing end() calls will cause compilation errors
```

## Examples
Explore the `examples/` folder for comprehensive usage patterns.