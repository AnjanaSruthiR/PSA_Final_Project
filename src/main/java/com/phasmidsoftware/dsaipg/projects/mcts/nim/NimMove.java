package com.phasmidsoftware.dsaipg.projects.mcts.nim;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;

public class NimMove implements Move<NimGame> {
    private final int player;
    private final int heapIndex;
    private final int objectsToRemove;

    public NimMove(int player, int heapIndex, int objectsToRemove) {
        this.player = player;
        this.heapIndex = heapIndex;
        this.objectsToRemove = objectsToRemove;
    }

    @Override
    public int player() {
        return player;
    }

    public int getHeapIndex() {
        return heapIndex;
    }

    public int getObjectsToRemove() {
        return objectsToRemove;
    }

    @Override
    public String toString() {
        return "Player " + player + " removes " + objectsToRemove + " from heap " + heapIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NimMove)) return false;
        NimMove other = (NimMove) o;
        return player == other.player &&
               heapIndex == other.heapIndex &&
               objectsToRemove == other.objectsToRemove;
    }

    @Override
    public int hashCode() {
        return 31 * heapIndex + 17 * objectsToRemove + player;
    }
}