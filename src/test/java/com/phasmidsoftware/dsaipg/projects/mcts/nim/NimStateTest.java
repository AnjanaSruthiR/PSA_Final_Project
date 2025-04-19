package com.phasmidsoftware.dsaipg.projects.mcts.nim;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

public class NimStateTest {

    @Test
    public void testMovesSingleHeap() {
        NimGame game = new NimGame(List.of(1));
        State<NimGame> state = game.start();
        assertFalse("Should not be terminal at start", state.isTerminal());
        Collection<Move<NimGame>> moves = state.moves(state.player());
        assertEquals("Exactly one move for [1]", 1, moves.size());
        NimMove move = (NimMove) moves.iterator().next();
        assertEquals("Heap index must be 0", 0, move.getHeapIndex());
        assertEquals("Must remove exactly 1", 1, move.getObjectsToRemove());
    }

    @Test
    public void testMovesMultipleHeaps() {
        NimGame game = new NimGame(List.of(2, 1));
        State<NimGame> state = game.start();
        Collection<Move<NimGame>> moves = state.moves(state.player());
        // Heap0(size2): remove 1 or 2 → 2 moves; Heap1(size1): remove 1 → 1 move; total = 3
        assertEquals("Three legal moves for [2,1]", 3, moves.size());
    }

    @Test
    public void testNextTransition() {
        NimGame game = new NimGame(List.of(2, 1));
        State<NimGame> state = game.start();
        NimMove move = new NimMove(state.player(), 0, 2);  // remove all from heap 0
        State<NimGame> next = state.next(move);

        List<Integer> heaps = ((NimState) next).getHeaps();
        assertEquals("Heaps should become [0,1]", List.of(0, 1), heaps);
        assertEquals("Turn should switch to player 1", 1, next.player());
        assertFalse("Not terminal yet", next.isTerminal());
    }

    @Test
    public void testTerminalAndWinner() {
        NimGame game = new NimGame(List.of(1));
        State<NimGame> state = game.start();
        NimMove move = new NimMove(state.player(), 0, 1);
        State<NimGame> next = state.next(move);

        assertTrue("Should be terminal when heaps empty", next.isTerminal());
        Optional<Integer> winner = next.winner();
        assertTrue("Winner must be present", winner.isPresent());
        // Winner is the player who made the last removal
        assertEquals("Winner should match the mover", state.player(), (int) winner.get());
    }
}
