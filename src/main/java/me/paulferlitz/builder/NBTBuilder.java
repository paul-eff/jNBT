package me.paulferlitz.builder;

import me.paulferlitz.api.ITag;
import me.paulferlitz.core.*;
import me.paulferlitz.util.NBTTags;

/**
 * Fluent factory for creating NBT tag structures with type safety and readable syntax.
 * Provides static factory methods and builder patterns for constructing complex NBT hierarchies.
 *
 * <p>Build NBT structures using method chaining and automatic type validation:</p>
 * <pre>{@code
 * Tag_Compound player = NBTBuilder.compound("Player")
 *     .addString("Name", "Steve")
 *     .addInt("Level", 50)
 *     .addList("Inventory", NBTTags.Tag_Compound)
 *         .addCompound("sword")
 *             .addString("id", "minecraft:diamond_sword")
 *             .addInt("Count", 1)
 *         .end()
 *     .end()
 *     .build();
 * }</pre>
 *
 * <p>Remember to call {@link #build()} on root builders and {@link #end()} on nested ones.</p>
 *
 * @author Paul Ferlitz
 * @see CompoundBuilder
 * @see ListBuilder
 */
public abstract class NBTBuilder
{
    protected String name;
    protected NBTBuilder parent;

    /**
     * Constructs a builder with parent-child relationship for nested structures.
     *
     * @param name tag name
     * @param parent parent builder, null for root builders
     */
    protected NBTBuilder(String name, NBTBuilder parent)
    {
        this.name = name;
        this.parent = parent;
    }

    /*
     * ========== STATIC FACTORY METHODS ==========
     */

    /**
     * Creates a fluent builder for compound tags containing key-value pairs.
     *
     * @param name compound tag name
     * @return new {@link CompoundBuilder}
     */
    public static CompoundBuilder compound(String name)
    {
        return new CompoundBuilder(name, null);
    }

    /**
     * Creates a fluent builder for homogeneous list tags.
     *
     * @param name list tag name
     * @param listType type constraint for all list elements
     * @return new {@link ListBuilder}
     */
    public static ListBuilder list(String name, NBTTags listType)
    {
        return new ListBuilder(name, listType, null);
    }

    /**
     * Creates a string tag with immediate value assignment.
     *
     * @param name tag name
     * @param value string content
     * @return {@link Tag_String} as {@link ITag}
     */
    public static ITag<String> string(String name, String value)
    {
        validateName(name);
        return new Tag_String(name, value);
    }

    /**
     * Creates an integer tag with immediate value assignment.
     *
     * @param name tag name
     * @param value integer content
     * @return {@link Tag_Int} as {@link ITag}
     */
    public static ITag<Integer> integer(String name, int value)
    {
        validateName(name);
        return new Tag_Int(name, value);
    }

    /**
     * Creates a double-precision tag with immediate value assignment.
     *
     * @param name tag name
     * @param value double content
     * @return {@link Tag_Double} as {@link ITag}
     */
    public static ITag<Double> doubleTag(String name, double value)
    {
        validateName(name);
        return new Tag_Double(name, value);
    }

    /**
     * Creates a single-precision tag with immediate value assignment.
     *
     * @param name tag name
     * @param value float content
     * @return {@link Tag_Float} as {@link ITag}
     */
    public static ITag<Float> floatTag(String name, float value)
    {
        validateName(name);
        return new Tag_Float(name, value);
    }

    /**
     * Creates a byte tag with immediate value assignment.
     *
     * @param name tag name
     * @param value byte content
     * @return {@link Tag_Byte} as {@link ITag}
     */
    public static ITag<Byte> byteTag(String name, byte value)
    {
        validateName(name);
        return new Tag_Byte(name, value);
    }

    /**
     * Creates a short integer tag with immediate value assignment.
     *
     * @param name tag name
     * @param value short content
     * @return {@link Tag_Short} as {@link ITag}
     */
    public static ITag<Short> shortTag(String name, short value)
    {
        validateName(name);
        return new Tag_Short(name, value);
    }

