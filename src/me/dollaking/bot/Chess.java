import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

import java.util.Scanner;

public class Chess {
    public static void main(String[] args) throws MoveGeneratorException {
        Board board = new Board();
        Scanner scanner = new Scanner(System.in);
        String input;
        Side botSide;
        System.out.println("Which side is the bot on? Type 'black' or 'white'!");
        input = scanner.nextLine().toUpperCase();
        System.out.println("The bot is on " + input + " side!");
        botSide = Side.valueOf(input);
        Side player = botSide.flip();
        Side turn;
        ChessBot bot = new MinimaxPruning(botSide);
        Move nextMove;
        boolean isLegal;
        if (player.toString().equalsIgnoreCase("WHITE")){
            turn = player;
        } else {
            turn = botSide;
        }


        while (!board.isDraw() && !board.isStaleMate() && !board.isInsufficientMaterial() && !board.isMated()){
            if (turn.toString().equalsIgnoreCase(player.toString())){
                System.out.println("Choose your move:");
                try {
                    isLegal = board.doMove(stringToMove(scanner.nextLine()));
                } catch (NullPointerException e){
                    isLegal = false;
                } catch (IllegalArgumentException e){
                    isLegal = false;
                }


            } else {

                long startTime = System.nanoTime();
                nextMove = bot.calculateNextMove(board);
                long endTime = System.nanoTime();

                long duration = (endTime - startTime) /1000000000;
                System.out.println("Took " + duration + " seconds!");

                System.out.println("Bots Moves to " + nextMove.toString() + "!");
                isLegal = board.doMove(nextMove);
            }

            if (!isLegal){
                System.out.println("Illegal Move!");
            } else {
                //System.out.println(board.toString());
                turn = turn.flip();
            }

        }

        if (board.isDraw()){
            System.out.println("This is a draw!");
        } else if (board.isStaleMate()){
            System.out.println("This is a stalemate!");
        } else if (board.isMated()) {
            System.out.println(board.getSideToMove() + " got checkmated!");
        }

        scanner.close();
    }

    public static Move stringToMove(String input){
        String current = input.substring(0,2).toUpperCase();
        String next = input.substring(2,4).toUpperCase();

        Square currentSquare = Square.valueOf(current);
        Square nextSquare = Square.valueOf(next);

        Move move = new Move(currentSquare, nextSquare);
        return move;
    }

}
