package com.aven.chessbot.bot;

import com.aven.chessbot.util.ChessUtil;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sorts the list based on (the value of the piece that is being captured) - (the value of the
 * piece that is doing the capturing)
 */
class CaptureValueMoveComparator implements Comparator<Move> {
    private static final int PV_MOVE_SCORE = 900_000;
    private static final int PROMOTION_SCORE = 800_000;
    private static final int CAPTURE_SCORE = 700_000;
    private static final int KILLER_MOVE_1_SCORE = 500_000;
    private static final int KILLER_MOVE_2_SCORE = 490_000;
    private static final int HISTORY_SCORE_BASE = 100_000;

    private final Board compareBoard;
    private final Map<Move, Integer> moveScores;

    private CaptureValueMoveComparator(Board compareBoard, Map<Move, Integer> moveScores) {
        this.compareBoard = compareBoard;
        this.moveScores = moveScores;
    }

    static List<Move> orderMoves(
            Board board,
            List<Move> moveList,
            int depth,
            Move ttMove,
            Move pvMove,
            Move[][] killerMoves,
            int[][][] historyHeuristic) {
        List<Move> orderedMoves = new ArrayList<>(moveList.size());
        List<Move> tacticalMoves = new ArrayList<>();
        List<Move> killerQuietMoves = new ArrayList<>();
        List<Move> historyMoves = new ArrayList<>();
        List<Move> losingCaptures = new ArrayList<>();
        Map<Move, Integer> moveScores = new HashMap<>(Math.max(16, moveList.size() * 2));
        Move prioritizedTtMove = null;

        for (Move move : moveList) {
            if (ttMove != null && ttMove.equals(move)) {
                prioritizedTtMove = move;
                continue;
            }

            int score = scoreMove(board, move, depth, pvMove, killerMoves, historyHeuristic);
            moveScores.put(move, score);

            if (isPromotion(move)) {
                tacticalMoves.add(move);
                continue;
            }

            if (isCapture(board, move)) {
                if (captureDelta(board, move) >= 0) {
                    tacticalMoves.add(move);
                } else {
                    losingCaptures.add(move);
                }
                continue;
            }

            if (isKillerMove(depth, move, killerMoves)) {
                killerQuietMoves.add(move);
            } else {
                historyMoves.add(move);
            }
        }

        CaptureValueMoveComparator comparator = new CaptureValueMoveComparator(board, moveScores);
        tacticalMoves.sort(comparator);
        killerQuietMoves.sort(comparator);
        historyMoves.sort(comparator);
        losingCaptures.sort(comparator);

        if (prioritizedTtMove != null) {
            orderedMoves.add(prioritizedTtMove);
        }
        orderedMoves.addAll(tacticalMoves);
        orderedMoves.addAll(killerQuietMoves);
        orderedMoves.addAll(historyMoves);
        orderedMoves.addAll(losingCaptures);
        return orderedMoves;
    }

    @Override
    public int compare(Move move, Move t1) {
        int moveScore = moveScores.getOrDefault(move, 0);
        int otherScore = moveScores.getOrDefault(t1, 0);
        if (moveScore != otherScore) {
            return Integer.compare(otherScore, moveScore);
        }

        int capturePieceValueT1 = ChessUtil.getPieceValue(compareBoard.getPiece(t1.getTo()));
        int capturePieceValueMove = ChessUtil.getPieceValue(compareBoard.getPiece(move.getTo()));
        if (capturePieceValueT1 != capturePieceValueMove) {
            return capturePieceValueT1 - capturePieceValueMove;
        }

        int attackerPieceValueMove = ChessUtil.getPieceValue(compareBoard.getPiece(move.getFrom()));
        int attackerPieceValueT1 = ChessUtil.getPieceValue(compareBoard.getPiece(t1.getFrom()));
        return attackerPieceValueMove - attackerPieceValueT1;
    }

    private static int scoreMove(
            Board board,
            Move move,
            int depth,
            Move pvMove,
            Move[][] killerMoves,
            int[][][] historyHeuristic) {
        if (depth == 0 && pvMove != null && pvMove.equals(move)) {
            return PV_MOVE_SCORE;
        }

        int score = 0;
        Piece movingPiece = board.getPiece(move.getFrom());
        Piece capturedPiece = board.getPiece(move.getTo());

        if (isPromotion(move)) {
            score += PROMOTION_SCORE + ChessUtil.getPieceValue(move.getPromotion());
        }

        if (capturedPiece != null && capturedPiece != Piece.NONE) {
            int captureDelta = ChessUtil.getPieceValue(capturedPiece) - ChessUtil.getPieceValue(movingPiece);
            score += CAPTURE_SCORE + captureDelta;
        } else if (isKillerMove(depth, move, killerMoves)) {
            score += getKillerMoveScore(depth, move, killerMoves);
        } else {
            score += HISTORY_SCORE_BASE
                    + historyHeuristic[board.getSideToMove().ordinal()][move.getFrom().ordinal()]
                            [move.getTo().ordinal()];
        }

        return score;
    }

    private static boolean isKillerMove(int depth, Move move, Move[][] killerMoves) {
        if (depth >= killerMoves.length) {
            return false;
        }
        return move.equals(killerMoves[depth][0]) || move.equals(killerMoves[depth][1]);
    }

    private static int getKillerMoveScore(int depth, Move move, Move[][] killerMoves) {
        if (depth >= killerMoves.length) {
            return 0;
        }
        if (move.equals(killerMoves[depth][0])) {
            return KILLER_MOVE_1_SCORE;
        }
        if (move.equals(killerMoves[depth][1])) {
            return KILLER_MOVE_2_SCORE;
        }
        return 0;
    }

    private static boolean isCapture(Board board, Move move) {
        Piece capturedPiece = board.getPiece(move.getTo());
        return capturedPiece != null && capturedPiece != Piece.NONE;
    }

    private static int captureDelta(Board board, Move move) {
        Piece movingPiece = board.getPiece(move.getFrom());
        Piece capturedPiece = board.getPiece(move.getTo());
        return ChessUtil.getPieceValue(capturedPiece) - ChessUtil.getPieceValue(movingPiece);
    }

    private static boolean isPromotion(Move move) {
        return move.getPromotion() != null && move.getPromotion() != Piece.NONE;
    }
}
