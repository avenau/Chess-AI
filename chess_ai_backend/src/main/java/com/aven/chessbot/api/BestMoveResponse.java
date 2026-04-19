package com.aven.chessbot.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BestMoveResponse {
  public BestMoveResponse (String targetPosition, String promotion) {
    if (promotion == null){
      this.nextMove = new NextMoveDto(targetPosition);
    } else {
      this.nextMove = new NextMoveDto(targetPosition, promotion);

    }
  }
  public BestMoveResponse (String error) {
    this.error = error;
  }
  private NextMoveDto nextMove;
  private String error;
}
