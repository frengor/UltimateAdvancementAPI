package com.fren_gor.ultimateAdvancementAPI;

import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class RemovalRootsTest {
    private record Node(String key, Node parent) {}

    @Test
    public void removesParentOnceInsteadOfRedundantDescendants() {
        Node root = new Node("root", null);
        Node child = new Node("child", root);
        Node leaf = new Node("leaf", child);
        assertEquals(Set.of("root"), select(leaf, root, child));
    }

    @Test
    public void preservesIndependentCustomTrees() {
        Node root = new Node("root", null);
        Node custom = new Node("custom", null);
        assertEquals(Set.of("root", "custom"), select(root, new Node("child", root), custom));
    }

    @Test
    public void followsAncestorsAcrossUnsentIntermediateNodes() {
        Node root = new Node("root", null);
        Node intermediate = new Node("intermediate", root);
        Node leaf = new Node("leaf", intermediate);
        assertEquals(Set.of("root"), select(root, leaf));
    }

    @Test
    public void keepsChildWhoseAncestorsWereNotSent() {
        Node absentRoot = new Node("absent", null);
        assertEquals(Set.of("child"), select(new Node("child", absentRoot)));
    }

    @Test
    public void emptySnapshotHasNoRemovalKeys() {
        assertEquals(Set.of(), select());
    }

    private static Set<String> select(Node... nodes) {
        return RemovalRoots.select(List.of(nodes), Node::key, Node::parent);
    }
}
