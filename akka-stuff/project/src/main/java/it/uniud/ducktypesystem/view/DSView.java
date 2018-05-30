package it.uniud.ducktypesystem.view;

import it.uniud.ducktypesystem.controller.DSApplication;
import it.uniud.ducktypesystem.distributed.data.*;
import it.uniud.ducktypesystem.distributed.errors.DSSystemError;
import it.uniud.ducktypesystem.controller.logger.DSAbstractLog;
import it.uniud.ducktypesystem.controller.logger.DSLog;
import org.graphstream.algorithm.generator.FlowerSnarkGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;

import static it.uniud.ducktypesystem.distributed.data.DSCluster.akkaEnvironment;


public class DSView implements DSAbstractView {
    private String defaultRobot;
    private Boolean autoMoveMOVEFAIL;
    private Boolean autoMoveCRITICALFAIL;
    private Boolean autoMoveWAITINGFAIL;
    private Integer backupMOVEFAIL;
    private Integer backupCRITICALFAIL;
    private Integer backupWAITINGFAIL;
    private ArrayList<Integer>activeHost;
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
    private Boolean betterVisualizationBool;
    private JFrame externalView;
    private JPanel externalPanel;

    public DSView(DSApplication application) {
        try {
            System.setProperty( "com.apple.mrj.application.apple.menu.about.name", "ted" );
            System.setProperty( "com.apple.macos.useScreenMenuBar", "true" );
            System.setProperty( "apple.laf.useScreenMenuBar", "true" );
            Class util = Class.forName("com.apple.eawt.Application");
            Method getApplication = util.getMethod("getApplication", new Class[0]);
            Object application2 = getApplication.invoke(util);
            Class params[] = new Class[1];
            params[0] = Image.class;
            Method setDockIconImage = util.getMethod("setDockIconImage", params);
            URL url = getClass().getResource("/DucktypeIcon.png");
            Image image = Toolkit.getDefaultToolkit().getImage(url);
            setDockIconImage.invoke(application2, image);
        } catch ( Throwable e ) {
        }
        defaultRobot="robot";
        processNumber=3;
        this.App = application;
        logger=new DSLog();
        graphCheck=false;
        queryCheck=false;
        autoMoveCRITICALFAIL=true;
        autoMoveWAITINGFAIL=true;
        autoMoveMOVEFAIL=true;
        eastPanelQuery=new JPanel();
        logScroll=new JScrollPane(logger.getLog(),
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logScroll.setPreferredSize(new Dimension(1000,150));
        logScroll.setSize(new Dimension(1000,300));
        graphPanel=new JPanel(new BorderLayout());
        autoMove=false;
        betterVisualizationBool=false;
        //Fixme: questo per consistenza dovrei aggiungerlo quando ho creato tutti i clustermanager.
        activeHost=new ArrayList<Integer>(0);
        activeHost.add(0);
        activeHost.add(1);
        activeHost.add(2);
        activeHost.add(5);activeHost.add(0);
        activeHost.add(1);
        activeHost.add(2);
        activeHost.add(5);activeHost.add(0);
        activeHost.add(1);
        activeHost.add(2);
        activeHost.add(5);
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
        JCheckBox autoMoveCB = new JCheckBox("Enable auto-move-retry");
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
        autoMoveCB.setSelected(true);
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
                mainPathField.setText(pathField.getText());
                configureSystem(graphPathString, processNumber, logger);
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
            }catch(NullPointerException error){
                error.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        " You have to choose a file description for the graph.","Error !",JOptionPane.ERROR_MESSAGE);
            }catch(DSSystemError sError){
                sError.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        " SETTINGS: I cannot read this file.\n" +
                                " Accepted extensions: DOT, DGS, GML,\" +\n" +
                                "                        \" TLP, NET, graphML, GEXF.","Error !",JOptionPane.ERROR_MESSAGE);
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
    private boolean isAutoMoveEnable(){
        return autoMoveCRITICALFAIL && autoMoveWAITINGFAIL && autoMoveMOVEFAIL;
    }
    private boolean isAutoMoveSelected(){
        return autoMove = autoMoveCRITICALFAIL && autoMoveWAITINGFAIL && autoMoveMOVEFAIL;
    }

