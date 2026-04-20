package com.aven.chessbot.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NextMoveDto {
    String target;
    String from;
    String promotion;
    public NextMoveDto(String from, String target) {
        this.target = target;
        this.from = from;
    }
}
