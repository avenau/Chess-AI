package com.aven.chessbot.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

/** Interface for any Bots */
public class Solution {

  public int solution(String S, String T) {
    if (S.length() != T.length()) return -1;

    char[] sourceCharArray = S.toCharArray();
    char[] targetCharArray = T.toCharArray();
    int lengthOfString = S.length();
    int moves = 0;


    for (int i = 0; i < lengthOfString - 1; i++) {
      while (sourceCharArray[i] != targetCharArray[i]) {
        if (sourceCharArray[i + 1] != targetCharArray[i + 1]) {
          sourceCharArray[i] = (char) increaseCharacterByOne(sourceCharArray[i]);
          sourceCharArray[i + 1] = (char)increaseCharacterByOne(sourceCharArray[i+1]);
          moves++;
        } else {
          return -1;
        }
      }
    }

    if (sourceCharArray[lengthOfString - 1] != targetCharArray[lengthOfString - 1]) return -1;

    return moves;
  }

  private int increaseCharacterByOne(char sourceCharArray) {
    return (sourceCharArray - '0' + 1) % 10 + '0';
  }
}
