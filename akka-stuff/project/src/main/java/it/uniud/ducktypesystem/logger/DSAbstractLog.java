package it.uniud.ducktypesystem.logger;

import javax.swing.*;
import java.awt.*;

public interface DSAbstractLog {
    void log(String logMessage, Color col);
    JTextPane getLog();
}