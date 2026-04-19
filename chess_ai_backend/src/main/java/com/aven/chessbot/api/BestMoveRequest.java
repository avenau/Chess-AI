package com.aven.chessbot.api;

import java.util.List;

public class BestMoveRequest {
  private String fen;
  private List<String> moves;

  public String getFen() {
    return fen;
  }

  public void setFen(String fen) {
    this.fen = fen;
  }

  public List<String> getMoves() {
    return moves;
  }

  public void setMoves(List<String> moves) {
    this.moves = moves;
  }
}
