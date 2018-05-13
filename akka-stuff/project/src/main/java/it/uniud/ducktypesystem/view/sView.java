package it.uniud.ducktypesystem.view;

import it.uniud.ducktypesystem.controller.Application;
import it.uniud.ducktypesystem.logger.abstractLog;
import it.uniud.ducktypesystem.logger.log;
import org.graphstream.algorithm.generator.BananaTreeGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;


public class sView implements View {
    protected JFrame mainFrame;
    protected Application App;
    protected JTextField pathField;
    protected URL pathUrl;
    protected abstractLog logger;
    protected JScrollPane logScroll;
    protected JPanel graphPanel;
    protected JPanel mainPanel;
    protected Graph graph;
    protected Generator gen;
    protected Viewer viewer;
    protected ViewPanel view;
    protected Color greenForest= new Color(11,102,35);

    public sView(Application application) {
        try {
            System.setProperty( "com.apple.mrj.application.apple.menu.about.name", "ted" );
            System.setProperty( "com.apple.macos.useScreenMenuBar", "true" );
            System.setProperty( "apple.laf.useScreenMenuBar", "true" );
            com.apple.eawt.Application macOS = com.apple.eawt.Application.getApplication();
            ImageIcon img=new ImageIcon(this.getClass().getResource("/DucktypeIcon.png"));

            macOS.setDockIconImage(img.getImage());
        } catch ( Throwable e ) {
            e.printStackTrace();
        }

        this.App = application;
        logger=new log();
        logScroll=new JScrollPane(logger.getLog());
        logScroll.setSize(new Dimension(600,300));
        graphPanel=new JPanel(new GridLayout()){
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(640, 480);
            }
        };
    }

    public void openApplication() {
        logger.log("Inizializzazione ambiente", greenForest);
        initMainFrame();
        setMenuItem();
        logger.log("Inizializzazione grafo", greenForest);
        welcomeGraph();
        mainFrame.setVisible(true);


        gen.begin();
        for(int i=0; i<20; i++) {
            gen.nextEvents();
            viewer=new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
            try {
                Thread.sleep(100);
            }catch(Throwable e){

            }
            viewer.addDefaultView(false);
        }
        gen.end();
        logger.log("Inzializzazione completata", greenForest);
        logger.log("And this is a very very very very very very  very very  very very  very very  very very  very very  very very  long string", Color.RED);
    }

    protected void exit() {
        if(confirmExit())
            App.exit();
    }

    protected boolean confirmExit() {
        return JOptionPane.showConfirmDialog(mainFrame,
                "Do you reallly want to Exit?",
                "Groot say:", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION;
    }

    protected void setMenuItem(){
        JMenuItem openMenuItem = new JMenuItem("Open...");
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent l) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.showOpenDialog(mainFrame);
                pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                try {
                    pathUrl = new URL(fileChooser.getSelectedFile().getAbsolutePath());
                }catch(Throwable e) {
                }
            }
        });

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic(KeyEvent.VK_E);
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                    exit();
            }
        });

        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);
        menuFile.add(openMenuItem);
        menuFile.addSeparator();
        menuFile.add(exitMenuItem);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menuFile);
        mainFrame.setJMenuBar(menuBar);
    }

    private void initMainFrame(){
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(graphPanel, BorderLayout.NORTH);
        mainPanel.add(logScroll, BorderLayout.CENTER);
        mainFrame = new JFrame();
        mainFrame.getContentPane().add(mainPanel);
        mainFrame.setTitle("A distributed subgraph isomorphism");
        mainFrame.setBounds(0, 0, 700, 750);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                exit();
            }
        });
    }
    private String getGraphAttribute(){
        return "" +
                "graph {\n" +
                "\tfill-color: #175676;\n" +
                "}\n" +
                "\n" +
                "edge {\n" +
                "\tfill-color: #C9A690;\n" +
                "}\n" +
                "node {\n" +
                "\tsize: 10px, 15px;\n" +
                "\tshape: box;\n" +
                "\tfill-color: #EF6F6C;\n" +
                "\tstroke-mode: plain;\n" +
                "\tstroke-color: yellow;\n" +
                "}\n"+
                "\n" +
                "node:clicked {\n" +
                "\tfill-color: #FFEECF ;\n" +
                "}";
    }



    private void welcomeGraph(){
        graph = new SingleGraph("Welcome Graph");
        gen = new BananaTreeGenerator();
        graph.addAttribute("ui.stylesheet", getGraphAttribute());
        gen.addSink(graph);
        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        view = viewer.addDefaultView(false);
        graphPanel.add(view);
    }
}

