import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class NurseDashboard extends JFrame {
    // Modern hospital color scheme
    private static final Color PRIMARY_COLOR = new Color(0, 120, 150);  // Teal Blue
    private static final Color SECONDARY_COLOR = new Color(200, 230, 240); // Light Teal Blue
    private static final Color SUCCESS_COLOR = new Color(0, 100, 140);  // Deep Teal Blue
    private static final Color DANGER_COLOR = new Color(230, 60, 80);   // Soft Red for alerts
    private static final Color WARNING_COLOR = new Color(255, 180, 50);  // Golden Yellow from logo
    private static final Color INFO_COLOR = new Color(0, 160, 180);      // Sky Blue variant
    private static final Color LIGHT_COLOR = new Color(240, 242, 245);  // Light Grayish White
    private static final Color DARK_COLOR = new Color(20, 40, 80);     // Darker Blue for contrast
    private static final Color BACKGROUND_COLOR = new Color(210, 235, 250);

    // Font constants
    private static final Font HEADER_FONT = new Font("Montserrat", Font.BOLD, 20);
    private static final Font TITLE_FONT = new Font("Montserrat", Font.BOLD, 16);
    private static final Font NORMAL_FONT = new Font("Montserrat", Font.PLAIN, 14);
    private static final Font SMALL_FONT = new Font("Montserrat", Font.PLAIN, 12);

    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/HMsystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Ashish030406";

    private String userId;
    private Connection connection;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private Map<String, String> nurseDetails = new HashMap<>();
    private DefaultTableModel appointmentsModel;
    private JLabel currentTimeLabel;
    private Timer timeUpdateTimer;
    private JButton activeSidebarButton;
    private JComboBox<String> statusFilter;
    private JComboBox<String> dateFilter;



    public NurseDashboard(String userId) {
        initializeDB();
        this.userId = userId;
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadNurseDetails();
        initializeUI();
        startTimeUpdater();
        setLocationRelativeTo(null); // Center the window
    }

    private void initializeDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/HMsystem",
                    "root",
                    "Ashish030406");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Database connection failed: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }


    private void loadNurseDetails() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT name, email, phone FROM employees WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nurseDetails.put("name", rs.getString("name"));
                nurseDetails.put("email", rs.getString("email"));
                nurseDetails.put("phone", rs.getString("phone"));
            }
        } catch (SQLException ex) {
            showError("Error loading nurse details: " + ex.getMessage());
        }
    }

    private void startTimeUpdater() {
        timeUpdateTimer = new Timer(1000, e -> {
            if (currentTimeLabel != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                currentTimeLabel.setText("Current Time: " + LocalDateTime.now().format(formatter));
            }
        });
        timeUpdateTimer.start();
    }

    private void initializeUI() {
        setTitle("MediCare Hospital - Nurse Portal");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);


        // Top status bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.NORTH);

        // Side Panel
        JPanel sidePanel = createSidePanel();
        add(sidePanel, BorderLayout.WEST);

        // Content Panel
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(BACKGROUND_COLOR);
        add(contentPanel, BorderLayout.CENTER);

        // Create all panels
        createHomePanel();
        createTimePunchPanel();
        createPatientPanel();
        createMedicinePanel();
        createDoctorPanel();
        createAppointmentPanel();

        // Activate the home panel by default
        JButton homeButton = (JButton) ((JPanel) sidePanel.getComponent(1)).getComponent(0);
        activateSidebarButton(homeButton);

        setVisible(true);
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(SUCCESS_COLOR);
        statusBar.setPreferredSize(new Dimension(getWidth(), 40));
        statusBar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        JLabel hospitalLabel = new JLabel("MediCare Hospital Management System");
        hospitalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hospitalLabel.setForeground(Color.WHITE);

        currentTimeLabel = new JLabel();
        currentTimeLabel.setFont(new Font("Segoe UI ", Font.PLAIN, 12));
        currentTimeLabel.setForeground(Color.WHITE);

        statusBar.add(hospitalLabel, BorderLayout.WEST);
        statusBar.add(currentTimeLabel, BorderLayout.EAST);

        return statusBar;
    }

    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(Color.white);
        sidePanel.setPreferredSize(new Dimension(250, getHeight()));

        sidePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0, 80, 120)));


        // Profile section at the top (unchanged)
        JPanel profileSection = new JPanel();
        profileSection.setLayout(new BoxLayout(profileSection, BoxLayout.Y_AXIS));
        profileSection.setBackground(Color.white);
        profileSection.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        profileSection.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Circle avatar (unchanged)
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(SECONDARY_COLOR);
                g2d.fillOval(0, 0, 70, 70);
                g2d.setColor(PRIMARY_COLOR);
                g2d.setFont(new Font("Segoe UI ", Font.BOLD, 28));

                String initials = nurseDetails.containsKey("name") && nurseDetails.get("name").length() > 0
                        ? nurseDetails.get("name").substring(0, 1).toUpperCase()
                        : "";
                FontMetrics fm = g2d.getFontMetrics();
                int stringWidth = fm.stringWidth(initials);
                int stringHeight = fm.getHeight();
                g2d.drawString(initials, (70 - stringWidth) / 2, (70 - stringHeight) / 2 + fm.getAscent());
                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(70, 70);
            }
        };
        avatarPanel.setMaximumSize(new Dimension(70, 70));
        avatarPanel.setOpaque(false);
        avatarPanel.setBackground(new Color(0, 0, 0, 0));
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(nurseDetails.get("name"));
        nameLabel.setFont(new Font("Segoe UI ", Font.BOLD, 16));
        nameLabel.setForeground(Color.black);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("Registered Nurse");
        roleLabel.setFont(new Font("Segoe UI ", Font.PLAIN, 12));
        roleLabel.setForeground(Color.black);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileSection.add(avatarPanel);
        profileSection.add(Box.createRigidArea(new Dimension(0, 10)));
        profileSection.add(nameLabel);
        profileSection.add(Box.createRigidArea(new Dimension(0, 5)));
        profileSection.add(roleLabel);

        sidePanel.add(profileSection);
        sidePanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));


        // Navigation menu - ONLY CHANGED THE GAPS BETWEEN BUTTONS
        sidePanel.add(createSidebarButtonPanel("Dashboard", "home", "/icons/dashboard.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6))); // Increased from default

        sidePanel.add(createSidebarButtonPanel("Time Punch", "time", "/icons/clock.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6))); // Increased from default

        sidePanel.add(createSidebarButtonPanel("Patients", "patients", "/icons/patient.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6))); // Increased from default

        sidePanel.add(createSidebarButtonPanel("Medicine Stock", "medicines", "/icons/medicine.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6))); // Increased from default

        sidePanel.add(createSidebarButtonPanel("Doctors", "doctors", "/icons/doctor.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6))); // Increased from default

        sidePanel.add(createSidebarButtonPanel("Appointments", "appointments", "/icons/calendar.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6))); // Increased from default

        sidePanel.add(Box.createVerticalGlue());

        // Create logout button with unique hover effects
        JPanel logoutPanel = createSidebarButtonPanel("Logout", "logout", "/icons/logout.png");
        logoutPanel.setBackground(WARNING_COLOR);  // Initial background color

// Remove any existing mouse listeners to prevent conflicts
        MouseListener[] listeners = logoutPanel.getMouseListeners();
        for (MouseListener ml : listeners) {
            logoutPanel.removeMouseListener(ml);
        }

// Find the text label component
        JLabel logoutLabel = null;
        for (Component comp : logoutPanel.getComponents()) {
            if (comp instanceof JLabel && comp.getParent() == logoutPanel) {
                logoutLabel = (JLabel) comp;
                break;
            }
        }

// Custom hover effects for logout button
        JLabel finalLogoutLabel = logoutLabel;
        logoutPanel.addMouseListener(new MouseAdapter() {
            private final Color originalBg = WARNING_COLOR;
            // Even darker when pressed
            private final Color originalText = finalLogoutLabel != null ? finalLogoutLabel.getForeground() : Color.WHITE;

            @Override
            public void mouseEntered(MouseEvent e) {

                if (finalLogoutLabel != null) {
                    finalLogoutLabel.setForeground(Color.WHITE);
                }
                logoutPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutPanel.setBackground(originalBg);
                if (finalLogoutLabel != null) {
                    finalLogoutLabel.setForeground(originalText);
                }
                logoutPanel.setCursor(Cursor.getDefaultCursor());
            }




            @Override
            public void mouseClicked(MouseEvent e) {
                performLogout();
            }
        });

// Add to panel
        sidePanel.add(logoutPanel);


        return sidePanel;
    }

    private JPanel createSidebarButtonPanel(String text, String card, String iconPath) {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        // Set different background for logout button
        Color bgColor = card.equals("logout") ? WARNING_COLOR : SUCCESS_COLOR;
        buttonPanel.setBackground(bgColor);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        buttonPanel.setMaximumSize(new Dimension(250, 50));

        JButton btn = new JButton(text);
        btn.setFont(NORMAL_FONT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);  // Use the same background color
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        // Try to load icon
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(img));
                btn.setIconTextGap(15);
            }
        } catch (Exception e) {
            // Fallback to text-only if icon fails
        }

        // Only add hover effect if not logout button
        if (!card.equals("logout")) {
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (btn != activeSidebarButton) {
                        buttonPanel.setBackground(new Color(0, 100, 140));
                        btn.setBackground(new Color(0, 120, 150));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (btn != activeSidebarButton) {
                        buttonPanel.setBackground(SUCCESS_COLOR);
                        btn.setBackground(DARK_COLOR);
                    }
                }
            });
        } else {
            // Custom hover effect for logout button
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    buttonPanel.setBackground(WARNING_COLOR.darker());
                    btn.setBackground(WARNING_COLOR.darker());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    buttonPanel.setBackground(WARNING_COLOR);
                    btn.setBackground(WARNING_COLOR);
                }
            });
        }

        btn.addActionListener(e -> {
            if (card.equals("logout")) {
                performLogout();
            } else {
                cardLayout.show(contentPanel, card);
                activateSidebarButton(btn);
                if (card.equals("appointments")) {
                    boolean today=true;
                    loadAppointmentsData(today,(String) statusFilter.getSelectedItem());
                }
            }
        });

        buttonPanel.add(btn);
        return buttonPanel;
    }



    private void activateSidebarButton(JButton button) {
        if (activeSidebarButton != null) {
            activeSidebarButton.setBackground(SUCCESS_COLOR);
            activeSidebarButton.getParent().setBackground(SUCCESS_COLOR);
            activeSidebarButton.setFont(NORMAL_FONT);
        }
        activeSidebarButton = button;
        button.setBackground(PRIMARY_COLOR);
        button.getParent().setBackground(PRIMARY_COLOR);
        button.setFont(new Font("Segoe UI ", Font.BOLD, 14));
    }

    private void createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Header with profile and date
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Welcome section
        JPanel welcomePanel = new JPanel(new BorderLayout(15, 0));
        welcomePanel.setOpaque(false);

        // Profile icon (circular)
        JLabel profileIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(100, 170, 255));
                g2.fillOval(0, 0, getWidth(), getHeight());

                // Initials
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                String initials = nurseDetails.get("name").substring(0, 1) +
                        (nurseDetails.get("name").contains(" ") ?
                                nurseDetails.get("name").substring(nurseDetails.get("name").indexOf(" ")+1, nurseDetails.get("name").indexOf(" ")+2) : "");
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initials)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initials, x, y);
                g2.dispose();
            }
        };
        profileIcon.setPreferredSize(new Dimension(50, 50));

        // Welcome text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back,");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeLabel.setForeground(new Color(120, 120, 120));

        JLabel nameLabel = new JLabel(nurseDetails.get("name"));
        nameLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 24));
        nameLabel.setForeground(new Color(50, 50, 50));

        textPanel.add(welcomeLabel);
        textPanel.add(nameLabel);

        welcomePanel.add(profileIcon, BorderLayout.WEST);
        welcomePanel.add(textPanel, BorderLayout.CENTER);

        // Date panel
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        JLabel dateLabel = new JLabel(dateFormat.format(new java.util.Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(150, 150, 150));
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        headerPanel.add(welcomePanel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        panel.add(headerPanel, BorderLayout.NORTH);

        // Stats cards grid
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setOpaque(false);

        // Use SwingWorker to load data asynchronously
        SwingWorker<Map<String, String>, Void> dataLoader = new SwingWorker<>() {
            @Override
            protected Map<String, String> doInBackground() {
                Map<String, String> data = new HashMap<>();
                data.put("appointments", getTodaysAppointments());
                data.put("patients", getActivePatients());
                data.put("medicines", getLowStockMedicines());
                data.put("messages", getNewMessages());
                return data;
            }

            @Override
            protected void done() {
                try {
                    Map<String, String> data = get();

                    // Create cards with modern styling
                    JPanel appointmentsCard = createModernStatCard(
                            "Appointments",
                            data.get("appointments"),
                            new Color(100, 170, 255),
                            "⏱️"
                    );

                    JPanel patientsCard = createModernStatCard(
                            "Active Patients",
                            data.get("patients"),
                            new Color(40, 180, 130),
                            "👥"
                    );

                    JPanel medicineCard = createModernStatCard(
                            "Low Stock",
                            data.get("medicines"),
                            new Color(255, 140, 50),
                            "💊"
                    );

//                    JPanel messagesCard = createModernStatCard(
//                            "Messages",
//                            data.get("messages"),
//                            new Color(220, 90, 120),
//                            "✉️"
//                    );

                    statsPanel.removeAll();
                    statsPanel.add(appointmentsCard);
                    statsPanel.add(patientsCard);
                    statsPanel.add(medicineCard);
                   // statsPanel.add(messagesCard);

                    statsPanel.revalidate();
                    statsPanel.repaint();

                } catch (Exception e) {
                    logError("Error loading dashboard data", e);
                    showErrorMessage("Could not load dashboard data. Please try again.");
                }
            }
        };
        dataLoader.execute();

        // Add loading cards initially
        for (int i = 0; i < 4; i++) {
            statsPanel.add(createModernLoadingCard());
        }

        panel.add(statsPanel, BorderLayout.CENTER);

        // Recent appointments section
        JPanel appointmentsPanel = new JPanel(new BorderLayout());
        appointmentsPanel.setOpaque(false);
        appointmentsPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        JLabel sectionTitle = new JLabel("Recent Appointments");
        sectionTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(60, 60, 60));
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        appointmentsPanel.add(sectionTitle, BorderLayout.NORTH);

        // Create table with real data from database
        String[] columnNames = {"Patient", "Doctor", "Date", "Time", "Status"};
        Object[][] recentAppointments = getRecentAppointmentsFromDatabase();

        JTable appointmentsTable = new JTable(recentAppointments, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Custom table styling
        appointmentsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        appointmentsTable.setRowHeight(40);
        appointmentsTable.setShowGrid(false);
        appointmentsTable.setIntercellSpacing(new Dimension(0, 0));
        appointmentsTable.setFillsViewportHeight(true);

        // Custom header renderer
        appointmentsTable.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        appointmentsTable.getTableHeader().setBackground(new Color(240, 242, 245));
        appointmentsTable.getTableHeader().setForeground(new Color(100, 100, 100));
        appointmentsTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        // Custom cell renderer
        appointmentsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Alternate row colors
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }

                // Status column coloring
                if (column == 4) {
                    String status = value.toString();
                    if (status.equalsIgnoreCase("completed")) {
                        c.setForeground(new Color(40, 180, 130)); // Green
                    } else if (status.equalsIgnoreCase("upcoming")) {
                        c.setForeground(new Color(100, 170, 255)); // Blue
                    } else if (status.equalsIgnoreCase("cancelled")) {
                        c.setForeground(new Color(220, 90, 120)); // Red
                    }
                }

                // Center align date, time, status
                if (column >= 2) {
                    ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel)c).setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        appointmentsPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(appointmentsPanel, BorderLayout.SOUTH);

        this.contentPanel.add(panel, "home");
    }

    private Object[][] getRecentAppointmentsFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT p.name AS patient, d.name AS doctor, " +
                    "DATE_FORMAT(a.date, '%d %b %Y') AS formatted_date, " +
                    "DATE_FORMAT(a.time, '%h:%i %p') AS formatted_time, " +
                    "a.status " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN doctors d ON a.doctor_id = d.user_id " +
                    "WHERE a.date >= CURDATE() " +
                    "ORDER BY a.date ASC, a.time ASC " +
                    "LIMIT 5"; // Get next 5 upcoming appointments

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                List<Object[]> rows = new ArrayList<>();
                while (rs.next()) {
                    Object[] row = new Object[5];
                    row[0] = rs.getString("patient");
                    row[1] = rs.getString("doctor");
                    row[2] = rs.getString("formatted_date");
                    row[3] = rs.getString("formatted_time");
                    row[4] = rs.getString("status");
                    rows.add(row);
                }

                return rows.toArray(new Object[0][]);
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching appointments: " + ex.getMessage());
            // Return empty data if error occurs
            return new Object[0][5];
        }
    }


    private JPanel createModernStatCard(String title, String value, Color color, String emoji) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Add subtle shadow
        card.setUI(new RoundedPanelUI(12, color));

        // Emoji/icon label
        JLabel emojiLabel = new JLabel(emoji, SwingConstants.CENTER);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        // Text content
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(120, 120, 120));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 28));
        valueLabel.setForeground(new Color(60, 60, 60));

        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        textPanel.add(valueLabel);

        card.add(emojiLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        // Add hover animation
        card.addMouseListener(new MouseAdapter() {
            private Timer timer;
            private float shadowSize = 0;

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                if (timer != null) timer.stop();

                timer = new Timer(10, evt -> {
                    shadowSize = Math.min(shadowSize + 0.2f, 8);
                    card.setUI(new RoundedPanelUI(12, color, shadowSize));
                    card.repaint();
                    if (shadowSize >= 8) timer.stop();
                });
                timer.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                if (timer != null) timer.stop();

                timer = new Timer(10, evt -> {
                    shadowSize = Math.max(shadowSize - 0.2f, 0);
                    card.setUI(new RoundedPanelUI(12, color, shadowSize));
                    card.repaint();
                    if (shadowSize <= 0) timer.stop();
                });
                timer.start();
            }
        });

        return card;
    }



    private JPanel createModernLoadingCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setUI(new RoundedPanelUI(12, new Color(240, 240, 240)));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Skeleton loader for emoji
        JPanel emojiLoader = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 230, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                g2.dispose();
            }
        };
        emojiLoader.setPreferredSize(new Dimension(40, 40));
        emojiLoader.setMaximumSize(new Dimension(40, 40));

        // Skeleton loader for text
        JPanel textLoader = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 230, 230));

                // Title loader
                g2.fillRoundRect(0, 0, 120, 16, 8, 8);

                // Value loader
                g2.fillRoundRect(0, 30, 60, 28, 8, 8);

                g2.dispose();
            }
        };
        textLoader.setPreferredSize(new Dimension(120, 60));

        content.add(emojiLoader);
        content.add(Box.createRigidArea(new Dimension(0, 15)));
        content.add(textLoader);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // Modern table cell renderer
    private class ModernTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
            setFont(new Font("Segoe UI", Font.PLAIN, 14));

            if (isSelected) {
                setBackground(new Color(220, 235, 255));
                setForeground(new Color(50, 50, 50));
            } else {
                if (row % 2 == 0) {
                    setBackground(new Color(250, 250, 250));
                } else {
                    setBackground(Color.WHITE);
                }
                setForeground(new Color(80, 80, 80));
            }

            // Status column styling
            if (column == 4) {
                String status = value.toString();
                if ("Completed".equals(status)) {
                    setForeground(new Color(40, 180, 130));
                } else if ("Upcoming".equals(status)) {
                    setForeground(new Color(255, 140, 50));
                } else if ("Cancelled".equals(status)) {
                    setForeground(new Color(220, 90, 120));
                }
                setFont(getFont().deriveFont(Font.BOLD));
            }

            return this;
        }
    }

    // Enhanced RoundedPanelUI with shadow support
    private class RoundedPanelUI extends BasicPanelUI {
        private int radius;
        private Color accentColor;
        private float shadowSize;

        public RoundedPanelUI(int radius, Color accentColor) {
            this(radius, accentColor, 0);
        }

        public RoundedPanelUI(int radius, Color accentColor, float shadowSize) {
            this.radius = radius;
            this.accentColor = accentColor;
            this.shadowSize = shadowSize;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw shadow
            if (shadowSize > 0) {
                for (int i = 1; i <= shadowSize; i++) {
                    float alpha = (shadowSize - i + 1) / (shadowSize * 2);
                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                            accentColor.getBlue(), (int)(alpha * 50)));
                    g2.fillRoundRect(i, i, c.getWidth()-i*2, c.getHeight()-i*2, radius, radius);
                }
            }

            // Draw panel
            g2.setColor(c.getBackground());
            g2.fillRoundRect(0, 0, c.getWidth() - (int)shadowSize, c.getHeight() - (int)shadowSize, radius, radius);

            // Draw accent border
            g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));
            g2.drawRoundRect(0, 0, c.getWidth() - (int)shadowSize, c.getHeight() - (int)shadowSize, radius, radius);

            g2.dispose();
        }
    }
    private JPanel createTimePunchCard() {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PRIMARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setBackground(PRIMARY_COLOR);
        iconPanel.setPreferredSize(new Dimension(70, 70));
        iconPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/clock.png"));
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                JLabel iconLabel = new JLabel(new ImageIcon(img));
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                iconPanel.add(iconLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel fallbackIcon = new JLabel("⏱");
            fallbackIcon.setFont(new Font("Segoe UI ", Font.BOLD, 24));
            fallbackIcon.setForeground(Color.WHITE);
            fallbackIcon.setHorizontalAlignment(SwingConstants.CENTER);
            iconPanel.add(fallbackIcon, BorderLayout.CENTER);
        }

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Time Punch");
        titleLabel.setFont(new Font("Segoe UI ", Font.PLAIN, 14));
        titleLabel.setForeground(DARK_COLOR);

        JLabel lastPunchLabel = new JLabel("Last: " + getLastPunchTime());
        lastPunchLabel.setFont(new Font("Segoe UI ", Font.PLAIN, 12));
        lastPunchLabel.setForeground(DARK_COLOR);

        JButton punchButton = new JButton(getNextPunchType() + " Punch");
        punchButton.setFont(new Font("Segoe UI ", Font.BOLD, 14));
        punchButton.setBackground(PRIMARY_COLOR);
        punchButton.setForeground(Color.WHITE);
        punchButton.setFocusPainted(false);
        punchButton.setBorderPainted(false);
        punchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        punchButton.addActionListener(e -> recordTimePunch());

        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(lastPunchLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        textPanel.add(punchButton);

        card.add(iconPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRecentAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel titleLabel = new JLabel("Recent Appointments");
        titleLabel.setFont(new Font("Segoe UI ", Font.BOLD, 16));
        titleLabel.setForeground(DARK_COLOR);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Patient", "Doctor", "Date", "Time", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT p.name AS patient_name, d.name AS doctor_name, " +
                    "a.date, a.time, a.status " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN doctors d ON a.doctor_id = d.user_id " +
                    "ORDER BY a.date DESC, a.time DESC LIMIT 5";

            ResultSet rs = conn.createStatement().executeQuery(sql);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        dateFormat.format(rs.getDate("date")),
                        timeFormat.format(rs.getTime("time")),
                        rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(NORMAL_FONT);
        table.getTableHeader().setFont(TITLE_FONT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value.toString();
                if (status.equals("Completed")) {
                    c.setForeground(SUCCESS_COLOR);
                } else if (status.equals("Scheduled")) {
                    c.setForeground(INFO_COLOR);
                } else if (status.equals("Cancelled")) {
                    c.setForeground(DANGER_COLOR);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String getNewMessages() {
        try {
            // In a real application, this would query your database or backend service
            // For this example, we'll simulate some data
            int messageCount = new Random().nextInt(10); // Random number between 0-9

            // Return formatted string with proper pluralization
            return messageCount + " new message" + (messageCount != 1 ? "s" : "");
        } catch (Exception e) {
            logError("Error fetching message count", e);
            return "N/A"; // Fallback if there's an error
        }
    }

    private void createTimePunchPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = createPanelHeader("Time Attendance Management", "Track your work hours");
        panel.add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(BACKGROUND_COLOR);

        JPanel punchCardPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        punchCardPanel.setBackground(BACKGROUND_COLOR);

        JPanel punchStatusCard = createPunchStatusCard();
        JPanel punchActionCard = createPunchActionCard();

        punchCardPanel.add(punchStatusCard);
        punchCardPanel.add(punchActionCard);

        contentPanel.add(punchCardPanel, BorderLayout.NORTH);

        JPanel recordsPanel = new JPanel(new BorderLayout(0, 10));
        recordsPanel.setBackground(BACKGROUND_COLOR);

        JLabel recordsTitle = new JLabel("Punch History");
        recordsTitle.setFont(TITLE_FONT);
        recordsTitle.setForeground(DARK_COLOR);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT punch_time, punch_type FROM time_punches WHERE user_id = ? ORDER BY punch_time DESC LIMIT 10";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);

            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Date", "Time", "Type"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

            while (rs.next()) {
                Timestamp punchTime = rs.getTimestamp("punch_time");
                model.addRow(new Object[]{
                        dateFormat.format(punchTime),
                        timeFormat.format(punchTime),
                        rs.getString("punch_type")
                });
            }

            JTable table = new JTable(model);
            table.setRowHeight(40);
            table.setFont(NORMAL_FONT);
            table.getTableHeader().setFont(TITLE_FONT);
            table.setShowGrid(false);
            table.setIntercellSpacing(new Dimension(0, 0));

            table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    String type = value.toString();
                    if (type.equals("IN")) {
                        c.setForeground(SUCCESS_COLOR);
                    } else if (type.equals("OUT")) {
                        c.setForeground(DANGER_COLOR);
                    }
                    return c;
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());

            tablePanel.add(scrollPane, BorderLayout.CENTER);

        } catch (SQLException ex) {
            JLabel errorLabel = new JLabel("Error loading time punch records: " + ex.getMessage());
            errorLabel.setForeground(DANGER_COLOR);
            tablePanel.add(errorLabel, BorderLayout.CENTER);
            ex.printStackTrace();
        }

        recordsPanel.add(recordsTitle, BorderLayout.NORTH);
        recordsPanel.add(tablePanel, BorderLayout.CENTER);

        contentPanel.add(recordsPanel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);

        this.contentPanel.add(panel, "time");
    }

    private JPanel createPunchStatusCard() {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PRIMARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Current Status");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(DARK_COLOR);

        JLabel statusLabel = new JLabel(getCurrentPunchStatus());
        statusLabel.setFont(new Font("Segoe UI ", Font.BOLD, 24));
        statusLabel.setForeground(getNextPunchType().equals("IN") ? DANGER_COLOR : SUCCESS_COLOR);

        JLabel lastPunchLabel = new JLabel("Last Punch: " + getLastPunchTime());
        lastPunchLabel.setFont(NORMAL_FONT);
        lastPunchLabel.setForeground(DARK_COLOR);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        textPanel.add(statusLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(lastPunchLabel);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createPunchActionCard() {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PRIMARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Punch Action");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(DARK_COLOR);

        JButton punchButton = new JButton(getNextPunchType() + " Punch");
        punchButton.setFont(new Font("Segoe UI ", Font.BOLD, 16));
        punchButton.setBackground(PRIMARY_COLOR);
        punchButton.setForeground(Color.WHITE);
        punchButton.setFocusPainted(false);
        punchButton.setBorderPainted(false);
        punchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        punchButton.addActionListener(e -> {
            recordTimePunch();
            // Refresh the panel
            cardLayout.show(contentPanel, "time");
            createTimePunchPanel();
            cardLayout.show(contentPanel, "time");
        });

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        textPanel.add(punchButton);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private void createPatientPanel() {
        // Main panel with modern card-like design
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Create a stylish header section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Left side title with accent bar
        JPanel titlePanel = new JPanel(new BorderLayout(15, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(5, 35));
        accentBar.setBackground(PRIMARY_COLOR);
        titlePanel.add(accentBar, BorderLayout.WEST);

        JLabel headerLabel = new JLabel("Patient Management");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerLabel.setForeground(DARK_COLOR);
        titlePanel.add(headerLabel, BorderLayout.CENTER);

        // Right side search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(BACKGROUND_COLOR);

        JTextField searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(searchField.getPreferredSize().width, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search patients...");

        JButton searchButton = createModernButtons("Search", new Color(7, 86, 154), Color.WHITE);

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);

        // Table with card-like container
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // Custom table model with non-editable cells
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{"Patient ID", "Name", "Email", "Phone", "Blood Group", "Gender", "DOB"});

        // Create and style table
        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setIntercellSpacing(new Dimension(10, 10));
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(240, 240, 255));
        table.setSelectionForeground(DARK_COLOR);
        table.getTableHeader().setBackground(new Color(245, 245, 245));
        table.getTableHeader().setForeground(DARK_COLOR);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getPreferredSize().width, 40));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Setup table renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Specialized renderer for colored status badges (for Blood Group column)
        DefaultTableCellRenderer specialRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                if (column == 4) { // Blood Group column
                    label.setOpaque(true);
                    label.setBackground(new Color(230, 240, 255));
                    label.setForeground(PRIMARY_COLOR);
                    label.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(210, 220, 255), 1, true),
                            BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                }

                return label;
            }
        };

        // Apply renderers
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 4) { // Blood Group column
                table.getColumnModel().getColumn(i).setCellRenderer(specialRenderer);
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // Custom scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Add empty state panel (shown when no data)
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(Color.WHITE);
        JLabel emptyLabel = new JLabel("No patients found", JLabel.CENTER);
        emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        emptyLabel.setForeground(new Color(150, 150, 150));
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);

        // Add components to table container
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Button panel with modern design
        JPanel actionPanel = new JPanel(new BorderLayout(0, 15));
        actionPanel.setBackground(BACKGROUND_COLOR);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Stats panel showing counts
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.setBackground(BACKGROUND_COLOR);

        JLabel totalPatientsLabel = new JLabel("Total Patients: 0");
        totalPatientsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalPatientsLabel.setForeground(DARK_COLOR);

        statsPanel.add(totalPatientsLabel);

        // Action buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(BACKGROUND_COLOR);

        // Modern styled buttons without icons
        JButton addButton = createModernButtons("Add Patient", new Color(76, 175, 80), Color.WHITE);
        JButton editButton = createModernButtons("Edit", new Color(255, 152, 0), Color.WHITE);
        JButton deleteButton = createModernButtons("Delete", new Color(244, 67, 54), Color.WHITE);
        JButton refreshButton = createModernButtons("Refresh", new Color(0, 120, 215), Color.WHITE);

        // Button actions
        addButton.addActionListener(e -> showPatientDialog(null, model));
        editButton.addActionListener(e -> editPatient(table, model));
        deleteButton.addActionListener(e -> deletePatient(table, model));
        refreshButton.addActionListener(e -> {
            refreshPatientTable(model);
            totalPatientsLabel.setText("Total Patients: " + model.getRowCount());
        });

        // Add buttons to panel
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(addButton);

        // Add components to action panel
        actionPanel.add(statsPanel, BorderLayout.WEST);
        actionPanel.add(buttonsPanel, BorderLayout.EAST);

        // Load initial data
        try {
            loadPatientTableData(model);
            totalPatientsLabel.setText("Total Patients: " + model.getRowCount());
        } catch (SQLException e) {
            showError("Failed to load patients: " + e.getMessage());
        }

        // Add search functionality
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            if (searchTerm.isEmpty()) {
                try {
                    loadPatientTableData(model);
                } catch (SQLException ex) {
                    showError("Failed to load patients: " + ex.getMessage());
                }
            } else {
                try {
                    String query = "SELECT * FROM patients WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? LIMIT 100";
                    PreparedStatement stmt = connection.prepareStatement(query);
                    String pattern = "%" + searchTerm + "%";
                    stmt.setString(1, pattern);
                    stmt.setString(2, pattern);
                    stmt.setString(3, pattern);

                    ResultSet rs = stmt.executeQuery();
                    model.setRowCount(0);

                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getInt("id"));
                        row.add(rs.getString("name"));
                        row.add(rs.getString("email"));
                        row.add(rs.getString("phone"));
                        row.add(rs.getString("blood_group"));
                        row.add(rs.getString("gender"));
                        row.add(rs.getDate("dob"));
                        model.addRow(row);
                    }

                    totalPatientsLabel.setText("Total Patients: " + model.getRowCount());
                } catch (SQLException ex) {
                    showError("Failed to search patients: " + ex.getMessage());
                }
            }
        });

        // Add all components to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tableContainer, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        this.contentPanel.add(panel, "patients");
    }

    // Helper method to load patient table data
    private void loadPatientTableData(DefaultTableModel model) throws SQLException {
        model.setRowCount(0);
        String query = "SELECT id, name, email, phone, blood_group, gender, dob FROM patients LIMIT 100";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("blood_group"));
                row.add(rs.getString("gender"));
                row.add(rs.getDate("dob"));
                model.addRow(row);
            }
        }
    }

    // Helper method to create modern buttons without icons
    private JButton createModernButtons(String text, Color bgColor, Color fgColor) {
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

    private void refreshPatientTable(DefaultTableModel model) {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0); // Clear existing table rows

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name, email, phone, blood_group, gender, dob FROM patients")) {

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("blood_group"),
                            rs.getString("gender"),
                            rs.getString("dob")
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error fetching patient data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showPatientDialog(Vector<Object> data, DefaultTableModel model) {
        // Create dialog with modern look
        JDialog dialog = new JDialog(this, "Patient Details", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(550, 650);
        dialog.setLocationRelativeTo(this);
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create main panel with better spacing
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                        BorderFactory.createEmptyBorder(20, 20, 20, 20)
                )
        ));

        // Header panel with title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Patient Information");
        titleLabel.setFont(new Font("Segoe UI ", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));
        headerPanel.add(titleLabel);

        // Form panel with grouped sections
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Account information section
        JPanel accountPanel = createSectionPanel("Account Information");
        JPanel accountFieldsPanel = new JPanel(new GridLayout(2, 2, 15, 10));

        JTextField usernameField = createStyledTextField();
        JPasswordField passwordField = createStyledPasswordField();

        accountFieldsPanel.add(createLabelPanel("Username:"));
        accountFieldsPanel.add(usernameField);
        accountFieldsPanel.add(createLabelPanel("Password:"));
        accountFieldsPanel.add(passwordField);
        accountPanel.add(accountFieldsPanel);

        // Personal information section
        JPanel personalPanel = createSectionPanel("Personal Information");
        JPanel personalFieldsPanel = new JPanel(new GridLayout(4, 2, 15, 10));

        JTextField nameField = createStyledTextField();
        JTextField emailField = createStyledTextField();
        JTextField phoneField = createStyledTextField();
        JTextField dobField = createStyledTextField();

        personalFieldsPanel.add(createLabelPanel("Full Name:"));
        personalFieldsPanel.add(nameField);
        personalFieldsPanel.add(createLabelPanel("Email:"));
        personalFieldsPanel.add(emailField);
        personalFieldsPanel.add(createLabelPanel("Phone:"));
        personalFieldsPanel.add(phoneField);
        personalFieldsPanel.add(createLabelPanel("Date of Birth (YYYY-MM-DD):"));
        personalFieldsPanel.add(dobField);
        personalPanel.add(personalFieldsPanel);

        // Medical information section
        JPanel medicalPanel = createSectionPanel("Medical Information");
        JPanel medicalFieldsPanel = new JPanel(new GridLayout(3, 2, 15, 10));

        JComboBox<String> bloodGroupCombo = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JTextField addressField = createStyledTextField();

        styleComboBox(bloodGroupCombo);
        styleComboBox(genderCombo);

        medicalFieldsPanel.add(createLabelPanel("Blood Group:"));
        medicalFieldsPanel.add(bloodGroupCombo);
        medicalFieldsPanel.add(createLabelPanel("Gender:"));
        medicalFieldsPanel.add(genderCombo);
        medicalFieldsPanel.add(createLabelPanel("Address:"));
        medicalFieldsPanel.add(addressField);
        medicalPanel.add(medicalFieldsPanel);

        // Add sections to form panel
        formPanel.add(accountPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(personalPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(medicalPanel);

        // Scroll pane for form
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Populate fields if editing an existing patient
        if (data != null) {
            usernameField.setText(data.get(1).toString());
            passwordField.setText(data.get(2).toString());
            nameField.setText(data.get(3).toString());
            emailField.setText(data.get(4).toString());
            phoneField.setText(data.get(5).toString());
            addressField.setText(data.get(6).toString());
            bloodGroupCombo.setSelectedItem(data.get(7).toString());
            genderCombo.setSelectedItem(data.get(8).toString());
            dobField.setText(data.get(9).toString());
        }

        // Buttons Panel with modern styling
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(189, 195, 199), Color.WHITE);

        JButton saveButton = new JButton("Save Patient");
        styleButton(saveButton, new Color(41, 128, 185), Color.WHITE);

        // Save button action listener (functionality unchanged)
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                if (usernameField.getText().isEmpty() || passwordField.getPassword().length == 0 ||
                        nameField.getText().isEmpty() || emailField.getText().isEmpty() ||
                        phoneField.getText().isEmpty() || dobField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Prepare SQL query
                String sql;
                if (data == null) {
                    sql = "INSERT INTO patients (username, password, name, email, phone, address, blood_group, gender, dob) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                } else {
                    sql = "UPDATE patients SET username=?, password=?, name=?, email=?, phone=?, address=?, blood_group=?, gender=?, dob=? WHERE id=?";
                }

                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, usernameField.getText());
                    stmt.setString(2, new String(passwordField.getPassword()));
                    stmt.setString(3, nameField.getText());
                    stmt.setString(4, emailField.getText());
                    stmt.setString(5, phoneField.getText());
                    stmt.setString(6, addressField.getText());
                    stmt.setString(7, bloodGroupCombo.getSelectedItem().toString());
                    stmt.setString(8, genderCombo.getSelectedItem().toString());
                    stmt.setString(9, dobField.getText());

                    if (data != null) {
                        stmt.setInt(10, (Integer) data.get(0));
                    }

                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows > 0) {
                        dialog.dispose();
                        refreshPatientTable(model);

                        // Improved success message
                        JOptionPane.showMessageDialog(
                                null,
                                data == null ? "Patient added successfully!" : "Patient updated successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving patient: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Cancel button action unchanged
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        // Assemble the dialog
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Set default button
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.setVisible(true);
    }

    // Helper methods for UI components
    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220)),
                        title,
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI ", Font.BOLD, 14),
                        new Color(41, 128, 185)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return panel;
    }

    private JPanel createLabelPanel(String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI ", Font.PLAIN, 12));
        panel.add(label);
        return panel;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)
        ));
        return field;
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 30));
        comboBox.setBackground(Color.WHITE);
        ((JComponent) comboBox.getRenderer()).setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI ", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(120, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private void editPatient(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "⚠ Please select a patient to edit!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int patientId = (Integer) model.getValueAt(selectedRow, 0);

            // Fetch full patient data
            String query = "SELECT * FROM patients WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, patientId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Vector<Object> patientData = new Vector<>();
                    patientData.add(rs.getInt("id"));
                    patientData.add(rs.getString("username"));
                    patientData.add(rs.getString("password"));
                    patientData.add(rs.getString("name"));
                    patientData.add(rs.getString("email"));
                    patientData.add(rs.getString("phone"));
                    patientData.add(rs.getString("address"));
                    patientData.add(rs.getString("blood_group"));
                    patientData.add(rs.getString("gender"));
                    patientData.add(rs.getString("dob"));

                    // Open the patient dialog
                    showPatientDialog(patientData, model);
                } else {
                    JOptionPane.showMessageDialog(null, "⚠ Patient not found!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading patient data:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showErrorDialog(String title, String message) {
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI ", Font.BOLD, 14));

        JOptionPane.showMessageDialog(null,
                "<html><b style='color:red;'>❌ " + title + ":</b><br>" + message + "</html>",
                title, JOptionPane.ERROR_MESSAGE);
    }

    private void deletePatient(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) {
            showErrorDialog("No Selection", "⚠ Please select a patient to delete!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null,
                "🗑 Are you sure you want to delete this patient?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = (Integer) model.getValueAt(row, 0);
                try (PreparedStatement stmt = connection.prepareStatement(
                        "DELETE FROM patients WHERE id = ?")) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    model.removeRow(row);
                    JOptionPane.showMessageDialog(null, "✅ Patient deleted successfully!");
                }
            } catch (SQLException e) {
                showErrorDialog("Database Error", "Failed to delete patient:\n" + e.getMessage());
            }
        }
    }


    private void createMedicinePanel() {
        // Main panel with card layout for content organization
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Enhanced header with title, subtitle and action buttons
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Left side - Title section
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Medicine Inventory");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 33, 33));

        JLabel subtitleLabel = new JLabel("Monitor and manage your pharmacy stock");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 100, 100));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        // Right side - Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setBackground(BACKGROUND_COLOR);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(7, 86, 154)); // Solid blue background
        refreshButton.setForeground(Color.WHITE); // White text
        refreshButton.setFont(NORMAL_FONT);
        refreshButton.setFocusPainted(false); // Remove focus border
        refreshButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Proper padding
        refreshButton.setOpaque(true); // Ensure background is painted
        refreshButton.setBorderPainted(false);

        actionPanel.add(refreshButton);


        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        // Enhanced search panel with multiple filters
        JPanel controlPanel = new JPanel(new BorderLayout(15, 0));
        controlPanel.setBackground(BACKGROUND_COLOR);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Left side - Search field with icon
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(BACKGROUND_COLOR);

        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search medicines by name or description...");

        // Right side - Filters
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setBackground(BACKGROUND_COLOR);

        String[] stockOptions = {"All Stock", "Low Stock", "Out of Stock", "Expiring Soon"};
        JComboBox<String> stockFilter = new JComboBox<>(stockOptions);
        stockFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        stockFilter.setPreferredSize(new Dimension(150, 37));

        String[] sortOptions = {"Name (A-Z)", "Expiry Date", "Quantity (Low-High)", "Price (Low-High)"};
        JComboBox<String> sortFilter = new JComboBox<>(sortOptions);
        sortFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sortFilter.setPreferredSize(new Dimension(150, 37));

        filterPanel.add(new JLabel("Stock Status:"));
        filterPanel.add(stockFilter);
        filterPanel.add(new JLabel("Sort By:"));
        filterPanel.add(sortFilter);

        searchPanel.add(searchField, BorderLayout.CENTER);

        controlPanel.add(searchPanel, BorderLayout.CENTER);
        controlPanel.add(filterPanel, BorderLayout.EAST);

        // Main content panel - Table with data
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // Table model with status column
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Name", "Description", "Quantity", "Price ($)", "Expiry Date", "Supplier", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // ID
                if (columnIndex == 3) return Integer.class; // Quantity
                if (columnIndex == 4) return Double.class;  // Price
                return String.class;
            }
        };

        loadMedicineData(model);

        // Modern styled table
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                    ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                } else {
                    c.setBackground(new Color(232, 240, 254));
                    c.setForeground(new Color(25, 103, 210));
                }
                return c;
            }
        };

        // Table styling
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 246, 247));
        table.getTableHeader().setForeground(new Color(70, 70, 70));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowMargin(0);

        // Set preferred widths
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);  // ID
        columnModel.getColumn(1).setPreferredWidth(150); // Name
        columnModel.getColumn(2).setPreferredWidth(200); // Description
        columnModel.getColumn(3).setPreferredWidth(80);  // Quantity
        columnModel.getColumn(4).setPreferredWidth(80);  // Price
        columnModel.getColumn(5).setPreferredWidth(100); // Expiry Date
        columnModel.getColumn(6).setPreferredWidth(150); // Supplier
        columnModel.getColumn(7).setPreferredWidth(100); // Status

        // Custom renderers for each column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // ID column - Center aligned
        table.getColumnModel().getColumn(0).setMinWidth(60);  // ID column

        // Price column - Currency format
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    setText(String.format("%.2f", (Double) value));
                }
                return c;
            }
        });

        // Quantity column - With color indicators
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    int qty = (Integer) value;
                    if (qty == 0) {
                        setForeground(new Color(220, 53, 69)); // Red for out of stock
                    } else if (qty <= 10) {
                        setForeground(new Color(255, 128, 0)); // Orange for low stock
                    } else {
                        setForeground(isSelected ? new Color(25, 103, 210) : new Color(33, 37, 41));
                    }
                }
                return c;
            }
        });

        // Status column - With colored badges
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String status = value.toString();
                    label.setText(status);
                    label.setHorizontalAlignment(SwingConstants.CENTER);

                    // Create rounded border and background for status badges
                    label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

                    switch (status) {
                        case "In Stock":
                            label.setForeground(new Color(25, 135, 84));
                            label.setBackground(new Color(209, 231, 221));
                            break;
                        case "Low Stock":
                            label.setForeground(new Color(255, 128, 0));
                            label.setBackground(new Color(255, 243, 205));
                            break;
                        case "Out of Stock":
                            label.setForeground(new Color(220, 53, 69));
                            label.setBackground(new Color(248, 215, 218));
                            break;
                        case "Expiring Soon":
                            label.setForeground(new Color(108, 117, 125));
                            label.setBackground(new Color(226, 227, 229));
                            break;
                    }

                    // Override selection background
                    if (isSelected) {
                        label.setBackground(label.getBackground().darker());
                    }
                }
                return label;
            }
        });

        // Create custom scrollpane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Add a summary panel at the bottom
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        summaryPanel.setBackground(new Color(249, 250, 251));
        summaryPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        // Add summary statistics
        summaryPanel.add(createSummaryLabel("Total Items:", calculateTotalItems(model)));
        summaryPanel.add(createSummaryLabel("Low Stock:", countLowStock(model)));
        summaryPanel.add(createSummaryLabel("Expiring Soon:", countExpiringSoon(model)));
        summaryPanel.add(createSummaryLabel("Out of Stock:", countOutOfStock(model)));

        // Add pagination controls
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        paginationPanel.setBackground(new Color(249, 250, 251));
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));
        paginationPanel.add(new JLabel("Rows per page:"));

        String[] pageOptions = {"10", "25", "50", "100"};
        JComboBox<String> pageSize = new JComboBox<>(pageOptions);
        pageSize.setPreferredSize(new Dimension(60, 28));
        pageSize.setMaximumSize(pageSize.getPreferredSize());
        paginationPanel.add(pageSize);

        JLabel pageInfo = new JLabel("1-10 of " + model.getRowCount());
        pageInfo.setFont(NORMAL_FONT);
        pageInfo.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); // Add horizontal padding
        paginationPanel.add(pageInfo);


        // Previous Button (disabled state)
        JButton prevButton = new JButton("Previous");
        prevButton.setEnabled(false);
        prevButton.setBackground(new Color(200, 200, 200)); // Light gray background
        prevButton.setForeground(Color.WHITE); // White text
        prevButton.setFont(NORMAL_FONT);
        prevButton.setFocusPainted(false);
        prevButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // Padding
        prevButton.setOpaque(true);
        prevButton.setBorderPainted(false);

