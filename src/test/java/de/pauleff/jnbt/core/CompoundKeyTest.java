package de.pauleff.jnbt.core;

import de.pauleff.jnbt.builder.NBTBuilder;
import de.pauleff.jnbt.formats.binary.Compression_Types;
import de.pauleff.jnbt.formats.binary.NBTReader;
import de.pauleff.jnbt.formats.binary.NBTWriter;
import de.pauleff.jnbt.formats.snbt.SNBTException;
import de.pauleff.jnbt.formats.snbt.SNBTParser;
import de.pauleff.jnbt.formats.snbt.SNBTSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompoundKeyTest
{
    private static Tag_Compound corruptCompound(String name, String key)
    {
        return new Tag_Compound(name)
        {
            {
                data.add(new Tag_Int(key, 1));
                data.add(new Tag_Int(key, 2));
            }
        };
    }

    @ParameterizedTest
    @ValueSource(strings = {"x", "", " ", "null", "café"})
    void rejectsDuplicateAdditionsAndConstruction(String key)
    {
        Tag_Compound root = new Tag_Compound("");
        root.addInt(key, 1);
        assertThrows(IllegalArgumentException.class, () -> root.addString(key, "second"));
        assertThrows(IllegalArgumentException.class, () -> root.addTag(root.getData().getFirst()));
        assertEquals(1, root.size());
        assertEquals(1, root.getInt(key));
        assertThrows(IllegalArgumentException.class, () -> new Tag_Compound("",
                new ArrayList<>(List.of(new Tag_Int(key, 1), new Tag_String(key, "second")))));
    }

    @Test
    void validatesBulkChangesBeforeMutating()
    {
        Tag_Compound root = new Tag_Compound("root");
        root.addInt("x", 1);
        List<Tag<?>> original = root.getData();
        assertThrows(IllegalArgumentException.class, () -> root.addAllTags(
                List.of(new Tag_Int("y", 2), new Tag_Int("x", 3))));
        assertThrows(IllegalArgumentException.class, () -> root.addAllTags(
                List.of(new Tag_Int("y", 2), new Tag_Int("y", 3))));
        assertThrows(IllegalArgumentException.class, () -> root.setData(
                new ArrayList<>(List.of(new Tag_Int("z", 2), new Tag_Int("z", 3)))));
        assertThrows(IllegalArgumentException.class, () -> root.setData(null));
        assertThrows(IllegalArgumentException.class, () -> root.setData(new ArrayList<>(List.of(new Tag_End()))));
        ArrayList<Tag<?>> nullChild = new ArrayList<>();
        nullChild.add(null);
        assertThrows(IllegalArgumentException.class, () -> root.setData(nullChild));
        assertEquals(original, root.getData());
    }

    @Test
    void copiesMembershipOnInputAndOutput()
    {
        Tag_Int child = new Tag_Int("x", 1);
        ArrayList<Tag<?>> input = new ArrayList<>(List.of(child));
        Tag_Compound root = new Tag_Compound("root", input);
        input.add(new Tag_Int("x", 2));
        root.getData().add(new Tag_Int("x", 3));
        assertEquals(1, root.size());
        root.setData(new ArrayList<>());
        input = new ArrayList<>(List.of(child));
        root.setData(input);
        input.clear();
        root.getData().clear();
        assertEquals(1, root.size());
        child.setData(4);
        assertEquals(4, root.getInt("x"));
    }

    @Test
    void explicitSettersReplaceKeysIncludingNormalizedNullName()
    {
        Tag_Compound root = new Tag_Compound("");
        root.addInt("x", 1);
        root.addInt(null, 2);
        root.setString("x", "replacement");
        root.setInt(null, 3);
        assertEquals(2, root.size());
        assertEquals("replacement", root.getString("x"));
        assertEquals(3, root.getInt("null"));
        root.setTag(new Tag_Long("x", 5L));
        assertEquals(2, root.size());
        assertEquals(5L, root.getLong("x"));
    }

    @Test
    void rejectsRenameWithoutChangingNameOrPayload()
    {
        Tag_Int child = new Tag_Int("y", 2);
        Tag_Compound root = new Tag_Compound("root");
        root.addInt("x", 1);
        root.addInt("null", 3);
        root.addTag(child);
        assertThrows(IllegalArgumentException.class, () -> child.setName("x"));
        assertThrows(IllegalArgumentException.class, () -> child.setName(null));
        assertThrows(IllegalArgumentException.class, () -> child.editTag(new Tag_Int("x", 9)));
        assertEquals("y", child.getName());
        assertEquals(2, child.getData());
        child.setName("y");
        child.setName("z");
        assertEquals(2, root.getInt("z"));
        assertFalse(root.hasTag("y"));
    }

    @Test
    void sharedChildRenameChecksEveryContainingCompound()
    {
        Tag_Int shared = new Tag_Int("shared", 1);
        Tag_Compound first = new Tag_Compound("first", new ArrayList<>(List.of(shared)));
        Tag_Compound second = new Tag_Compound("second");
        second.addAllTags(List.of(shared, new Tag_Int("taken", 2)));
        assertThrows(IllegalArgumentException.class, () -> shared.setName("taken"));
        assertTrue(first.hasTag("shared"));
        assertTrue(second.hasTag("shared"));
        second.removeTag("shared");
        shared.setName("taken");
        assertEquals(1, first.getInt("taken"));
        assertEquals(2, second.getInt("taken"));
    }

    @Test
    void removedChildrenAreFreeToRenameAfterEveryRemovalRoute()
    {
        for (int route = 0; route < 6; route++)
        {
            Tag_Int child = new Tag_Int("old", 1);
            Tag_Compound parent = new Tag_Compound("parent");
            parent.addTag(child);
            parent.addInt("taken", 2);
            switch (route)
            {
                case 0 -> parent.removeTag("old");
                case 1 -> parent.clear();
                case 2 -> parent.setData(new ArrayList<>(List.of(new Tag_Int("taken", 2))));
                case 3 -> parent.setInt("old", 3);
                case 4 -> parent.removeTagByName("old");
                case 5 -> parent.removeAllTags(child);
            }
            assertDoesNotThrow(() -> child.setName("taken"));
        }
    }

    @Test
    void compoundAndListEditsRejectConflictingNameBeforeChangingData()
    {
        Tag_Compound compound = new Tag_Compound("compound");
        compound.addInt("original", 1);
        Tag_List list = new Tag_List("list", 3);
        list.addInt("", 1);
        Tag_Compound parent = new Tag_Compound("parent");
        parent.addAllTags(List.of(compound, list, new Tag_Int("taken", 9)));
        Tag_Compound source = new Tag_Compound("taken");
        source.addInt("replacement", 2);
        assertThrows(IllegalArgumentException.class, () -> compound.editTag(source));
        assertEquals("compound", compound.getName());
        assertEquals(1, compound.getInt("original"));
        Tag_List sourceList = new Tag_List("taken", 3);
        sourceList.addInt("", 2);
        assertThrows(IllegalArgumentException.class, () -> list.editTag(sourceList));
        assertEquals("list", list.getName());
        assertEquals(1, list.getData().getFirst().getData());
    }

    @Test
    void successfulEditsAndReplacementRegisterNewChildren()
    {
        Tag_Int shared = new Tag_Int("x", 1);
        Tag_Compound source = new Tag_Compound("source", new ArrayList<>(List.of(shared)));
        Tag_Compound target = new Tag_Compound("target");
        target.editTag(source);
        target.addInt("taken", 2);
        assertThrows(IllegalArgumentException.class, () -> shared.setName("taken"));
        Tag_Int replaced = new Tag_Int("taken", 3);
        target.setTag(replaced);
        assertThrows(IllegalArgumentException.class, () -> replaced.setName("x"));
    }

    @Test
    void invalidCompoundEditPreservesNameAndChildren()
    {
        Tag_Compound target = new Tag_Compound("target");
        target.addInt("original", 1);
        Tag_Compound corrupt = corruptCompound("source", "x");
        assertThrows(IllegalArgumentException.class, () -> target.editTag(corrupt));
        assertEquals("target", target.getName());
        assertEquals(1, target.getInt("original"));
    }

    @Test
    void buildersRejectDuplicateKeys()
    {
        assertThrows(IllegalArgumentException.class,
                () -> NBTBuilder.compound("root").addInt("x", 1).addInt("x", 2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{x:1,x:2}", "{x:1,'x':2}", "{'':1,\"\":2}",
            "{outer:{x:1,x:2}}", "[{x:1,x:2}]"})
    void snbtRejectsDuplicateKeysWithParserContext(String input)
    {
        SNBTException error = assertThrows(SNBTException.class, () -> new SNBTParser(input).parse());
        assertTrue(error.getMessage().contains("Duplicate compound key"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"x", "", "null", "café"})
    void binaryRejectsIndependentlyEncodedDuplicateKeys(String key) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes))
        {
            out.writeByte(10);
            out.writeUTF("");
            out.writeByte(3);
            out.writeUTF(key);
            out.writeInt(1);
            out.writeByte(8);
            out.writeUTF(key);
            out.writeUTF("second");
            out.writeByte(0);
        }
        IOException error = assertThrows(IOException.class, () -> new NBTReader(
                new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()))).read());
        assertTrue(error.getMessage().contains("Duplicate compound key: " + key));
        assertTrue(error.getMessage().contains("[Position:"));
    }

    @Test
    void permitsRepeatedNamesAcrossContainersAndInLists() throws Exception
    {
        var parsed = new SNBTParser("{x:1,a:{x:2},b:{x:3}}").parse();
        Tag_Compound root = (Tag_Compound) parsed;
        Tag_List list = new Tag_List("list", 3);
        list.addInt("x", 4);
        list.addInt("x", 5);
        root.addTag(list);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(root);
        var restored = new NBTReader(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()))).read();
        assertEquals(1, restored.getInt("x"));
        assertEquals(2, restored.getCompound("a").getInt("x"));
        assertEquals(3, restored.getCompound("b").getInt("x"));
        assertEquals(2, restored.getList("list").size());
        assertDoesNotThrow(() -> new SNBTSerializer().serialize(root));
    }

    @ParameterizedTest
    @ValueSource(strings = {"x", ""})
    void serializersRejectCorruptedSubclassBeforeOutput(String key)
    {
        Tag_Compound root = new Tag_Compound("root");
        root.addTag(corruptCompound("nested", key));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        IOException error = assertThrows(IOException.class,
                () -> new NBTWriter(new DataOutputStream(bytes)).write(root));
        assertTrue(error.getMessage().contains("Duplicate compound key"));
        assertEquals(0, bytes.size());
        assertThrows(IllegalArgumentException.class, () -> new SNBTSerializer().serialize(root));
    }

    @Test
    void fileValidationFailurePreservesDestination(@TempDir Path directory) throws IOException
    {
        Path file = directory.resolve("save.nbt");
        byte[] original = {10, 0, 0, 0};
        Files.write(file, original);
        assertThrows(IOException.class, () -> new NBTWriter(file.toFile(), Compression_Types.NONE)
                .write(corruptCompound("root", "x")));
        assertArrayEquals(original, Files.readAllBytes(file));
        assertFalse(Files.exists(directory.resolve("save.nbt.bak")));
    }
}
