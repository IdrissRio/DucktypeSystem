package it.uniud.ducktypesystem.view;

import it.uniud.ducktypesystem.controller.DSApplication;
import it.uniud.ducktypesystem.distributed.controller.DSAbstractInterface;
import it.uniud.ducktypesystem.distributed.controller.DSInterface;
import it.uniud.ducktypesystem.distributed.data.DSGraph;
import it.uniud.ducktypesystem.distributed.data.DataFacade;
import it.uniud.ducktypesystem.errors.SystemError;
import it.uniud.ducktypesystem.logger.DSAbstractLog;
import it.uniud.ducktypesystem.logger.DSLog;
import org.graphstream.algorithm.generator.FlowerSnarkGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
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
    private ViewPanel graphView;
    // The DSGraph is accessible by `facade.getGraph()' *after* configureSystem() is called.
    // or NullPointerException will be thrown.
    private DataFacade facade;
    private Integer processNumber;
    private Integer replicasNumber;
    private Graph graph;
    private Viewer viewer;
    private Color greenForest= new Color(11,102,35);
    private String graphPathString;
    private JTextField mainPathField;
    private Boolean graphCheck;
    private Boolean queryCheck;
    private JButton startNewComputation;

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
        graphCheck=true; //This should be false, but in production we set it true.
        queryCheck=true; //This should be false, but in production we set it true.
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
        initMainFrame();
        setMenuItem();
        facade = null;
        welcomeGraph();
        mainFrame.setVisible(true);
        showInformationMessage("DucktypeSystem v 0.1");
    }

    @Override
    public void exit() {
        if(confirmExit())
            App.exit();
    }

    private boolean confirmExit() {
        return JOptionPane.showConfirmDialog(mainFrame,
                "Do you really want to Exit?",
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
            JPanel northPanel= new JPanel(new FlowLayout());
            JButton confirmButton = new JButton("Confirm");
            JTextField pathField=new JTextField("",13);
            JButton pathButton =new JButton("Source...");
            northPanel.add(pathButton);
            northPanel.add(pathField);
            pathField.setEditable(false);
            settingPanel.add(northPanel);
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
            secondFrame.setBounds(100, 200, 350, 170);
            secondFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            secondFrame.setVisible(true);
            secondFrame.setResizable(false);
            confirmButton.addActionListener(e -> {
                try {
                    processNumber = Integer.parseInt(numberProcess.getText());
                    replicasNumber = Integer.parseInt(numberReplica.getText());
                    mainPathField.setText(pathField.getText());
                    configureSystem(graphPathString, processNumber, replicasNumber, logger);
                    graphVisualization(facade.getMap());
                    secondFrame.dispose();
                }catch(NumberFormatException error){
                    showErrorMessage("SETTINGS: Inavlid number. Please check it!");
                }catch(NullPointerException error){
                    showErrorMessage("SETTINGS: You have to choose a file description for the graph.");
                }catch(SystemError sError){
                    showErrorMessage("SETTINGS: I cannot read this file. Accepted extensions: DOT, DGS, GML," +
                            " TLP, NET, graphML, GEXF.");
                    pathField.setText("");
                }
            });

            pathButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.showOpenDialog(mainFrame);
                pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                graphPathString=pathField.getText();
                setGraphCheck(true);
                startNewComputation.setEnabled(isStartEnable());

            });
            secondFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent evento) {
                    if(JOptionPane.showConfirmDialog(secondFrame,
                            "Do you rellay want to exit?\n" +
                                    "If you exit, the changes of the settings will not be applied.",
                            "Groot say:", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                        secondFrame.dispose();
                    }
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
        JLabel pathLbl =new JLabel("Graph path:");
        JButton queryButton = new JButton("Query source");
        JTextField queryField = new JTextField();
        JPanel soutWithStartPanel = new JPanel(new BorderLayout());
        startNewComputation = new JButton("Start");
        startNewComputation.setEnabled(isStartEnable());
        mainPathField=new JTextField();
        mainPathField.setEditable(false);
        northPanel.add(mainPathField,BorderLayout.CENTER);
        northPanel.add(pathLbl,BorderLayout.WEST);
        queryField.setEditable(false);
        southPanel.add(queryButton,BorderLayout.EAST);
        southPanel.add(queryField,BorderLayout.CENTER);
        soutWithStartPanel.add(southPanel, BorderLayout.CENTER);
        soutWithStartPanel.add(startNewComputation,BorderLayout.EAST);
        graphAndNorthPanel.add(northPanel,BorderLayout.NORTH);
        graphAndNorthPanel.add(graphPanel, BorderLayout.CENTER);
        mainPanel.add(graphAndNorthPanel, BorderLayout.NORTH);
        mainPanel.add(logScroll, BorderLayout.CENTER);
        mainPanel.add(soutWithStartPanel,BorderLayout.SOUTH);
        mainFrame = new JFrame();
        mainFrame.getContentPane().add(mainPanel);
        mainFrame.setTitle("A distributed subgraph isomorphism");
        mainFrame.setBounds(20, 00, 700, 750);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainPathField.setFont(Font.getFont("Bariol"));

        // MainFrame window listener
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                exit();
            }
        });
        startNewComputation.addActionListener(e -> {
            //Start the computation in a new thread.
            Thread thread = new Thread(() -> {
                showInformationMessage("INFO: starting the AKKA environment.");
                new DSInterface(facade);
            });
            thread.start();
        });

        // Start computation listener.
        queryButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showOpenDialog(mainFrame);
            queryField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            graphPathString=queryField.getText();
            //If there is no exception enable startButton
            setQueryCheck(true);
            startNewComputation.setEnabled(isStartEnable());
            //try {
                //Here we should create a new query.
                showInformationMessage("SETTINGS: Graph reading complete.");
            /*//}catch(SystemError sError){
            showErrorMessage("SETTINGS: I cannot read this file. Accepted extensions: DOT, DGS, GML," +
                        " TLP, NET, graphML, GEXF.");
                queryField.setText("");
            //}*/
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
        graph =(Graph) x.getGraphImpl();
        graph.setStrict(false);
        graph.setAutoCreate( true );
        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        graphPanel.remove(graphView);
        graphView = viewer.addDefaultView(false);
        graphPanel.add(graphView);
        graphPanel.updateUI();
    }

    private void welcomeGraph(){
        graph=new DefaultGraph("WelcomeGraph");
        Generator gen = new FlowerSnarkGenerator();
        gen.addSink(graph);
        gen.begin();
        for(int i=0; i<100; i++)
            gen.nextEvents();
        gen.end();
        viewer=new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        graphView=viewer.addDefaultView(false);
        graphPanel.add(graphView);
    }
    // Initialize graph and system parameters got from visual interface.
    // NB: the caller is responsible of initialize `numRobot' and `numSearchGroup' default parameters (0 and 3 respectively).
    private void configureSystem(String filePath, int numRobot, int numSearchGroup, DSAbstractLog log) throws SystemError {
            facade = DataFacade.create(filePath);
            facade.setNumSearchGroups(numSearchGroup);
            facade.setOccupied(numRobot);
            facade.setLogger(log);
    }

    // Configure occupied vector: this should be called *after* configureSystem().
    // This allows the initialization of the robots' position by selecting nodes on visual interface.
    private void configureOccupied(ArrayList<String> occupied) {
        if (facade == null || facade.getMap() == null ) {
            showErrorMessage("An error occurred in System Configuration.");
            return;
        }
        facade.setOccupied(occupied);
    }
    private void setGraphCheck(Boolean b){graphCheck=b;}
    private void setQueryCheck(Boolean b){queryCheck=b;}
    private Boolean getGraphCheck(){return graphCheck;}
    private Boolean getQueryCheck(){return queryCheck;}
    private Boolean isStartEnable(){return getGraphCheck() && getQueryCheck();}
    private void showInformationMessage(String s){
        logger.log(s,greenForest);
    }
    private void showErrorMessage(String s) {
        logger.log(s, Color.RED);
    }
}

