package me.dollaking.bot;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MinimaxPruning implements ChessBot {
    public Side side;
    private int nodeCount;
    private Move bestNextMove;
    private StandardStrategy heuristic;
    private ArrayList<Zobrist> zobList;
    private HashMap<Board, Integer> tranpositionList;

    public MinimaxPruning (Side side){
        this.side = side;
        this.heuristic = new StandardStrategy(side);
        this.zobList = new ArrayList<Zobrist>();
        this.tranpositionList = new HashMap<Board, Integer>();
    }

    private Zobrist getZobBySquare(Square square){
        for (Zobrist zob : zobList){
            if (zob.getSquare() == square){
                return zob;
            }
        }
        return null;
    }


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



    Move bestMove(Board board) throws MoveGeneratorException, InterruptedException {
        Move move = null;
        nodeCount = 0;
        int max = Integer.MIN_VALUE;
        minimax(0, Integer.MIN_VALUE, Integer.MAX_VALUE, board.getSideToMove(), board);
        System.out.println("info Number of Nodes Visited: " + nodeCount);

        return bestNextMove;
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

    class sortByCaptureValue implements Comparator<Move> {
        private Board compareBoard;
        public sortByCaptureValue(Board compareBoard){
            this.compareBoard = compareBoard;
        }
        @Override
        public int compare(Move move, Move t1) {
            int capturePieceValueT1 = getPieceValue(compareBoard.getPiece(t1.getTo()));
            int capturePieceValueMove = getPieceValue(compareBoard.getPiece(move.getTo()));
            if (!(capturePieceValueT1 - capturePieceValueMove == 0)){
                return capturePieceValueT1 - capturePieceValueMove;
            }

            return getPieceValue(compareBoard.getPiece(move.getTo())) - getPieceValue(compareBoard.getPiece(t1.getTo()));

        }
    }



    int minimax(int depth, int alpha, int beta, Side side, Board board) throws MoveGeneratorException, InterruptedException {

        int totalValue = 0;

        totalValue = heuristic.calculateScore(board);


        if (depth == 6 || totalValue >= 10000 || totalValue <= -10000){

            return totalValue;
        }
        MoveList moveList = MoveGenerator.generateLegalMoves(board);
        Collections.sort(moveList, new sortByCaptureValue(board));

        if (board.getSideToMove().value().equalsIgnoreCase(this.side.value())){
            Move other = null;
            for (Move temp : moveList){
                board.doMove(temp);
                int currentScore;

                if (tranpositionList.containsKey(board)){
                    currentScore = tranpositionList.get(board);
                } else {
                    nodeCount++;
                    currentScore = minimax(depth + 1, alpha, beta, board.getSideToMove(), board);
                    tranpositionList.put(board, currentScore);
                }

                board.undoMove();

                if (currentScore > alpha){
                    alpha = currentScore;
                    other = temp;
                }

                if (alpha >= beta){
                    break;
                }
            }
            if (depth == 0){
                bestNextMove = other;

            }
            return alpha;

        } else {
            for (Move temp : moveList){
                board.doMove(temp);
                int currentScore;

                if (tranpositionList.containsKey(board)){
                    currentScore = tranpositionList.get(board);
                } else {
                    nodeCount++;
                    currentScore = minimax(depth + 1, alpha, beta, board.getSideToMove(), board);
                    tranpositionList.put(board, currentScore);
                }

                board.undoMove();
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
