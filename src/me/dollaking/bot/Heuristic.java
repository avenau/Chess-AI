package me.dollaking.bot;

import com.github.bhlangonijr.chesslib.Board;

public interface Heuristic {

    int calculateScore(Board board);
}
