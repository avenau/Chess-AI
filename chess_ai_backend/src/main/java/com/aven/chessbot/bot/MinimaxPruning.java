package com.aven.chessbot.bot;

import com.aven.chessbot.components.TranspositionEntry;
import com.aven.chessbot.heuristic.Heuristic;
import com.aven.chessbot.heuristic.StaticBoardEvaluation;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

import java.util.HashMap;
import java.util.List;

/**
 * This bot is using minimax algorithm with alpha beta pruning Transposition are also used Move
 * orders are used but very basic; Moves are ordered based on (the value of the piece that is being
 * captured) - (the value of the piece that is doing the capturing)
 */
public class MinimaxPruning implements ChessBot {
    private static final int MAX_SEARCH_DEPTH = 128;
    private final long TIME_LIMIT = 15000;
    public Side side;
    private int nodeCount;
    private Move bestNextMove;
    private final Heuristic heuristic;
    private final HashMap<Long, TranspositionEntry> transpositionTable;
    private final Move[][] killerMoves;
    private final int[][][] historyHeuristic;
    private long startTime;
    private long firstStartTime;
    private long maxDepth;
    private long endTime;
    private long maxValue;

    /**
     * Constructor
     *
     * @param side This is the side that the bot is in
     */
    public MinimaxPruning(Side side) {
        this.side = side;
        this.heuristic = new StaticBoardEvaluation(side);
        this.transpositionTable = new HashMap<>();
        this.killerMoves = new Move[MAX_SEARCH_DEPTH][2];
        this.historyHeuristic = new int[Side.values().length][64][64];
    }

    /**
     * Calculates the best next move
     *
     * @param board The current board
     * @return The best next move
     * @throws MoveGeneratorException
     * @throws InterruptedException
     */
    @Override
    public Move calculateNextMove(Board board) throws MoveGeneratorException, InterruptedException {
        return bestMove(board);
    }

    /**
     * @param board Current Board
     * @return Best next move
     * @throws MoveGeneratorException
     */
    Move bestMove(Board board) throws MoveGeneratorException {
        nodeCount = 0;
        maxDepth = 0;
        maxValue = 0;
        clearMoveOrderingState();

        int depth = 2;
        List<Move> rootMoves = MoveGenerator.generateLegalMoves(board);
        if (rootMoves.isEmpty()) {
            bestNextMove = null;
            return null;
        }
        if (bestNextMove == null || !rootMoves.contains(bestNextMove)) {
            bestNextMove = rootMoves.getFirst();
        }

        this.startTime = System.currentTimeMillis();
        this.firstStartTime = System.currentTimeMillis();
        this.endTime = startTime + TIME_LIMIT;

        System.out.println("Start: " + this.startTime + " End: " + this.endTime);
        while (this.startTime <= this.endTime) {
            System.out.println("info: Searching depth " + depth);
            try {
                minimax(0, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, board);
            } catch (InterruptedException e) {
                System.out.println("info: Search interrupted due to time limit");
                break;
            }
            depth++;
            this.startTime = System.currentTimeMillis();
            if (this.maxValue >= 100_000_000 || this.maxValue <= -100_000_000) {
                break;
            }
        }

        System.out.println("info: Value " + this.maxValue);
        System.out.println("info: Move " + (this.bestNextMove != null ? this.bestNextMove : "(none)"));
        System.out.println("info Depth Searched: " + maxDepth);
        System.out.println("info Number of Nodes Visited: " + nodeCount);
        System.out.println(
                "Time taken: " + (System.currentTimeMillis() - this.firstStartTime) / 1000 + " seconds");
        return bestNextMove;
    }

