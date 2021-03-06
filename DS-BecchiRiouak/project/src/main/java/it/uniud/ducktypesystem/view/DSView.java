package it.uniud.ducktypesystem.view;

import it.uniud.ducktypesystem.DucktypeSystem;
import it.uniud.ducktypesystem.controller.DSApplication;
import it.uniud.ducktypesystem.distributed.data.*;
import it.uniud.ducktypesystem.distributed.errors.DSSystemError;
import it.uniud.ducktypesystem.controller.logger.DSAbstractLog;
import it.uniud.ducktypesystem.controller.logger.DSLog;
import it.uniud.ducktypesystem.distributed.system.DSCluster;
import it.uniud.ducktypesystem.distributed.system.DSDataFacade;
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
import java.lang.reflect.Proxy;
import java.net.URL;

import static it.uniud.ducktypesystem.distributed.system.DSCluster.akkaEnvironment;

/**
 * DSGView:
 * Implementation of the DSAbstractView Inteface.
 * Provide all the methods for the UI visualization except for the logger.
 */

public class DSView implements DSAbstractView {
    private Color greenForest= new Color(11,102,35);
    private Color pink= new Color(255, 102, 255);
    private Boolean autoMoveMOVEFAIL;
    private Boolean autoMoveCRITICALFAIL;
    private Boolean autoMoveWAITINGFAIL;
    private Integer backupMOVEFAIL;
    private Integer backupCRITICALFAIL;
    private Integer backupWAITINGFAIL;
    private Integer lastHost;
    private String defaultRobot;
    private DefaultListModel<Integer> activeHost;
    private JFrame mainFrame;
    private JButton queryButton;
    private DSApplication App;
    private DSAbstractLog logger;
    private JScrollPane logScroll;
    private JTextField queryField;
    private JPanel graphPanel;
    private ViewPanel graphView;
    private DSDataFacade facade;
    private Integer processNumber;
    private Graph graph;
    private Viewer viewer;
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
        //Intialization of all the components of the view.
        try {
            {
                try
                {
                    Class<?> clazz = Class.forName("com.apple.eawt.Application");
                    Method getApplication = clazz.getMethod("getApplication");
                    Object appli = getApplication.invoke(null);
                    Class<?> abouthandlerclass = Class.forName("com.apple.eawt.AboutHandler");
                    Method setAboutHandler = clazz.getMethod("setAboutHandler", abouthandlerclass);
                    Object abouthandler = Proxy.newProxyInstance(DucktypeSystem.class.getClassLoader(),
                            new Class<?>[] { abouthandlerclass }, (proxy, method, args) -> {
                                if (method.getName().equals("handleAbout"))
                                    aboutUsOnlyForMac();
                                return null;
                            });
                    setAboutHandler.invoke(appli, abouthandler);
                }
                catch (Exception e) {}
            }
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DucktypeSystem");
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
            //Empty catch. Not a macOSX
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
        activeHost= new DefaultListModel<>();
        activeHost.addElement(0);
    }

    //Main method that is called from the DSApplication.
    @Override
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


    //Mathod that handle the About in MacOS.
    private void aboutUsOnlyForMac(){
        JPanel welcomeMainPanel=new JPanel(new BorderLayout());
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        JLabel DucktypeSystemLbl= new JLabel("<html><center><p color=\"orange\">Anna Becchi - Idriss Riouak.<br> A.A: 2017-2018</p>  <br> Università degli Studi di Udine<br>DucktypeSystem v. 0.1</center></html>");
        JFrame aboutFrame = new JFrame();
        JLabel mallardLbl= new JLabel("<html><center> \"If it looks like a duck, swims like a duck,<br> and quacks like a duck, then it probably is a duck.\" <br> Just a Ducktype System...</center>  </html>");
        aboutFrame.setAlwaysOnTop(true);
        URL url = getClass().getResource("/JavaDuck.png");
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        welcomeMainPanel.add(new JLabel(new ImageIcon(image)), BorderLayout.CENTER);
        mallardLbl.setSize(200, 200);
        mallardLbl.setFont(new Font("Bariol", Font.PLAIN,20));
        mallardLbl.setForeground(Color.WHITE);
        mallardLbl.setHorizontalAlignment(SwingConstants.CENTER);
        DucktypeSystemLbl.setSize(200, 200);
        DucktypeSystemLbl.setFont(new Font("Bariol", Font.PLAIN,30));
        DucktypeSystemLbl.setForeground(Color.WHITE);
        DucktypeSystemLbl.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeMainPanel.add(mallardLbl, BorderLayout.NORTH);
        welcomeMainPanel.setBackground(Color.BLACK);
        welcomeMainPanel.add(DucktypeSystemLbl, BorderLayout.SOUTH);
        aboutFrame.getContentPane().add(welcomeMainPanel);
        aboutFrame.setTitle("All right reserved.");
        aboutFrame.setBounds(0, 0, 700, 500);
        aboutFrame.setLocation(dim.width/2-aboutFrame.getSize().width/2, dim.height/2-aboutFrame.getSize().height/2);
        aboutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        aboutFrame.setVisible(true);
    }



