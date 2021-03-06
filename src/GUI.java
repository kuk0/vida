import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintStream;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

public class GUI {
    static Random random = new Random();

    static JFileChooser graphLoader;
    static JFileChooser graphSaver;

    // uz pristupujeme cez graf 
    static Graph graph;
    static Model model;
    static Controls controls;
    static MessageQueue messageQueue;
    static GlobalKeyListener gkl;
    static ZoomWindow zoomWindow;
    static InformationPanel informationPanel;
    static JFrame frame;
    static PopupPanel popupInformation;
    static PopupPanel popupZoomWindow;
    static JMenuBar menu;
    static JLayeredPane layeredPane;

    static Timer globalTimer;

    static Language language;

    static class Window implements Runnable {
        @Override
        public void run() {
            // TODO spravit krajsie

            final JFrame frame = new JFrame("ViDA");
            GUI.frame = frame;
            frame.setLayout(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            graphLoader = new JFileChooser("./");
            graphSaver = new JFileChooser("./");

            menu = new JMenuBar();

            for (int i = 0; i < Menu.menuItems.length; i++) {
                final JMenu item = new JMenu(Menu.menuItems[i]);
                menu.add(item);
                for (int j = 0; j < Menu.allMenuItems[i].length; ++j) {
                    if (Menu.allMenuItems[i][j].equals("--")) {
                        item.addSeparator();
                    } else {
                        final JMenuItem meno = new JMenuItem(Menu.allMenuItems[i][j]);
                        item.add(meno);
                        final int fi = i;
                        final int fj = j;
                        meno.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                Menu.performAction(fi, fj);
                            }
                        });
                    }
                }
            }

            popupInformation = new PopupPanel(informationPanel.scrollPanel);
            gkl.addMouseListener(KeyEvent.VK_I, popupInformation);
            popupZoomWindow = new PopupPanel(zoomWindow.canvas);

            layeredPane = new JLayeredPane();
            frame.add(layeredPane);
            //layeredPane.setLayout(null);
            layeredPane.add(menu);
            layeredPane.add(graph.canvas);
            layeredPane.add(informationPanel.scrollPanel);
            layeredPane.add(zoomWindow.canvas);
            layeredPane.add(popupInformation);
            layeredPane.add(popupZoomWindow);
            frame.add(controls.panel);

            layeredPane.setComponentZOrder(menu, 0);
            layeredPane.setComponentZOrder(graph.canvas, 1);
            layeredPane.setComponentZOrder(informationPanel.scrollPanel, 0);
            layeredPane.setComponentZOrder(zoomWindow.canvas, 0);
            layeredPane.setComponentZOrder(popupInformation, 0);
            layeredPane.setComponentZOrder(popupZoomWindow, 0);

            informationPanel.scrollPanel.setVisible(false);
            zoomWindow.canvas.setVisible(false);

            KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            manager.addKeyEventDispatcher(gkl);

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    saveApp();
                }
            });
            frame.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    refreshLayout();
                }
            });

            frame.setSize(CONST.windowWidth, CONST.windowHeight);
            frame.setVisible(true);
            int dw = frame.getWidth() - frame.getContentPane().getWidth();
            int dh = frame.getHeight() - frame.getContentPane().getHeight();
            System.out.println("frame " + dw + " " + dh);
            frame.setMinimumSize(new Dimension(CONST.minWindowWidth + dw, CONST.minWindowHeight
                    + dh));
            gRepaint();

        }
    }

    public static void refreshLayout() {
        int gw = frame.getContentPane().getWidth() - CONST.popupwidth;
        int gh = frame.getContentPane().getHeight() - CONST.menuHeight - CONST.controlsHeight;
        System.out.println("refreshLayout " + gw + " " + gh);
        layeredPane.setBounds(0, 0, frame.getContentPane().getWidth(), gh + CONST.menuHeight);
        menu.setBounds(0, 0, frame.getContentPane().getWidth(), CONST.menuHeight);
        graph.canvas.setLocation(0, CONST.menuHeight);
        graph.resizeCanvas(gw, gh);
        controls.panel.setBounds(0, gh + CONST.menuHeight, frame.getContentPane().getWidth(),
                CONST.controlsHeight);
        informationPanel.scrollPanel.setBounds(gw - CONST.informationWidth, CONST.menuHeight,
                CONST.informationWidth, gh - CONST.zoomWindowHeight);
        zoomWindow.canvas.setBounds(gw - CONST.zoomWindowWidth, CONST.menuHeight + gh
                - CONST.zoomWindowHeight, CONST.zoomWindowWidth, CONST.zoomWindowHeight);
        popupInformation.setBounds(gw, CONST.menuHeight, CONST.popupwidth, gh
                - CONST.zoomWindowHeight);
        popupZoomWindow.setBounds(gw, CONST.menuHeight + gh - CONST.zoomWindowHeight,
                CONST.popupwidth, CONST.zoomWindowHeight);
        controls.refresh();
        gRepaint();
    }

    public static void gRepaint() {
        if (controls != null)
            GUI.controls.canvas.repaint();
        if (graph != null)
            GUI.graph.canvas.repaint();
        if (layeredPane != null)
            GUI.layeredPane.repaint();
    }

    public static void acceptSettings(ModelSettings settings) {
        System.out.println("Gui is accepting new settings");
        graph.acceptSettings(settings);
        System.out.println("graph accepted new settings");
        setModel(settings.getModel());
        System.out.println("Gui accepted new settings");
    }

    public static void setModel(ModelType modelTyp) {
        if (modelTyp == ModelType.LE)
            model = new LeaderElectionModel();
        else if (modelTyp == ModelType.BC)
            model = new BroadcastModel();
        else if (modelTyp == ModelType.TR)
            model = new TraversalModel();
        controls.setModel(model);
    }

    public static void saveApp() {
        try {
            File file = new File("backup/graf.in");
            PrintStream out = new PrintStream(file);
            graph.print(out);
            out.close();
            file = new File("backup/program.in");
            out = new PrintStream(file);
            model.print(out);
            out.close();
            file = new File("backup/settings.in");
            out = new PrintStream(file);
            model.settings.print(out);
            out.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void loadApp() {
        try {
            File file = new File("backup/graf.in");
            Scanner in = new Scanner(file);
            graph.read(in);
            in.close();
            file = new File("backup/program.in");
            in = new Scanner(file);
            model.read(in);
            model.compile();
            in.close();
            file = new File("backup/settings.in");
            in = new Scanner(file);
            model.settings.read(in);
            in.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void main(String[] args) {
        language = Language.english;
        // TODO spravit krajsie
        gkl = new GlobalKeyListener();
        graph = new Graph();
        //FIXME len na testovanie
        model = new LeaderElectionModel();
        controls = new Controls();
        graph.listener.setControls(controls);

        zoomWindow = new ZoomWindow();
        informationPanel = new InformationPanel();
        globalTimer = new Timer();
        final Window window = new Window();
        SwingUtilities.invokeLater(window);
        loadApp();
    }
}
