/*
 * Spencer Little
 * T CSS - 305 Autumn 2019
 * Assignment 5 - Tetris
 */

package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import javax.swing.JComponent;

import model.Block;

/**
 * A custom JComponent to animate the Tetris graphics.
 * @author Spencer
 * @version 1.0.0
 */
public class TetrisComponent extends JComponent {
    
    /** The current grid data from the Board object. */
    protected Block[][] myBoardModel;
    
    /** The width of the board (in grid units). */
    private int myWidth = 10;
    
    /** The height of the board (in grid units). */
    private int myHeight = 20;
    
    /** The number of pixels in one grid unit. */
    private int myGridUnit = 30;
    
    /** The score of the game, displayed when game is over. */
    private int myEndGameScore = Integer.MAX_VALUE;
    
    /**
     * Default constructor, initializes board dimensions to
     * 10 by 20.
     */
    public TetrisComponent() {
        super();
        int pixelWidth = myGridUnit * myWidth;
        int pixelHeight = myGridUnit * myHeight;
        setPreferredSize(new Dimension(pixelWidth, pixelHeight));
    }
    
    /**
     * Constructor in which custom dimension can be specified.
     * @param theWidth the width (in grid units) of the panel
     * @param theHeight the height (in grid units) of the panel
     * @param thePixelDim the number of pixels in one grid unit
     */
    public TetrisComponent(final int theWidth, final int theHeight, 
                           final int thePixelDim) {
        super();
        myWidth = theWidth;
        myHeight = theHeight;
        myGridUnit = thePixelDim;
        int pixelWidth = myGridUnit * theWidth;
        int pixelHeight = myGridUnit * theHeight;
        setPreferredSize(new Dimension(pixelWidth, pixelHeight));
    }
    
    /**
     * Calculates optimal grid unit based on screen size.
     * @param theMainDim the dimensions of the enclosing main frame
     */
    public TetrisComponent(final Dimension theMainDim) {
        super();
        // account for two extra blocks to offset rounding 
        myGridUnit = (int) (theMainDim.height / (myHeight+2));
        int pixelWidth = myGridUnit * myWidth;
        int pixelHeight = myGridUnit * myHeight;
        setPreferredSize(new Dimension(pixelWidth, pixelHeight));
    }
    
    /**
     * Updates the Board model data, redraws the panel.
     * @param theModel the Block[][] array from the Board
     */
    public void updatePanel(final Block[][] theModel) {
        myBoardModel = deepCopy(theModel);
        repaint();
    }
    
    /**
     * Updates the Board model data by removing the 
     * complete rows and shifting other rows accordingly.
     * Assumes that theRows will not be empty.
     * @param theRows an array containing the indices of complete rows
     */
    public void updatePanel(final Integer[] theRows) {
        Arrays.sort(theRows); // sort for ease of processing
        for (int i = theRows[0]; i < myHeight; i++) {
            updateBoardRow(i);
            // update twice after first, if complete row 
            if (i != theRows[0] && arrayContains(theRows, i)) {
                updateBoardRow(i);
            }
        }
        repaint();
    }
    
    /**
     * Sets the user game score for drawing end of game
     * display.
     * @param theScore, the final score
     */
    public void setEndGameScore(final int theScore) {
        myEndGameScore = theScore;
        repaint();
    }
    
    /**
     * Reset the EOG score value to remove the end of game
     * display.
     */
    public void newGame() {
        myEndGameScore = Integer.MAX_VALUE;
        repaint();
    }
    
    /**
     * Draws the Tetris board, or the starting grapihc.
     * @param theGraphics Graphics object used to draw components
     */
    public void paintComponent(final Graphics theGraphics) {
        if (myBoardModel == null) {
            myBoardModel = new Block[myHeight][myWidth];
            drawBoardFromModel(theGraphics.create());
        } else if (myEndGameScore != Integer.MAX_VALUE) {
            drawBoardFromModel(theGraphics.create());
            drawEOGDisplay(theGraphics.create());
        } else {
            drawBoardFromModel(theGraphics.create());
        }
    }
    
    /**
     * Draws the main board using data from myBoardModel.
     * @param theGraphics
     */
    private void drawBoardFromModel(final Graphics theGraphics) {
        Graphics2D tempGraphics = (Graphics2D) theGraphics;
        drawBackground(theGraphics.create(), Color.BLACK);
        for (int i = 0; i < myWidth; i++) {
            for (int j = 0; j < myHeight; j++) {
                // needed to invert the y position bc of model 
                Rectangle2D tempRect = new Rectangle2D.Double(i*myGridUnit, (myHeight - j)*myGridUnit, 
                                                              myGridUnit-2, myGridUnit-2);
                setColorAt(tempGraphics, i, j);
                tempGraphics.draw(tempRect);
                tempGraphics.fill(tempRect);
                
            }
        }
    }
    
