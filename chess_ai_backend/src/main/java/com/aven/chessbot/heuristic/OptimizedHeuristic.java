package com.aven.chessbot.heuristic;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.CastleRight;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

/**
 * Faster static evaluation that scores the board directly from the bot's perspective.
 *
 * <p>The evaluator combines:
 *
 * <p>- material balance
 *
 * <p>- piece-square placement
 *
 * <p>- pawn structure penalties
 *
 * <p>- rook activity bonuses
 *
 * <p>- castling and king safety
 *
 * <p>- limited mobility in the opening and middlegame
 */
public class OptimizedHeuristic implements Heuristic {

  private static final int EARLY_GAME_MOBILITY_LIMIT = 20;
  private static final boolean USE_MOBILITY = false;

  private static final int[] PAWN_TABLE = {
    0, 0, 0, 0, 0, 0, 0, 0,
    5, 10, 10, -20, -20, 10, 10, 5,
    5, -5, -10, 0, 0, -10, -5, 5,
    0, 0, 0, 20, 20, 0, 0, 0,
    5, 5, 10, 25, 25, 10, 5, 5,
    10, 10, 20, 30, 30, 20, 10, 10,
    50, 50, 50, 50, 50, 50, 50, 50,
    0, 0, 0, 0, 0, 0, 0, 0
  };

  private static final int[] KNIGHT_TABLE = {
    -50, -40, -30, -30, -30, -30, -40, -50,
    -40, -20, 0, 0, 0, 0, -20, -40,
    -30, 0, 10, 15, 15, 10, 0, -30,
    -30, 5, 15, 20, 20, 15, 5, -30,
    -30, 0, 15, 20, 20, 15, 0, -30,
    -30, 5, 10, 15, 15, 10, 5, -30,
    -40, -20, 0, 5, 5, 0, -20, -40,
    -50, -40, -30, -30, -30, -30, -40, -50
  };

  private static final int[] BISHOP_TABLE = {
    -20, -10, -10, -10, -10, -10, -10, -20,
    -10, 5, 0, 0, 0, 0, 5, -10,
    -10, 10, 10, 10, 10, 10, 10, -10,
    -10, 0, 10, 10, 10, 10, 0, -10,
    -10, 5, 5, 10, 10, 5, 5, -10,
    -10, 0, 5, 10, 10, 5, 0, -10,
    -10, 0, 0, 0, 0, 0, 0, -10,
    -20, -10, -10, -10, -10, -10, -10, -20
  };

  private static final int[] ROOK_TABLE = {
    0, 0, 0, 5, 5, 0, 0, 0,
    -5, 0, 0, 0, 0, 0, 0, -5,
    -5, 0, 0, 0, 0, 0, 0, -5,
    -5, 0, 0, 0, 0, 0, 0, -5,
    -5, 0, 0, 0, 0, 0, 0, -5,
    -5, 0, 0, 0, 0, 0, 0, -5,
    5, 10, 10, 10, 10, 10, 10, 5,
    0, 0, 0, 0, 0, 0, 0, 0
  };

  private static final int[] QUEEN_TABLE = {
    -20, -10, -10, -5, -5, -10, -10, -20,
    -10, 0, 0, 0, 0, 0, 0, -10,
    -10, 0, 5, 5, 5, 5, 0, -10,
    -5, 0, 5, 5, 5, 5, 0, -5,
    0, 0, 5, 5, 5, 5, 0, -5,
    -10, 5, 5, 5, 5, 5, 0, -10,
    -10, 0, 5, 0, 0, 0, 0, -10,
    -20, -10, -10, -5, -5, -10, -10, -20
  };

  private static final int[] KING_MIDDLEGAME_TABLE = {
    -30, -40, -40, -50, -50, -40, -40, -30,
    -30, -40, -40, -50, -50, -40, -40, -30,
    -30, -40, -40, -50, -50, -40, -40, -30,
    -30, -40, -40, -50, -50, -40, -40, -30,
    -20, -30, -30, -40, -40, -30, -30, -20,
    -10, -20, -20, -20, -20, -20, -20, -10,
    20, 20, 0, 0, 0, 0, 20, 20,
    20, 30, 10, 0, 0, 10, 30, 20
  };

