package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.core.Tag_List;
import de.pauleff.jnbt.core.Tag_String;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static de.pauleff.jnbt.util.NBTTags.*;
import static org.junit.jupiter.api.Assertions.*;

class BinaryListTypeTest
{
    private static byte[] nestedListsFixture() throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes))
        {
            out.writeByte(Tag_Compound.getId());
            out.writeUTF("");

            // Named outer list containing two lists.
            out.writeByte(Tag_List.getId());
            out.writeUTF("lists");
            out.writeByte(Tag_List.getId());
            out.writeInt(2);

            // First inner list: [42]. List elements have no name header.
            out.writeByte(Tag_Int.getId());
            out.writeInt(1);
            out.writeInt(42);

            // Second inner list: ["x"].
            out.writeByte(Tag_String.getId());
            out.writeInt(1);
            out.writeUTF("x");

            out.writeByte(Tag_End.getId());
        }
        return bytes.toByteArray();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 3, 99})
    void rejectsCorruptSubclassBeforeOutput(int declaredType)
    {
        Tag_List corrupt = new Tag_List("list", 3)
        {
            { data.add(new Tag_String("", "wrong")); }

            @Override
            public int getListTypeID() { return declaredType; }
        };
        Tag_Compound root = new Tag_Compound("");
        root.addInt("before", 1);
        root.addTag(corrupt);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assertThrows(IOException.class, () -> new NBTWriter(new DataOutputStream(bytes)).write(root));
        assertEquals(0, bytes.size());
    }

    @Test
    void nestedListsMayHaveDifferentInnerTypes() throws IOException
    {
        byte[] fixture = nestedListsFixture();
        ICompoundTag root = new NBTReader(new DataInputStream(new ByteArrayInputStream(fixture))).read();
        IListTag lists = root.getList("lists");
        assertEquals(2, lists.size());
        Tag_List integers = assertInstanceOf(Tag_List.class, lists.get(0));
        Tag_List strings = assertInstanceOf(Tag_List.class, lists.get(1));
        assertEquals(Tag_Int.getId(), integers.getListTypeID());
        assertEquals(42, integers.get(0).getData());
        assertEquals(Tag_String.getId(), strings.getListTypeID());
        assertEquals("x", strings.get(0).getData());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(root);
        assertArrayEquals(fixture, bytes.toByteArray());
    }
}