    /**
     * Determines the color for the grid unit at theX, theY and sets
     * theGraphics to draw with that color.
     * @param theGraphics the Graphics object used for drawing
     * @param theX the X position of this block
     * @param theY the Y position of this block
     */
    private void setColorAt(final Graphics2D theGraphics, 
                            final int theX, final int theY) {
        if (myBoardModel[theY][theX] == null) {
            theGraphics.setColor(new Color(19, 49, 59));
            return;
        }
        switch (myBoardModel[theY][theX]) {
            case EMPTY:
                theGraphics.setColor(new Color(8, 45, 166));
                break;
            case I:
                theGraphics.setColor(new Color(12, 217, 240));
                break;
            case J:
                theGraphics.setColor(new Color(12, 58, 240));
                break;
            case L:
                theGraphics.setColor(new Color(240, 171, 12));
                break;
            case O:
                theGraphics.setColor(new Color(232, 240, 12));
                break;
            case S:
                theGraphics.setColor(new Color(20, 240, 12));
                break;
            case T:
                theGraphics.setColor(new Color(202, 12, 240));
                break;
            case Z:
                theGraphics.setColor(new Color(240, 12, 12));
                break;
            default:
                theGraphics.setColor(new Color(255, 255, 255));
                break;
        }
    }
    
    /**
     * Draws a black background in the dimensions of the board.
     * @param theGraphics the Graphics object used for drawing
     * @param theColor the Color to paint the background
     */
    private void drawBackground(final Graphics theGraphics, final Color theColor) {
        Graphics2D tempGraphics = (Graphics2D) theGraphics;
        for (int i = 0; i < myWidth; i++) {
            for (int j = 0; j < myHeight; j++) {
                Rectangle2D tempRect = new Rectangle2D.Double(i*myGridUnit, (myHeight - j)*myGridUnit, 
                                                              myGridUnit, myGridUnit);
                tempGraphics.setPaint(theColor);
                tempGraphics.draw(tempRect);
                tempGraphics.fill(tempRect);
                
            }
        }
    }
    
    /**
     * Draws the end of game display.
     * @param theGraphics, the Graphics object used for drawing
     */
    private void drawEOGDisplay(final Graphics theGraphics) {
        Graphics2D tempGraphics = (Graphics2D) theGraphics;
        double startX = 2;
        double startY = 5;
        double width = myWidth - 4;
        double height = myHeight - 14;
        
        Rectangle2D centerBound = new Rectangle2D.Double(startX*myGridUnit, startY*myGridUnit,
                                                         width*myGridUnit, height*myGridUnit);
        Rectangle2D centerWrap = new Rectangle2D.Double((startX*myGridUnit)+5, (startY*myGridUnit)+5,
                                                         (width*myGridUnit)-10, (height*myGridUnit)-10);
        tempGraphics.setPaint(new Color(58, 154, 161));
        tempGraphics.draw(centerBound);
        tempGraphics.fill(centerBound);
        tempGraphics.setPaint(new Color(33, 185, 196));
        tempGraphics.draw(centerWrap);
        tempGraphics.fill(centerWrap);
        
        tempGraphics.setFont(new Font("Display", Font.BOLD, 19));
        tempGraphics.setPaint(Color.black);
        tempGraphics.drawString("Game Over!", (int)(startX*myGridUnit)+40, (int) (startY*myGridUnit)+40);
        
        tempGraphics.setFont(new Font("Info", Font.PLAIN, 12));
        tempGraphics.drawString("Total points:", (int)(startX*myGridUnit)+20, (int) (startY*myGridUnit)+80);
        
        tempGraphics.drawString(String.valueOf(myEndGameScore), (int)(startX*myGridUnit)+90, (int) (startY*myGridUnit)+80);
        String playAgain = "Press start to play again!";
        tempGraphics.drawString(playAgain, (int)(startX*myGridUnit)+20, (int) (startY*myGridUnit)+120);
    }

    /**
     * Erases the row at theRow and replaces with the 
     * row above
     * @param theRow the row to erase
     */
    private void updateBoardRow(final int theRow) {
        if (theRow+1 <= myHeight) {
            myBoardModel[theRow] = myBoardModel[theRow+1].clone();
        } else {
            myBoardModel[theRow] = genEmptyBlockRow(myWidth);
        }
    }
    
    /**
     * Generates an 'empty' Block array of size theBlocks.
     * @param theBlocks size of the empty array
     * @return theEmpty the Block array containing all EMPTY values
     */
    private Block[] genEmptyBlockRow(final int theBlocks) {
        Block[] theEmpty = new Block[theBlocks];
        for (int i = 0; i < theBlocks; i++) {
            theEmpty[i] = Block.EMPTY;
        }
        return theEmpty;
    }
    
    /**
     * Checks a primitive integer array for value theKey.
     * @param theArray the array to check
     * @param theKey the value to check for
     * @return contains, boolean indicating if array contains theKey
     */
    private boolean arrayContains(final Integer[] theArray, final int theKey) {
        boolean contains = false;
        for (int x : theArray) {
            if (x == theKey) contains = true;
        }
        return contains;
    }
    
    /**
     * Creates a deep copy of a 2D Block array.
     * Assums the input arrary is square.
     * @param theArray the array to copy
     */
    private Block[][] deepCopy(final Block[][] theArray) {
        final Block[][] copy = new Block[theArray.length][theArray[0].length];
        for (int i = 0; i < theArray.length; i++) {
            copy[i] = theArray[i].clone();
        }
        return copy;
    }
    
}
