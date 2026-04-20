package com.aven.chessbot.util;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;

public final class ChessUtil {
    public static final int PAWN_VALUE = 100;
    public static final int KNIGHT_VALUE = 325;
    public static final int BISHOP_VALUE = 340;
    public static final int ROOK_VALUE = 500;
    public static final int QUEEN_VALUE = 900;
    public static final int KING_VALUE = 20_000;

    private ChessUtil() {}

    public static int getPieceValue(Piece piece) {
        if (piece == null || piece == Piece.NONE || piece.getPieceType() == null) {
            return 0;
        }
        return getPieceValue(piece.getPieceType());
    }

    public static int getPieceValue(PieceType pieceType) {
        if (pieceType == null) {
            return 0;
        }
        return switch (pieceType) {
            case PAWN -> PAWN_VALUE;
            case KNIGHT -> KNIGHT_VALUE;
            case BISHOP -> BISHOP_VALUE;
            case ROOK -> ROOK_VALUE;
            case QUEEN -> QUEEN_VALUE;
            case KING -> KING_VALUE;
            default -> 0;
        };
    }
}
