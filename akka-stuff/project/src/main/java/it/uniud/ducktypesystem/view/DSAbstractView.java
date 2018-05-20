package it.uniud.ducktypesystem.view;

import javax.swing.*;

public interface DSAbstractView {
    public void openApplication();
    public void exit();
    public void showInformationMessage(String s);
    public void showErrorMessage(String s);
    public JFrame getMainFrame();

    boolean askMoveAndRetry(String version);
}
