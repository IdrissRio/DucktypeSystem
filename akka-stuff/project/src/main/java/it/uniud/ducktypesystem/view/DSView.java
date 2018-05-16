package it.uniud.ducktypesystem.view;

import it.uniud.ducktypesystem.controller.DSApplication;
import it.uniud.ducktypesystem.distributed.controller.DSAbstractInterface;
import it.uniud.ducktypesystem.distributed.controller.DSInterface;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DataFacade;
import it.uniud.ducktypesystem.errors.SystemError;
import it.uniud.ducktypesystem.logger.DSAbstractLog;
import it.uniud.ducktypesystem.logger.DSLog;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;


public class DSView implements DSAbstractView {
    private JFrame mainFrame;
    private DSApplication App;
    private DSAbstractLog logger;
    private JScrollPane logScroll;
    private JPanel graphPanel;

    // The DSGraph is accessible by `facade.getGraph()' *after* configureSystem() is called.
    // or NullPointerException will be thrown.
    private DataFacade facade;

    private Integer processNumber;
    private Integer replicasNumber;
    private Graph graph;
    private Viewer viewer;
    private Color greenForest= new Color(11,102,35);
    private String graphPathString;

    public DSView(DSApplication application) {
        try {
            System.setProperty( "com.apple.mrj.application.apple.menu.about.name", "ted" );
            System.setProperty( "com.apple.macos.useScreenMenuBar", "true" );
            System.setProperty( "apple.laf.useScreenMenuBar", "true" );
            //If using a macOS this can be useful.
            // com.apple.eawt.Application macOS = com.apple.eawt.Application.getApplication();
            // ImageIcon img=new ImageIcon(this.getClass().getResource("/DucktypeIcon.png"));
            // macOS.setDockIconImage(img.getImage());
        } catch ( Throwable e ) {
            e.printStackTrace();
        }
        processNumber=new Integer(3);
        replicasNumber=new Integer(1);
        this.App = application;
        logger=new DSLog();
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
        facade = null;
        welcomeGraph();
        mainFrame.setVisible(true);
        logger.log("Inzializzazione completata", greenForest);
    }

    @Override
    public void exit() {
        if(confirmExit())
            App.exit();
    }

    private boolean confirmExit() {
        return JOptionPane.showConfirmDialog(mainFrame,
                "Do you reallly want to Exit?",
                "Groot say:", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION;
    }

    private void setMenuItem(){
        JMenuItem openMenuItem = new JMenuItem("Settings...");
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.addActionListener(l -> {
            JDialog secondFrame = new JDialog(mainFrame);



            JTextField numberReplica = new JTextField(replicasNumber.toString(),10);
            JTextField numberProcess = new JTextField(processNumber.toString(),10);
            JLabel numberProcessLbl  = new JLabel("Number of process:");
            JLabel numberReplicaLbl = new JLabel("Number of replicas:");
            JPanel panelProcess = new JPanel(new FlowLayout());
            JPanel panelReplica=new JPanel(new FlowLayout());
            JPanel settingPanel=new JPanel();
            JButton confirmButton = new JButton("Confirm");
            settingPanel.setLayout(new BoxLayout(settingPanel, BoxLayout.Y_AXIS));
            panelProcess.add(numberProcessLbl);
            panelProcess.add(numberProcess);
            panelReplica.add(numberReplicaLbl);
            panelReplica.add(numberReplica);
            settingPanel.add(panelProcess);
            settingPanel.add(panelReplica);
            confirmButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            settingPanel.add(confirmButton);
            secondFrame.getContentPane().add(settingPanel);
            secondFrame.setTitle("Settings");
            secondFrame.setBounds(200, 200, 300, 150);
            secondFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            secondFrame.setVisible(true);
            secondFrame.setResizable(false);
            confirmButton.addActionListener(e -> {
                try {
                    processNumber = Integer.parseInt(numberProcess.getText());
                    replicasNumber = Integer.parseInt(numberReplica.getText());
                    secondFrame.dispose();
                }catch(NumberFormatException error){
                    showErrorMessage("Inavlid number. Please check it!");
                }
            });
            secondFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent evento) {
                    if(JOptionPane.showConfirmDialog(secondFrame,
                            "Do you rellay want to exit?\n" +
                                    "If you exit, the changes to the settings will not be applied.",
                            "Groot say:", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) secondFrame.dispose();

                }
            });


        });

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic(KeyEvent.VK_E);
        exitMenuItem.addActionListener(evento -> exit());
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
        JPanel mainPanel=new JPanel(new BorderLayout());
        JPanel southPanel=new JPanel(new BorderLayout());
        JPanel northPanel=new JPanel(new BorderLayout());
        JPanel graphAndNorthPanel = new JPanel(new BorderLayout());
        JTextField pathField=new JTextField();
        pathField.setEditable(false);
        JButton pathButton=new JButton("Graph path");
        northPanel.add(pathField,BorderLayout.CENTER);
        northPanel.add(pathButton,BorderLayout.EAST);

