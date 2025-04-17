package com.phasmidsoftware.dsaipg.projects.mcts.nim;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;


public class NimExperiment {
    public static void main(String[] args) {
        automatedExperiment();
    }

    public static void automatedExperiment() {
        int games = 1000;
        List<List<Integer>> heapConfigs = List.of(
            List.of(1, 2),
            List.of(1, 3, 5, 7),
            List.of(2, 2, 2, 2),
            List.of(3, 4, 5),
            List.of(5, 5, 5), 
            List.of(1, 3, 5, 7)

        );

        int[][] simulationPairs = {
            {500, 100},
            {500, 250},
            {500, 500}, 
            {500, 0},     // MCTS vs Random
            {0, 500},    // Random vs MCTS
            {500,5000}
        };

       List<String> summary = new ArrayList<>();

        for (List<Integer> heaps : heapConfigs) {
            for (int[] pair : simulationPairs) {
                int simsP0 = pair[0];
                int simsP1 = pair[1];
                String label = "MCTS(" + simsP0 + ") vs MCTS(" + simsP1 + ") on " + heaps;
                String result = runMatchups(games, simsP0, simsP1, heaps, label);
                summary.add(result);
            }
        }

        

        System.out.println("\n========= EXPERIMENT SUMMARY =========");
        for (String s : summary) System.out.println(s);

        try (PrintWriter writer = new PrintWriter("nim_experiment_summary.csv")) {
            writer.println("Experiment,P0 Win %,P1 Win %,Avg Time (ms)");
            for (String line : summary) {
                String[] parts = line.split("\\|");
                String expName = parts[0].trim();
                String p0 = parts[1].trim().replace("P0: ", "").replace("%", "");
                String p1 = parts[2].trim().replace("P1: ", "").replace("%", "");
                String time = parts[3].trim().replace("Time: ", "").replace("ms", "");
                writer.printf("\"%s\",%s,%s,%s%n", expName, p0, p1, time);
            }
            System.out.println("✅ Summary exported to nim_experiment_summary.csv");
        } catch (Exception e) {
            System.err.println("CSV export failed: " + e.getMessage());
        }
    }

    private static NimMove chooseHeuristicMove(NimState s) {
        int xor = s.getHeaps().stream().reduce(0, (a, b) -> a ^ b);
        if (xor != 0) {
            for (int i = 0; i < s.getHeaps().size(); i++) {
                int h = s.getHeaps().get(i);
                int want = h ^ xor;
                if (want < h) {
                    return new NimMove(s.player(), i, h - want);
                }
            }
        }
        // fallback to random if already zero-sum
        for (Move<NimGame> m : s.moves(s.player()))
           return (NimMove)m;
          throw new IllegalStateException("No moves at zero‑sum!");
    }

    public static String runMatchups(int games,
                                     int p0Iterations,
                                     int p1Iterations,
                                     List<Integer> heaps,
                                     String label) {
        System.out.println("\n=== Running: " + label + " ===\n");

        int p0Wins = 0, p1Wins = 0;
        long totalTime = 0;

        for (int i = 0; i < games; i++) {
            State<NimGame> state = new NimGame(heaps).start();
            long t0 = System.nanoTime();
            while (!state.isTerminal()) {
                state = mctsStep(state, p0Iterations, p1Iterations);
            }
            totalTime += System.nanoTime() - t0;

            Optional<Integer> winner = state.winner();
            if (winner.isPresent()) {
                if (winner.get() == 0) p0Wins++;
                else p1Wins++;
            }
        }

        double p0Pct = 100.0 * p0Wins / games;
        double p1Pct = 100.0 * p1Wins / games;
        double avgMs = totalTime / games / 1_000_000.0;

        System.out.printf("Results:%n- Player 0 (MCTS %d): %d wins (%.1f%%)%n",
                p0Iterations, p0Wins, p0Pct);
        System.out.printf("- Player 1 (MCTS %d): %d wins (%.1f%%)%n",
                p1Iterations, p1Wins, p1Pct);
        System.out.printf("- Avg decision+playout time: %.2f ms/game%n", avgMs);
        System.out.println("----------------------------------------");

        return String.format("%-40s | P0: %5.1f%% | P1: %5.1f%% | Time: %6.2f ms", label, p0Pct, p1Pct, avgMs);
    }

    private static State<NimGame> mctsStep(State<NimGame> state, int p0Iterations, int p1Iterations) {
        int current = state.player();
        if (current == 0 && p0Iterations > 0)
            return mctsDecision(state, p0Iterations);
        else if (current == 1 && p1Iterations > 0)
            return mctsDecision(state, p1Iterations);
        else
            return state.next(state.chooseMove(current));
    }

    public static State<NimGame> mctsDecision(State<NimGame> state, int iterations) {
        NimNode root = new NimNode(state, null);
        int target = state.player();

        for (int i = 0; i < iterations; i++) {
            simulate(root, 0, 50, target);
        }

      /* System.out.println("=== ROOT CHILD STATS after " + iterations + " sims ===");
        for (Node<NimGame> c : root.children()) {
            NimNode n = (NimNode) c;
            double rate = n.playouts() > 0 ? (double) n.wins() / n.playouts() : 0.0;
            System.out.printf("move=%s, wins=%d, plays=%d, rate=%.2f%n",
                    n.state(), n.wins(), n.playouts(), rate);
        } */ 

        List<NimNode> bestChildren = root.children().stream()
                .map(n -> (NimNode) n)
                .filter(c -> c.playouts() > 0)
                .toList();

        double bestRate = bestChildren.stream()
                .mapToDouble(c -> (double) c.wins() / c.playouts())
                .max().orElse(-1);

        List<NimNode> topChoices = new ArrayList<>(
            bestChildren.stream()
                .filter(c -> (double) c.wins() / c.playouts() == bestRate)
                .toList()
        );
        Collections.shuffle(topChoices);
        return topChoices.isEmpty() ? state : topChoices.get(0).state();
    }

    public static int simulate(NimNode node, int depth, int maxDepth, int targetPlayer) {
        if (depth >= maxDepth || node.state().isTerminal()) {
            return node.state().winner().map(w -> w == targetPlayer ? 1 : 0).orElse(0);
        }

        if (node.children().isEmpty()) {
            node.explore();
            List<NimNode> kids = node.children().stream().map(n -> (NimNode) n).toList();
            NimNode child = kids.get(node.state().random().nextInt(kids.size()));

            State<NimGame> rollout = child.state();
            while (!rollout.isTerminal()) {
            NimState rs = (NimState) rollout;
            NimMove move = chooseHeuristicMove(rs);
            rollout = rollout.next(move);
          }

            int reward = rollout.winner().map(w -> w == targetPlayer ? 1 : 0).orElse(0);
            child.addPlayout(reward);
            return reward;
        }

        NimNode next = selectUCT(node, Math.sqrt(2));
        int reward = simulate(next, depth + 1, maxDepth, targetPlayer);
        node.addPlayout(reward);
        return reward;
    }

    private static NimNode selectUCT(NimNode parent, double C) {
        double logN = Math.log(Math.max(1, parent.playouts()));
        return parent.children().stream()
                .map(n -> (NimNode) n)
                .max(Comparator.comparingDouble(child -> {
                    double wi = child.wins();
                    double ni = child.playouts();
                    double exploitation = wi / ni;
                    double exploration = C * Math.sqrt(logN / ni);
                    return exploitation + exploration;
                }))
                .orElseThrow(() -> new IllegalStateException("No children in UCT selection"));
    }
}
