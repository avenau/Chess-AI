package me.dollaking.bot;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;

public class Zobrist {
    private Square square;
    private int blackPawn;
    private int blackBishop;
    private int blackKnight;
    private int blackQueen;
    private int blackRook;
    private int whitePawn;
    private int whiteBishop;
    private int whiteKnight;
    private int whiteQueen;
    private int whiteRook;
    private int blackKing;
    private int whiteKing;



    public Zobrist(Square square){
        this.square = square;
        //generateRandom(start);

    }

    public int generateRandom(int start){
        blackPawn = start;
        blackBishop = blackPawn + 1;
        blackKnight = blackBishop + 1;
        blackQueen = blackKnight + 1;
        blackRook = blackQueen + 1;
        whitePawn = blackRook + 1;
        whiteBishop = whitePawn + 1;
        whiteKnight = whiteBishop + 1;
        whiteQueen = whiteKnight + 1;
        whiteRook = whiteQueen + 1;
        blackKing = whiteRook + 1;
        whiteKing = blackKing + 1;
        return whiteKing++;
    }

    public Square getSquare() {
        return this.square;
    }

    public int getValue(Piece piece){
        if (piece == Piece.BLACK_PAWN){
            return blackPawn;
        } else if (piece == Piece.WHITE_PAWN){
            return whitePawn;
        } else if (piece == Piece.BLACK_BISHOP){
            return blackBishop;
        } else if (piece == Piece.WHITE_BISHOP){
            return whiteBishop;
        } else if (piece == Piece.BLACK_KNIGHT){
            return blackKnight;
        } else if (piece == Piece.WHITE_KNIGHT){
            return whiteKnight;
        } else if (piece == Piece.BLACK_ROOK){
            return blackRook;
        } else if (piece == Piece.WHITE_ROOK){
            return whiteRook;
        } else if (piece == Piece.BLACK_QUEEN){
            return blackQueen;
        } else if (piece == Piece.WHITE_QUEEN){
            return whiteQueen;
        } else if (piece == Piece.BLACK_KING){
            return blackKing;
        } else if (piece == Piece.WHITE_KING){
            return whiteKing;
        }
        return -1;
    }




}
