package com.aven.chessbot.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import org.junit.jupiter.api.Test;

class MovePickerTest {

    @Test
    void rootPvMoveIsPickedFirst() throws MoveGeneratorException {
        Board board = new Board();
        board.loadFromFen("4k3/8/8/3p4/4P3/8/6N1/4K3 w - - 0 1");

        MovePicker movePicker =
                new MovePicker(
                        board,
                        MoveGenerator.generateLegalMoves(board),
                        0,
                        null,
                        new Move("g2f4", board.getSideToMove()),
                        new Move[128][2],
                        new int[2][64][64]);

        assertEquals(new Move("g2f4", board.getSideToMove()), movePicker.pickNextMove(0));
    }

    @Test
    void enPassantIsPickedAsCapture() throws MoveGeneratorException {
        Board board = new Board();
        board.loadFromFen("4k3/8/8/3pP3/8/8/6N1/4K3 w - d6 0 1");

        MovePicker movePicker =
                new MovePicker(
                        board,
                        MoveGenerator.generateLegalMoves(board),
                        1,
                        null,
                        null,
                        new Move[128][2],
                        new int[2][64][64]);

        assertEquals(new Move("e5d6", board.getSideToMove()), movePicker.pickNextMove(0));
    }
}
