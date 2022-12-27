package me.paulferlitz;

public class Constants
{
    public enum NBTTags {
        Tag_End(0),
        Tag_Byte(1),
        Tag_Short(2),
        Tag_Int(3),
        Tag_Long(4),
        Tag_Float(5),
        Tag_Double(6),
        Tag_Byte_Array(7),
        Tag_String(8),
        Tag_List(9),
        Tag_Compound(10),
        Tag_Int_Array(11),
        Tag_Long_Array(12);

        public final int id;

        NBTTags(int id)
        {
            this.id = id;
        }

        public static NBTTags getById(int id)
        {
            for (NBTTags tag : NBTTags.values())
            {
                if (tag.id == id) return tag;
            }
            return null;
        }
    }
}
