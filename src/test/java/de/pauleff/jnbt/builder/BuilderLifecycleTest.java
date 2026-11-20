package de.pauleff.jnbt.builder;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.formats.binary.NBTWriter;
import de.pauleff.jnbt.util.NBTTags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HexFormat;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BuilderLifecycleTest
{
    static Stream<Arguments> combinations()
    {
        return Stream.of(Arguments.of(false, false), Arguments.of(false, true),
                Arguments.of(true, false), Arguments.of(true, true));
    }

    @ParameterizedTest
    @MethodSource("combinations")
    void repeatedCompletionAttachesOnceAndAlwaysReturnsTheParent(boolean listParent, boolean listChild)
    {
        NBTBuilder parent = parent(listParent, listChild);
        NBTBuilder child = child(parent, listChild);
        assertSame(parent, child.getParent());
        assertEquals(0, size(parent.build()));
        assertSame(parent, child.end());
        ITag<?> attached = first(parent.build());
        assertSame(parent, child.end());
        assertSame(parent, typedEnd(child, listParent));
        assertSame(parent, child.end());
        assertEquals(1, size(parent.build()));
        assertSame(attached, first(parent.build()));
    }

    @ParameterizedTest
    @MethodSource("combinations")
    void wrongTypedHelperLeavesTheParentUnchangedAndChildUsable(boolean listParent, boolean listChild)
    {
        NBTBuilder parent = parent(listParent, listChild);
        NBTBuilder child = child(parent, listChild);
        assertThrows(IllegalStateException.class, () -> typedEnd(child, !listParent));
        assertEquals(0, size(parent.build()));
        addValue(child);
        assertSame(parent, typedEnd(child, listParent));
        assertEquals(1, size(parent.build()));
        assertValue(first(parent.build()));
    }

    @ParameterizedTest
    @MethodSource("combinations")
    void wrongTypedHelperAfterCompletionStillFailsWithoutAnotherAttachment(boolean listParent, boolean listChild)
    {
        NBTBuilder parent = parent(listParent, listChild);
        NBTBuilder child = child(parent, listChild);
        child.end();
        ITag<?> attached = first(parent.build());
        assertThrows(IllegalStateException.class, () -> typedEnd(child, !listParent));
        assertSame(parent, typedEnd(child, listParent));
        assertEquals(1, size(parent.build()));
        assertSame(attached, first(parent.build()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void failedDuplicateAttachmentCanBeRetriedAfterRemovingTheConflict(boolean listChild)
    {
        CompoundBuilder parent = NBTBuilder.compound("root").addInt("child", 99);
        NBTBuilder child = child(parent, listChild);
        ITag<?> original = parent.build().getTag("child");
        for (int attempt = 0; attempt < 2; attempt++)
        {
            assertThrows(IllegalArgumentException.class, child::end);
            assertSame(original, parent.build().getTag("child"));
            assertEquals(1, parent.build().size());
        }
        addValue(child);
        parent.removeTag("child");
        assertSame(parent, child.end());
        assertValue(parent.build().getTag("child"));
        assertSame(parent, typedEnd(child, false));
        assertEquals(1, parent.build().size());
    }

    @ParameterizedTest
    @MethodSource("combinations")
    void completionDoesNotRestoreAChildRemovedFromTheParent(boolean listParent, boolean listChild)
    {
        NBTBuilder parent = parent(listParent, listChild);
        NBTBuilder child = child(parent, listChild);
        child.end();
        ITag<?> built = parent.build();
        ITag<?> detached = first(built);
        if (built instanceof ICompoundTag compound) compound.clear();
        else ((IListTag) built).clear();
        addValue(child);
        assertValue(detached);
        assertSame(parent, child.end());
        assertSame(parent, typedEnd(child, listParent));
        assertEquals(0, size(built));
    }

    @ParameterizedTest
    @MethodSource("combinations")
    void buildsAndCompletedChildrenRemainLiveAndMutable(boolean listParent, boolean listChild)
    {
        NBTBuilder parent = parent(listParent, listChild);
        NBTBuilder child = child(parent, listChild);
        ITag<?> builtBeforeCompletion = parent.build();
        assertSame(builtBeforeCompletion, parent.build());
        child.end();
        assertEquals(1, size(builtBeforeCompletion));
        ITag<?> attached = first(builtBeforeCompletion);
        addValue(child);
        assertValue(attached);
        assertSame(builtBeforeCompletion, parent.build());
        if (attached instanceof ICompoundTag compound) compound.setInt("value", 8);
        else ((IListTag) attached).addInt("", 8);
        assertEquals(listChild ? 2 : 1, size(first(parent.build())));
    }

    @ParameterizedTest
    @MethodSource("combinations")
    void nestedBuildRemainsRejectedBeforeAndAfterCompletion(boolean listParent, boolean listChild)
    {
        NBTBuilder parent = parent(listParent, listChild);
        NBTBuilder child = child(parent, listChild);
        assertThrows(IllegalStateException.class, child::build);
        assertEquals(0, size(parent.build()));
        child.end();
        assertThrows(IllegalStateException.class, child::build);
        assertSame(parent, child.getParent());
        assertEquals(1, size(parent.build()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rootsRejectAllCompletionMethodsAndRemainEditable(boolean listRoot)
    {
        NBTBuilder root = listRoot ? NBTBuilder.list("root", NBTTags.Tag_Int) : NBTBuilder.compound("root");
        assertThrows(IllegalStateException.class, root::end);
        assertThrows(IllegalStateException.class, () -> typedEnd(root, false));
        assertThrows(IllegalStateException.class, () -> typedEnd(root, true));
        addValue(root);
        assertValue(root.build());
        assertSame(root.build(), root.build());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void incompatibleListParentRejectsAttachmentWithoutChangingItsContents(boolean listChild)
    {
        ListBuilder parent = NBTBuilder.list("root", NBTTags.Tag_Int).addInt("", 1);
        NBTBuilder child = listChild ? new ListBuilder("child", NBTTags.Tag_Int, parent)
                : new CompoundBuilder("child", parent);
        for (int attempt = 0; attempt < 2; attempt++)
        {
            assertThrows(IllegalArgumentException.class, child::end);
            assertEquals(1, parent.build().size());
            assertEquals(1, parent.build().get(0).getData());
        }
        parent.addInt("", 2);
        assertEquals(2, parent.build().size());
    }

    @Test
    void descendantsCanCompleteAfterAnAncestorWasAttached()
    {
        CompoundBuilder root = NBTBuilder.compound("root");
        CompoundBuilder branch = root.addCompound("branch");
        ListBuilder items = branch.addList("items", NBTTags.Tag_Compound);
        CompoundBuilder item = items.addCompound("item");
        ICompoundTag built = root.build();
        branch.endCompound();
        assertEquals(0, built.getCompound("branch").size());
        items.endCompound();
        assertEquals(0, built.getCompound("branch").getList("items").size());
        item.addInt("value", 7).endList();
        assertEquals(7, ((ICompoundTag) built.getCompound("branch").getList("items").get(0)).getInt("value"));
        assertSame(root, branch.end());
        assertSame(branch, items.end());
        assertSame(items, item.end());
    }

    @Test
    void existingCompoundConstructorRetainsItsLiveObject()
    {
        Tag_Compound existing = new Tag_Compound("child");
        ListBuilder parent = NBTBuilder.list("root", NBTTags.Tag_Compound);
        CompoundBuilder child = new CompoundBuilder(existing, parent);
        child.endList();
        child.addInt("value", 7);
        assertSame(existing, parent.build().get(0));
        assertEquals(7, existing.getInt("value"));
        assertSame(parent, child.end());
        assertEquals(1, parent.build().size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void repeatedCompletionMatchesIndependentBinaryFixtures(boolean listChild) throws Exception
    {
        CompoundBuilder root = NBTBuilder.compound("");
        NBTBuilder child = child(root, listChild);
        addValue(child);
        child.end();
        typedEnd(child, false);
        String hex = listChild ? "0a0000 0900056368696c64 03 00000001 00000007 00"
                : "0a0000 0a00056368696c64 03000576616c7565 00000007 00 00";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(root.build());
        assertArrayEquals(HexFormat.of().parseHex(hex.replace(" ", "")), bytes.toByteArray());
    }

    private static NBTBuilder parent(boolean listParent, boolean listChild)
    {
        return listParent ? NBTBuilder.list("root", listChild ? NBTTags.Tag_List : NBTTags.Tag_Compound)
                : NBTBuilder.compound("root");
    }

    private static NBTBuilder child(NBTBuilder parent, boolean listChild)
    {
        if (parent instanceof CompoundBuilder compound)
            return listChild ? compound.addList("child", NBTTags.Tag_Int) : compound.addCompound("child");
        ListBuilder list = (ListBuilder) parent;
        return listChild ? list.addList("child", NBTTags.Tag_Int) : list.addCompound("child");
    }

    private static NBTBuilder typedEnd(NBTBuilder child, boolean listParent)
    {
        if (child instanceof CompoundBuilder compound)
            return listParent ? compound.endList() : compound.endCompound();
        ListBuilder list = (ListBuilder) child;
        return listParent ? list.endList() : list.endCompound();
    }

    private static void addValue(NBTBuilder builder)
    {
        if (builder instanceof CompoundBuilder compound) compound.setInt("value", 7);
        else ((ListBuilder) builder).addInt("", 7);
    }

    private static int size(ITag<?> tag)
    {
        return tag instanceof ICompoundTag compound ? compound.size() : ((IListTag) tag).size();
    }

    private static ITag<?> first(ITag<?> tag)
    {
        return tag instanceof ICompoundTag compound ? compound.getTag("child") : ((IListTag) tag).get(0);
    }

    private static void assertValue(ITag<?> tag)
    {
        assertEquals(7, tag instanceof ICompoundTag compound ? compound.getInt("value") : ((IListTag) tag).get(0).getData());
    }
}
