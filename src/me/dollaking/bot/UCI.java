package me.dollaking.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

import java.util.Scanner;

/**
 * UCI Protocol
 */
public class UCI {
    static String ENGINENAME="AvenEngine";
    public static void uciCommunication() throws MoveGeneratorException, InterruptedException {
        Board board = new Board();
        Scanner input = new Scanner(System.in);
        while (true)
        {
            String inputString=input.nextLine();
            if ("uci".equals(inputString))
            {
                inputUCI();
            }
            else if ("isready".equals(inputString))
            {
                inputIsReady();
            }
            else if (inputString.startsWith("position"))
            {
                board = inputPosition(inputString, board);

            }
            else if (inputString.startsWith("go"))
            {
                inputGo(board);
            }
            else if (inputString.equals("quit"))
            {
                inputQuit();
            }
            else if ("print".equals(inputString))
            {
                inputPrint(board);
            }
        }
    }
    public static void inputUCI() {
        System.out.println("id name "+ENGINENAME);
        System.out.println("id author dollaking");
        //options go here
        System.out.println("uciok");
    }

    public static void inputIsReady() {
        System.out.println("readyok");
    }
    public static Board inputPosition(String input, Board board) {
        input=input.substring(9).concat(" ");
       if (input.contains("startpos")) {
            board = new Board();
        }
        if (input.contains("fen")) {
            input=input.substring(4);
            board.loadFromFen(input);
        }
        if (input.contains("moves")) {
            input=input.substring(input.indexOf("moves")+6);
            String[] pastMoves = input.split(" ");
            for (String indexMove : pastMoves){
                board.doMove(stringToMove(indexMove, board));
            }
        }
        return board;
    }


    public static Move stringToMove(String input, Board board){
        String current = input.substring(0,2).toUpperCase();
        String next = input.substring(2,4).toUpperCase();
        char promoted = 'x';

        if (input.length() == 5){
            promoted = input.charAt(4);
        }

        Square currentSquare = Square.valueOf(current);
        Square nextSquare = Square.valueOf(next);
        Move move = null;
        if (board.getSideToMove().value().equalsIgnoreCase("BLACK")){
            if (promoted == 'x'){
                move = new Move(currentSquare, nextSquare);
            } else {
                switch(promoted){
                    case 'q':
                        move = new Move(currentSquare, nextSquare, Piece.BLACK_QUEEN);
                        break;
                    case 'r':
                        move = new Move(currentSquare, nextSquare, Piece.BLACK_ROOK);
                        break;
                    case 'b':
                        move = new Move(currentSquare, nextSquare, Piece.BLACK_BISHOP);
                        break;
                    case 'n':
                        move = new Move(currentSquare, nextSquare, Piece.BLACK_KNIGHT);
                }
            }

        } else {
            if (promoted == 'x'){
                move = new Move(currentSquare, nextSquare);
            } else {
                switch(promoted){
                    case 'q':
                        move = new Move(currentSquare, nextSquare, Piece.WHITE_QUEEN);
                        break;
                    case 'r':
                        move = new Move(currentSquare, nextSquare, Piece.WHITE_ROOK);
                        break;
                    case 'b':
                        move = new Move(currentSquare, nextSquare, Piece.WHITE_BISHOP);
                        break;
                    case 'n':
                        move = new Move(currentSquare, nextSquare, Piece.WHITE_KNIGHT);
                        break;
                }
            }
        }

        return move;
    }
    public static void inputGo(Board board) throws MoveGeneratorException, InterruptedException {
        ChessBot bot = new MinimaxPruning(board.getSideToMove());

        long startTime = System.nanoTime();
        Move bestMove = bot.calculateNextMove(board);
        long endTime = System.nanoTime();
        System.out.println("info Took "+(endTime - startTime)/1000000000 + " seconds");

        if (bestMove == null){
            System.out.println("bestmove 0000");
        } else {
            System.out.println("bestmove " + bestMove.toString());
        }

    }
    public static void inputQuit() {
        System.exit(0);
    }
    public static void inputPrint(Board board) {
       System.out.println(board.toString());
    }
}
