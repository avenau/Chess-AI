package com.aven.chessbot.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NextMoveDto {
    String target;
    String promotion;
    public NextMoveDto(String target) {
        this.target = target;
    }
}
