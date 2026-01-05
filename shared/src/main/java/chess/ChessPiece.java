package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    // Fields
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    // Constructor
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    // Enumerator
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    // Getters
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    // Overrides
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return pieceColor + " " + type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        // Switch
//        switch (type) {
//            case KING:
//                addKingMoves(board, myPosition, moves);
//                break;
//            case QUEEN:
//                addQueenMoves(board, myPosition, moves);
//                break;
//            case BISHOP:
//                addBishopMoves(board, myPosition, moves);
//                break;
//            case KNIGHT:
//                addKnightMoves(board, myPosition, moves);
//                break;
//            case ROOK:
//                addRookMoves(board, myPosition, moves);
//                break;
//            case PAWN:
//                addPawnMoves(board, myPosition, moves);
//                break;
//        }
        return moves;
    }
    // Piece Moves

    // Knight
    private void addKnightMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] knightOffsets = {
                {2, 1},   // 2 up, 1 right
                {2, -1},  // 2 up, 1 left
                {-2, 1},  // 2 down, 1 right
                {-2, -1}, // 2 down, 1 left
                {1, 2},   // 1 up, 2 right
                {1, -2},  // 1 up, 2 left
                {-1, 2},  // 1 down, 2 right
                {-1, -2}  // 1 down, 2 left
        };

        for (int[] offset : knightOffsets) {
            int newRow = myPosition.getRow() + offset[0];
            int newColumn = myPosition.getColumn() + offset[1];

            // Check if new position is valid
            // Check if a move to new position is possible
        }
    }
}
