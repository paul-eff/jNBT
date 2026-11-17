package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.NBTLimits;
import de.pauleff.jnbt.core.Collection_Tag;
import de.pauleff.jnbt.core.Tag_Byte;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.core.Tag_List;
import de.pauleff.jnbt.formats.snbt.SNBTException;
import de.pauleff.jnbt.formats.snbt.SNBTParser;
import de.pauleff.jnbt.formats.snbt.SNBTSerializer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ResourceLimitsTest runs these stress checks in a separate JVM with memory and time limits.
 * The first argument is a temporary directory for file checks.
 */
class ResourceLimitsProbe
{
    static NBTLimits limits(int depth, long bytes, long allocation, long tags, int length, int text)
    {
        return new NBTLimits(depth, bytes, allocation, tags, length, text);
    }

    static NBTLimits depthLimit(int depth)
    {
        NBTLimits defaults = NBTLimits.DEFAULT;
        return limits(depth, defaults.maxBytes(), defaults.maxAllocationBytes(), defaults.maxTags(),
                defaults.maxCollectionLength(), defaults.maxTextLength());
    }

    static byte[] nestedBinary(int containers, boolean lists) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeByte(10);
        out.writeUTF("");
        if (lists && containers > 1)
        {
            out.writeByte(9);
            out.writeUTF("x");
            for (int level = 1; level < containers; level++)
            {
                out.writeByte(level == containers - 1 ? 1 : 9);
                out.writeInt(1);
            }
            out.writeByte(7);
            out.writeByte(0);
        } else
        {
            for (int level = 1; level < containers; level++)
            {
                out.writeByte(10);
                out.writeUTF("x");
            }
            out.writeByte(1);
            out.writeUTF("v");
            out.writeByte(7);
            for (int level = 0; level < containers; level++) out.writeByte(0);
        }
        return bytes.toByteArray();
    }

    static Tag_Compound nestedTree(int containers, boolean lists)
    {
        Tag_Compound root = new Tag_Compound("");
        Collection_Tag parent = root;
        for (int level = 1; level < containers; level++)
        {
            Collection_Tag child = lists ? new Tag_List("x", level == containers - 1 ? 1 : 9)
                    : new Tag_Compound("x");
            parent.addTag(child);
            parent = child;
        }
        parent.addTag(new Tag_Byte("v", (byte) 7));
        return root;
    }

    static String nestedText(int containers, boolean lists)
    {
        return lists ? "[".repeat(containers) + "7b" + "]".repeat(containers)
                : "{x:".repeat(containers) + "7b" + "}".repeat(containers);
    }

    static byte[] declaredLength(int type, int length, byte[] payload) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeByte(10);
        out.writeUTF("");
        out.writeByte(type);
        out.writeUTF("x");
        if (type == 9) out.writeByte(1);
        out.writeInt(length);
        out.write(payload);
        out.writeByte(0);
        return bytes.toByteArray();
    }

    static Object read(byte[] bytes, NBTLimits limits) throws IOException
    {
        return new NBTReader(new DataInputStream(new ByteArrayInputStream(bytes)), limits).read();
    }

    static void main(String[] args) throws Exception
    {
        for (boolean lists : new boolean[]{false, true})
        {
            for (int depth : new int[]{511, 512})
            {
                read(nestedBinary(depth, lists), NBTLimits.DEFAULT);
                new SNBTParser(nestedText(depth, lists)).parse();
                Tag_Compound root = nestedTree(depth, lists);
                new NBTWriter(new DataOutputStream(new ByteArrayOutputStream())).write(root);
                new SNBTSerializer().serialize(root);
            }
            for (int depth : new int[]{513, 20_000})
            {
                rejected(() -> read(nestedBinary(depth, lists), NBTLimits.DEFAULT), "depth limit");
                rejected(() -> new SNBTParser(nestedText(depth, lists)).parse(), "depth limit");
                Tag_Compound root = nestedTree(depth, lists);
                rejected(() -> new NBTWriter(new DataOutputStream(new ByteArrayOutputStream())).write(root), "depth limit");
                rejected(() -> new SNBTSerializer().serialize(root), "depth limit");
            }
        }
        // Only 257 textual containers, but storage wrappers bring the compound root to depth 512.
        String mixed = "[0,".repeat(255) + "[]" + "]".repeat(255);
        Tag_Compound mixedRoot = (Tag_Compound) new SNBTParser("{x:" + mixed + "}").parse();
        ByteArrayOutputStream mixedBytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(mixedBytes)).write(mixedRoot);
        read(mixedBytes.toByteArray(), NBTLimits.DEFAULT);
        new SNBTParser(new SNBTSerializer().serialize(mixedRoot)).parse();
        rejected(() -> new SNBTParser("[0," + mixed + "]").parse(), "depth limit");
        // A mismatch after a deep homogeneous subtree must also account for the new wrapper.
        String deepList = "[".repeat(511) + "0" + "]".repeat(511);
        rejected(() -> new SNBTParser("[" + deepList + ",{}]").parse(), "depth limit");
        // Long numeric runs stay linear and use fixed-width integer accumulation.
        String paddedHex = "0x" + "0".repeat(200_000) + "1";
        if (!new SNBTParser(paddedHex).parse().getData().equals(1))
            throw new AssertionError("Padded hexadecimal value changed");
        rejected(() -> new SNBTParser("9".repeat(200_000)).parse(), "Invalid number format");
        rejected(() -> new SNBTParser("1" + "_".repeat(200_000)).parse(), "Invalid number format");
        String separatedFloat = "0." + "0_".repeat(100_000) + "1";
        new SNBTParser(separatedFloat).parse(); // Finite underflow to zero remains accepted.
        NBTLimits numericAllocation = limits(1, 4096, 4L * separatedFloat.length() + 64,
                1, 1, separatedFloat.length());
        rejected(() -> new SNBTParser(separatedFloat, numericAllocation).parse(), "allocation limit");
        rejected(() -> new SNBTParser("[I;".repeat(20_000) + "1" + "]".repeat(20_000)).parse(), "scalar value");
        for (int type : new int[]{7, 9, 11, 12})
        {
            rejected(() -> read(declaredLength(type, Integer.MAX_VALUE, new byte[0]), NBTLimits.DEFAULT), "collection length");
            // Even relaxed element limits must reject huge payloads using overflow-safe byte accounting.
            NBTLimits relaxed = limits(512, Long.MAX_VALUE, 1024, Long.MAX_VALUE, Integer.MAX_VALUE, 4096);
            rejected(() -> read(declaredLength(type, Integer.MAX_VALUE, new byte[0]), relaxed), "allocation limit");
        }
        Tag_Compound self = new Tag_Compound("self");
        self.addTag(self);
        cycleRejected(self, Path.of(args[0], "self.dat"));
        Tag_Compound root = new Tag_Compound("root");
        Tag_List list = new Tag_List("list", 10);
        root.addTag(list);
        list.addTag(root);
        cycleRejected(root, Path.of(args[0], "indirect.dat"));
        Tag_Compound listRoot = new Tag_Compound("root");
        Tag_List selfList = new Tag_List("list", 9);
        selfList.addTag(selfList);
        listRoot.addTag(selfList);
        cycleRejected(listRoot, Path.of(args[0], "list.dat"));
        System.out.println("PROBES PASSED");
    }

    private static void cycleRejected(Tag_Compound root, Path file) throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        rejected(() -> new NBTWriter(new DataOutputStream(bytes)).write(root), "Cyclic");
        if (bytes.size() != 0) throw new AssertionError("Output before cycle rejection");
        rejected(() -> new SNBTSerializer().serialize(root), "Cyclic");
        Files.writeString(file, "original");
        rejected(() -> {
            try (NBTWriter writer = new NBTWriter(file.toFile(), Compression_Types.GZIP))
            {
                writer.write(root);
            }
        }, "Cyclic");
        if (!Files.readString(file).equals("original") || Files.exists(Path.of(file + ".bak")))
            throw new AssertionError("Cycle rejection changed destination");
        rejected(() -> new SNBTSerializer().writeToFile(root, file.toFile()), "Cyclic");
        if (!Files.readString(file).equals("original")) throw new AssertionError("SNBT cycle rejection changed destination");
    }

    private static void rejected(Operation operation, String message) throws Exception
    {
        try
        {
            operation.run();
        } catch (IOException | SNBTException | IllegalArgumentException e)
        {
            if (e.getMessage().contains(message)) return;
            throw new AssertionError("Expected " + message + ", got " + e.getMessage(), e);
        }
        throw new AssertionError("Expected rejection: " + message);
    }

    @FunctionalInterface
    private interface Operation
    {
        void run() throws Exception;
    }
}
