package de.pauleff.jnbt.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TagNameTest
{
    @ParameterizedTest
    @ValueSource(strings = {"", "null", " ", "  spaced name  ", "\t"})
    void preservesExplicitNames(String name)
    {
        assertEquals(name, new Tag_Int(name).getName());
        assertEquals(name, new Tag_Int(name, 1).getName());

        Tag_Int tag = new Tag_Int("original", 1);
        tag.setName(name);
        assertEquals(name, tag.getName());

        tag.editTag(new Tag_Int(name, 2));
        assertEquals(name, tag.getName());
        assertEquals(2, tag.getData());
    }

    @Test
    void retainsLegacyNullAndDefaultNamePolicy()
    {
        assertEquals("null", new Tag_Int().getName());
        assertEquals("null", new Tag_Int(null).getName());
        assertEquals("null", new Tag_Int(null, 1).getName());

        Tag_Int tag = new Tag_Int("", 1);
        tag.setName(null);
        assertEquals("null", tag.getName());
    }
}