    /**
     * The minimax algorthim
     *
     * @param depth The depth that the algorithm is currently running
     * @param boundDepth The max depth that the algorithm will go to
     * @param alpha The maximum board score (First call of minimax alpha should be Integer.MIN)
     * @param beta The minimum board score (First call of minimax alpha should be Integer.MAX)
     * @param board The board you want to evaluate and get the score of
     * @return The highest board evaluated score
     * @throws MoveGeneratorException
     * @throws InterruptedException
     */
    int minimax(int depth, int boundDepth, int alpha, int beta, Board board)
            throws MoveGeneratorException, InterruptedException {

        if (System.currentTimeMillis() >= endTime) {
            throw new InterruptedException("Time limit exceeded");
        }

        long positionKey = board.getIncrementalHashKey();
        TranspositionEntry entry = transpositionTable.get(positionKey);
        if (entry != null && entry.getDepth() >= boundDepth - depth) {
            if (entry.getFlag() == TranspositionEntry.EXACT) {
                return entry.getScore();
            }
            if (entry.getFlag() == TranspositionEntry.LOWERBOUND && entry.getScore() > alpha) {
                alpha = entry.getScore();
            }
            if (entry.getFlag() == TranspositionEntry.UPPERBOUND && entry.getScore() < beta) {
                beta = entry.getScore();
            }
            if (alpha >= beta) {
                return entry.getScore();
            }
        }

        if (depth == boundDepth || board.isDraw() || board.isMated() || board.isStaleMate()) {
            if (depth > maxDepth) {
                maxDepth = depth;
            }
            return heuristic.calculateScore(board, depth);
        }

        List<Move> moveList = MoveGenerator.generateLegalMoves(board);
        if (moveList.isEmpty()) {
            return heuristic.calculateScore(board, depth);
        }
        moveList =
                CaptureValueMoveComparator.orderMoves(
                        board,
                        moveList,
                        depth,
                        entry != null ? entry.getBestMove() : null,
                        bestNextMove,
                        killerMoves,
                        historyHeuristic);

        if (board.getSideToMove().value().equalsIgnoreCase(this.side.value())) {
            Move bestMove = null;
            int originalAlpha = alpha;
            int originalBeta = beta;

            for (Move temp : moveList) {
                board.doMove(temp);
                nodeCount++;
                int currentScore = minimax(depth + 1, boundDepth, alpha, beta, board);
                board.undoMove();

                if (currentScore > alpha) {
                    alpha = currentScore;
                    bestMove = temp;
                }
                if (alpha >= beta) {
                    recordKillerMove(depth, temp, board);
                    recordHistoryMove(board.getSideToMove(), temp, boundDepth - depth);
                    transpositionTable.put(
                            positionKey,
                            new TranspositionEntry(
                                    alpha, boundDepth - depth, TranspositionEntry.LOWERBOUND, bestMove));
                    break;
                }
            }

            byte flag =
                    alpha <= originalAlpha
                            ? TranspositionEntry.UPPERBOUND
                            : alpha >= originalBeta ? TranspositionEntry.LOWERBOUND : TranspositionEntry.EXACT;
            transpositionTable.put(
                    positionKey, new TranspositionEntry(alpha, boundDepth - depth, flag, bestMove));

            if (depth == 0) {
                bestNextMove = bestMove;
                this.maxValue = alpha;
            }
            return alpha;
        }

        Move bestMove = null;
        int originalAlpha = alpha;
        int originalBeta = beta;

        for (Move temp : moveList) {
            board.doMove(temp);
            nodeCount++;
            int currentScore = minimax(depth + 1, boundDepth, alpha, beta, board);
            board.undoMove();

            if (currentScore < beta) {
                beta = currentScore;
                bestMove = temp;
            }

            if (alpha >= beta) {
                recordKillerMove(depth, temp, board);
                recordHistoryMove(board.getSideToMove(), temp, boundDepth - depth);
                transpositionTable.put(
                        positionKey,
                        new TranspositionEntry(
                                beta, boundDepth - depth, TranspositionEntry.UPPERBOUND, bestMove));
                break;
            }
        }

        byte flag =
                beta <= originalAlpha
                        ? TranspositionEntry.UPPERBOUND
                        : beta >= originalBeta ? TranspositionEntry.LOWERBOUND : TranspositionEntry.EXACT;
        transpositionTable.put(
                positionKey, new TranspositionEntry(beta, boundDepth - depth, flag, bestMove));
        return beta;
    }

    private void clearMoveOrderingState() {
        for (int ply = 0; ply < killerMoves.length; ply++) {
            killerMoves[ply][0] = null;
            killerMoves[ply][1] = null;
        }
        for (int sideIndex = 0; sideIndex < historyHeuristic.length; sideIndex++) {
            for (int from = 0; from < historyHeuristic[sideIndex].length; from++) {
                for (int to = 0; to < historyHeuristic[sideIndex][from].length; to++) {
                    historyHeuristic[sideIndex][from][to] = 0;
                }
            }
        }
    }

    private void recordKillerMove(int depth, Move move, Board board) {
        if (depth >= killerMoves.length || isCapture(board, move) || isPromotion(move)) {
            return;
        }
        if (move.equals(killerMoves[depth][0])) {
            return;
        }
        killerMoves[depth][1] = killerMoves[depth][0];
        killerMoves[depth][0] = move;
    }

    private void recordHistoryMove(Side sideToMove, Move move, int remainingDepth) {
        int bonus = remainingDepth * remainingDepth;
        historyHeuristic[sideToMove.ordinal()][move.getFrom().ordinal()][move.getTo().ordinal()] +=
                bonus;
    }

    private boolean isCapture(Board board, Move move) {
        Piece capturedPiece = board.getPiece(move.getTo());
        return capturedPiece != null && capturedPiece != Piece.NONE;
    }

    private boolean isPromotion(Move move) {
        return move.getPromotion() != null && move.getPromotion() != Piece.NONE;
    }
}
