package com.phasmidsoftware.dsaipg.projects.mcts.nim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

public class NimMain {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int humanPlayer;
        while (true) {
            System.out.println("Choose your role:\n0 - You play first (Player 0)\n1 - You play second (Player 1)");
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid: enter 0 or 1.");
                scanner.next();
                continue;
            }
            humanPlayer = scanner.nextInt();
            if (humanPlayer == 0 || humanPlayer == 1) break;
            System.out.println("Invalid choice; must be 0 or 1.");
        }

        System.out.println("Enter MCTS iterations (non‑negative integer, e.g. 500):");
        int mctsIterations;
        while (true) {
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid: enter a non‑negative integer.");
                scanner.next();
                continue;
            }
            mctsIterations = scanner.nextInt();
            if (mctsIterations >= 0) break;
            System.out.println("Must be ≥ 0. Try again.");
        }
        scanner.nextLine();

        List<Integer> heaps;
        while (true) {
            System.out.println("Enter heap sizes (space separated, each > 0), e.g. \"1 3 5 7\":");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            boolean ok = true;
            heaps = new ArrayList<>();
            for (String p : parts) {
                try {
                    int h = Integer.parseInt(p);
                    if (h <= 0) throw new NumberFormatException();
                    heaps.add(h);
                } catch (NumberFormatException e) {
                    System.out.printf(" “%s” isn’t a valid positive integer.%n", p);
                    ok = false;
                    break;
                }
            }
            if (ok) break;
        }

        // 4) Start the game
        NimGame game = new NimGame(heaps);
        State<NimGame> state = game.start();

        // 5) Game loop
        while (!state.isTerminal()) {
            System.out.println("\nCurrent State: " + state);
            int current = state.player();

            if (current == humanPlayer) {
                // Human turn
                System.out.printf("Your turn (Player %d) — enter heapIndex and count:%n", current);
                String moveLine = scanner.nextLine().trim();
                String[] tok = moveLine.split("\\s+");
                if (tok.length != 2) {
                    System.out.println("Please enter exactly two numbers.");
                    continue;
                }
                try {
                    int heapIndex = Integer.parseInt(tok[0]);
                    int count     = Integer.parseInt(tok[1]);
                    state = state.next(new NimMove(current, heapIndex, count));
                } catch (Exception e) {
                    System.out.println("Invalid move: " + e.getMessage());
                }
            } else {
                // AI turn
                System.out.printf("MCTS (Player %d) is thinking...%n", current);
                state = mctsDecision(state, mctsIterations);
                System.out.println(" → MCTS chooses: " + state);
            }
        }

        // 6) Announce winner
        int winner = state.winner().orElse(-1);
        System.out.printf("%nGame Over! 🎉 Winner: Player %d%n", winner);
    }

    private static State<NimGame> mctsDecision(State<NimGame> state, int iterations) {
        NimNode root = new NimNode(state, null);
        int target   = state.player();
        for (int i = 0; i < iterations; i++) {
            simulate(root, 0, Integer.MAX_VALUE, target);
        }

        List<NimNode> viable = new ArrayList<>();
        for (Node<NimGame> c : root.children()) {
            NimNode n = (NimNode) c;
            if (n.playouts() > 0) viable.add(n);
        }
        if (viable.isEmpty()) return state;

        double bestRate = viable.stream()
                                .mapToDouble(n -> (double) n.wins() / n.playouts())
                                .max().orElse(-1);
        Collections.shuffle(viable);
        return viable.stream()
                     .filter(n -> Math.abs((double)n.wins()/n.playouts() - bestRate) < 1e-6)
                     .findFirst().get()
                     .state();
    }

    private static int simulate(NimNode node, int depth, int maxDepth, int target) {
        if (node.state().isTerminal() || depth >= maxDepth) {
            return node.state().winner().map(w -> w == target ? 1 : 0).orElse(0);
        }
        if (node.children().isEmpty()) {
            node.explore();
            List<NimNode> kids = new ArrayList<>();
            for (Node<NimGame> c : node.children()) kids.add((NimNode)c);
            NimNode child = kids.get(node.state().random().nextInt(kids.size()));

            State<NimGame> rollout = child.state();
            while (!rollout.isTerminal()) {
                rollout = rollout.next(rollout.chooseMove(rollout.player()));
            }
            int reward = rollout.winner().map(w -> w == target ? 1 : 0).orElse(0);
            child.addPlayout(reward);
            return reward;
        }
        NimNode next = selectUCT(node, Math.sqrt(2));
        int reward = simulate(next, depth + 1, maxDepth, target);
        node.addPlayout(reward);
        return reward;
    }

    private static NimNode selectUCT(NimNode parent, double C) {
        double logN = Math.log(Math.max(1, parent.playouts()));
        return parent.children().stream()
                     .map(n -> (NimNode)n)
                     .max(Comparator.comparingDouble(child -> {
                         double wi = child.wins(), ni = child.playouts();
                         return (wi/ni) + C * Math.sqrt(logN/ni);
                     }))
                     .orElseThrow(() -> new IllegalStateException("UCT: no children"));
    }
}
