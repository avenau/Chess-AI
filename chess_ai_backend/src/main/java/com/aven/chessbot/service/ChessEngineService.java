package com.aven.chessbot.service;

import com.aven.chessbot.api.BestMoveRequest;
import com.aven.chessbot.api.BestMoveResponse;
import com.aven.chessbot.bot.ChessBot;
import com.aven.chessbot.bot.MinimaxPruning;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChessEngineService {
  public BestMoveResponse findBestMove(BestMoveRequest request) throws InterruptedException {
    Board board = buildBoard(request);
    ChessBot bot = new MinimaxPruning(board.getSideToMove());

    try {
      Move bestMove = bot.calculateNextMove(board);
      String moveText = bestMove == null ? "0000" : bestMove.toString();
      return new BestMoveResponse(moveText, board.getFen(), board.getSideToMove().name());
    } catch (MoveGeneratorException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Unable to generate legal moves for the supplied position.", e);
    }
  }

  private Board buildBoard(BestMoveRequest request) {
    Board board = new Board();
    String fen = request.getFen();
    if (fen != null && !fen.isBlank()) {
      try {
        board.loadFromFen(fen);
      } catch (RuntimeException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid FEN supplied.", e);
      }
    }

    for (String moveText : safeMoves(request.getMoves())) {
      try {
        Move move = parseMove(moveText, board);
        if (!board.isMoveLegal(move, true)) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "Illegal move in move list: " + moveText);
        }
        board.doMove(move);
      } catch (IllegalArgumentException e) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Invalid move format: " + moveText, e);
      }
    }

    return board;
  }

  private List<String> safeMoves(List<String> moves) {
    return moves == null ? Collections.emptyList() : moves;
  }

  private Move parseMove(String input, Board board) {
    if (input == null || (input.length() != 4 && input.length() != 5)) {
      throw new IllegalArgumentException("Move must be in long algebraic format.");
    }

    Square from = Square.valueOf(input.substring(0, 2).toUpperCase());
    Square to = Square.valueOf(input.substring(2, 4).toUpperCase());
    if (input.length() == 4) {
      return new Move(from, to);
    }

    return new Move(from, to, promotionPiece(input.charAt(4), board.getSideToMove()));
  }

  private Piece promotionPiece(char promotion, Side side) {
    switch (Character.toLowerCase(promotion)) {
      case 'q':
        return side == Side.WHITE ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
      case 'r':
        return side == Side.WHITE ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
      case 'b':
        return side == Side.WHITE ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP;
      case 'n':
        return side == Side.WHITE ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
      default:
        throw new IllegalArgumentException("Unsupported promotion piece.");
    }
  }
}
