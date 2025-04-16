package com.phasmidsoftware.dsaipg.projects.mcts.nim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

public class NimNode implements Node<NimGame> {

    private final State<NimGame> state;
    private final List<Node<NimGame>> children = new ArrayList<>();
    private final boolean white;
    private int wins = 0;
    private int playouts = 0;
    private final NimNode parent;


    /*public NimNode(State<NimGame> state) {
        this.state = state;
        this.white = state.player() == state.game().opener();
    }*/

    public NimNode(State<NimGame> state, NimNode parent) {
        this.state = state;
        this.parent = parent;
        this.white = state.player() == state.game().opener();
    }

    @Override
    public boolean isLeaf() {
        return state.isTerminal();
    }

    @Override
    public State<NimGame> state() {
        return state;
    }

    @Override
    public boolean white() {
        return white;
    }

    @Override
    public Collection<Node<NimGame>> children() {
        return children;
    }

    @Override
    public void addChild(State<NimGame> childState) {
        children.add(new NimNode(childState, this));
        System.out.println("Added child: " + childState);

    }

    @Override
    public void backPropagate() {
        if (isLeaf()) {
            playouts = 1;
            int lastPlayer = 1 - state.player(); // the player who just made the move
            wins = state.winner().map(w -> w == lastPlayer ? 2 : 0).orElse(1);

        } else {
            wins = 0;
            playouts = 0;
            for (Node<NimGame> child : children) {
                child.backPropagate();
                wins += child.wins();
                playouts += child.playouts();
            }
        }
    }

    @Override
    public int wins() {
        return wins;
    }

    @Override
    public int playouts() {
        return playouts;
    }

    @Override
    public String toString() {
        return "NimNode{state=" + state + ", wins=" + wins + ", playouts=" + playouts + "}";
    }

    public void addPlayout(int reward) {
        this.wins += reward;
        this.playouts += 1;
    
        // Recursively update parent if exists
        if (parent != null) {
            parent.addPlayout(reward);
        }
    }
}

