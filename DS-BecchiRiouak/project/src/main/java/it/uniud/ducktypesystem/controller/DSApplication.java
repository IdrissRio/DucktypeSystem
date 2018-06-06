package it.uniud.ducktypesystem.controller;

import it.uniud.ducktypesystem.view.DSView;

public class DSApplication  implements Runnable{
    protected DSView view;

    @Override
    public void run() {
        view=new DSView(this);
        view.openApplication();
    }
    public void exit() {
        System.exit(0);
    }
}
