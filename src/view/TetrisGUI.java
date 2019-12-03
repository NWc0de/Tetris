/*
 * Spencer Little
 * T CSS - 305 Autumn 2019
 * Assignment 5 - Tetris
 */

package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.Timer;

import model.Block;
import model.Board;
import model.TetrisPiece;

/**
 * The main GUI class, manages flow and presents UI.
 * @author Spencer
 * @version 1.0.0
 */
public class TetrisGUI implements Observer {
    
    /** The path to the block movement audio file. */
    private final String myMoveAF = "res/audio/shift_sound.wav";
    
    /** The path to the block rotation audio file. */
    private final String myRotateAF = "res/audio/rotate_sound.wav";
    
    /** The path to the block drop audio file. */
    private final String myDropAF = "res/audio/drop_sound.wav";
    
    /** The path to the row clear audio file. */
    private final String myRowClearAF = "res/audio/rclear_sound.wav";
    
    /** The path to the end game audio file. */
    private final String myEndGameAF = "res/audio/endgame_sound.wav";
    
    /** The main GUI component that encapsulates all subsidiary components. */
    private final JFrame myMainFrame = new JFrame("UW CSS 305 - Tetris");
    
    /** The custom Graphics panel - draws elements based on Board object. */
    private final TetrisComponent myGraphicPanel = new TetrisComponent(getPreferredDim());
    
    /** The start button - begin auto-step game play. */
    private final JButton myStartButton = new JButton("      Start      ");
    
    /** The stop button - stops auto-step game play. */
    private final JButton myStopButton = new JButton("      Stop      ");
    
    /** The step button - makes current piece move on step. */
    private final JButton myStepButton = new JButton("      Step      ");
    
    /** The pause button - pauses auto-step game play. */
    private final JButton myPauseButton = new JButton("     Pause     ");
    
    /** The about button - displays rules and controls. */
    private final JButton myAboutButton = new JButton("     About    ");
    
    /** The check button to toggle auto-step mode. */
    private final JRadioButton myAutoStepBox = new JRadioButton("Auto Step", true);
    
    /** The check button to toggle audio output. */
    private final JRadioButton myAudioBox = new JRadioButton("Audio", true);
    
    /** The timer to orchestrate auto stepping. */
    private final Timer myAutoStepTimer = new Timer(500, new ButtonListener());
    
    /** Label to display user score data. */
    private JLabel myScoreLabel = new JLabel();

    /** The Panel that displays a preview of the next Tetris piece. */
    private final NextPiecePanel myNextPiecePnl = new NextPiecePanel();
    
    /** A list containing all buttons. */
    private final List<JButton> myAllButtons =  new ArrayList<JButton>();
    
    /** The users current score. */
    private int myUserScore;
    
    /** The Board, model containing data about user game session. */
    private final Board myUserBoard;

    /** A flag indicating whether a game is currently occuring. */
    private boolean myIsGameOn;
    
    /** A flag indicating whether the current mode is step or auto-step. */
    private boolean myIsAutoStep = true;
    
    /** A flag indicating whether sound is enabled or not. */
    private boolean myIsSoundEnabled = true;

    
    /**
     * Initializes GUI components, begins game. 
     */
    public TetrisGUI() {
        myMainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myMainFrame.setPreferredSize(getPreferredDim());
        myMainFrame.setResizable(false);
        
        myUserBoard = new Board();
        myUserBoard.addObserver(this);
       
        myMainFrame.add(buildControlPanel(), BorderLayout.CENTER);
        myMainFrame.add(myGraphicPanel, BorderLayout.EAST);
        
        addKeyListeners();
        myMainFrame.pack();
        myMainFrame.setVisible(true);
    }
    

    /**
     * Observes the Board model data, sends updated data to 
     * Tetris component to animate GUI.
     * @param theObs the observable object
     * @param theArg the optional data passed by the observable 
     */
    @Override
    public void update(final Observable theObs, final Object theArg) {
        if (theArg.getClass().isArray()) {
            Object[] check = (Object[]) theArg;
            if (check[0] instanceof Integer) {
                Integer[] rows = (Integer[]) theArg;
                myGraphicPanel.updatePanel(rows);
                playSoundAt(myRowClearAF);
                updateScore(rows.length);
            } else {
                Block[][] boardData = (Block[][]) theArg;
                myGraphicPanel.updatePanel(boardData);
            }
        } else if (theArg.getClass() == TetrisPiece.class) {
            TetrisPiece piece = (TetrisPiece) theArg;
            myNextPiecePnl.setPiece(piece);
        } else if (theArg.getClass() == Boolean.class) {
            gameOver();
        }
    }
    
    /**************************************************************
     *                      GUI Initialization                    * 
     **************************************************************/
    
    /**
     * Builds the panel that displays controls and user data.
     */
    private JPanel buildControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        
        JPanel nextPieceContainer = new JPanel();
        nextPieceContainer.add(myNextPiecePnl, BorderLayout.CENTER);
        controlPanel.add(nextPieceContainer);
        