        JButton startComputation = new JButton("Submit a new query");
        southPanel.add(startComputation,BorderLayout.CENTER);
        graphAndNorthPanel.add(northPanel,BorderLayout.NORTH);
        graphAndNorthPanel.add(graphPanel, BorderLayout.CENTER);
        mainPanel.add(graphAndNorthPanel, BorderLayout.NORTH);
        mainPanel.add(logScroll, BorderLayout.CENTER);
        mainPanel.add(southPanel,BorderLayout.SOUTH);
        mainFrame = new JFrame();
        mainFrame.getContentPane().add(mainPanel);
        mainFrame.setTitle("A distributed subgraph isomorphism");
        mainFrame.setBounds(0, 0, 700, 750);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        pathField.setFont(Font.getFont("Bariol"));
        //PathButton listener
        pathButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showOpenDialog(mainFrame);
            pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            graphPathString=pathField.getText();
            configureSystem(graphPathString, processNumber, replicasNumber);
            graphVisualization(facade.getMap());
        });

        // MainFrame window listener
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                exit();
            }
        });

        // Start computation listener.
        startComputation.addActionListener(e -> {
            Thread thread = new Thread(() -> {
                logger.log("Let's start the computation", greenForest);
                DSAbstractInterface computation = new DSInterface(logger,graph,graph);
            });
            thread.start();
            graph.removeEdge("CF");
            graph.removeEdge("CD");
            graph.removeEdge("CE");
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


    private void graphVisualization(DSGraph x){
        graph =(Graph) x.getGraph();
    }
    private void welcomeGraph(){
        graph = new SingleGraph("Welcome Graph");
        //graph.addAttribute("ui.stylesheet", getGraphAttribute());


        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        ViewPanel view = viewer.addDefaultView(false);
        graphPanel.add(view);
    }

    // Initialize graph and system parameters got from visual interface.
    // NB: the caller is responsible of initialize `numRobot' and `numSearchGroup' default parameters (0 and 3 respectively).
    private void configureSystem(String filePath, int numRobot, int numSearchGroup) {
        try {
            facade = DataFacade.create(filePath);
            facade.setNumSearchGroups(numSearchGroup);
            facade.setOccupied(numRobot);
        } catch (SystemError e) {
            showErrorMessage("An error occurred in System Configuration.");
            e.printStackTrace();
        }
    }
    // Configure occupied vector: this should be called *after* configureSystem().
    // This allows the initialization of the robots' position by selecting nodes on visual interface.
    private void configureOccupied(ArrayList<DSGraph.Node> occupied) {
        if (facade == null || facade.getMap() == null ) {
            showErrorMessage("An error occurred in System Configuration.");
            return;
        }
        facade.setOccupied(occupied);

    }
    private void showInformationMessage(String s){
        logger.log(s,greenForest);
    }
    private void showErrorMessage(String s) {
        logger.log(s, Color.RED);
    }
}

