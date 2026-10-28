package de.pauleff.jnbt.core;

import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.api.NBTFactory;
import de.pauleff.jnbt.formats.binary.NBTWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TagPayloadTest
{
    private static Tag<Object> source(int id, String name, Object value)
    {
        return new Tag<>(id, name, value)
        {
        };
    }

    private static Tag<Object> corruptSource(int id, Object value)
    {
        return new Tag<>(id, "changed")
        {
            {
                data = value;
            }
        };
    }

    private static Stream<Payload> payloads()
    {
        return Stream.of(
                new Payload("byte", 1, NBTFactory::createByte, NBTFactory::createByte,
                        (byte) 1, (byte) 2, 1),
                new Payload("short", 2, NBTFactory::createShort, NBTFactory::createShort,
                        (short) 1, (short) 2, 1),
                new Payload("int", 3, NBTFactory::createInt, NBTFactory::createInt,
                        1, 2, 1L),
                new Payload("long", 4, NBTFactory::createLong, NBTFactory::createLong,
                        1L, 2L, 1),
                new Payload("float", 5, NBTFactory::createFloat, NBTFactory::createFloat,
                        1.0f, 2.0f, 1.0d),
                new Payload("double", 6, NBTFactory::createDouble, NBTFactory::createDouble,
                        1.0d, 2.0d, 1.0f),
                new Payload("byte[]", 7, NBTFactory::createByteArray, NBTFactory::createByteArray,
                        new byte[]{1}, new byte[]{2}, new Byte[]{1}),
                new Payload("string", 8, NBTFactory::createString, NBTFactory::createString,
                        "first", "second", new StringBuilder("wrong")),
                new Payload("int[]", 11, NBTFactory::createIntArray, NBTFactory::createIntArray,
                        new int[]{1}, new int[]{2}, new long[]{1}),
                new Payload("long[]", 12, NBTFactory::createLongArray, NBTFactory::createLongArray,
                        new long[]{1}, new long[]{2}, new int[]{1}));
    }

    private static Stream<Supplier<ITag<?>>> explicitNullPayloads()
    {
        return Stream.of(() -> NBTFactory.createString("x", null), () -> NBTFactory.createByteArray("x", null),
                () -> NBTFactory.createIntArray("x", null), () -> NBTFactory.createLongArray("x", null),
                () -> new Tag_String("x", null), () -> new Tag_Byte_Array("x", null),
                () -> new Tag_Int_Array("x", null), () -> new Tag_Long_Array("x", null),
                () -> new Tag_Compound("x", null), () -> new Tag_List("x", 3, null),
                () -> new Tag_End("end", null), () -> NBTFactory.createCompound("x", null));
    }

    private static void assertDefault(int id, Object value)
    {
        switch (id)
        {
            case 1 -> assertEquals((byte) 0, value);
            case 2 -> assertEquals((short) 0, value);
            case 3 -> assertEquals(0, value);
            case 4 -> assertEquals(0L, value);
            case 5 -> assertEquals(0.0f, value);
            case 6 -> assertEquals(0.0d, value);
            case 7 -> assertArrayEquals(new byte[0], (byte[]) value);
            case 8 -> assertEquals("", value);
            case 11 -> assertArrayEquals(new int[0], (int[]) value);
            case 12 -> assertArrayEquals(new long[0], (long[]) value);
            default -> throw new AssertionError("Unexpected test type");
        }
    }

    private static void setUnchecked(ITag tag, Object value)
    {
        tag.setData(value);
    }

    @ParameterizedTest
    @MethodSource("payloads")
    void initializesBothEmptyFactoryOverloadsWithDefaultsAndReplacesPayload(Payload type)
    {
        for (ITag<?> tag : new ITag<?>[]{type.empty.get(), type.named.apply("x")})
        {
            assertDefault(type.id, tag.getData());
            setUnchecked(tag, type.first);
            assertSame(type.first, tag.getData());
            setUnchecked(tag, type.second);
            assertSame(type.second, tag.getData());
            assertEquals(type.id, tag.getId());
        }
    }

    @ParameterizedTest
    @MethodSource("payloads")
    void nullAndWrongTypeUpdatesPreserveDefaultAndUpdatedState(Payload type)
    {
        ITag<?> tag = type.named.apply("original");
        assertThrows(IllegalArgumentException.class, () -> setUnchecked(tag, null));
        assertThrows(IllegalArgumentException.class, () -> setUnchecked(tag, type.wrong));
        assertDefault(type.id, tag.getData());
        setUnchecked(tag, type.first);
        assertThrows(IllegalArgumentException.class, () -> setUnchecked(tag, null));
        assertThrows(IllegalArgumentException.class, () -> setUnchecked(tag, type.wrong));
        assertSame(type.first, tag.getData());
        assertEquals("original", tag.getName());
    }

    @ParameterizedTest
    @MethodSource("payloads")
    void editsInitializeAndReplaceNameAndPayload(Payload type)
    {
        ITag<?> tag = type.named.apply("original");
        tag.editTag(source(type.id, "first", type.first));
        assertEquals("first", tag.getName());
        assertSame(type.first, tag.getData());
        tag.editTag(source(type.id, "second", type.second));
        assertEquals("second", tag.getName());
        assertSame(type.second, tag.getData());
        assertDoesNotThrow(() -> tag.editTag(tag));
    }

    @ParameterizedTest
    @MethodSource("payloads")
    void invalidEditsPreserveNameAndPayload(Payload type)
    {
        ITag<?> tag = type.named.apply("original");
        Object defaultValue = tag.getData();
        for (boolean updated : new boolean[]{false, true})
        {
            if (updated) setUnchecked(tag, type.first);
            assertThrows(IllegalArgumentException.class, () -> tag.editTag(null));
            assertThrows(IllegalArgumentException.class, () -> tag.editTag(corruptSource(type.id, null)));
            assertThrows(IllegalArgumentException.class, () -> tag.editTag(corruptSource(type.id, type.wrong)));
            int otherId = type.id == 3 ? 8 : 3;
            Object otherValue = type.id == 3 ? "wrong" : 1;
            assertThrows(IllegalArgumentException.class, () -> tag.editTag(source(otherId, "changed", otherValue)));
            assertEquals("original", tag.getName());
            assertSame(updated ? type.first : defaultValue, tag.getData());
        }
    }

    @ParameterizedTest
    @MethodSource("payloads")
    void conflictingEditNameLeavesPayloadUntouched(Payload type)
    {
        ITag<?> tag = type.named.apply("original");
        setUnchecked(tag, type.first);
        Tag_Compound parent = new Tag_Compound("root");
        parent.addTag(tag);
        parent.addInt("taken", 42);
        assertThrows(IllegalArgumentException.class,
                () -> tag.editTag(source(type.id, "taken", type.second)));
        assertEquals("original", tag.getName());
        assertSame(type.first, tag.getData());
        assertEquals(42, parent.getInt("taken"));
    }

    @ParameterizedTest
    @MethodSource("payloads")
    void validatesAgainstDeclaredTypeEvenIfExistingStorageIsCorrupt(Payload type)
    {
        ITag<?> tag = corruptSource(type.id, type.wrong);
        assertThrows(IllegalArgumentException.class, () -> setUnchecked(tag, type.wrong));
        setUnchecked(tag, type.first);
        assertSame(type.first, tag.getData());
    }

    @ParameterizedTest
    @MethodSource("payloads")
    void baseConstructorRejectsNullAndWrongPayload(Payload type)
    {
        assertThrows(IllegalArgumentException.class, () -> source(type.id, "x", null));
        assertThrows(IllegalArgumentException.class, () -> source(type.id, "x", type.wrong));
        assertSame(type.first, source(type.id, "x", type.first).getData());
    }

    @ParameterizedTest
    @MethodSource("explicitNullPayloads")
    void explicitNullConstructorPayloadIsRejected(Supplier<ITag<?>> constructor)
    {
        assertThrows(IllegalArgumentException.class, constructor::get);
    }

    @ParameterizedTest
    @MethodSource("payloads")
    void defaultsSerializeAsIndependentBinaryFixtureAndValidSNBT(Payload type) throws Exception
    {
        Tag_Compound root = new Tag_Compound("");
        root.addTag(type.named.apply("x"));
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(actual)).write(root);
        ByteArrayOutputStream expected = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(expected))
        {
            out.writeByte(10);
            out.writeUTF("");
            out.writeByte(type.id);
            out.writeUTF("x");
            switch (type.id)
            {
                case 1 -> out.writeByte(0);
                case 2 -> out.writeShort(0);
                case 3 -> out.writeInt(0);
                case 4 -> out.writeLong(0L);
                case 5 -> out.writeFloat(0.0f);
                case 6 -> out.writeDouble(0.0d);
                case 7, 11, 12 -> out.writeInt(0);
                case 8 -> out.writeUTF("");
                default -> throw new AssertionError("Unexpected test type");
            }
            out.writeByte(0);
        }
        assertArrayEquals(expected.toByteArray(), actual.toByteArray());
        var parsed = NBTFactory.parseCompoundFromSNBT(NBTFactory.toSNBT(root));
        assertEquals(type.id, parsed.getData().getFirst().getId());
        assertDefault(type.id, parsed.getData().getFirst().getData());
    }

    @Test
    void emptyCollectionsAndArraysHaveIndependentStorage()
    {
        for (int id : new int[]{7, 9, 10, 11, 12})
        {
            Tag<Object> first = new Tag<>(id, "first")
            {
            };
            Tag<Object> second = new Tag<>(id, "second")
            {
            };
            assertNotNull(first.getData());
            assertNotSame(first.getData(), second.getData());
        }
    }

    @Test
    void nullCompoundSetterDoesNotRemoveExistingValue()
    {
        Tag_Compound root = new Tag_Compound("");
        root.addString("x", "original");
        assertThrows(IllegalArgumentException.class, () -> root.setString("x", null));
        assertThrows(IllegalArgumentException.class, () -> root.setIntArray("x", null));
        assertEquals("original", root.getString("x"));
        assertEquals(1, root.size());
    }

    @Test
    void preservesLegacyEndMetadataSupport()
    {
        Tag_End end = new Tag_End();
        assertNull(end.getData());
        assertNull(new Tag_End("end").getData());
        Object metadata = new Object();
        end.setData(metadata);
        assertSame(metadata, end.getData());
        end.editTag(new Tag_End("end", "legacy"));
        assertEquals("legacy", end.getData());
        assertThrows(IllegalArgumentException.class, () -> end.setData(null));
        assertEquals("legacy", end.getData());
    }

    private record Payload(String name, int id, Supplier<? extends ITag<?>> empty,
                           Function<String, ? extends ITag<?>> named,
                           Object first, Object second, Object wrong)
    {
        @Override
        public String toString()
        {
            return name;
        }
    }
}
