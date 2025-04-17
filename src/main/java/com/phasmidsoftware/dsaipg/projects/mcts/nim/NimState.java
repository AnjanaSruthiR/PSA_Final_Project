package com.phasmidsoftware.dsaipg.projects.mcts.nim;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.RandomState;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

public class NimState implements State<NimGame> {
    private final NimGame game;
    private final List<Integer> heaps;
    private final int player;
    private final Optional<Integer> winner;
    private final RandomState random;

    public NimState(NimGame game, List<Integer> heaps, int player, Optional<Integer> winner, RandomState random) {
        this.game = game;
        this.heaps = heaps;
        this.player = player;
        this.winner = winner;
        this.random = random;
    }

    @Override
    public NimGame game() {
        return game;
    }

    @Override
    public boolean isTerminal() {
        return heaps.stream().allMatch(h -> h == 0);
    }

    @Override
    public int player() {
        return player;
    }

    @Override
    public Optional<Integer> winner() {
        return winner;
    }

    @Override
    public Random random() {
        return random.random(); // returns java.util.Random
    }

    @Override
    public Collection<Move<NimGame>> moves(int player) {
        List<Move<NimGame>> legalMoves = new ArrayList<>();
        for (int i = 0; i < heaps.size(); i++) {
            int heapSize = heaps.get(i);
            for (int count = 1; count <= heapSize; count++) {
                legalMoves.add(new NimMove(player, i, count));
            }
        }
        //System.out.println("Legal moves for player " + player + ": " + legalMoves.size());

        return legalMoves;
    }

    @Override
public State<NimGame> next(Move<NimGame> move) {
    NimMove m = (NimMove) move;

    int heapIndex = m.getHeapIndex();
    int toRemove = m.getObjectsToRemove();

    if (heapIndex < 0 || heapIndex >= heaps.size())
        throw new IllegalArgumentException("Invalid heap index");

    if (toRemove < 1 || toRemove > heaps.get(heapIndex))
        throw new IllegalArgumentException("Trying to remove too many objects from heap");

    List<Integer> newHeaps = new ArrayList<>(heaps);
    newHeaps.set(heapIndex, newHeaps.get(heapIndex) - toRemove);

    boolean terminal = newHeaps.stream().allMatch(h -> h == 0);
    Optional<Integer> newWinner = terminal ? Optional.of(player) : Optional.empty();

    return new NimState(game, newHeaps, 1 - player, newWinner, random.next());
}

public List<Integer> getHeaps() {
    return heaps;
}
    @Override
    public String toString() {
        return "Heaps: " + heaps + " | Player: " + player + " | Winner: " + winner.orElse(null);
    }


}

