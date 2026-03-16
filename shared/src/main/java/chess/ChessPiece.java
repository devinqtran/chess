package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    // Stores piece's color W/B and it's type
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    // Constructor

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChessPiece{");
        sb.append("pieceColor=").append(pieceColor);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }

    /**
     * The various different chess piece options
     */
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
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        // Creates an empty list to store valid moves
        Collection<ChessMove> moves = new ArrayList<>();
        // Switch statement to change piece type
        switch (type) {
            // Calls individual piece methods to add moves for that type
            case KING: addKingMoves(board, myPosition, moves); break;
            case KNIGHT: addKnightMoves(board, myPosition, moves); break;
            case QUEEN: addQueenMoves(board, myPosition, moves); break;
            case BISHOP: addBishopMoves(board, myPosition, moves); break;
            case ROOK: addRookMoves(board, myPosition, moves); break;
            case PAWN: addPawnMoves(board, myPosition, moves); break;
        }
        // Returns the collection of all valid moves
        return moves;
    }

    // Helper method to determine valid movements
    private boolean isValidPosition(int row, int col) {
        // Checks to see if a position is within the 8x8 chess board
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    // Helper method to check which piece is at the target position
    private boolean canMoveto(ChessBoard board, ChessPosition position) {
        // Returns true if the position is empty (null) or contains the opposite color piece
        ChessPiece pieceAtPosition = board.getPiece(position);
        return pieceAtPosition == null || pieceAtPosition.getTeamColor() != this.pieceColor;
    }

    // Helper method to add promotion moves
    private void addPromotionMoves(ChessPosition myPosition, ChessPosition targetPosition, Collection<ChessMove> moves) {
        moves.add(new ChessMove(myPosition, targetPosition, PieceType.QUEEN));
        moves.add(new ChessMove(myPosition, targetPosition, PieceType.ROOK));
        moves.add(new ChessMove(myPosition, targetPosition, PieceType.BISHOP));
        moves.add(new ChessMove(myPosition, targetPosition, PieceType.KNIGHT));
    }

    private void addSlidingMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, int rowDir, int colDir) {
        int currentRow = myPosition.getRow() + rowDir;
        int currentCol = myPosition.getColumn() + colDir;

        // Uses isValidPosition to create a loop while still on the board
        while (isValidPosition(currentRow,currentCol)) {
            // Establishes the new board position using currentRow and currentCol
            ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
            ChessPiece pieceAtPosition = board.getPiece(newPosition);

            if (pieceAtPosition == null) {
                // Position is empty, move and continue
                moves.add(new ChessMove(myPosition, newPosition, null));
            } else if (pieceAtPosition.getTeamColor() != this.pieceColor) {
                // If piece in position is opposite color, add move and stop
                moves.add(new ChessMove(myPosition, newPosition, null));
                break;
            } else {
                // Piece in position is your color, stop
                break;
            }

            // Continue moving to next square in same direction
            currentRow += rowDir;
            currentCol += colDir;
        }
    }
    // Helper method to fix duplicated code blocks
    private void addMovesFromOffsets(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, int[][] offsets) {
        for (int[] offset : offsets) {
            int newRow = myPosition.getRow() + offset[0];
            int newColumn = myPosition.getColumn() + offset[1];

            if (isValidPosition(newRow, newColumn)) {
                ChessPosition newPosition = new ChessPosition(newRow, newColumn);
                if (canMoveto(board, newPosition)) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
    }
    private void addKingMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] kingOffsets = {
                {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}
        };
        addMovesFromOffsets(board, myPosition, moves, kingOffsets);
    }
    private void addKnightMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] knightOffsets = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
        addMovesFromOffsets(board, myPosition, moves, knightOffsets);
    }
    private void addBishopMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        addSlidingMoves(board, myPosition, moves, 1, 1);
        addSlidingMoves(board, myPosition, moves, 1, -1);
        addSlidingMoves(board, myPosition, moves, -1, 1);
        addSlidingMoves(board, myPosition, moves, -1, -1);
    }
    private void addRookMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        addSlidingMoves(board, myPosition, moves, 1, 0);
        addSlidingMoves(board, myPosition, moves, 0, 1);
        addSlidingMoves(board, myPosition, moves, 0, -1);
        addSlidingMoves(board, myPosition, moves, -1, 0);
    }
    private void addQueenMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        addRookMoves(board, myPosition, moves);
        addBishopMoves(board, myPosition, moves);
    }
    private void addPawnMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        // Set direction, starting row, and promotion row for each color pawn
        int direction = (this.pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (this.pieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promotionRow = (this.pieceColor == ChessGame.TeamColor.WHITE) ? 8 : 1;

        int forwardRow = myPosition.getRow() + direction;
        int forwardCol = myPosition.getColumn();
        if (isValidPosition(forwardRow, forwardCol)) {
            ChessPosition forwardPosition = new ChessPosition(forwardRow, forwardCol);
            // Sets possible promotion pieces
            if (board.getPiece(forwardPosition) == null) {
                if (forwardRow == promotionRow) {
                    addPromotionMoves(myPosition, forwardPosition, moves);
                } else {
                    // Regular forward move
                    moves.add(new ChessMove(myPosition, forwardPosition, null));
                }
            }

            // Double movement for pawn
            if (myPosition.getRow() == startRow) {
                int doubleForwardRow = myPosition.getRow() + (2 * direction);
                ChessPosition doubleForwardPosition = new ChessPosition(doubleForwardRow, forwardCol);
                // Check two forward positions are empty
                if (board.getPiece(forwardPosition) == null && board.getPiece(doubleForwardPosition) == null) {
                    moves.add(new ChessMove(myPosition, doubleForwardPosition, null));
                }
            }
        }
        // Diagonal capture
        int[] captureColumns = {myPosition.getColumn() - 1, myPosition.getColumn() + 1};

        for (int captureCol : captureColumns) {
            if (isValidPosition(forwardRow, captureCol)) {
                ChessPosition capturePosition = new ChessPosition(forwardRow, captureCol);
                ChessPiece pieceAtCapture = board.getPiece(capturePosition);

                // Check the color of the piece in the position
                if (pieceAtCapture != null && pieceAtCapture.getTeamColor() != this.pieceColor) {
                    // Check to see if promotion is possible
                    if (forwardRow == promotionRow) {
                        addPromotionMoves(myPosition, capturePosition, moves);
                    } else {
                        moves.add(new ChessMove(myPosition, capturePosition, null));
                    }
                }
            }
        }
    }
}
