package me.dollaking.bot;

import com.github.bhlangonijr.chesslib.Board;

public class TranpositionEntry {
    private Board board;
    private int score;
    public TranpositionEntry(Board board, int score){
        this.board = board;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public Board getBoard() {
        return board;
    }
}
