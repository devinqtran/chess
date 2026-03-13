package client;

import chess.*;
import ui.EscapeSequences;

public class BoardRenderer {

    private static final String[] COLUMN_LABELS = {"a", "b", "c", "d", "e", "f", "g", "h"};

    public static void render(boolean whitePerspective) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        System.out.println();
        printBoard(board, whitePerspective);
        System.out.println();
    }

    private static void printBoard(ChessBoard board, boolean whitePerspective) {
        printColumnLabels(whitePerspective);

        for (int row = 0; row < 8; row++) {
            int chessRow = whitePerspective ? (8 - row) : (row + 1);

            // Left border
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY +
                    EscapeSequences.SET_TEXT_COLOR_WHITE +
                    " " + chessRow + " " +
                    EscapeSequences.RESET_BG_COLOR +
                    EscapeSequences.RESET_TEXT_COLOR);

            // Empty squares for now
            for (int col = 0; col < 8; col++) {
                boolean isLightSquare = (chessRow + (whitePerspective ? col + 1 : 8 - col)) % 2 == 0;
                String bg = isLightSquare
                        ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY
                        : EscapeSequences.SET_BG_COLOR_BLUE;
                System.out.print(bg + EscapeSequences.EMPTY + EscapeSequences.RESET_BG_COLOR);
            }

            // Right border
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY +
                    EscapeSequences.SET_TEXT_COLOR_WHITE +
                    " " + chessRow + " " +
                    EscapeSequences.RESET_BG_COLOR +
                    EscapeSequences.RESET_TEXT_COLOR);
            System.out.println();
        }

        printColumnLabels(whitePerspective);
        System.out.println("Board not yet implemented!");
    }

    // Method for printing the column labels based on team color
    private static void printColumnLabels(boolean whitePerspective) {
        System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + EscapeSequences.SET_TEXT_BOLD + EscapeSequences.SET_TEXT_COLOR_WHITE + "   ");
        for (int col = 0; col < 8; col++) {
            int chessColumn = whitePerspective ? (col + 1) : (8 - col);
            System.out.print(" " + COLUMN_LABELS[chessColumn - 1] + " ");
        }
        System.out.print("   " + EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_BOLD_FAINT + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();
    }
}