// Next Button (active state)
        JButton nextButton = new JButton("Next");
        nextButton.setBackground(WARNING_COLOR); // Solid blue background
        nextButton.setForeground(Color.WHITE); // White text
        nextButton.setFont(NORMAL_FONT);
        nextButton.setFocusPainted(false);
        nextButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // Padding
        nextButton.setOpaque(true);
        nextButton.setBorderPainted(false);

        paginationPanel.add(prevButton);
        paginationPanel.add(new JLabel("1-10 of " + model.getRowCount()));
        paginationPanel.add(nextButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(summaryPanel, BorderLayout.WEST);
        bottomPanel.add(paginationPanel, BorderLayout.EAST);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add all components to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(controlPanel, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.SOUTH);

        // Add event listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch(searchField.getText().trim(), model, table);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch(searchField.getText().trim(), model, table);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                performSearch(searchField.getText().trim(), model, table);
            }
        });

        stockFilter.addActionListener(e -> filterByStockStatus((String) stockFilter.getSelectedItem(), table));
        sortFilter.addActionListener(e -> sortTable((String) sortFilter.getSelectedItem(), table));
        refreshButton.addActionListener(e -> refreshData(model));

        // Set the panel in the content area
        this.contentPanel.add(panel, "medicines");
    }

    private void stylePaginationButton(JButton button) {
        button.setFont(NORMAL_FONT);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(70, 70, 70));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), // Outer border (line border)
                BorderFactory.createEmptyBorder(4, 12, 4, 12) // Inner border (padding)
        ));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(90, 28)); // Fixed size for both buttons
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(245, 245, 245));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });
    }

    // Helper methods for the medicine panel
    private JButton createIconButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private JLabel createSummaryLabel(String title, int value) {
        JLabel label = new JLabel(title + " " + value);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    private void performSearch(String searchText, DefaultTableModel model, JTable table) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        if (searchText.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // Search in name and description columns (indexes 1 and 2)
            sorter.setRowFilter(RowFilter.regexFilter("(?i).*" + Pattern.quote(searchText) + ".*", 1, 2));
        }
    }

    private void filterByStockStatus(String status, JTable table) {
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) table.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
            table.setRowSorter(sorter);
        }

        if (status.equals("All Stock")) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter(status, 7)); // Filter by status column
        }
    }

    private void sortTable(String sortOption, JTable table) {
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) table.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
            table.setRowSorter(sorter);
        }

        switch (sortOption) {
            case "Name (A-Z)":
                sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
                break;
            case "Expiry Date":
                sorter.setSortKeys(List.of(new RowSorter.SortKey(5, SortOrder.ASCENDING)));
                break;
            case "Quantity (Low-High)":
                sorter.setSortKeys(List.of(new RowSorter.SortKey(3, SortOrder.ASCENDING)));
                break;
            case "Price (Low-High)":
                sorter.setSortKeys(List.of(new RowSorter.SortKey(4, SortOrder.ASCENDING)));
                break;
        }
        sorter.sort();
    }

    private void refreshData(DefaultTableModel model) {
        loadMedicineData(model);
    }

    private int calculateTotalItems(DefaultTableModel model) {
        int total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            total += (int) model.getValueAt(i, 3); // Quantity column
        }
        return total;
    }

    private int countLowStock(DefaultTableModel model) {
        int count = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            if ("Low Stock".equals(model.getValueAt(i, 7))) {
                count++;
            }
        }
        return count;
    }

    private int countExpiringSoon(DefaultTableModel model) {
        int count = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            if ("Expiring Soon".equals(model.getValueAt(i, 7))) {
                count++;
            }
        }
        return count;
    }

    private int countOutOfStock(DefaultTableModel model) {
        int count = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            if ("Out of Stock".equals(model.getValueAt(i, 7))) {
                count++;
            }
        }
        return count;
    }

    private void loadMedicineData(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing data

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT *, " +
                             "CASE " +
                             "  WHEN quantity = 0 THEN 'Out of Stock' " +
                             "  WHEN quantity <= 10 THEN 'Low Stock' " +
                             "  WHEN DATE(expiry_date) <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 'Expiring Soon' " +
                             "  ELSE 'In Stock' " +
                             "END AS status " +
                             "FROM medicines")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("expiry_date"),
                        rs.getString("supplier"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading medicine data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    private void createDoctorPanel() {
        // Main panel setup
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = createPanelHeader("Doctors", "View and manage doctor information");
        panel.add(headerPanel, BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(BACKGROUND_COLOR);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(NORMAL_FONT);

        JTextField searchField = new JTextField(25);
        searchField.setFont(NORMAL_FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Solid blue search button
        JButton searchButton = new JButton("Search");
        searchButton.setFont(NORMAL_FONT);
        searchButton.setBackground(PRIMARY_COLOR);
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        searchButton.setFocusPainted(false);
        searchButton.setOpaque(true);
        searchButton.setBorderPainted(false);
        searchButton.setContentAreaFilled(true);
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Table model
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Name", "Phone", "Specialization", "Qualification"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                return String.class;
            }
        };

        // Table with perfect alignment
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }
                return c;
            }
        };

        // Column alignment setup
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Apply alignments
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);  // ID column // ID
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);  // Name
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Phone
        table.getColumnModel().getColumn(3).setCellRenderer(leftRenderer);  // Specialization
        table.getColumnModel().getColumn(4).setCellRenderer(leftRenderer);  // Qualification

        // Custom header renderer with bold and larger font
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(TITLE_FONT.deriveFont(Font.BOLD, 14f)); // Bold and 14px size
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBackground(PRIMARY_COLOR);
                label.setForeground(Color.WHITE);
                label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Add vertical padding
                return label;
            }
        });

        // Table styling
        table.setRowHeight(35);
        table.setFont(NORMAL_FONT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(225, 240, 255));
        table.setSelectionForeground(DARK_COLOR);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Search functionality
        ActionListener searchAction = e -> {
            String searchText = searchField.getText().trim();
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            if (searchText.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText, 1, 3, 4));
            }
        };

        searchButton.addActionListener(searchAction);
        searchField.addActionListener(searchAction);

        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);
        this.contentPanel.add(panel, "doctors");

        // Initial data load
        loadDoctorData(model, "");
    }



    private void createAppointmentPanel() {
        // Main panel setup
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = createPanelHeader("Appointment Management", "Manage patient appointments");
        panel.add(headerPanel, BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);

        // Control panel with filter and action buttons
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(BACKGROUND_COLOR);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(BACKGROUND_COLOR);

        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(NORMAL_FONT);

        String[] dateOptions = {"Today", "All Dates"};
        dateFilter = new JComboBox<>(dateOptions);
        dateFilter.setFont(NORMAL_FONT);

        JLabel filterLabel = new JLabel("Filter by Status:");
        filterLabel.setFont(NORMAL_FONT);

        statusFilter = new JComboBox<>(new String[]{ "Scheduled", "Completed", "Cancelled"});
        statusFilter.setFont(NORMAL_FONT);

        JButton filterButton = createSolidButton("Filter", PRIMARY_COLOR);
        filterButton.setToolTipText("Filter appointments by date and status");

        filterPanel.add(dateLabel);
        filterPanel.add(dateFilter);
        filterPanel.add(filterLabel);
        filterPanel.add(statusFilter);
        filterPanel.add(filterButton);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setBackground(BACKGROUND_COLOR);

        JButton updateButton = createSolidButton("Update Status", PRIMARY_COLOR);
        updateButton.setToolTipText("Update the status of selected appointment");

        JButton refreshButton = createSolidButton("Refresh", PRIMARY_COLOR);
        refreshButton.setToolTipText("Refresh the appointment list");

        actionPanel.add(updateButton);
        actionPanel.add(refreshButton);

        controlPanel.add(filterPanel, BorderLayout.WEST);
        controlPanel.add(actionPanel, BorderLayout.EAST);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Table model
        appointmentsModel = new DefaultTableModel(
                new Object[]{"ID", "Patient", "Doctor", "Date", "Time", "Status", "Description"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // ID column
                return String.class;
            }
        };

        // Create table with custom styling
        JTable table = new JTable(appointmentsModel);

        // Custom header renderer
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(TITLE_FONT.deriveFont(Font.BOLD, 14f));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBackground(PRIMARY_COLOR);
                label.setForeground(Color.WHITE);
                label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                return label;
            }
        });

        // Custom renderers
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Set alternate row colors
                if (!table.isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }

                // Handle column-specific alignment
                switch (column) {
                    case 0:  // ID column - right aligned (standard for numbers)
                        ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                        break;
                    case 3:  // Date column
                    case 4:  // Time column
                    case 5:  // Status column
                        ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                        break;
                    default: // All other columns (Patient, Doctor, Description)
                        ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                }

                return c;
            }
        });

        // Status column renderer with color coding
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String status = value.toString();
                if (status.equals("Completed")) {
                    c.setForeground(SUCCESS_COLOR);
                } else if (status.equals("Scheduled")) {
                    c.setForeground(INFO_COLOR);
                } else if (status.equals("Cancelled")) {
                    c.setForeground(DANGER_COLOR);
                }

                ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);

                if (!table.isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }

                return c;
            }
        });

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);  // ID column // ID
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);  // Name
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Phone
        table.getColumnModel().getColumn(3).setCellRenderer(leftRenderer);  // Specialization
        table.getColumnModel().getColumn(4).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(leftRenderer);

        // Table styling
        table.setRowHeight(35);
        table.setFont(NORMAL_FONT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(225, 240, 255));
        table.setSelectionForeground(DARK_COLOR);

        // Double-click to view details
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = table.getSelectedRow();
                    if (viewRow >= 0) {
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        int appointmentId = (Integer) appointmentsModel.getValueAt(modelRow, 0);
                        viewAppointmentDetails(appointmentId);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Button actions
        filterButton.addActionListener(e -> {
            String selectedDate = (String) dateFilter.getSelectedItem();
            String selectedStatus = (String) statusFilter.getSelectedItem();
            boolean showTodayOnly = "Today".equals(selectedDate);  // This should be false when "All Dates" is selected
            String statusFilterValue = "All".equals(selectedStatus) ? null : selectedStatus;
            loadAppointmentsData(showTodayOnly, statusFilterValue);
        });

        refreshButton.addActionListener(e -> {
            String selectedDate = (String) dateFilter.getSelectedItem();
            String selectedStatus = (String) statusFilter.getSelectedItem();
            boolean showTodayOnly = "Today".equals(selectedDate);
            String statusFilterValue = "All".equals(selectedStatus) ? null : selectedStatus;
            loadAppointmentsData(showTodayOnly, statusFilterValue);
        });

        updateButton.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                updateAppointmentStatus(
                        (Integer) appointmentsModel.getValueAt(modelRow, 0),
                        (String) appointmentsModel.getValueAt(modelRow, 5)
                );
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select an appointment to update",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        contentPanel.add(controlPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);
        this.contentPanel.add(panel, "appointments");

        // Initial data load - show today's appointments
        loadAppointmentsData(true, null);
    }

    private void loadAppointmentsData(boolean todayOnly, String statusFilter) {
        appointmentsModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT a.id, p.name AS patient_name, COALESCE(d.name, 'Unknown Doctor') AS doctor_name, ")
                    .append("a.date, a.time, a.status, a.description ")
                    .append("FROM appointments a ")
                    .append("LEFT JOIN patients p ON a.patient_id = p.id ")  // Changed to LEFT JOIN
                    .append("LEFT JOIN doctors d ON a.doctor_id = d.user_id ");  // Changed to LEFT JOIN

            // Add filters
            boolean whereAdded = false;

            // Add date filter for today only
            if (todayOnly) {
                sqlBuilder.append("WHERE a.date = CURDATE() ");
                whereAdded = true;
            }

            // Add status filter if provided
            if (statusFilter != null && !statusFilter.isEmpty()) {
                if (whereAdded) {
                    sqlBuilder.append("AND ");
                } else {
                    sqlBuilder.append("WHERE ");
                }
                sqlBuilder.append("a.status = ? ");
            }

            sqlBuilder.append("ORDER BY a.date, a.time");

            PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString());

            // Set status parameter if provided
            if (statusFilter != null && !statusFilter.isEmpty()) {
                stmt.setString(1, statusFilter);
            }

            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

            while (rs.next()) {
                appointmentsModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        dateFormat.format(rs.getDate("date")),
                        timeFormat.format(rs.getTime("time")),
                        rs.getString("status"),
                        rs.getString("description")
                });
            }

            // Add a message in the UI if no appointments are found
            if (appointmentsModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        todayOnly ?
                                "No appointments found for today" + (statusFilter != null ? " with status: " + statusFilter : "") :
                                "No appointments found" + (statusFilter != null ? " with status: " + statusFilter : ""),
                        "No Results",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            showError("Error loading appointment data: " + ex.getMessage());
        }
    }

    private void updateAppointmentStatus(int appointmentId, String currentStatus) {
        String[] statusOptions = {"Scheduled", "Completed", "Cancelled"};
        JComboBox<String> statusCombo = new JComboBox<>(statusOptions);
        statusCombo.setSelectedItem(currentStatus);

        JPanel messagePanel = new JPanel(new BorderLayout(0, 10));
        messagePanel.add(new JLabel("Select new status:"), BorderLayout.NORTH);
        messagePanel.add(statusCombo, BorderLayout.CENTER);

        int option = JOptionPane.showConfirmDialog(
                this,
                messagePanel,
                "Update Appointment Status",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String newStatus = (String) statusCombo.getSelectedItem();

            // Don't proceed if status didn't change
            if (newStatus.equals(currentStatus)) {
                JOptionPane.showMessageDialog(this,
                        "Status remains unchanged",
                        "No Change",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "UPDATE appointments SET status = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newStatus);
                stmt.setInt(2, appointmentId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Appointment status updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Simplify the refresh logic - just use the current filters
                    String selectedDate = (String) dateFilter.getSelectedItem();
                    String selectedStatus = (String) statusFilter.getSelectedItem();
                    boolean showTodayOnly = "Today".equals(selectedDate);
                    String statusFilterValue = "All".equals(selectedStatus) ? null : selectedStatus;
                    loadAppointmentsData(showTodayOnly, statusFilterValue);

                    // Default to showing today's appointments if we can't determine date filter
                     showTodayOnly = true;

                    // Refresh the data
                    loadAppointmentsData(showTodayOnly, statusFilterValue);
                } else {
                    showError("Failed to update appointment status");
                }
            } catch (SQLException ex) {
                showError("Error updating appointment status: " + ex.getMessage());
            }
        }
    }
    private void viewAppointmentDetails(int appointmentId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN doctors d ON a.doctor_id = d.user_id " +
                    "WHERE a.id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Create a styled details panel
                JPanel detailsPanel = new JPanel();
                detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
                detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                detailsPanel.setPreferredSize(new Dimension(400, 350));

                // Add title
                JLabel titleLabel = new JLabel("Appointment Details");
                titleLabel.setFont(new Font("Segoe UI ", Font.BOLD, 16));
                titleLabel.setForeground(PRIMARY_COLOR);
                titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                detailsPanel.add(titleLabel);

                detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

                // Create a grid for details
                JPanel gridPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                gridPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                // Add details to grid
                addDetailField(gridPanel, "ID:", String.valueOf(rs.getInt("id")));
                addDetailField(gridPanel, "Patient:", rs.getString("patient_name"));
                addDetailField(gridPanel, "Doctor:", rs.getString("doctor_name"));
                addDetailField(gridPanel, "Date:", new SimpleDateFormat("dd MMM yyyy").format(rs.getDate("date")));
                addDetailField(gridPanel, "Time:", new SimpleDateFormat("hh:mm a").format(rs.getTime("time")));

                // Status with color
                JLabel statusLabel = new JLabel("Status:");
                statusLabel.setFont(new Font("Segoe UI ", Font.BOLD, 12));
                gridPanel.add(statusLabel);

                JLabel statusValue = new JLabel(rs.getString("status"));
                statusValue.setFont(new Font("Segoe UI ", Font.PLAIN, 12));
                String status = rs.getString("status");
                if (status.equals("Completed")) {
                    statusValue.setForeground(SUCCESS_COLOR);
                } else if (status.equals("Scheduled")) {
                    statusValue.setForeground(INFO_COLOR);
                } else if (status.equals("Cancelled")) {
                    statusValue.setForeground(DANGER_COLOR);
                }
                gridPanel.add(statusValue);

                detailsPanel.add(gridPanel);

                detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

                // Description in a separate panel
                JLabel descLabel = new JLabel("Description:");
                descLabel.setFont(new Font("Segoe UI ", Font.BOLD, 12));
                descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                detailsPanel.add(descLabel);

                detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

                JTextArea descArea = new JTextArea(rs.getString("description"));
                descArea.setFont(new Font("Segoe UI ", Font.PLAIN, 12));
                descArea.setLineWrap(true);
                descArea.setWrapStyleWord(true);
                descArea.setEditable(false);
                descArea.setBackground(detailsPanel.getBackground());
                descArea.setAlignmentX(Component.LEFT_ALIGNMENT);

                JScrollPane descScroll = new JScrollPane(descArea);
                descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
                descScroll.setPreferredSize(new Dimension(380, 100));
                detailsPanel.add(descScroll);

                JOptionPane.showMessageDialog(this,
                        detailsPanel,
                        "Appointment Details",
                        JOptionPane.PLAIN_MESSAGE);
            }
        } catch (SQLException ex) {
            showError("Error retrieving appointment details: " + ex.getMessage());
        }
    }

    private JButton createSolidButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(NORMAL_FONT);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void addDetailField(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI ", Font.BOLD, 12));
        panel.add(labelComponent);

        JLabel valueComponent = new JLabel(value != null ? value : "");
        valueComponent.setFont(new Font("Segoe UI ", Font.PLAIN, 12));
        panel.add(valueComponent);
    }

    // Helper methods
    private JPanel createPanelHeader(String title, String subtitle) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(DARK_COLOR);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(SMALL_FONT);
        subtitleLabel.setForeground(DARK_COLOR);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        return headerPanel;
    }

    private String getTodaysAppointments() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Query to count all appointments for today's date
            String sql = "SELECT COUNT(*) AS total FROM appointments WHERE date = CURDATE()";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                if (rs.next()) {
                    return rs.getString("total");
                } else {
                    return "0";
                }
            }
        } catch (SQLException ex) {
            // Log the error for debugging
            System.err.println("Error fetching today's appointments: " + ex.getMessage());
            return "N/A";
        }
    }

    private String getActivePatients() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT COUNT(DISTINCT patient_id) FROM appointments WHERE date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            return rs.next() ? rs.getString(1) : "0";
        } catch (SQLException ex) {
            return "N/A";
        }
    }

    private String getLowStockMedicines() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT COUNT(*) FROM medicines WHERE quantity <= 10";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            return rs.next() ? rs.getString(1) : "0";
        } catch (SQLException ex) {
            return "N/A";
        }
    }

    private String getLastPunchTime() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT punch_time FROM time_punches WHERE user_id = ? ORDER BY punch_time DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
                return sdf.format(rs.getTimestamp("punch_time"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "No punch recorded";
    }

    private String getNextPunchType() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT punch_type FROM time_punches WHERE user_id = ? ORDER BY punch_time DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("punch_type").equals("IN") ? "OUT" : "IN";
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "IN"; // Default to IN if no previous records
    }

    private String getCurrentPunchStatus() {
        return getNextPunchType().equals("IN") ? "Not Punched In" : "Punched In";
    }

    private void recordTimePunch() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String punchType = getNextPunchType();
            String sql = "INSERT INTO time_punches (user_id, punch_time, punch_type) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, punchType);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Punched " + punchType + " successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            showError("Error recording punch: " + ex.getMessage());
        }
    }




    private void loadDoctorData(DefaultTableModel model, String searchQuery) {
        model.setRowCount(0); // Clear existing data

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, name, phone, specialization, qualification FROM doctors " +
                             "WHERE name LIKE ? OR specialization LIKE ? OR qualification LIKE ? " +
                             "ORDER BY name")) {

            String searchParam = "%" + searchQuery + "%";
            stmt.setString(1, searchParam);
            stmt.setString(2, searchParam);
            stmt.setString(3, searchParam);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        formatPhoneNumber(rs.getString("phone")), // Format phone number
                        rs.getString("specialization"),
                        rs.getString("qualification")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error loading doctor data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Helper method to format phone numbers
    private String formatPhoneNumber(String phone) {
        if (phone == null) return "";
        // Format +919876543210 as +91 98765 43210
        return phone.replaceFirst("(\\+\\d{2})(\\d{5})(\\d{5})", "$1 $2 $3");
    }





    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // Dispose of the current window
            dispose();
            System.out.println("User logged out");
            // Show the Connectpage
            SwingUtilities.invokeLater(() -> {
                ConnectPage connectPage = new ConnectPage(); // Instantiate the separate Connectpage class
                connectPage.setVisible(true); // Display the Connectpage
            });
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NurseDashboard("101"));
    }
}