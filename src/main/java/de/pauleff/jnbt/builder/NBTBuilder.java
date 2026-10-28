package de.pauleff.jnbt.builder;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.api.NBTFileFactory;
import de.pauleff.jnbt.core.*;
import de.pauleff.jnbt.formats.binary.Compression_Types;
import de.pauleff.jnbt.util.NBTTags;

/**
 * Builds NBT tags with chained method calls. Names cannot be null. Empty names and whitespace are
 * kept. Call {@link #build()} on root builders and {@link #end()} on nested builders. Binary NBT does
 * not store list element names.
 *
 * @since 1.3
 */
public abstract class NBTBuilder
{
    protected String name;
    protected NBTBuilder parent;

    /**
     * Creates a builder with an optional parent.
     *
     * @param name   tag name
     * @param parent parent builder, null for root builders
     */
    protected NBTBuilder(String name, NBTBuilder parent)
    {
        this.name = name;
        this.parent = parent;
    }

    /**
     * Creates a compound builder.
     *
     * @param name compound tag name
     * @return new {@link CompoundBuilder}
     */
    public static CompoundBuilder compound(String name)
    {
        return new CompoundBuilder(name, null);
    }

    /**
     * Creates a list builder with a fixed element type.
     *
     * @param name     list tag name
     * @param listType type constraint for all list elements
     * @return new {@link ListBuilder}
     */
    public static ListBuilder list(String name, NBTTags listType)
    {
        return new ListBuilder(name, listType, null);
    }

    /**
     * Creates a string tag.
     *
     * @param name  tag name
     * @param value string content
     * @return {@link Tag_String} as {@link ITag}
     */
    public static ITag<String> string(String name, String value)
    {
        validateName(name);
        return new Tag_String(name, value);
    }

    /**
     * Creates an integer tag.
     *
     * @param name  tag name
     * @param value integer content
     * @return {@link Tag_Int} as {@link ITag}
     */
    public static ITag<Integer> integer(String name, int value)
    {
        validateName(name);
        return new Tag_Int(name, value);
    }

    /**
     * Creates a double tag.
     *
     * @param name  tag name
     * @param value double content
     * @return {@link Tag_Double} as {@link ITag}
     */
    public static ITag<Double> doubleTag(String name, double value)
    {
        validateName(name);
        return new Tag_Double(name, value);
    }

    /**
     * Creates a float tag.
     *
     * @param name  tag name
     * @param value float content
     * @return {@link Tag_Float} as {@link ITag}
     */
    public static ITag<Float> floatTag(String name, float value)
    {
        validateName(name);
        return new Tag_Float(name, value);
    }

    /**
     * Creates a byte tag.
     *
     * @param name  tag name
     * @param value byte content
     * @return {@link Tag_Byte} as {@link ITag}
     */
    public static ITag<Byte> byteTag(String name, byte value)
    {
        validateName(name);
        return new Tag_Byte(name, value);
    }

    /**
     * Creates a short integer tag.
     *
     * @param name  tag name
     * @param value short content
     * @return {@link Tag_Short} as {@link ITag}
     */
    public static ITag<Short> shortTag(String name, short value)
    {
        validateName(name);
        return new Tag_Short(name, value);
    }

    /**
     * Creates a long integer tag.
     *
     * @param name  tag name
     * @param value long content
     * @return {@link Tag_Long} as {@link ITag}
     */
    public static ITag<Long> longTag(String name, long value)
    {
        validateName(name);
        return new Tag_Long(name, value);
    }

    /**
     * Rejects null builder names while allowing empty strings and whitespace.
     *
     * @param name candidate tag name
     * @throws IllegalArgumentException if name is null
     */
    protected static void validateName(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Tag name cannot be null");
        }
    }

    /**
     * Reads a file into a compound builder.
     *
     * @param file the file to read
     * @return a {@link CompoundBuilder} containing the loaded data
     * @throws java.io.IOException if the file cannot be read
     */
    public static CompoundBuilder fromFile(java.io.File file) throws java.io.IOException
    {
        ICompoundTag loadedData = NBTFileFactory.readNBTFile(file);
        CompoundBuilder builder = new CompoundBuilder(loadedData.getName(), null);

        if (loadedData instanceof Tag_Compound concrete)
        {
            for (Tag<?> tag : concrete.getData())
            {
                builder.addTag(tag);
            }
        }

        return builder;
    }

    /**
     * Creates a builder with the compound's children. Child objects are shared.
     *
     * @param compoundTag the {@link ICompoundTag} to load data from
     * @return a {@link CompoundBuilder} containing the loaded data
     */
    public static CompoundBuilder fromCompound(ICompoundTag compoundTag)
    {
        CompoundBuilder builder = new CompoundBuilder(compoundTag.getName(), null);

        if (compoundTag instanceof Tag_Compound concrete)
        {
            for (Tag<?> tag : concrete.getData())
            {
                builder.addTag(tag);
            }
        }

        return builder;
    }

    /**
     * Returns the built tag. Only root builders can call this method.
     *
     * @return the built tag
     * @throws IllegalStateException if called on a nested builder. Call {@link #end()} first.
     */
    public abstract ITag<?> build();

    /**
     * Adds this tag to its parent and returns the parent builder.
     *
     * @return parent builder for continued chaining
     * @throws IllegalStateException if called on a root builder. Call {@link #build()} instead.
     */
    public abstract NBTBuilder end();

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
     * Returns the parent builder, or null for a root builder.
     *
     * @return parent builder, null for root builders
     */
    public NBTBuilder getParent()
    {
        return parent;
    }

    /**
     * Builds and saves a root compound. Nested builders and root lists are rejected.
     *
     * @param file the file to write to
     * @throws java.io.IOException   if writing fails
     * @throws IllegalStateException if this is not a root builder
     */
    public void buildAndSave(java.io.File file) throws java.io.IOException
    {
        if (parent != null)
        {
            throw new IllegalStateException("buildAndSave() can only be called on root builders");
        }

        ITag<?> result = build();
        if (result instanceof ICompoundTag compoundTag)
        {
            NBTFileFactory.writeNBTFile(file, compoundTag);
        } else
        {
            throw new IllegalStateException("Root NBT structure must be a compound tag");
        }
    }

    /**
     * Builds and saves a root compound. Nested builders and root lists are rejected.
     *
     * @param file        the file to write to
     * @param compression the {@link Compression_Types} to use
     * @throws java.io.IOException   if writing fails
     * @throws IllegalStateException if this is not a root builder
     */
    public void buildAndSave(java.io.File file, Compression_Types compression) throws java.io.IOException
    {
        if (parent != null)
        {
            throw new IllegalStateException("buildAndSave() can only be called on root builders");
        }

        ITag<?> result = build();
        if (result instanceof ICompoundTag compoundTag)
        {
            NBTFileFactory.writeNBTFile(file, compoundTag, compression);
        } else
        {
            throw new IllegalStateException("Root NBT structure must be a compound tag");
        }
    }
}
