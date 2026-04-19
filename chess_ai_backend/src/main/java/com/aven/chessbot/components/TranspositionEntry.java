package com.aven.chessbot.components;

import com.github.bhlangonijr.chesslib.move.Move;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TranspositionEntry {
    private int score;
    private int depth;
    private byte flag;  // EXACT = 0, LOWERBOUND = 1, UPPERBOUND = 2
    private Move bestMove;  // Optional: store the best move found at this position

    public static final byte EXACT = 0;
    public static final byte LOWERBOUND = 1;
    public static final byte UPPERBOUND = 2;
}
