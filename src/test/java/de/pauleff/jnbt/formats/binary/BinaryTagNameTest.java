package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.core.Tag_Compound;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BinaryTagNameTest
{
    @ParameterizedTest
    @ValueSource(strings = {"", "null", " ", "  spaced name  "})
    void preservesIndependentFixtureAndWritesExpectedBytes(String rootName) throws IOException
    {
        // Construct the wire format independently so reader/writer agreement cannot hide data loss.
        ByteArrayOutputStream fixture = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(fixture))
        {
            out.writeByte(10);
            out.writeUTF(rootName);
            writeInt(out, "", 1);
            writeInt(out, "null", 2);
            writeInt(out, " ", 3);
            out.writeByte(0);
        }

        byte[] expected = fixture.toByteArray();
        ICompoundTag loaded = new NBTReader(new DataInputStream(new ByteArrayInputStream(expected))).read();
        assertEquals(rootName, loaded.getName());
        assertEquals(3, loaded.getData().size());
        assertEquals(1, loaded.getInt(""));
        assertEquals(2, loaded.getInt("null"));
        assertEquals(3, loaded.getInt(" "));
        assertArrayEquals(expected, serialize(loaded));

        Tag_Compound created = new Tag_Compound(rootName);
        created.addInt("", 1);
        created.addInt("null", 2);
        created.addInt(" ", 3);
        assertArrayEquals(expected, serialize(created));
    }

    private static void writeInt(DataOutputStream out, String name, int value) throws IOException
    {
        out.writeByte(3);
        out.writeUTF(name);
        out.writeInt(value);
    }

    private static byte[] serialize(ICompoundTag compound) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(compound);
        return bytes.toByteArray();
    }
}