    private void setMenuItem(){
        JMenuItem openMenuItem = new JMenuItem("Settings...");
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.addActionListener(l -> {
            JCheckBox autoMoveCB = new JCheckBox("Enable auto-move-retry");
            JButton confirmButton = new JButton("Confirm");
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            autoMoveCB.setSelected(autoMove);
            autoMoveCB.setEnabled(isAutoMoveEnable());
            backupCRITICALFAIL=facade.getCRITICALFAIL();
            backupMOVEFAIL=facade.getMOVEFAIL();
            backupWAITINGFAIL=facade.getWAITINGFAIL();
            JDialog secondFrame = new JDialog(getMainFrame());
            JPanel settingPanel = new JPanel(new FlowLayout());
            JRadioButton nullaMOVEFAIL = new JRadioButton("Disable");
            nullaMOVEFAIL.setSelected(facade.getMOVEFAIL()==1);
            nullaMOVEFAIL.addActionListener(x->{
                    autoMoveMOVEFAIL=true;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupMOVEFAIL=1;
            });
            JRadioButton nullaWAITINGFAIL = new JRadioButton("Disabled");
            nullaWAITINGFAIL.setSelected(facade.getWAITINGFAIL()==1);
            nullaWAITINGFAIL.addActionListener(x->{
                    autoMoveWAITINGFAIL=true;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupWAITINGFAIL=1;
            });
            JRadioButton nullaCRITICALFAIL = new JRadioButton("Disabled");
            nullaCRITICALFAIL.setSelected(facade.getCRITICALFAIL()==1);
            nullaCRITICALFAIL.addActionListener(x->{
                    autoMoveCRITICALFAIL=true;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                     backupCRITICALFAIL=1;
            });
            JRadioButton bassaMOVEFAIL = new JRadioButton("Low");
            bassaMOVEFAIL.setSelected(facade.getMOVEFAIL()==20);
            bassaMOVEFAIL.addActionListener(x->{
                    autoMoveMOVEFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupMOVEFAIL=20;
            });
            JRadioButton bassaWAITINGFAIL = new JRadioButton("Low");
            bassaWAITINGFAIL.setSelected(facade.getWAITINGFAIL()==20);
            bassaWAITINGFAIL.addActionListener(x->{
                    autoMoveWAITINGFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupWAITINGFAIL=20;
            });
            JRadioButton bassaCRITICALFAIL = new JRadioButton("Low");
            bassaCRITICALFAIL.setSelected(facade.getCRITICALFAIL()==20);
            bassaCRITICALFAIL.addActionListener(x->{
                    autoMoveCRITICALFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupCRITICALFAIL=20;

            });
            JRadioButton altaMOVEFAIL = new JRadioButton("High");
            altaMOVEFAIL.setSelected(facade.getMOVEFAIL()==2);
           altaMOVEFAIL.addActionListener(x->{
                    autoMoveMOVEFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupMOVEFAIL=2;
            });
            JRadioButton altaWAITINGFAIL = new JRadioButton("High");
            altaWAITINGFAIL.setSelected(facade.getWAITINGFAIL()==2);
            altaWAITINGFAIL.addActionListener(x->{
                    autoMoveWAITINGFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupWAITINGFAIL=2;
            });
            JRadioButton altaCRITICALFAIL = new JRadioButton("High");
            altaCRITICALFAIL.setSelected(facade.getCRITICALFAIL()==2);
            altaCRITICALFAIL.addActionListener(x->{
                    autoMoveCRITICALFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupCRITICALFAIL=2;

            });
            JRadioButton mediaMOVEFAIL = new JRadioButton("Medium");
            mediaMOVEFAIL.setSelected(facade.getMOVEFAIL()==5);
            mediaMOVEFAIL.addActionListener(x->{

                    autoMoveMOVEFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                   backupMOVEFAIL=5;

            });
            JRadioButton mediaWAITINGFAIL = new JRadioButton("Medium");
            mediaWAITINGFAIL.setSelected(facade.getWAITINGFAIL()==5);
            mediaWAITINGFAIL.addActionListener(x->{
                    autoMoveWAITINGFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupWAITINGFAIL=5;
            });
            JRadioButton mediaCRITICALFAIL = new JRadioButton("Medium");
            mediaCRITICALFAIL.setSelected(facade.getCRITICALFAIL()==5);
            mediaCRITICALFAIL.addActionListener(x->{
                    autoMoveCRITICALFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupCRITICALFAIL=5;
            });
            ButtonGroup CRITICALFAIL = new ButtonGroup();
            ButtonGroup WAITINGFAIL = new ButtonGroup();
            ButtonGroup MOVEFAIL = new ButtonGroup();
            CRITICALFAIL.add(nullaCRITICALFAIL);
            CRITICALFAIL.add(bassaCRITICALFAIL);
            CRITICALFAIL.add(mediaCRITICALFAIL);
            CRITICALFAIL.add(altaCRITICALFAIL);
            WAITINGFAIL.add(nullaWAITINGFAIL);
            WAITINGFAIL.add(bassaWAITINGFAIL);
            WAITINGFAIL.add(mediaWAITINGFAIL);
            WAITINGFAIL.add(altaWAITINGFAIL);
            MOVEFAIL.add(nullaMOVEFAIL);
            MOVEFAIL.add(bassaMOVEFAIL);
            MOVEFAIL.add(mediaMOVEFAIL);
            MOVEFAIL.add(altaMOVEFAIL);
            JPanel radioPanelCritical = new JPanel(new GridLayout(0, 1));
            JLabel criticalLabel = new JLabel("  CriticalFail");
            criticalLabel.setFont(new Font("Bariol", Font.PLAIN,15));
            criticalLabel.setForeground(Color.DARK_GRAY);
            radioPanelCritical.add(criticalLabel);
            radioPanelCritical.add(nullaCRITICALFAIL);
            radioPanelCritical.add(bassaCRITICALFAIL);
            radioPanelCritical.add(mediaCRITICALFAIL);
            radioPanelCritical.add(altaCRITICALFAIL);
            JPanel radioPanelMoving = new JPanel(new GridLayout(0, 1));
            JLabel movingLabel = new JLabel("  RobotFail");
            movingLabel.setFont(new Font("Bariol", Font.PLAIN,15));
            movingLabel.setForeground(Color.DARK_GRAY);
            radioPanelMoving.add(movingLabel);
            radioPanelMoving.add(nullaMOVEFAIL);
            radioPanelMoving.add(bassaMOVEFAIL);
            radioPanelMoving.add(mediaMOVEFAIL);
            radioPanelMoving.add(altaMOVEFAIL);
            JPanel radioPanelWaiting = new JPanel(new GridLayout(0, 1));
            JLabel waitingLabel = new JLabel("  WaitingFail");
            waitingLabel.setFont(new Font("Bariol", Font.PLAIN,15));
            waitingLabel.setForeground(Color.DARK_GRAY);
            radioPanelWaiting.add(waitingLabel);
            radioPanelWaiting.add(nullaWAITINGFAIL);
            radioPanelWaiting.add(bassaWAITINGFAIL);
            radioPanelWaiting.add(mediaWAITINGFAIL);
            radioPanelWaiting.add(altaWAITINGFAIL);
            settingPanel.add(radioPanelCritical);
            settingPanel.add(radioPanelMoving);
            settingPanel.add(radioPanelWaiting);

            settingPanel.add(autoMoveCB);
            URL url = getClass().getResource("/DSInfo.png");
            Image image = Toolkit.getDefaultToolkit().getImage(url);
            ImageIcon icon = new ImageIcon(image);
            JLabel InfoLabel = new JLabel(icon, JLabel.CENTER);
            InfoLabel.setText("<html> <i>High, medium</i> and <i>low</i> are the probability that a fail in <br>" +
                    "CRITICAL-FAIL, MOVE-FAIL or WAITING-FAIL can occur.<br><br>" +
                    "When one of this failure is enabled<br>" +
                    "the auto-move-retry is automatically <b>turned off</b>. </html>");
            settingPanel.add(InfoLabel);
            settingPanel.add(confirmButton);

            secondFrame.getContentPane().add(settingPanel);
            secondFrame.setTitle("Settings");
            secondFrame.setBounds(0, 0, 400, 300);
            secondFrame.setLocation(dim.width/2-secondFrame.getSize().width/2, dim.height/2-secondFrame.getSize().height/2);
            secondFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            secondFrame.setVisible(true);
            secondFrame.setResizable(false);
            confirmButton.addActionListener(e -> {
                facade.setCRITICALFAIL(backupCRITICALFAIL);
                facade.setMOVEFAIL(backupMOVEFAIL);
                facade.setWAITINGFAIL(backupWAITINGFAIL);
                autoMove=autoMoveCB.isSelected();
                secondFrame.dispose();
                refreshButton();
                    }
            );

            secondFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent evento) {
                    if(JOptionPane.showConfirmDialog(secondFrame,
                            "Do you really want to exit from settings ?\nYou will lost all the modify.",
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
        JMenu Theme = new JMenu("Robot style");
        JMenuItem bender = new JMenuItem("Bender",new ImageIcon(getClass().getResource("/DSBenderLittle.png")));
        bender.addActionListener(x->{defaultRobot="bender";updateRobotsPosition();});
        JMenuItem robot = new JMenuItem("Robot",new ImageIcon(getClass().getResource("/DSRobotLittle.png")));
        robot.addActionListener(x->{defaultRobot="robot";updateRobotsPosition();});
        JMenuItem r2d2 = new JMenuItem("R2D2",new ImageIcon(getClass().getResource("/DSLittleR2d2.png")));
        r2d2.addActionListener(x->{defaultRobot="r2d2";updateRobotsPosition();});
        JMenuItem duck = new JMenuItem("Duck",new ImageIcon(getClass().getResource("/DSDuckLittle.png")));
        duck.addActionListener(x->{defaultRobot="duck";updateRobotsPosition();});
        JMenuItem Halo = new JMenuItem("Halo",new ImageIcon(getClass().getResource("/DSHaloLittle.png")));
        Halo.addActionListener(x->{defaultRobot="halo";updateRobotsPosition();});
        Theme.add(bender);
        Theme.add(robot);
        Theme.add(r2d2);
        Theme.add(duck);
        Theme.add(Halo);
        menuFile.add(Theme);
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
        mainFrame.setAlwaysOnTop(true);
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
        scrollForQuery.setPreferredSize(new Dimension(500,500));
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
        mainFrame.setBounds(20, 00, 1200, 750);
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
                DSCluster.getInstance().startNewComputation(0, newQuery);
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
            //FixMe: da aggiungere la storia degli host
           // hostManager();
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
            }catch(DSSystemError sError){
                showErrorMessage("SETTINGS: I cannot read this file. Accepted extensions: DOT, DGS, GML," +
                        " TLP, NET, graphML, GEXF.");
                queryField.setText("");
            }
        });
    }

    private void hostManager(){
        JDialog secondFrame = new JDialog(getMainFrame());
        JPanel hostPanel= new JPanel(new BorderLayout());
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        JList list = new JList(activeHost.toArray());
        JPanel centralPanel = new JPanel(new BorderLayout());
        JScrollPane listScroller = new JScrollPane(list);
        JButton addHost= new JButton("Connect new host");
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(-1);
        listScroller.setPreferredSize(new Dimension(300, 200));
        hostPanel.add(addHost,BorderLayout.NORTH);
        centralPanel.add(listScroller, BorderLayout.CENTER);
        hostPanel.add(centralPanel, BorderLayout.CENTER);

        secondFrame.getContentPane().add(hostPanel);
        secondFrame.setBounds(0, 0, 300, 400);
        secondFrame.setLocation(dim.width/2-secondFrame.getSize().width/2, dim.height/2-secondFrame.getSize().height/2);
        secondFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        secondFrame.setVisible(true);
        secondFrame.setTitle("Select host");
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
                node.addAttribute("ui.class", defaultRobot);
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
    private void configureSystem(String filePath, int numRobot, DSAbstractLog log) throws DSSystemError {
        facade=DataFacade.create(filePath);
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
                node.addAttribute("ui.class", defaultRobot);
            else
                node.addAttribute("ui.class", "");
        }
        graphPanel.updateUI();
    }
    private void refreshButton(){
        // FIXME for all hosts -- for each ( active queries) should be ok..
        DSCluster.getInstance().getActiveQueries(0).forEach((mapVersionTmp, mapWrapperTmp) -> {
            for (Component c : eastPanelQuery.getComponents()) {
                if (c instanceof JPanel && c.getName().equals(mapVersionTmp)) {
                    for(Component e: ((JPanel) c).getComponents())
                        if(e instanceof  JButton) {
                            e.setVisible(!autoMove);
                            if(autoMove) {
                                e.setEnabled(false);
                                if(!((DSQueryResult) mapWrapperTmp).getStillToVerify().equals("\n")) {
                                    DSQuery.QueryId id = ((DSQueryResult) mapWrapperTmp).getQuery().getId();
                                    DSCluster.getInstance().retryQuery(id);
                                }
                            }
                        }
                }
            }
        });
    }
    private void refreshQuery(DSQuery.QueryId qId, DSQuery.QueryStatus status) {
        // FIXME: here 0 stands for `host' parameter
        String version = qId.getVersion();
        int host = qId.getHost();
        DSCluster.getInstance().getActiveQueries(0).forEach((mapVersionTmp, mapWrapperTmp) -> {
            Boolean find = false;
            DSQueryResult mapWrapper = (DSQueryResult) mapWrapperTmp;
            String mapVersion = (String) mapVersionTmp;
            String labelText = mapVersion;
            if (version.equals(mapVersion)) {
                JPanel aglomeratePanel = new JPanel(new BorderLayout());
                aglomeratePanel.setPreferredSize(new Dimension(300, 520));
                JPanel twoQueryStatusPanel = new JPanel();

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
                for (Component c : eastPanelQuery.getComponents()) {
                    if (c instanceof JPanel && c.getName().equals(mapVersion)) { //FIXME: Rossi G. ... Perdoname por mi vida loca !
                        find = true;
                        for (Component d : ((JPanel) c).getComponents()) {
                            if (d instanceof JPanel && d.getName().equals(mapVersion)) {
                                int i = 0;
                                for (Component e : ((JPanel) d).getComponents()) {
                                    ++i;
                                    if (e.getName() != null && !mapWrapper.getStillToVerify().equals("\n") && status!=DSQuery.QueryStatus.FAIL) {
                                        twoQueryStatusPanel.setLayout(new GridLayout(0, 2));
                                        ((JPanel) d).remove(e);
                                        JPanel tmp = queryVisualization(DSGraphImpl.createFromSerializedString(mapWrapper.getStillToVerify()));
                                        JPanel northPanelForBetterVisualization = new JPanel(new BorderLayout());
                                        JPanel reallyNorthPanel = new JPanel(new BorderLayout());
                                        JLabel statusLable= new JLabel("Still to verify...");
                                        URL url = getClass().getResource("/apri.png");
                                        JButton betterVisualization = new JButton(new ImageIcon(url));
                                        betterVisualization.setUI(new BasicButtonUI());
                                        betterVisualization.setContentAreaFilled(false);
                                        betterVisualization.setOpaque(true);
                                        betterVisualization.setBackground(Color.DARK_GRAY);
                                        betterVisualization.addActionListener(x->{
                                            try {
                                                if (betterVisualizationBool)throw new DSSystemError("You already have one query open in SUPER-DUPER-VISUALIZATION.\n" +
                                                        "Close it before open another one.");
                                                else{
                                                    betterVisualizationBool=true;
                                                    externalPanel = eastPanelQuery;
                                                    mainFrame.remove(scrollForQuery);
                                                    externalView = new JFrame();
                                                    JScrollPane www = scrollForQuery;
                                                    externalView.setContentPane(www);
                                                    externalView.setSize(new Dimension(900,700));
                                                    externalView.setName("Query:" + mapVersion);
                                                    mainPanel.updateUI();
                                                    externalView.setVisible(true);
                                                    externalView.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                                                    externalView.addWindowListener(new WindowAdapter() {
                                                        @Override
                                                        public void windowClosing(WindowEvent evento) {
                                                            mainPanel.add(scrollForQuery, BorderLayout.EAST);
                                                            betterVisualizationBool=false;
                                                            externalView.dispose();
                                                            mainPanel.updateUI();
                                                        }
                                                    });
                                                }
                                            }catch(DSSystemError error){
                                                JOptionPane.showMessageDialog(null,
                                                        error.getMessage(),"Error !",JOptionPane.ERROR_MESSAGE);
                                            }
                                        });
                                        statusLable.setForeground(Color.WHITE);
                                        statusLable.setFont(new Font("Bariol", Font.PLAIN, 20));
                                        reallyNorthPanel.add(statusLable, BorderLayout.CENTER);
                                        if(!betterVisualizationBool)
                                            reallyNorthPanel.add(betterVisualization, BorderLayout.EAST);
                                        northPanelForBetterVisualization.add(reallyNorthPanel,BorderLayout.NORTH);
                                        reallyNorthPanel.setBackground(Color.DARK_GRAY);
                                        northPanelForBetterVisualization.add(tmp, BorderLayout.CENTER);
                                        tmp.setName(mapVersion);
                                        northPanelForBetterVisualization.setName(mapVersion);
                                        northPanelForBetterVisualization.setBorder(new MatteBorder(2, 0, 0, 0, Color.BLACK));
                                        ((JPanel) d).add(northPanelForBetterVisualization);
                                    }
                                    if (e.getName() != null && status != DSQuery.QueryStatus.DONTKNOW) {
                                        twoQueryStatusPanel.setLayout(new GridLayout(0, 1));
                                        ((JPanel) d).remove(e);
                                    }
                                }
                                if (i < 2 && !mapWrapper.getStillToVerify().equals("\n") && status!=DSQuery.QueryStatus.FAIL) {
                                    JPanel tmp = queryVisualization(DSGraphImpl.createFromSerializedString(mapWrapper.getStillToVerify()));
                                    JPanel northPanelForBetterVisualization = new JPanel(new BorderLayout());
                                    JPanel reallyNorthPanel = new JPanel(new BorderLayout());
                                    JLabel statusLable= new JLabel("Still to verify...");
                                    URL url = getClass().getResource("/apri.png");
                                    JButton betterVisualization = new JButton(new ImageIcon(url));
                                    betterVisualization.setUI(new BasicButtonUI());
                                    betterVisualization.setContentAreaFilled(false);
                                    betterVisualization.setOpaque(true);
                                    betterVisualization.setBackground(Color.DARK_GRAY);
                                    betterVisualization.addActionListener(x->{
                                        try {
                                            if (betterVisualizationBool)throw new DSSystemError("You already have one query open in SUPER-DUPER-VISUALIZATION.\n" +
                                                    "Close it before open another one.");
                                            else{
                                                betterVisualizationBool=true;
                                                externalPanel = eastPanelQuery;
                                                mainFrame.remove(scrollForQuery);
                                                externalView = new JFrame();
                                                JScrollPane www = scrollForQuery;
                                                externalView.setContentPane(www);
                                                externalView.setSize(new Dimension(900,700));
                                                externalView.setName("Query:" + mapVersion);
                                                mainPanel.updateUI();
                                                externalView.setVisible(true);
                                                externalView.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                                                externalView.addWindowListener(new WindowAdapter() {
                                                    @Override
                                                    public void windowClosing(WindowEvent evento) {
                                                        mainPanel.add(scrollForQuery, BorderLayout.EAST);
                                                        betterVisualizationBool=false;
                                                        externalView.dispose();
                                                        mainPanel.updateUI();
                                                    }
                                                });
                                            }

                                        }catch(DSSystemError error){
                                            JOptionPane.showMessageDialog(null,
                                                    error.getMessage(),"Error !",JOptionPane.ERROR_MESSAGE);
                                        }

                                    });
                                    statusLable.setForeground(Color.WHITE);
                                    statusLable.setFont(new Font("Bariol", Font.PLAIN, 20));
                                    reallyNorthPanel.add(statusLable,BorderLayout.CENTER);
                                    if(!betterVisualizationBool)
                                        reallyNorthPanel.add(betterVisualization, BorderLayout.EAST);
                                    northPanelForBetterVisualization.add(reallyNorthPanel,BorderLayout.NORTH);
                                    northPanelForBetterVisualization.add(tmp, BorderLayout.CENTER);
                                    reallyNorthPanel.setBackground(Color.DARK_GRAY);
                                    northPanelForBetterVisualization.setName(mapVersion);
                                    northPanelForBetterVisualization.setBorder(new MatteBorder(2, 0, 0, 0, Color.BLACK));
                                    ((JPanel) d).add(northPanelForBetterVisualization);
                                }
                            }
                            if (d instanceof JPanel && d.getName().equals("northPanel")) {
                                showInformationMessage(d.getName());
                                for(Component e : ((JPanel) d).getComponents()) {
                                    if(e instanceof  JLabel)
                                    ((JLabel) e).setText(labelText);
                                    e.setForeground(labelColor);
                                }
                            }
                        }
                    }
                }
                if (find == false) {
                    JButton retry = new JButton("Retry");
                    retry.addActionListener(e ->{
                                retry.setEnabled(false);
                                DSCluster.getInstance().retryQuery(qId);
                            }
                    );
                    retry.setVisible(!autoMove);
                    aglomeratePanel.add(retry, BorderLayout.SOUTH);
                    twoQueryStatusPanel.setLayout(new GridLayout(0, 1));
                    aglomeratePanel.setName(mapVersion);
                    twoQueryStatusPanel.setName(mapVersion);
                    twoQueryStatusPanel.add(queryVisualization(mapWrapper.getQuery()));
                    aglomeratePanel.setBackground(Color.DARK_GRAY);
                    JPanel northPanelWithLabelAndClosure = new JPanel(new BorderLayout());
                    URL url = getClass().getResource("/DSClose.png");
                    Image image = Toolkit.getDefaultToolkit().getImage(url);
                    ImageIcon icon = new ImageIcon(image);
                    JButton killQuery = new JButton(icon);
                    killQuery.setUI(new BasicButtonUI());
                    killQuery.setContentAreaFilled(false);
                    killQuery.setOpaque(true);
                    killQuery.setBackground(Color.DARK_GRAY);
                    killQuery.setForeground(Color.DARK_GRAY);
                    url = getClass().getResource("/DSPause.png");
                    image = Toolkit.getDefaultToolkit().getImage(url);
                    icon = new ImageIcon(image);
                    JButton playAndPause = new JButton(icon);
                    playAndPause.setUI(new BasicButtonUI());
                    playAndPause.setContentAreaFilled(false);
                    playAndPause.setBackground(Color.DARK_GRAY);
                    playAndPause.setName("play");
                    playAndPause.setOpaque(true);
                    northPanelWithLabelAndClosure.add(playAndPause, BorderLayout.WEST);
                    northPanelWithLabelAndClosure.setName("northPanel");
                    playAndPause.addActionListener(e->{
                        if(playAndPause.getName().equals("play")){
                            URL tmpUrl = getClass().getResource("/DSPlay.png");
                            Image tmpImage = Toolkit.getDefaultToolkit().getImage(tmpUrl);
                            ImageIcon tmpIcon = new ImageIcon(tmpImage);
                            playAndPause.setIcon(tmpIcon);
                            playAndPause.setName("pause");
                            DSCluster.getInstance().temporaryQueryStop(qId);
                        }else{
                            URL tmpUrl = getClass().getResource("/DSPause.png");
                            Image tmpImage = Toolkit.getDefaultToolkit().getImage(tmpUrl);
                            ImageIcon tmpIcon = new ImageIcon(tmpImage);
                            playAndPause.setIcon(tmpIcon);
                            playAndPause.setName("play");
                            if(status!=DSQuery.QueryStatus.DONTKNOW){
                                DSCluster.getInstance().retryQuery(qId);
                            }
                        }

                    });
                    killQuery.setBackground(Color.DARK_GRAY);
                    killQuery.setForeground(Color.DARK_GRAY);
                    killQuery.addActionListener(e->{
                        //FIXME: inviare messaggio a tutti dicendo di stoppare questa query
                        mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        eastPanelQuery.remove(aglomeratePanel);
                        mainFrame.setCursor(Cursor.getDefaultCursor());
                        if(betterVisualizationBool)externalPanel.updateUI();
                        else mainPanel.updateUI();
                        DSCluster.getInstance().killQuery(qId);
                    });
                    northPanelWithLabelAndClosure.add(queryNameLbl, BorderLayout.CENTER);
                    northPanelWithLabelAndClosure.add(killQuery, BorderLayout.EAST);
                    northPanelWithLabelAndClosure.setBackground(Color.DARK_GRAY);
                    aglomeratePanel.add(northPanelWithLabelAndClosure, BorderLayout.NORTH);

                    aglomeratePanel.add(twoQueryStatusPanel, BorderLayout.CENTER);
                    aglomeratePanel.setBorder(new MatteBorder(0, 0, 2, 0, Color.WHITE));
                    eastPanelQuery.add(aglomeratePanel);
                }
            }
            eastPanelQuery.add(Box.createVerticalGlue());
        });
        if(betterVisualizationBool)externalPanel.updateUI();
        else mainPanel.updateUI();
    }
    @Override
    public void updateQuery(DSQuery.QueryId qId, DSQuery.QueryStatus status) {
        // FIXME: update the correct host view.
        String version = qId.getVersion();
        int host = qId.getHost();
        refreshQuery(qId, status);
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
                if(autoMove){
                 DSCluster.getInstance().makeMove(host);
                DSCluster.getInstance().retryQuery(qId);
                }
        }
    }
}