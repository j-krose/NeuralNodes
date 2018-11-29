package main;

import bugs.BugController;
import display.MainWindow;

public class Main {

    public static void main(String[] args) throws Exception
    {
        BugController bugController = new BugController();
        new MainWindow(bugController);
	}

}
