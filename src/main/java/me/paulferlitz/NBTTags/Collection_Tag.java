package me.paulferlitz.NBTTags;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Collection_Tag extends Tag<ArrayList<Tag<?>>>
{
    /**
     * Create a NBT tag with ID.
     *
     * @param id ID of the tag.
     */
    public Collection_Tag(int id)
    {
        super(id);
    }

    /**
     * Create a NBT tag with ID and name and data set to null.
     *
     * @param id   ID of the tag.
     * @param name Name of the tag.
     */
    public Collection_Tag(int id, String name)
    {
        super(id, name);
    }

    /**
     * Create a NBT tag with ID, name and data.
     *
     * @param id   ID of the tag.
     * @param name Name of the tag. Can be null or empty.
     * @param data The initial data of the tag.
     */
    public Collection_Tag(int id, String name, ArrayList<Tag<?>> data)
    {
        super(id, name, data);
    }

    /*
     * ========== ADDERS ==========
     */
    /**
     * Method for adding another tag to the current tag.
     *
     * @param tag Tag to add.
     */
    public void addTag(Tag<?> tag) {
        addAllTags(List.of(tag));
    }

    /**
     * Method for adding more tags to the current tag.
     *
     * @param tags List of tags to add.
     */
    public void addAllTags(List<Tag<?>> tags) {
        if (this instanceof Tag_List list)
        {
            for (Tag<?> tag : tags) {
                if (tag.getId() != list.getListTypeID()) {
                    throw new IllegalArgumentException(String.format("Mixed datatypes!. Expected %d but got %d", this.getId(), tag.getId()));
                }
            }
        }
        this.getData().addAll(tags);
    }

    /*
     * ========== GETTERS VIA NAME ==========
     */
    /**
     * Method for finding all tags with the given name.
     *
     * @param name Name of the tag(s) to find.
     */
    public List<Tag<?>> getAllTagsByName(String name) {
        return getTagsByName(name, true);
    }

    /**
     * Method for finding only the first tag with the given name.
     *
     * @param name Name of the tag to find.
     */
    public Tag<?> getTagByName(String name) {
        return getTagsByName(name, false).stream().findFirst().orElse(null);
    }

    /**
     * Method for removing tags with a given name.
     *
     * @param name Name of the tag(s) to remove.
     * @param global Whether to remove all tags with the given name or just the first one found.
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
     * Method for finding all tags which match the given tag.
     *
     * @param targetTag The tag to find.
     */
    public List<Tag<?>> getAllTags(Tag<?> targetTag) {
        return getTags(targetTag, true);
    }

    /**
     * Method for finding only the first tag which matches the given tag.
     *
     * @param targetTag The tag to find.
     */
    public Tag<?> getTag(Tag<?> targetTag) {
        return getTags(targetTag, false).stream().findFirst().orElse(null);
    }

    /**
     * Method for finding tags which match the given tag.
     *
     * @param targetTag The tag to find.
     * @param global Whether to find all tags which match the given tag or just the first.
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
     * Method for removing all tags with the given name.
     *
     * @param name Name of the tag(s) to remove.
     */
    public void removeAllTagsByName(String name) {
        removeTagsByName(name, true);
    }

    /**
     * Method for removing only the first tag found with the given name.
     *
     * @param name Name of the tag to remove.
     */
    public void removeTagByName(String name) {
        removeTagsByName(name, false);
    }

    /**
     * Method for removing tags with a given name.
     *
     * @param name Name of the tag(s) to remove.
     * @param global Whether to remove all tags with the given name or just the first one found.
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
     * Method for removing all tags which match the given tag.
     *
     * @param targetTag The tag to remove.
     */
    public void removeAllTags(Tag<?> targetTag) {
        removeTags(targetTag, true);
    }

    /**
     * Method for removing only the first tag found which matches the given tag.
     *
     * @param targetTag The tag to remove.
     */
    public void removeTag(Tag<?> targetTag) {
        removeTags(targetTag, false);
    }

    /**
     * Method for removing tags which match a given tag
     *
     * @param targetTag The tag to remove.
     * @param global Whether to remove all tags with the given name or just the first one found.
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
