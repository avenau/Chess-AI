package com.aven.chessbot.components;

import com.github.bhlangonijr.chesslib.move.Move;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MoveHistoryEntry {
  private int score;
  private Move move;
}
