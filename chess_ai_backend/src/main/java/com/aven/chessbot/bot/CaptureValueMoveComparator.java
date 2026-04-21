package com.aven.chessbot.bot;

import com.aven.chessbot.util.ChessUtil;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sorts the list based on (the value of the piece that is being captured) - (the value of the
 * piece that is doing the capturing)
 */
class CaptureValueMoveComparator implements Comparator<Move> {
    private static final int TT_MOVE_SCORE = 2_000_000;
    private static final int PV_MOVE_SCORE = 900_000;
    private static final int WINNING_PROMOTION_SCORE = 800_000;
    private static final int WINNING_CAPTURE_SCORE = 700_000;
    private static final int LOSING_PROMOTION_SCORE = 400_000;
    private static final int LOSING_CAPTURE_SCORE = 300_000;
    private static final int KILLER_MOVE_1_SCORE = 600_000;
    private static final int KILLER_MOVE_2_SCORE = 590_000;
    private static final int HISTORY_SCORE_BASE = 500_000;
    private static final int HISTORY_SCORE_CAP = 50_000;

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
        Map<Move, Integer> moveScores = new HashMap<>(Math.max(16, moveList.size() * 2));
        for (Move move : moveList) {
            moveScores.put(move, scoreMove(board, move, depth, ttMove, pvMove, killerMoves, historyHeuristic));
        }

        CaptureValueMoveComparator comparator = new CaptureValueMoveComparator(board, moveScores);
        List<Move> orderedMoves = moveList.stream().sorted(comparator).toList();
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

    static int scoreMove(
            Board board,
            Move move,
            int depth,
            Move ttMove,
            Move pvMove,
            Move[][] killerMoves,
            int[][][] historyHeuristic) {
        if (ttMove != null && ttMove.equals(move)) {
            return TT_MOVE_SCORE;
        }

        if (depth == 0 && pvMove != null && pvMove.equals(move)) {
            return PV_MOVE_SCORE;
        }

        int score;
        Piece movingPiece = board.getPiece(move.getFrom());
        Piece capturedPiece = getCapturedPiece(board, move);
        int captureDelta = ChessUtil.getPieceValue(capturedPiece) - ChessUtil.getPieceValue(movingPiece);
        boolean promotion = isPromotion(move);
        boolean capture = capturedPiece != Piece.NONE;

        if (promotion || capture) {
            int tacticalBase =
                    captureDelta >= 0
                            ? promotion ? WINNING_PROMOTION_SCORE : WINNING_CAPTURE_SCORE
                            : promotion ? LOSING_PROMOTION_SCORE : LOSING_CAPTURE_SCORE;
            score = tacticalBase;
            score += ChessUtil.getPieceValue(capturedPiece) * 16;
            score -= ChessUtil.getPieceValue(movingPiece);
            if (promotion) {
                score += ChessUtil.getPieceValue(move.getPromotion());
            }
            return score;
        }

        if (isKillerMove(depth, move, killerMoves)) {
            return getKillerMoveScore(depth, move, killerMoves);
        }

        return HISTORY_SCORE_BASE
                + Math.min(
                        HISTORY_SCORE_CAP,
                        historyHeuristic[board.getSideToMove().ordinal()][move.getFrom().ordinal()]
                                [move.getTo().ordinal()]);
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
        return getCapturedPiece(board, move) != Piece.NONE;
    }

    static Piece getCapturedPiece(Board board, Move move) {
        Piece capturedPiece = board.getPiece(move.getTo());
        if (capturedPiece != null && capturedPiece != Piece.NONE) {
            return capturedPiece;
        }

        Piece movingPiece = board.getPiece(move.getFrom());
        if (movingPiece == null
                || movingPiece == Piece.NONE
                || movingPiece.getPieceType() != PieceType.PAWN
                || move.getFrom().getFile() == move.getTo().getFile()
                || board.getEnPassant() == Square.NONE
                || move.getTo() != board.getEnPassant()) {
            return Piece.NONE;
        }

        return board.getPiece(board.getEnPassantTarget());
    }

    private static boolean isPromotion(Move move) {
        return move.getPromotion() != null && move.getPromotion() != Piece.NONE;
    }
}
