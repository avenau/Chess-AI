package com.aven.chessbot.bot;

import com.aven.chessbot.components.TranspositionEntry;
import com.aven.chessbot.components.Zobrist;
import com.aven.chessbot.heuristic.Heuristic;
import com.aven.chessbot.heuristic.StaticBoardEvaluation;
import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;

import java.util.*;

/**
 * This bot is using minimax algorithm with alpha beta pruning Transposition are also used Move
 * orders are used but very basic; Moves are ordered based on (the value of the piece that is being
 * captured) - (the value of the piece that is doing the capturing)
 */
public class MinimaxPruning implements ChessBot {
  public Side side;
  private int nodeCount;
  private Move bestNextMove;
  private Heuristic heuristic;
  private ArrayList<Zobrist> zobList;
  private HashMap<String, TranspositionEntry> transpositionTable;
  private long startTime;
  private long maxDepth;
  private long timeLimit;
  private long endTime;
  private long maxValue;

  // private HashMap<Integer, MoveHistoryEntry> bestMoveHistory;

  /**
   * Constructor
   *
   * @param side This is the side that the bot is in
   */
  public MinimaxPruning(Side side) {
    // Side of the bot
    this.side = side;
    // Change your heuristic HERE
    this.heuristic = new StaticBoardEvaluation(side);

    this.zobList = new ArrayList<Zobrist>();
    this.transpositionTable = new HashMap<>();
    this.timeLimit = 20000;
    // this.bestMoveHistory = new HashMap<Integer, MoveHistoryEntry>();
  }

  /**
   * Getting the Zobrist class that contains all the keys for each piece within the square
   *
   * @param square The square that the Zobrist class belongs to
   * @return
   */
  private Zobrist getZobBySquare(Square square) {
    for (Zobrist zob : zobList) {
      if (zob.getSquare() == square) {
        return zob;
      }
    }
    return null;
  }

  /**
   * Calculates the best next move
   *
   * @param board The current board
   * @return The best next move
   * @throws MoveGeneratorException
   * @throws InterruptedException
   */
  @Override
  public Move calculateNextMove(Board board) throws MoveGeneratorException, InterruptedException {
    /* this.zobList = new ArrayList<Zobrist>();
    int counter = 0;
    for (Square indexSquare : Square.values()){
        if (indexSquare.value().equalsIgnoreCase(Square.NONE.value())){
            continue;
        }
        Zobrist zob = new Zobrist(indexSquare);
        counter = zob.generateRandom(counter);

    }*/

    Move nextMove = bestMove(board);

    return nextMove;
  }

  /**
   * @param board Current Board
   * @return Best next move
   * @throws MoveGeneratorException
   * @throws InterruptedException
   */
  Move bestMove(Board board) throws MoveGeneratorException, InterruptedException {
    Move move = null;
    nodeCount = 0;
    int depth = 6;
    this.startTime = System.currentTimeMillis();
    this.endTime = startTime + timeLimit;
    if (board.getMoveCounter() < 10) {
      depth = 5;
    }
    System.out.println("Start: " + this.startTime + " End: " + this.endTime);
    while (this.startTime <= this.endTime){
        this.transpositionTable = new HashMap<>();
        System.out.println("info: Searching depth " + depth);
        minimax(0, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, board.getSideToMove(), board);
        depth++;
        this.startTime = System.currentTimeMillis();
    }

      System.out.println("info: Value " + this.maxValue);
      System.out.println("info: Move " + this.bestNextMove.toString());
    System.out.println("info Depth Searched: " + maxDepth);
    System.out.println("info Number of Nodes Visited: " + nodeCount);
    return bestNextMove;
  }

  /**
   * Get the value of the Pieces
   *
   * @param piece The piece that you want the value of
   * @return The value of the piece
   */
  private static int getPieceValue(Piece piece) {
    if (piece.getPieceType() == PieceType.PAWN) {
      return 1;
    } else if (piece.getPieceType() == PieceType.BISHOP) {
      return 3;
    } else if (piece.getPieceType() == PieceType.KNIGHT) {
      return 3;
    } else if (piece.getPieceType() == PieceType.ROOK) {
      return 6;
    } else if (piece.getPieceType() == PieceType.QUEEN) {
      return 9;
    } else if (piece.getPieceType() == PieceType.KING) {
      return 20;
    }
    return 0;
  }

  /**
   * Sorts the list based on (the value of the piece that is being captured) - (the value of the
   * piece that is doing the capturing)
   */
  class sortByCaptureValue implements Comparator<Move> {
    private Board compareBoard;

