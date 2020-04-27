package me.dollaking.bot;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;

import java.util.ArrayList;
import java.util.List;

/**
 * Similar to standard strategy but now, rook placement, pawn structure and castling are considered
 */

public class StaticBoardEvaluation implements Heuristic{

    private Side side;

    /**
     *
     * @param side The side of the bot
     */
    public StaticBoardEvaluation(Side side){
        this.side = side;
    }

    @Override
    public int calculateScore(Board board, int depth) throws MoveGeneratorException {
        int total = 0;
        total = gameEnder(board, total, depth);
        if (total != 0){
            return total;
        }

        if (board.getMoveCounter() < 10) {
            total = total + spaceEvaluation(board);
        }

        total = pieceEvaluation(board, total);
        total = analyseCastle(board, total);
        total = total + analysePawn(board);
        total = total + analyseRook(board);
        return total;
    }

    /**
     * Evaluated How much space each side has control of compared to the enemy
     * @param board The board that you want to evaluate with
     * @return The score of how much space is controlled
     * @throws MoveGeneratorException
     */
    private int spaceEvaluation(Board board) throws MoveGeneratorException {
        int total;
        String fen = board.getFen();
        String[] fenSplit = fen.split(" ");
        if (board.getSideToMove().value().equalsIgnoreCase("black")){
            fenSplit[1] = "w";
        } else {
            fenSplit[1] = "b";
        }

        fen = String.join(" ", fenSplit);

        Board enemyBoard = new Board();
        enemyBoard.loadFromFen(fen);

        MoveList allyList = new MoveList();
        MoveGenerator.generatePawnCaptures(board, allyList);
        MoveGenerator.generatePawnMoves(board, allyList);
        MoveGenerator.generateKnightMoves(board, allyList);
        MoveGenerator.generateBishopMoves(board, allyList);
        MoveGenerator.generateRookMoves(board, allyList);
        MoveGenerator.generateCastleMoves(board, allyList);

        MoveList enemyList = new MoveList();
        MoveGenerator.generatePawnCaptures(enemyBoard, enemyList);
        MoveGenerator.generatePawnMoves(enemyBoard, enemyList);
        MoveGenerator.generateKnightMoves(enemyBoard, enemyList);
        MoveGenerator.generateBishopMoves(enemyBoard, enemyList);
        MoveGenerator.generateRookMoves(enemyBoard, enemyList);
        MoveGenerator.generateCastleMoves(enemyBoard, enemyList);

        int allyMoves = allyList.size();
        int enemyMoves = enemyList.size();

        //If this is the board of the bot
        if (board.getSideToMove().equals(this.side)){
            total = allyMoves - enemyMoves;
        } else {
            total = enemyMoves - allyMoves;
        }
        return total;
    }

    /**
     * Evaluate how many pieces are on the board compared to the enemy
     * @param board The board you want evaluate with
     * @param total The score of piece evaluation
     * @return
     */

    private int pieceEvaluation(Board board, int total) {
        if (board.getSideToMove().value().equalsIgnoreCase("black")){
            total = total + board.getPieceLocation(Piece.BLACK_BISHOP).size() * 340;
            total = total + board.getPieceLocation(Piece.BLACK_KNIGHT).size() * 325;
            total = total + board.getPieceLocation(Piece.BLACK_PAWN).size() * 100;
            total = total + board.getPieceLocation(Piece.BLACK_ROOK).size() * 500;
            total = total + board.getPieceLocation(Piece.BLACK_QUEEN).size() * 900;
            total = total - board.getPieceLocation(Piece.WHITE_BISHOP).size() * 340;
            total = total - board.getPieceLocation(Piece.WHITE_KNIGHT).size() * 325;
            total = total - board.getPieceLocation(Piece.WHITE_PAWN).size() * 100;
            total = total - board.getPieceLocation(Piece.WHITE_ROOK).size() * 500;
            total = total - board.getPieceLocation(Piece.WHITE_QUEEN).size() * 900;
            if (!side.value().equalsIgnoreCase("black")){
                total = total * -1;
            }

        } else {
            total = total - board.getPieceLocation(Piece.BLACK_BISHOP).size() * 340;
            total = total - board.getPieceLocation(Piece.BLACK_KNIGHT).size() * 325;
            total = total - board.getPieceLocation(Piece.BLACK_PAWN).size() * 100;
            total = total - board.getPieceLocation(Piece.BLACK_ROOK).size() * 500;
            total = total - board.getPieceLocation(Piece.BLACK_QUEEN).size() * 900;
            total = total + board.getPieceLocation(Piece.WHITE_BISHOP).size() * 340;
            total = total + board.getPieceLocation(Piece.WHITE_KNIGHT).size() * 325;
            total = total + board.getPieceLocation(Piece.WHITE_PAWN).size() * 100;
            total = total + board.getPieceLocation(Piece.WHITE_ROOK).size() * 500;
            total = total + board.getPieceLocation(Piece.WHITE_QUEEN).size() * 900;
            if (!side.value().equalsIgnoreCase("white")){
                total = total * -1;
            }
        }
        return total;
    }

    /**
     * Get score for when the game has ended
     * @param board The board that needs to be evaluated
     * @param total The score
     * @param depth The depth in minimax algorithm when this is called
     * @return The score
     */
    private int gameEnder(Board board, int total, int depth) {
        if (board.isMated()){
            if (board.getSideToMove().value().equalsIgnoreCase(side.value())){
                total = -100000000 + depth;
            } else {
                total = 100000000 - depth;
            }
        } else if (board.isDraw()){
            if (board.getSideToMove().value().equalsIgnoreCase(side.value())){
                total = -100000000 + depth;
            } else {
                total = 100000000 - depth;
            }
        }
        return total;
    }