  private static final int[] KING_ENDGAME_TABLE = {
    -50, -40, -30, -20, -20, -30, -40, -50,
    -30, -20, -10, 0, 0, -10, -20, -30,
    -30, -10, 20, 30, 30, 20, -10, -30,
    -30, -10, 30, 40, 40, 30, -10, -30,
    -30, -10, 30, 40, 40, 30, -10, -30,
    -30, -10, 20, 30, 30, 20, -10, -30,
    -30, -30, 0, 0, 0, 0, -30, -30,
    -50, -30, -30, -30, -30, -30, -30, -50
  };

  private final Side side;

  public OptimizedHeuristic(Side side) {
    this.side = side;
  }

  @Override
  public int calculateScore(Board board, int depth) throws MoveGeneratorException {
    int whiteMaterial = 0;
    int blackMaterial = 0;
    int whitePositional = 0;
    int blackPositional = 0;
    int[] whiteKingTable = isEndgame(board, Side.WHITE) ? KING_ENDGAME_TABLE : KING_MIDDLEGAME_TABLE;
    int[] blackKingTable = isEndgame(board, Side.BLACK) ? KING_ENDGAME_TABLE : KING_MIDDLEGAME_TABLE;

    int[] whitePawnFiles = new int[8];
    int[] blackPawnFiles = new int[8];
    int[] whiteRookFiles = new int[8];
    int[] blackRookFiles = new int[8];

    for (Square square : Square.values()) {
      if (square == Square.NONE) {
        continue;
      }

      Piece piece = board.getPiece(square);
      if (piece == Piece.NONE) {
        continue;
      }

      int fileIndex = square.getFile().ordinal();
      int tableIndex = toWhitePerspectiveIndex(square);
      int mirroredIndex = mirrorIndex(tableIndex);

      switch (piece) {
        case WHITE_PAWN:
          whiteMaterial += 100;
          whitePositional += PAWN_TABLE[tableIndex];
          whitePawnFiles[fileIndex]++;
          break;
        case BLACK_PAWN:
          blackMaterial += 100;
          blackPositional += PAWN_TABLE[mirroredIndex];
          blackPawnFiles[fileIndex]++;
          break;
        case WHITE_KNIGHT:
          whiteMaterial += 320;
          whitePositional += KNIGHT_TABLE[tableIndex];
          break;
        case BLACK_KNIGHT:
          blackMaterial += 320;
          blackPositional += KNIGHT_TABLE[mirroredIndex];
          break;
        case WHITE_BISHOP:
          whiteMaterial += 330;
          whitePositional += BISHOP_TABLE[tableIndex];
          break;
        case BLACK_BISHOP:
          blackMaterial += 330;
          blackPositional += BISHOP_TABLE[mirroredIndex];
          break;
        case WHITE_ROOK:
          whiteMaterial += 500;
          whitePositional += ROOK_TABLE[tableIndex];
          whiteRookFiles[fileIndex]++;
          break;
        case BLACK_ROOK:
          blackMaterial += 500;
          blackPositional += ROOK_TABLE[mirroredIndex];
          blackRookFiles[fileIndex]++;
          break;
        case WHITE_QUEEN:
          whiteMaterial += 900;
          whitePositional += QUEEN_TABLE[tableIndex];
          break;
        case BLACK_QUEEN:
          blackMaterial += 900;
          blackPositional += QUEEN_TABLE[mirroredIndex];
          break;
        case WHITE_KING:
          whitePositional += whiteKingTable[tableIndex];
          break;
        case BLACK_KING:
          blackPositional += blackKingTable[mirroredIndex];
          break;
        default:
          break;
      }
    }

    int whiteScore = whiteMaterial + whitePositional;
    int blackScore = blackMaterial + blackPositional;

    whiteScore += pawnStructureScore(whitePawnFiles);
    blackScore += pawnStructureScore(blackPawnFiles);
    whiteScore += rookActivityScore(whiteRookFiles, whitePawnFiles, blackPawnFiles);
    blackScore += rookActivityScore(blackRookFiles, blackPawnFiles, whitePawnFiles);
    whiteScore += castleScore(board, Side.WHITE);
    blackScore += castleScore(board, Side.BLACK);

    if (USE_MOBILITY && board.getMoveCounter() < EARLY_GAME_MOBILITY_LIMIT) {
      int mobilityScore = mobilityScore(board);
      if (side == Side.WHITE) {
        whiteScore += mobilityScore;
      } else {
        blackScore += mobilityScore;
      }
    }

    int perspectiveScore = whiteScore - blackScore;
    return side == Side.WHITE ? perspectiveScore : -perspectiveScore;
  }

