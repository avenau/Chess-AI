package com.aven.chessbot.components;

import com.github.bhlangonijr.chesslib.move.Move;

import java.util.Arrays;

public class TranspositionTable {
    private static final int TABLE_BITS = 18;
    private static final int TABLE_SIZE = 1 << TABLE_BITS;
    private static final int TABLE_MASK = TABLE_SIZE - 1;

    private final long[] keys = new long[TABLE_SIZE];
    private final int[] scores = new int[TABLE_SIZE];
    private final int[] depths = new int[TABLE_SIZE];
    private final byte[] flags = new byte[TABLE_SIZE];
    private final Move[] bestMoves = new Move[TABLE_SIZE];

    public TranspositionTable() {
        Arrays.fill(depths, -1);
    }

    public int findSlot(long key) {
        int slot = slotFor(key);
        return depths[slot] >= 0 && keys[slot] == key ? slot : -1;
    }

    public int getScore(int slot) {
        return scores[slot];
    }

    public int getDepth(int slot) {
        return depths[slot];
    }

    public byte getFlag(int slot) {
        return flags[slot];
    }

    public Move getBestMove(int slot) {
        return bestMoves[slot];
    }

    public void store(long key, int score, int depth, byte flag, Move bestMove) {
        int slot = slotFor(key);
        if (depths[slot] > depth && keys[slot] != key) {
            return;
        }

        keys[slot] = key;
        scores[slot] = score;
        depths[slot] = depth;
        flags[slot] = flag;
        bestMoves[slot] = bestMove;
    }

    private int slotFor(long key) {
        return Long.hashCode(key) & TABLE_MASK;
    }
}
