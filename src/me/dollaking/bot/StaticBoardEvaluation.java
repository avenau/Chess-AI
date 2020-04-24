package me.dollaking.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;

import java.util.ArrayList;
import java.util.List;

public class StaticBoardEvaluation implements Heuristic{

    private Side side;
    public StaticBoardEvaluation(Side side){
        this.side = side;
    }

    @Override
    public int calculateScore(Board board) {
        int total = 0;
        if (board.getSideToMove().value().equalsIgnoreCase("black")){
            total = total + board.getPieceLocation(Piece.BLACK_BISHOP).size() * 340;
            total = total + board.getPieceLocation(Piece.BLACK_KNIGHT).size() * 325;
            total = total + board.getPieceLocation(Piece.BLACK_PAWN).size() * 100;
            total = total + board.getPieceLocation(Piece.BLACK_ROOK).size() * 500;
            total = total + board.getPieceLocation(Piece.BLACK_QUEEN).size() * 900;
            total = total - board.getPieceLocation(Piece.WHITE_BISHOP).size() * 340;
            total = total - board.getPieceLocation(Piece.WHITE_KNIGHT).size() * 325;
            total = total - board.getPieceLocation(Piece.WHITE_PAWN).size() * 100;
            total = total - board.getPieceLocation(Piece.WHITE_ROOK).size() * 500;
            total = total - board.getPieceLocation(Piece.WHITE_QUEEN).size() * 900;
            if (!side.value().equalsIgnoreCase("black")){
                total = total * -1;
            }

        } else {
            total = total - board.getPieceLocation(Piece.BLACK_BISHOP).size() * 340;
            total = total - board.getPieceLocation(Piece.BLACK_KNIGHT).size() * 325;
            total = total - board.getPieceLocation(Piece.BLACK_PAWN).size() * 100;
            total = total - board.getPieceLocation(Piece.BLACK_ROOK).size() * 500;
            total = total - board.getPieceLocation(Piece.BLACK_QUEEN).size() * 900;
            total = total + board.getPieceLocation(Piece.WHITE_BISHOP).size() * 340;
            total = total + board.getPieceLocation(Piece.WHITE_KNIGHT).size() * 325;
            total = total + board.getPieceLocation(Piece.WHITE_PAWN).size() * 100;
            total = total + board.getPieceLocation(Piece.WHITE_ROOK).size() * 500;
            total = total + board.getPieceLocation(Piece.WHITE_QUEEN).size() * 900;
            if (!side.value().equalsIgnoreCase("white")){
                total = total * -1;
            }
        }

        total = total + analysePawn(board);
        total = total + analyseRook(board);

        if (board.isMated()){
            if (board.getSideToMove().value().equalsIgnoreCase(side.value())){
                total = -100000000;
            } else {
                total = 100000000;
            }
        } else if (board.isStaleMate() || board.isDraw()){
            if (board.getSideToMove().value().equalsIgnoreCase(side.value())){
                if (total < 0){
                    total = 10000000;
                } else if (total > 0){
                    total = -10000000;
                }
            } else {
                if (total < 0){
                    total = -10000000;
                } else if (total > 0) {
                    total = 10000000;
                }
            }
        }
        return total;
    }

    private int analysePawn(Board board){
        int total = 0;
        total = total + sameFile(board);


        return total;
    }

    private int analyseRook(Board board){
        int total = 0;
        total = total + powerRank(board);
        total = total + rookSameFile(board);

        return total;
    }

    private int rookSameFile(Board board){
        int total = 0;
        List<String> doubleFile = new ArrayList<String>();
        List<String> seenSquares = new ArrayList<String>();
        List<Square> blackRooks = board.getPieceLocation(Piece.BLACK_ROOK);
        List<Square> whiteRooks = board.getPieceLocation(Piece.WHITE_ROOK);

        for (Square index : blackRooks){
            if (seenSquares.contains(index.getFile().value())){
                if (!doubleFile.contains(index.getFile().value())){
                    doubleFile.add(index.getFile().value());
                    if (side.value().equalsIgnoreCase("black")){
                        total = total + 15;
                    } else {
                        total = total - 15;
                    }

                }
            } else {
                seenSquares.add(index.getFile().value());
            }
        }

        for (Square index : whiteRooks){
            if (seenSquares.contains(index.getFile().value())){
                if (!doubleFile.contains(index.getFile().value())){
                    doubleFile.add(index.getFile().value());
                    if (side.value().equalsIgnoreCase("white")){
                        total = total + 15;
                    } else {
                        total = total - 15;
                    }

                }
            } else {
                seenSquares.add(index.getFile().value());
            }
        }

        return total;
    }

    private int powerRank(Board board) {
        int total = 0;
        List<Square> whiteRook = board.getPieceLocation(Piece.WHITE_ROOK);
        List<Square> blackRook = board.getPieceLocation(Piece.BLACK_ROOK);

        for (Square index : whiteRook){
            if (index.getRank().getNotation().equalsIgnoreCase("7")){
                if (side.value().equalsIgnoreCase("white")){
                    total = total + 20;
                } else {
                    total = total - 20;
                }
            }
        }

        for (Square index : blackRook){
            if (index.getRank().getNotation().equalsIgnoreCase("2")){
                if (side.value().equalsIgnoreCase("black")){
                    total = total + 20;
                } else {
                    total = total - 20;
                }
            }
        }
        return total;
    }

    private int sameFile(Board board){
        int total = 0;
        List<String> doubleFile = new ArrayList<String>();
        List<String> seenSquares = new ArrayList<String>();
        List<Square> blackPawns = board.getPieceLocation(Piece.BLACK_PAWN);
        List<Square> whitePawns = board.getPieceLocation(Piece.WHITE_PAWN);

        for (Square index : blackPawns){
            if (seenSquares.contains(index.getFile().value())){
                if (!doubleFile.contains(index.getFile().value())){
                    doubleFile.add(index.getFile().value());
                    if (side.value().equalsIgnoreCase("black")){
                        total = total - 7;
                    } else {
                        total = total + 7;
                    }

                }
            } else {
                seenSquares.add(index.getFile().value());
            }
        }

        for (Square index : whitePawns){
            if (seenSquares.contains(index.getFile().value())){
                if (!doubleFile.contains(index.getFile().value())){
                    doubleFile.add(index.getFile().value());
                    if (side.value().equalsIgnoreCase("white")){
                        total = total - 7;
                    } else {
                        total = total + 7;
                    }

                }
            } else {
                seenSquares.add(index.getFile().value());
            }
        }

        return total;
    }




}
