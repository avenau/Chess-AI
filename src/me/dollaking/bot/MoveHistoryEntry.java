package me.dollaking.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

public class MoveHistoryEntry {
    private int score;
    private Move move;

    public MoveHistoryEntry (Move move, int Score) {
        this.move = move;
        this.score = score;
    }

    public int getScore() {
        return this.score;
    }

    public Move getMove(){
        return this.move;
    }
}
