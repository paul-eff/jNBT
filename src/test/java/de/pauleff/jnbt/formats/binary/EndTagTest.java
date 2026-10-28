package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EndTagTest
{
    private static byte[] listFixture(int type, int size) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes))
        {
            out.writeByte(10);
            out.writeUTF("");
            out.writeByte(9);
            out.writeUTF("list");
            out.writeByte(type);
            out.writeInt(size);
            out.writeByte(3);
            out.writeUTF("after");
            out.writeInt(42);
            out.writeByte(0);
        }
        return bytes.toByteArray();
    }

    private static Stream<Collection_Tag> invalidContainers()
    {
        Collection_Tag child = withUncheckedChild(new Tag_Compound("child"), new Tag_End());
        return Stream.of(
                withUncheckedChild(new Tag_Compound("compound"), new Tag_End()),
                withUncheckedChild(new Tag_List("endList", 0), new Tag_End()),
                withUncheckedChild(new Tag_List("intList", 3), new Tag_End()),
                withUncheckedChild(new Tag_List("nested", 10), child),
                withUncheckedChild(new Tag_List("wrongPayload", 0), new Tag_Int("", 1)));
    }

    private static Collection_Tag withUncheckedChild(Collection_Tag parent, Tag<?> child)
    {
        if (parent instanceof Tag_List list)
        {
            // Deliberately corrupt subclass storage to exercise the writer's final checks.
            return new Tag_List(list.getName(), list.getListTypeID())
            {
                {
                    data.add(child);
                }
            };
        }
        return new Tag_Compound(parent.getName())
        {
            {
                data.add(child);
            }
        };
    }

    private static ICompoundTag read(byte[] bytes) throws IOException
    {
        return new NBTReader(new DataInputStream(new ByteArrayInputStream(bytes))).read();
    }

    @Test
    void allEndConstructorsUseTypeZero()
    {
        assertEquals(0, new Tag_End().getId());
        assertEquals(0, new Tag_End("end").getId());
        assertEquals(0, new Tag_End("end", 1.0).getId());
    }

    @Test
    void rejectsEndChildrenWithoutChangingCompound()
    {
        Tag_Compound root = new Tag_Compound("");
        root.addInt("x", 42);
        assertThrows(IllegalArgumentException.class, () -> root.addTag(new Tag_End()));
        assertThrows(IllegalArgumentException.class, () -> root.setTag(new Tag_End("x")));
        assertThrows(IllegalArgumentException.class,
                () -> root.addAllTags(List.of(new Tag_Int("other", 1), new Tag_End())));
        assertEquals(1, root.getData().size());
        assertEquals(42, root.getInt("x"));
        assertThrows(IllegalArgumentException.class,
                () -> new Tag_Compound("", new ArrayList<>(List.of(new Tag_End()))));
    }

    @Test
    void rejectsAddingEndElementsToLists()
    {
        Tag_List list = new Tag_List("list", 0);
        assertThrows(IllegalArgumentException.class, () -> list.addTag(new Tag_End()));
        assertThrows(IllegalArgumentException.class, () -> list.addAllTags(List.of(new Tag_End())));
        assertThrows(IllegalArgumentException.class,
                () -> new Tag_List("list", 0, new ArrayList<>(List.of(new Tag_End()))));
        assertThrows(IllegalArgumentException.class,
                () -> new Tag_List("list", 0, new ArrayList<>(List.of(new Tag_Int("", 1)))));
        assertTrue(list.getData().isEmpty());
        Tag_List integers = new Tag_List("ints", 3);
        integers.addInt("", 1);
        assertThrows(IllegalArgumentException.class, () -> integers.setTag(0, new Tag_End()));
        assertInstanceOf(Tag_Int.class, integers.getData().getFirst());
    }

    @ParameterizedTest
    @MethodSource("invalidContainers")
    void writerRejectsInvalidContainersBeforeOutput(Collection_Tag container)
    {
        Tag_Compound root = new Tag_Compound("");
        root.addInt("before", 1);
        root.addTag(container);
        root.addInt("after", 2);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        IOException error = assertThrows(IOException.class,
                () -> new NBTWriter(new DataOutputStream(bytes)).write(root));
        assertTrue(error.getMessage().contains("TAG_End"));
        assertEquals(0, bytes.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void rejectsNonemptyEndTypedLists(int size) throws IOException
    {
        byte[] fixture = listFixture(0, size);
        IOException error = assertThrows(IOException.class, () -> read(fixture));
        assertTrue(error.getMessage().contains("TAG_End"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void preservesEmptyListAndFollowingChild(int type) throws IOException
    {
        byte[] fixture = listFixture(type, 0);
        ICompoundTag root = read(fixture);
        assertTrue(root.getList("list").getData().isEmpty());
        assertEquals(42, root.getInt("after"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(root);
        assertArrayEquals(fixture, bytes.toByteArray());
    }
}
