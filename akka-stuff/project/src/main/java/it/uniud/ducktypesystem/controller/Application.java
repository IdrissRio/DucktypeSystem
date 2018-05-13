package it.uniud.ducktypesystem.controller;

import it.uniud.ducktypesystem.view.sView;

public class Application  implements Runnable{
    protected sView view;

    @Override
    public void run() {
        view=new sView(this);
        view.openApplication();
    }
    public void exit() {
        System.exit(0);
    }
}
