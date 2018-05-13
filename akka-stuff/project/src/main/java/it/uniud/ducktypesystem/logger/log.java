package it.uniud.ducktypesystem.logger;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class log implements abstractLog{
    private JTextPane logPane;
    public log(){
        logPane=new JTextPane();
        logPane.setEditable(false);
        logPane.setFont(new Font("Avenir", Font.PLAIN, 20));
    }
    @Override
    public void log(String logMessage, Color col) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, col);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Bariol");

        int len = logPane.getDocument().getLength();
        logPane.setCaretPosition(len);
        logPane.setCharacterAttributes(aset, false);
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss");
        logPane.setEditable(true);
        logPane.replaceSelection("<"+LocalDate.now().format(formatterDate) +" "+ LocalDateTime.now().format(formatterTime) +">: "+logMessage+"\n");
        logPane.setEditable(false);
    }
    public JTextPane getLog(){
        return logPane;
    }
}
