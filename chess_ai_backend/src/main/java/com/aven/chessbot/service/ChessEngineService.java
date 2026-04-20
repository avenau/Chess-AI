package com.aven.chessbot.service;

import com.aven.chessbot.api.BestMoveRequest;
import com.aven.chessbot.api.BestMoveResponse;
import com.aven.chessbot.bot.ChessBot;
import com.aven.chessbot.bot.MinimaxPruning;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChessEngineService {
    public BestMoveResponse findBestMove(BestMoveRequest request) throws InterruptedException {
        Board board = new Board();
        board.loadFromFen(request.getFen());
        ChessBot bot = new MinimaxPruning(board.getSideToMove());
        
        try {
            Move bestMove = bot.calculateNextMove(board);
            if (bestMove == null) {
                return new BestMoveResponse("Internal Error");
            }
            return new BestMoveResponse(bestMove.getFrom().name().toLowerCase(), bestMove.getTo().name().toLowerCase(), bestMove.getPromotion().name());
        } catch (MoveGeneratorException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Unable to generate legal moves for the supplied position.", e);
        }
    }

}
