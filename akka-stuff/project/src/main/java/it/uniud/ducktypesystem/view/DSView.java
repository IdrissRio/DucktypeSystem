package it.uniud.ducktypesystem.view;

import it.uniud.ducktypesystem.controller.DSApplication;
import it.uniud.ducktypesystem.distributed.data.*;
import it.uniud.ducktypesystem.errors.SystemError;
import it.uniud.ducktypesystem.logger.DSAbstractLog;
import it.uniud.ducktypesystem.logger.DSLog;
import org.graphstream.algorithm.generator.FlowerSnarkGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;

import static it.uniud.ducktypesystem.distributed.data.DSCluster.akkaEnvironment;


public class DSView implements DSAbstractView {
    private JFrame mainFrame;
    private DSApplication App;
    private DSAbstractLog logger;
    private JScrollPane logScroll;
    private JPanel graphPanel;
    private ViewPanel graphView;
    // The DSGraph is accessible by `facade.getMap()' *after* configureSystem() is called.
    // or NullPointerException will be thrown.
    private DataFacade facade;
    private Integer processNumber;
    private Integer replicasNumber;
    private Graph graph;
    private Viewer viewer;
    private Color greenForest= new Color(11,102,35);
    private Color pink= new Color(255, 102, 255);
    private String graphPathString;
    private JTextField mainPathField;
    private Boolean graphCheck;
    private Boolean queryCheck;
    private JButton startNewComputation;
    private DSQuery newQuery;
    private JPanel eastPanelQuery;
    private JScrollPane scrollForQuery;
    private JPanel mainPanel;
    private Boolean autoMove;

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
        processNumber=3;
        replicasNumber=1;
        this.App = application;
        logger=new DSLog();
        graphCheck=false;
        queryCheck=false;
        eastPanelQuery=new JPanel();
        logScroll=new JScrollPane(logger.getLog(),
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logScroll.setPreferredSize(new Dimension(1000,150));
        logScroll.setSize(new Dimension(1000,300));
        graphPanel=new JPanel(new BorderLayout());
        autoMove=false;
    }


    public void openApplication() {
        welcomeFrame();
        setup();
        initMainFrame();
        setMenuItem();
        facade = null;
        graphViewInit();
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

    private void setup(){
        JDialog secondFrame = new JDialog();
        JCheckBox autoMoveCB = new JCheckBox("Enable auto-move");
        JTextField numberProcess = new JTextField(processNumber.toString(),10);
        JLabel numberProcessLbl  = new JLabel("Number of process:");
        JPanel panelProcess = new JPanel(new FlowLayout());
        JPanel panelReplica=new JPanel(new FlowLayout());
        JPanel settingPanel=new JPanel();
        JPanel northPanel= new JPanel(new FlowLayout());
        JButton confirmButton = new JButton("Confirm");
        JTextField pathField=new JTextField("",13);
        JButton pathButton =new JButton("Source...");
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        northPanel.add(pathButton);
        northPanel.add(pathField);
        pathField.setEditable(false);
        settingPanel.add(northPanel);
        settingPanel.setLayout(new BoxLayout(settingPanel, BoxLayout.Y_AXIS));
        panelProcess.add(numberProcessLbl);
        panelProcess.add(numberProcess);
        panelReplica.add(autoMoveCB);
        settingPanel.add(panelProcess);
        settingPanel.add(panelReplica);
        confirmButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingPanel.add(confirmButton);
        secondFrame.getContentPane().add(settingPanel);
        secondFrame.setTitle("Settings");
        secondFrame.setBounds(0, 0, 350, 170);
        secondFrame.setLocation(dim.width/2-secondFrame.getSize().width/2, dim.height/2-secondFrame.getSize().height/2);
        secondFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        secondFrame.setVisible(true);
        secondFrame.setResizable(false);
        confirmButton.addActionListener(e -> {
            try {
                processNumber = Integer.parseInt(numberProcess.getText());
                autoMove=autoMoveCB.isSelected();
                //replicasNumber = Integer.parseInt(numberReplica.getText());
                mainPathField.setText(pathField.getText());
                configureSystem(graphPathString, processNumber, replicasNumber, logger);
                Thread thread = new Thread(() -> {
                    showInformationMessage("INFO: starting the AKKA environment.");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    akkaEnvironment(facade, this, this.App);
                });
                graphVisualization(facade.getMap());
                thread.start();
                secondFrame.dispose();
                mainFrame.setVisible(true);
            }catch(NumberFormatException error){
                error.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Invalid number.\n Please check it!","Error !",JOptionPane.ERROR_MESSAGE);
                //showErrorMessage("SETTINGS: Inavlid number. Please check it!");
            }catch(NullPointerException error){
                error.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        " You have to choose a file description for the graph.","Error !",JOptionPane.ERROR_MESSAGE);
                //showErrorMessage("SETTINGS: You have to choose a file description for the graph.");
            }catch(SystemError sError){
                sError.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        " SETTINGS: I cannot read this file.\n" +
                                " Accepted extensions: DOT, DGS, GML,\" +\n" +
                                "                        \" TLP, NET, graphML, GEXF.","Error !",JOptionPane.ERROR_MESSAGE);
                //showErrorMessage("SETTINGS: I cannot read this file. Accepted extensions: DOT, DGS, GML," +
                //" TLP, NET, graphML, GEXF.");
                pathField.setText("");
            }
        });

        pathButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showOpenDialog(mainFrame);
            try {
                pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }catch(NullPointerException f){}
            graphPathString=pathField.getText();
            setGraphCheck(true);
            startNewComputation.setEnabled(isStartEnable());

        });
        secondFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                if(JOptionPane.showConfirmDialog(secondFrame,
                        "Do you rellay want to exit?\n",
                        "Groot say:", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                    App.exit();
                }
            }
        });
    }

    private void setMenuItem(){
        JMenuItem openMenuItem = new JMenuItem("Settings...");
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.addActionListener(l -> {

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

    private void welcomeFrame(){
        JPanel welcomeMainPanel=new JPanel(new BorderLayout());
        ViewPanel graphViewNew;
        JPanel panelGraphNew=new JPanel(new BorderLayout());
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        JLabel DucktypeSystemLbl= new JLabel("DucktypeSystem v. 0.1");
        JFrame mainFrame = new JFrame();
        welcomeMainPanel.add(panelGraphNew, BorderLayout.CENTER);
        DucktypeSystemLbl.setSize(200, 200);
        DucktypeSystemLbl.setFont(new Font("Bariol", Font.PLAIN,30));
        DucktypeSystemLbl.setForeground(Color.WHITE);
        DucktypeSystemLbl.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeMainPanel.setBackground(Color.BLACK);
        welcomeMainPanel.add(DucktypeSystemLbl, BorderLayout.SOUTH);
        graph=new DefaultGraph("WelcomeGraph");
        graph.setAttribute("ui.class", "marked");
        graph.addAttribute("ui.stylesheet","url(welcomeGraphStyleSheet.css)");
        Generator gen = new FlowerSnarkGenerator();
        gen.addSink(graph);
        gen.begin();
        for(int i=0; i<100; i++)
            gen.nextEvents();
        gen.end();
        viewer=new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        graphViewNew=viewer.addDefaultView(false);
        panelGraphNew.add(graphViewNew);
        mainFrame.getContentPane().add(welcomeMainPanel);
        mainFrame.setTitle("A distributed subgraph isomorphism");
        mainFrame.setUndecorated(true);
        mainFrame.setBounds(00, 00, 700, 500);
        mainFrame.setLocation(dim.width/2-mainFrame.getSize().width/2, dim.height/2-mainFrame.getSize().height/2);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.setVisible(true);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mainFrame.dispose();
    }

    private void initMainFrame(){
        mainPanel=new JPanel(new BorderLayout());
        JPanel southPanel=new JPanel(new BorderLayout());
        JPanel northPanel=new JPanel(new BorderLayout());
        JLabel pathLbl =new JLabel("Graph path:");
        JButton queryButton = new JButton("Query source");
        JTextField queryField = new JTextField();
        JPanel soutWithStartPanel = new JPanel(new BorderLayout());
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        JPanel southWithScroll= new JPanel(new BorderLayout());
        JButton moveRobot = new JButton("Move robot");
        eastPanelQuery=new JPanel();
        eastPanelQuery.setLayout(new BoxLayout(eastPanelQuery, BoxLayout.Y_AXIS));
        scrollForQuery=new JScrollPane(eastPanelQuery,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollForQuery.setPreferredSize(new Dimension(300,500));
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
        southWithScroll.add(soutWithStartPanel, BorderLayout.SOUTH);
        southWithScroll.add(logScroll,BorderLayout.CENTER);
        mainPanel.add(northPanel,BorderLayout.NORTH);
        graphPanel.add(moveRobot, BorderLayout.SOUTH);
        mainPanel.add(graphPanel, BorderLayout.CENTER);
        mainPanel.add(southWithScroll, BorderLayout.SOUTH);
        mainPanel.add(scrollForQuery,BorderLayout.EAST);
        mainFrame = new JFrame();
        mainFrame.getContentPane().add(mainPanel);
        mainFrame.setTitle("A distributed subgraph isomorphism");
        mainFrame.setBounds(20, 00, 1000, 750);
        mainFrame.setLocation(dim.width/2-mainFrame.getSize().width/2, dim.height/2-mainFrame.getSize().height/2);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainPathField.setFont(Font.getFont("Bariol"));
        // MainFrame window listener
        moveRobot.addActionListener(e -> {
            try {
                DSCluster.getInstance().makeMove(0); //FIXME: this is the current host. Deve essere indicizzatato.
            }catch (NullPointerException error){
                showErrorMessage("Wait. We are initializing the Akka environment.");
            }
        });

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                exit();
            }
        });
        startNewComputation.addActionListener(e -> {
            //Start the computation in a new thread.
            Thread thread = new Thread(() -> {
                // FIXME: here 0 stands for `host' index.
                String qVersion = DSCluster.getInstance().startNewComputation(0, newQuery);
                setQueryCheck(false);
                startNewComputation.setEnabled(isStartEnable());
                updateQuery(newQuery.getId(), DSQuery.QueryStatus.NEW);
                JScrollBar vertical = scrollForQuery.getVerticalScrollBar();
                vertical.setValue( vertical.getMaximum() );
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
            try {
                newQuery = DSQueryImpl.createQueryFromFile(graphPathString);
                showInformationMessage("SETTINGS: Graph reading complete.");

            }catch(NullPointerException error) {
                showErrorMessage("SETTINGS: You have to choose a file description for the graph.");
            }catch(SystemError sError){
                showErrorMessage("SETTINGS: I cannot read this file. Accepted extensions: DOT, DGS, GML," +
                        " TLP, NET, graphML, GEXF.");
                queryField.setText("");
            }
        });
    }

    private ViewPanel queryVisualization(DSGraph x){
        ViewPanel queryViewPanel;
        graph =(Graph) x.getGraphImpl();
        graph.setStrict(false);
        graph.setAutoCreate( true );
        graph.addAttribute("ui.stylesheet","url(nodeStyle.css)");
        for (Node node : graph) {
            node.addAttribute("ui.label", node.getId());
            node.addAttribute("ui.class", "normal");
        }
        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        queryViewPanel = viewer.addDefaultView(false);
        return queryViewPanel;
    }
    private void graphVisualization(DSGraph x){
        graph =(Graph) x.getGraphImpl();
        graph.setStrict(false);
        graph.setAutoCreate( true );
        graph.addAttribute("ui.stylesheet", "url(nodeStyle.css)");
        for (Node node : graph) {
            node.addAttribute("ui.label", node.getId());
            if(facade.getOccupied().contains(node.toString()))
                node.addAttribute("ui.class", "occupied");
            else
                node.addAttribute("ui.class", "normal");
        }
        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        graphPanel.remove(graphView);
        graphView = viewer.addDefaultView(false);
        graphPanel.add(graphView);
        graphPanel.updateUI();
    }
    private void graphViewInit(){
        graph=new DefaultGraph("WelcomeGraph");
        graph.setAttribute("ui.class", "marked");
        viewer=new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        graphView=viewer.addDefaultView(false);
        graphPanel.add(graphView);
    }
    // Initialize graph and system parameters got from visual interface.
    // NB: the caller is responsible of initialize `numRobot' and `numSearchGroup' default parameters (0 and 3 respectively).
    private void configureSystem(String filePath, int numRobot, int numSearchGroup, DSAbstractLog log) throws SystemError {
        facade=DataFacade.create(filePath);
        facade.setNumSearchGroups(numSearchGroup);
        facade.setOccupied(numRobot);
        StringBuilder b = new StringBuilder();
        b.append("Robot posizionati in: ");
        for (String s : facade.getOccupied())
            b.append(s + " ");
        showInformationMessage(b.toString());
    }
    private void setGraphCheck(Boolean b){graphCheck=b;}
    private void setQueryCheck(Boolean b){queryCheck=b;}
    private Boolean getGraphCheck(){return graphCheck;}
    private Boolean getQueryCheck(){return queryCheck;}
    private Boolean isStartEnable(){return getGraphCheck() && getQueryCheck();}
    public void showInformationMessage(String s){
        logger.log(s,greenForest);
        JScrollBar vertical = logScroll.getVerticalScrollBar();
        vertical.setValue( vertical.getMaximum() );
    }
    public void showErrorMessage(String s) {
        logger.log(s, Color.RED);
        JScrollBar vertical = logScroll.getVerticalScrollBar();
        vertical.setValue( vertical.getMaximum() );
    }

    public void showQueryStatus( DSQuery.QueryStatus status, String version){
        switch(status) {
            case MATCH:
                logger.log("Query " + version + " ended: MATCH!", Color.BLUE);
                break;
            case FAIL:
                logger.log("Query " + version + " ended: FAIL!", Color.red);
                break;
            case NEW:
                logger.log("Started the computation of a new query: " + version , pink);
                break;
            default:
                logger.log("Query "+version+" ended: DONTKNOW!",Color.ORANGE );
        }
        JScrollBar vertical = logScroll.getVerticalScrollBar();
        vertical.setValue( vertical.getMaximum() );
    }

    public JFrame getMainFrame(){return mainFrame;}
    @Override
    public void updateRobotsPosition() {
        StringBuilder b = new StringBuilder();
        b.append("Robot posizionati in: ");
        for (String s : facade.getOccupied())
            b.append(s + " ");
        showInformationMessage(b.toString());
        graph =(Graph) facade.getMap().getGraphImpl();
        graph.addAttribute("ui.stylesheet","url(nodeStyle.css)");
        for (Node node : graph) {
            node.addAttribute("ui.label", node.getId());
            if(facade.getOccupied().contains(node.toString()))
                node.addAttribute("ui.class", "occupied");
            else
                node.addAttribute("ui.class", "");
        }

        graphPanel.updateUI();
    }

    private void refreshQuery(int host,String version, DSQuery.QueryStatus status) {
        // FIXME: here 0 stands for `host' parameter
        DSCluster.getInstance().getActiveQueries(0).forEach((mapVersionTmp, mapWrapperTmp) -> {
            Boolean find = false;
            DSQueryResult mapWrapper = (DSQueryResult) mapWrapperTmp;
            String mapVersion = (String) mapVersionTmp;
            String labelText = mapVersion;
            if (version.equals(mapVersion)) {
                JPanel aglomeratePanel = new JPanel(new BorderLayout());
                aglomeratePanel.setPreferredSize(new Dimension(300, 520));
                JPanel twoQueryStatusPanel = new JPanel();
                JButton retry = new JButton("Retry");
                retry.addActionListener(e ->{
                            retry.setEnabled(false);
                            DSCluster.getInstance().retryQuery(host, version);
                        }
                );
                if(!autoMove)
                    aglomeratePanel.add(retry, BorderLayout.SOUTH);
                if (mapWrapper.getStillToVerify() == null)
                    twoQueryStatusPanel.setLayout(new GridLayout(0, 1));
                else
                    twoQueryStatusPanel.setLayout(new GridLayout(0, 2));
                aglomeratePanel.setName(mapVersion);
                twoQueryStatusPanel.setName(mapVersion);
                twoQueryStatusPanel.add(queryVisualization(mapWrapper.getQuery()));
                Color labelColor;
                switch (status) {
                    case MATCH:
                        labelText += " - MATCH";
                        labelColor = Color.CYAN;
                        break;
                    case DONTKNOW:
                        labelText += " - DONTKNOW";
                        labelColor = Color.ORANGE;
                        break;
                    case NEW:
                        labelText += " - NEW";
                        labelColor = pink;
                        break;
                    default:
                        labelText += " - FAIL";
                        labelColor = Color.RED;
                }
                JLabel queryNameLbl = new JLabel(labelText);
                queryNameLbl.setSize(200, 200);
                queryNameLbl.setFont(new Font("Bariol", Font.PLAIN, 20));
                queryNameLbl.setForeground(labelColor);
                queryNameLbl.setHorizontalAlignment(SwingConstants.CENTER);
                aglomeratePanel.setBackground(Color.DARK_GRAY);
                aglomeratePanel.add(queryNameLbl, BorderLayout.NORTH);
                for (Component c : eastPanelQuery.getComponents()) {
                    if (c instanceof JPanel && c.getName().equals(mapVersion)) { //FIXME: Rossi G. ... Perdoname por mi vida loca !
                        find = true;
                        for (Component d : ((JPanel) c).getComponents()) {
                            if (d instanceof JPanel && d.getName().equals(mapVersion)) {
                                int i = 0;
                                for (Component e : ((JPanel) d).getComponents()) {
                                    ++i;
                                    if (e.getName() != null && mapWrapper.getStillToVerify() != null) {
                                        ((JPanel) d).remove(e);
                                        JPanel tmp = queryVisualization(DSGraphImpl.createFromSerializedString(mapWrapper.getStillToVerify()));
                                        tmp.setName(mapVersion);
                                        tmp.setBorder(new MatteBorder(2, 0, 0, 0, Color.BLACK));
                                        ((JPanel) d).add(tmp);
                                    }
                                    if (e.getName() != null & status != DSQuery.QueryStatus.DONTKNOW) {
                                        ((JPanel) d).remove(e);
                                    }
                                }
                                if (i < 2 && mapWrapper.getStillToVerify() != null) {
                                    JPanel tmp = queryVisualization(DSGraphImpl.createFromSerializedString(mapWrapper.getStillToVerify()));
                                    tmp.setName(mapVersion);
                                    tmp.setBorder(new MatteBorder(2, 0, 0, 0, Color.BLACK));
                                    ((JPanel) d).add(tmp);
                                }
                            }
                            if ((d instanceof JLabel)) {
                                ((JLabel) d).setText(labelText);
                                d.setForeground(labelColor);
                            }
                        }
                    }
                }


                if (find == false) {
                    aglomeratePanel.add(twoQueryStatusPanel, BorderLayout.CENTER);
                    aglomeratePanel.setBorder(new MatteBorder(0, 0, 2, 0, Color.WHITE));
                    eastPanelQuery.add(aglomeratePanel);
                }
            }
            eastPanelQuery.add(Box.createVerticalGlue());
        });
        mainPanel.updateUI();
    }


    @Override
    public void updateQuery(DSQuery.QueryId qId, DSQuery.QueryStatus status) {
        // FIXME: update the correct host view.
        String version = qId.getVersion();
        int host = qId.getHost();

        refreshQuery(qId.getHost(), qId.getVersion(), status);

        switch(status) {
            case MATCH:
            case FAIL:
            case NEW: showQueryStatus(status, version);
                for (Component c : eastPanelQuery.getComponents()) {
                    if (c instanceof JPanel && c.getName().equals(version)) {
                        for (Component d : ((JPanel) c).getComponents()) {
                            if ((d instanceof JButton)) {
                                (d).setEnabled(false);
                            }
                        }
                    }
                }
                break;
            default:
                DSCluster.getInstance().getActiveQueries(0).forEach((mapVersionTmp, mapWrapperTmp) -> {
                    for (Component c : eastPanelQuery.getComponents()) {
                        if (c instanceof JPanel && c.getName().equals(version)) {
                            for (Component d : ((JPanel) c).getComponents()) {
                                if ((d instanceof JButton) && autoMove==false) {
                                    (d).setEnabled(true);
                                }
                            }
                        }
                    }
                });
                showQueryStatus(status, version);
                //showInformationMessage("Query "+version+" ended: DONTKNOW!");
                // TODO: enable retry query button.
                // retry query button action listener should invoke:
                if(autoMove){
                 DSCluster.getInstance().makeMove(host);
                DSCluster.getInstance().retryQuery(host, version);
                }
        }
    }
}

