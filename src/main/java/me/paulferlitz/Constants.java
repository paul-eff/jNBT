package me.paulferlitz;

public class Constants
{
    public enum NBTTags
    {
        Tag_End(0, "Tag_End"),
        Tag_Byte(1, "Tag_Byte"),
        Tag_Short(2, "Tag_Short"),
        Tag_Int(3, "Tag_Int"),
        Tag_Long(4, "Tag_Long"),
        Tag_Float(5, ""),
        Tag_Double(6, "Tag_Double"),
        Tag_Byte_Array(7, "Tag_Byte_Array"),
        Tag_String(8, "Tag_String"),
        Tag_List(9, "Tag_List"),
        Tag_Compound(10, "Tag_Compound"),
        Tag_Int_Array(11, "Tag_Int_Array"),
        Tag_Long_Array(12, "Tag_Long_Array");

        private final int id;
        private final String name;

        NBTTags(int id, String name)
        {
            this.id = id;
            this.name = name;
        }

        public int getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
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
