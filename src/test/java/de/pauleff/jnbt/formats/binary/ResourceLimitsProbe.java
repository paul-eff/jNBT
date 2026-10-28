package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.NBTLimits;
import de.pauleff.jnbt.core.*;
import de.pauleff.jnbt.formats.snbt.SNBTException;
import de.pauleff.jnbt.formats.snbt.SNBTParser;
import de.pauleff.jnbt.formats.snbt.SNBTSerializer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public static void main(String[] args) throws Exception
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
        rejected(() -> new NBTWriter(file.toFile(), Compression_Types.GZIP).write(root), "Cyclic");
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
