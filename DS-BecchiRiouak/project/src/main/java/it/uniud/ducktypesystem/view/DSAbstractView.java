package it.uniud.ducktypesystem.view;

import it.uniud.ducktypesystem.distributed.data.DSQuery;

import javax.swing.*;

public interface DSAbstractView {
    void openApplication();
    void exit();
    void showInformationMessage(String s);
    void showErrorMessage(String s);
    JFrame getMainFrame();
    void refreshButton();
    void updateRobotsPosition();
    void enableButton();
    void updateQuery(DSQuery.QueryId qId, DSQuery.QueryStatus status);
}
