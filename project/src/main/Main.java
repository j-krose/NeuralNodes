package main;

import bugs.BugController;
import display.MainWindow;
import utils.ConcurrentTimers;
import utils.Sizes;

public class Main
{

    public static void main(String[] args) throws Exception
    {
        Sizes.initialize();
        ConcurrentTimers.initialize();
        BugController bugController = new BugController();
        new MainWindow(bugController);
    }

}
