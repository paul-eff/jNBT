package de.pauleff.jnbt.core;

import de.pauleff.jnbt.api.ITag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for collections of child tags. Name and tag searches and removals include nested
 * collections. Null children and END markers are rejected.
 *
 * @since 1.0
 */
public class Collection_Tag extends Tag<ArrayList<Tag<?>>>
{
    /**
     * Creates a collection tag with the NBT type ID. Initializes with an empty ArrayList for child tags.
     *
     * @param id the NBT type ID
     */
    public Collection_Tag(int id)
    {
        super(id, null, new ArrayList<>());
    }

    /**
     * Creates a named collection tag with the NBT type ID. Initializes with an empty ArrayList for child
     * tags.
     *
     * @param id   the NBT type ID
     * @param name the tag name
     */
    public Collection_Tag(int id, String name)
    {
        super(id, name, new ArrayList<>());
    }

    /**
     * Creates a collection tag filled with child tags.
     *
     * @param id   the NBT type ID
     * @param name the tag name (null becomes "null", empty strings are preserved)
     * @param data the initial collection of child tags
     */
    public Collection_Tag(int id, String name, ArrayList<Tag<?>> data)
    {
        super(id, name, data);
        for (Tag<?> tag : data)
        {
            validateChildTag(tag);
        }
    }

    /**
     * Rejects null children and END markers.
     *
     * @param tag the proposed child
     * @throws IllegalArgumentException if the child is null or a TAG_End
     */
    protected static void validateChildTag(ITag<?> tag)
    {
        if (tag == null || tag.getId() == 0)
        {
            throw new IllegalArgumentException("Collection children cannot be null or TAG_End");
        }
    }

    /**
     * Adds a child. List elements must match the declared element type.
     *
     * @param tag the tag to add
     * @return this collection tag
     * @throws IllegalArgumentException if the child is null, is TAG_End, or has an incompatible list type
     */
    public Collection_Tag addTag(ITag<?> tag)
    {
        validateChildTag(tag);
        if (this instanceof Tag_List list && tag.getId() != list.getListTypeID())
        {
            throw new IllegalArgumentException(String.format("Mixed datatypes! Expected %d but got %d", list.getListTypeID(), tag.getId()));
        }
        this.data.add((Tag<?>) tag);
        return this;
    }

    /**
     * Adds children after checking all their types.
     *
     * @param tags the collection of tags to add
     * @return this collection tag
     * @throws IllegalArgumentException if any child is null, is TAG_End, or has an incompatible list type
     */
    public Collection_Tag addAllTags(List<Tag<?>> tags)
    {
        for (Tag<?> tag : tags)
        {
            validateChildTag(tag);
            if (this instanceof Tag_List list)
            {
                if (tag.getId() != list.getListTypeID())
                {
                    throw new IllegalArgumentException(String.format("Mixed datatypes!. Expected %d but got %d", this.getId(), tag.getId()));
                }
            }
        }
        this.data.addAll(tags);
        return this;
    }

    /**
     * Finds all tags with the given name, including nested tags.
     *
     * @param name the tag name to search for
     * @return list of matching tags (empty if none found)
     */
    public List<Tag<?>> getAllTagsByName(String name)
    {
        return getTagsByName(name, true);
    }

    /**
     * Finds a tag with the given name, including nested tags.
     *
     * @param name the tag name to search for
     * @return the first matching tag, or null if not found
     */
    public Tag<?> getTagByName(String name)
    {
        return getTagsByName(name, false).stream().findFirst().orElse(null);
    }

    /**
     * Finds tags by name in nested collections.
     *
     * @param name   the tag name to search for
     * @param global true to find all matches, false to stop at first match
     * @return list of matching tags
     */
    private List<Tag<?>> getTagsByName(String name, boolean global)
    {
        List<Tag<?>> tags = new ArrayList<>();
        for (Tag<?> tag : this.data)
        {
            if (tag.getName().equals(name))
            {
                tags.add(tag);
                if (!global) return tags;
            } else if (tag instanceof Collection_Tag collectionTag)
            {
                tags.addAll(collectionTag.getTagsByName(name, global));
            }
        }
        return tags;
    }

    /**
     * Finds equal tags in this collection and nested collections.
     *
     * @param targetTag the tag to match against
     * @return list of equal tags (empty if none found)
     */
    public List<Tag<?>> getAllTags(Tag<?> targetTag)
    {
        return getTags(targetTag, true);
    }

    /**
     * Finds an equal tag in this collection or nested collections.
     *
     * @param targetTag the tag to match against
     * @return the first equal tag, or null if not found
     */
    public Tag<?> getTag(Tag<?> targetTag)
    {
        return getTags(targetTag, false).stream().findFirst().orElse(null);
    }

    /**
     * Finds equal tags in nested collections.
     *
     * @param targetTag the tag to match against
     * @param global    true to find all matches, false to stop at first match
     * @return list of matching tags
     */
    private List<Tag<?>> getTags(Tag<?> targetTag, boolean global)
    {
        List<Tag<?>> tags = new ArrayList<>();
        for (Tag<?> tag : this.data)
        {
            if (tag.equals(targetTag))
            {
                tags.add(tag);
                if (!global) return tags;
            } else if (tag instanceof Collection_Tag collectionTag)
            {
                tags.addAll(collectionTag.getTags(targetTag, global));
            }
        }
        return tags;
    }

    /**
     * Removes tags with the given name from this collection and nested collections.
     *
     * @param name the tag name to remove
     */
    public void removeAllTagsByName(String name)
    {
        removeTagsByName(name, true);
    }

    /**
     * Removes a matching tag by name from this collection and nested collections.
     *
     * @param name the tag name to remove
     * @return this collection tag
     */
    public Collection_Tag removeTagByName(String name)
    {
        removeTagsByName(name, false);
        return this;
    }

    /**
     * Removes tags by name from nested collections.
     *
     * @param name   the tag name to remove
     * @param global true to remove all matches, false to stop at first removal
     */
    private void removeTagsByName(String name, boolean global)
    {
        Iterator<Tag<?>> iterator = this.data.iterator();
        while (iterator.hasNext())
        {
            Tag<?> tag = iterator.next();
            if (tag.getName().equals(name))
            {
                iterator.remove();
                if (!global) return;
            } else if (tag instanceof Collection_Tag collectionTag)
            {
                collectionTag.removeTagsByName(name, global);
            }
        }
    }

    /**
     * Removes equal tags from this collection and nested collections.
     *
     * @param targetTag the tag to remove
     */
    public void removeAllTags(Tag<?> targetTag)
    {
        removeTags(targetTag, true);
    }

    /**
     * Removes a matching tag from this collection and nested collections.
     *
     * @param targetTag the tag to remove
     * @return this collection tag
     */
    public Collection_Tag removeTag(Tag<?> targetTag)
    {
        removeTags(targetTag, false);
        return this;
    }

    /**
     * Removes equal tags from nested collections.
     *
     * @param targetTag the tag to remove
     * @param global    true to remove all matches, false to stop at first removal
     */
    private void removeTags(Tag<?> targetTag, boolean global)
    {
        Iterator<Tag<?>> iterator = this.data.iterator();
        while (iterator.hasNext())
        {
            Tag<?> tag = iterator.next();
            if (tag.equals(targetTag))
            {
                iterator.remove();
                if (!global) return;
            } else if (tag instanceof Collection_Tag collectionTag)
            {
                collectionTag.removeTags(targetTag, global);
            }
        }
    }
}
