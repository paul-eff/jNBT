package me.paulferlitz.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.paulferlitz.api.ITag;

/**
 * Abstract base class for NBT tags that contain collections of other tags.
 * 
 * <p>Provides comprehensive functionality for managing child tags including
 * addition, removal, and retrieval operations. Supports both name-based and
 * object-based lookups with optional recursive searching.</p>
 * 
 * <p>Implements fluent API patterns for method chaining and offers specialized
 * handling for different collection types (compounds vs. lists).</p>
 *
 * @see Tag_Compound
 * @see Tag_List
 */
public class Collection_Tag extends Tag<ArrayList<Tag<?>>>
{
    /**
     * Creates a collection tag with the specified NBT type identifier.
     *
     * @param id the NBT type identifier
     */
    public Collection_Tag(int id)
    {
        super(id);
    }

    /**
     * Creates a named collection tag with the specified NBT type identifier.
     *
     * @param id the NBT type identifier
     * @param name the tag name
     */
    public Collection_Tag(int id, String name)
    {
        super(id, name);
    }

    /**
     * Creates a collection tag pre-populated with child tags.
     *
     * @param id the NBT type identifier
     * @param name the tag name (null or empty becomes "null")
     * @param data the initial collection of child tags
     */
    public Collection_Tag(int id, String name, ArrayList<Tag<?>> data)
    {
        super(id, name, data);
    }

    /*
     * ========== ADDERS ==========
     */
    /**
     * Adds a child tag to this collection with type validation.
     * 
     * <p>For lists, validates that the new tag matches the declared list type.
     * Compounds accept any tag type.</p>
     *
     * @param tag the tag to add
     * @return this collection tag for method chaining
     * @throws IllegalArgumentException if adding incompatible type to a list
     * @see Tag_List#getListTypeID()
     */
    public Collection_Tag addTag(ITag<?> tag)
    {
        if (this instanceof Tag_List list && tag.getId() != list.getListTypeID())
        {
            throw new IllegalArgumentException(String.format("Mixed datatypes! Expected %d but got %d", list.getListTypeID(), tag.getId()));
        }
        this.getData().add((Tag<?>) tag);
        return this;
    }

    /**
     * Adds multiple child tags to this collection with batch type validation.
     * 
     * <p>For lists, validates that all new tags match the declared list type
     * before adding any elements.</p>
     *
     * @param tags the collection of tags to add
     * @return this collection tag for method chaining
     * @throws IllegalArgumentException if any tag is incompatible with list type
     * @see #addTag(ITag)
     */
    public Collection_Tag addAllTags(List<Tag<?>> tags)
    {
        if (this instanceof Tag_List list)
        {
            for (Tag<?> tag : tags) {
                if (tag.getId() != list.getListTypeID()) {
                    throw new IllegalArgumentException(String.format("Mixed datatypes!. Expected %d but got %d", this.getId(), tag.getId()));
                }
            }
        }
        this.getData().addAll(tags);
        return this;
    }

    /*
     * ========== GETTERS VIA NAME ==========
     */
    /**
     * Finds all tags with the specified name, including nested matches.
     * 
     * <p>Performs recursive search through all collection descendants.</p>
     *
     * @param name the tag name to search for
     * @return list of matching tags (empty if none found)
     * @see #getTagByName(String)
     */
    public List<Tag<?>> getAllTagsByName(String name) {
        return getTagsByName(name, true);
    }

    /**
     * Finds the first tag with the specified name, including nested search.
     * 
     * <p>Returns immediately upon finding the first match during traversal.</p>
     *
     * @param name the tag name to search for
     * @return the first matching tag, or null if not found
     * @see #getAllTagsByName(String)
     */
    public Tag<?> getTagByName(String name) {
        return getTagsByName(name, false).stream().findFirst().orElse(null);
    }

