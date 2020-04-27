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


/**
 * This bot is using minimax algorithm with alpha beta pruning
 * Transposition are also used
 * Move orders are used but very basic; Moves are ordered based on (the value of the piece that is being captured) - (the value of the piece that is doing the capturing)
 */
public class MinimaxPruning implements ChessBot {
    public Side side;
    private int nodeCount;
    private Move bestNextMove;
    private Heuristic heuristic;
    private ArrayList<Zobrist> zobList;
    private HashMap<Board, Integer> tranpositionList;
    private HashMap<Board, Integer> enemyTranpositionList;
    private long startTime;
    private long maxDepth;
    private long timeLimit;
    private long endTime;
    private long maxValue;
   //private HashMap<Integer, MoveHistoryEntry> bestMoveHistory;

    /**
     * Constructor
     * @param side This is the side that the bot is in
     */
    public MinimaxPruning (Side side){
        //Side of the bot
        this.side = side;
        //Change your heuristic HERE
        this.heuristic = new StaticBoardEvaluation(side);

        this.zobList = new ArrayList<Zobrist>();
        this.tranpositionList = new HashMap<Board, Integer>();
        this.enemyTranpositionList = new HashMap<Board, Integer>();
        this.timeLimit = 10000;
        //this.bestMoveHistory = new HashMap<Integer, MoveHistoryEntry>();
    }

    /**
     * Getting the Zobrist class that contains all the keys for each piece within the square
     * @param square The square that the Zobrist class belongs to
     * @return
     */
    private Zobrist getZobBySquare(Square square){
        for (Zobrist zob : zobList){
            if (zob.getSquare() == square){
                return zob;
            }
        }
        return null;
    }

    /**
     * Calculates the best next move
     * @param board The current board
     * @return The best next move
     * @throws MoveGeneratorException
     * @throws InterruptedException
     */
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


    /**
     *
     * @param board Current Board
     * @return Best next move
     * @throws MoveGeneratorException
     * @throws InterruptedException
     */
    Move bestMove(Board board) throws MoveGeneratorException, InterruptedException {
        Move move = null;
        nodeCount = 0;
        int depth = 6;
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime + timeLimit;
        if (board.getMoveCounter() < 10){
            depth = 5;
        }
        //System.out.println("Start: " + this.startTime + " End: " + this.endTime);
        //while (this.startTime <= this.endTime){
            this.tranpositionList = new HashMap<Board, Integer>();
            this.enemyTranpositionList = new HashMap<Board, Integer>();
            System.out.println("info: Searching depth " + depth);
            minimax(0, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, board.getSideToMove(), board);
            System.out.println("info: Value " + this.maxValue);
            System.out.println("info: Move " + this.bestNextMove.toString());
            //depth++;
            this.startTime = System.currentTimeMillis();
        //}

        //System.out.println("info Depth Searched: " + maxDepth);
        System.out.println("info Number of Nodes Visited: " + nodeCount);

        return bestNextMove;
    }

    /**
     * Get the value of the Pieces
     * @param piece The piece that you want the value of
     * @return The value of the piece
     */
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
        } else if (piece.getPieceType() == PieceType.KING){
            return 20;
        }
        return 0;

    }

    /**
     * Sorts the list based on (the value of the piece that is being captured) - (the value of the piece that is doing the capturing)
     */
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

    /**
     * The minimax algorthim
     * @param depth The depth that the algorithm is currently running
     * @param boundDepth The max depth that the algorithm will go to
     * @param alpha The maximum board score (First call of minimax alpha should be Integer.MIN)
     * @param beta The minimum board score (First call of minimax alpha should be Integer.MAX)
     * @param side
     * @param board The board you want to evaluate and get the score of
     * @return The highest board evaluated score
     * @throws MoveGeneratorException
     * @throws InterruptedException
     */
    int minimax(int depth, int boundDepth, int alpha, int beta, Side side, Board board) throws MoveGeneratorException, InterruptedException {

        if (depth == boundDepth || board.isDraw() || board.isMated() || board.isStaleMate()){
            if (depth > maxDepth){
                maxDepth = depth;
            }
            int totalValue = 0;
            //System.out.println("On " + board.getSideToMove().value());
            totalValue = heuristic.calculateScore(board, depth);
            return totalValue;

        }


        MoveList moveList = MoveGenerator.generateLegalMoves(board);
        Collections.sort(moveList, new sortByCaptureValue(board));

        /*try {
            if (board.isMoveLegal(bestMoveHistory.get(depth).getMove(), true)){
                moveList.add(0, bestMoveHistory.get(depth).getMove());

            }
        } catch (RuntimeException e){
            //System.out.println("ERROR");
        }*/

        if (board.getSideToMove().value().equalsIgnoreCase(this.side.value())){
            if (this.bestNextMove != null && depth == 0){
                moveList.add(0, this.bestNextMove);
            }
            Move other = null;
            for (Move temp : moveList){
                board.doMove(temp);
                int currentScore;

                if (tranpositionList.containsKey(board)){
                    currentScore = tranpositionList.get(board);
                } else {
                    nodeCount++;
                    currentScore = minimax(depth + 1, boundDepth, alpha, beta, board.getSideToMove(), board);
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
                this.maxValue = alpha;
                //bestMoveHistory.put(depth, new MoveHistoryEntry(other, alpha));
            } /*else {
                if (bestMoveHistory.get(depth) != null){
                    if (bestMoveHistory.get(depth).getScore() < alpha){
                        bestMoveHistory.put(depth, new MoveHistoryEntry(other, alpha));
                    }
                } else {
                    bestMoveHistory.put(depth, new MoveHistoryEntry(other, alpha));
                }

            }*/
            return alpha;

        } else {
            for (Move temp : moveList){
                board.doMove(temp);
                int currentScore;

                if (enemyTranpositionList.containsKey(board)){
                    currentScore = enemyTranpositionList.get(board);
                } else {
                    nodeCount++;
                    currentScore = minimax(depth + 1, boundDepth, alpha, beta, board.getSideToMove(), board);
                    enemyTranpositionList.put(board, currentScore);
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
