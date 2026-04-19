package com.aven.chessbot.api;

public class BestMoveResponse {
  private final String bestMove;
  private final String fen;
  private final String sideToMove;

  public BestMoveResponse(String bestMove, String fen, String sideToMove) {
    this.bestMove = bestMove;
    this.fen = fen;
    this.sideToMove = sideToMove;
  }

  public String getBestMove() {
    return bestMove;
  }

  public String getFen() {
    return fen;
  }

  public String getSideToMove() {
    return sideToMove;
  }
}
