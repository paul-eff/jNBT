package de.pauleff.jnbt.api;

import de.pauleff.jnbt.builder.CompoundBuilder;
import de.pauleff.jnbt.builder.NBTBuilder;
import de.pauleff.jnbt.core.*;
import de.pauleff.jnbt.formats.binary.Compression_Types;
import de.pauleff.jnbt.formats.binary.NBTWriter;
import de.pauleff.jnbt.formats.snbt.SNBTSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TagImplementationContractTest
{
    static Stream<ITag<?>> independentTags()
    {
        return Stream.of(new IndependentInt("x", 7),
                independent(ICompoundTag.class, new Tag_Compound("x").addInt("n", 7)),
                independent(IListTag.class, new Tag_List("x", 3).addInt("", 7)));
    }

    @ParameterizedTest
    @MethodSource("independentTags")
    void compoundInsertionRejectsUnsupportedImplementationsBeforeReplacement(ITag<?> unsupported)
    {
        Tag_Compound compound = new Tag_Compound("");
        compound.addInt("x", 1);
        ITag<?> original = compound.getTag("x");
        assertThrows(IllegalArgumentException.class, () -> compound.setTag(unsupported));
        assertEquals(1, compound.size());
        assertSame(original, compound.getTag("x"));
        compound.removeTag("x");
        assertThrows(IllegalArgumentException.class, () -> compound.addTag(unsupported));
        assertTrue(compound.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("independentTags")
    void listInsertionRejectsUnsupportedImplementationsBeforeMutation(ITag<?> unsupported)
    {
        Tag_List list = new Tag_List("", unsupported.getId());
        Tag<?> original = switch (unsupported.getId())
        {
            case 3 -> new Tag_Int("x", 1);
            case 9 -> new Tag_List("x", 3);
            case 10 -> new Tag_Compound("x");
            default -> throw new AssertionError();
        };
        list.addTag(original);
        assertThrows(IllegalArgumentException.class, () -> list.setTag(0, unsupported));
        assertThrows(IllegalArgumentException.class, () -> list.addTag(unsupported));
        assertThrows(IllegalArgumentException.class, () -> list.remove(unsupported));
        assertEquals(1, list.size());
        assertSame(original, list.get(0));
    }

    @ParameterizedTest
    @MethodSource("independentTags")
    void compoundFactoryRejectsUnsupportedChildren(ITag<?> unsupported)
    {
        ArrayList<ITag<?>> values = new ArrayList<>();
        values.add(new Tag_Int("before", 1));
        values.add(unsupported);
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.createCompound("", values));
        assertEquals(2, values.size());
        assertSame(unsupported, values.getLast());
    }

    @Test
    void unsupportedCopySourcesLeaveTheirDestinationsUntouched()
    {
        Tag_Int scalar = new Tag_Int("original", 1);
        assertThrows(IllegalArgumentException.class, () -> scalar.editTag(new IndependentInt("changed", 7)));
        assertEquals("original", scalar.getName());
        assertEquals(1, scalar.getData());
        Tag_Compound compound = new Tag_Compound("original");
        compound.addInt("before", 1);
        ICompoundTag source = independent(ICompoundTag.class, new Tag_Compound("changed").addInt("after", 7));
        assertThrows(IllegalArgumentException.class, () -> compound.editTag(source));
        assertEquals("original", compound.getName());
        assertEquals(1, compound.getInt("before"));
        assertEquals(1, compound.size());
        Tag_List list = new Tag_List("original", 3);
        list.addInt("", 1);
        IListTag sourceList = independent(IListTag.class, new Tag_List("changed", 3).addInt("", 7));
        assertThrows(IllegalArgumentException.class, () -> list.editTag(sourceList));
        assertEquals("original", list.getName());
        assertEquals(1, list.get(0).getData());
    }

    @Test
    void buildersRejectUnsupportedCompoundsInsteadOfSilentlyDroppingChildren()
    {
        ICompoundTag source = independent(ICompoundTag.class, new Tag_Compound("source").addInt("n", 7));
        assertThrows(IllegalArgumentException.class, () -> NBTBuilder.fromCompound(source));
        assertThrows(IllegalArgumentException.class, () -> new CompoundBuilder(source, null));
        assertEquals(7, source.getInt("n"));
    }

    @ParameterizedTest
    @MethodSource("independentTags")
    void snbtRejectsUnsupportedRootsAndPreservesTextFiles(ITag<?> unsupported, @TempDir Path directory) throws Exception
    {
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.toSNBT(unsupported));
        assertThrows(IllegalArgumentException.class, () -> new SNBTSerializer().serialize(unsupported));
        Path existing = directory.resolve("existing.snbt");
        Files.writeString(existing, "original");
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.writeToSNBTFile(unsupported, existing.toFile()));
        assertEquals("original", Files.readString(existing));
        Path missing = directory.resolve("missing.snbt");
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.writeToSNBTFile(unsupported, missing.toFile()));
        assertFalse(Files.exists(missing));
    }

    @Test
    void binaryRejectsAnUnsupportedRootBeforeOutputAndClosesTheStream()
    {
        ICompoundTag unsupported = independent(ICompoundTag.class, new Tag_Compound("").addInt("n", 7));
        class Output extends ByteArrayOutputStream
        {
            int closes;
            @Override public void close() { closes++; }
        }
        Output bytes = new Output();
        IOException error = assertThrows(IOException.class,
                () -> new NBTWriter(new DataOutputStream(bytes)).write(unsupported));
        assertInstanceOf(IllegalArgumentException.class, error.getCause());
        assertEquals(0, bytes.size());
        assertEquals(1, bytes.closes);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void binaryRejectionPreservesExistingAndAbsentFiles(boolean existing, @TempDir Path directory) throws Exception
    {
        Path file = directory.resolve("root.dat");
        Path backup = directory.resolve("root.dat.bak");
        if (existing) Files.writeString(file, "original");
        Files.writeString(backup, "prior backup");
        ICompoundTag unsupported = independent(ICompoundTag.class, new Tag_Compound("").addInt("n", 7));
        assertThrows(IOException.class,
                () -> NBTFileFactory.writeNBTFile(file.toFile(), unsupported, Compression_Types.GZIP));
        if (existing) assertEquals("original", Files.readString(file));
        else assertFalse(Files.exists(file));
        assertEquals("prior backup", Files.readString(backup));
    }

    @Test
    void coreSubclassesRetainSharedMembershipAndSupportedCopies() throws Exception
    {
        Tag_Int child = new Tag_Int("x", 7) {};
        Tag_Compound root = new Tag_Compound("") {};
        root.addTag(child);
        assertSame(child, root.getTag("x"));
        CompoundBuilder copied = NBTBuilder.fromCompound(root);
        assertSame(child, copied.build().getTag("x"));
        child.setData(8);
        assertEquals(8, root.getInt("x"));
        assertEquals(8, copied.build().getInt("x"));
        Tag_Compound edited = new Tag_Compound("");
        edited.editTag(root);
        assertSame(child, edited.getTag("x"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(root);
        assertArrayEquals(java.util.HexFormat.of().parseHex("0a0000030001780000000800"), bytes.toByteArray());
        assertEquals(8, NBTFactory.parseCompoundFromSNBT(NBTFactory.toSNBT(root)).getInt("x"));
    }

    @ParameterizedTest
    @MethodSource("independentTags")
    void bulkOperationsRejectUnsupportedRawChildrenBeforeMutation(ITag<?> unsupported)
    {
        Tag_Compound compound = new Tag_Compound("");
        compound.addInt("original", 1);
        ITag<?> original = compound.getTag("original");
        ArrayList<Tag<?>> children = unsupportedChildren(unsupported);
        assertThrows(IllegalArgumentException.class, () -> compound.setData(children));
        assertThrows(IllegalArgumentException.class, () -> compound.addAllTags(children));
        assertThrows(IllegalArgumentException.class, () -> new Tag_Compound("", children));
        assertEquals(1, compound.size());
        assertSame(original, compound.getTag("original"));
        Tag_List list = new Tag_List("", unsupported.getId());
        assertThrows(IllegalArgumentException.class, () -> list.setData(children));
        assertThrows(IllegalArgumentException.class, () -> list.addAllTags(children));
        assertThrows(IllegalArgumentException.class, () -> new Tag_List("", unsupported.getId(), children));
        assertTrue(list.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("independentTags")
    void corruptedCoreContainersCannotSerializeIndependentChildren(ITag<?> unsupported)
    {
        Tag_Compound corrupt = new Tag_Compound("nested")
        {
            {
                data = unsupportedChildren(unsupported);
            }
        };
        Tag_Compound root = new Tag_Compound("");
        root.addInt("before", 1);
        root.addTag(corrupt);
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.toSNBT(root));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assertThrows(IOException.class, () -> new NBTWriter(new DataOutputStream(bytes)).write(root));
        assertEquals(0, bytes.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {9, 10})
    void containerIdsRequireTheirCoreCollectionClass(int id)
    {
        Tag<?> unsupported = new Tag<ArrayList<Tag<?>>>(id, "x") {};
        Tag_Compound root = new Tag_Compound("");
        root.addInt("x", 1);
        assertThrows(IllegalArgumentException.class, () -> root.setTag(unsupported));
        assertEquals(1, root.getInt("x"));
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.toSNBT(unsupported));
    }

    @ParameterizedTest
    @MethodSource("independentTags")
    void builderTagInputsValidateBeforeChangingTheLiveCompound(ITag<?> unsupported)
    {
        CompoundBuilder builder = NBTBuilder.compound("").addInt("x", 1);
        ITag<?> original = builder.build().getTag("x");
        assertThrows(IllegalArgumentException.class, () -> builder.setTag(unsupported));
        assertSame(original, builder.build().getTag("x"));
        builder.removeTag("x");
        assertThrows(IllegalArgumentException.class, () -> builder.addTag(unsupported));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void factoryListsAndBuilderInputsAcceptCoreTagsThroughInterfaces()
    {
        ITag<Integer> child = NBTFactory.createInt("x", 7);
        List<ITag<?>> input = List.of(child);
        ICompoundTag compound = NBTFactory.createCompound("", input);
        assertSame(child, compound.getTag("x"));
        CompoundBuilder builder = NBTBuilder.compound("").addTag(child);
        assertSame(child, builder.build().getTag("x"));
        ITag<Integer> replacement = NBTFactory.createInt("x", 8);
        builder.setTag(replacement);
        assertSame(replacement, builder.build().getTag("x"));
        assertSame(child, input.getFirst());
        assertEquals(7, compound.getInt("x"));
    }

    static Stream<Arguments> primitivePayloads()
    {
        return Stream.of(Arguments.of(1, (byte) 7), Arguments.of(2, (short) 300),
                Arguments.of(3, 70_000), Arguments.of(4, 5_000_000_000L), Arguments.of(5, 0.5f),
                Arguments.of(6, 0.5d), Arguments.of(7, new byte[]{7}), Arguments.of(8, "text"),
                Arguments.of(11, new int[]{7}), Arguments.of(12, new long[]{7}));
    }

    @ParameterizedTest
    @MethodSource("primitivePayloads")
    void baseTagSubclassesWorkWithTypedGetters(int id, Object payload)
    {
        Tag<?> child = new Tag<Object>(id, "x", payload) {};
        Tag_Compound root = new Tag_Compound("");
        root.addTag(child);
        Object actual = switch (id)
        {
            case 1 -> root.getByte("x");
            case 2 -> root.getShort("x");
            case 3 -> root.getInt("x");
            case 4 -> root.getLong("x");
            case 5 -> root.getFloat("x");
            case 6 -> root.getDouble("x");
            case 7 -> root.getByteArray("x");
            case 8 -> root.getString("x");
            case 11 -> root.getIntArray("x");
            case 12 -> root.getLongArray("x");
            default -> throw new AssertionError();
        };
        assertEquals(payload.getClass(), actual.getClass());
        assertEquals(payload, actual);
        assertSame(child, root.getTag("x"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 11, 12})
    void binaryPayloadValidationPrecedesOutputCasts(int id)
    {
        for (Object payload : new Object[]{null, new Object()})
        {
            Tag_Compound root = new Tag_Compound("");
            root.addInt("before", 1);
            root.addTag(new Tag<Object>(id, "bad")
            {
                @Override public Object getData() { return payload; }
            });
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            assertThrows(IOException.class, () -> new NBTWriter(new DataOutputStream(bytes)).write(root));
            assertEquals(0, bytes.size());
        }
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<Tag<?>> unsupportedChildren(ITag<?> unsupported)
    {
        ArrayList<Object> values = new ArrayList<>();
        values.add(switch (unsupported.getId())
        {
            case 9 -> new Tag_List("before", 3);
            case 10 -> new Tag_Compound("before");
            default -> new Tag_Int("before", 1);
        });
        values.add(unsupported);
        return (ArrayList<Tag<?>>) (ArrayList<?>) values;
    }

    @SuppressWarnings("unchecked")
    private static <T> T independent(Class<T> contract, T delegate)
    {
        return (T) Proxy.newProxyInstance(contract.getClassLoader(), new Class<?>[]{contract}, (proxy, method, args) ->
        {
            try
            {
                return method.invoke(delegate, args);
            } catch (InvocationTargetException error)
            {
                throw error.getCause();
            }
        });
    }

    private static class IndependentInt implements ITag<Integer>
    {
        private String name;
        private Integer value;
        IndependentInt(String name, int value) { this.name = name; this.value = value; }
        @Override public int getId() { return 3; }
        @Override public String getName() { return name; }
        @Override public void setName(String name) { this.name = name; }
        @Override public Integer getData() { return value; }
        @Override public void setData(Integer value) { this.value = value; }
        @Override public void editTag(ITag<?> other) { name = other.getName(); value = (Integer) other.getData(); }
        @Override public void applyOperation(Consumer<ITag<Integer>> operation) { operation.accept(this); }
        @Override public String toString() { return name + ":" + value; }
    }
}