    private int analyseCastle(Board board, int total) {
        //King Safety
        CastleRight ally = board.getCastleRight(this.side);
        CastleRight enemy = board.getCastleRight(this.side.flip());


        //If conditions for the bot
        if (board.getSideToMove().equals(this.side)){
            if (ally == CastleRight.NONE){
                total = total - 15;
            } else {
                total = total + 12;
            }

            if (enemy == CastleRight.NONE){
                total = total + 15;
            } else {
                total = total - 12;
            }
        } else {
            if (ally == CastleRight.NONE){
                total = total + 15;
            } else {
                total = total - 12;
            }

            if (enemy == CastleRight.NONE){
                total = total - 15;
            } else {
                total = total + 12;
            }
        }
        return total;
    }

    private int analysePawn(Board board){
        int total = 0;
        total = total + sameFile(board);


        return total;
    }

    private int analyseRook(Board board){
        int total = 0;
        total = total + powerRank(board);
        total = total + rookSameFile(board);

        return total;
    }

    private int rookSameFile(Board board){
        int total = 0;
        List<String> doubleFile = new ArrayList<String>();
        List<String> seenSquares = new ArrayList<String>();
        List<Square> blackRooks = board.getPieceLocation(Piece.BLACK_ROOK);
        List<Square> whiteRooks = board.getPieceLocation(Piece.WHITE_ROOK);
        List<Square> blackPawn = board.getPieceLocation(Piece.BLACK_PAWN);
        List<Square> whitePawn = board.getPieceLocation(Piece.WHITE_PAWN);




        for (Square index : blackRooks){
            int allyPawn = 0;
            int enemyPawn = 0;
            for (Square pawnIndex : blackPawn){
                if (pawnIndex.getFile().equals(index.getFile())){
                    allyPawn++;
                    break;
                }
            }

            for (Square pawnIndex : whitePawn){
                if (pawnIndex.getFile().equals(index.getFile())){
                    enemyPawn++;
                    break;
                }
            }

            if (allyPawn == 0 && enemyPawn == 0){
                total = total + 10;
            } else if (allyPawn == 0 && enemyPawn > 0){
                total = total + 3;
            }

            if (seenSquares.contains(index.getFile().value())){
                if (!doubleFile.contains(index.getFile().value())){
                    doubleFile.add(index.getFile().value());
                    if (side.value().equalsIgnoreCase("black")){
                        total = total + 15;
                    } else {
                        total = total - 15;
                    }

                }
            } else {
                seenSquares.add(index.getFile().value());
            }
        }

        doubleFile.clear();
        seenSquares.clear();

        for (Square index : whiteRooks){
            int allyPawn = 0;
            int enemyPawn = 0;
            for (Square pawnIndex : whitePawn){
                if (pawnIndex.getFile().equals(index.getFile())){
                    allyPawn++;
                    break;
                }
            }

            for (Square pawnIndex : blackPawn){
                if (pawnIndex.getFile().equals(index.getFile())){
                    enemyPawn++;
                    break;
                }
            }

            if (allyPawn == 0 && enemyPawn == 0){
                total = total + 10;
            } else if (allyPawn == 0 && enemyPawn > 0){
                total = total + 3;
            }

            if (seenSquares.contains(index.getFile().value())){
                if (!doubleFile.contains(index.getFile().value())){
                    doubleFile.add(index.getFile().value());
                    if (side.value().equalsIgnoreCase("white")){
                        total = total + 15;
                    } else {
                        total = total - 15;
                    }

                }
            } else {
                seenSquares.add(index.getFile().value());
            }
        }
        doubleFile.clear();
        seenSquares.clear();



        return total;
    }

    private int powerRank(Board board) {
        int total = 0;
        List<Square> whiteRook = board.getPieceLocation(Piece.WHITE_ROOK);
        List<Square> blackRook = board.getPieceLocation(Piece.BLACK_ROOK);

        for (Square index : whiteRook){
            if (index.getRank().getNotation().equalsIgnoreCase("7")){
                if (side.value().equalsIgnoreCase("white")){
                    total = total + 20;
                } else {
                    total = total - 20;
                }
            }
        }

        for (Square index : blackRook){
            if (index.getRank().getNotation().equalsIgnoreCase("2")){
                if (side.value().equalsIgnoreCase("black")){
                    total = total + 20;
                } else {
                    total = total - 20;
                }
            }
        }
        return total;
    }

    private int sameFile(Board board){
        int total = 0;
        List<String> doubleFile = new ArrayList<String>();
        List<String> seenSquares = new ArrayList<String>();
        List<Square> blackPawns = board.getPieceLocation(Piece.BLACK_PAWN);
        List<Square> whitePawns = board.getPieceLocation(Piece.WHITE_PAWN);

        for (Square index : blackPawns){
            if (seenSquares.contains(index.getFile().value())){
                if (!doubleFile.contains(index.getFile().value())){
                    doubleFile.add(index.getFile().value());
                    if (side.value().equalsIgnoreCase("black")){
                        total = total - 7;
                    } else {
                        total = total + 7;
                    }

                }
            } else {
                seenSquares.add(index.getFile().value());
            }
        }

        for (Square index : whitePawns){
            if (seenSquares.contains(index.getFile().value())){
                if (!doubleFile.contains(index.getFile().value())){
                    doubleFile.add(index.getFile().value());
                    if (side.value().equalsIgnoreCase("white")){
                        total = total - 7;
                    } else {
                        total = total + 7;
                    }

                }
            } else {
                seenSquares.add(index.getFile().value());
            }
        }

        return total;
    }




}
