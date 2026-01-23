package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    // Fields to store board and whose turn it is
    private ChessBoard board;
    private TeamColor currentTurn;

    // Tracking for en passant and castling
    private Set<ChessPosition> piecesMoved;
    private ChessMove previousMove;

    public ChessGame() {
        // Creates a new empty board, put pieces into starting positions, set starting turn white
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.currentTurn = TeamColor.WHITE;
        this.piecesMoved = new HashSet<>();
        this.previousMove = null;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        // Extra credit castling and en passant need to implement moves for king/pawns
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            possibleMoves.addAll(getCastlingMoves(startPosition, piece.getTeamColor()));
        } else if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            possibleMoves.addAll(getEnPassantMoves(startPosition, piece.getTeamColor()));
        }

        for (ChessMove move : possibleMoves) {
            if (!dangerousMove(move, piece.getTeamColor())) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null) {
            throw new InvalidMoveException("There is no piece at the starting position");
        }
        if (piece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException("Wait for this team's turn");
        }
        Collection<ChessMove> valid = validMoves(move.getStartPosition());
        if (valid == null || !valid.contains(move)) {
            throw new InvalidMoveException("This piece cannot make this move");
        }
        // Execute the move
        executeMove(move);

        // Tracking for piecesMoved and previousMove
        piecesMoved.add(move.getEndPosition());
        previousMove = move;

        // Change turn
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        if (kingPosition == null) {
            return false;
        }
        TeamColor enemyColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == enemyColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(board, position);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return !anyValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return !anyValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    /**
     * Helper methods
     */
    private void executeMove(ChessMove move) {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        board.addPiece(move.getStartPosition(), null);
        // Pawn promotion
        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        // Castling
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            int colDiff = move.getEndPosition().getColumn() - move.getStartPosition().getColumn();
            if (Math.abs(colDiff) == 2) {
                int row = move.getStartPosition().getRow();
                if (colDiff == 2) {
                    ChessPosition rookStart = new ChessPosition(row, 8);
                    ChessPosition rookEnd = new ChessPosition(row, 6);
                    ChessPiece rook = board.getPiece(rookStart);
                    board.addPiece(rookStart, null);
                    board.addPiece(rookEnd, rook);
                } else {
                    ChessPosition rookStart = new ChessPosition(row, 1);
                    ChessPosition rookEnd = new ChessPosition(row, 4);
                    ChessPiece rook = board.getPiece(rookStart);
                    board.addPiece(rookStart, null);
                    board.addPiece(rookEnd, rook);
                }
            }
        }

        // En Passant
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int colDiff = Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn());
            if (colDiff == 1 && board.getPiece(move.getEndPosition()) == null) {
                int capturedRow = move.getStartPosition().getRow();
                int capturedCol = move.getEndPosition().getColumn();
                board.addPiece(new ChessPosition(capturedRow, capturedCol), null);
            }
        }
        board.addPiece(move.getEndPosition(), piece);
    }

    /**
     * Copy method to use copy constructor from ChessBoard.java
     * @return
     */
    private ChessBoard copyBoard() {
        return new ChessBoard(board);
    }

    /**
     *
     * @param teamColor
     * @return position of king
     */
    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param teamColor
     * @return True if there are valid moves
     */
    private boolean anyValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (moves != null && !moves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     *
     * @param move
     * @param teamColor
     * @return king is in check after move
     */
    private boolean dangerousMove(ChessMove move, TeamColor teamColor) {
        ChessBoard originalBoard = copyBoard();
        Set<ChessPosition> originalMoved = new HashSet<>(piecesMoved);
        ChessMove originalLastMove = previousMove;
        executeMove(move);
        piecesMoved.add(move.getEndPosition());
        boolean inCheck = isInCheck(teamColor);
        this.board = originalBoard;
        this.piecesMoved = originalMoved;
        this.previousMove = originalLastMove;
        return inCheck;
    }

    /**
     * Castling and En Passant methods
     */
    private Collection<ChessMove> getCastlingMoves(ChessPosition kingPos, TeamColor teamColor) {
        Collection<ChessMove> castlingMoves = new ArrayList<>();
        // Check to see if the king piece has been moved or for check
        if (piecesMoved.contains(kingPos)) {
            return castlingMoves;
        }
        if (isInCheck(teamColor)) {
            return castlingMoves;
        }
        int kingRow = kingPos.getRow();
        int kingCol = kingPos.getColumn();
        // Kingside castle
        ChessPosition kingsideRookPos = new ChessPosition(kingRow, 8);
        if (!piecesMoved.contains(kingsideRookPos)) {
            ChessPiece rook = board.getPiece(kingsideRookPos);
            if (rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK
                    && rook.getTeamColor() == teamColor) {
                boolean pathClear = true;
                for (int col = kingCol + 1; col < 8; col++) {
                    if (board.getPiece(new ChessPosition(kingRow, col)) != null) {
                        pathClear = false;
                        break;
                    }
                }
                if (pathClear) {
                    boolean safe = true;
                    for (int col = kingCol + 1; col <= kingCol + 2; col++) {
                        if (checkSquare(new ChessPosition(kingRow, col), teamColor)) {
                            safe = false;
                            break;
                        }
                    }
                    if (safe) {
                        castlingMoves.add(new ChessMove(kingPos, new ChessPosition(kingRow, kingCol + 2), null));
                    }
                }
            }
        }
        // Queenside castle
        ChessPosition queensideRookPos = new ChessPosition(kingRow, 1);
        if (!piecesMoved.contains(queensideRookPos)) {
            ChessPiece rook = board.getPiece(queensideRookPos);
            if (rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK
                    && rook.getTeamColor() == teamColor) {
                boolean pathClear = true;
                for (int col = 2; col < kingCol; col++) {
                    if (board.getPiece(new ChessPosition(kingRow, col)) != null) {
                        pathClear = false;
                        break;
                    }
                }
                if (pathClear) {
                    boolean safe = true;
                    for (int col = kingCol - 1; col >= kingCol - 2; col--) {
                        if (checkSquare(new ChessPosition(kingRow, col), teamColor)) {
                            safe = false;
                            break;
                        }
                    }
                    if (safe) {
                        castlingMoves.add(new ChessMove(kingPos, new ChessPosition(kingRow, kingCol - 2), null));
                    }
                }
            }
        }

        return castlingMoves;
    }
    private Collection<ChessMove> getEnPassantMoves(ChessPosition pawnPos, TeamColor teamColor) {
        Collection<ChessMove> enPassantMoves = new ArrayList<>();
        // Check for previous move
        if (previousMove == null) {
            return enPassantMoves;
        }
        // Check pawn movement
        ChessPiece lastPiece = board.getPiece(previousMove.getEndPosition());
        if (lastPiece == null || lastPiece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return enPassantMoves;
        }
        int rowDiff = Math.abs(previousMove.getEndPosition().getRow() - previousMove.getStartPosition().getRow());
        if (rowDiff != 2) {
            return enPassantMoves;
        }
        int ourRow = pawnPos.getRow();
        int ourCol = pawnPos.getColumn();
        int enemyRow = previousMove.getEndPosition().getRow();
        int enemyCol = previousMove.getEndPosition().getColumn();
        if (enemyRow != ourRow || Math.abs(enemyCol - ourCol) != 1) {
            return enPassantMoves;
        }
        int direction = (teamColor == TeamColor.WHITE) ? 1 : -1;
        ChessPosition capturePos = new ChessPosition(ourRow + direction, enemyCol);
        enPassantMoves.add(new ChessMove(pawnPos, capturePos, null));
        return enPassantMoves;
    }
    private boolean checkSquare(ChessPosition position, TeamColor friendlyColor) {
        TeamColor enemyColor = (friendlyColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == enemyColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(board, pos);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(position)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Overrides
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && currentTurn == chessGame.currentTurn;
    }
    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurn);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChessGame{");
        sb.append("board=").append(board);
        sb.append(", currentTurn=").append(currentTurn);
        sb.append('}');
        return sb.toString();
    }
}