    /**
     * Internal method for flexible tag retrieval by name with scope control.
     *
     * @param name the tag name to search for
     * @param global true to find all matches, false to stop at first match
     * @return list of matching tags
     */
    private List<Tag<?>> getTagsByName(String name, boolean global) {
        List<Tag<?>> tags = new ArrayList<>();
        for (Tag<?> tag : this.getData())
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

    /*
     * ========== GETTERS VIA TAG ==========
     */
    /**
     * Finds all tags that are equal to the specified target tag.
     * 
     * <p>Uses {@link Tag#equals(Object)} for comparison and searches recursively.</p>
     *
     * @param targetTag the tag to match against
     * @return list of equal tags (empty if none found)
     * @see #getTag(Tag)
     */
    public List<Tag<?>> getAllTags(Tag<?> targetTag) {
        return getTags(targetTag, true);
    }

    /**
     * Finds the first tag that equals the specified target tag.
     * 
     * <p>Returns immediately upon finding the first match during traversal.</p>
     *
     * @param targetTag the tag to match against
     * @return the first equal tag, or null if not found
     * @see #getAllTags(Tag)
     */
    public Tag<?> getTag(Tag<?> targetTag) {
        return getTags(targetTag, false).stream().findFirst().orElse(null);
    }

    /**
     * Internal method for flexible tag retrieval by equality with scope control.
     *
     * @param targetTag the tag to match against
     * @param global true to find all matches, false to stop at first match
     * @return list of matching tags
     */
    private List<Tag<?>> getTags(Tag<?> targetTag, boolean global) {
        List<Tag<?>> tags = new ArrayList<>();
        for (Tag<?> tag : this.getData())
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

    /*
     * ========== REMOVERS VIA NAME ==========
     */
    /**
     * Removes all tags with the specified name from this collection and descendants.
     * 
     * <p>Performs recursive removal through all nested collections.</p>
     *
     * @param name the tag name to remove
     * @see #removeTagByName(String)
     */
    public void removeAllTagsByName(String name) {
        removeTagsByName(name, true);
    }

    /**
     * Removes the first tag with the specified name and returns this collection.
     * 
     * <p>Stops after removing the first match found during traversal.</p>
     *
     * @param name the tag name to remove
     * @return this collection tag for method chaining
     * @see #removeAllTagsByName(String)
     */
    public Collection_Tag removeTagByName(String name)
    {
        removeTagsByName(name, false);
        return this;
    }

    /**
     * Internal method for flexible tag removal by name with scope control.
     *
     * @param name the tag name to remove
     * @param global true to remove all matches, false to stop at first removal
     */
    private void removeTagsByName(String name, boolean global) {
        Iterator<Tag<?>> iterator = this.getData().iterator();
        while (iterator.hasNext()) {
            Tag<?> tag = iterator.next();
            if (tag.getName().equals(name))
            {
                iterator.remove();
                if (!global) return;
            }else if (tag instanceof Collection_Tag collectionTag)
            {
                collectionTag.removeTagsByName(name, global);
            }
        }
    }

    /*
     * ========== REMOVERS VIA TAG ==========
     */
    /**
     * Removes all tags equal to the specified target from this collection and descendants.
     * 
     * <p>Uses {@link Tag#equals(Object)} for comparison and removes recursively.</p>
     *
     * @param targetTag the tag to remove
     * @see #removeTag(Tag)
     */
    public void removeAllTags(Tag<?> targetTag) {
        removeTags(targetTag, true);
    }

    /**
     * Removes the first tag equal to the specified target and returns this collection.
     * 
     * <p>Stops after removing the first match found during traversal.</p>
     *
     * @param targetTag the tag to remove
     * @return this collection tag for method chaining
     * @see #removeAllTags(Tag)
     */
    public Collection_Tag removeTag(Tag<?> targetTag)
    {
        removeTags(targetTag, false);
        return this;
    }

    /**
     * Internal method for flexible tag removal by equality with scope control.
     *
     * @param targetTag the tag to remove
     * @param global true to remove all matches, false to stop at first removal
     */
    private void removeTags(Tag<?> targetTag, boolean global) {
        Iterator<Tag<?>> iterator = this.getData().iterator();
        while (iterator.hasNext()) {
            Tag<?> tag = iterator.next();
            if (tag.equals(targetTag))
            {
                iterator.remove();
                if (!global) return;
            }else if (tag instanceof Collection_Tag collectionTag)
            {
                collectionTag.removeTags(targetTag, global);
            }
        }
    }
}
