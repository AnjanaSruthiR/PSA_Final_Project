package com.phasmidsoftware.dsaipg.projects.mcts.nim;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.Scanner;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

public class NimMain {

    public static void main(String[] args) {
        NimGame game = new NimGame(Arrays.asList(1,3,5,7));
        State<NimGame> state = game.start();

        Scanner scanner = new Scanner(System.in);

        while (!state.isTerminal()) {
            System.out.println("\nCurrent State: " + state);
            int currentPlayer = state.player();

            if (currentPlayer == 0) {
                System.out.println("MCTS (Player 0) is thinking...");
                Node<NimGame> root = new NimNode(state, null);
                for (int i = 0; i < 1000; i++) {
                    simulate(root);
                }

                Node<NimGame> best = root.children().stream()
                 .max(Comparator.comparingInt(Node::wins))
                 .orElse(root); // fallback if children are empty
                 state = best.state();
                System.out.println("MCTS chooses move → " + state);
            } else {
                // Human player's turn
                System.out.println("Your turn (Player 1). Enter heap index and count to remove:");
                int heapIndex = scanner.nextInt();
                int count = scanner.nextInt();

                try {
                    NimMove move = new NimMove(1, heapIndex, count);
                    state = state.next(move);
                } catch (Exception e) {
                    System.out.println("Invalid move! Please try again.");
                }
            }
        }

        Optional<Integer> winner = state.winner();
        System.out.println("\nGame Over! 🎉 Winner: Player " + winner.orElse(null));
    }
     public static void simulate(Node<NimGame> node) {
        if (node.isLeaf()) return;

        if (node.children().isEmpty()) {
            node.explore(); 
            return;
        }

       
        Node<NimGame> child = node.children().iterator().next();
        simulate(child);
        node.backPropagate();
    }
}
