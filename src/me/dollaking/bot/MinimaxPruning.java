import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;

public class MinimaxPruning implements ChessBot {
    Side side;
    int min;
    int max;

    public MinimaxPruning (Side side){
        this.side = side;
        this.min = Integer.MIN_VALUE;
        this.max = Integer.MAX_VALUE;

    }
    @Override
    public Move calculateNextMove(Board board) throws MoveGeneratorException {
        Move nextMove = bestMove(board);
        return nextMove;
    }




    int heuristic(Board board){
        int total = 0;
        if (side.value().equalsIgnoreCase("black")){
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

        } else {
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
        }

        if (board.isMated()){
            if (board.getSideToMove().toString().equalsIgnoreCase(side.toString())){
                total = total - 10000;
            } else {
                total = total + 10000;
            }
        }
        return total;


    }

    Move bestMove(Board board) throws MoveGeneratorException {
        int i = 0;
        Move move = null;
        int max = 0;
        MoveList moveList = MoveGenerator.generateLegalMoves(board);
        for (Move temp : moveList){
            Board tempBoard;
            int minimaxResult;
            tempBoard = board.clone();
            tempBoard.doMove(temp);
            minimaxResult = minimax(0, min, max, tempBoard.getSideToMove(), tempBoard);

            if (move != null){
                if (minimaxResult > max){
                    move = temp;
                    max = minimaxResult;
                }
            } else {
                move = temp;
                max = minimaxResult;
            }
        }
        return move;
    }



    int minimax(int depth, int alpha, int beta, Side side, Board board) throws MoveGeneratorException {

        int totalValue = 0;

        totalValue = heuristic(board);


        if (depth == 5 || totalValue >= 10000 || totalValue <= -10000){
            return totalValue;
        }

        if (side.toString().equalsIgnoreCase(this.side.toString())){
            for (Move temp : MoveGenerator.generateLegalMoves(board)){
                Board dummy = board.clone();
                int currentScore = minimax(depth + 1, alpha, beta, side.flip(), dummy);
                if (currentScore > alpha){
                    alpha = currentScore;
                }

                if (alpha >= beta){
                    break;
                }
            }
            return alpha;

        } else {
            for (Move temp : MoveGenerator.generateLegalMoves(board)){
                Board dummy = board.clone();
                int currentScore = minimax(depth + 1, alpha, beta, side.flip(), dummy);
                if (currentScore < beta){
                    beta = currentScore;
                }

                if (alpha >= beta){
                    break;
                }
            }
            return beta;
        }
    }

}
