package view;

import model.GameState;
import presentation.ImmediateQueue;

import javax.swing.*;

public class TableData {
    private String[] names = {"players", "score"};
    private String[][] data = {};


    String[] getNames() {
        return names;
    }
    String[][] getData() {return data;}
}
