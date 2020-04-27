package me.dollaking.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

/**
 * Interface for any Bots
 */
public interface ChessBot {
    //Calculate the best move to make
    Move calculateNextMove(Board board) throws MoveGeneratorException, InterruptedException;
}
