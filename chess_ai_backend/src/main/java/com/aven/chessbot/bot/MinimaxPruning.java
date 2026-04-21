package com.aven.chessbot.bot;

import com.aven.chessbot.components.TranspositionEntry;
import com.aven.chessbot.components.TranspositionTable;
import com.aven.chessbot.heuristic.Heuristic;
import com.aven.chessbot.heuristic.OptimizedHeuristic;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

import java.util.ArrayList;
import java.util.List;

/**
 * This bot is using minimax algorithm with alpha beta pruning Transposition are also used Move
 * orders are used but very basic; Moves are ordered based on (the value of the piece that is being
 * captured) - (the value of the piece that is doing the capturing)
 */
public class MinimaxPruning implements ChessBot {
    private static final int MAX_SEARCH_DEPTH = 128;
    private static final int CHECKMATE_SCORE = 100_000_000;
    private static final int NULL_MOVE_REDUCTION = 2;
    private static final int NULL_MOVE_MIN_REMAINING_DEPTH = NULL_MOVE_REDUCTION + 2;
    private static final boolean LOG_SEARCH_INFO = true;
    private final long TIME_LIMIT = 10000;
    private final Heuristic heuristic;
    private final TranspositionTable transpositionTable;
    private final Move[][] killerMoves;
    private final int[][][] historyHeuristic;
    public Side side;
    private int nodeCount;
    private Move bestNextMove;
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
        this.heuristic = new OptimizedHeuristic(side);
        this.transpositionTable = new TranspositionTable();
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
        if (LOG_SEARCH_INFO) {
            System.out.println("info: Calculating best move!");
        }
        while (this.startTime <= this.endTime) {
            if (LOG_SEARCH_INFO) {
                System.out.println("info: Searching at depth " + depth);
            }
            try {
                minimax(0, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, board, true);
            } catch (InterruptedException e) {
                if (LOG_SEARCH_INFO) {
                    System.out.println("info: Search interrupted due to time limit");
                }
                break;
            }
            depth++;
            this.startTime = System.currentTimeMillis();
            if (this.maxValue >= 100_000_000 || this.maxValue <= -100_000_000) {
                break;
            }
        }

