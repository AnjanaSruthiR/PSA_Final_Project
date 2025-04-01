/*
 * Copyright (c) 2024. Robin Hillyard
 */

package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

/**
 * Class to represent a Monte Carlo Tree Search for TicTacToe.
 */
public class MCTS {

    public static void main(String[] args) {
        TicTacToe game = new TicTacToe(1234);
        TicTacToe.TicTacToeState initialState = (TicTacToe.TicTacToeState) game.start();
        TicTacToeNode root = new TicTacToeNode(initialState);

        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            simulate(root, game);
        }

        Node<TicTacToe> bestChild = chooseBestChild(root);
        if (bestChild != null) {
            System.out.println("Best move determined by MCTS leads to state:");
            System.out.println(bestChild.state().toString());
        } else {
            System.out.println("No best move found (root has no children).");
        }
    }

    private static void simulate(TicTacToeNode root, TicTacToe game) {
        TicTacToeNode node = root;
        List<TicTacToeNode> path = new ArrayList<>();
        path.add(node);
        while (!node.isLeaf() && !node.state().isTerminal()) {
            Collection<Node<TicTacToe>> children = node.children();
            if (children.isEmpty()) break;
            node = (TicTacToeNode) children.iterator().next();
            path.add(node);
        }

        if (!node.state().isTerminal()) {
            Collection<Move<TicTacToe>> moves = node.state().moves(node.state().player());
            for (Move<TicTacToe> move : moves) {
                node.addChild(node.state().next(move));
            }
            if (!node.children().isEmpty()) {
                node = (TicTacToeNode) node.children().iterator().next();
                path.add(node);
            }
        }

        State<TicTacToe> simulationState = node.state();
        Random random = simulationState.random();
        while (!simulationState.isTerminal()) {
            Collection<Move<TicTacToe>> moves = simulationState.moves(simulationState.player());
            List<Move<TicTacToe>> moveList = new ArrayList<>(moves);
            if (moveList.isEmpty()) break;
            Move<TicTacToe> chosenMove = moveList.get(random.nextInt(moveList.size()));
            simulationState = simulationState.next(chosenMove);
        }

        int winValue = simulationState.winner().isPresent() ? 2 : 1;
        for (TicTacToeNode n : path) {
            n.incrementPlayout();
            n.incrementWins(winValue);
        }
    }

    private static Node<TicTacToe> chooseBestChild(TicTacToeNode root) {
        Node<TicTacToe> best = null;
        double bestScore = -1;
        for (Node<TicTacToe> child : root.children()) {
            if (child.playouts() == 0) continue;
            double score = child.wins() / (double) child.playouts();
            if (score > bestScore) {
                bestScore = score;
                best = child;
            }
        }
        return best;
    }

    public MCTS(Node<TicTacToe> root) {
        this.root = root;
    }
        
    private final Node<TicTacToe> root;
}