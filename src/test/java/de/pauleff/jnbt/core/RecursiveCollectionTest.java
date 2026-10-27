package de.pauleff.jnbt.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecursiveCollectionTest
{
    private static final Tag_Int TARGET = new Tag_Int("x", 7);

    private static Collection_Tag branches(boolean listRoot)
    {
        Collection_Tag root = listRoot ? new Tag_List("root", 10) : new Tag_Compound("root");
        Tag_Compound left = new Tag_Compound("left");
        Tag_List nested = new Tag_List("nested", 10);
        Tag_Compound deep = new Tag_Compound("deep");
        deep.addInt("x", 7);
        nested.addTag(deep);
        left.addTag(nested);
        left.addInt("x", 7);
        Tag_Compound right = new Tag_Compound("right");
        right.addInt("x", 7);
        root.addTag(left);
        root.addTag(right);
        return root;
    }

    private static List<Tag<?>> matches(Collection_Tag root, boolean byName)
    {
        return byName ? root.getAllTagsByName("x") : root.getAllTags(TARGET);
    }

    private static Tag<?> first(Collection_Tag root, boolean byName)
    {
        return byName ? root.getTagByName("x") : root.getTag(TARGET);
    }

    private static Collection_Tag removeFirst(Collection_Tag root, boolean byName)
    {
        return byName ? root.removeTagByName("x") : root.removeTag(TARGET);
    }

    private static void removeAll(Collection_Tag root, boolean byName)
    {
        if (byName) root.removeAllTagsByName("x");
        else root.removeAllTags(TARGET);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void findAllIncludesMatchingContainersAndTheirDescendants(boolean listRoot)
    {
        Collection_Tag root = listRoot ? new Tag_List("x", 10) : new Tag_Compound("x");
        Tag_Compound outer = new Tag_Compound("x");
        Tag_List inner = new Tag_List("x", 3);
        Tag_Int leaf = new Tag_Int("x", 7);
        inner.addTag(leaf);
        outer.addTag(inner);
        root.addTag(outer);

        List<Tag<?>> found = root.getAllTagsByName("x");
        assertEquals(3, found.size());
        assertSame(outer, found.get(0));
        assertSame(inner, found.get(1));
        assertSame(leaf, found.get(2));
        assertSame(outer, root.getTagByName("x"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void recursiveFindUsesDepthFirstOrderForBothRootTypes(boolean listRoot)
    {
        for (boolean byName : new boolean[]{false, true})
        {
            Collection_Tag root = branches(listRoot);
            Tag_Compound left = (Tag_Compound) root.getData().getFirst();
            Tag_List nested = (Tag_List) left.getTag("nested");
            Tag_Compound deep = (Tag_Compound) nested.get(0);
            Tag_Compound right = (Tag_Compound) root.getData().getLast();
            List<Tag<?>> found = matches(root, byName);
            assertEquals(3, found.size());
            assertSame(deep.getTag("x"), found.get(0));
            assertSame(left.getTag("x"), found.get(1));
            assertSame(right.getTag("x"), found.get(2));
            assertSame(found.getFirst(), first(root, byName));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void removeFirstStopsAcrossAncestorsAndSiblingBranches(boolean listRoot)
    {
        for (boolean byName : new boolean[]{false, true})
        {
            Collection_Tag root = branches(listRoot);
            List<Tag<?>> before = matches(root, byName);
            assertSame(root, removeFirst(root, byName));
            List<Tag<?>> remaining = matches(root, byName);
            assertEquals(2, remaining.size());
            assertSame(before.get(1), remaining.get(0));
            assertSame(before.get(2), remaining.get(1));
            removeFirst(root, byName);
            assertSame(before.get(2), first(root, byName));
            removeFirst(root, byName);
            assertNull(first(root, byName));
            assertSame(root, removeFirst(root, byName));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void removeAllVisitsEveryBranchAndPreservesContainers(boolean listRoot)
    {
        for (boolean byName : new boolean[]{false, true})
        {
            Collection_Tag root = branches(listRoot);
            List<Tag<?>> containers = root.getData();
            removeAll(root, byName);
            assertTrue(matches(root, byName).isEmpty());
            assertEquals(containers, root.getData());
            removeAll(root, byName);
            assertEquals(containers, root.getData());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void removingMatchingContainerDetachesItsSubtree(boolean all)
    {
        Tag_Compound root = new Tag_Compound("root");
        Tag_Compound child = new Tag_Compound("x");
        Tag_Int leaf = new Tag_Int("x", 7);
        child.addTag(leaf);
        root.addTag(child);
        Tag_Compound sibling = new Tag_Compound("sibling");
        sibling.addInt("x", 7);
        root.addTag(sibling);

        if (all) root.removeAllTagsByName("x");
        else root.removeTagByName("x");
        assertEquals(1, root.size());
        assertSame(sibling, root.getData().getFirst());
        assertSame(leaf, child.getTag("x"));
        assertEquals(all ? 0 : 1, sibling.size());
        child.setName("sibling"); // Detached children no longer conflict with the former parent.
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void firstOperationsNeverInspectLaterTags(boolean byName)
    {
        Collection_Tag root = branches(false);
        Tag_Int unvisited = new Tag_Int("later", 0)
        {
            @Override
            public String getName()
            {
                fail("First-match operation visited a later tag's name");
                return super.getName();
            }

            @Override
            public boolean equals(Object other)
            {
                fail("First-match operation compared a later tag");
                return false;
            }
        };
        // Use a list so insertion does not inspect the child's name.
        Tag_List later = new Tag_List("later", 3);
        later.addTag(unvisited);
        root.addTag(later);
        assertNotNull(first(root, byName));
        removeFirst(root, byName);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void missingMatchesLeaveTreeUnchanged(boolean listRoot)
    {
        Collection_Tag root = branches(listRoot);
        List<Tag<?>> before = root.getData();
        assertNull(root.getTagByName("missing"));
        assertNull(root.getTag(new Tag_Int("x", 8)));
        assertTrue(root.getAllTagsByName("missing").isEmpty());
        assertTrue(root.getAllTags(new Tag_Int("x", 8)).isEmpty());
        root.removeTagByName("missing");
        root.removeAllTagsByName("missing");
        root.removeTag(new Tag_Int("x", 8));
        root.removeAllTags(new Tag_Int("x", 8));
        assertEquals(before, root.getData());
        assertEquals(3, matches(root, false).size());
    }

    @Test
    void equalitySearchStillDescendsIntoAMatchingCollection()
    {
        // A collection subtype may define equality independently of its children.
        Tag_Compound child = new Tag_Compound("child")
        {
            @Override
            public boolean equals(Object other)
            {
                return other == TARGET || super.equals(other);
            }
        };
        Tag_Int leaf = new Tag_Int("x", 7);
        child.addTag(leaf);
        Tag_Compound root = new Tag_Compound("root");
        root.addTag(child);
        List<Tag<?>> found = root.getAllTags(TARGET);
        assertEquals(2, found.size());
        assertSame(child, found.getFirst());
        assertSame(leaf, found.getLast());
        assertSame(child, root.getTag(TARGET));
        root.removeAllTags(TARGET);
        assertEquals(0, root.size());
        assertSame(leaf, child.getTag("x"));
    }
}
