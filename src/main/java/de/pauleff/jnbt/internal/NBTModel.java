package de.pauleff.jnbt.internal;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.core.Tag;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.core.Tag_List;

/**
 * Checks the supported implementation hierarchy at public tag boundaries.
 *
 * @hidden
 */
public final class NBTModel
{
    private NBTModel()
    {
    }

    /**
     * Requires a core tag, with the concrete collection hierarchy for container IDs.
     *
     * @param value the proposed tag
     * @return the supported tag
     * @throws IllegalArgumentException if the implementation is unsupported or null
     */
    public static Tag<?> requireTag(Object value)
    {
        if (!(value instanceof Tag<?> tag))
            throw new IllegalArgumentException("Tag must extend de.pauleff.jnbt.core.Tag");
        if (tag.getId() == 9 && !(tag instanceof Tag_List))
            throw new IllegalArgumentException("List tag must extend de.pauleff.jnbt.core.Tag_List");
        if (tag.getId() == 10 && !(tag instanceof Tag_Compound))
            throw new IllegalArgumentException("Compound tag must extend de.pauleff.jnbt.core.Tag_Compound");
        return tag;
    }

    /**
     * Requires the core compound implementation before accessing or copying its contents.
     *
     * @param value the proposed compound
     * @return the supported compound
     * @throws IllegalArgumentException if the implementation is unsupported or null
     */
    public static Tag_Compound requireCompound(ICompoundTag value)
    {
        if (!(value instanceof Tag_Compound compound))
            throw new IllegalArgumentException("Compound tag must extend de.pauleff.jnbt.core.Tag_Compound");
        return compound;
    }
}
