package it.uniud.ducktypesystem.logger;

import javax.swing.*;
import java.awt.*;

public interface abstractLog {
    void log(String logMessage, Color col);
    JTextPane getLog();
}