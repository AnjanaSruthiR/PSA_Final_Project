/*
 * Copyright (c) 2024. Robin Hillyard
 */

package com.phasmidsoftware.dsaipg.projects.mcts.core;

import java.util.Objects;
import java.util.Random;

public class RandomState {

    public RandomState() {
        this(1);
    } 
    public RandomState next() {
        return new RandomState(x, longValue());
    }

    public int intValue() {
        return random.nextInt(x);
    }

    public long longValue() {
        return random.nextLong();
    }

    public boolean booleanValue() {
        return random.nextBoolean();
    }

    public RandomState(int x, long seed) {
        this(x, new Random(seed));
    }

    public RandomState(int x) {
        this(x, System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RandomState that)) return false;
        return x == that.x && random.nextLong() == that.random.nextLong();
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, random);
    }

    @Override
    public String toString() {
        return "RandomState{" +
                "x=" + x +
                ", random=" + random +
                '}';
    }

    private RandomState(int x, Random random) {
        this.x = x;
        this.random = random;
    }

    private final int x;
    private final Random random;

    public Random random() {
        return random;
    }
    

}