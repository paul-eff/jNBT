package de.pauleff.jnbt.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TagListTest
{
    private static Stream<Consumer<Tag_List>> typedSetters()
    {
        return Stream.of(l -> l.setString(0, "", "x"), l -> l.setInt(0, "", 1),
                l -> l.setDouble(0, "", 1), l -> l.setCompound(0, ""),
                l -> l.setFloat(0, "", 1), l -> l.setByte(0, "", (byte) 1),
                l -> l.setShort(0, "", (short) 1), l -> l.setLong(0, "", 1));
    }

    @ParameterizedTest
    @MethodSource("typedSetters")
    void typedSettersRejectWrongTypeWithoutMutation(Consumer<Tag_List> setter)
    {
        Tag_Int_Array original = new Tag_Int_Array("original", new int[]{42});
        Tag_List list = new Tag_List("list", 11, new ArrayList<>(List.of(original)));
        assertThrows(IllegalArgumentException.class, () -> setter.accept(list));
        assertSame(original, list.get(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 13, 99})
    void rejectsInvalidDeclaredTypes(int type)
    {
        assertThrows(IllegalArgumentException.class, () -> new Tag_List(type));
        assertThrows(IllegalArgumentException.class, () -> new Tag_List("", type));
        assertThrows(IllegalArgumentException.class, () -> new Tag_List("", type, new ArrayList<>()));
    }

    @Test
    void validatesConstructionAndReplacementData()
    {
        ArrayList<Tag<?>> wrong = new ArrayList<>(List.of(new Tag_String("", "x")));
        assertThrows(IllegalArgumentException.class, () -> new Tag_List("", 3, wrong));
        Tag_List list = new Tag_List("ints", 3);
        list.addInt("", 42);
        assertThrows(IllegalArgumentException.class, () -> list.setData(wrong));
        assertThrows(IllegalArgumentException.class, () -> list.setData(null));
        assertThrows(IllegalArgumentException.class, () -> list.setData(new ArrayList<>(List.of(new Tag_End()))));
        assertEquals(42, list.get(0).getData());
    }

    @Test
    void doesNotExposeOrRetainMutableMembership()
    {
        ArrayList<Tag<?>> supplied = new ArrayList<>(List.of(new Tag_Int("", 1)));
        Tag_List list = new Tag_List("ints", 3, supplied);
        supplied.clear();
        assertEquals(1, list.size());
        ArrayList<Tag<?>> replacement = new ArrayList<>(List.of(new Tag_Int("", 2)));
        list.setData(replacement);
        replacement.add(new Tag_String("", "wrong"));
        ArrayList<Tag<?>> snapshot = list.getData();
        snapshot.clear();
        snapshot.add(new Tag_String("", "wrong"));
        assertEquals(1, list.size());
        assertEquals(2, list.get(0).getData());
    }

    @Test
    void editTagRequiresMatchingDeclaredTypeAndCopiesMembership()
    {
        Tag_List target = new Tag_List("target", 3);
        target.addInt("", 1);
        Tag_List wrong = new Tag_List("wrong", 8);
        wrong.addString("", "x");
        assertThrows(IllegalArgumentException.class, () -> target.editTag(wrong));
        assertThrows(IllegalArgumentException.class, () -> target.editTag(new Tag_Int("", 2)));
        assertEquals("target", target.getName());
        assertEquals(1, target.get(0).getData());

        Tag_List source = new Tag_List("source", 3);
        source.addInt("", 2);
        target.editTag(source);
        source.clear();
        assertEquals("source", target.getName());
        assertEquals(2, target.get(0).getData());
    }

    @Test
    void regularMutationsStillWorkAndBulkAdditionIsAtomic()
    {
        Tag_List list = new Tag_List("ints", 3);
        list.addInt("first", 1);
        list.setInt(0, "first", 2);
        assertEquals(2, list.get(0).getData());
        assertThrows(IllegalArgumentException.class, () -> list.addTag(new Tag_String("", "x")));
        assertThrows(IllegalArgumentException.class, () -> list.setTag(0, new Tag_String("", "x")));
        assertThrows(IllegalArgumentException.class,
                () -> list.addAllTags(List.of(new Tag_Int("second", 3), new Tag_String("", "x"))));
        assertEquals(1, list.size());
        list.addAllTags(List.of(new Tag_Int("second", 3)));
        list.removeTagByName("first");
        assertEquals(3, list.get(0).getData());
        list.removeAt(0);
        assertTrue(list.isEmpty());
    }
}
