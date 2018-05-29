package it.uniud.ducktypesystem.controller.logger;

import javax.swing.*;
import java.awt.*;

public interface DSAbstractLog {
    void log(String logMessage, Color col);
    JTextPane getLog();
}