package com.aven.chessbot.bot;

import com.aven.chessbot.util.ChessUtil;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.List;

class MovePicker {
    private final List<Move> moves;
    private final int[] scores;
    private final int[] capturedValues;
    private final int[] attackerValues;

    MovePicker(
            Board board,
            List<Move> moveList,
            int depth,
            Move ttMove,
            Move pvMove,
            Move[][] killerMoves,
            int[][][] historyHeuristic) {
        this.moves = moveList;
        this.scores = new int[moves.size()];
        this.capturedValues = new int[moves.size()];
        this.attackerValues = new int[moves.size()];

        for (int index = 0; index < moves.size(); index++) {
            Move move = moves.get(index);
            scores[index] =
                    CaptureValueMoveComparator.scoreMove(
                            board, move, depth, ttMove, pvMove, killerMoves, historyHeuristic);
            capturedValues[index] =
                    ChessUtil.getPieceValue(CaptureValueMoveComparator.getCapturedPiece(board, move));
            attackerValues[index] = ChessUtil.getPieceValue(board.getPiece(move.getFrom()));
        }
    }

    int size() {
        return moves.size();
    }

    Move pickNextMove(int startIndex) {
        int bestIndex = startIndex;
        for (int index = startIndex + 1; index < moves.size(); index++) {
            if (isBetterMove(index, bestIndex)) {
                bestIndex = index;
            }
        }
        swap(startIndex, bestIndex);
        return moves.get(startIndex);
    }

    private boolean isBetterMove(int candidateIndex, int currentBestIndex) {
        int candidateScore = scores[candidateIndex];
        int currentBestScore = scores[currentBestIndex];
        if (candidateScore != currentBestScore) {
            return candidateScore > currentBestScore;
        }

        int capturedCandidateValue = capturedValues[candidateIndex];
        int capturedBestValue = capturedValues[currentBestIndex];
        if (capturedCandidateValue != capturedBestValue) {
            return capturedCandidateValue > capturedBestValue;
        }

        int attackerCandidateValue = attackerValues[candidateIndex];
        int attackerBestValue = attackerValues[currentBestIndex];
        return attackerCandidateValue < attackerBestValue;
    }

    private void swap(int firstIndex, int secondIndex) {
        if (firstIndex == secondIndex) {
            return;
        }

        Move firstMove = moves.get(firstIndex);
        moves.set(firstIndex, moves.get(secondIndex));
        moves.set(secondIndex, firstMove);

        int firstScore = scores[firstIndex];
        scores[firstIndex] = scores[secondIndex];
        scores[secondIndex] = firstScore;

        int firstCapturedValue = capturedValues[firstIndex];
        capturedValues[firstIndex] = capturedValues[secondIndex];
        capturedValues[secondIndex] = firstCapturedValue;

        int firstAttackerValue = attackerValues[firstIndex];
        attackerValues[firstIndex] = attackerValues[secondIndex];
        attackerValues[secondIndex] = firstAttackerValue;
    }
}
