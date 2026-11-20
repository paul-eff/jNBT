package de.pauleff.jnbt.core;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.formats.binary.NBTReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ArrayTagEqualityTest
{
    @ParameterizedTest
    @ValueSource(ints = {7, 11, 12})
    void separatelyAllocatedArraysHaveValueEquality(int id)
    {
        Tag<?> first = array(id, "x", -128, 0, 127);
        Tag<?> second = array(id, "x", -128, 0, 127);
        Tag<?> third = array(id, "x", -128, 0, 127);
        assertNotSame(first.getData(), second.getData());
        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(second, third);
        assertEquals(first, third);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(null, first);
        assertNotEquals(first, first.getData());
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 11, 12})
    void emptyArraysCompareByContent(int id)
    {
        Tag<?> first = array(id, "x");
        Tag<?> second = array(id, "x");
        assertNotSame(first.getData(), second.getData());
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, array(id, "x", 0));
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 11, 12})
    void namesLengthsOrderAndValuesStillDistinguishTags(int id)
    {
        Tag<?> tag = array(id, "x", 1, 2, 3);
        assertNotEquals(tag, array(id, "y", 1, 2, 3));
        assertNotEquals(tag, array(id, "x", 1, 2));
        assertNotEquals(tag, array(id, "x", 3, 2, 1));
        assertNotEquals(tag, array(id, "x", 1, 2, 4));
        assertNotEquals(array(id, "", 1), array(id, null, 1));
        for (int other : new int[]{7, 11, 12})
            if (other != id) assertNotEquals(tag, array(other, "x", 1, 2, 3));
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 11, 12})
    void hashCollectionsRecognizeAnEqualArrayTag(int id)
    {
        Tag<?> stored = array(id, "x", 1, 2);
        Tag<?> lookup = array(id, "x", 1, 2);
        HashSet<Tag<?>> set = new HashSet<>();
        assertTrue(set.add(stored));
        assertTrue(set.contains(lookup));
        assertFalse(set.add(lookup));
        assertTrue(set.remove(lookup));
        assertTrue(set.isEmpty());
        HashMap<Tag<?>, String> map = new HashMap<>();
        map.put(stored, "value");
        assertEquals("value", map.get(lookup));
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 11, 12})
    void nestedListsAndCompoundsUseChildArrayContents(int id)
    {
        Tag_Compound first = tree(id);
        Tag_Compound second = tree(id);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(new HashSet<>(List.of(first)).contains(second));
        second.getCompound("branch").getList("items").get(0).setName("different");
        assertNotEquals(first, second);
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 11, 12})
    void recursiveLookupAndRemovalMatchIndependentArrayValues(int id)
    {
        Tag_Compound root = tree(id);
        Tag_Compound sibling = new Tag_Compound("sibling");
        Tag<?> later = array(id, "x", 1, 2, 3);
        Tag<?> different = array(id, "different", 1, 2, 3);
        sibling.addTag(later);
        sibling.addTag(different);
        root.addTag(sibling);
        Tag<?> target = array(id, "x", 1, 2, 3);
        Tag<?> earlier = (Tag<?>) root.getCompound("branch").getList("items").get(0);
        assertSame(earlier, root.getTag(target));
        List<Tag<?>> matches = root.getAllTags(target);
        assertEquals(2, matches.size());
        assertSame(earlier, matches.getFirst());
        assertSame(later, matches.getLast());
        assertSame(root, root.removeTag(target));
        assertSame(later, root.getTag(target));
        root.removeAllTags(target);
        assertNull(root.getTag(target));
        assertSame(different, sibling.getTag("different"));
        assertEquals(2, root.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 11, 12})
    void listRemovalByValueKeepsDifferentArrays(int id)
    {
        Tag_List list = new Tag_List("items", id);
        Tag<?> first = array(id, "x", 1);
        Tag<?> different = array(id, "x", 2);
        Tag<?> last = array(id, "x", 1);
        list.addTag(first).addTag(different).addTag(last);
        list.removeTag(array(id, "x", 1));
        assertSame(different, list.get(0));
        assertSame(last, list.get(1));
        list.removeAllTags(array(id, "x", 1));
        assertEquals(1, list.size());
        assertSame(different, list.get(0));
    }

    @Test
    void equalityAndHashesReflectCurrentSharedArrayContents()
    {
        byte[] bytes = {1};
        int[] ints = {1};
        long[] longs = {1};
        Tag<?>[] tags = {new Tag_Byte_Array("x", bytes), new Tag_Int_Array("x", ints), new Tag_Long_Array("x", longs)};
        assertSame(bytes, tags[0].getData());
        assertSame(ints, tags[1].getData());
        assertSame(longs, tags[2].getData());
        for (Tag<?> tag : tags)
        {
            assertEquals(array(tag.getId(), "x", 1), tag);
            assertEquals(array(tag.getId(), "x", 1).hashCode(), tag.hashCode());
        }
        bytes[0] = 2;
        ints[0] = 2;
        longs[0] = 2;
        for (Tag<?> tag : tags)
        {
            Tag<?> expected = array(tag.getId(), "x", 2);
            assertEquals(expected, tag);
            assertEquals(expected.hashCode(), tag.hashCode());
            assertNotEquals(array(tag.getId(), "x", 1), tag);
        }
    }

    @Test
    void widerArraysCompareAllBits()
    {
        assertNotEquals(new Tag_Int_Array("x", new int[]{1}), new Tag_Int_Array("x", new int[]{257}));
        assertNotEquals(new Tag_Long_Array("x", new long[]{1}), new Tag_Long_Array("x", new long[]{0x100000001L}));
        assertEquals(new Tag_Long_Array("x", new long[]{Long.MIN_VALUE, Long.MAX_VALUE}),
                new Tag_Long_Array("x", new long[]{Long.MIN_VALUE, Long.MAX_VALUE}));
    }

    @Test
    void exactClassChecksRemainSymmetric()
    {
        Tag_Int_Array base = new Tag_Int_Array("x", new int[]{1});
        Tag_Int_Array subclass = new Tag_Int_Array("x", new int[]{1}) {};
        assertNotEquals(base, subclass);
        assertNotEquals(subclass, base);
    }

    static Stream<Arguments> binaryFixtures()
    {
        // Independent binary roots whose array payloads are allocated by the reader.
        return Stream.of(
                Arguments.of(7, "0a0000 07000178 00000003 010203 00"),
                Arguments.of(11, "0a0000 0b000178 00000003 000000010000000200000003 00"),
                Arguments.of(12, "0a0000 0c000178 00000003 000000000000000100000000000000020000000000000003 00"));
    }

    @ParameterizedTest
    @MethodSource("binaryFixtures")
    void independentlyDecodedArraysEqualConstructedValues(int id, String hex) throws Exception
    {
        byte[] bytes = HexFormat.of().parseHex(hex.replace(" ", ""));
        ICompoundTag decoded = new NBTReader(new DataInputStream(new ByteArrayInputStream(bytes))).read();
        Tag_Compound expected = new Tag_Compound("");
        expected.addTag(array(id, "x", 1, 2, 3));
        assertNotSame(expected.getTag("x").getData(), decoded.getTag("x").getData());
        assertEquals(expected, decoded);
        assertEquals(expected.hashCode(), decoded.hashCode());
    }

    private static Tag_Compound tree(int id)
    {
        Tag_Compound root = new Tag_Compound("root");
        Tag_Compound branch = new Tag_Compound("branch");
        Tag_List list = new Tag_List("items", id);
        list.addTag(array(id, "x", 1, 2, 3));
        branch.addTag(list);
        root.addTag(branch);
        return root;
    }

    private static Tag<?> array(int id, String name, long... values)
    {
        return switch (id)
        {
            case 7 ->
            {
                byte[] bytes = new byte[values.length];
                for (int i = 0; i < values.length; i++) bytes[i] = (byte) values[i];
                yield new Tag_Byte_Array(name, bytes);
            }
            case 11 ->
            {
                int[] ints = new int[values.length];
                for (int i = 0; i < values.length; i++) ints[i] = (int) values[i];
                yield new Tag_Int_Array(name, ints);
            }
            case 12 -> new Tag_Long_Array(name, values);
            default -> throw new IllegalArgumentException("Not an array type: " + id);
        };
    }
}
