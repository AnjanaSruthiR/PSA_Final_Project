/*
 * Copyright (c) 2024. Robin Hillyard
 */

package com.phasmidsoftware.dsaipg.sort.par;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

/**
 * This code has been fleshed out by Ziyao Qiao. Thanks very much.
 * CONSIDER tidy it up a bit.
 */
public class Main {

    public static void main(String[] args) {
        processArgs(args);
        System.out.println("Degree of parallelism: " + ForkJoinPool.getCommonPoolParallelism());
        Random random = new Random();
        int[] sizes = {500000, 1000000, 2000000, 5000000};
        ArrayList<String> csvRows = new ArrayList<>();
        csvRows.add("ArraySize,Cutoff,AvgTime(ms)");        
        for (int size : sizes) {
            int[] array = new int[size];

            for (int j = 0; j < 50; j++) {
                ParSort.cutoff = 1000 * (j + 1);

                long startTime = System.currentTimeMillis();
                for (int t = 0; t < 10; t++) {
                    for (int i = 0; i < array.length; i++) {
                        array[i] = random.nextInt(10000000);
                    }
                    ParSort.sort(array, 0, array.length);
                }
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                double avgTime = totalTime / 10.0;
                System.out.println("ArraySize: " + size + ", cutoff: " + ParSort.cutoff + "\tAvgTime: " + avgTime + " ms");

                csvRows.add(size + "," + ParSort.cutoff + "," + avgTime);
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./src/result.csv")))) {
            for (String row : csvRows) {
                bw.write(row);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processArgs(String[] args) {
        String[] xs = args;
        while (xs.length > 0)
            if (xs[0].startsWith("-")) xs = processArg(xs);
    }

    private static String[] processArg(String[] xs) {
        String[] result = new String[0];
        System.arraycopy(xs, 2, result, 0, xs.length - 2);
        processCommand(xs[0], xs[1]);
        return result;
    }

    private static void processCommand(String x, String y) {
        if (x.equalsIgnoreCase("N")) setConfig(x, Integer.parseInt(y));
        else if (x.equalsIgnoreCase("P"))
            ForkJoinPool.getCommonPoolParallelism();
    }

    private static void setConfig(String x, int i) {
        configuration.put(x, i);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Map<String, Integer> configuration = new HashMap<>();


}