        JPanel buttonPanel = new JPanel();
        JPanel buttonWrapper = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        addButtonsToList();
        addButtonListeners();
        packButtons(buttonPanel);
        buttonWrapper.add(buttonPanel, BorderLayout.CENTER);
        controlPanel.add(buttonWrapper);

        JPanel theScorePanel = new JPanel();
        myScoreLabel = new JLabel("Current Score: " + myUserScore);
        myScoreLabel.setFont(new Font("Score", Font.BOLD, 17));
        theScorePanel.add(myScoreLabel);
        controlPanel.add(theScorePanel);
        
        return controlPanel;
    }
    
    /**
     * Packs all of the buttons into the specified panel.
     */
    private void packButtons(final JPanel thePanel) {
        styleButtons();

        for (JButton b : myAllButtons) {
            thePanel.add(b);
            thePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        thePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        thePanel.add(myAutoStepBox);
        thePanel.add(myAudioBox);
    }
    
    /**
     * Sets the buttons color, style, and dimensions
     */
    private void styleButtons() {
        final int stdHeight = 26;
        for (JButton b : myAllButtons) {
            b.setForeground(new Color(2, 79, 105));
            b.setFocusPainted(false);
        }
    }
    
    /**
     * Adds an instance of ButtonListener to each of the control
     * buttons.
     */
    private void addButtonListeners() {
        ButtonListener theListener = new ButtonListener();
        for (JButton b : myAllButtons) {
            b.addActionListener(theListener);
        }
        myAutoStepBox.addActionListener(theListener);
        myAudioBox.addActionListener(theListener);
    }
    
    /**
     * Adds the custom KeyListener for any object that may gain
     * focus.
     */
    private void addKeyListeners() {
        for (JButton b : myAllButtons) {
            b.setFocusable(false); // buttons can interfere with listener
        }
        myAutoStepBox.setFocusable(false);
        myAudioBox.setFocusable(false);
        
        KeyPressListener theListener = new KeyPressListener();
        myMainFrame.setFocusable(true);
        myMainFrame.addKeyListener(theListener);
    }
    
    /**
     * Adds all buttons to the button list.
     */
    private void addButtonsToList() {
        myAllButtons.add(myStartButton);
        myAllButtons.add(myPauseButton);
        myAllButtons.add(myStopButton);
        myAllButtons.add(myStepButton);
        myAllButtons.add(myAboutButton);
    }
    
    
    /**
     * Gets the preferred dimension based on total screen size.
     */
    private Dimension getPreferredDim() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int prefWidth = (int) (screenSize.getWidth() * .50);
        int prefHeight = (int) (screenSize.getHeight() * .90);
        return new Dimension(prefWidth, prefHeight);
    }
    
    
    /*************************************************************
     *                Game Logic + Sequencing                    *
     *************************************************************/
    
    /**
     * Resets the state of the Board, generates a new random
     * TetrisPiece sequence.
     */
    private void initGame() {
        myUserScore = 0;
        myScoreLabel.setText("Current Score: " + myUserScore);
        myGraphicPanel.newGame();
        myUserBoard.setPieceSequence(genPieceSequence(100));
        myUserBoard.newGame(); 
        myIsGameOn = true;
        if (myIsAutoStep)
            myAutoStepTimer.start();
    }
    
    /**
     * Displays a message to the user when the game is over. 
     */
    private void gameOver() {
        playSoundAt(myEndGameAF);
        if (myIsAutoStep)
            myAutoStepTimer.stop();
        myIsGameOn = false;
        myGraphicPanel.setEndGameScore(myUserScore);
    }
    
    /**
     * Displays a help message describing user controls.
     */
    private void displayHelpDialog() {
        String helpMessage = buildHelpString();
        JOptionPane.showMessageDialog(myMainFrame, helpMessage, "User Controls",
                                      JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Displays a dialog when the user pauses the game.
     */
    private void displayPauseDialog() {
        Object[] options = {"Resume", "Quit"};
        int rsp = JOptionPane.showOptionDialog(myGraphicPanel,
                                     "Current score: " + myUserScore, 
                                     "Game Paused", 
                                     JOptionPane.YES_NO_CANCEL_OPTION, 
                                     JOptionPane.INFORMATION_MESSAGE, 
                                     null, 
                                     options, 
                                     options[0]);
        
        if (rsp == 1) {
            gameOver();
        } else if (myIsAutoStep) {
            myAutoStepTimer.start();
        }
    }
    
    /**
     * Builds the help message for the help dialog. Returns a 
     * string describing user controls.
     */
    private String buildHelpString() {
        StringBuilder theBuild = new StringBuilder();
        theBuild.append("Shift peice left: a or A or (left arrow key)\n");
        theBuild.append("Shift piece right: d or D or (right arrow key)\n");
        theBuild.append("Step piece down: s or S or (down arrow key)\n");
        theBuild.append("Rotate piece: w or W or (up arrow key\n");
        theBuild.append("Drop piece: (space bar)\n");
        theBuild.append("Pause game: p or P\n");
        return theBuild.toString();
    }
    
    /**
     * Generates a random sequence of theCount pieces
     * @param theCount the number of pieces to Generate
     * @return thePieces an ArrayList of theCount random TetrisPieces
     */
    private List<TetrisPiece> genPieceSequence(final int theCount) {
        List<TetrisPiece> pieces = new ArrayList<TetrisPiece>();
        for (int i = 0; i < theCount; i++) {
            pieces.add(TetrisPiece.getRandomPiece());
        }
        return pieces;
    }
    
    /**
     * Update the score in the event of a line clear based on
     * OG tetris rules.
     * @param theClear number of lines that were cleared
     */
    private void updateScore(final int theClear) {
        switch (theClear) { 
            case 1:
                myUserScore += 40;
                break;
            case 2:
                myUserScore += 100;
                break;
            case 3:
                myUserScore += 300;
                break;
            case 4:
                myUserScore += 1200;
                break;
        }
        myScoreLabel.setText("Current Score: " + myUserScore);
    }
    
    /*************************************************************
     *                           Sounds                          *
     *************************************************************/
    
    /**
     * Plays a sound from the audio file stored at theAudioFile.
     * @param theAudioFile path to the audio resource as a string
     */
    private void playSoundAt(final String theAudioFile) {
        File soundFile = new File(theAudioFile);
        try {
            Clip clip = AudioSystem.getClip();
            AudioInputStream ain = AudioSystem.getAudioInputStream(soundFile);
            clip.open(ain);
            if (myIsSoundEnabled)
                clip.start();
        } catch (LineUnavailableException lux) {
            System.out.println("Error getting clip.");
            lux.printStackTrace();
        } catch (UnsupportedAudioFileException uafx) {
            System.out.println("Unsupported audio file. Is type .wav?");
            uafx.printStackTrace();
        } catch (IOException iox) {
            System.out.println("Error accessing resource.");
            iox.printStackTrace();
        }
    }
    
    /*************************************************************
     *                Listeners + Event Management               *
     *************************************************************/
    
    /**
     * A class to handle button events for the main GUI.
     * @author Spencer
     * @version 1.0.0
     */
    private final class ButtonListener implements ActionListener {

        /**
         * Processes button clicks for the main GUI.
         * @param theEvent the Event object to process
         */
        @Override
        public void actionPerformed(final ActionEvent theEvent) {
            if ((theEvent.getSource() == myStepButton && myIsGameOn) ||
                            theEvent.getSource() == myAutoStepTimer) {
                myUserBoard.step();
                // sound here ? 
            } else if (theEvent.getSource() == myStartButton && !myIsGameOn) {
                initGame();
            } else if (theEvent.getSource() == myStopButton && myIsGameOn) {
                gameOver();
            } else if (theEvent.getSource() == myAutoStepBox && myIsGameOn) {
                // disabled switching during game 
                myAutoStepBox.setSelected(myIsAutoStep);  
            } else if (theEvent.getSource() == myAutoStepBox) {
                myIsAutoStep = !myIsAutoStep;
            } else if (theEvent.getSource() == myAboutButton) {
                if (myIsAutoStep && myIsGameOn) myAutoStepTimer.stop();
                displayHelpDialog();
                if (myIsAutoStep && myIsGameOn) myAutoStepTimer.start();
            } else if (theEvent.getSource() == myAudioBox) {
                myIsSoundEnabled = !myIsSoundEnabled;
            } else if (theEvent.getSource() == myPauseButton && myIsGameOn) {
                if (myIsAutoStep) myAutoStepTimer.stop();
                displayPauseDialog();
            }
            
        }
        
    }
    
    /**
     * A class to handle keyboard events for the main GUI.
     * @author Spencer
     * @version 1.0.0
     */
    private final class KeyPressListener extends KeyAdapter {

        /**
         * Processes key event data.
         * @param theEvent the key event
         */
        @Override
        public void keyPressed(final KeyEvent theEvent) {
            if (myIsGameOn == false) return; // protect against null ptr

            char theKey = theEvent.getKeyChar();
            int theCode = theEvent.getKeyCode();
            boolean left = theKey == 'a' || theKey == 'A' 
                            || theCode == KeyEvent.VK_LEFT;
            boolean right = theKey == 'd' || theKey == 'D' 
                            || theCode == KeyEvent.VK_RIGHT;
            boolean up = theKey == 'w' || theKey == 'W' 
                            || theCode == KeyEvent.VK_UP;
            boolean down = theKey == 's' || theKey == 'S' 
                            || theCode == KeyEvent.VK_DOWN;
            boolean drop = theCode == KeyEvent.VK_SPACE;
            boolean pause = theKey == 'p' || theKey == 'P';
            
            if (right) {
                myUserBoard.right();
                playSoundAt(myMoveAF);
            } else if (left) {
                myUserBoard.left();
                playSoundAt(myMoveAF);
            } else if (up) {
                myUserBoard.rotateCW();
                playSoundAt(myRotateAF);
            } else if (down) {
                myUserBoard.down();
                playSoundAt(myMoveAF);
            } else if (drop) {
                myUserBoard.drop();
                playSoundAt(myDropAF);
            } else if (pause) {
                if (myIsAutoStep) myAutoStepTimer.stop();
                displayPauseDialog();
            }
        }

        
    }

}
