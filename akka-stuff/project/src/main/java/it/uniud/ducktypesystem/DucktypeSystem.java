package it.uniud.ducktypesystem;


import it.uniud.ducktypesystem.controller.DSApplication;

public class DucktypeSystem {

    public static void main(String[] args){
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        new DSApplication().run();
    }
    public void exit() {
        System.exit(0);
    }
}
