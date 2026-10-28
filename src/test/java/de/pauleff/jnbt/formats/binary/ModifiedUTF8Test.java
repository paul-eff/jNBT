package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.core.Tag_List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ModifiedUTF8Test
{
    private static Stream<String> strings()
    {
        return Stream.of("", "ASCII", "A\u0000Z", "\u007f\u0080\u07ff\u0800\uffff",
                "Grüße 世界", "A\u0000\uD83D\uDE00Z", "\uD800", "\uDC00", "\uD800x\uDC00");
    }

    private static Stream<byte[]> malformedEncodings()
    {
        return Stream.of(new byte[]{(byte) 0x80}, new byte[]{(byte) 0xc2},
                new byte[]{(byte) 0xc2, 0x41}, new byte[]{(byte) 0xe1, (byte) 0x80},
                new byte[]{(byte) 0xe1, 0x41, (byte) 0x80},
                new byte[]{(byte) 0xe1, (byte) 0x80, 0x41},
                new byte[]{(byte) 0xf0, (byte) 0x9f, (byte) 0x98, (byte) 0x80});
    }

    private static byte[] independentFixture(String text) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes))
        {
            out.writeByte(10);
            out.writeUTF(text);
            out.writeByte(8);
            out.writeUTF(text);
            out.writeUTF(text);
            out.writeByte(9);
            out.writeUTF("list");
            out.writeByte(8);
            out.writeInt(1);
            out.writeUTF(text);
            out.writeByte(0);
        }

        return bytes.toByteArray();
    }

    private static ICompoundTag read(byte[] bytes) throws IOException
    {
        return new NBTReader(new DataInputStream(new ByteArrayInputStream(bytes))).read();
    }

    @ParameterizedTest
    @MethodSource("strings")
    void readsIndependentModifiedUTF8NamesAndValues(String text) throws IOException
    {
        ICompoundTag root = read(independentFixture(text));
        assertEquals(text, root.getName());
        assertEquals(text, root.getData().getFirst().getName());
        assertEquals(text, root.getString(text));
        assertEquals(text, root.getList("list").getData().getFirst().getData());
    }

    @ParameterizedTest
    @MethodSource("strings")
    void writesSameBytesAsJava(String text) throws IOException
    {
        Tag_Compound root = new Tag_Compound(text);
        root.addString(text, text);
        Tag_List list = new Tag_List("list", 8);
        list.addString("", text);
        root.addTag(list);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(root);
        // Exact agreement with writeUTF also verifies the headers and list payload.
        assertArrayEquals(independentFixture(text), bytes.toByteArray());
    }

    @ParameterizedTest
    @MethodSource("malformedEncodings")
    void rejectsMalformedRootNamesWithPosition(byte[] encoded) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes))
        {
            out.writeByte(10);
            out.writeShort(encoded.length);
            out.write(encoded);
            out.writeByte(0);
        }

        IOException error = assertThrows(IOException.class, () -> read(bytes.toByteArray()));
        assertInstanceOf(UTFDataFormatException.class, error.getCause());
        assertTrue(error.getMessage().contains("Position: " + (3 + encoded.length) + " bytes"));
        assertTrue(error.getMessage().contains("Type: 10"));
    }

    @ParameterizedTest
    @MethodSource("malformedEncodings")
    void rejectsMalformedValuesWithTagContext(byte[] encoded) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes))
        {
            out.writeByte(10);
            out.writeUTF("");
            out.writeByte(8);
            out.writeUTF("s");
            out.writeShort(encoded.length);
            out.write(encoded);
            out.writeByte(0);
        }

        IOException error = assertThrows(IOException.class, () -> read(bytes.toByteArray()));
        assertInstanceOf(UTFDataFormatException.class, error.getCause());
        assertTrue(error.getMessage().contains("Position: " + (9 + encoded.length) + " bytes"));
        assertTrue(error.getMessage().contains("Tag: 's'"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void retainsEOFCauseForTruncatedUTFFields(boolean payload) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes))
        {
            out.writeByte(10);
            if (payload)
            {
                out.writeUTF("");
                out.writeByte(8);
                out.writeUTF("s");
            }
            out.writeShort(2);
            out.writeByte(0xc2);
        }

        IOException error = assertThrows(IOException.class, () -> read(bytes.toByteArray()));
        assertInstanceOf(EOFException.class, error.getCause());
    }

    @Test
    void tracksEncodedBytesForUTFReadsAndDecodeErrors() throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes))
        {
            out.writeUTF("A\u0000\uD83D\uDE00Z");
            out.writeShort(1);
            out.writeByte(0x80);
        }
        PositionTrackingDataInputStream in = new PositionTrackingDataInputStream(
                new ByteArrayInputStream(bytes.toByteArray()));
        try
        {
            assertEquals("A\u0000\uD83D\uDE00Z", in.readUTF());
            assertEquals(12, in.getBytesRead());
            assertThrows(UTFDataFormatException.class, in::readUTF);
            assertEquals(15, in.getBytesRead());
        } finally
        {
            in.close();
        }
    }
}
