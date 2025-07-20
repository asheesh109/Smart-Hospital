import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

import java.io.File;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class ModernPatientPanel extends JFrame {
    // Color scheme (unchanged)
    private static final Color PRIMARY_COLOR = new Color(66, 133, 244);
    private static final Color SECONDARY_COLOR = new Color(234, 67, 53);

    private static final Color SIDEBAR_COLOR = new Color(33, 41, 52);
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color LIGHT_TEXT_COLOR = new Color(255, 255, 255);
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255);


    private static final Color SUCCESS_COLOR = new Color(0, 100, 140);
    private static final Color DANGER_COLOR = new Color(230, 60, 80);
    private static final Color WARNING_COLOR = new Color(255, 180, 50);
    private static final Color INFO_COLOR = new Color(0, 160, 180);
    private static final Color LIGHT_COLOR = new Color(240, 242, 245);
    private static final Color DARK_COLOR = new Color(20, 40, 80);
    private static final Color BACKGROUND_COLOR = new Color(210, 235, 250);

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/HMsystem";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Ashish030406";

    // Patient details (fetched dynamically)
    private int patientId; // Example: Priya Patel's ID = 2
    private String patientName;
    private String patientEmail;
    private Connection connection;

    // Main panels
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JPanel statusBar;
    private CardLayout cardLayout;
    private Map<String, JPanel> panelMap;

    private JPanel messageListContent;
    private JLabel currentConversationLabel;
    private JPanel messagesDisplayPanel;
    private JTextArea messageInputField;
    private Timer refreshTimer;
    private boolean isRefreshing = false;
    private long lastRefreshTimestamp = 0;
    private Map<Integer, Long> conversationLastUpdated = new HashMap<>();
    int currentUserId;

    private JTextArea chatArea;
    private JTextField userInput;
    private final String GEMINI_API_KEY = "AIzaSyCvmzv8kQpkZACq3YdvUzCXw0XmhUkHcxc";
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";




    private int currentOtherUserId = 0;

    public ModernPatientPanel(int patientId) {
        this.patientId = patientId;
        currentUserId=patientId;

        // Initialize database connection
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Exit if connection fails
        }

        fetchPatientDetails(); // Fetch patient data on initialization

        setTitle("Modern Patient Portal");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        panelMap = new HashMap<>();

        setupStatusBar();
        setupSidebar();
        setupContentPanel();

        mainPanel.add(statusBar, BorderLayout.NORTH);
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Add shutdown hook to close connection
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeConnection();
            }
        });
    }




    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchPatientDetails() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT name, email FROM patients WHERE id = ?")) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                patientName = rs.getString("name");
                patientEmail = rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            patientName = "Unknown Patient";
            patientEmail = "unknown@example.com";
        }
    }

    private void setupStatusBar() {
        statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(SUCCESS_COLOR);
        statusBar.setPreferredSize(new Dimension(getWidth(), 45));
        statusBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 62, 76)));

        JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dateTimePanel.setOpaque(false);

        JLabel dateTimeLabel = new JLabel();
        dateTimeLabel.setForeground(LIGHT_TEXT_COLOR);
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateTimeLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        Timer timer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss");
            dateTimeLabel.setText(now.format(formatter));
        });
        timer.start();

        dateTimePanel.add(dateTimeLabel);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); // Increased gap to 15px
        userPanel.setOpaque(false);

        // User avatar and information panel
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); // Increased gap between avatar and name
        userInfoPanel.setOpaque(false);

        // Modern profile avatar with gradient
        JPanel userAvatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient background for avatar
                RadialGradientPaint gradient = new RadialGradientPaint(
                        new Point(16, 16), 16,
                        new float[] {0.0f, 1.0f},
                        new Color[] {new Color(220, 230, 240), new Color(180, 200, 220)}
                );
                g2.setPaint(gradient);
                g2.fillOval(0, 0, 32, 32); // Larger avatar

                // Draw profile silhouette
                g2.setColor(new Color(80, 100, 120));
                g2.fillOval(11, 7, 10, 10); // Head
                g2.fillRoundRect(8, 18, 16, 14, 6, 6); // Body shape with rounded corners

                // Add subtle border
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(150, 170, 190));
                g2.drawOval(0, 0, 32, 32);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(32, 32); // Larger avatar
            }
        };

        // Enhanced user profile information with name and role
        JPanel userTextPanel = new JPanel();
        userTextPanel.setOpaque(false);
        userTextPanel.setLayout(new BoxLayout(userTextPanel, BoxLayout.Y_AXIS));

        JLabel userLabel = new JLabel(patientName);
        userLabel.setForeground(LIGHT_TEXT_COLOR);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel("Patient");
        roleLabel.setForeground(new Color(180, 190, 200));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userTextPanel.add(userLabel);
        userTextPanel.add(roleLabel);

        userInfoPanel.add(userAvatar);
        userInfoPanel.add(userTextPanel);

        userPanel.add(userInfoPanel);
        userPanel.add(Box.createHorizontalStrut(20)); // Add extra padding at the end

        statusBar.add(dateTimePanel, BorderLayout.WEST);
        statusBar.add(userPanel, BorderLayout.EAST);
    }

    private JPanel createRoundButton(String text, Color textColor) {
        JPanel button = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(60, 72, 86));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setOpaque(false);
        button.setPreferredSize(new Dimension(30, 30));

        JLabel label = new JLabel(text);
        label.setForeground(textColor);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        button.add(label);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(50, 62, 76));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(null);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                button.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(new Color(40, 52, 66));
                button.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(new Color(50, 62, 76));
                button.repaint();
            }
        });

        return button;
    }

    private void setupSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setPreferredSize(new Dimension(220, getHeight()));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));

        // Logo panel with separator
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setBackground(new Color(30, 39, 50));
        logoPanel.setMaximumSize(new Dimension(220, 70));
        logoPanel.setPreferredSize(new Dimension(220, 70));

        JLabel logoLabel = new JLabel("Patient Portal");
        logoLabel.setForeground(LIGHT_TEXT_COLOR);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        logoPanel.add(Box.createHorizontalStrut(8)); // Increased gap
        logoPanel.add(logoLabel);

        sidebarPanel.add(logoPanel);

        // Add a separator
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(220, 1));
        separator.setForeground(new Color(50, 62, 76));
        sidebarPanel.add(separator);

        // Add some space
        sidebarPanel.add(Box.createVerticalStrut(20));

        // Removed navigation label as requested

        String[] menuItems = {"Dashboard", "Schedule Appointment", "Messages", "Reports", "Chatbot"};

        // Menu icons drawn with Java2D instead of emoji text
        for (int i = 0; i < menuItems.length; i++) {
            JPanel menuItemPanel = createMenuItem(menuItems[i], i); // Pass index instead of emoji icon
            sidebarPanel.add(menuItemPanel);
            sidebarPanel.add(Box.createVerticalStrut(8)); // Increased gap between menu items
        }

        sidebarPanel.add(Box.createVerticalGlue());

        // Removed separator before logout as requested

        // Add gap before logout button
        sidebarPanel.add(Box.createVerticalStrut(8));

        JPanel logoutPanel = createMenuItem("Logout", 5); // 5 is the index for logout
        sidebarPanel.add(logoutPanel);
        sidebarPanel.add(Box.createVerticalStrut(20));
    }

    private JPanel createMenuItem(String text, int iconType) {
        JPanel menuItem = new JPanel(new BorderLayout(17, 0)); // Increased gap between icon and text
        menuItem.setBackground(SUCCESS_COLOR);
        menuItem.setMaximumSize(new Dimension(220, 55)); // Increased height of menu items
        menuItem.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15)); // Increased vertical padding

        // Create custom icon based on type, increased size
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(LIGHT_TEXT_COLOR);

                // Scale factor for larger icons
                float scale = 1.3f;
                g2.scale(scale, scale);

                switch (iconType) {
                    case 0: // Dashboard
                        g2.fillRect(5, 5, 8, 6);
                        g2.fillRect(16, 5, 8, 6);
                        g2.fillRect(5, 14, 8, 6);
                        g2.fillRect(16, 14, 8, 6);
                        break;
                    case 1: // Schedule Appointment
                        g2.drawRect(4, 4, 20, 16);
                        g2.drawLine(4, 10, 24, 10);
                        g2.drawLine(12, 10, 12, 20);
                        break;
                    case 2: // Messages
                        g2.drawRect(4, 6, 20, 14);
                        g2.drawLine(4, 9, 24, 9);
                        g2.drawLine(14, 14, 22, 14);
                        g2.drawLine(14, 17, 20, 17);
                        break;
                    case 3: // Reports
                        g2.drawRect(6, 4, 16, 20);
                        g2.drawLine(10, 9, 18, 9);
                        g2.drawLine(10, 13, 18, 13);
                        g2.drawLine(10, 17, 15, 17);
                        break;
                    case 4: // Chatbot
                        g2.drawRoundRect(4, 5, 20, 14, 6, 6);
                        g2.drawLine(15, 19, 18, 24);
                        g2.drawLine(9, 11, 13, 11);
                        g2.drawLine(9, 14, 19, 14);
                        break;
                    case 5: // Logout
                        g2.drawRect(5, 6, 15, 12);
                        g2.fillPolygon(new int[]{20, 25, 20}, new int[]{5, 12, 19}, 3);
                        g2.drawLine(15, 12, 20, 12);
                        break;
                }

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(36, 30); // Increased icon size
            }
        };
        iconPanel.setOpaque(false);

        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(LIGHT_TEXT_COLOR);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Increased font size

        menuItem.add(iconPanel, BorderLayout.WEST);
        menuItem.add(textLabel, BorderLayout.CENTER);

        // Add indicator for active menu item
        JPanel indicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if ("Dashboard".equals(text)) {  // Default active menu item
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(92, 184, 230));
                    g2.fillRoundRect(0, 0, 4, getHeight(), 3, 3); // Made indicator thicker
                    g2.dispose();
                }
            }
        };
        indicator.setPreferredSize(new Dimension(4, 0)); // Increased width
        indicator.setOpaque(false);
        menuItem.add(indicator, BorderLayout.WEST);

        menuItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                menuItem.setBackground(new Color(44, 55, 69));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                menuItem.setBackground(SUCCESS_COLOR);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Remove active indicator from all menu items
                for (Component comp : sidebarPanel.getComponents()) {
                    if (comp instanceof JPanel && ((JPanel) comp).getComponentCount() > 0) {
                        Component westComp = ((JPanel) comp).getComponent(0);
                        if (westComp instanceof JPanel) {
                            westComp.repaint();
                        }
                    }
                }

                // Update the indicator for this menu item
                indicator.repaint();

                switch (text) {
                    case "Dashboard":
                        cardLayout.show(contentPanel, "Dashboard");
                        break;
                    case "Schedule Appointment":
                        cardLayout.show(contentPanel, "Appointment");
                        break;
                    case "Messages":
                        cardLayout.show(contentPanel, "Messages");
                        break;
                    case "Reports":
                        cardLayout.show(contentPanel, "Reports");
                        break;
                    case "Chatbot":
                        cardLayout.show(contentPanel, "Chatbot");
                        break;
                    case "Logout":
                        // Create custom dialog panel
                        JPanel dialogPanel = new JPanel(new BorderLayout(20, 10));
                        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                        JLabel confirmLabel = new JLabel("Are you sure you want to logout?");
                        confirmLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Added 10px gap between buttons

                        JButton cancelButton = new JButton("Cancel");
                        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        cancelButton.setFocusPainted(false);

                        JButton logoutButton = new JButton("Logout");
                        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        logoutButton.setBackground(new Color(220, 53, 69));
                        logoutButton.setForeground(Color.WHITE);
                        logoutButton.setFocusPainted(false);

                        buttonPanel.add(cancelButton);
                        buttonPanel.add(logoutButton);

                        dialogPanel.add(confirmLabel, BorderLayout.CENTER);
                        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

                        JDialog logoutDialog = new JDialog();
                        logoutDialog.setTitle("Logout Confirmation");
                        logoutDialog.setModal(true);
                        logoutDialog.setContentPane(dialogPanel);
                        logoutDialog.pack();
                        logoutDialog.setLocationRelativeTo(ModernPatientPanel.this);

// Action listener for cancelButton to dispose of the dialog
                        cancelButton.addActionListener(ae -> logoutDialog.dispose());

// Action listener for logoutButton to confirm and proceed
                        logoutButton.addActionListener(ae -> {
                            // Dispose of the logout confirmation dialog
                            logoutDialog.dispose();
                            // Dispose of the current frame (e.g., ModernPatientPanel)
                            dispose();
                            // Open ConnectPage on the EDT with error handling
                            SwingUtilities.invokeLater(() -> {
                                try {
                                    ConnectPage connectPage = new ConnectPage();
                                    connectPage.setVisible(true);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    JOptionPane.showMessageDialog(null, "Error opening ConnectPage: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        });

// Show the logout dialog when needed (e.g., triggered by another action)
                        logoutDialog.setVisible(true); // Uncomment or move this to where you want to show the dialog
                        break;
                }
            }
        });

        return menuItem;
    }

    private void setupContentPanel() {
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(BACKGROUND_COLOR);

        panelMap = new HashMap<>();

        createDashboardPanel();
        createAppointmentPanel();
        createMessagesPanel();
        createReportsPanel();
        createChatbotPanel();

        contentPanel.add(panelMap.get("Dashboard"), "Dashboard");
        contentPanel.add(panelMap.get("Appointment"), "Appointment");
        contentPanel.add(panelMap.get("Messages"), "Messages");
        contentPanel.add(panelMap.get("Reports"), "Reports");
        contentPanel.add(panelMap.get("Chatbot"), "Chatbot");

        cardLayout.show(contentPanel, "Dashboard");
    }

    private void createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back, " + patientName);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(TEXT_COLOR);

        JLabel subTitle = new JLabel("Here's your health overview");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subTitle.setForeground(new Color(120, 120, 120));

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createVerticalStrut(5));
        welcomePanel.add(subTitle);

        JPanel photoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight()) - 4;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                g2d.setColor(Color.WHITE);
                g2d.fillOval(x, y, size, size);

                g2d.setColor(PRIMARY_COLOR);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(x, y, size, size);

                g2d.setColor(new Color(150, 150, 150));
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 24));
                FontMetrics fm = g2d.getFontMetrics();
                String text = patientName.substring(0, 1) + patientName.substring(patientName.indexOf(" ") + 1, patientName.indexOf(" ") + 2);
                int textWidth = fm.stringWidth(text);
                g2d.drawString(text, (getWidth() - textWidth) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(80, 80);
            }
        };
        photoPanel.setOpaque(false);

        headerPanel.add(welcomePanel, BorderLayout.WEST);
        headerPanel.add(photoPanel, BorderLayout.EAST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        // Upcoming Appointments Card (Dynamic from DB)
        JPanel appointmentsCard = createCard("Upcoming Appointments", "📅");
        JPanel appointmentsContent = new JPanel();
        appointmentsContent.setLayout(new BoxLayout(appointmentsContent, BoxLayout.Y_AXIS));
        appointmentsContent.setOpaque(false);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT date, time, description FROM appointments WHERE patient_id = ? AND status = 'Scheduled' ORDER BY date LIMIT 2")) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String date = rs.getString("date");
                String time = rs.getString("time");
                String description = rs.getString("description");

                JPanel apptPanel = new JPanel(new BorderLayout());
                apptPanel.setBackground(new Color(245, 247, 250));
                apptPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230)),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                JLabel descLabel = new JLabel(description);
                descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

                JPanel leftPanel = new JPanel();
                leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
                leftPanel.setOpaque(false);
                leftPanel.add(descLabel);

                JLabel dateLabel = new JLabel(date);
                dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                JLabel timeLabel = new JLabel(time);
                timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

                JPanel rightPanel = new JPanel();
                rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
                rightPanel.setOpaque(false);
                rightPanel.add(dateLabel);
                rightPanel.add(timeLabel);

                apptPanel.add(leftPanel, BorderLayout.WEST);
                apptPanel.add(rightPanel, BorderLayout.EAST);

                appointmentsContent.add(apptPanel);
                appointmentsContent.add(Box.createVerticalStrut(10));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JButton scheduleButton = createModernButton("schedule Appointment",WARNING_COLOR,Color.WHITE);
        scheduleButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scheduleButton.setBackground(PRIMARY_COLOR);
        scheduleButton.setForeground(Color.WHITE);
        scheduleButton.setFocusPainted(false);
        scheduleButton.setBorder(new RoundedBorder(Color.WHITE,5));
        scheduleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        scheduleButton.addActionListener(e -> cardLayout.show(this.contentPanel, "Appointment"));

        appointmentsContent.add(Box.createVerticalStrut(10));
        appointmentsContent.add(scheduleButton);

        appointmentsCard.add(appointmentsContent, BorderLayout.CENTER);

        // Health Metrics Card (Dynamic from DB)
        JPanel metricsCard = createCard("Health Metrics", "❤️");
        JPanel metricsContent = new JPanel(new GridLayout(4, 2, 15, 15));
        metricsContent.setOpaque(false);
        metricsContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT weight, bp, bpi, sugar, oxygen, rate FROM patient_detail WHERE patient_id = ? ORDER BY record_date DESC LIMIT 1")) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String[][] metrics = {
                        {"Blood Pressure", rs.getString("bp") + " mmHg", "Normal"},
                        {"Heart Rate", rs.getInt("rate") + " bpm", "Normal"},
                        {"Weight", rs.getFloat("weight") + " kg", "Stable"},
                        {"BMI", String.format("%.2f", rs.getFloat("bpi")), "Normal"},
                        {"Glucose", rs.getFloat("sugar") + " mg/dL", "Normal"},
                        {"Oxygen", rs.getFloat("oxygen") + "%", "Normal"}
                };

                for (String[] metric : metrics) {
                    JPanel metricPanel = new JPanel();
                    metricPanel.setLayout(new BoxLayout(metricPanel, BoxLayout.Y_AXIS));
                    metricPanel.setBackground(CARD_BACKGROUND);
                    metricPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(230, 230, 230)),
                            BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    ));

                    JLabel titleLabel = new JLabel(metric[0]);
                    titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    titleLabel.setForeground(new Color(100, 100, 100));

                    JLabel valueLabel = new JLabel(metric[1]);
                    valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

                    JLabel statusLabel = new JLabel(metric[2]);
                    statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    statusLabel.setForeground(new Color(46, 125, 50));

                    metricPanel.add(titleLabel);
                    metricPanel.add(Box.createVerticalStrut(5));
                    metricPanel.add(valueLabel);
                    metricPanel.add(Box.createVerticalStrut(5));
                    metricPanel.add(statusLabel);

                    metricsContent.add(metricPanel);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        metricsCard.add(metricsContent, BorderLayout.CENTER);

        // Recent Messages Card (Dynamic from DB)
        JPanel messagesCard = createCard("Recent Messages", "✉️");
        JPanel messagesContent = new JPanel();
        messagesContent.setLayout(new BoxLayout(messagesContent, BoxLayout.Y_AXIS));
        messagesContent.setOpaque(false);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT message_text, timestamp, is_read FROM messages WHERE receiver_id = ? ORDER BY timestamp DESC LIMIT 3")) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String messageText = rs.getString("message_text");
                String timestamp = rs.getTimestamp("timestamp").toString().substring(0, 10);
                boolean isRead = rs.getBoolean("is_read");

                JPanel msgPanel = new JPanel(new BorderLayout());
                msgPanel.setBackground(new Color(245, 247, 250));
                msgPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230)),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                JLabel contentLabel = new JLabel(messageText);
                contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                contentLabel.setForeground(isRead ? new Color(100, 100, 100) : PRIMARY_COLOR);

                JPanel leftPanel = new JPanel();
                leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
                leftPanel.setOpaque(false);
                leftPanel.add(contentLabel);

                JLabel dateLabel = new JLabel(timestamp);
                dateLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                dateLabel.setForeground(new Color(150, 150, 150));

                msgPanel.add(leftPanel, BorderLayout.WEST);
                msgPanel.add(dateLabel, BorderLayout.EAST);

                messagesContent.add(msgPanel);
                messagesContent.add(Box.createVerticalStrut(10));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JButton viewAllButton = createModernButton("View All",DARK_COLOR,Color.WHITE);
        viewAllButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewAllButton.setBackground(PRIMARY_COLOR);
        viewAllButton.setForeground(Color.WHITE);
        viewAllButton.setFocusPainted(false);
        viewAllButton.setBorder(new RoundedBorder(Color.WHITE,5));
        viewAllButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllButton.addActionListener(e -> cardLayout.show(this.contentPanel, "Messages"));

        messagesContent.add(Box.createVerticalStrut(10));
        messagesContent.add(viewAllButton);

        messagesCard.add(messagesContent, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        contentPanel.add(appointmentsCard, gbc);

        gbc.gridx = 1;
        contentPanel.add(messagesCard, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        contentPanel.add(metricsCard, gbc);

        dashboardPanel.add(headerPanel, BorderLayout.NORTH);
        dashboardPanel.add(new JScrollPane(contentPanel), BorderLayout.CENTER);

        panelMap.put("Dashboard", dashboardPanel);
    }

    private JPanel createCard(String title, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(Color.WHITE,10),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BACKGROUND);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        card.add(headerPanel, BorderLayout.NORTH);

        return card;
    }

    private void createAppointmentPanel() {
        JPanel appointmentPanel = new JPanel(new BorderLayout(0, 0));
        appointmentPanel.setBackground(new Color(245, 247, 250));

        // Create header panel with gradient
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(25, 118, 210),
                        0, getHeight(), new Color(21, 101, 192)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 80));

        // Add title to header
        JLabel titleLabel = new JLabel("Schedule Appointment");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Create card for form content
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Create form panel with rounded corners
        JPanel formPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        // Set up grid bag constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;

        // Create form elements with modern styling

        // Doctor selection with improved styling
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel doctorLabel = createStyledLabel("Select Doctor");
        formPanel.add(doctorLabel, gbc);

        JComboBox<DoctorItem> doctorComboBox = createStyledComboBox();
        loadDoctors(doctorComboBox);
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(doctorComboBox, gbc);

        // Date selection panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel dateLabel = createStyledLabel("Appointment Date");
        formPanel.add(dateLabel, gbc);

        // Modern date selection with shadows
        JPanel dateSelectPanel = new JPanel(new GridLayout(1, 5, 8, 0));
        dateSelectPanel.setBackground(Color.WHITE);

        String[] years = new String[10];
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i < 10; i++) {
            years[i] = String.valueOf(currentYear + i);
        }
        JComboBox<String> yearCombo = createStyledComboBox();
        for (String year : years) {
            yearCombo.addItem(year);
        }

        String[] months = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        JComboBox<String> monthCombo = createStyledComboBox();
        for (String month : months) {
            monthCombo.addItem(month);
        }

        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.format("%02d", i + 1);
        }
        JComboBox<String> dayCombo = createStyledComboBox();
        for (String day : days) {
            dayCombo.addItem(day);
        }

        JLabel separatorLabel1 = new JLabel("-", JLabel.CENTER);
        separatorLabel1.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel separatorLabel2 = new JLabel("-", JLabel.CENTER);
        separatorLabel2.setFont(new Font("Segoe UI", Font.BOLD, 16));

        dateSelectPanel.add(yearCombo);
        dateSelectPanel.add(separatorLabel1);
        dateSelectPanel.add(monthCombo);
        dateSelectPanel.add(separatorLabel2);
        dateSelectPanel.add(dayCombo);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(dateSelectPanel, gbc);

        // Set defaults to current date
        Calendar cal = Calendar.getInstance();
        yearCombo.setSelectedItem(String.valueOf(cal.get(Calendar.YEAR)));
        monthCombo.setSelectedItem(String.format("%02d", cal.get(Calendar.MONTH) + 1));
        dayCombo.setSelectedItem(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));

        // Time selection
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel timeLabel = createStyledLabel("Appointment Time");
        formPanel.add(timeLabel, gbc);

        JComboBox<String> timeComboBox = createStyledComboBox();
        populateTimeSlots(timeComboBox);
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(timeComboBox, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 6;
        JLabel descriptionLabel = createStyledLabel("Reason for Visit");
        formPanel.add(descriptionLabel, gbc);

        JTextArea descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(scrollPane, gbc);

        // Status message
        JLabel statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(244, 67, 54));
        gbc.gridx = 0;
        gbc.gridy = 8;
        formPanel.add(statusLabel, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        // Check availability button with animation effects
        JButton checkButton = createStyledButton("Check Availability", new Color(33, 150, 243));

        // Submit button with animation effects
        JButton submitButton = createStyledButton("Schedule Appointment", new Color(76, 175, 80));
        submitButton.setEnabled(false);

        buttonPanel.add(checkButton);
        buttonPanel.add(submitButton);

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.insets = new Insets(20, 10, 10, 10);
        formPanel.add(buttonPanel, gbc);

        // Add event listeners
        ActionListener dateChangeListener = e -> {
            submitButton.setEnabled(false);
            statusLabel.setText("");
        };

        yearCombo.addActionListener(dateChangeListener);
        monthCombo.addActionListener(dateChangeListener);
        dayCombo.addActionListener(dateChangeListener);

        timeComboBox.addActionListener(e -> {
            submitButton.setEnabled(false);
            statusLabel.setText("");
        });

        doctorComboBox.addActionListener(e -> {
            submitButton.setEnabled(false);
            statusLabel.setText("");
        });

        // Update days in month when month or year changes
        ActionListener updateDaysListener = e -> {
            updateDaysInMonth(yearCombo, monthCombo, dayCombo);
        };

        monthCombo.addActionListener(updateDaysListener);
        yearCombo.addActionListener(updateDaysListener);

        // Check availability
        checkButton.addActionListener(e -> {
            // Validate date
            if (!isValidDate(yearCombo, monthCombo, dayCombo)) {
                animateErrorMessage(statusLabel, "Invalid date selection");
                return;
            }

            // Check if date is in the past
            if (isDateInPast(yearCombo, monthCombo, dayCombo)) {
                animateErrorMessage(statusLabel, "Cannot schedule appointments in the past");
                return;
            }

            DoctorItem selectedDoctor = (DoctorItem) doctorComboBox.getSelectedItem();
            LocalDate selectedDate = getSelectedLocalDate(yearCombo, monthCombo, dayCombo);
            String selectedTime = (String) timeComboBox.getSelectedItem();

            // Show loading animation
            statusLabel.setText("Checking availability...");
            statusLabel.setForeground(new Color(33, 150, 243));

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    // Simulate network delay for better UX
                    Thread.sleep(800);
                    return isTimeSlotAvailable(selectedDoctor.getId(), selectedDate, selectedTime);
                }

                @Override
                protected void done() {
                    try {
                        boolean available = get();
                        if (available) {
                            statusLabel.setText("✓ Time slot available!");
                            statusLabel.setForeground(new Color(76, 175, 80));
                            submitButton.setEnabled(true);

                            // Flash effect on submit button
                            Timer flashTimer = new Timer(100, new ActionListener() {
                                int count = 0;
                                Color originalColor = submitButton.getBackground();

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    if (count % 2 == 0) {
                                        submitButton.setBackground(new Color(129, 199, 132));
                                    } else {
                                        submitButton.setBackground(originalColor);
                                    }
                                    count++;
                                    if (count >= 6) {
                                        ((Timer)e.getSource()).stop();
                                        submitButton.setBackground(originalColor);
                                    }
                                }
                            });
                            flashTimer.start();
                        } else {
                            animateErrorMessage(statusLabel, "Time slot not available. Please select another time.");
                            submitButton.setEnabled(false);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        animateErrorMessage(statusLabel, "Error checking availability");
                    }
                }
            };
            worker.execute();
        });

        // Submit appointment
        submitButton.addActionListener(e -> {
            DoctorItem selectedDoctor = (DoctorItem) doctorComboBox.getSelectedItem();
            LocalDate selectedDate = getSelectedLocalDate(yearCombo, monthCombo, dayCombo);
            String selectedTime = (String) timeComboBox.getSelectedItem();
            String description = descriptionArea.getText();

            if (description.trim().isEmpty()) {
                animateErrorMessage(statusLabel, "Please provide a reason for your visit");
                return;
            }

            if (createAppointment(currentUserId, selectedDoctor.getId(), selectedDate, selectedTime, description)) {
                // Reset form will be handled by the createAppointment method
                statusLabel.setText("");
                submitButton.setEnabled(false);
            }
        });

        // Add form to card
        cardPanel.add(formPanel, BorderLayout.CENTER);

        // Create a container with shadow effect
        JPanel containerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int shadowSize = 10;
                for (int i = 0; i < shadowSize; i++) {
                    float opacity = 0.1f - (i * 0.01f);
                    g2d.setColor(new Color(0, 0, 0, Math.max(0, (int)(opacity * 255))));
                    g2d.drawRoundRect(
                            shadowSize - i, shadowSize - i,
                            getWidth() - 2 * (shadowSize - i) - 1,
                            getHeight() - 2 * (shadowSize - i) - 1,
                            15, 15
                    );
                }
            }
        };
        containerPanel.setOpaque(false);
        containerPanel.setLayout(new BorderLayout());
        containerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        containerPanel.add(cardPanel, BorderLayout.CENTER);

        // Add components to main panel
        appointmentPanel.add(headerPanel, BorderLayout.NORTH);
        appointmentPanel.add(containerPanel, BorderLayout.CENTER);
         scrollPane = new JScrollPane(appointmentPanel);
        scrollPane.setBorder(null);

        // Create a wrapper panel that will hold the scroll pane
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);

        // Now add to panelMap which expects JPanel
        panelMap.put("Appointment", wrapperPanel);
    }

    // Helper method to create a styled label
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(66, 66, 66));
        return label;
    }

    // Helper method to create a styled combo box
    private <T> JComboBox<T> createStyledComboBox() {
        JComboBox<T> comboBox = new JComboBox<T>() {
            @Override
            public void updateUI() {
                super.updateUI();
                setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                  int index, boolean isSelected,
                                                                  boolean cellHasFocus) {
                        JLabel renderer = (JLabel) super.getListCellRendererComponent(
                                list, value, index, isSelected, cellHasFocus);
                        renderer.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                        renderer.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                        if (isSelected) {
                            renderer.setBackground(new Color(33, 150, 243));
                            renderer.setForeground(Color.WHITE);
                        } else {
                            renderer.setForeground(new Color(33, 33, 33));
                        }
                        return renderer;
                    }
                });
            }
        };

        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(8, 5, 8, 5)));
        comboBox.setBackground(Color.WHITE);

        return comboBox;
    }

    // Helper method to create a styled button
    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(baseColor.brighter());
                } else {
                    g2d.setColor(baseColor);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Draw text
                FontMetrics metrics = g2d.getFontMetrics(getFont());
                int x = (getWidth() - metrics.stringWidth(getText())) / 2;
                int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();

                if (isEnabled()) {
                    g2d.setColor(Color.WHITE);
                } else {
                    g2d.setColor(new Color(200, 200, 200));
                }
                g2d.drawString(getText(), x, y);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 40));

        return button;
    }

    // Animate error message for better visual feedback
    private void animateErrorMessage(JLabel label, String message) {
        label.setText(message);
        label.setForeground(new Color(244, 67, 54));

        // Create shaking animation
        final int originalX = label.getLocation().x;
        final Timer timer = new Timer(30, null);
        final int[] moves = {-5, 5, -5, 5, -3, 3, -2, 2, -1, 1, 0};
        final AtomicInteger currentMove = new AtomicInteger(0);

        timer.addActionListener(e -> {
            if (currentMove.get() >= moves.length) {
                timer.stop();
                label.setLocation(originalX, label.getLocation().y);
                return;
            }

            label.setLocation(originalX + moves[currentMove.getAndIncrement()], label.getLocation().y);
        });

        timer.start();
    }

    // Helper class to store doctor information
    private class DoctorItem {
        private int id;
        private String name;

        public DoctorItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // Helper method to update days in month based on selected month and year
    private void updateDaysInMonth(JComboBox<String> yearCombo, JComboBox<String> monthCombo,
                                   JComboBox<String> dayCombo) {
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());
        int month = Integer.parseInt((String) monthCombo.getSelectedItem());

        // Save current selection
        String currentSelection = (String) dayCombo.getSelectedItem();

        // Get days in month
        YearMonth yearMonthObject = YearMonth.of(year, month);
        int daysInMonth = yearMonthObject.lengthOfMonth();

        // Store current model
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) dayCombo.getModel();

        // Update model
        dayCombo.removeAllItems();
        for (int i = 1; i <= daysInMonth; i++) {
            dayCombo.addItem(String.format("%02d", i));
        }

        // Try to restore selection if valid, otherwise select first day
        if (Integer.parseInt(currentSelection) <= daysInMonth) {
            dayCombo.setSelectedItem(currentSelection);
        } else {
            dayCombo.setSelectedItem("01");
        }
    }

    // Helper method to check if date is valid
    private boolean isValidDate(JComboBox<String> yearCombo, JComboBox<String> monthCombo,
                                JComboBox<String> dayCombo) {
        try {
            int year = Integer.parseInt((String) yearCombo.getSelectedItem());
            int month = Integer.parseInt((String) monthCombo.getSelectedItem());
            int day = Integer.parseInt((String) dayCombo.getSelectedItem());

            // Check if date is valid
            LocalDate.of(year, month, day);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method to check if date is in the past
    private boolean isDateInPast(JComboBox<String> yearCombo, JComboBox<String> monthCombo,
                                 JComboBox<String> dayCombo) {
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());
        int month = Integer.parseInt((String) monthCombo.getSelectedItem());
        int day = Integer.parseInt((String) dayCombo.getSelectedItem());

        LocalDate selectedDate = LocalDate.of(year, month, day);
        LocalDate today = LocalDate.now();

        return selectedDate.isBefore(today);
    }

    // Helper method to get LocalDate from combo boxes
    private LocalDate getSelectedLocalDate(JComboBox<String> yearCombo, JComboBox<String> monthCombo,
                                           JComboBox<String> dayCombo) {
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());
        int month = Integer.parseInt((String) monthCombo.getSelectedItem());
        int day = Integer.parseInt((String) dayCombo.getSelectedItem());

        return LocalDate.of(year, month, day);
    }

    // Populate time slots (30-minute intervals)
    private void populateTimeSlots(JComboBox<String> comboBox) {
        // Populate with times from 8 AM to 8 PM
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(20, 0);

        while (!startTime.isAfter(endTime)) {
            comboBox.addItem(startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            startTime = startTime.plusMinutes(30);
        }
    }

    // Load doctors from database
    private void loadDoctors(JComboBox<DoctorItem> comboBox) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String query = "SELECT id, name FROM doctors";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                comboBox.addItem(new DoctorItem(id, name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Error loading doctors: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    // Check if time slot is available
    private boolean isTimeSlotAvailable(int doctorId, LocalDate date, String timeStr) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();

            // Parse time string to LocalTime
            LocalTime localTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));

            // Convert to SQL Date and Time
            java.sql.Date sqlDate = java.sql.Date.valueOf(date);
            java.sql.Time sqlTime = java.sql.Time.valueOf(localTime);

            // Check for clashing appointments (within 30 minutes)
            String query = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND date = ? " +
                    "AND ABS(TIME_TO_SEC(TIMEDIFF(time, ?))) < 1800";

            stmt = conn.prepareStatement(query);
            stmt.setInt(1, doctorId);
            stmt.setDate(2, sqlDate);
            stmt.setTime(3, sqlTime);

            rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // Time slot not available
            }

            return true; // Time slot is available
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Error checking appointment availability: " + e.getMessage());
            return false;
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    // Create a new appointment
    /**
     * Creates a new appointment with improved UI feedback
     * @param patientId The ID of the patient
     * @param doctorId The ID of the doctor
     * @param date The appointment date
     * @param timeStr The appointment time as string (format: HH:mm)
     * @param description The appointment description
     * @return true if appointment was successfully created, false otherwise
     */
    /**
     * Creates a new appointment with improved UI feedback
     * @param patientId The ID of the patient
     * @param doctorId The ID of the doctor
     * @param date The appointment date
     * @param timeStr The appointment time as string (format: HH:mm)
     * @param description The appointment description
     * @return true if appointment was successfully created, false otherwise
     */
    /**
     * Creates an appointment with improved UI feedback and resource management
     */
    private boolean createAppointment(int patientId, int doctorId, LocalDate date, String timeStr, String description) {
        final Connection[] conn = {null};
        final PreparedStatement[] stmt = {null};
        final boolean[] result = {false};

        JDialog loadingDialog = createLoadingDialog("Creating Appointment...");

        Thread worker = new Thread(() -> {
            try {
                conn[0] = getConnection();
                LocalTime localTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                java.sql.Date sqlDate = java.sql.Date.valueOf(date);
                java.sql.Time sqlTime = java.sql.Time.valueOf(localTime);

                String query = "INSERT INTO appointments (patient_id, doctor_id, date, time, status, description) " +
                        "VALUES (?, ?, ?, ?, 'Scheduled', ?)";

                stmt[0] = conn[0].prepareStatement(query);
                stmt[0].setInt(1, patientId);
                stmt[0].setInt(2, doctorId);
                stmt[0].setDate(3, sqlDate);
                stmt[0].setTime(4, sqlTime);
                stmt[0].setString(5, description);

                result[0] = stmt[0].executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                result[0] = false;
            } finally {
                closeResources(conn[0], stmt[0], null);
            }

            SwingUtilities.invokeLater(() -> {
                loadingDialog.dispose();
                if (result[0]) {
                    showSuccessMessage("Appointment successfully created!");
                } else {
                    showErrorMessage("Failed to create appointment");
                }
            });
        });

        worker.start();
        loadingDialog.setVisible(true);

        try {
            worker.join();
            return result[0];
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates a modern loading dialog with improved styling
     */
    private JDialog createLoadingDialog(String message) {
        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        dialog.setSize(350, 130);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(false);

        // Main panel with custom painting
        JPanel panel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background with rounded corners
                g2d.setColor(new Color(250, 250, 250));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Top accent bar
                g2d.setColor(new Color(33, 150, 243));
                g2d.fillRect(0, 0, getWidth(), 4);

                // Draw border
                g2d.setColor(new Color(220, 220, 220));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Message label
        JLabel loadingLabel = new JLabel(message);
        loadingLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        loadingLabel.setForeground(new Color(66, 66, 66));
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Progress bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorderPainted(false);
        progressBar.setBackground(new Color(224, 224, 224));
        progressBar.setForeground(new Color(33, 150, 243));

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setOpaque(false);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        progressPanel.add(progressBar, BorderLayout.CENTER);

        panel.add(loadingLabel, BorderLayout.NORTH);
        panel.add(progressPanel, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1));

        return dialog;
    }

    /**
     * Displays a modern success message
     */
    private void showSuccessMessage(String message) {
        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        dialog.setSize(400, 160);
        dialog.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // White background with rounded corners
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Left success indicator
                g2d.setColor(new Color(76, 175, 80));
                g2d.fillRect(0, 0, 8, getHeight());

                // Success icon
                int iconSize = 30;
                int iconX = 30;
                int iconY = getHeight()/2 - 15;

                g2d.setColor(new Color(76, 175, 80));
                g2d.fillOval(iconX, iconY, iconSize, iconSize);

                // Draw checkmark
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine(iconX + 8, iconY + 15, iconX + 13, iconY + 20);
                g2d.drawLine(iconX + 13, iconY + 20, iconX + 22, iconY + 10);

                // Draw border
                g2d.setColor(new Color(220, 220, 220));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 75, 20, 20));

        JLabel messageLabel = new JLabel("<html>" + message + "</html>");
        messageLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        messageLabel.setForeground(new Color(33, 33, 33));

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Dialog", Font.BOLD, 14));
        okButton.setBackground(new Color(76, 175, 80));
        okButton.setForeground(Color.WHITE);
        okButton.setBorderPainted(false);
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setPreferredSize(new Dimension(80, 30));

        // Button hover effect
        okButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                okButton.setBackground(new Color(104, 195, 108));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                okButton.setBackground(new Color(76, 175, 80));
            }
        });

        okButton.addActionListener(e -> {
            animateDialogClose(dialog);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);

        panel.add(messageLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1));

        animateDialogOpen(dialog);
    }

    /**
     * Displays a modern error message
     */
    private void showErrorMessage(String message) {
        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        dialog.setSize(400, 160);
        dialog.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // White background with rounded corners
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Left error indicator
                g2d.setColor(new Color(244, 67, 54));
                g2d.fillRect(0, 0, 8, getHeight());

                // Error icon
                int iconSize = 30;
                int iconX = 30;
                int iconY = getHeight()/2 - 15;

                g2d.setColor(new Color(244, 67, 54));
                g2d.fillOval(iconX, iconY, iconSize, iconSize);

                // Draw X symbol
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine(iconX + 10, iconY + 10, iconX + 20, iconY + 20);
                g2d.drawLine(iconX + 20, iconY + 10, iconX + 10, iconY + 20);

                // Draw border
                g2d.setColor(new Color(220, 220, 220));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 75, 20, 20));

        JLabel messageLabel = new JLabel("<html>" + message + "</html>");
        messageLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        messageLabel.setForeground(new Color(33, 33, 33));

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Dialog", Font.BOLD, 14));
        okButton.setBackground(new Color(244, 67, 54));
        okButton.setForeground(Color.WHITE);
        okButton.setBorderPainted(false);
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setPreferredSize(new Dimension(80, 30));

        // Button hover effect
        okButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                okButton.setBackground(new Color(255, 93, 81));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                okButton.setBackground(new Color(244, 67, 54));
            }
        });

        okButton.addActionListener(e -> {
            animateDialogClose(dialog);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);

        panel.add(messageLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1));

        animateDialogOpen(dialog);
    }

    /**
     * Adds a fade-in animation to dialogs
     */
    private void animateDialogOpen(JDialog dialog) {
        dialog.setOpacity(0.0f);
        dialog.setVisible(true);

        Timer timer = new Timer(15, null);
        timer.addActionListener(new ActionListener() {
            float opacity = 0.0f;
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.1f;
                dialog.setOpacity(Math.min(opacity, 1.0f));
                if (opacity >= 1.0f) {
                    timer.stop();
                }
            }
        });
        timer.start();
    }

    /**
     * Adds a fade-out animation to dialogs
     */
    private void animateDialogClose(JDialog dialog) {
        Timer timer = new Timer(15, null);
        timer.addActionListener(new ActionListener() {
            float opacity = 1.0f;
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.1f;
                dialog.setOpacity(Math.max(opacity, 0.0f));
                if (opacity <= 0.0f) {
                    timer.stop();
                    dialog.dispose();
                }
            }
        });
        timer.start();
    }

    /**
     * Helper method to close database resources
     */
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get database connection
     */
    private Connection getConnection() throws SQLException {
        // Replace with your actual database connection details
        String url = "jdbc:mysql://localhost:3306/HMsystem";
        String username = "root";
        String password = "Ashish030406";
        return DriverManager.getConnection(url, username, password);
    }

    private void startMessageRefreshTimer() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }

        // Increase the timer interval to reduce visual disruption
        refreshTimer = new Timer(3000, e -> {
            // Prevent concurrent refreshes
            if (isRefreshing) {
                return;
            }

            // Only refresh if the window is visible
            if (SwingUtilities.getWindowAncestor(messageListContent) != null &&
                    SwingUtilities.getWindowAncestor(messageListContent).isVisible()) {

                isRefreshing = true;

                try {
                    // Check if conversations have been updated
                    boolean conversationsUpdated = checkForNewOrUpdatedConversations();

                    if (conversationsUpdated) {
                        // Store current scroll position
                        JScrollPane listScrollPane = (JScrollPane) messageListContent.getParent().getParent();
                        int listScrollPos = listScrollPane.getVerticalScrollBar().getValue();
                        boolean wasAtScrollBottom = isAtScrollBottom(listScrollPane);

                        // Refresh conversations
                        loadConversations(messageListContent);

                        // Restore scroll position based on previous position
                        if (wasAtScrollBottom) {
                            SwingUtilities.invokeLater(() -> scrollToBottom(listScrollPane));
                        } else {
                            SwingUtilities.invokeLater(() -> listScrollPane.getVerticalScrollBar().setValue(listScrollPos));
                        }
                    }

                    // Only refresh messages if a conversation is open
                    if (currentOtherUserId > 0) {
                        // Check if this conversation has new messages
                        boolean hasNewMessages = checkForNewMessages(currentOtherUserId);

                        if (hasNewMessages) {
                            JScrollPane msgScrollPane = (JScrollPane) messagesDisplayPanel.getParent().getParent();
                            boolean wasAtBottom = isAtScrollBottom(msgScrollPane);

                            // Only update the messages
                            loadMessages(currentOtherUserId);

                            // If user was at bottom, scroll to bottom
                            if (wasAtBottom) {
                                SwingUtilities.invokeLater(() -> scrollToBottom(msgScrollPane));
                            }
                        }
                    }
                } finally {
                    isRefreshing = false;
                }
            }
        });

        refreshTimer.setInitialDelay(3000);
        refreshTimer.start();
    }

    // Helper method to check if view is scrolled to bottom
    private boolean isAtScrollBottom(JScrollPane scrollPane) {
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        return scrollBar.getValue() + scrollBar.getVisibleAmount() >= scrollBar.getMaximum() - 10;
    }

    // Helper method to scroll to bottom
    private void scrollToBottom(JScrollPane scrollPane) {
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    // Check if there are any new conversations or updates to existing ones
    private boolean checkForNewOrUpdatedConversations() {
        try {
            String query = "SELECT MAX(timestamp) as last_update " +
                    "FROM messages " +
                    "WHERE ? IN (sender_id, receiver_id)";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp lastUpdate = rs.getTimestamp("last_update");
                if (lastUpdate != null) {
                    long updateTime = lastUpdate.getTime();
                    if (updateTime > lastRefreshTimestamp) {
                        lastRefreshTimestamp = updateTime;
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error checking for conversation updates: " + e.getMessage());
            return false;
        }
    }

    // Check if the specific conversation has new messages
    private boolean checkForNewMessages(int otherUserId) {
        try {
            String query = "SELECT MAX(timestamp) as last_message " +
                    "FROM messages " +
                    "WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, otherUserId);
            stmt.setInt(3, otherUserId);
            stmt.setInt(4, currentUserId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp lastMessage = rs.getTimestamp("last_message");
                if (lastMessage != null) {
                    long messageTime = lastMessage.getTime();
                    Long lastKnownUpdate = conversationLastUpdated.get(otherUserId);

                    if (lastKnownUpdate == null || messageTime > lastKnownUpdate) {
                        conversationLastUpdated.put(otherUserId, messageTime);
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error checking for new messages: " + e.getMessage());
            return false;
        }
    }

    private void createMessagesPanel() { // Changed return type to void to match others
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Modern Header Panel with gradient effect
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(245, 247, 250), getWidth(), 0, new Color(240, 242, 245));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));

        JLabel titleLabel = new JLabel("Messages");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));

        JButton newMessageBtn = new JButton("New Message") {
            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                setForeground(Color.WHITE);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(SUCCESS_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(SUCCESS_COLOR.brighter());
                } else {
                    g2d.setColor(SUCCESS_COLOR);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        newMessageBtn.setPreferredSize(new Dimension(130, 40));
        newMessageBtn.addActionListener(e -> showNewMessageDialog());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(newMessageBtn, BorderLayout.EAST);

        // Modern Split Pane with subtle divider
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT) {
            @Override
            public void updateUI() {
                super.updateUI();
                setUI(new BasicSplitPaneUI() {
                    @Override
                    public BasicSplitPaneDivider createDefaultDivider() {
                        return new BasicSplitPaneDivider(this) {
                            @Override
                            public void paint(Graphics g) {
                                g.setColor(new Color(230, 230, 230));
                                g.fillRect(0, 0, getWidth(), getHeight());
                            }
                        };
                    }
                });
                setBorder(null);
                setDividerSize(1);
            }
        };
        splitPane.setDividerLocation(280);
        splitPane.setContinuousLayout(true);

        // Message List Panel with subtle shadow
        JPanel messageListPanel = new JPanel(new BorderLayout());
        messageListPanel.setBackground(Color.WHITE);
        messageListPanel.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JPanel messageListHeader = new JPanel(new BorderLayout());
        messageListHeader.setBackground(new Color(248, 249, 250));
        messageListHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel messageListTitle = new JLabel("Conversations");
        messageListTitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        messageListTitle.setForeground(new Color(52, 58, 64));

        // Search box for conversations
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 32));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search conversations...");
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        searchPanel.add(searchField);

        messageListHeader.add(messageListTitle, BorderLayout.NORTH);
        messageListHeader.add(searchPanel, BorderLayout.CENTER);

        messageListPanel.add(messageListHeader, BorderLayout.NORTH);

        // Message List Content with modern styling
        messageListContent = new JPanel();
        messageListContent.setLayout(new BoxLayout(messageListContent, BoxLayout.Y_AXIS));
        messageListContent.setBackground(Color.WHITE);

        // Load conversations from database
        loadConversations(messageListContent);

        JScrollPane listScrollPane = new JScrollPane(messageListContent);
        listScrollPane.setBorder(null);
        listScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        listScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        listScrollPane.getVerticalScrollBar().putClientProperty("JScrollBar.showButtons", false);
        messageListPanel.add(listScrollPane, BorderLayout.CENTER);

        // Message View Panel with modern styling
        JPanel messageViewPanel = new JPanel(new BorderLayout());
        messageViewPanel.setBackground(Color.WHITE);
        messageViewPanel.setBorder(new ShadowBorder());

        // Conversation Header with subtle gradient
        JPanel conversationHeader = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(248, 249, 250), 0, getHeight(), new Color(242, 244, 246));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        conversationHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 224, 228)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel headerContent = new JPanel(new BorderLayout(10, 0));
        headerContent.setOpaque(false);

        // Circular avatar for contact
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 160, 180));
                g2d.fillOval(0, 0, 36, 36);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2d.getFontMetrics();
                String initial = "A"; // First letter of contact name
                int textWidth = fm.stringWidth(initial);
                int textHeight = fm.getAscent();
                g2d.drawString(initial, (36 - textWidth) / 2, (36 - textHeight) / 2 + textHeight);
                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(36, 36);
            }
        };

        JPanel nameStatusPanel = new JPanel();
        nameStatusPanel.setLayout(new BoxLayout(nameStatusPanel, BoxLayout.Y_AXIS));
        nameStatusPanel.setOpaque(false);

        currentConversationLabel = new JLabel("Select a conversation");
        currentConversationLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        currentConversationLabel.setForeground(new Color(33, 37, 41));
        currentConversationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel statusLabel = new JLabel("Online");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(40, 167, 69));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        nameStatusPanel.add(currentConversationLabel);
        nameStatusPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        nameStatusPanel.add(statusLabel);

        headerContent.add(avatarPanel, BorderLayout.WEST);
        headerContent.add(nameStatusPanel, BorderLayout.CENTER);

        conversationHeader.add(headerContent, BorderLayout.CENTER);
        messageViewPanel.add(conversationHeader, BorderLayout.NORTH);

        // Messages Display Area with better padding
        messagesDisplayPanel = new JPanel();
        messagesDisplayPanel.setLayout(new BoxLayout(messagesDisplayPanel, BoxLayout.Y_AXIS));
        messagesDisplayPanel.setBackground(new Color(248, 249, 250));
        messagesDisplayPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane messagesScrollPane = new JScrollPane(messagesDisplayPanel);
        messagesScrollPane.setBorder(null);
        messagesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        messagesScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        messageViewPanel.add(messagesScrollPane, BorderLayout.CENTER);

        // Modern Message Input Area
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        inputPanel.setBackground(Color.WHITE);

        JPanel textAreaWrapper = new JPanel(new BorderLayout());
        textAreaWrapper.setBackground(Color.WHITE);
        textAreaWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        messageInputField = new JTextArea(2, 20);
        messageInputField.setLineWrap(true);
        messageInputField.setWrapStyleWord(true);
        messageInputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageInputField.setBorder(null);

        // Add placeholder text
        messageInputField.setText("Type a message...");
        messageInputField.setForeground(new Color(134, 142, 150));

        messageInputField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (messageInputField.getText().equals("Type a message...")) {
                    messageInputField.setText("");
                    messageInputField.setForeground(new Color(33, 37, 41));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (messageInputField.getText().isEmpty()) {
                    messageInputField.setText("Type a message...");
                    messageInputField.setForeground(new Color(134, 142, 150));
                }
            }
        });

        // Add key listener for Enter key to send message
        messageInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume();
                    sendMessage();
                }
            }
        });

        JScrollPane inputScrollPane = new JScrollPane(messageInputField);
        inputScrollPane.setBorder(null);
        inputScrollPane.setViewportBorder(null);
        textAreaWrapper.add(inputScrollPane, BorderLayout.CENTER);

        // Modern send button with circular design
        JButton sendButton = new JButton() {
            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setPreferredSize(new Dimension(40, 40));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(WARNING_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(WARNING_COLOR.brighter());
                } else {
                    g2d.setColor(WARNING_COLOR);
                }

                g2d.fillOval(0, 0, 40, 40);

                // Draw paper plane icon
                g2d.setColor(Color.WHITE);
                int[] xPoints = {12, 28, 20};
                int[] yPoints = {12, 20, 28};
                g2d.fillPolygon(xPoints, yPoints, 3);

                g2d.dispose();
            }
        };
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(textAreaWrapper, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        messageViewPanel.add(inputPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(messageListPanel);
        splitPane.setRightComponent(messageViewPanel);

        panel.add(headerPanel, BorderLayout.PAGE_START);
        panel.add(splitPane, BorderLayout.CENTER);

        startMessageRefreshTimer();

        panelMap.put("Messages", panel); // Add the panel to panelMap
    }



    private void loadConversations(JPanel messageListContent) {
        messageListContent.removeAll();
        messageListContent.setLayout(new BoxLayout(messageListContent, BoxLayout.Y_AXIS));

        // Track users we've already added to prevent duplicates
        Map<Integer, JPanel> userPanels = new HashMap<>();

        try {
            System.out.println("Loading conversations for user ID: " + currentUserId);

            // Simpler query that works across different database systems
            String query =
                    "SELECT m.message_id, m.sender_id, m.receiver_id, m.message_text, m.timestamp, m.is_read, " +
                            "u.user_id, CONCAT(u.first_name, ' ', u.last_name) as name " +
                            "FROM messages m " +
                            "JOIN users u ON (m.sender_id = u.user_id OR m.receiver_id = u.user_id) " +
                            "WHERE (m.sender_id = ? OR m.receiver_id = ?) " +
                            "AND u.user_id != ? " +
                            "ORDER BY m.timestamp DESC";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, currentUserId);

            ResultSet rs = stmt.executeQuery();
            boolean hasResults = false;

            while (rs.next()) {
                hasResults = true;
                int senderId = rs.getInt("sender_id");
                int receiverId = rs.getInt("receiver_id");

                // Determine which user is the conversation partner
                int otherUserId = (senderId == currentUserId) ? receiverId : senderId;

                // Skip if we've already added this user
                if (userPanels.containsKey(otherUserId)) {
                    continue;
                }

                String userName = rs.getString("name");
                String message = rs.getString("message_text");
                String time = new SimpleDateFormat("h:mm a").format(rs.getTimestamp("timestamp"));
                boolean unread = !rs.getBoolean("is_read") && senderId != currentUserId;

                System.out.println("Found conversation with: " + userName + " (ID: " + otherUserId + ")");

                // Create truncated message preview if needed
                String messagePreview = (message != null && message.length() > 30) ?
                        message.substring(0, 30) + "..." : message;

                JPanel messageItem = createMessageListItem(userName, messagePreview, time, unread, otherUserId);

                userPanels.put(otherUserId, messageItem);
            }

            // Add all conversation panels to the UI in order
            if (!userPanels.isEmpty()) {
                for (JPanel panel : userPanels.values()) {
                    messageListContent.add(panel);
                    messageListContent.add(Box.createRigidArea(new Dimension(0, 5)));
                }
            } else if (!hasResults) {
                System.out.println("No conversations found in database");
                JLabel noMessagesLabel = new JLabel("No conversations found");
                noMessagesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                noMessagesLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                messageListContent.add(Box.createVerticalGlue());
                messageListContent.add(noMessagesLabel);
                messageListContent.add(Box.createVerticalGlue());
            }

            messageListContent.revalidate();
            messageListContent.repaint();

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();

            // Show error in UI with more details
            messageListContent.setLayout(new BorderLayout());
            JPanel errorPanel = new JPanel();
            errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));

            JLabel errorLabel = new JLabel("Error loading conversations");
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel errorDetailsLabel = new JLabel("Error: " + e.getMessage());
            errorDetailsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            errorDetailsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            errorPanel.add(Box.createVerticalGlue());
            errorPanel.add(errorLabel);
            errorPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            errorPanel.add(errorDetailsLabel);
            errorPanel.add(Box.createVerticalGlue());

            messageListContent.add(errorPanel, BorderLayout.CENTER);
        }
    }



    private void sendMessage() {
        String messageText = messageInputField.getText().trim();
        if (messageText.isEmpty() || messageText.equals("Type a message...") || currentOtherUserId == 0) {
            return;
        }

        try {
            String query = "INSERT INTO messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentOtherUserId);
            stmt.setString(3, messageText);
            stmt.executeUpdate();

            // Clear input field
            messageInputField.setText("");
            messageInputField.setForeground(Color.BLACK);

            // Refresh the conversation
            loadMessages(currentOtherUserId);

            // Refresh the conversation list
            loadConversations(messageListContent);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error sending message: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createMessageListItem(String sender, String preview, String time, boolean unread, int userId) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(unread ? new Color(240, 248, 255) : Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BACKGROUND_COLOR),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panel.putClientProperty("userId", userId);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setBackground(panel.getBackground());

        JLabel senderLabel = new JLabel(sender);
        senderLabel.setFont(new Font("Segoe UI", unread ? Font.BOLD : Font.PLAIN, 14));
        senderLabel.setForeground(DARK_COLOR);

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(PRIMARY_COLOR);

        headerPanel.add(senderLabel);
        headerPanel.add(Box.createHorizontalGlue());
        headerPanel.add(timeLabel);

        JLabel previewLabel = new JLabel(preview);
        previewLabel.setFont(new Font("Segoe UI", unread ? Font.BOLD : Font.PLAIN, 12));
        previewLabel.setForeground(DARK_COLOR);

        panel.add(headerPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(previewLabel);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                loadMessages(userId);
            }
        });

        return panel;
    }

    private void loadMessages(int otherUserId) {
        messagesDisplayPanel.removeAll();
        currentOtherUserId = otherUserId; // Store for sending messages

        try {
            // Get the other user's name for the header
            String nameQuery = "SELECT CONCAT(first_name, ' ', last_name) as name FROM users WHERE user_id = ?";
            PreparedStatement nameStmt = connection.prepareStatement(nameQuery);
            nameStmt.setInt(1, otherUserId);
            ResultSet nameRs = nameStmt.executeQuery();
            if (nameRs.next()) {
                currentConversationLabel.setText("Conversation with " + nameRs.getString("name"));
            }

            // Load the messages
            String query = "SELECT m.*, CONCAT(u.first_name, ' ', u.last_name) as sender_name " +
                    "FROM messages m " +
                    "JOIN users u ON m.sender_id = u.user_id " +
                    "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR " +
                    "(m.sender_id = ? AND m.receiver_id = ?) " +
                    "ORDER BY m.timestamp ASC";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, otherUserId);
            stmt.setInt(3, otherUserId);
            stmt.setInt(4, currentUserId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                boolean isCurrentUser = rs.getInt("sender_id") == currentUserId;
                String senderName = rs.getString("sender_name");
                String messageText = rs.getString("message_text");
                String time = new SimpleDateFormat("MMM d, h:mm a").format(rs.getTimestamp("timestamp"));

                JPanel messagePanel = createMessageBubble(
                        isCurrentUser ? "You" : senderName,
                        messageText,
                        time,
                        isCurrentUser
                );

                messagesDisplayPanel.add(messagePanel);
                messagesDisplayPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                // Mark as read if current user is receiver
                if (!isCurrentUser && !rs.getBoolean("is_read")) {
                    markMessageAsRead(rs.getInt("message_id"));
                }
            }

            messagesDisplayPanel.revalidate();
            messagesDisplayPanel.repaint();

            // Scroll to bottom
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = ((JScrollPane)messagesDisplayPanel.getParent().getParent()).getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading messages: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Modern message bubble with enhanced visual design and improved text contrast
    private JPanel createMessageBubble(String sender, String message, String time, boolean isCurrentUser) {
        JPanel bubblePanel = new JPanel();
        bubblePanel.setLayout(new BoxLayout(bubblePanel, BoxLayout.Y_AXIS));
        bubblePanel.setAlignmentX(isCurrentUser ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        bubblePanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        bubblePanel.setMaximumSize(new Dimension(450, Integer.MAX_VALUE));
        bubblePanel.setOpaque(false);

        JPanel contentPanel;

        // Use rounded bubble with gradient for current user
        if (isCurrentUser) {
            contentPanel = new JPanel(new BorderLayout(0, 5)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Create gradient for user's messages - brighter blue for better contrast
                    GradientPaint gradient = new GradientPaint(
                            0, 0,
                            new Color(0, 123, 255),  // Primary blue
                            getWidth(), getHeight(),
                            new Color(0, 105, 217)   // Slightly darker blue
                    );
                    g2d.setPaint(gradient);

                    // Rounded corners
                    int arc = 18;
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                    g2d.dispose();
                }
            };
        } else {
            contentPanel = new JPanel(new BorderLayout(0, 5)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(240, 240, 240)); // Light gray background

                    // Rounded corners
                    int arc = 18;
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                    g2d.dispose();
                }
            };
        }

        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Create a smaller avatar for the other user
        if (!isCurrentUser) {
            JPanel avatarPanel = new JPanel(new BorderLayout());
            avatarPanel.setOpaque(false);
            avatarPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

            JPanel avatar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(0, 160, 180));
                    g2d.fillOval(0, 0, 28, 28);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    FontMetrics fm = g2d.getFontMetrics();
                    String initial = sender.substring(0, 1).toUpperCase();
                    int textWidth = fm.stringWidth(initial);
                    int textHeight = fm.getAscent();
                    g2d.drawString(initial, (28 - textWidth) / 2, (28 - textHeight) / 2 + textHeight);
                    g2d.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(28, 28);
                }
            };

            avatarPanel.setPreferredSize(new Dimension(36, 28));
            avatarPanel.add(avatar, BorderLayout.WEST);

            JPanel bubbleWithAvatar = new JPanel(new BorderLayout());
            bubbleWithAvatar.setOpaque(false);
            bubbleWithAvatar.add(avatarPanel, BorderLayout.WEST);
            bubbleWithAvatar.add(contentPanel, BorderLayout.CENTER);
            bubblePanel.add(bubbleWithAvatar);
        } else {
            bubblePanel.add(contentPanel);
        }

        // Sender name with improved contrast
        JLabel senderLabel = new JLabel(sender);
        senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Changed to BOLD for better visibility
        senderLabel.setForeground(isCurrentUser ? Color.WHITE : new Color(33, 37, 41)); // Dark color for recipient bubbles

        // Message content with improved contrast
        JTextArea messageArea = new JTextArea(message);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setOpaque(false);
        messageArea.setBorder(null);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Slightly larger font

        // High contrast text colors
        if (isCurrentUser) {
            messageArea.setForeground(Color.WHITE); // Pure white for maximum contrast on blue
        } else {
            messageArea.setForeground(new Color(0, 0, 0)); // Pure black for maximum contrast on light gray
        }

        // Set preferred width
        int preferredWidth = 350;
        messageArea.setPreferredSize(new Dimension(preferredWidth, 20));

        // Time with improved contrast
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        // Higher contrast time stamps
        if (isCurrentUser) {
            timeLabel.setForeground(new Color(255, 255, 255, 200)); // Nearly white, slightly transparent
        } else {
            timeLabel.setForeground(new Color(90, 90, 90)); // Darker gray for better contrast
        }
        timeLabel.setHorizontalAlignment(JLabel.RIGHT);

        contentPanel.add(senderLabel, BorderLayout.NORTH);
        contentPanel.add(messageArea, BorderLayout.CENTER);
        contentPanel.add(timeLabel, BorderLayout.SOUTH);

        return bubblePanel;
    }

    // Format message to handle URLs, emojis, etc.
    private String formatMessage(String message) {
        // Convert URLs to clickable links
        String formattedMessage = message.replaceAll("(https?://\\S+)", "<a href='$1' style='color:inherit'>$1</a>");

        // Convert emoji codes to actual emojis if needed
        formattedMessage = formattedMessage.replace(":)", "😊")
                .replace(":(", "😔")
                .replace(":D", "😃")
                .replace(";)", "😉");

        return formattedMessage;
    }

    // Shadow border for panels
    private class ShadowBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw subtle shadow
            for (int i = 0; i < 3; i++) {
                g2d.setColor(new Color(0, 0, 0, 3 * (3 - i)));
                g2d.drawRoundRect(x + i, y + i, width - 2 * i - 1, height - 2 * i - 1, 8, 8);
            }

            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(3, 3, 3, 3);
        }
    }

    private void markMessageAsRead(int messageId) {
        try {
            String query = "UPDATE messages SET is_read = TRUE WHERE message_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, messageId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error marking message as read: " + e.getMessage());
        }
    }

    private void showNewMessageDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Message", true);
        dialog.setLayout(new BorderLayout(0, 0));
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        // Define modern color scheme
        Color PRIMARY_COLOR = new Color(64, 81, 181);    // Deep blue
        Color ACCENT_COLOR = new Color(255, 64, 129);    // Pink accent
        Color LIGHT_COLOR = new Color(250, 250, 250);    // Off-white background
        Color TEXT_COLOR = new Color(33, 33, 33);        // Dark gray text
        Color BORDER_COLOR = new Color(224, 224, 224);   // Light gray borders

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));
        mainPanel.setBackground(LIGHT_COLOR);

        // Header panel with title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(LIGHT_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Create New Message");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Form panel using GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(LIGHT_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Recipient field
        JLabel recipientLabel = new JLabel("To:");
        recipientLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        recipientLabel.setForeground(TEXT_COLOR);

        JComboBox<DoctorDashboard.UserComboItem> recipientCombo = new JComboBox<>();
        recipientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recipientCombo.setBackground(Color.WHITE);

        // Create a wrapper panel for the combo box to add padding
        JPanel comboWrapper = new JPanel(new BorderLayout());
        comboWrapper.setBackground(Color.WHITE);
        comboWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        comboWrapper.add(recipientCombo, BorderLayout.CENTER);

        // Load recipients
        try {
            String query = "SELECT user_id, CONCAT(first_name, ' ', last_name) as name FROM users WHERE user_id != ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                recipientCombo.addItem(new DoctorDashboard.UserComboItem(
                        rs.getInt("user_id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error loading users: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        // Message area
        JLabel messageLabel = new JLabel("Message:");
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        messageLabel.setForeground(TEXT_COLOR);

        JTextArea messageArea = new JTextArea();
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageArea.setBackground(Color.WHITE);
        messageArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.setBackground(Color.WHITE);

        // Add components to form with GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(recipientLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(comboWrapper, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(15, 5, 8, 5); // Extra spacing between rows
        formPanel.add(messageLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.8;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(scrollPane, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(LIGHT_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JButton cancelButton = createModernButton("Cancel", LIGHT_COLOR, TEXT_COLOR);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        JButton sendButton = createModernButton("Send", ACCENT_COLOR, Color.WHITE);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));

        buttonPanel.add(cancelButton);
        buttonPanel.add(sendButton);

        // Add panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Action listeners
        sendButton.addActionListener(e -> {
            if (recipientCombo.getSelectedItem() == null || messageArea.getText().trim().isEmpty()) {
                // Modern error dialog
                JOptionPane optionPane = new JOptionPane(
                        "Please select a recipient and enter a message.",
                        JOptionPane.WARNING_MESSAGE);
                optionPane.setBackground(LIGHT_COLOR);
                JDialog errorDialog = optionPane.createDialog(dialog, "Missing Information");
                errorDialog.setVisible(true);
                return;
            }

            DoctorDashboard.UserComboItem recipient = (DoctorDashboard.UserComboItem) recipientCombo.getSelectedItem();
            try {
                String query = "INSERT INTO messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, currentUserId);
                stmt.setInt(2, recipient.getId());
                stmt.setString(3, messageArea.getText().trim());
                stmt.executeUpdate();

                // Refresh conversation list
                loadConversations(messageListContent);

                // Show the new conversation
                loadMessages(recipient.getId());

                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error sending message: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        // Add hover effects to buttons
        addHoverEffect(sendButton, ACCENT_COLOR, new Color(236, 64, 122));
        addHoverEffect(cancelButton, LIGHT_COLOR, new Color(240, 240, 240));

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private JButton createModernButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(new Color(
                            Math.max(bgColor.getRed() - 30, 0),
                            Math.max(bgColor.getGreen() - 30, 0),
                            Math.max(bgColor.getBlue() - 30, 0)
                    ));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(
                            Math.max(bgColor.getRed() - 20, 0),
                            Math.max(bgColor.getGreen() - 20, 0),
                            Math.max(bgColor.getBlue() - 20, 0)
                    ));
                } else {
                    g2d.setColor(bgColor);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2d.dispose();

                super.paintComponent(g);
            }
        };

        button.setForeground(fgColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false); // Important for custom painting
        button.setBorderPainted(false);    // Important for custom painting
        button.setOpaque(false);           // Important for custom painting
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    // Helper method for button hover effects
    private void addHoverEffect(JButton button, Color defaultColor, Color hoverColor) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(defaultColor);
            }
        });
    }



    private static class UserComboItem {
        private int id;
        private String name;

        public UserComboItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private void createReportsPanel() {
        JPanel reportsPanel = new JPanel(new BorderLayout(10, 10));
        reportsPanel.setBackground(BACKGROUND_COLOR);
        reportsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title panel with heading and refresh button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        JLabel titleLabel = new JLabel("My Medical Reports");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));

        JButton refreshButton = createModernButton("Refresh",DARK_COLOR,Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> loadPatientReports());

        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(refreshButton, BorderLayout.EAST);

        // Create table model
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Add columns
        tableModel.addColumn("Report ID");
        tableModel.addColumn("Type");
        tableModel.addColumn("Date");
        tableModel.addColumn("Status");
        tableModel.addColumn("Description");
        tableModel.addColumn("File Name");

        // Create table
        JTable reportsTable = new JTable(tableModel);
        reportsTable.setRowHeight(30);
        reportsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        reportsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        reportsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportsTable.setShowGrid(false);
        reportsTable.setIntercellSpacing(new Dimension(0, 0));

        // Customize column widths
        reportsTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Report ID
        reportsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Type
        reportsTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Date
        reportsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Status
        reportsTable.getColumnModel().getColumn(4).setPreferredWidth(300); // Description
        reportsTable.getColumnModel().getColumn(5).setPreferredWidth(150); // File Name

        // Customize the appearance
        reportsTable.setRowHeight(35);
        reportsTable.setShowVerticalLines(true);
        reportsTable.setShowHorizontalLines(true);
        reportsTable.setGridColor(new Color(230, 230, 230));

        // Add zebra striping
        reportsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                if (!isSelected) {
                    comp.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 250));
                } else {
                    comp.setBackground(new Color(66, 139, 202));
                    comp.setForeground(Color.WHITE);
                }

                // Center-align content
                ((JLabel) comp).setHorizontalAlignment(JLabel.CENTER);

                // Custom rendering for the status column
                if (column == 3) { // Status column
                    String status = value.toString().toLowerCase();
                    if (status.equals("pending")) {
                        comp.setForeground(new Color(255, 153, 0));
                    } else if (status.equals("completed")) {
                        comp.setForeground(new Color(0, 153, 51));
                    } else if (status.equals("urgent")) {
                        comp.setForeground(new Color(204, 0, 0));
                    } else {
                        comp.setForeground(table.getForeground());
                    }

                    if (isSelected) {
                        comp.setForeground(Color.WHITE);
                    }
                } else if (!isSelected) {
                    comp.setForeground(table.getForeground());
                }

                return comp;
            }
        });

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(reportsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Create info panel for when no reports are available
        JPanel noReportsPanel = new JPanel(new BorderLayout());
        noReportsPanel.setBackground(Color.WHITE);
        JLabel noReportsLabel = new JLabel("No reports available", JLabel.CENTER);
        noReportsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        noReportsLabel.setForeground(new Color(150, 150, 150));
        noReportsPanel.add(noReportsLabel, BorderLayout.CENTER);

        // Create a card layout to switch between table and no reports message
        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.add(scrollPane, "REPORTS_TABLE");
        contentPanel.add(noReportsPanel, "NO_REPORTS");

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setBackground(BACKGROUND_COLOR);

        JButton viewButton = createModernButton("View Report",SUCCESS_COLOR,Color.WHITE);
        viewButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        viewButton.setFocusPainted(false);
        viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewButton.setEnabled(false);

        JButton downloadButton = createModernButton("Download pdf",WARNING_COLOR,Color.WHITE);
        downloadButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        downloadButton.setFocusPainted(false);
        downloadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        downloadButton.setEnabled(false);

        buttonsPanel.add(viewButton);
        buttonsPanel.add(downloadButton);

        // Add selection listener to enable/disable buttons
        reportsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = reportsTable.getSelectedRow() != -1;
            viewButton.setEnabled(rowSelected);
            downloadButton.setEnabled(rowSelected);
        });

        // Add action listeners to buttons
        viewButton.addActionListener(e -> {
            int selectedRow = reportsTable.getSelectedRow();
            if (selectedRow != -1) {
                String reportId = reportsTable.getValueAt(selectedRow, 0).toString();
                String fileName = reportsTable.getValueAt(selectedRow, 5).toString();
                showReportViewer(reportId, fileName);
            }
        });

        downloadButton.addActionListener(e -> {
            int selectedRow = reportsTable.getSelectedRow();
            if (selectedRow != -1) {
                String reportId = reportsTable.getValueAt(selectedRow, 0).toString();
                String fileName = reportsTable.getValueAt(selectedRow, 5).toString();
                downloadReport(reportId, fileName);
            }
        });

        // Assemble the panel
        reportsPanel.add(titlePanel, BorderLayout.NORTH);
        reportsPanel.add(contentPanel, BorderLayout.CENTER);
        reportsPanel.add(buttonsPanel, BorderLayout.SOUTH);

        // Store references for later use
        reportsPanel.putClientProperty("tableModel", tableModel);
        reportsPanel.putClientProperty("contentPanel", contentPanel);

        // Add the panel to the map
        panelMap.put("Reports", reportsPanel);

        // Load reports data
        loadPatientReports();
    }




    /**
     * Loads patient reports from the database
     */
    private void loadPatientReports() {
        JPanel reportsPanel = panelMap.get("Reports");
        DefaultTableModel tableModel = (DefaultTableModel) reportsPanel.getClientProperty("tableModel");
        JPanel contentPanel = (JPanel) reportsPanel.getClientProperty("contentPanel");
        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();

        // Clear existing data
        tableModel.setRowCount(0);

        // Get the current patient ID (assuming it's stored somewhere in your application)
        int currentPatientId = getCurrentPatientId();

        try (Connection conn = getDatabaseConnection()) {
            String query = "SELECT report_id, report_type, report_date, status, description, file_name " +
                    "FROM reports WHERE patient_id = ? ORDER BY report_date DESC";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, currentPatientId);

                try (ResultSet rs = stmt.executeQuery()) {
                    boolean hasReports = false;

                    while (rs.next()) {
                        hasReports = true;
                        tableModel.addRow(new Object[] {
                                rs.getInt("report_id"),
                                rs.getString("report_type"),
                                rs.getDate("report_date"),
                                rs.getString("status"),
                                rs.getString("description"),
                                rs.getString("file_name")
                        });
                    }

                    // Show appropriate panel based on whether reports exist
                    if (hasReports) {
                        cardLayout.show(contentPanel, "REPORTS_TABLE");
                    } else {
                        cardLayout.show(contentPanel, "NO_REPORTS");
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    reportsPanel,
                    "Error loading reports: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    /**
     * Gets the current logged-in patient's ID
     */
    private int getCurrentPatientId() {
        // Replace this with your actual method to get the current patient ID
        // This could be from session data, a global variable, etc.
        return 7; // For demonstration purposes
    }

    /**
     * Opens a report viewer for the selected report
     */
    /**
     * Opens a simple report viewer for the selected report
     */
    /**
     * Opens a simple report viewer to display the report description
     */
    private void showReportViewer(String reportId, String fileName) {
        // Create a simple dialog to display report information
        JDialog reportDialog = new JDialog();
        reportDialog.setTitle("Report Details: " + fileName);
        reportDialog.setSize(600, 400);
        reportDialog.setLocationRelativeTo(null);
        reportDialog.setLayout(new BorderLayout());

        // Create a panel to display report information
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        infoPanel.setBackground(Color.WHITE);

        // Create a text area to display report description
        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setMargin(new Insets(10, 10, 10, 10));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        // Add scroll pane for description
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Report Description"));
        infoPanel.add(scrollPane, BorderLayout.CENTER);

        // Add report metadata panel at top
        JPanel metadataPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        metadataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Report Information"));
        metadataPanel.setBackground(Color.WHITE);

        reportDialog.add(infoPanel, BorderLayout.CENTER);

        // Add button panel at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton downloadButton = createModernButton("Download PDF", WARNING_COLOR, Color.WHITE);
        downloadButton.addActionListener(e -> {
            downloadReport(reportId, fileName);
        });

        JButton closeButton = createModernButton("Close", DARK_COLOR, Color.WHITE);
        closeButton.addActionListener(e -> reportDialog.dispose());

        buttonPanel.add(downloadButton);
        buttonPanel.add(closeButton);
        reportDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Try to load report data
        try {
            ReportDetails details = getReportDetailsFromDatabase(reportId);
            if (details != null) {
                // Populate metadata
                metadataPanel.add(new JLabel("Report ID:"));
                metadataPanel.add(new JLabel(reportId));
                metadataPanel.add(new JLabel("Type:"));
                metadataPanel.add(new JLabel(details.type));
                metadataPanel.add(new JLabel("Date:"));
                metadataPanel.add(new JLabel(details.date.toString()));

                // Set description
                descriptionArea.setText(details.description);
            } else {
                descriptionArea.setText("No information available for this report.");
            }
        } catch (Exception e) {
            descriptionArea.setText("Error loading report information: " + e.getMessage());
            e.printStackTrace();
        }

        infoPanel.add(metadataPanel, BorderLayout.NORTH);
        reportDialog.setVisible(true);
    }

    /**
     * Helper class to store report details
     */
    private static class ReportDetails {
        String type;
        Date date;
        String status;
        String description;
    }

    /**
     * Gets report details from database
     */
    private ReportDetails getReportDetailsFromDatabase(String reportId) throws SQLException {
        try (Connection conn = getDatabaseConnection()) {
            String query = "SELECT report_type, report_date, status, description FROM reports WHERE report_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, Integer.parseInt(reportId));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        ReportDetails details = new ReportDetails();
                        details.type = rs.getString("report_type");
                        details.date = rs.getDate("report_date");
                        details.status = rs.getString("status");
                        details.description = rs.getString("description");
                        return details;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Downloads the selected report
     */
    private void downloadReport(String reportId, String fileName) {
        // Implementation for downloading the report
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report");
        fileChooser.setSelectedFile(new File(fileName));

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            try (Connection conn = getDatabaseConnection()) {
                // Here you would implement the actual file download from the database
                // This is a simplified example
                JOptionPane.showMessageDialog(
                        null,
                        "Report saved to: " + fileToSave.getAbsolutePath(),
                        "Download Complete",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Error downloading report: " + e.getMessage(),
                        "Download Error",
                        JOptionPane.ERROR_MESSAGE
                );
                e.printStackTrace();
            }
        }
    }

    /**
     * Helper method to get database connection
     */
    private void createChatbotPanel() {
        // Create main panel with custom background
        JPanel chatbotPanel = new JPanel(new BorderLayout(0, 0));
        chatbotPanel.setBackground(new Color(240, 242, 245)); // Light gray background

        // Header panel with gradient
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(79, 134, 247),
                        getWidth(), 0, new Color(67, 97, 238));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 60));

        // Add chatbot title to header
        JLabel titleLabel = new JLabel("Medical Assistant");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Online status indicator
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        statusPanel.setOpaque(false);
        JLabel statusDot = new JLabel("●");
        statusDot.setForeground(new Color(50, 205, 50)); // Green
        statusDot.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel statusText = new JLabel("Online");
        statusText.setForeground(new Color(220, 220, 220));
        statusText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusPanel.add(statusDot);
        statusPanel.add(statusText);
        headerPanel.add(statusPanel, BorderLayout.EAST);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        // Create a container panel with fixed width
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(new Color(240, 242, 245));
        containerPanel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE)); // Reduced width
        containerPanel.setPreferredSize(new Dimension(500, Integer.MAX_VALUE)); // Reduced width

        // Create a custom panel for chat messages
        JPanel chatMessagesPanel = new JPanel();
        chatMessagesPanel.setLayout(new BoxLayout(chatMessagesPanel, BoxLayout.Y_AXIS));
        chatMessagesPanel.setBackground(new Color(240, 242, 245));

        // Create scrollable panel for messages
        JScrollPane scrollPane = new JScrollPane(chatMessagesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Store the panel for later use when adding messages
        chatArea = new JTextArea(0, 0); // We'll use this as storage only, not display
        chatArea.setVisible(false);

        // Welcome message
        addBotMessage(chatMessagesPanel, "Hello! I'm your medical assistant chatbot. I can help you with:\n\n" +
                        "1. ask for any type of symptoms and medication'\n"+
                "2. Check appointment availability: Type 'check YYYY-MM-DD HH:MM:SS'\n" +
                "3. Book an appointment: Type 'book YYYY-MM-DD HH:MM:SS patient_id doctor_name [description]'\n\n" );

        // Modern input area with rounded borders
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(218, 220, 224)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        // Custom input field with rounded corners
        userInput = new JTextField();
        userInput.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(Color.WHITE, 8),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        userInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Modern send button
        JButton sendButton = new JButton();
        sendButton.setPreferredSize(new Dimension(40, 40));
        sendButton.setBackground(new Color(79, 134, 247));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setOpaque(true);

        // Custom send icon
        sendButton.setIcon(new ImageIcon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                int[] xPoints = {x + 10, x + 10, x + 20};
                int[] yPoints = {y + 10, y + 20, y + 15};
                g2d.fillPolygon(xPoints, yPoints, 3);
                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 30;
            }

            @Override
            public int getIconHeight() {
                return 30;
            }
        });

        // Make the send button round
        sendButton.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(c.getBackground());
                g2d.fillOval(0, 0, c.getWidth(), c.getHeight());
                super.paint(g2d, c);
                g2d.dispose();
            }
        });

        // Action listeners for sending messages
        ActionListener sendAction = e -> sendMessages(chatMessagesPanel, scrollPane);
        sendButton.addActionListener(sendAction);
        userInput.addActionListener(sendAction);

        // Add hover effect to send button
        sendButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                sendButton.setBackground(new Color(67, 97, 238));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                sendButton.setBackground(new Color(79, 134, 247));
            }
        });

        // Input field placeholder text
        userInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (userInput.getText().equals("Type your message here...")) {
                    userInput.setText("");
                    userInput.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (userInput.getText().isEmpty()) {
                    userInput.setForeground(Color.GRAY);
                    userInput.setText("Type your message here...");
                }
            }
        });
        userInput.setForeground(Color.GRAY);
        userInput.setText("Type your message here...");

        // Assemble input components
        inputPanel.add(userInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Add components to container panel
        containerPanel.add(scrollPane, BorderLayout.CENTER);
        containerPanel.add(inputPanel, BorderLayout.SOUTH);

        // Create a wrapper panel to center the container
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(new Color(240, 242, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        wrapperPanel.add(containerPanel, gbc);

        // Add all components to main panel
        chatbotPanel.add(headerPanel, BorderLayout.NORTH);
        chatbotPanel.add(wrapperPanel, BorderLayout.CENTER);

        // Store panel in map
        panelMap.put("Chatbot", chatbotPanel);
    }

    // Helper method to add bot messages with styling (left side)
    // Updated method to add bot messages with enhanced styling and structure
    private void addBotMessage(JPanel chatPanel, String message) {
        JPanel messageContainer = new JPanel();
        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.X_AXIS));
        messageContainer.setBackground(new Color(240, 242, 245));
        messageContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatar icon for bot
        JLabel avatarLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(79, 134, 247));
                g2d.fillOval(0, 0, 30, 30);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2d.drawString("M", 10, 20);
                g2d.dispose();
            }
        };
        avatarLabel.setPreferredSize(new Dimension(30, 30));

        // Message bubble with enhanced styling
        JPanel bubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Add subtle shadow effect
                g2d.setColor(new Color(0, 0, 0, 15));
                g2d.drawRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 15, 15);

                g2d.dispose();
            }
        };
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // Format the message with proper structure
        String formattedMessage = formatBotMessage(message);

        // Text content with increased width for better readability
        JLabel textLabel = new JLabel("<html><body style='width: 280px;'>" + formattedMessage + "</body></html>");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bubble.add(textLabel, BorderLayout.CENTER);

        // Add components
        messageContainer.add(Box.createRigidArea(new Dimension(5, 0)));
        messageContainer.add(avatarLabel);
        messageContainer.add(Box.createRigidArea(new Dimension(8, 0)));
        messageContainer.add(bubble);
        messageContainer.add(Box.createHorizontalGlue());

        // Add to chat panel with spacing
        chatPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        chatPanel.add(messageContainer);

        // Update text area for storage
        chatArea.append("Medical Assistant: " + message + "\n\n");
    }

    // New helper method to format bot messages with proper structure
    private String formatBotMessage(String message) {
        // Check if message contains bullet points or numbered lists
        if (message.contains("\n1.") || message.contains("\n2.") || message.contains("\n•")) {
            // Message already has structure, just enhance formatting
            return enhanceMessageFormatting(message);
        }

        // For welcome message or instruction message
        if (message.contains("I'm your medical assistant") || message.contains("help you with")) {
            return "<div style='color:#4F86F7; font-weight:bold; font-size:15px; margin-bottom:6px;'>Medical Assistant</div>" +
                    enhanceMessageFormatting(message);
        }

        // For appointment availability responses
        if (message.contains("appointment slot")) {
            if (message.contains("available.")) {
                return "<div style='color:#4F86F7; font-weight:bold; font-size:15px; margin-bottom:6px;'>Appointment Check</div>" +
                        "<div style='color:#28a745; font-weight:bold;'>" + message.replace("is available.", "is available ✓") + "</div>";
            } else {
                return "<div style='color:#4F86F7; font-weight:bold; font-size:15px; margin-bottom:6px;'>Appointment Check</div>" +
                        "<div style='color:#dc3545; font-weight:bold;'>" + message.replace("is already booked.", "is already booked ✗") + "</div>";
            }
        }

        // For booking confirmation
        if (message.contains("successfully booked")) {
            return "<div style='color:#4F86F7; font-weight:bold; font-size:15px; margin-bottom:6px;'>Booking Confirmation</div>" +
                    "<div style='color:#28a745;'>" + message.replace("successfully booked", "<b>successfully booked</b>") + "</div>";
        }

        // For booking errors
        if (message.contains("Failed to book")) {
            return "<div style='color:#4F86F7; font-weight:bold; font-size:15px; margin-bottom:6px;'>Booking Error</div>" +
                    "<div style='color:#dc3545;'>" + message + "</div>";
        }

        // For invalid format messages
        if (message.contains("Invalid format") || message.contains("Invalid booking format")) {
            return "<div style='color:#4F86F7; font-weight:bold; font-size:15px; margin-bottom:6px;'>Format Guide</div>" +
                    "<div style='color:#ffc107;'>" + message + "</div>";
        }

        // Default case: add a heading based on content keywords
        if (message.toLowerCase().contains("medication") || message.toLowerCase().contains("medicine")) {
            return "<div style='color:#4F86F7; font-weight:bold; font-size:15px; margin-bottom:6px;'>Medication Information</div>" +
                    enhanceMessageFormatting(message);
        } else if (message.toLowerCase().contains("symptom")) {
            return "<div style='color:#4F86F7; font-weight:bold; font-size:15px; margin-bottom:6px;'>Symptom Analysis</div>" +
                    enhanceMessageFormatting(message);
        } else if (message.toLowerCase().contains("doctor") || message.toLowerCase().contains("specialist")) {
            return "<div style='color:#4F86F7; font-weight:bold; font-size:15px; margin-bottom:6px;'>Doctor Information</div>" +
                    enhanceMessageFormatting(message);
        } else {
            // Generic response heading
            return "<div style='color:#4F86F7; font-weight:bold; font-size:15px; margin-bottom:6px;'>Medical Information</div>" +
                    enhanceMessageFormatting(message);
        }
    }

    // Helper method to enhance formatting of message content
    private String enhanceMessageFormatting(String message) {
        // Replace plain list markers with better formatted ones
        message = message.replaceAll("(?m)^(\\d+)\\.", "<div style='margin-left:5px; margin-top:5px;'><b>$1.</b>");
        message = message.replaceAll("(?m)^•", "<div style='margin-left:5px; margin-top:5px;'>•");

        // Add closing tags for the list items
        message = message.replaceAll("(?m)$", "</div>");

        // Highlight important words
        message = message.replace("important", "<span style='color:#dc3545; font-weight:bold;'>important</span>");
        message = message.replace("required", "<span style='color:#dc3545; font-weight:bold;'>required</span>");
        message = message.replace("recommended", "<span style='color:#28a745; font-weight:bold;'>recommended</span>");

        // Format date and time references to stand out
        message = message.replaceAll("(\\d{4}-\\d{2}-\\d{2})", "<span style='font-weight:bold;'>$1</span>");
        message = message.replaceAll("(\\d{2}:\\d{2}:\\d{2})", "<span style='font-weight:bold;'>$1</span>");

        // Format doctor names to stand out
        message = message.replaceAll("(Dr\\. [A-Za-z ]+)", "<span style='color:#4F86F7; font-weight:bold;'>$1</span>");

        return message;
    }

    // Updated welcome message with better formatting for the chatbot
    private void initializeChatbot(JPanel chatMessagesPanel) {
        addBotMessage(chatMessagesPanel, "Hello! I'm your medical assistant chatbot. I can help you with:\n\n" +
                "1. Information about symptoms and medications\n" +
                "2. Check appointment availability: Type 'check YYYY-MM-DD HH:MM:SS'\n" +
                "3. Book an appointment: Type 'book YYYY-MM-DD HH:MM:SS patient@email.com \"Dr. Name\" [description]'\n\n" +
                "How can I assist you today?");
    }

    // Add new helper method for user messages (right side)
    private void addUserMessage(JPanel chatPanel, String message) {
        JPanel messageContainer = new JPanel();
        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.X_AXIS));
        messageContainer.setBackground(new Color(240, 242, 245));
        messageContainer.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // Message bubble
        JPanel bubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(79, 134, 247));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // Text content with reduced width
        JLabel textLabel = new JLabel("<html><body style='width: 200px; color: white;'>" + message + "</body></html>");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textLabel.setForeground(Color.WHITE);
        bubble.add(textLabel, BorderLayout.CENTER);

        // Add components
        messageContainer.add(Box.createHorizontalGlue());
        messageContainer.add(bubble);
        messageContainer.add(Box.createRigidArea(new Dimension(5, 0)));

        // Add to chat panel with spacing
        chatPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        chatPanel.add(messageContainer);

        // Update text area for storage
        chatArea.append("You: " + message + "\n");
    }

    // Add new helper method for typing indicator
    private JPanel addTypingIndicator(JPanel chatPanel) {
        JPanel messageContainer = new JPanel();
        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.X_AXIS));
        messageContainer.setBackground(new Color(240, 242, 245));
        messageContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatar icon for bot
        JLabel avatarLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(79, 134, 247));
                g2d.fillOval(0, 0, 30, 30);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2d.drawString("M", 10, 20);
                g2d.dispose();
            }
        };
        avatarLabel.setPreferredSize(new Dimension(30, 30));

        // Message bubble
        JPanel bubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        bubble.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        // Typing indicator dots
        JLabel typingLabel = new JLabel("typing...");
        typingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        typingLabel.setForeground(Color.GRAY);
        bubble.add(typingLabel);

        // Add components
        messageContainer.add(Box.createRigidArea(new Dimension(5, 0)));
        messageContainer.add(avatarLabel);
        messageContainer.add(Box.createRigidArea(new Dimension(8, 0)));
        messageContainer.add(bubble);
        messageContainer.add(Box.createHorizontalGlue());

        // Add to chat panel with spacing
        chatPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        chatPanel.add(messageContainer);

        return messageContainer;
    }


    // Helper method to establish database connection
    private Connection getDatabaseConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/HMsystem"; // Updated with your actual DB
        String username = "root"; // Update with your actual username
        String password = "Ashish030406"; // Update with your actual password
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            throw new SQLException("JDBC Driver not found", e);
        }
    }

    // Helper method to check if an appointment slot is available
    private boolean isAppointmentSlotAvailable(String date, String time) {
        String query = "SELECT COUNT(*) FROM appointments WHERE date = ? AND time = ?";
        try (Connection conn = getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, date); // e.g., "2025-04-07"
            stmt.setString(2, time); // e.g., "09:00:00"
            System.out.println("Executing query: " + query + " with date=" + date + ", time=" + time);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Query result: count=" + count);
                return count == 0; // Slot is available if count is 0
            }
            System.out.println("No results returned from query");
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false; // Default to false if there's an error or no results
    }

    // New method to book an appointment
    private boolean bookAppointment(String date, String time, int patientId, int doctorId, String description) {
        String query = "INSERT INTO appointments (patient_id, doctor_id, date, time, status, description) VALUES (?, ?, ?, ?, 'Scheduled', ?)";
        try (Connection conn = getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, date);
            stmt.setString(4, time);
            stmt.setString(5, description);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("SQL Error when booking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Keep the original parseAvailabilityQuery method
    private String parseAvailabilityQuery(String message) {
        // Example input: "check 2025-04-07 09:00:00"
        if (message.toLowerCase().startsWith("check")) {
            String[] parts = message.split(" ");
            if (parts.length >= 3) {
                String date = parts[1]; // e.g., "2025-04-07"
                String time = parts[2]; // e.g., "09:00:00"
                // Validate date format (YYYY-MM-DD)
                if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    System.out.println("Invalid date format: " + date);
                    return null;
                }
                // Validate time format (HH:MM:SS)
                if (!time.matches("\\d{2}:\\d{2}:\\d{2}")) {
                    // Try converting HH:MM to HH:MM:SS
                    if (time.matches("\\d{2}:\\d{2}")) {
                        time = time + ":00";
                    } else {
                        System.out.println("Invalid time format: " + time);
                        return null;
                    }
                }
                System.out.println("Parsed query: date=" + date + ", time=" + time);
                return date + " " + time; // Return formatted for validation
            } else {
                System.out.println("Insufficient parts in input: " + message);
            }
        }
        return null; // Not an availability query
    }

    // Updated helper method to parse booking query
    // Updated helper method to parse booking query
    private String parseBookingQuery(String message) {
        // Example input: "book 2025-04-25 10:00 ash@gmail.com priya sharma checkup"
        if (message.toLowerCase().startsWith("book")) {
            try {
                // Extract the parts after "book" command
                String[] mainParts = message.trim().split(" ", 5);
                if (mainParts.length < 4) {
                    System.out.println("Insufficient parts in booking input: " + message);
                    return null;
                }

                String date = mainParts[1]; // e.g., "2025-04-25"
                String time = mainParts[2]; // e.g., "10:00"
                String patientEmail = mainParts[3]; // e.g., "ash@gmail.com"

                // Get the remaining text (doctor name + description)
                String remaining = mainParts.length >= 5 ? mainParts[4] : "";

                // Now we need to extract doctor name and description from the remaining text
                String doctorName;
                String description = "";

                // Find the last space to separate doctor name from description
                // This assumes the description is a single word at the end
                int lastSpaceIndex = remaining.lastIndexOf(" ");
                if (lastSpaceIndex > 0) {
                    doctorName = remaining.substring(0, lastSpaceIndex).trim();
                    description = remaining.substring(lastSpaceIndex + 1).trim();
                } else {
                    doctorName = remaining.trim();
                }

                // Validate date format (YYYY-MM-DD)
                if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    System.out.println("Invalid date format: " + date);
                    return null;
                }

                // Validate time format (HH:MM:SS or HH:MM)
                if (!time.matches("\\d{2}:\\d{2}:\\d{2}") && !time.matches("\\d{2}:\\d{2}")) {
                    System.out.println("Invalid time format: " + time);
                    return null;
                }

                // Convert HH:MM to HH:MM:SS if needed
                if (time.matches("\\d{2}:\\d{2}")) {
                    time = time + ":00";
                }

                // Validate email format
                if (!patientEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    System.out.println("Invalid email format: " + patientEmail);
                    return null;
                }

                System.out.println("Parsed booking: date=" + date + ", time=" + time +
                        ", email=" + patientEmail + ", doctor=" + doctorName +
                        ", description=" + description);

                // Return formatted string for processing
                return date + "|" + time + "|" + patientEmail + "|" + doctorName + "|" + description;
            } catch (Exception e) {
                System.out.println("Error parsing booking query: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null; // Not a booking query
    }

    // Updated method to book an appointment with email and name instead of IDs
    private boolean bookAppointment(String date, String time, String patientEmail, String doctorName, String description) {
        // First, look up the patient ID from the email
        int patientId = getPatientIdFromEmail(patientEmail);
        if (patientId == -1) {
            System.out.println("Patient not found with email: " + patientEmail);
            return false;
        }

        // Next, look up the doctor ID from the name - using user_id field
        int doctorUserId = getDoctorUserIdFromName(doctorName);
        if (doctorUserId == -1) {
            System.out.println("Doctor not found with name: " + doctorName);
            return false;
        }

        // Now book the appointment with the IDs
        String query = "INSERT INTO appointments (patient_id, doctor_id, date, time, status, description) VALUES (?, ?, ?, ?, 'Scheduled', ?)";
        try (Connection conn = getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorUserId);  // Using the user_id from doctors table
            stmt.setString(3, date);
            stmt.setString(4, time);
            stmt.setString(5, description);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("SQL Error when booking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to get patient ID from email
    private int getPatientIdFromEmail(String email) {
        String query = "SELECT id FROM patients WHERE email = ?";
        try (Connection conn = getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error when looking up patient: " + e.getMessage());
            e.printStackTrace();
        }
        return -1; // Patient not found
    }

    // Updated helper method to get doctor user_id from name
    private int getDoctorUserIdFromName(String name) {
        String query = "SELECT user_id FROM doctors WHERE name = ?";
        try (Connection conn = getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
            // If exact match not found, try partial match (for cases like "Dr. Sharma" vs "Dr. Priya Sharma")
            query = "SELECT user_id FROM doctors WHERE name LIKE ?";
            try (PreparedStatement stmt2 = conn.prepareStatement(query)) {
                stmt2.setString(1, "%" + name + "%");
                ResultSet rs2 = stmt2.executeQuery();
                if (rs2.next()) {
                    return rs2.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error when looking up doctor: " + e.getMessage());
            e.printStackTrace();
        }
        return -1; // Doctor not found
    }

    // Helper method to get patient name from email for a more personalized response
    private String getPatientNameFromEmail(String email) {
        String query = "SELECT name FROM patients WHERE email = ?";
        try (Connection conn = getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error when looking up patient name: " + e.getMessage());
        }
        return null;
    }

    // Helper method to get full doctor name for a more personalized response
    private String getFullDoctorName(String partialName) {
        String query = "SELECT name FROM doctors WHERE name = ? OR name LIKE ?";
        try (Connection conn = getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, partialName);
            stmt.setString(2, "%" + partialName + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error when looking up doctor name: " + e.getMessage());
        }
        return null;
    }

    // The sendMessages method with updated booking flow
    private void sendMessages(JPanel chatMessagesPanel, JScrollPane scrollPane) {
        String message = userInput.getText().trim();
        if (message.isEmpty() || message.equals("Type your message here...")) return;

        // Display user message
        addUserMessage(chatMessagesPanel, message);
        userInput.setText("");

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });

        // Show typing indicator
        JPanel typingIndicator = addTypingIndicator(chatMessagesPanel);

        // Scroll to bottom after adding typing indicator
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });

        // Process in background thread to keep UI responsive
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override

            protected String doInBackground() {
                // Simulate processing delay
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                String lowerMessage = message.toLowerCase().trim();

                // === Availability Check ===
                if (lowerMessage.startsWith("check")) {
                    String availabilityQuery = parseAvailabilityQuery(message);
                    if (availabilityQuery != null) {
                        String[] parts = availabilityQuery.split(" ");
                        if (parts.length >= 2) {
                            String date = parts[0]; // YYYY-MM-DD
                            String time = parts[1]; // HH:MM:SS
                            boolean isAvailable = isAppointmentSlotAvailable(date, time);
                            return isAvailable
                                    ? "The appointment slot on " + date + " at " + time + " is available."
                                    : "The appointment slot on " + date + " at " + time + " is already booked.";
                        } else {
                            return "Invalid format. Please use: check YYYY-MM-DD HH:MM:SS";
                        }
                    } else {
                        return "Invalid format. Please use: check YYYY-MM-DD HH:MM:SS";
                    }
                }

                // === Booking Appointment ===
                else if (lowerMessage.startsWith("book")) {
                    try {
                        // Regex pattern to extract values
                        Pattern pattern = Pattern.compile(
                                "book\\s+(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{2}:\\d{2}:\\d{2})\\s+(\\S+@\\S+)\\s+\"([^\"]+)\"\\s*(.*)?",
                                Pattern.CASE_INSENSITIVE
                        );
                        Matcher matcher = pattern.matcher(message);

                        if (matcher.matches()) {
                            String date = matcher.group(1);
                            String time = matcher.group(2);
                            String patientEmail = matcher.group(3);
                            String doctorName = matcher.group(4);
                            String description = matcher.group(5) != null ? matcher.group(5).trim() : "";

                            // Check slot availability
                            boolean isAvailable = isAppointmentSlotAvailable(date, time);
                            if (!isAvailable) {
                                return "Sorry, the appointment slot on " + date + " at " + time + " is already booked.";
                            }

                            // Book it
                            boolean booked = bookAppointment(date, time, patientEmail, doctorName, description);
                            if (booked) {
                                String patientName = getPatientNameFromEmail(patientEmail);
                                String fullDoctorName = getFullDoctorName(doctorName);

                                return "Appointment successfully booked for " +
                                        (patientName != null ? patientName : "the patient") +
                                        " with " +
                                        (fullDoctorName != null ? fullDoctorName : doctorName) +
                                        " on " + date + " at " + time + ".";
                            } else {
                                return "Failed to book appointment. Please check that the patient email and doctor name are correct.";
                            }
                        } else {
                            return "Invalid booking format. Please use: book YYYY-MM-DD HH:MM:SS patient@email.com \"Dr. Name\" [description]";
                        }
                    } catch (Exception e) {
                        return "Error while booking appointment: " + e.getMessage();
                    }
                }

                // === Default Mock Response ===
                return getMockResponse(message);
            }


            @Override
            protected void done() {
                try {
                    // Remove typing indicator
                    chatMessagesPanel.remove(typingIndicator);

                    // Add response
                    String response = get();
                    addBotMessage(chatMessagesPanel, response);

                    // Refresh the panel
                    chatMessagesPanel.revalidate();
                    chatMessagesPanel.repaint();

                    // Scroll to bottom
                    SwingUtilities.invokeLater(() -> {
                        JScrollBar vertical = scrollPane.getVerticalScrollBar();
                        vertical.setValue(vertical.getMaximum());
                    });

                } catch (Exception e) {
                    addBotMessage(chatMessagesPanel, "Sorry, I encountered an error processing your request.");
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }



    class RoundedBorder extends AbstractBorder {
        private Color color;
        private int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius / 2, this.radius / 2, this.radius / 2, this.radius / 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
    private String getMockResponse(String question) {
        // Convert to lowercase for easier matching
        question = question.toLowerCase();

        // Expanded medical responses

        if (question.contains("hi") || question.contains("hello") || question.contains("good morning") ||
                question.contains("good afternoon") || question.contains("good night") || question.contains("hey") || question.contains("i need help") ) {
            return "Hello there! Hope you're doing well today. How can I assist you? ";
        }

        if (question.contains("hello") || question.contains("hi")) {
            return "Hello! How can I assist you with your medical appointments today?";
        } else if (question.contains("help")) {
            return "I can help you:\n\n" +
                    "1. Check appointment availability: Type 'check YYYY-MM-DD HH:MM:SS'\n" +
                    "2. Book appointments: Type 'book YYYY-MM-DD HH:MM:SS [patient_id] [doctor_id] [description]'\n\n" +
                    "Example: 'check 2025-04-07 09:00:00' or 'book 2025-04-30 14:00:00 5 103 Annual checkup'";
        } else if (question.contains("thank")) {
            return "You're welcome! Is there anything else I can help you with?";
        }

        if (question.contains("headache") || question.contains("head ache") || question.contains("migraine")) {
            return "Headaches can vary from mild tension headaches to severe migraines. Common causes include:\n" +
                    "- Stress or anxiety\n" +
                    "- Dehydration\n" +
                    "- Lack of sleep\n" +
                    "- Eye strain\n" +
                    "- Caffeine withdrawal\n" +
                    "- Sinus congestion\n" +
                    "- Hormonal changes (e.g., menstruation)\n\n" +
                    "For migraines specifically, symptoms may include:\n" +
                    "- Throbbing pain on one side\n" +
                    "- Sensitivity to light/sound\n" +
                    "- Nausea/vomiting\n" +
                    "- Visual disturbances (aura)\n" +
                    "- Tingling in limbs\n\n" +
                    "Try resting in a dark room, applying a cool compress, staying hydrated, " +
                    "and OTC pain relievers (e.g., ibuprofen, acetaminophen). Seek medical help if:\n" +
                    "- Headache is severe and sudden ('thunderclap' headache)\n" +
                    "- Accompanied by fever, stiff neck, confusion, or seizures\n" +
                    "- Follows a head injury\n" +
                    "- Worsens over days\n" +
                    "- Vision loss or speech difficulty occurs";
        }
        else if (question.contains("cold") || question.contains("flu") || question.contains("fever") || question.contains("cough")) {
            return "Viral infections like colds and flu share some symptoms but differ in severity:\n\n" +
                    "Common Cold:\n" +
                    "- Gradual onset\n" +
                    "- Runny/stuffy nose\n" +
                    "- Sneezing\n" +
                    "- Sore throat\n" +
                    "- Mild cough\n" +
                    "- Low-grade fever (rare)\n\n" +
                    "Influenza (Flu):\n" +
                    "- Sudden onset\n" +
                    "- High fever (100-102°F, sometimes higher)\n" +
                    "- Body aches\n" +
                    "- Fatigue/weakness\n" +
                    "- Dry cough\n" +
                    "- Headache\n\n" +
                    "Treatment for both includes:\n" +
                    "- Rest\n" +
                    "- Hydration (water, herbal tea, broth)\n" +
                    "- OTC meds (decongestants, cough syrup, fever reducers)\n" +
                    "- Humidifier or steam inhalation\n" +
                    "- Gargling salt water for sore throat\n\n" +
                    "Seek medical care if:\n" +
                    "- Difficulty breathing\n" +
                    "- Fever >103°F or lasting >3 days\n" +
                    "- Symptoms improve then worsen (possible secondary infection)\n" +
                    "- Dehydration signs (dry mouth, dizziness)\n" +
                    "- Persistent vomiting";
        }
        else if (question.contains("blood pressure") || question.contains("hypertension") || question.contains("hypotension")) {
            return "Blood Pressure Guidelines:\n\n" +
                    "Category        | Systolic | Diastolic\n" +
                    "----------------|----------|----------\n" +
                    "Normal          | <120     | <80\n" +
                    "Elevated        | 120-129  | <80\n" +
                    "Hypertension 1 | 130-139  | 80-89\n" +
                    "Hypertension 2 | ≥140     | ≥90\n" +
                    "Hypotension    | <90      | <60\n\n" +
                    "Lifestyle modifications for high BP:\n" +
                    "- DASH diet (fruits, vegetables, whole grains)\n" +
                    "- Reduce sodium to <1500mg/day\n" +
                    "- Regular aerobic exercise (30 min most days)\n" +
                    "- Limit alcohol (1 drink/day women, 2 men)\n" +
                    "- Stress management (yoga, meditation)\n" +
                    "- Quit smoking\n" +
                    "- Maintain healthy weight\n\n" +
                    "For low blood pressure (hypotension):\n" +
                    "- Increase fluid intake\n" +
                    "- Add modest salt (if not hypertensive)\n" +
                    "- Rise slowly from sitting/lying\n" +
                    "- Wear compression stockings\n" +
                    "- Eat small, frequent meals\n\n" +
                    "See a doctor if: BP is consistently abnormal, dizziness/fainting occurs, or chest pain develops.";
        }
        else if (question.contains("diabetes") || question.contains("blood sugar")) {
            return "Diabetes Management Guidelines:\n\n" +
                    "Normal Blood Sugar Levels:\n" +
                    "- Fasting: 70-99 mg/dL\n" +
                    "- 2 hrs after eating: <140 mg/dL\n" +
                    "- A1C: <5.7%\n" +
                    "Prediabetes: A1C 5.7-6.4%\n" +
                    "Diabetes: A1C ≥6.5%\n\n" +
                    "Type 1 Diabetes:\n" +
                    "- Autoimmune; usually develops in childhood\n" +
                    "- Requires insulin therapy\n" +
                    "- Symptoms: excessive thirst, urination, weight loss, fatigue\n\n" +
                    "Type 2 Diabetes:\n" +
                    "- More common in adults\n" +
                    "- Managed with diet, exercise, meds (e.g., metformin), possibly insulin\n" +
                    "- Risk factors: family history, obesity, inactivity, age >45\n\n" +
                    "Complications to monitor:\n" +
                    "- Eye exams annually (retinopathy)\n" +
                    "- Foot exams regularly (neuropathy)\n" +
                    "- Kidney function tests (nephropathy)\n" +
                    "- Heart health (cardiovascular disease)\n" +
                    "- Blood pressure and cholesterol\n\n" +
                    "Management tips:\n" +
                    "- Monitor blood sugar regularly\n" +
                    "- Low-carb diet, avoid sugary drinks\n" +
                    "- Exercise 150 min/week";
        }
        else if (question.contains("exercise") || question.contains("workout") || question.contains("physical activity")) {
            return "Physical Activity Recommendations:\n\n" +
                    "Adults (18-64):\n" +
                    "- 150-300 min moderate aerobic OR\n" +
                    "- 75-150 min vigorous aerobic weekly\n" +
                    "- Muscle-strengthening 2+ days/week (e.g., weights, resistance bands)\n\n" +
                    "Older Adults (65+):\n" +
                    "- Include balance training (e.g., tai chi)\n" +
                    "- Adjust for chronic conditions\n" +
                    "- Flexibility exercises (e.g., stretching)\n\n" +
                    "Children (6-17):\n" +
                    "- 60 min daily (mostly aerobic)\n" +
                    "- Include bone/muscle-strengthening 3 days/week\n\n" +
                    "Examples:\n" +
                    "Moderate Activity:\n" +
                    "- Brisk walking\n" +
                    "- Water aerobics\n" +
                    "- Cycling <10mph\n" +
                    "- Dancing\n\n" +
                    "Vigorous Activity:\n" +
                    "- Running\n" +
                    "- Swimming laps\n" +
                    "- Cycling >10mph\n" +
                    "- Jumping rope\n\n" +
                    "Start slow if new to exercise, warm up/cool down, and consult your doctor if you have health concerns.";
        }
        else if (question.contains("thanks") || question.contains("thank you") || question.contains("you are great")) {
            return "Its my pleasure stay hydrated ";
        }
        else if (question.contains("diet") || question.contains("nutrition") || question.contains("eat") || question.contains("food")) {
            return "Healthy Eating Guidelines:\n\n" +
                    "Plate Method:\n" +
                    "- 1/2 plate non-starchy vegetables/fruits\n" +
                    "- 1/4 plate whole grains (quinoa, brown rice)\n" +
                    "- 1/4 plate lean protein (chicken, tofu)\n" +
                    "- Healthy fats in moderation (nuts, seeds)\n\n" +
                    "Key Recommendations:\n" +
                    "- Limit added sugars (<10% calories/day)\n" +
                    "- Reduce sodium (<2300mg/day, ideally <1500mg)\n" +
                    "- Choose whole grains over refined\n" +
                    "- Variety of colorful vegetables (leafy greens, peppers)\n" +
                    "- Lean proteins (fish, poultry, beans, lentils)\n" +
                    "- Healthy fats (avocados, nuts, olive oil)\n" +
                    "- Stay hydrated (water, unsweetened teas)\n" +
                    "- Limit processed foods\n\n" +
                    "Special Diets:\n" +
                    "- DASH (Hypertension)\n" +
                    "- Mediterranean (Heart Health)\n" +
                    "- Low-carb/Keto (Diabetes/Weight Loss)\n" +
                    "- Vegan (Plant-based)\n" +
                    "- Consult dietitian for personalized plans";
        }
        else if (question.contains("covid") || question.contains("coronavirus")) {
            return "COVID-19 Information (Updated April 2025):\n\n" +
                    "Common Symptoms:\n" +
                    "- Fever/chills\n" +
                    "- Cough\n" +
                    "- Shortness of breath\n" +
                    "- Fatigue\n" +
                    "- Muscle/body aches\n" +
                    "- Loss of taste/smell\n" +
                    "- Sore throat\n" +
                    "- Congestion\n" +
                    "- Nausea/vomiting/diarrhea\n\n" +
                    "Emergency Warning Signs:\n" +
                    "- Trouble breathing\n" +
                    "- Persistent chest pain\n" +
                    "- New confusion\n" +
                    "- Inability to wake/stay awake\n" +
                    "- Pale/gray/blue skin/lips\n\n" +
                    "Prevention:\n" +
                    "- Vaccination (including boosters)\n" +
                    "- Masking in crowded/indoor high-risk areas\n" +
                    "- Hand hygiene (soap or sanitizer)\n" +
                    "- Ventilation (open windows, air purifiers)\n" +
                    "- Avoid close contact if symptomatic\n\n" +
                    "Current CDC guidelines recommend isolation for 5 days after positive test if symptoms improve " +
                    "and no fever for 24 hours without medication. Test-to-treat antiviral options may be available.";
        }
        else if (question.contains("mental health") || question.contains("depress") || question.contains("anxiety")) {
            return "Mental Health Information:\n\n" +
                    "Common Signs:\n" +
                    "- Persistent sadness/anxiety\n" +
                    "- Loss of interest in activities\n" +
                    "- Changes in sleep/appetite\n" +
                    "- Difficulty concentrating\n" +
                    "- Physical symptoms (e.g., fatigue, headaches) without clear cause\n" +
                    "- Irritability or restlessness\n\n" +
                    "Self-Care Strategies:\n" +
                    "- Maintain regular routine\n" +
                    "- Stay connected with friends/family\n" +
                    "- Physical activity (even a short walk)\n" +
                    "- Mindfulness/relaxation (deep breathing, meditation)\n" +
                    "- Limit alcohol/caffeine\n" +
                    "- Get 7-9 hours sleep\n" +
                    "- Journaling or creative outlets\n\n" +
                    "When to Seek Help:\n" +
                    "- Symptoms persist >2 weeks\n" +
                    "- Interfere with work/relationships\n" +
                    "- Thoughts of self-harm or suicide\n" +
                    "- Panic attacks (racing heart, shortness of breath)\n\n" +
                    "Crisis Resources:\n" +
                    "- 988 Suicide & Crisis Lifeline (US)\n" +
                    "- Contact your healthcare provider or therapist";
        }
        else if (question.contains("emergency") || question.contains("urgent")) {
            return "Seek Emergency Medical Care for:\n\n" +
                    "- Chest pain or pressure (possible heart attack)\n" +
                    "- Difficulty breathing\n" +
                    "- Severe bleeding that won’t stop\n" +
                    "- Sudden severe pain (e.g., abdomen, head)\n" +
                    "- Weakness/numbness on one side (possible stroke)\n" +
                    "- Sudden vision changes\n" +
                    "- High fever with stiff neck (possible meningitis)\n" +
                    "- Suicidal/homicidal thoughts\n" +
                    "- Severe burns or electric shock\n" +
                    "- Poisoning or overdose\n" +
                    "- Broken bones with deformity\n\n" +
                    "For emergencies in the US, call 911 or go to nearest emergency department.\n" +
                    "For non-emergencies, contact your healthcare provider or local urgent care.";
        }
        else if (question.contains("allergy") || question.contains("allergies") || question.contains("sneeze") || question.contains("rash")) {
            return "Allergy Information:\n\n" +
                    "Common Triggers:\n" +
                    "- Pollen (trees, grass, weeds)\n" +
                    "- Dust mites\n" +
                    "- Pet dander\n" +
                    "- Mold\n" +
                    "- Foods (nuts, shellfish, dairy)\n" +
                    "- Insect stings\n" +
                    "- Medications\n\n" +
                    "Symptoms:\n" +
                    "- Sneezing, runny/stuffy nose\n" +
                    "- Itchy/watery eyes\n" +
                    "- Rash or hives\n" +
                    "- Swelling (face, throat)\n" +
                    "- Wheezing or shortness of breath\n" +
                    "- Anaphylaxis (severe reaction: difficulty breathing, drop in BP)\n\n" +
                    "Management:\n" +
                    "- Avoid triggers (e.g., air purifiers, pet-free zones)\n" +
                    "- OTC antihistamines (e.g., cetirizine, loratadine)\n" +
                    "- Nasal saline rinses\n" +
                    "- Prescription meds (e.g., inhalers, EpiPen for severe cases)\n" +
                    "- Allergy testing if unknown cause\n\n" +
                    "Seek help if: breathing difficulty, swelling, or anaphylaxis occurs—use epinephrine and call 911.";
        }
        else if (question.contains("stomach") || question.contains("digestion") || question.contains("nausea") || question.contains("diarrhea")) {
            return "Digestive Health Information:\n\n" +
                    "Common Issues:\n" +
                    "- Nausea: Stress, food poisoning, motion sickness, pregnancy\n" +
                    "- Diarrhea: Viral infection, food intolerance, bacteria\n" +
                    "- Constipation: Low fiber, dehydration, inactivity\n" +
                    "- Acid reflux: Spicy/fatty foods, overeating, lying down after meals\n" +
                    "- Bloating: Gas, overeating, lactose intolerance\n\n" +
                    "Self-Care:\n" +
                    "- Nausea: Ginger tea, small bland meals (crackers, rice)\n" +
                    "- Diarrhea: Hydrate (electrolyte drinks), BRAT diet (bananas, rice, applesauce, toast)\n" +
                    "- Constipation: Increase fiber (fruits, veggies), water, exercise\n" +
                    "- Reflux: Avoid triggers, eat smaller meals, elevate head when sleeping\n" +
                    "- Bloating: Avoid carbonated drinks, eat slowly\n\n" +
                    "Seek help if:\n" +
                    "- Severe abdominal pain\n" +
                    "- Blood in stool/vomit\n" +
                    "- Persistent vomiting/diarrhea (>2 days)\n" +
                    "- Unexplained weight loss";
        }
        else if (question.contains("skin") || question.contains("acne") || question.contains("eczema") || question.contains("psoriasis")) {
            return "Skin Health Information:\n\n" +
                    "Common Conditions:\n" +
                    "- Acne: Clogged pores (hormones, oil, bacteria)\n" +
                    "- Eczema: Dry, itchy, inflamed skin (often allergic)\n" +
                    "- Psoriasis: Red, scaly patches (autoimmune)\n" +
                    "- Sunburn: UV exposure\n\n" +
                    "Management:\n" +
                    "- Acne: Cleanse gently, OTC benzoyl peroxide/salicylic acid, avoid touching face\n" +
                    "- Eczema: Moisturize (thick creams), avoid irritants, hydrocortisone for flare-ups\n" +
                    "- Psoriasis: Moisturizers, coal tar, prescription meds (e.g., biologics)\n" +
                    "- Sunburn: Aloe vera, cool compresses, hydrate, avoid further sun\n" +
                    "- General: SPF 30+ sunscreen daily\n\n" +
                    "See a dermatologist if: severe, persistent, signs of infection (pus, fever), or changing moles.";
        }
        else if (question.contains("sleep") || question.contains("insomnia") || question.contains("tired")) {
            return "Sleep Health Information:\n\n" +
                    "Recommendations:\n" +
                    "- Adults: 7-9 hours/night\n" +
                    "- Teens: 8-10 hours\n" +
                    "- Children: 9-11 hours\n\n" +
                    "Common Issues:\n" +
                    "- Insomnia: Difficulty falling/staying asleep\n" +
                    "- Sleep apnea: Pauses in breathing, snoring\n" +
                    "- Restless legs: Urge to move legs at night\n\n" +
                    "Sleep Hygiene:\n" +
                    "- Consistent sleep schedule\n" +
                    "- Dark, quiet, cool room\n" +
                    "- Limit screens 1-2 hours before bed\n" +
                    "- Avoid caffeine/alcohol late in day\n" +
                    "- Relaxation (reading, meditation)\n" +
                    "- Exercise (not close to bedtime)\n\n" +
                    "Seek help if: daytime fatigue persists, loud snoring with gasping, or sleep issues >1 month.";
        }
        else if (question.contains("vaccine") || question.contains("vaccination") || question.contains("shot")) {
            return "Vaccination Information:\n\n" +
                    "Common Vaccines:\n" +
                    "- Flu: Annual, especially fall/winter\n" +
                    "- COVID-19: Initial series + boosters\n" +
                    "- Tdap: Tetanus, diphtheria, pertussis (every 10 years)\n" +
                    "- MMR: Measles, mumps, rubella\n" +
                    "- Shingles: Age 50+\n" +
                    "- Pneumococcal: Age 65+ or high-risk\n\n" +
                    "Benefits:\n" +
                    "- Prevent severe illness\n" +
                    "- Reduce spread to vulnerable groups\n" +
                    "- Community immunity\n\n" +
                    "Side Effects (usually mild):\n" +
                    "- Soreness at injection site\n" +
                    "- Low-grade fever\n" +
                    "- Fatigue\n" +
                    "- Rare: allergic reactions (seek immediate care)\n\n" +
                    "Consult your doctor for personalized vaccine schedules.";
        }
        if (question.contains("asthma") || question.contains("wheez") || question.contains("breath")) {
            return "Asthma Information:\n\n" +
                    "Symptoms:\n" +
                    "- Wheezing (whistling sound when breathing)\n" +
                    "- Shortness of breath\n" +
                    "- Chest tightness\n" +
                    "- Coughing (often worse at night)\n\n" +
                    "Triggers:\n" +
                    "- Allergens (pollen, dust mites, pet dander)\n" +
                    "- Air pollution/smoke\n" +
                    "- Exercise (exercise-induced asthma)\n" +
                    "- Cold air\n" +
                    "- Respiratory infections\n\n" +
                    "Management:\n" +
                    "- Quick-relief inhalers (albuterol) for attacks\n" +
                    "- Controller medications (steroid inhalers) for prevention\n" +
                    "- Asthma action plan from your doctor\n" +
                    "- Identify and avoid triggers\n" +
                    "- Regular check-ups\n\n" +
                    "Emergency Signs:\n" +
                    "- Inhaler not helping\n" +
                    "- Difficulty speaking due to breathlessness\n" +
                    "- Lips/fingernails turning blue\n" +
                    "- Rapid worsening of symptoms";
        }
        else if (question.contains("thyroid") || question.contains("hypothyroid") || question.contains("hyperthyroid")) {
            return "Thyroid Disorders:\n\n" +
                    "Hypothyroidism (Underactive Thyroid):\n" +
                    "- Fatigue\n" +
                    "- Weight gain\n" +
                    "- Cold intolerance\n" +
                    "- Dry skin/hair\n" +
                    "- Constipation\n" +
                    "- Depression\n" +
                    "- Treatment: Synthetic thyroid hormone (levothyroxine)\n\n" +
                    "Hyperthyroidism (Overactive Thyroid):\n" +
                    "- Weight loss\n" +
                    "- Rapid heartbeat\n" +
                    "- Heat intolerance\n" +
                    "- Tremors\n" +
                    "- Anxiety\n" +
                    "- Treatment: Anti-thyroid meds, radioactive iodine, surgery\n\n" +
                    "Diagnosis:\n" +
                    "- TSH blood test\n" +
                    "- T3/T4 levels\n" +
                    "- Thyroid ultrasound if nodules present\n\n" +
                    "See your doctor if you have persistent symptoms for thyroid testing.";
        }
        else if (question.contains("cholesterol") || question.contains("lipid")) {
            return "Cholesterol Management:\n\n" +
                    "Ideal Levels:\n" +
                    "- Total cholesterol: <200 mg/dL\n" +
                    "- LDL ('bad' cholesterol): <100 mg/dL\n" +
                    "- HDL ('good' cholesterol): >60 mg/dL\n" +
                    "- Triglycerides: <150 mg/dL\n\n" +
                    "Risk Factors for High Cholesterol:\n" +
                    "- Family history\n" +
                    "- Poor diet (saturated/trans fats)\n" +
                    "- Obesity\n" +
                    "- Lack of exercise\n" +
                    "- Smoking\n" +
                    "- Diabetes\n\n" +
                    "Lowering Cholesterol Naturally:\n" +
                    "- Increase soluble fiber (oats, beans, apples)\n" +
                    "- Eat healthy fats (avocados, nuts, olive oil)\n" +
                    "- Exercise 30+ minutes most days\n" +
                    "- Lose excess weight\n" +
                    "- Limit alcohol\n" +
                    "- Quit smoking\n\n" +
                    "When Medication is Needed:\n" +
                    "- Statins (e.g., atorvastatin) most common\n" +
                    "- If lifestyle changes aren't enough\n" +
                    "- For those with heart disease/diabetes";
        }
        else if (question.contains("osteoporosis") || question.contains("bone density")) {
            return "Osteoporosis Information:\n\n" +
                    "Risk Factors:\n" +
                    "- Age (especially postmenopausal women)\n" +
                    "- Family history\n" +
                    "- Low calcium/vitamin D intake\n" +
                    "- Sedentary lifestyle\n" +
                    "- Smoking/excessive alcohol\n" +
                    "- Certain medications (e.g., steroids)\n\n" +
                    "Prevention:\n" +
                    "- Calcium-rich foods (dairy, leafy greens, fortified foods)\n" +
                    "- Vitamin D (sunlight, fatty fish, supplements)\n" +
                    "- Weight-bearing exercise (walking, dancing)\n" +
                    "- Strength training 2-3x/week\n" +
                    "- Fall prevention at home\n\n" +
                    "Diagnosis:\n" +
                    "- DEXA scan (bone density test)\n" +
                    "- Recommended for women 65+, men 70+, or younger with risk factors\n\n" +
                    "Treatment:\n" +
                    "- Bisphosphonates (e.g., alendronate)\n" +
                    "- Calcium/vitamin D supplements\n" +
                    "- Newer biologics for severe cases";
        }
        else if (question.contains("stroke") || question.contains("cva")) {
            return "Stroke Information:\n\n" +
                    "BE FAST Recognition:\n" +
                    "B - Balance loss\n" +
                    "E - Eyesight changes\n" +
                    "F - Face drooping\n" +
                    "A - Arm weakness\n" +
                    "S - Speech difficulty\n" +
                    "T - Time to call emergency services\n\n" +
                    "Types:\n" +
                    "- Ischemic (blocked artery) - 87% of strokes\n" +
                    "- Hemorrhagic (bleeding in brain) - 13%\n\n" +
                    "Risk Factors:\n" +
                    "- High blood pressure\n" +
                    "- Atrial fibrillation\n" +
                    "- Diabetes\n" +
                    "- Smoking\n" +
                    "- High cholesterol\n" +
                    "- Physical inactivity\n\n" +
                    "Prevention:\n" +
                    "- Control blood pressure\n" +
                    "- Manage atrial fibrillation\n" +
                    "- Quit smoking\n" +
                    "- Healthy diet (Mediterranean)\n" +
                    "- Regular exercise\n\n" +
                    "Time is brain - call emergency services immediately if stroke suspected!";
        }
        else if (question.contains("kidney") || question.contains("renal")) {
            return "Kidney Health Information:\n\n" +
                    "Functions:\n" +
                    "- Filter waste from blood\n" +
                    "- Balance fluids/electrolytes\n" +
                    "- Regulate blood pressure\n" +
                    "- Produce urine\n\n" +
                    "Chronic Kidney Disease (CKD) Stages:\n" +
                    "Stage 1-2: Mild (normal or slightly reduced function)\n" +
                    "Stage 3: Moderate\n" +
                    "Stage 4: Severe\n" +
                    "Stage 5: Kidney failure (dialysis or transplant needed)\n\n" +
                    "Symptoms of Kidney Problems:\n" +
                    "- Swelling in feet/ankles\n" +
                    "- Fatigue\n" +
                    "- Changes in urination\n" +
                    "- Nausea\n" +
                    "- Itchy skin\n\n" +
                    "Protect Your Kidneys:\n" +
                    "- Control blood pressure\n" +
                    "- Manage diabetes\n" +
                    "- Stay hydrated\n" +
                    "- Limit NSAID pain relievers\n" +
                    "- Reduce salt intake\n" +
                    "- Get regular check-ups";
        }
        else if (question.contains("alzheimer") || question.contains("dementia")) {
            return "Cognitive Health Information:\n\n" +
                    "Early Signs of Dementia:\n" +
                    "- Memory loss affecting daily life\n" +
                    "- Difficulty planning/solving problems\n" +
                    "- Trouble completing familiar tasks\n" +
                    "- Confusion with time/place\n" +
                    "- New problems with words\n" +
                    "- Misplacing things\n" +
                    "- Poor judgment\n" +
                    "- Withdrawal from activities\n\n" +
                    "Risk Reduction Strategies:\n" +
                    "- Regular physical exercise\n" +
                    "- Mental stimulation (reading, puzzles)\n" +
                    "- Social engagement\n" +
                    "- Healthy diet (Mediterranean or MIND diet)\n" +
                    "- Quality sleep\n" +
                    "- Manage cardiovascular risk factors\n\n" +
                    "When to See a Doctor:\n" +
                    "- Persistent memory concerns\n" +
                    "- Personality/mood changes\n" +
                    "- Difficulty with daily tasks\n" +
                    "- Family history of dementia";
        }
        else if (question.contains("cancer") || question.contains("tumor")) {
            return "Cancer Information:\n\n" +
                    "Common Types:\n" +
                    "- Breast cancer\n" +
                    "- Lung cancer\n" +
                    "- Prostate cancer\n" +
                    "- Colorectal cancer\n" +
                    "- Skin cancer (melanoma, basal cell)\n\n" +
                    "Early Detection:\n" +
                    "- Know your family history\n" +
                    "- Be aware of your body\n" +
                    "- Get age-appropriate screenings\n" +
                    "- Don't ignore persistent symptoms\n\n" +
                    "Potential Warning Signs:\n" +
                    "- Unexplained weight loss\n" +
                    "- Persistent fatigue\n" +
                    "- Unusual lumps\n" +
                    "- Changes in bowel/bladder habits\n" +
                    "- Persistent cough/hoarseness\n" +
                    "- Unusual bleeding\n" +
                    "- Skin changes\n\n" +
                    "Prevention Strategies:\n" +
                    "- Don't smoke\n" +
                    "- Limit alcohol\n" +
                    "- Maintain healthy weight\n" +
                    "- Exercise regularly\n" +
                    "- Eat fruits/vegetables\n" +
                    "- Protect skin from sun\n" +
                    "- Get vaccinated (HPV, hepatitis B)";
        }
        else if (question.contains("autoimmune") || question.contains("lupus") || question.contains("ms")) {
            return "Autoimmune Diseases:\n\n" +
                    "Common Conditions:\n" +
                    "- Rheumatoid arthritis (joints)\n" +
                    "- Lupus (multiple organs)\n" +
                    "- Multiple sclerosis (nervous system)\n" +
                    "- Type 1 diabetes (pancreas)\n" +
                    "- Psoriasis (skin)\n" +
                    "- Celiac disease (digestive)\n\n" +
                    "Characteristics:\n" +
                    "- Immune system attacks healthy tissue\n" +
                    "- Often chronic with flare-ups\n" +
                    "- More common in women\n" +
                    "- Genetic and environmental factors\n\n" +
                    "Management:\n" +
                    "- Immunosuppressant medications\n" +
                    "- Anti-inflammatory drugs\n" +
                    "- Lifestyle modifications\n" +
                    "- Stress management\n" +
                    "- Regular medical care\n\n" +
                    "When to See a Specialist:\n" +
                    "- Unexplained symptoms affecting multiple systems\n" +
                    "- Family history of autoimmune disease\n" +
                    "- Positive autoimmune blood tests";
        }
        else if (question.contains("anemia") || question.contains("iron") || question.contains("hemoglobin")) {
            return "Anemia Information:\n\n" +
                    "Common Types:\n" +
                    "- Iron deficiency (most common)\n" +
                    "- Vitamin B12/folate deficiency\n" +
                    "- Chronic disease-related\n" +
                    "- Hemolytic (red blood cell destruction)\n\n" +
                    "Symptoms:\n" +
                    "- Fatigue\n" +
                    "- Pale skin\n" +
                    "- Shortness of breath\n" +
                    "- Dizziness\n" +
                    "- Cold hands/feet\n" +
                    "- Brittle nails\n" +
                    "- Unusual cravings (pica)\n\n" +
                    "Diagnosis:\n" +
                    "- Complete blood count (CBC)\n" +
                    "- Iron studies\n" +
                    "- Vitamin B12/folate levels\n" +
                    "- Sometimes bone marrow tests\n\n" +
                    "Treatment:\n" +
                    "- Iron supplements (with vitamin C for absorption)\n" +
                    "- Dietary changes (red meat, leafy greens, fortified cereals)\n" +
                    "- Vitamin B12 injections if deficient\n" +
                    "- Address underlying cause";
        }
        else if (question.contains("pcos") || question.contains("polycystic ovary")) {
            return "PCOS Information:\n\n" +
                    "Diagnostic Criteria (Need 2 of 3):\n" +
                    "1. Irregular periods\n" +
                    "2. Signs of excess androgen (acne, excess hair)\n" +
                    "3. Polycystic ovaries on ultrasound\n\n" +
                    "Symptoms:\n" +
                    "- Irregular/absent periods\n" +
                    "- Difficulty conceiving\n" +
                    "- Weight gain\n" +
                    "- Acne/oily skin\n" +
                    "- Excess facial/body hair\n" +
                    "- Thinning scalp hair\n" +
                    "- Skin tags/dark patches\n\n" +
                    "Management:\n" +
                    "- Lifestyle changes (diet/exercise for weight loss)\n" +
                    "- Birth control pills to regulate cycles\n" +
                    "- Metformin for insulin resistance\n" +
                    "- Fertility treatments if trying to conceive\n" +
                    "- Anti-androgen medications for symptoms\n\n" +
                    "Long-term Risks:\n" +
                    "- Type 2 diabetes\n" +
                    "- Heart disease\n" +
                    "- Endometrial cancer\n" +
                    "- Depression/anxiety";
        }
        else if (question.contains("menopause") || question.contains("hot flash")) {
            return "Menopause Information:\n\n" +
                    "Stages:\n" +
                    "- Perimenopause: Transition period (may last years)\n" +
                    "- Menopause: 12 months without period\n" +
                    "- Postmenopause: Years after\n\n" +
                    "Common Symptoms:\n" +
                    "- Hot flashes/night sweats\n" +
                    "- Vaginal dryness\n" +
                    "- Sleep disturbances\n" +
                    "- Mood changes\n" +
                    "- Weight gain\n" +
                    "- Thinning hair/dry skin\n" +
                    "- Loss of breast fullness\n\n" +
                    "Management Options:\n" +
                    "- Hormone therapy (estrogen/progestin)\n" +
                    "- Non-hormonal medications\n" +
                    "- Vaginal moisturizers/lubricants\n" +
                    "- Regular exercise\n" +
                    "- Phytoestrogen-rich foods (soy, flaxseeds)\n" +
                    "- Stress reduction techniques\n\n" +
                    "When to See Your Doctor:\n" +
                    "- Symptoms affecting quality of life\n" +
                    "- Bleeding after menopause\n" +
                    "- Concerns about bone health";
        }
        else if (question.contains("adhd") || question.contains("attention deficit")) {
            return "ADHD Information:\n\n" +
                    "Core Symptoms:\n" +
                    "- Inattention (difficulty focusing, forgetfulness)\n" +
                    "- Hyperactivity (fidgeting, restlessness)\n" +
                    "- Impulsivity (interrupting, risk-taking)\n\n" +
                    "Types:\n" +
                    "- Predominantly inattentive\n" +
                    "- Predominantly hyperactive/impulsive\n" +
                    "- Combined\n\n" +
                    "Management Strategies:\n" +
                    "- Stimulant medications (methylphenidate, amphetamines)\n" +
                    "- Non-stimulant options (atomoxetine)\n" +
                    "- Behavioral therapy\n" +
                    "- Organizational tools/reminders\n" +
                    "- Regular exercise\n" +
                    "- Structured routines\n\n" +
                    "In Adults:\n" +
                    "- May present as time management issues\n" +
                    "- Difficulty completing tasks\n" +
                    "- Relationship challenges\n" +
                    "- Often coexists with anxiety/depression";
        }
        else if (question.contains("ibs") || question.contains("irritable bowel")) {
            return "Irritable Bowel Syndrome (IBS) Information:\n\n" +
                    "Subtypes:\n" +
                    "- IBS-D (diarrhea predominant)\n" +
                    "- IBS-C (constipation predominant)\n" +
                    "- IBS-M (mixed)\n\n" +
                    "Common Symptoms:\n" +
                    "- Abdominal pain relieved by bowel movements\n" +
                    "- Altered bowel habits\n" +
                    "- Bloating\n" +
                    "- Gas\n" +
                    "- Mucus in stool\n\n" +
                    "Management:\n" +
                    "- Low FODMAP diet (temporarily eliminate fermentable carbs)\n" +
                    "- Fiber supplements (for IBS-C)\n" +
                    "- Peppermint oil capsules\n" +
                    "- Probiotics\n" +
                    "- Stress management\n" +
                    "- Prescription medications for severe cases\n\n" +
                    "Red Flag Symptoms (Not Typical of IBS):\n" +
                    "- Weight loss\n" +
                    "- Blood in stool\n" +
                    "- Nighttime symptoms\n" +
                    "- Family history of colon cancer/celiac disease";
        }
        else {
            return "I can provide information on these additional health topics:\n" +
                    "- Asthma and respiratory conditions\n" +
                    "- Thyroid disorders\n" +
                    "- Cholesterol management\n" +
                    "- Osteoporosis and bone health\n" +
                    "- Stroke prevention\n" +
                    "- Kidney function\n" +
                    "- Alzheimer's/dementia\n" +
                    "- Cancer screening\n" +
                    "- Autoimmune diseases\n" +
                    "- Anemia\n" +
                    "- PCOS and women's health\n" +
                    "- Menopause\n" +
                    "- ADHD\n" +
                    "- IBS and digestive disorders\n\n" +
                    "Please ask about a specific health concern. Remember I provide general information only - " +
                    "consult your healthcare provider for personal medical advice.";
        }
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ModernPatientPanel app = new ModernPatientPanel(2); // Example: Priya Patel's ID
            app.setVisible(true);
        });
    }
}