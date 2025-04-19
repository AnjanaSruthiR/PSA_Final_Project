package com.phasmidsoftware.dsaipg.projects.mcts.nim;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

public class NimNodeTest {

    @Test
    public void testLeafBackpropagate() {
        // Create a terminal state by removing the only object
        NimGame game = new NimGame(List.of(1));
        State<NimGame> state0 = game.start();
        NimMove lastMove = new NimMove(state0.player(), 0, 1);
        State<NimGame> terminal = state0.next(lastMove);
        assertTrue("Should be terminal", terminal.isTerminal());

        NimNode leaf = new NimNode(terminal, null);
        leaf.backPropagate();
        assertEquals("Terminal node playouts should be 1", 1, leaf.playouts());
        assertEquals("Terminal node wins should be 2 (win)", 2, leaf.wins());
    }

    @Test
    public void testExploreCreatesChildren() {
        NimGame game = new NimGame(List.of(2, 1));
        State<NimGame> state = game.start();
        NimNode root = new NimNode(state, null);
        assertTrue("No children initially", root.children().isEmpty());

        // Expand the node
        root.explore();
        int expected = state.moves(state.player()).size();
        assertEquals("Children should match number of legal moves", expected, root.children().size());
    }

    @Test
    public void testAddPlayoutPropagates() {
        NimGame game = new NimGame(List.of(1, 1));
        State<NimGame> state = game.start();
        NimNode root = new NimNode(state, null);

        // Expand so we have at least one child
        root.explore();
        NimNode child = (NimNode) root.children().iterator().next();

        // Simulate a win (reward = 1) at the child level
        child.addPlayout(1);

        // Child should have one playout and one win
        assertEquals("Child playout count", 1, child.playouts());
        assertEquals("Child wins count", 1, child.wins());

        // Parent (root) should also be updated
        assertEquals("Root playout count", 1, root.playouts());
        assertEquals("Root wins count", 1, root.wins());
    }
}
