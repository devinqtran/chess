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
        switch (type) {
            case KING:
                addKingMoves(board, myPosition, moves);
                break;
            case QUEEN:
                addQueenMoves(board, myPosition, moves);
                break;
            case BISHOP:
                addBishopMoves(board, myPosition, moves);
                break;
            case KNIGHT:
                addKnightMoves(board, myPosition, moves);
                break;
            case ROOK:
                addRookMoves(board, myPosition, moves);
                break;
            case PAWN:
                addPawnMoves(board, myPosition, moves);
                break;
        }
        return moves;
    }
    // Valid position method
    private boolean isValidPosition(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    // Method to determine if the position is empty or has an enemy piece
    private boolean canMoveto(ChessBoard board, ChessPosition position) {
        ChessPiece pieceAtPosition = board.getPiece(position);
        return pieceAtPosition == null || pieceAtPosition.getTeamColor() != this.pieceColor;
    }

    private void addSlidingMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, int rowDir, int colDir) {
        int currentRow = myPosition.getRow() + rowDir;
        int currentCol = myPosition.getColumn() + colDir;

        while (isValidPosition(currentRow, currentCol)) {
            ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
            ChessPiece pieceAtPosition = board.getPiece(newPosition);

            if (pieceAtPosition == null) {
                // Empty square - can move here and continue
                moves.add(new ChessMove(myPosition, newPosition, null));
            } else if (pieceAtPosition.getTeamColor() != this.pieceColor) {
                // Enemy piece - can capture but must stop
                moves.add(new ChessMove(myPosition, newPosition, null));
                break;
            } else {
                // Friendly piece - blocked, stop
                break;
            }

            // Move to next square
            currentRow += rowDir;
            currentCol += colDir;
        }
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

            // Check if new position is
            // Check if a move to new position is possible
            if (isValidPosition(newRow, newColumn)) {
                ChessPosition newPosition = new ChessPosition(newRow, newColumn);
                if (canMoveto(board, newPosition)) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
    }
    // King
    private void addKingMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] kingOffsets = {
                {1, 0},   // up
                {1, 1},   // up-right
                {0, 1},   // right
                {-1, 1},  // down-right
                {-1, 0},  // down
                {-1, -1}, // down-left
                {0, -1},  // left
                {1, -1}   // up-left
        };

        for (int[] offset : kingOffsets) {
            int newRow = myPosition.getRow() + offset[0];
            int newColumn = myPosition.getColumn() + offset[1];

            // Check if new position is valid
            // Check if a move to new position is possible
            if (isValidPosition(newRow, newColumn)) {
                ChessPosition newPosition = new ChessPosition(newRow, newColumn);
                if (canMoveto(board, newPosition)) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
    }
    // Queen
    private void addQueenMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        addRookMoves(board, myPosition, moves);
        addBishopMoves(board, myPosition, moves);
    }
    // Bishop
    private void addBishopMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        addSlidingMoves(board, myPosition, moves, 1, 1);    // up-right
        addSlidingMoves(board, myPosition, moves, 1, -1);   // up-left
        addSlidingMoves(board, myPosition, moves, -1, 1);   // down-right
        addSlidingMoves(board, myPosition, moves, -1, -1);  // down-left
    }
    // Rook
    private void addRookMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        addSlidingMoves(board, myPosition, moves, 1, 0);   // up
        addSlidingMoves(board, myPosition, moves, -1, 0);  // down
        addSlidingMoves(board, myPosition, moves, 0, 1);   // right
        addSlidingMoves(board, myPosition, moves, 0, -1);  // left
    }
    // Pawn
    private void addPawnMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int direction = (this.pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (this.pieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promotionRow = (this.pieceColor == ChessGame.TeamColor.WHITE) ? 8 : 1;

        // Forward one square
        int forwardRow = myPosition.getRow() + direction;
        int forwardCol = myPosition.getColumn();

        if (isValidPosition(forwardRow, forwardCol)) {
            ChessPosition forwardPosition = new ChessPosition(forwardRow, forwardCol);

            // Can only move forward if square is empty
            if (board.getPiece(forwardPosition) == null) {
                // Check for promotion
                if (forwardRow == promotionRow) {
                    moves.add(new ChessMove(myPosition, forwardPosition, PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, forwardPosition, PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, forwardPosition, PieceType.BISHOP));
                    moves.add(new ChessMove(myPosition, forwardPosition, PieceType.KNIGHT));
                } else {
                    moves.add(new ChessMove(myPosition, forwardPosition, null));
                }

                // Forward two squares
                if (myPosition.getRow() == startRow) {
                    int doubleForwardRow = myPosition.getRow() + (2 * direction);
                    ChessPosition doubleForwardPosition = new ChessPosition(doubleForwardRow, forwardCol);

                    // Both squares must be empty
                    if (board.getPiece(doubleForwardPosition) == null) {
                        moves.add(new ChessMove(myPosition, doubleForwardPosition, null));
                    }
                }
            }
        }

        // Diagonal captures
        int[] captureColumns = {myPosition.getColumn() - 1, myPosition.getColumn() + 1};

        for (int captureCol : captureColumns) {
            if (isValidPosition(forwardRow, captureCol)) {
                ChessPosition capturePosition = new ChessPosition(forwardRow, captureCol);
                ChessPiece pieceAtCapture = board.getPiece(capturePosition);

                // Can only capture diagonally if there's an enemy piece
                if (pieceAtCapture != null && pieceAtCapture.getTeamColor() != this.pieceColor) {
                    // Check for promotion
                    if (forwardRow == promotionRow) {
                        moves.add(new ChessMove(myPosition, capturePosition, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, capturePosition, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, capturePosition, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, capturePosition, PieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(myPosition, capturePosition, null));
                    }
                }
            }
        }
    }
}
