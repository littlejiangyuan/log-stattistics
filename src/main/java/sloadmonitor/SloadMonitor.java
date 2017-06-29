package sloadmonitor;

import load.FileLoad;


public class SloadMonitor {



    public static void main(String[] args) {

        FileLoad sload = new FileLoad();
        sload.load(true);

        try {
            sload.process();
        } catch(Exception e) {

        }









    }


}

