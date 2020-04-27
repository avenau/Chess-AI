package me.dollaking.bot;

import com.github.bhlangonijr.chesslib.*;

/**
 * Standard Strategy, just calculates how many pieces the board has and if the game has ended
 */

public class StandardStrategy implements Heuristic{
    //Side of the bot
    private Side side;
    public StandardStrategy(Side side){
        this.side = side;
    }

    /**
     * Calculate the score of current board
     * @param board The board that you want evaluated
     * @param depth The current depth of the minimax algorithm when this is called
     * @return The score of the board
     */
    @Override
    public int calculateScore(Board board, int depth) {
        int total = 0;
        //System.out.println("Current Side: " + this.side.value());
        if (board.getSideToMove().value().equalsIgnoreCase("black")){
            //System.out.println("Chosen: black\n");
            total = total + board.getPieceLocation(Piece.BLACK_BISHOP).size() * 3;
            total = total + board.getPieceLocation(Piece.BLACK_KNIGHT).size() * 3;
            total = total + board.getPieceLocation(Piece.BLACK_PAWN).size();
            total = total + board.getPieceLocation(Piece.BLACK_ROOK).size() * 6;
            total = total + board.getPieceLocation(Piece.BLACK_QUEEN).size() * 9;
            total = total - board.getPieceLocation(Piece.WHITE_BISHOP).size() * 3;
            total = total - board.getPieceLocation(Piece.WHITE_KNIGHT).size() * 3;
            total = total - board.getPieceLocation(Piece.WHITE_PAWN).size();
            total = total - board.getPieceLocation(Piece.WHITE_ROOK).size() * 6;
            total = total - board.getPieceLocation(Piece.WHITE_QUEEN).size() * 9;
            if (!side.value().equalsIgnoreCase("black")){
                total = total * -1;
            }

        } else {
            //System.out.println("Chosen: white\n");
            total = total - board.getPieceLocation(Piece.BLACK_BISHOP).size() * 3;
            total = total - board.getPieceLocation(Piece.BLACK_KNIGHT).size() * 3;
            total = total - board.getPieceLocation(Piece.BLACK_PAWN).size();
            total = total - board.getPieceLocation(Piece.BLACK_ROOK).size() * 6;
            total = total - board.getPieceLocation(Piece.BLACK_QUEEN).size() * 9;
            total = total + board.getPieceLocation(Piece.WHITE_BISHOP).size() * 3;
            total = total + board.getPieceLocation(Piece.WHITE_KNIGHT).size() * 3;
            total = total + board.getPieceLocation(Piece.WHITE_PAWN).size();
            total = total + board.getPieceLocation(Piece.WHITE_ROOK).size() * 6;
            total = total + board.getPieceLocation(Piece.WHITE_QUEEN).size() * 9;
            if (!side.value().equalsIgnoreCase("white")){
                total = total * -1;
            }
        }


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


}


