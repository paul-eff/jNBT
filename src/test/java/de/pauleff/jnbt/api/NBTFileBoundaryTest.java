package de.pauleff.jnbt.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class NBTFileBoundaryTest
{
    @TempDir
    Path tempDir;

    private static Stream<Arguments> missingPathParents()
    {
        return Stream.of(
                Arguments.of("{wrapper:{a:{x:1}}}", "a.x"),
                Arguments.of("{wrapper:{a:{x:1}},a:5}", "a.x"),
                Arguments.of("{a:{hidden:{b:{x:1}}}}", "a.b.x"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void pathOperationsTouchOnlyTheSpecifiedParent(boolean remove) throws IOException
    {
        File file = write("input.dat", "{wrapper:{a:{x:1}},a:{nested:{x:2},x:3},x:4}");
        if (remove)
        {
            NBTFileFactory.removeTag(file, "a.x");
        } else
        {
            NBTFileFactory.updateTag(file, "a.x", 9);
        }
        ICompoundTag root = NBTFileFactory.readNBTFile(file);
        assertEquals(1, root.getCompound("wrapper").getCompound("a").getInt("x"));
        assertEquals(2, root.getCompound("a").getCompound("nested").getInt("x"));
        assertEquals(4, root.getInt("x"));
        assertEquals(!remove, root.getCompound("a").hasTag("x"));
        if (!remove)
        {
            assertEquals(9, root.getCompound("a").getInt("x"));
        }
    }

    @ParameterizedTest
    @MethodSource("missingPathParents")
    void missingOrWrongTypePathParentDoesNotSearchDescendants(String input, String path) throws IOException
    {
        File file = write("input.dat", input);
        byte[] original = Files.readAllBytes(file.toPath());
        assertThrows(IllegalArgumentException.class, () -> NBTFileFactory.updateTag(file, path, 9));
        assertThrows(IllegalArgumentException.class, () -> NBTFileFactory.removeTag(file, path));
        assertArrayEquals(original, Files.readAllBytes(file.toPath()));
    }

    @Test
    void absentLeafIsCreatedAndRemovedLocally() throws IOException
    {
        File file = write("input.dat", "{a:{nested:{x:2}}}");
        NBTFileFactory.updateTag(file, "a.x", 9);
        ICompoundTag parent = NBTFileFactory.readNBTFile(file).getCompound("a");
        assertEquals(9, parent.getInt("x"));
        assertEquals(2, parent.getCompound("nested").getInt("x"));
        NBTFileFactory.removeTag(file, "a.x");
        NBTFileFactory.removeTag(file, "a.x");
        parent = NBTFileFactory.readNBTFile(file).getCompound("a");
        assertFalse(parent.hasTag("x"));
        assertEquals(2, parent.getCompound("nested").getInt("x"));
    }

    @Test
    void nestedMergePreservesUnrelatedDescendants() throws IOException
    {
        File base = write("base.dat", "{wrapper:{a:{x:1}},a:{nested:{x:2},x:3,keep:5},x:4}");
        File source = write("source.dat", "{a:{x:9,added:6},x:8}");
        File output = tempDir.resolve("output.dat").toFile();
        NBTFileFactory.mergeNBTFiles(base, source, output);
        ICompoundTag root = NBTFileFactory.readNBTFile(output);
        assertEquals(1, root.getCompound("wrapper").getCompound("a").getInt("x"));
        ICompoundTag a = root.getCompound("a");
        assertEquals(2, a.getCompound("nested").getInt("x"));
        assertEquals(9, a.getInt("x"));
        assertEquals(5, a.getInt("keep"));
        assertEquals(6, a.getInt("added"));
        assertEquals(8, root.getInt("x"));
    }

    @Test
    void mergeResolvesTypeConflictsAtTheCurrentLevel() throws IOException
    {
        File base = write("base.dat", "{hidden:{settings:{old:1},value:2},settings:3,value:{old:4}}");
        File source = write("source.dat", "{settings:{new:5},value:6}");
        File output = tempDir.resolve("output.dat").toFile();
        NBTFileFactory.mergeNBTFiles(base, source, output);
        ICompoundTag root = NBTFileFactory.readNBTFile(output);
        assertEquals(5, root.getCompound("settings").getInt("new"));
        assertFalse(root.getCompound("settings").hasTag("old"));
        assertEquals(6, root.getInt("value"));
        assertEquals(1, root.getCompound("hidden").getCompound("settings").getInt("old"));
        assertEquals(2, root.getCompound("hidden").getInt("value"));
    }

    private File write(String filename, String snbt) throws IOException
    {
        File file = tempDir.resolve(filename).toFile();
        ICompoundTag root = (ICompoundTag) assertDoesNotThrow(() -> NBTFactory.parseFromSNBT(snbt));
        NBTFileFactory.writeNBTFile(file, root);
        return file;
    }
}
