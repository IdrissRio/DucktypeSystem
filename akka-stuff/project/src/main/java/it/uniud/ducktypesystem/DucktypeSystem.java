package it.uniud.ducktypesystem;


import it.uniud.ducktypesystem.controller.DSApplication;

public class DucktypeSystem {
    public static void main(String[] args){
        new DSApplication().run();
    }
    public void exit() {
        System.exit(0);
    }
}
