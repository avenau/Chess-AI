package com.aven.chessbot.heuristic;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

/** Interface for any board evaluations */
public interface Heuristic {
  // Calculate the score of the board
  int calculateScore(Board board, int depth) throws MoveGeneratorException;
}
