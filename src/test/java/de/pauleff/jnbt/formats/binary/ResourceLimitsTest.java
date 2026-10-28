package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.NBTLimits;
import de.pauleff.jnbt.core.*;
import de.pauleff.jnbt.formats.snbt.SNBTException;
import de.pauleff.jnbt.formats.snbt.SNBTParser;
import de.pauleff.jnbt.formats.snbt.SNBTSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static de.pauleff.jnbt.formats.binary.ResourceLimitsProbe.*;

class ResourceLimitsTest
{
    @TempDir
    Path tempDir;

    @ParameterizedTest
    @ValueSource(ints = {7, 9, 11, 12})
    void rejectsNegativeAndExcessiveLengthsBeforeReadingPayload(int type) throws IOException
    {
        NBTLimits limits = limits(5, 4096, 4096, 100, 4, 4096);
        for (int length : new int[]{-1, 5})
        {
            IOException error = assertThrows(IOException.class,
                    () -> read(declaredLength(type, length, new byte[0]), limits));
            assertTrue(error.getMessage().contains("collection length"), error::getMessage);
            assertNotNull(error.getCause());
            assertTrue(error.getMessage().contains("[Position:"));
        }
        assertNotNull(read(declaredLength(type, 4, new byte[type == 12 ? 32 : type == 11 ? 16 : 4]), limits));
        assertNotNull(read(declaredLength(type, 0, new byte[0]), limits));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void customDepthCountsContainersAndAllowsScalarAtBoundary(boolean lists) throws IOException, SNBTException
    {
        NBTLimits limits = depthLimit(3);
        assertNotNull(read(nestedBinary(3, lists), limits));
        assertTrue(assertThrows(IOException.class, () -> read(nestedBinary(4, lists), limits))
                .getMessage().contains("depth limit"));
        assertNotNull(new SNBTParser(nestedText(3, lists), limits).parse());
        assertTrue(assertThrows(SNBTException.class, () -> new SNBTParser(nestedText(4, lists), limits).parse())
                .getMessage().contains("depth limit"));
        Tag_Compound tree = nestedTree(3, lists);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes), limits).write(tree);
        assertNotNull(new SNBTSerializer(limits).serialize(tree));
        assertOutputRejected(nestedTree(4, lists), limits, "depth limit");
    }

    @Test
    void arraysDoNotAddContainerDepth() throws IOException, SNBTException
    {
        NBTLimits limits = depthLimit(1);
        assertNotNull(new SNBTParser("{x:[I;1,2]}", limits).parse());
        assertNotNull(read(declaredLength(11, 2, new byte[8]), limits));
        Tag_Compound root = new Tag_Compound("");
        root.addIntArray("x", new int[]{1, 2});
        new NBTWriter(new DataOutputStream(new ByteArrayOutputStream()), limits).write(root);
        assertEquals("{x: [I;1,2]}", new SNBTSerializer(limits).serialize(root));
    }

    @Test
    void binaryByteBudgetIncludesHeadersAndExactBoundary() throws IOException
    {
        byte[] fixture = nestedBinary(1, false);
        NBTLimits exact = limits(2, fixture.length, 4096, 10, 10, 4096);
        NBTLimits shortBudget = limits(2, fixture.length - 1, 4096, 10, 10, 4096);
        assertNotNull(read(fixture, exact));
        assertTrue(assertThrows(IOException.class, () -> read(fixture, shortBudget))
                .getMessage().contains("byte limit"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes), exact).write(nestedTree(1, false));
        assertArrayEquals(fixture, bytes.toByteArray());
        assertBinaryOutputRejected(nestedTree(1, false), shortBudget, "byte limit");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12})
    void binaryOutputByteAccountingMatchesEveryPayloadType(int type) throws IOException
    {
        Tag<?> value = switch (type)
        {
            case 7 -> new Tag_Byte_Array("value", new byte[]{1, 2});
            case 8 -> new Tag_String("value", "A\u0000\uD83D\uDE00");
            case 9 -> new Tag_List("value", 0);
            case 10 -> new Tag_Compound("value");
            case 11 -> new Tag_Int_Array("value", new int[]{1, 2});
            case 12 -> new Tag_Long_Array("value", new long[]{1, 2});
            default -> new Tag<Object>(type, "value") {};
        };
        Tag_Compound root = new Tag_Compound("");
        root.addTag(value);
        ByteArrayOutputStream baseline = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(baseline)).write(root);
        NBTLimits exact = limits(2, baseline.size(), 4096, 10, 10, 4096);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(actual), exact).write(root);
        assertArrayEquals(baseline.toByteArray(), actual.toByteArray());
        assertNotNull(read(actual.toByteArray(), exact));
        assertBinaryOutputRejected(root, limits(2, baseline.size() - 1, 4096, 10, 10, 4096), "byte limit");
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 11, 12})
    void declaredArrayPayloadMustFitByteBudgetBeforeAllocation(int type) throws IOException
    {
        NBTLimits limits = limits(2, 20, 4096, 10, 100, 4096);
        assertTrue(assertThrows(IOException.class, () -> read(declaredLength(type, 100, new byte[0]), limits))
                .getMessage().contains("byte limit"));
    }

    @Test
    void encodedStringMustFitByteBudgetBeforeAllocation() throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeByte(10);
        out.writeShort(60000);
        NBTLimits limits = limits(2, 20, 1_000_000, 10, 10, 4096);
        assertTrue(assertThrows(IOException.class, () -> read(bytes.toByteArray(), limits))
                .getMessage().contains("byte limit"));
    }

    @Test
    void allocationBudgetIsSharedAcrossSiblingArrays() throws IOException
    {
        byte[] one = declaredLength(7, 40, new byte[40]);
        ByteArrayOutputStream both = new ByteArrayOutputStream();
        both.write(one, 0, one.length - 1);
        both.write(one, 3, one.length - 3);
        NBTLimits limits = limits(2, 4096, 450, 10, 100, 4096);
        assertNotNull(read(one, limits));
        assertTrue(assertThrows(IOException.class, () -> read(both.toByteArray(), limits))
                .getMessage().contains("allocation limit"));
    }

    @Test
    void compoundLimitIsCheckedBeforeReadingAnotherChildPayload() throws IOException
    {
        byte[] headerOnly = {10, 0, 0, 12}; // Empty root name, then a long-array type without a payload.
        NBTLimits limits = limits(2, 4096, 4096, 10, 0, 4096);
        assertTrue(assertThrows(IOException.class, () -> read(headerOnly, limits))
                .getMessage().contains("collection length"));
    }

    @Test
    void declaredListReservesRemainingTagBudget() throws IOException
    {
        NBTLimits limits = limits(2, 4096, 4096, 5, 100, 4096);
        assertTrue(assertThrows(IOException.class, () -> read(declaredLength(9, 4, new byte[0]), limits))
                .getMessage().contains("tag count limit"));
    }

    @Test
    void cumulativeTagBudgetIncludesRootAndSiblingBranches() throws IOException
    {
        byte[] fixture = nestedBinary(3, false); // Three compounds and a byte.
        assertNotNull(read(fixture, limits(3, 4096, 4096, 4, 10, 4096)));
        NBTLimits strict = limits(3, 4096, 4096, 3, 10, 4096);
        assertTrue(assertThrows(IOException.class, () -> read(fixture, strict))
                .getMessage().contains("tag count limit"));
        assertOutputRejected(nestedTree(3, false), strict, "tag count limit");
    }

    @ParameterizedTest
    @ValueSource(strings = {"[1,2,3]", "[B;1b,2b,3b]", "[I;1,2,3]", "[L;1l,2l,3l]", "{a:1,b:2,c:3}"})
    void snbtChecksEveryCollectionLength(String text)
    {
        NBTLimits limits = limits(5, 4096, 4096, 100, 2, 4096);
        SNBTException error = assertThrows(SNBTException.class, () -> new SNBTParser(text, limits).parse());
        assertTrue(error.getMessage().contains("collection length"), error::getMessage);
        assertTrue(error.getPosition() >= 0);
    }

    @Test
    void snbtInputOutputAndAllocationBudgetsAreBounded() throws SNBTException
    {
        NBTLimits exact = limits(2, 4096, 4096, 10, 10, 5);
        assertEquals("abc", new SNBTParser("\"abc\"", exact).parse().getData());
        assertThrows(SNBTException.class, () -> new SNBTParser("\"abcd\"", exact).parse());
        SNBTSerializer serializer = new SNBTSerializer(exact);
        assertEquals("abcde", serializer.serialize(new Tag_String("", "abcde")));
        assertThrows(IllegalArgumentException.class, () -> serializer.serialize(new Tag_String("", "abcdef")));
        assertEquals("abcde", serializer.serialize(new Tag_String("", "abcde"))); // Fresh budget for reuse.
        NBTLimits allocation = limits(2, 4096, 150, 10, 10, 4096);
        assertTrue(assertThrows(SNBTException.class, () -> new SNBTParser("[1,2]", allocation).parse())
                .getMessage().contains("allocation limit"));
        NBTLimits tags = limits(2, 4096, 4096, 2, 10, 4096);
        assertTrue(assertThrows(SNBTException.class, () -> new SNBTParser("[I;1,2]", tags).parse())
                .getMessage().contains("tag count limit"));
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void fileReaderAppliesBudgetToUncompressedBytes(Compression_Types compression) throws IOException
    {
        byte[] fixture = declaredLength(7, 4096, new byte[4096]);
        Path file = tempDir.resolve("input.dat");
        try (OutputStream output = compressed(file, compression))
        {
            output.write(fixture);
        }
        assertNotNull(new NBTReader(file.toFile()).read());
        NBTLimits limits = limits(2, 100, 100_000, 10, 10000, 4096);
        assertTrue(assertThrows(IOException.class, () -> new NBTReader(file.toFile(), limits).read())
                .getMessage().contains("byte limit"));
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void rejectedOutputPreservesExistingFileAndBackup(Compression_Types compression) throws IOException
    {
        Path file = tempDir.resolve("save.dat");
        Path backup = tempDir.resolve("save.dat.bak");
        Files.writeString(file, "original");
        Files.writeString(backup, "backup");
        NBTWriter writer = new NBTWriter(file.toFile(), compression, depthLimit(1));
        assertThrows(IOException.class, () -> writer.write(nestedTree(2, false)));
        assertEquals("original", Files.readString(file));
        assertEquals("backup", Files.readString(backup));
        Path absent = tempDir.resolve("absent.dat");
        assertThrows(IOException.class, () -> new NBTWriter(absent.toFile(), compression, depthLimit(1))
                .write(nestedTree(2, false)));
        assertFalse(Files.exists(absent));
    }

    @Test
    void sharedAcyclicChildrenAreAllowed() throws IOException
    {
        Tag_Compound shared = new Tag_Compound("shared");
        shared.addInt("value", 7);
        Tag_List children = new Tag_List("children", 10);
        children.addTag(shared);
        children.addTag(shared);
        Tag_Compound root = new Tag_Compound("");
        root.addTag(children);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(root);
        assertNotNull(read(bytes.toByteArray(), NBTLimits.DEFAULT));
        assertNotNull(new SNBTSerializer().serialize(root));
    }

    @ParameterizedTest
    @ValueSource(strings = {"[1,2,3]", "[B;1b,2b,3b]", "[I;1,2,3]", "[L;1l,2l,3l]", "{a:1,b:2,c:3}"})
    void outputChecksEveryCollectionLength(String text) throws Exception
    {
        Tag<?> value = (Tag<?>) new SNBTParser(text).parse();
        value.setName("value");
        Tag_Compound root = new Tag_Compound("");
        root.addTag(value);
        assertOutputRejected(root, limits(5, 4096, 4096, 100, 2, 4096), "collection length");
    }

    @Test
    void outputChecksAllocationAndEscapedTextGrowth()
    {
        assertOutputRejected(nestedTree(2, false), limits(5, 4096, 128, 100, 100, 4096), "allocation limit");
        SNBTSerializer serializer = new SNBTSerializer(limits(1, 4096, 4096, 10, 10, 3));
        assertThrows(IllegalArgumentException.class, () -> serializer.serialize(new Tag_String("", "\n")));
        assertThrows(IllegalArgumentException.class, () -> serializer.serialize(null));
    }

    @Test
    void emptyCollectionsFitZeroElementLimit() throws Exception
    {
        NBTLimits limits = limits(1, 4096, 4096, 10, 0, 4096);
        assertNotNull(read(nestedBinary(1, false), NBTLimits.DEFAULT));
        Tag_Compound root = new Tag_Compound("");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes), limits).write(root);
        assertNotNull(read(bytes.toByteArray(), limits));
        for (String text : new String[]{"{}", "[]", "[B;]", "[I;]", "[L;]"})
        {
            assertNotNull(new SNBTSerializer(limits).serialize(new SNBTParser(text, limits).parse()));
        }
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void byteBudgetAccumulatesAcrossCompressedStringFields(Compression_Types compression) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeByte(10);
        out.writeUTF("");
        for (int i = 0; i < 20; i++)
        {
            out.writeByte(8);
            out.writeUTF("key" + i);
            out.writeUTF("z".repeat(100));
        }
        out.writeByte(0);
        Path file = tempDir.resolve("strings.dat");
        try (OutputStream compressed = compressed(file, compression))
        {
            compressed.write(bytes.toByteArray());
        }
        NBTLimits limits = limits(2, 1000, 100_000, 100, 100, 4096);
        assertTrue(assertThrows(IOException.class, () -> new NBTReader(file.toFile(), limits).read())
                .getMessage().contains("byte limit"));
    }

    @Test
    void rejectsInvalidConfiguration()
    {
        assertThrows(IllegalArgumentException.class, () -> depthLimit(0));
        assertThrows(IllegalArgumentException.class, () -> depthLimit(513));
        assertThrows(IllegalArgumentException.class, () -> limits(1, 0, 1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> limits(1, 1, -1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> limits(1, 1, 1, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> limits(1, 1, 1, 1, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> limits(1, 1, 1, 1, 1, 0));
    }

    @Test
    void resourceExhaustionProbesRunInBoundedChildJvm() throws Exception
    {
        String classpath = Path.of(ResourceLimitsTest.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator + Path.of(NBTReader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path log = tempDir.resolve("probe.log");
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Xmx64m", "-Xss1m", "-cp", classpath, ResourceLimitsProbe.class.getName(), tempDir.toString())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try
        {
            assertTrue(process.waitFor(20, TimeUnit.SECONDS), "Resource probe timed out");
            assertEquals(0, process.exitValue(), () -> {
                try { return Files.readString(log); }
                catch (IOException e) { throw new UncheckedIOException(e); }
            });
            assertTrue(Files.readString(log).contains("PROBES PASSED"));
        } finally
        {
            if (process.isAlive()) process.destroyForcibly();
        }
    }

    private static OutputStream compressed(Path file, Compression_Types compression) throws IOException
    {
        return switch (compression)
        {
            case NONE -> Files.newOutputStream(file);
            case GZIP -> new GZIPOutputStream(Files.newOutputStream(file));
            case ZLIB -> new DeflaterOutputStream(Files.newOutputStream(file));
            default -> throw new IllegalArgumentException();
        };
    }

    private static void assertBinaryOutputRejected(Tag_Compound tree, NBTLimits limits, String message)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        IOException error = assertThrows(IOException.class,
                () -> new NBTWriter(new DataOutputStream(bytes), limits).write(tree));
        assertTrue(error.getMessage().contains(message), error::getMessage);
        assertEquals(0, bytes.size());
    }

    private static void assertOutputRejected(Tag_Compound tree, NBTLimits limits, String message)
    {
        assertBinaryOutputRejected(tree, limits, message);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new SNBTSerializer(limits).serialize(tree));
        assertTrue(error.getMessage().contains(message), error::getMessage);
    }
}
