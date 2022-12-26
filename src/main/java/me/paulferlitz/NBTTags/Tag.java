package me.paulferlitz.NBTTags;

/**
 * Class representing a basic NBT tag.
 *
 * @author Paul Ferlitz
 */
public abstract class Tag
{
    private final int id;
    private final String name;

    /**
     * Create a NBT tag with ID.
     *
     * @param id ID of the tag.
     */
    public Tag(int id)
    {
        this.id = id;
        this.name = "null";
    }

    /**
     * Create a NBT tag with ID and name.
     *
     * @param id ID of the tag.
     * @param name Name of the tag. Can be null or empty.
     */
    public Tag(int id, String name)
    {
        this.id = id;
        if (name == null || name.equals(""))
        {
            this.name = "null";
        }else {
            this.name = name;
        }
    }

    /**
     * Method returing the tag's ID.
     *
     * @return The ID of this tag.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Method returing the tag's name.
     *
     * @return The name of this tag.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Method returning the tag's value.
     *
     * @return The value/playload of the tag.
     */
    public abstract Object getValue();

    /**
     * Method for generating a formatable string representation of the tag.
     *
     * @return String representatin of tag.
     */
    public abstract String toString();
}
