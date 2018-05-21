package it.uniud.ducktypesystem.view;

import it.uniud.ducktypesystem.distributed.data.DSQuery;

import javax.swing.*;

public interface DSAbstractView {
    public void openApplication();
    public void exit();
    public void showInformationMessage(String s);
    public void showErrorMessage(String s);
    public JFrame getMainFrame();

    void updateRobotsPosition();

    void updateQuery(String version, DSQuery.QueryStatus status);
}
