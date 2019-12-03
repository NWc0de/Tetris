/*
 * Spencer Little
 * T CSS - 305 Autumn 2019
 * Assignment 5 - Tetris
 */

package view;

import java.awt.Graphics;

import javax.swing.JComponent;

import model.Block;
import model.Point;
import model.TetrisPiece;

/**
 * A custom component that displays a single tetris piece.
 * @author Spencer
 * @version 1.0.0
 */
public class NextPiecePanel extends TetrisComponent {

    /** The pixel dimension of one grid unit for this component. */
    private static final int GRID_UNIT = 45;
    
    /** The current TetrisPiece to display. */
    private TetrisPiece myCurrentPiece;
    
    /**
     * Initializes the panel with no tetris piece.
     */
    public NextPiecePanel() {
        super(4, 4, GRID_UNIT);
        myBoardModel = new Block[4][4];
    }
    
    /**
     * Initializes the panel with the specified object.
     * @param thePiece the piece to display
     */
    public NextPiecePanel(final TetrisPiece thePiece) {
        super(4, 4, GRID_UNIT);
        myBoardModel = new Block[4][4];
        setPiece(thePiece);
        composeBlockModel();
    }
    
    /**
     * Updates the Panel with the specified piece.
     * @param thePiece the piece to display
     */
    public void setPiece(final TetrisPiece thePiece) {
        if (thePiece == null) {
            throw new IllegalArgumentException("Null piece object passed to preview panel.");
        }
        myCurrentPiece = thePiece;
        composeBlockModel();
        repaint();
    }
    
    /**
     * Translates the current piece into a two dimensional array of 
     * Block objects to be drawn by super's paintComponent.
     */
    private void composeBlockModel() {
        myBoardModel = new Block[4][4];
        for (Point p : myCurrentPiece.getPoints()) {
            myBoardModel[p.y()][p.x()] = myCurrentPiece.getBlock();
        }
    }
    
    
}
