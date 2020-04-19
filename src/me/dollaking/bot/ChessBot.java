import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

public interface ChessBot {
    Move calculateNextMove(Board board) throws MoveGeneratorException, InterruptedException;
}
