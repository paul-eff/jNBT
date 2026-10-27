package de.pauleff.jnbt.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CompoundBoundaryTest
{
    private static Tag_Compound repeatedNames()
    {
        Tag_Compound root = new Tag_Compound("");
        Tag_Compound a = new Tag_Compound("a");
        a.addInt("x", 1);
        Tag_Compound b = new Tag_Compound("b");
        b.addInt("x", 2);
        root.addTag(a);
        root.addTag(b);
        root.addInt("x", 3);
        return root;
    }

    private static Tag_Compound values(String name, int value)
    {
        Tag_Compound compound = new Tag_Compound(name);
        compound.addString("string", Integer.toString(value));
        compound.addInt("integer", value);
        compound.addDouble("double", value);
        compound.addFloat("float", value);
        compound.addByte("byte", (byte) value);
        compound.addShort("short", (short) value);
        compound.addLong("long", value);
        compound.addByteArray("bytes", new byte[]{(byte) value});
        compound.addIntArray("ints", new int[]{value});
        compound.addLongArray("longs", new long[]{value});
        Tag_Compound child = new Tag_Compound("compound");
        child.addInt("value", value);
        compound.addTag(child);
        Tag_List list = new Tag_List("list", 3);
        list.addInt("", value);
        compound.addTag(list);
        return compound;
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void gettersUseDirectChildrenRegardlessOfOrder(boolean directFirst)
    {
        Tag_Compound root = values("", 3);
        Tag_Compound nested = values("nested", 1);
        var children = root.getData();
        children.add(directFirst ? root.size() : 0, nested);
        root.setData(children);
        assertValues(root, 3);
        assertValues(nested, 1);
        assertTrue(root.hasTag("integer"));
        assertEquals(3, root.getTag("integer").getData());
    }

    @Test
    void descendantsDoNotSatisfyMissingOrWrongTypeLookups()
    {
        Tag_Compound root = new Tag_Compound("");
        root.addTag(values("nested", 1));
        assertFalse(root.hasTag("integer"));
        assertNull(root.getTag("integer"));
        assertNull(root.getString("string"));
        assertNull(root.getCompound("compound"));
        assertNull(root.getList("list"));
        assertEquals(0, root.getInt("integer"));
        root.addString("integer", "wrong type");
        assertEquals(0, root.getInt("integer"));
        root.removeTag("string");
        assertEquals("1", root.getCompound("nested").getString("string"));
    }

    private static Stream<Consumer<Tag_Compound>> updates()
    {
        return Stream.of(c -> c.setString("x", "new"), c -> c.setInt("x", 9),
                c -> c.setDouble("x", 9), c -> c.setFloat("x", 9),
                c -> c.setByte("x", (byte) 9), c -> c.setShort("x", (short) 9),
                c -> c.setLong("x", 9), c -> c.setByteArray("x", new byte[]{9}),
                c -> c.setIntArray("x", new int[]{9}), c -> c.setLongArray("x", new long[]{9}),
                c -> c.setTag(new Tag_Int("x", 9)), c -> c.setTag(new Tag_Compound("x")),
                c -> c.setTag(new Tag_List("x", 3)));
    }

    @ParameterizedTest
    @MethodSource("updates")
    void updatesPreserveMatchingDescendants(Consumer<Tag_Compound> update)
    {
        Tag_Compound root = repeatedNames();
        Tag<?> original = root.getData().getLast();
        update.accept(root);
        assertNotSame(original, root.getTag("x"));
        assertEquals(3, root.size());
        assertEquals(1, root.getCompound("a").getInt("x"));
        assertEquals(2, root.getCompound("b").getInt("x"));
    }

    private static Stream<Consumer<Tag_Compound>> removals()
    {
        return Stream.of(c -> c.removeTag("x"), c -> c.removeTags("x", "missing"));
    }

    @ParameterizedTest
    @MethodSource("removals")
    void removalsPreserveMatchingDescendants(Consumer<Tag_Compound> remove)
    {
        Tag_Compound root = repeatedNames();
        remove.accept(root);
        assertFalse(root.hasTag("x"));
        assertEquals(2, root.size());
        assertEquals(1, root.getCompound("a").getInt("x"));
        assertEquals(2, root.getCompound("b").getInt("x"));
    }

    @Test
    void explicitRecursiveSearchAndRemovalRemainAvailable()
    {
        Tag_Compound root = repeatedNames();
        assertEquals(1, root.getTagByName("x").getData());
        assertEquals(3, root.getAllTagsByName("x").size());
        root.removeAllTagsByName("x");
        assertTrue(root.getAllTagsByName("x").isEmpty());
    }

    private static void assertValues(Tag_Compound compound, int value)
    {
        assertEquals(Integer.toString(value), compound.getString("string"));
        assertEquals(value, compound.getInt("integer"));
        assertEquals((double) value, compound.getDouble("double"));
        assertEquals((float) value, compound.getFloat("float"));
        assertEquals((byte) value, compound.getByte("byte"));
        assertEquals((short) value, compound.getShort("short"));
        assertEquals((long) value, compound.getLong("long"));
        assertArrayEquals(new byte[]{(byte) value}, compound.getByteArray("bytes"));
        assertArrayEquals(new int[]{value}, compound.getIntArray("ints"));
        assertArrayEquals(new long[]{value}, compound.getLongArray("longs"));
        assertEquals(value, compound.getCompound("compound").getInt("value"));
        assertEquals(value, compound.getList("list").get(0).getData());
    }
}
