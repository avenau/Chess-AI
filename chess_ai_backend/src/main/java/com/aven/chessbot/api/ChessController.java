package com.aven.chessbot.api;

import com.aven.chessbot.service.ChessEngineService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/chess")
public class ChessController {
  private final ChessEngineService chessEngineService;

  public ChessController(ChessEngineService chessEngineService) {
    this.chessEngineService = chessEngineService;
  }

  @GetMapping("/health")
  public String health() {
    return "ok";
  }

  @PostMapping("/best-move")
  public BestMoveResponse bestMove(@RequestBody(required = false) BestMoveRequest request)
      throws InterruptedException {
    BestMoveRequest safeRequest = request == null ? new BestMoveRequest() : request;
    return chessEngineService.findBestMove(safeRequest);
  }
}
