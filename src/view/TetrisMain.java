/*
 * Spencer Little
 * T CSS - 305 Autumn 2019
 * Assignment 5 - Tetris
 */


package view;

import java.awt.EventQueue;

/**
 * Runs the Tetris program.
 * @author Spencer
 * @version 1.0.0
 */
public class TetrisMain {

    /**
     * Creates the main GUI frame and begins the game.
     * @param theArgs, cli args which are ignored
     */
    public static void main(final String[] theArgs) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TetrisGUI();
            }
        });
    }
}
