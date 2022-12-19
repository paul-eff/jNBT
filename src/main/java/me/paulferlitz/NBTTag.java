package me.paulferlitz;

/**
 * Class representing all possible NBT Tag types.
 * payloadSize = -1 means the size can vary.
 */
public enum NBTTag
{
    TAG_End(0, 0),
    TAG_Byte(1, 1),
    TAG_Short(2, 2),
    TAG_Int(3, 4),
    TAG_Long(4, 8),
    TAG_Float(5, 4),
    TAG_Double(6, 8),
    TAG_Byte_Array(7, -1),
    TAG_String(8, -1),
    TAG_List(9, -1),
    TAG_Compound(10, -1),
    TAG_Int_Array(11, -1),
    TAG_Long_Array(12, -1);

    private int id;
    private int payloadSize;

    NBTTag(int id, int payloadSize)
    {
        this.id = id;
        this.payloadSize = payloadSize;
    }

    public static NBTTag getById(int id)
    {
        for (NBTTag tag : values())
        {
            if (tag.id == id) return tag;
        }
        throw new EnumConstantNotPresentException(NBTTag.class, String.valueOf(id));
    }

    public int getId()
    {
        return this.id;
    }

    public int getPayloadSize()
    {
        return this.payloadSize;
    }
}
