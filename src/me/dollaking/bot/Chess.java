package me.dollaking.bot;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

import me.dollaking.bot.UCI;

import java.util.Scanner;

public class Chess {

    public static void main(String[] args) throws MoveGeneratorException, InterruptedException {
        UCI.uciCommunication();
    }


}