    public sortByCaptureValue(Board compareBoard) {
      this.compareBoard = compareBoard;
    }

    @Override
    public int compare(Move move, Move t1) {
      int capturePieceValueT1 = getPieceValue(compareBoard.getPiece(t1.getTo()));
      int capturePieceValueMove = getPieceValue(compareBoard.getPiece(move.getTo()));
      if (!(capturePieceValueT1 - capturePieceValueMove == 0)) {
        return capturePieceValueT1 - capturePieceValueMove;
      }

      return getPieceValue(compareBoard.getPiece(move.getTo()))
          - getPieceValue(compareBoard.getPiece(t1.getTo()));
    }
  }

  /**
   * The minimax algorthim
   *
   * @param depth The depth that the algorithm is currently running
   * @param boundDepth The max depth that the algorithm will go to
   * @param alpha The maximum board score (First call of minimax alpha should be Integer.MIN)
   * @param beta The minimum board score (First call of minimax alpha should be Integer.MAX)
   * @param side
   * @param board The board you want to evaluate and get the score of
   * @return The highest board evaluated score
   * @throws MoveGeneratorException
   * @throws InterruptedException
   */
  int minimax(int depth, int boundDepth, int alpha, int beta, Side side, Board board)
      throws MoveGeneratorException, InterruptedException {

//   if (System.currentTimeMillis() >= endTime) {
//        throw new InterruptedException("Time limit exceeded");
//return Math.toIntExact(this.maxValue);
//    }

    String positionKey = board.getFen();

    TranspositionEntry entry = transpositionTable.get(positionKey);
    if (entry != null && entry.getDepth() >= boundDepth - depth) {
        if (entry.getFlag() == TranspositionEntry.EXACT) {
            return entry.getScore();
        }
        if (entry.getFlag() == TranspositionEntry.LOWERBOUND && entry.getScore() > alpha) {
            alpha = entry.getScore();
        }
        if (entry.getFlag() == TranspositionEntry.UPPERBOUND && entry.getScore() < beta) {
            beta = entry.getScore();
        }
        if (alpha >= beta) {
            return entry.getScore();
        }
    }

    if (depth == boundDepth || board.isDraw() || board.isMated() || board.isStaleMate()) {
      if (depth > maxDepth) {
        maxDepth = depth;
      }
      int totalValue = 0;
      // System.out.println("On " + board.getSideToMove().value());
      totalValue = heuristic.calculateScore(board, depth);
      return totalValue;
    }

    List<Move> moveList = MoveGenerator.generateLegalMoves(board);
    Collections.sort(moveList, new sortByCaptureValue(board));

    if (entry != null && entry.getBestMove() != null) {
        moveList.remove(entry.getBestMove());
        moveList.add(0, entry.getBestMove());
    }

    if (board.getSideToMove().value().equalsIgnoreCase(this.side.value())) {
        Move bestMove = null;
        int originalAlpha = alpha;

        for (Move temp : moveList) {
            board.doMove(temp);
            int currentScore = minimax(depth + 1, boundDepth, alpha, beta, board.getSideToMove(), board);
            board.undoMove();

            if (currentScore > alpha) {
                alpha = currentScore;
                bestMove = temp;
            }
            if (alpha >= beta) {
                transpositionTable.put(positionKey,
                    new TranspositionEntry(alpha, boundDepth - depth, TranspositionEntry.LOWERBOUND, bestMove));
                break;
            }
        }

        byte flag = alpha <= originalAlpha ? TranspositionEntry.UPPERBOUND :
                    alpha >= beta ? TranspositionEntry.LOWERBOUND :
                    TranspositionEntry.EXACT;
        transpositionTable.put(positionKey,
            new TranspositionEntry(alpha, boundDepth - depth, flag, bestMove));

        if (depth == 0) {
            bestNextMove = bestMove;
            this.maxValue = alpha;
        }
        return alpha;
    } else {
        for (Move temp : moveList) {
            board.doMove(temp);
            int currentScore;

            if (entry != null && entry.getBestMove() != null && entry.getBestMove().equals(temp)) {
                currentScore = minimax(depth + 1, boundDepth, alpha, beta, board.getSideToMove(), board);
            } else {
                nodeCount++;
                currentScore = minimax(depth + 1, boundDepth, alpha, beta, board.getSideToMove(), board);
            }

            board.undoMove();
            if (currentScore < beta) {
                beta = currentScore;
            }

            if (alpha >= beta) {
                break;
            }
        }
        return beta;
    }
  }
}
