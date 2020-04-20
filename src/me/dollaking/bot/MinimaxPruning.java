package me.dollaking.bot;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;

public class MinimaxPruning implements ChessBot {
    public Side side;
    private int nodeCount;
    private Move bestNextMove;
    private StandardStrategy heuristic;

    public MinimaxPruning (Side side){
        this.side = side;
        this.heuristic = new StandardStrategy(side);

    }
    @Override
    public Move calculateNextMove(Board board) throws MoveGeneratorException, InterruptedException {
        Move nextMove = bestMove(board);
       System.out.println("info " + board.getSideToMove().value());
        return nextMove;
    }



    Move bestMove(Board board) throws MoveGeneratorException, InterruptedException {
        Move move = null;
        nodeCount = 0;
        int max = Integer.MIN_VALUE;
        minimax(0, Integer.MIN_VALUE, Integer.MAX_VALUE, board.getSideToMove(), board);
        System.out.println("info Number of Nodes Visited: " + nodeCount);

        return bestNextMove;
    }



    int minimax(int depth, int alpha, int beta, Side side, Board board) throws MoveGeneratorException, InterruptedException {

        int totalValue = 0;

        totalValue = heuristic.calculateScore(board);


        if (depth == 5 || totalValue >= 10000 || totalValue <= -10000){

            return totalValue;
        }
        if (board.getSideToMove().value().equalsIgnoreCase(this.side.value())){
            Move other = null;
            for (Move temp : MoveGenerator.generateLegalMoves(board)){
                board.doMove(temp);
                nodeCount++;
                int currentScore = minimax(depth + 1, alpha, beta, board.getSideToMove(), board);
                board.undoMove();

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
                board.doMove(temp);
                nodeCount++;
                int currentScore = minimax(depth + 1, alpha, beta, board.getSideToMove(), board);
                board.undoMove();
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
