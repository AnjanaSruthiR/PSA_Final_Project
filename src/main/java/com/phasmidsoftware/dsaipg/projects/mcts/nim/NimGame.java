package com.phasmidsoftware.dsaipg.projects.mcts.nim;

import java.util.ArrayList;
import java.util.List;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Game;
import com.phasmidsoftware.dsaipg.projects.mcts.core.RandomState;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

public class NimGame implements Game<NimGame> {

    private final List<Integer> initialHeaps;

    public NimGame(List<Integer> initialHeaps) {
        this.initialHeaps = new ArrayList<>(initialHeaps); // Defensive copy
    }

    @Override
    public State<NimGame> start() {
        return new NimState(this, new ArrayList<>(initialHeaps), 0, 
                java.util.Optional.empty(), new RandomState(100));
    }

    @Override
    public int opener() {
        return 0; 
    }

    @Override
    public String toString() {
        return "NimGame with initial heaps: " + initialHeaps;
    }
}