    /*
    This function handle the initial configuration of the application such:
        * Graph path with relative control of the correctness of the graph.
        * Number of active robots
        * If the auto-move is enable.
     */

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
                if(processNumber==0)
                    throw new NumberFormatException();
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



    /*
    This metohod set the menu Item such:
        * Setting: allow to select the failure level of the program.
        * Robot Style: allow to select a different skin for the robot
        * Exit: a shortcut for terminating the application.
     */
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
            altaMOVEFAIL.setSelected(facade.getMOVEFAIL()==5);
           altaMOVEFAIL.addActionListener(x->{
                    autoMoveMOVEFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupMOVEFAIL=5;
            });
            JRadioButton altaWAITINGFAIL = new JRadioButton("High");
            altaWAITINGFAIL.setSelected(facade.getWAITINGFAIL()==5);
            altaWAITINGFAIL.addActionListener(x->{
                    autoMoveWAITINGFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupWAITINGFAIL=5;
            });
            JRadioButton altaCRITICALFAIL = new JRadioButton("High");
            altaCRITICALFAIL.setSelected(facade.getCRITICALFAIL()==5);
            altaCRITICALFAIL.addActionListener(x->{
                    autoMoveCRITICALFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupCRITICALFAIL=5;

            });
            JRadioButton mediaMOVEFAIL = new JRadioButton("Medium");
            mediaMOVEFAIL.setSelected(facade.getMOVEFAIL()==10);
            mediaMOVEFAIL.addActionListener(x->{

                    autoMoveMOVEFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                   backupMOVEFAIL=10;

            });
            JRadioButton mediaWAITINGFAIL = new JRadioButton("Medium");
            mediaWAITINGFAIL.setSelected(facade.getWAITINGFAIL()==10);
            mediaWAITINGFAIL.addActionListener(x->{
                    autoMoveWAITINGFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupWAITINGFAIL=10;
            });
            JRadioButton mediaCRITICALFAIL = new JRadioButton("Medium");
            mediaCRITICALFAIL.setSelected(facade.getCRITICALFAIL()==10);
            mediaCRITICALFAIL.addActionListener(x->{
                    autoMoveCRITICALFAIL=false;
                    autoMoveCB.setEnabled(isAutoMoveEnable());
                    autoMoveCB.setSelected(isAutoMoveSelected());
                    backupCRITICALFAIL=10;
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
                    "<center>When one of this failure is enabled<br>" +
                    "the auto-move-retry is automatically <b>turned off</b>.<br><br><br><p color=\"red\"> Be sure <b>not to have active queries when changing settings</b>.<br>" +
                    "If you have some queries that are being verified, <br>" +
                    "stop them before confirming new failure parameters.</p><br><br></center> </html>");
            settingPanel.add(InfoLabel);
            settingPanel.add(confirmButton);

            secondFrame.getContentPane().add(settingPanel);
            secondFrame.setTitle("Settings");
            secondFrame.setBounds(0, 0, 450, 400);
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
        mainFrame.setTitle("A distributed subgraph isomorphism implementation");
        mainFrame.setUndecorated(true);
        mainFrame.setBounds(0, 0, 700, 500);
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

    //This function render the main view application:
    private void initMainFrame(){
        mainPanel=new JPanel(new BorderLayout());
        JPanel southPanel=new JPanel(new BorderLayout());
        JPanel northPanel=new JPanel(new BorderLayout());
        JLabel pathLbl =new JLabel("Graph path:");
        queryButton = new JButton("Query source");
        queryField = new JTextField();
        JPanel soutWithStartPanel = new JPanel(new BorderLayout());
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        JPanel southWithScroll= new JPanel(new BorderLayout());
        JButton moveRobot = new JButton("Move robot");
        eastPanelQuery=new JPanel();
        eastPanelQuery.setLayout(new BoxLayout(eastPanelQuery, BoxLayout.Y_AXIS));
        scrollForQuery=new JScrollPane(eastPanelQuery,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollForQuery.setPreferredSize(new Dimension(500,500));
        startNewComputation = new JButton("START");
        startNewComputation.setForeground(greenForest);
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
                DSCluster.getInstance().makeMove(0);
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
                DSCluster.getInstance().startNewComputation(lastHost, newQuery);
                setQueryCheck(false);
                startNewComputation.setEnabled(isStartEnable());
                updateQuery(newQuery.getId(), DSQuery.QueryStatus.NEW);
                JScrollBar vertical = scrollForQuery.getVerticalScrollBar();
                vertical.setValue( vertical.getMaximum() );
            });
            thread.start();
        });
        queryButton.addActionListener(e -> {
            queryButton.setEnabled(false);
            hostManager();

        });
    }

    /*
    hostManager(): funciton that provide a little UI for
     connecting and selecting a new host which will submit the query
     */

    private void hostManager(){
        JDialog hostManagerFrame = new JDialog(getMainFrame());
        JPanel hostPanel= new JPanel(new BorderLayout());
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        JList list = new JList(activeHost);
        list.setFont(new Font("Bariol", Font.PLAIN , 50));
        JPanel centralPanel = new JPanel(new BorderLayout());
        JScrollPane listScroller = new JScrollPane(list);
        JButton addHost= new JButton("Connect new host");
        addHost.addActionListener(x->{
            hostManagerFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            activeHost.addElement(DSCluster.getInstance().connectNewHost());

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hostManagerFrame.setCursor(Cursor.getDefaultCursor());
        });
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) {
                    try {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.showOpenDialog(mainFrame);
                        queryField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                        graphPathString = queryField.getText();
                        setQueryCheck(true);
                        startNewComputation.setEnabled(isStartEnable());
                        lastHost = (Integer)list.getSelectedValue();
                        newQuery = DSQueryImpl.createQueryFromFile(graphPathString, lastHost);
                        showInformationMessage("SETTINGS: Graph reading complete.");
                        hostManagerFrame.dispose();
                        queryButton.setEnabled(true);
                    } catch (NullPointerException error) {
                        error.printStackTrace();
                        showErrorMessage("SETTINGS: You have to choose a file description for the graph.");
                        lastHost = null;
                    } catch (DSSystemError sError) {
                        sError.printStackTrace();
                        showErrorMessage("SETTINGS: I cannot read this file. Accepted extensions: DOT, DGS, GML," +
                                " TLP, NET, graphML, GEXF.");
                        queryField.setText("");
                        lastHost = null;
                    }
                }
            }
        });
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL_WRAP);
        list.setVisibleRowCount(-1);
        listScroller.setPreferredSize(new Dimension(300, 200));
        hostPanel.add(addHost,BorderLayout.NORTH);
        centralPanel.add(listScroller, BorderLayout.CENTER);
        URL url = getClass().getResource("/DSInfo.png");
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        ImageIcon icon = new ImageIcon(image);
        JLabel InfoLabel = new JLabel(icon, JLabel.CENTER);
        InfoLabel.setText("<html>Select one <b>host</b><br>" +
                          "which will submit the query.</html>");
        centralPanel.add(InfoLabel,BorderLayout.SOUTH);
        hostPanel.add(centralPanel, BorderLayout.CENTER);
        //hostPanel.add(querySelection, BorderLayout.SOUTH);
        hostManagerFrame.getContentPane().add(hostPanel);
        hostManagerFrame.setBounds(0, 0, 300, 400);
        hostManagerFrame.setLocation(dim.width/2-hostManagerFrame.getSize().width/2, dim.height/2-hostManagerFrame.getSize().height/2);
        hostManagerFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        hostManagerFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                queryButton.setEnabled(true);
                hostManagerFrame.dispose();
            }
        });
        hostManagerFrame.setVisible(true);
        hostManagerFrame.setTitle("Select host...");
    }

    /*
    queryVisualization(): this function allow the system to
    refesh the status of the submitted query.
     */
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
    /*
    graphVisualization(): this function allow to render the
    main query.
 */
    private void graphVisualization(DSGraph x){
        graph =(Graph) x.getGraphImpl();
        graph.setStrict(false);
        graph.setAutoCreate( true );
        graph.addAttribute("ui.stylesheet", "url(nodeStyle.css)");
        for (Node node : graph) {
            node.addAttribute("ui.label", node.getId());
            if(facade.getOccupied().contains(node.toString()))
                node.addAttribute("ui.class", defaultRobot);
            else {
                if(node.toString().equals("Parma")) node.addAttribute("ui.class", "parma");
                else if(node.toString().equals("Udine")) node.addAttribute("ui.class", "udine");
                else node.addAttribute("ui.class", "normal");
            }
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
    /*
    * Whit configureSystem() function we pass all the parameter
     * that we have collected in the view to the DSDataFacade.
    * */
    private void configureSystem(String filePath, int numRobot, DSAbstractLog log) throws DSSystemError {
        facade=DSDataFacade.create(filePath);
        facade.setOccupied(numRobot);
        StringBuilder b = new StringBuilder();
        b.append("Robot positioned in: ");
        for (String s : facade.getOccupied())
            b.append(s + " ");
        showInformationMessage(b.toString());
    }

    private boolean isAutoMoveEnable(){
        return autoMoveCRITICALFAIL && autoMoveWAITINGFAIL && autoMoveMOVEFAIL;
    }
    private boolean isAutoMoveSelected(){
        return autoMove = autoMoveCRITICALFAIL && autoMoveWAITINGFAIL && autoMoveMOVEFAIL;
    }
    private void setGraphCheck(Boolean b){graphCheck=b;}
    private void setQueryCheck(Boolean b){queryCheck=b;}
    private Boolean getGraphCheck(){return graphCheck;}
    private Boolean getQueryCheck(){return queryCheck;}
    private Boolean isStartEnable(){return getGraphCheck() && getQueryCheck();}

    /**
     * Those method:
     *      *showInforamtionMessage
     *      *showErrorMessage
     *      *showQueryStatus
     * allow the DSView to interface with the logger.
     */
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
    public void showQueryStatus( DSQuery.QueryStatus status, String name,int host){
        switch(status) {
            case MATCH:
                logger.log("Host < " +host +" >: Query  < " + name + " > ended: MATCH!", Color.BLUE);
                break;
            case FAIL:
                logger.log("Host < " +host +" >: Query < " + name + " > ended: FAIL!", Color.red);
                break;
            case NEW:
                logger.log("Host < "+ host+" >: Started the computation of a new query: < " + name +" >" , pink);
                break;
            default:
                logger.log("Host < "+ host +" >: Query < "+name+" > ended: DONTKNOW!",Color.ORANGE );
        }
        JScrollBar vertical = logScroll.getVerticalScrollBar();
        try {
            vertical.setValue(vertical.getMaximum());
        }catch(Throwable e){
           // e.printStackTrace();
        }
    }
    public JFrame getMainFrame(){return mainFrame;}

    @Override
    public void updateRobotsPosition() {
        StringBuilder b = new StringBuilder();
        b.append("Robot positioned in: ");
        for (String s : facade.getOccupied())
            b.append(s + " ");
        showInformationMessage(b.toString());
        graph =(Graph) facade.getMap().getGraphImpl();
        graph.addAttribute("ui.stylesheet","url(nodeStyle.css)");
        for (Node node : graph) {
            node.addAttribute("ui.label", node.getId());
            if(facade.getOccupied().contains(node.toString()))
                node.addAttribute("ui.class", defaultRobot);
            else {
                if(node.toString().equals("Parma")) node.addAttribute("ui.class", "parma");
                else if(node.toString().equals("Udine")) node.addAttribute("ui.class", "udine");
                else node.addAttribute("ui.class", "normal");
            }
        }
        graphPanel.updateUI();
    }
    @Override
    public void refreshButton() {
        for (int i = 0; i < DSCluster.getInstance().getNumHost(); ++i) {
            if (DSCluster.getInstance().getActiveQueries(i) != null) {
                DSCluster.getInstance().getActiveQueries(i).forEach((mapVersionTmp, mapWrapperTmp) -> {
                    for (Component c : eastPanelQuery.getComponents()) {
                        if (c instanceof JPanel && c.getName().equals(mapVersionTmp)) {
                            for (Component e : ((JPanel) c).getComponents())
                                if (e instanceof JButton) {
                                    e.setVisible(!autoMove);
                                    if (autoMove) {
                                        e.setEnabled(false);
                                        if (!((DSQueryResult) mapWrapperTmp).getStillToVerify().equals("\n") && ((DSQueryResult) mapWrapperTmp).getStatus()!=DSQuery.QueryStatus.FAIL && ((DSQueryResult) mapWrapperTmp).getStatus()!=DSQuery.QueryStatus.MATCH ) {
                                            DSQuery.QueryId id = ((DSQueryResult) mapWrapperTmp).getQuery().getId();
                                            DSCluster.getInstance().retryQuery(id);
                                        }
                                    }
                                }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void enableButton() {
        for (int i = 0; i < DSCluster.getInstance().getNumHost(); ++i) {
            if (DSCluster.getInstance().getActiveQueries(i) != null) {
                DSCluster.getInstance().getActiveQueries(i).forEach((mapVersionTmp, mapWrapperTmp) -> {
                    for (Component c : eastPanelQuery.getComponents()) {
                        if (c instanceof JPanel && c.getName().equals(mapVersionTmp)) {
                            for (Component e : ((JPanel) c).getComponents())
                                if (e instanceof JButton) {
                                    e.setVisible(true);
                                    if (!((DSQueryResult) mapWrapperTmp).getStillToVerify().equals("\n") && ((DSQueryResult) mapWrapperTmp).getStatus()!=DSQuery.QueryStatus.FAIL && ((DSQueryResult) mapWrapperTmp).getStatus()!=DSQuery.QueryStatus.MATCH ) {
                                        e.setEnabled(true);
                                    }

                                }
                        }
                    }
                });
            }
        }
    }


    //This method allow the view to refresh the query status. In particular for the still-to-verofy query.
    private void refreshQuery(DSQuery.QueryId qId, DSQuery.QueryStatus status) {
        String version = qId.getVersion();
        int host = qId.getHost();
        DSCluster.getInstance().getActiveQueries(host).forEach((mapVersionTmp, mapWrapperTmp) -> {
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
                    if (c instanceof JPanel && c.getName().equals(mapVersion)) {
                        find = true;
                        for (Component d : ((JPanel) c).getComponents()) {
                            if (d instanceof JPanel && d.getName().equals(mapVersion)) {
                                int i = 0;
                                for (Component e : ((JPanel) d).getComponents()) {
                                    ++i;
                                    if (e.getName() != null && !mapWrapper.getStillToVerify().equals("\n") && (status!=DSQuery.QueryStatus.FAIL && status!=DSQuery.QueryStatus.MATCH)) {
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
                                        ((JPanel) d).remove(e);
                                        twoQueryStatusPanel.setLayout(new GridLayout(0, 1));
                                        twoQueryStatusPanel.updateUI();
                                    }
                                }
                                if (i < 2 && !mapWrapper.getStillToVerify().equals("\n") && (status!=DSQuery.QueryStatus.FAIL && status!=DSQuery.QueryStatus.MATCH)){
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
        String version = qId.getVersion();
        int host = qId.getHost();
        refreshQuery(qId, status);
        switch(status) {
            case MATCH:
            case FAIL:
            case NEW: showQueryStatus(status, qId.getName(),host);
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
                DSCluster.getInstance().getActiveQueries(host).forEach((mapVersionTmp, mapWrapperTmp) -> {
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
                showQueryStatus(status, qId.getName(), host);
                if(autoMove){
                 DSCluster.getInstance().makeMove(host);
                DSCluster.getInstance().retryQuery(qId);
                }
        }
    }
}