    /**
     * Creates a long integer tag with immediate value assignment.
     *
     * @param name tag name
     * @param value long content
     * @return {@link Tag_Long} as {@link ITag}
     */
    public static ITag<Long> longTag(String name, long value)
    {
        validateName(name);
        return new Tag_Long(name, value);
    }

    /*
     * ========== ABSTRACT METHODS ==========
     */

    /**
     * Finalizes construction and returns the completed NBT tag.
     *
     * @return immutable tag structure
     */
    public abstract ITag<?> build();

    /**
     * Closes this nested builder and returns control to its parent.
     *
     * @return parent builder for continued chaining
     * @throws IllegalStateException if called on root builder
     */
    public abstract NBTBuilder end();

    /*
     * ========== VALIDATION METHODS ==========
     */

    /**
     * Ensures tag names meet NBT requirements.
     *
     * @param name candidate tag name
     * @throws IllegalArgumentException if name is null or empty
     */
    protected static void validateName(String name)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException("Tag name cannot be null or empty");
        }
    }

    /**
     * Returns the name that will be assigned to the built tag.
     *
     * @return tag name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the parent builder in the construction hierarchy.
     *
     * @return parent builder, null for root builders
     */
    public NBTBuilder getParent()
    {
        return parent;
    }

    /*
     * ========== I/O INTEGRATION METHODS ==========
     */

    /**
     * Builds the NBT structure and writes it directly to a file.
     * Only available for root builders (builders without parents).
     *
     * @param file The {@link java.io.File} to write to
     * @throws java.io.IOException If writing fails
     * @throws IllegalStateException If this is not a root builder
     */
    public void buildAndSave(java.io.File file) throws java.io.IOException
    {
        if (parent != null)
        {
            throw new IllegalStateException("buildAndSave() can only be called on root builders");
        }
        
        ITag<?> result = build();
        if (result instanceof me.paulferlitz.api.ICompoundTag compoundTag)
        {
            me.paulferlitz.api.NBTFileFactory.writeNBTFile(file, compoundTag);
        }
        else
        {
            throw new IllegalStateException("Root NBT structure must be a compound tag");
        }
    }

    /**
     * Builds the NBT structure and writes it to a file with specified compression.
     * Only available for root builders (builders without parents).
     *
     * @param file The {@link java.io.File} to write to
     * @param compression The {@link me.paulferlitz.formats.binary.Compression_Types} to use
     * @throws java.io.IOException If writing fails
     * @throws IllegalStateException If this is not a root builder
     */
    public void buildAndSave(java.io.File file, me.paulferlitz.formats.binary.Compression_Types compression) throws java.io.IOException
    {
        if (parent != null)
        {
            throw new IllegalStateException("buildAndSave() can only be called on root builders");
        }
        
        ITag<?> result = build();
        if (result instanceof me.paulferlitz.api.ICompoundTag compoundTag)
        {
            me.paulferlitz.api.NBTFileFactory.writeNBTFile(file, compoundTag, compression);
        }
        else
        {
            throw new IllegalStateException("Root NBT structure must be a compound tag");
        }
    }

    /*
     * ========== FACTORY METHODS WITH I/O INTEGRATION ==========
     */

    /**
     * Creates a compound builder pre-loaded with data from an existing NBT file.
     * The builder can then be used to modify the structure before saving.
     *
     * @param file The {@link java.io.File} to load NBT data from
     * @return A {@link CompoundBuilder} containing the loaded data
     * @throws java.io.IOException If the file cannot be read
     */
    public static CompoundBuilder fromFile(java.io.File file) throws java.io.IOException
    {
        me.paulferlitz.api.ICompoundTag loadedData = me.paulferlitz.api.NBTFileFactory.readNBTFile(file);
        CompoundBuilder builder = new CompoundBuilder(loadedData.getName(), null);
        
        // Copy loaded data into the builder
        if (loadedData instanceof me.paulferlitz.core.Tag_Compound concrete)
        {
            for (me.paulferlitz.core.Tag<?> tag : concrete.getData())
            {
                builder.addTag(tag);
            }
        }
        
        return builder;
    }
}