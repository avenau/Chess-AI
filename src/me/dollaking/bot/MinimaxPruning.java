import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;

public class MinimaxPruning implements ChessBot {
    public Side side;
    int nodeCount;
    Move bestNextMove;

    public MinimaxPruning (Side side){
        this.side = side;

    }
    @Override
    public Move calculateNextMove(Board board) throws MoveGeneratorException, InterruptedException {
        Move nextMove = bestMove(board);
       System.out.println("info " + board.getSideToMove().value());
        return nextMove;
    }

    private static int getPieceValue(Piece piece){
        if (piece.getPieceType() == PieceType.PAWN){
            return 1;
        } else if (piece.getPieceType() == PieceType.BISHOP){
            return 3;
        } else if (piece.getPieceType() == PieceType.KNIGHT){
            return 3;
        } else if (piece.getPieceType() == PieceType.ROOK){
            return 6;
        } else if (piece.getPieceType() == PieceType.QUEEN){
            return 9;
        }
        return 0;

    }



    //Attack score
    private static int attacks(Board board) throws MoveGeneratorException {
        int attackScore = 0;
        for(final Move move : MoveGenerator.generateLegalMoves(board)) {
            String stringMove = move.toString().substring(2,4).toUpperCase();
            String stringCurrent = move.toString().substring(0,2).toUpperCase();
            Square square = Square.valueOf(stringMove);
            Square prevSquare = Square.valueOf(stringCurrent);
            Piece taken = board.getPiece(square);
            Piece your = board.getPiece(prevSquare);
            if (!taken.value().equalsIgnoreCase(Piece.NONE.value())){
                if (taken.getPieceSide().value().equalsIgnoreCase(board.getSideToMove().value())){
                    attackScore++;
                } else if (getPieceValue(your) <= getPieceValue(taken)) {
                    attackScore++;
                }

            }

        }
        return attackScore * 2;
    }

    int heuristic(Board board) throws MoveGeneratorException {
        int total = 0;
        if (board.getSideToMove().value().equalsIgnoreCase("black")){
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
        }

       // total = total + attacks(board);
        return total;


    }

    Move bestMove(Board board) throws MoveGeneratorException, InterruptedException {
        Move move = null;
        nodeCount = 0;
        int max = Integer.MIN_VALUE;
        minimax(0, Integer.MIN_VALUE, Integer.MAX_VALUE, board.getSideToMove(), board);
       /* if (move != null){
            if (minimaxResult > max){
                move = temp;
                max = minimaxResult;
            }
        } else {
            move = temp;
            max = minimaxResult;
        }*/

        System.out.println("info Number of Nodes Visited: " + nodeCount);



        return bestNextMove;
    }



    int minimax(int depth, int alpha, int beta, Side side, Board board) throws MoveGeneratorException, InterruptedException {

        int totalValue = 0;

        totalValue = heuristic(board);


        if (depth == 5 || totalValue >= 10000 || totalValue <= -10000){

            return totalValue;
        }

        //System.out.println(board.toString());
        //Thread.sleep(1000);

        if (board.getSideToMove().value().equalsIgnoreCase(this.side.value())){
            Move other = null;
            for (Move temp : MoveGenerator.generateLegalMoves(board)){
                Board dummy = board.clone();
                dummy.doMove(temp);
                nodeCount++;
                int currentScore = minimax(depth + 1, alpha, beta, dummy.getSideToMove(), dummy);

                if (currentScore > alpha){
                    alpha = currentScore;
                    other = temp;
                }

                if (alpha == beta){
                    break;
                }
            }
            if (depth == 0){
                bestNextMove = other;
            }
            return alpha;

        } else {
            for (Move temp : MoveGenerator.generateLegalMoves(board)){
                Board dummy = board.clone();
                dummy.doMove(temp);
                nodeCount++;
                int currentScore = minimax(depth + 1, alpha, beta, dummy.getSideToMove(), dummy);
                if (currentScore < beta){
                    beta = currentScore;
                }

                if (alpha == beta){
                    break;
                }
            }
            return beta;
        }

    }

}
