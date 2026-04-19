package com.aven.chessbot.api;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aven.chessbot.service.ChessEngineService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChessController.class)
class ChessControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private ChessEngineService chessEngineService;

  @Test
  void healthEndpointReturnsOk() throws Exception {
    mockMvc
        .perform(get("/api/chess/health"))
        .andExpect(status().isOk())
        .andExpect(content().string("ok"));
  }

  @Test
  void bestMoveEndpointReturnsEngineResponse() throws Exception {
    Mockito.when(chessEngineService.findBestMove(any()))
        .thenReturn(
            new BestMoveResponse(
                "e2e4",
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                "WHITE"));

    mockMvc
        .perform(
            post("/api/chess/best-move")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"moves\":[\"e2e4\"]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bestMove").value("e2e4"))
        .andExpect(jsonPath("$.sideToMove").value("WHITE"));
  }
}