  private int mobilityScore(Board board) throws MoveGeneratorException {
    int currentMoves = MoveGenerator.generateLegalMoves(board).size();
    int enemyMoves;

    if (board.getEnPassant() == Square.NONE) {
      board.doNullMove();
      try {
        enemyMoves = MoveGenerator.generateLegalMoves(board).size();
      } finally {
        board.undoMove();
      }
    } else {
      // Keep en passant mobility exact when a null move would clear the target square.
      String fen = board.getFen();
      String[] fenParts = fen.split(" ");
      fenParts[1] = board.getSideToMove() == Side.WHITE ? "b" : "w";

      Board enemyBoard = new Board();
      enemyBoard.loadFromFen(String.join(" ", fenParts));
      enemyMoves = MoveGenerator.generateLegalMoves(enemyBoard).size();
    }

    int mobilityDelta = currentMoves - enemyMoves;

    if (board.getSideToMove() != side) {
      mobilityDelta *= -1;
    }

    return mobilityDelta * 2;
  }

  private int pawnStructureScore(int[] pawnFiles) {
    int score = 0;

    for (int count : pawnFiles) {
      if (count > 1) {
        score -= (count - 1) * 8;
      }
    }

    for (int file = 0; file < pawnFiles.length; file++) {
      if (pawnFiles[file] == 0) {
        continue;
      }

      boolean leftSupport = file > 0 && pawnFiles[file - 1] > 0;
      boolean rightSupport = file < pawnFiles.length - 1 && pawnFiles[file + 1] > 0;
      if (!leftSupport && !rightSupport) {
        score -= 10;
      }
    }

    return score;
  }

  private int rookActivityScore(int[] rookFiles, int[] allyPawnFiles, int[] enemyPawnFiles) {
    int score = 0;

    for (int file = 0; file < rookFiles.length; file++) {
      int rookCount = rookFiles[file];
      if (rookCount == 0) {
        continue;
      }

      if (allyPawnFiles[file] == 0 && enemyPawnFiles[file] == 0) {
        score += rookCount * 12;
      } else if (allyPawnFiles[file] == 0) {
        score += rookCount * 5;
      }

      if (rookCount > 1) {
        score += 15;
      }
    }

    return score;
  }

  private int castleScore(Board board, Side sideToEvaluate) {
    if (isCastled(board, sideToEvaluate)) {
      return 90;
    }

    CastleRight castleRight = board.getCastleRight(sideToEvaluate);
    if (castleRight != CastleRight.NONE) {
      return 10;
    }

    return -15;
  }

  private boolean isCastled(Board board, Side sideToEvaluate) {
    Square kingSquare = board.getKingSquare(sideToEvaluate);

    if (sideToEvaluate == Side.WHITE) {
      return (kingSquare == Square.G1 && board.getPiece(Square.F1) == Piece.WHITE_ROOK)
          || (kingSquare == Square.C1 && board.getPiece(Square.D1) == Piece.WHITE_ROOK);
    }

    return (kingSquare == Square.G8 && board.getPiece(Square.F8) == Piece.BLACK_ROOK)
        || (kingSquare == Square.C8 && board.getPiece(Square.D8) == Piece.BLACK_ROOK);
  }

  private boolean isEndgame(Board board, Side kingSide) {
    Piece queen = kingSide == Side.WHITE ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
    Piece rook = kingSide == Side.WHITE ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
    Piece bishop = kingSide == Side.WHITE ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP;
    Piece knight = kingSide == Side.WHITE ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;

    int queenCount = Long.bitCount(board.getBitboard(queen));
    int rookCount = Long.bitCount(board.getBitboard(rook));
    int bishopCount = Long.bitCount(board.getBitboard(bishop));
    int knightCount = Long.bitCount(board.getBitboard(knight));

    int nonPawnMaterial = queenCount * 900 + rookCount * 500 + bishopCount * 330 + knightCount * 320;
    return nonPawnMaterial <= 1300;
  }

  private int toWhitePerspectiveIndex(Square square) {
    int rankFromWhiteSide = Character.getNumericValue(square.getRank().getNotation().charAt(0)) - 1;
    int fileFromWhiteSide = square.getFile().ordinal();
    int tableRank = 7 - rankFromWhiteSide;
    return (tableRank * 8) + fileFromWhiteSide;
  }

  private int mirrorIndex(int index) {
    int rank = index / 8;
    int file = index % 8;
    int mirroredRank = 7 - rank;
    return (mirroredRank * 8) + file;
  }
}
