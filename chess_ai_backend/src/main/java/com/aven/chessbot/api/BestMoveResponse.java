package com.aven.chessbot.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BestMoveResponse {
    private NextMoveDto nextMove;
    private String error;

    public BestMoveResponse(String fromPosition, String targetPosition, String promotion) {
        if (promotion == null) {
            this.nextMove = new NextMoveDto(fromPosition, targetPosition);
        } else {
            this.nextMove = new NextMoveDto(fromPosition, targetPosition, promotion);
        }
    }

    public BestMoveResponse(String error) {
        this.error = error;
    }
}
