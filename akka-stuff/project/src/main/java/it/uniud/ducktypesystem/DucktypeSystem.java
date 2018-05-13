package it.uniud.ducktypesystem;

import it.uniud.ducktypesystem.controller.Application;

public class DucktypeSystem {
    public static void main(String[] args){
        new Application().run();
    }
    public void exit() {
        System.exit(0);
    }
}