        if (LOG_SEARCH_INFO) {
            System.out.println("info: Value " + this.maxValue);
            System.out.println("info: Move " + (this.bestNextMove != null ? this.bestNextMove : "(none)"));
            //System.out.println("info Depth Searched: " + maxDepth);
            System.out.println("info Number of Nodes Visited: " + nodeCount);
            System.out.println(
                    "Time taken: " + (System.currentTimeMillis() - this.firstStartTime) / 1000 + " seconds");
        }
        return bestNextMove;
    }

    /**
     * The minimax algorthim
     *
     * @param depth         The depth that the algorithm is currently running
     * @param boundDepth    The max depth that the algorithm will go to
     * @param alpha         The maximum board score (First call of minimax alpha should be Integer.MIN)
     * @param beta          The minimum board score (First call of minimax alpha should be Integer.MAX)
     * @param board         The board you want to evaluate and get the score of
     * @param allowNullMove Whether null move pruning can be attempted at this node
     * @return The highest board evaluated score
     * @throws MoveGeneratorException
     * @throws InterruptedException
     */
    int minimax(int depth, int boundDepth, int alpha, int beta, Board board, boolean allowNullMove)
            throws MoveGeneratorException, InterruptedException {

        checkTimeLimit();
        updateMaxDepth(depth);

        if (isForcedDraw(board)) {
            return 0;
        }

        int remainingDepth = boundDepth - depth;
        if (remainingDepth <= 0) {
            return quiescence(alpha, beta, board, depth);
        }

        long positionKey = board.getIncrementalHashKey();
        int ttSlot = transpositionTable.findSlot(positionKey);
        Move ttMove = ttSlot >= 0 ? transpositionTable.getBestMove(ttSlot) : null;
        if (ttSlot >= 0 && transpositionTable.getDepth(ttSlot) >= remainingDepth) {
            int ttScore = transpositionTable.getScore(ttSlot);
            byte ttFlag = transpositionTable.getFlag(ttSlot);
            if (ttFlag == TranspositionEntry.EXACT) {
                return ttScore;
            }
            if (ttFlag == TranspositionEntry.LOWERBOUND && ttScore > alpha) {
                alpha = ttScore;
            }
            if (ttFlag == TranspositionEntry.UPPERBOUND && ttScore < beta) {
                beta = ttScore;
            }
            if (alpha >= beta) {
                return ttScore;
            }
        }

        Integer nullMoveCutoff =
                tryNullMovePruning(depth, boundDepth, alpha, beta, board, positionKey, ttMove, allowNullMove);
        if (nullMoveCutoff != null) {
            return nullMoveCutoff;
        }

        List<Move> moveList = MoveGenerator.generateLegalMoves(board);
        if (moveList.isEmpty()) {
            return evaluateNoLegalMoves(board, depth);
        }
        MovePicker movePicker =
                new MovePicker(
                        board,
                        moveList,
                        depth,
                        ttMove,
                        bestNextMove,
                        killerMoves,
                        historyHeuristic);

        if (board.getSideToMove() == this.side) {
            Move bestMove = null;
            int originalAlpha = alpha;
            int originalBeta = beta;
            boolean firstMove = true;

            for (int moveIndex = 0; moveIndex < movePicker.size(); moveIndex++) {
                Move temp = movePicker.pickNextMove(moveIndex);
                board.doMove(temp);
                nodeCount++;
                int currentScore;
                if (firstMove) {
                    currentScore = minimax(depth + 1, boundDepth, alpha, beta, board, true);
                } else {
                    currentScore =
                            minimax(
                                    depth + 1,
                                    boundDepth,
                                    alpha,
                                    incrementWithoutOverflow(alpha),
                                    board,
                                    true);
                    if (currentScore > alpha && currentScore < beta) {
                        currentScore = minimax(depth + 1, boundDepth, alpha, beta, board, true);
                    }
                }
                board.undoMove();
                firstMove = false;

                if (currentScore > alpha) {
                    alpha = currentScore;
                    bestMove = temp;
                }
                if (alpha >= beta) {
                    recordKillerMove(depth, temp, board);
                    recordHistoryMove(board.getSideToMove(), temp, boundDepth - depth);
                    transpositionTable.store(
                            positionKey,
                            alpha,
                            remainingDepth,
                            TranspositionEntry.LOWERBOUND,
                            bestMove);
                    break;
                }
            }

            byte flag =
                    alpha <= originalAlpha
                            ? TranspositionEntry.UPPERBOUND
                            : alpha >= originalBeta ? TranspositionEntry.LOWERBOUND : TranspositionEntry.EXACT;
            transpositionTable.store(positionKey, alpha, remainingDepth, flag, bestMove);

            if (depth == 0) {
                bestNextMove = bestMove;
                this.maxValue = alpha;
            }
            return alpha;
        }

        Move bestMove = null;
        int originalAlpha = alpha;
        int originalBeta = beta;
        boolean firstMove = true;

        for (int moveIndex = 0; moveIndex < movePicker.size(); moveIndex++) {
            Move temp = movePicker.pickNextMove(moveIndex);
            board.doMove(temp);
            nodeCount++;
            int currentScore;
            if (firstMove) {
                currentScore = minimax(depth + 1, boundDepth, alpha, beta, board, true);
            } else {
                currentScore =
                        minimax(
                                depth + 1,
                                boundDepth,
                                decrementWithoutOverflow(beta),
                                beta,
                                board,
                                true);
                if (currentScore > alpha && currentScore < beta) {
                    currentScore = minimax(depth + 1, boundDepth, alpha, beta, board, true);
                }
            }
            board.undoMove();
            firstMove = false;

            if (currentScore < beta) {
                beta = currentScore;
                bestMove = temp;
            }

            if (alpha >= beta) {
                recordKillerMove(depth, temp, board);
                recordHistoryMove(board.getSideToMove(), temp, boundDepth - depth);
                transpositionTable.store(
                        positionKey,
                        beta,
                        remainingDepth,
                        TranspositionEntry.UPPERBOUND,
                        bestMove);
                break;
            }
        }

        byte flag =
                beta <= originalAlpha
                        ? TranspositionEntry.UPPERBOUND
                        : beta >= originalBeta ? TranspositionEntry.LOWERBOUND : TranspositionEntry.EXACT;
        transpositionTable.store(positionKey, beta, remainingDepth, flag, bestMove);
        return beta;
    }

    private int quiescence(int alpha, int beta, Board board, int depth)
            throws MoveGeneratorException, InterruptedException {
        checkTimeLimit();
        updateMaxDepth(depth);

        if (isForcedDraw(board)) {
            return 0;
        }

        boolean maximizingNode = board.getSideToMove() == this.side;
        boolean inCheck = board.isKingAttacked();
        int standPat = 0;

        if (!inCheck) {
            standPat = heuristic.calculateScore(board, depth);
            if (maximizingNode) {
                if (standPat >= beta) {
                    return beta;
                }
                if (standPat > alpha) {
                    alpha = standPat;
                }
            } else {
                if (standPat <= alpha) {
                    return alpha;
                }
                if (standPat < beta) {
                    beta = standPat;
                }
            }
        }

        List<Move> moveList = MoveGenerator.generateLegalMoves(board);
        if (moveList.isEmpty()) {
            return evaluateNoLegalMoves(board, depth);
        }

        if (!inCheck) {
            moveList = filterTacticalMoves(board, moveList);
            if (moveList.isEmpty()) {
                return standPat;
            }
        }

        MovePicker movePicker =
                new MovePicker(board, moveList, depth, null, null, killerMoves, historyHeuristic);

        if (maximizingNode) {
            for (int moveIndex = 0; moveIndex < movePicker.size(); moveIndex++) {
                Move move = movePicker.pickNextMove(moveIndex);
                board.doMove(move);
                nodeCount++;
                int score = quiescence(alpha, beta, board, depth + 1);
                board.undoMove();

                if (score > alpha) {
                    alpha = score;
                }
                if (alpha >= beta) {
                    return beta;
                }
            }
            return alpha;
        }

        for (int moveIndex = 0; moveIndex < movePicker.size(); moveIndex++) {
            Move move = movePicker.pickNextMove(moveIndex);
            board.doMove(move);
            nodeCount++;
            int score = quiescence(alpha, beta, board, depth + 1);
            board.undoMove();

            if (score < beta) {
                beta = score;
            }
            if (alpha >= beta) {
                return alpha;
            }
        }
        return beta;
    }

    // ===================== NULL PRUNING ============================
    private Integer tryNullMovePruning(
            int depth,
            int boundDepth,
            int alpha,
            int beta,
            Board board,
            long positionKey,
            Move ttMove,
            boolean allowNullMove)
            throws MoveGeneratorException, InterruptedException {
        int remainingDepth = boundDepth - depth;
        if (!allowNullMove
                || depth == 0
                || remainingDepth < NULL_MOVE_MIN_REMAINING_DEPTH
                || board.isKingAttacked()
                || !hasNonPawnMaterial(board, board.getSideToMove())) {
            return null;
        }

        boolean maximizingNode = board.getSideToMove() == this.side;
        //System.out.println("Info: Trying null move at depth " + depth + " with remaining depth " + remainingDepth + " and side " + (maximizingNode ? "maximizing" : "minimizing") + " node.");
        board.doNullMove();
        nodeCount++;

        int nullMoveScore;
        try {
            nullMoveScore =
                    maximizingNode
                            ? minimax(
                            depth + NULL_MOVE_REDUCTION + 1,
                            boundDepth,
                            decrementWithoutOverflow(beta),
                            beta,
                            board,
                            false)
                            : minimax(
                            depth + NULL_MOVE_REDUCTION + 1,
                            boundDepth,
                            alpha,
                            incrementWithoutOverflow(alpha),
                            board,
                            false);
        } finally {
            board.undoMove();
        }

        if (maximizingNode && nullMoveScore >= beta) {
            transpositionTable.store(
                    positionKey,
                    beta,
                    remainingDepth,
                    TranspositionEntry.LOWERBOUND,
                    ttMove);
            return beta;
        }

        if (!maximizingNode && nullMoveScore <= alpha) {
            transpositionTable.store(
                    positionKey,
                    alpha,
                    remainingDepth,
                    TranspositionEntry.UPPERBOUND,
                    ttMove);
            return alpha;
        }

        return null;
    }

    private boolean hasNonPawnMaterial(Board board, Side sideToMove) {
        long minorMajorMaterial =
                sideToMove == Side.WHITE
                        ? board.getBitboard(Piece.WHITE_KNIGHT)
                          | board.getBitboard(Piece.WHITE_BISHOP)
                          | board.getBitboard(Piece.WHITE_ROOK)
                          | board.getBitboard(Piece.WHITE_QUEEN)
                        : board.getBitboard(Piece.BLACK_KNIGHT)
                          | board.getBitboard(Piece.BLACK_BISHOP)
                          | board.getBitboard(Piece.BLACK_ROOK)
                          | board.getBitboard(Piece.BLACK_QUEEN);
        return minorMajorMaterial != 0L;
    }

    private boolean isForcedDraw(Board board) {
        return board.isRepetition() || board.isInsufficientMaterial() || board.getHalfMoveCounter() >= 100;
    }

    private int evaluateNoLegalMoves(Board board, int depth) {
        if (board.isKingAttacked()) {
            return board.getSideToMove() == side ? -CHECKMATE_SCORE + depth : CHECKMATE_SCORE - depth;
        }
        return 0;
    }

    private List<Move> filterTacticalMoves(Board board, List<Move> moveList) {
        List<Move> tacticalMoves = new ArrayList<>();
        for (Move move : moveList) {
            if (isPromotion(move) || isCapture(board, move)) {
                tacticalMoves.add(move);
            }
        }
        return tacticalMoves;
    }

    private void checkTimeLimit() throws InterruptedException {
        if (System.currentTimeMillis() >= endTime) {
            throw new InterruptedException("Time limit exceeded");
        }
    }

    private void updateMaxDepth(int depth) {
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    private int decrementWithoutOverflow(int value) {
        return value == Integer.MIN_VALUE ? Integer.MIN_VALUE : value - 1;
    }

    private int incrementWithoutOverflow(int value) {
        return value == Integer.MAX_VALUE ? Integer.MAX_VALUE : value + 1;
    }

    // ===================== NULL PRUNING ============================

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
        return CaptureValueMoveComparator.getCapturedPiece(board, move) != Piece.NONE;
    }

    private boolean isPromotion(Move move) {
        return move.getPromotion() != null && move.getPromotion() != Piece.NONE;
    }
}
