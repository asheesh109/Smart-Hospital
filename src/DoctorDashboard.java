import jdk.internal.icu.text.UnicodeSet;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.Date;
import java.util.List;

public class DoctorDashboard extends JFrame {
    // Color palette
    private static final Color PRIMARY_COLOR = new Color(0, 120, 150);
    private static final Color SECONDARY_COLOR = new Color(200, 230, 240);
    private static final Color SUCCESS_COLOR = new Color(0, 100, 140);
    private static final Color DANGER_COLOR = new Color(230, 60, 80);
    private static final Color WARNING_COLOR = new Color(255, 180, 50);
    private static final Color INFO_COLOR = new Color(0, 160, 180);
    private static final Color LIGHT_COLOR = new Color(240, 242, 245);
    private static final Color DARK_COLOR = new Color(20, 40, 80);
    private static final Color BACKGROUND_COLOR = new Color(210, 235, 250);

    // Font configurations
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SUBHEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private static final Font TITLE_FONT = new Font("Montserrat", Font.BOLD, 16);
    private static final Font NORMAL_FONT = new Font("Montserrat", Font.PLAIN, 14);
    private static final Font SMALL_FONT = new Font("Montserrat", Font.PLAIN, 12);

    // Database connection
    private Connection connection;
    private String DB_URL = "jdbc:mysql://localhost:3306/HMsystem";
    private String DB_USER = "root";
    private String DB_PASS = "Ashish030406";

    // UI Components
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JPanel statusBarPanel;
    private CardLayout cardLayout;
    private JLabel doctorNameLabel;
    private JLabel dateTimeLabel;
    JTable reportTable;
    DefaultTableModel tableModel;
    private JButton button;
    private Map<String, String> doctorData;  // To store doctor information
    private int currentDoctorId;
    JSplitPane splitPane;
    String currentUserRole;
    Vector<Object> conversationListModel;
    UnicodeSet conversations;

    private JPanel messageListContent;
    private JLabel currentConversationLabel;
    private JPanel messagesDisplayPanel;
    private JTextArea messageInputField;
    private Timer refreshTimer;
    private boolean isRefreshing = false;
    private long lastRefreshTimestamp = 0;
    private Map<Integer, Long> conversationLastUpdated = new HashMap<>();


    private int currentOtherUserId = 0;
    int currentUserId;

    // Doctor information
    private int doctorId;
    private String doctorName;





    public String getDoctorName(int userId) {
        String doctorName = null;
        String query = "SELECT name FROM doctors WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                doctorName = rs.getString("name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return doctorName;
    }


    public DoctorDashboard(int logId) {
        this.doctorId=logId;
        currentUserId=doctorId;
        doctorName=getDoctorName(doctorId);


        setTitle("Hospital Management System - Doctor Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Database connection successful");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        createStatusBar();
        createSidebar();
        createContentPanel();


        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(statusBarPanel, BorderLayout.NORTH);

        showPanel("dashboard");
        startTimeUpdater();

        setVisible(true);
    }




    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(Color.white);
        sidebarPanel.setPreferredSize(new Dimension(220, getHeight()));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel logoLabel = new JLabel("Medicare");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logoLabel.setForeground(DARK_COLOR);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Doctor Portal");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(DARK_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.setMaximumSize(new Dimension(220, 100));

        JLabel profileImage = new JLabel();
        profileImage.setIcon(createCircularProfileIcon());
        profileImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        doctorNameLabel = new JLabel(doctorName);
        doctorNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        doctorNameLabel.setForeground(Color.WHITE);
        doctorNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(DARK_COLOR);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profilePanel.add(logoLabel);
        profilePanel.add(profileImage);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        profilePanel.add(doctorNameLabel);
        profilePanel.add(roleLabel);

        String[] buttonLabels = {"Dashboard", "Appointments", "Patients", "Messages", "Reports", "Settings"};
        String[] buttonIcons = {"dashboard", "calendar", "users", "message", "report", "settings"};

        sidebarPanel.add(logoLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebarPanel.add(subtitleLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        sidebarPanel.add(profilePanel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 40))); // Increased gap after profile section

        // Add regular buttons
        for (int i = 0; i < buttonLabels.length; i++) {
            JPanel buttonPanel = createSidebarButton(buttonLabels[i].toLowerCase(), buttonLabels[i], buttonIcons[i]);
            sidebarPanel.add(buttonPanel);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Add flexible space between regular buttons and logout button
        sidebarPanel.add(Box.createVerticalGlue());

        // Create logout button with different style
        JPanel logoutPanel = new JPanel();
        logoutPanel.setLayout(new BoxLayout(logoutPanel, BoxLayout.X_AXIS));
        logoutPanel.setBackground(WARNING_COLOR); // Different color for logout
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        logoutPanel.setMaximumSize(new Dimension(220, 40));
        logoutPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel logoutIconLabel = new JLabel("•");
        logoutIconLabel.setForeground(Color.WHITE);
        logoutIconLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel logoutTextLabel = new JLabel("Logout");
        logoutTextLabel.setForeground(Color.WHITE);
        logoutTextLabel.setFont(REGULAR_FONT);

        logoutPanel.add(logoutIconLabel);
        logoutPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        logoutPanel.add(logoutTextLabel);
        logoutPanel.add(Box.createHorizontalGlue());

        logoutPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPanel("logout");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                logoutPanel.setBackground(new Color(241, 199, 91)); // Darker red on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutPanel.setBackground(WARNING_COLOR);
            }
        });

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Space before logout button
        sidebarPanel.add(logoutPanel);
    }

    private JPanel createSidebarButton(String actionCommand, String labelText, String iconName) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(SUCCESS_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        buttonPanel.setMaximumSize(new Dimension(220, 40));
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel("•");
        iconLabel.setForeground(SECONDARY_COLOR);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel textLabel = new JLabel(labelText);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(REGULAR_FONT);

        buttonPanel.add(iconLabel);
        buttonPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        buttonPanel.add(textLabel);
        buttonPanel.add(Box.createHorizontalGlue());

        buttonPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPanel(actionCommand);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                buttonPanel.setBackground(PRIMARY_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                buttonPanel.setBackground(SUCCESS_COLOR);
            }
        });

        return buttonPanel;
    }

    private void createContentPanel() {
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(LIGHT_COLOR);

        JPanel dashboardPanel = createDashboardPanel();
        JPanel appointmentsPanel = createAppointmentPanel();
        JPanel patientsPanel = createPatientPanel();
        JPanel messagesPanel = createMessagesPanel();
        JPanel reportsPanel = createReportsPanel();
        JPanel settingsPanel = createSettingsPanel();
        JPanel logoutPanel = createLogoutPanel();

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(appointmentsPanel, "appointments");
        contentPanel.add(patientsPanel, "patients");
        contentPanel.add(messagesPanel, "messages");
        contentPanel.add(reportsPanel, "reports");
        contentPanel.add(settingsPanel, "settings");
        contentPanel.add(logoutPanel, "logout");
    }

    private JPanel createDashboardPanel() {
        // Create a main container panel
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(BACKGROUND_COLOR);

        // Main panel with modern flat design
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(25, 25));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(35, 35, 35, 35));

        // Enhanced Header Panel with personalized welcome message
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        // Create a more visually appealing welcome panel with doctor's information
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(BACKGROUND_COLOR);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Doctor's name with larger font and styling

        JLabel nameLabel = new JLabel(doctorName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        nameLabel.setForeground(new Color(33, 37, 41));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Welcome message with medium font
        JLabel welcomeLabel = new JLabel("Welcome back to your Medical Dashboard");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        welcomeLabel.setForeground(new Color(108, 117, 125));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Optional subtitle with smaller font
        JLabel subtitleLabel = new JLabel("Your clinic dashboard is ready with today's updates");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(108, 117, 125));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add a small vertical padding between components
        welcomePanel.add(nameLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 3)));
        welcomePanel.add(subtitleLabel);

        // Right-aligned date display
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        datePanel.setBackground(BACKGROUND_COLOR);

        JLabel dateLabel = new JLabel(new SimpleDateFormat("EEEE, MMMM dd, yyyy").format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dateLabel.setForeground(new Color(108, 117, 125));
        datePanel.add(dateLabel);

        headerPanel.add(welcomePanel, BorderLayout.WEST);
        headerPanel.add(datePanel, BorderLayout.EAST);

        // Stats Panel with enhanced modern cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(BACKGROUND_COLOR);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 25, 0));

        // Modern gradient stat card colors (start -> end)
        Color[][] statGradients = {
                {new Color(56, 128, 255), new Color(33, 150, 243)},   // Blue
                {new Color(46, 213, 115), new Color(37, 187, 135)},   // Green
                {new Color(255, 145, 0), new Color(255, 123, 0)},     // Orange
                {new Color(236, 64, 122), new Color(216, 27, 96)}     // Pink
        };

        // Custom icons would be better than default ones
        statsPanel.add(createModernStatCard("Today's Patients", String.valueOf(getTodaysPatientsCount()),
                statGradients[0][0], UIManager.getIcon("OptionPane.informationIcon")));
        statsPanel.add(createModernStatCard("Appointments", String.valueOf(getTodaysAppointmentsCount()),
                statGradients[1][0], UIManager.getIcon("OptionPane.questionIcon")));
        statsPanel.add(createModernStatCard("Pending Reports", String.valueOf(getPendingReportsCount()),
                statGradients[2][0], UIManager.getIcon("OptionPane.warningIcon")));
        

        // Main Content with enhanced rounded panels and shadows
        JPanel mainContent = new JPanel(new GridLayout(2, 1, 0, 25));
        mainContent.setBackground(BACKGROUND_COLOR);

        // Row 1 - Chart and Appointments
        JPanel row1 = new JPanel(new GridLayout(1, 2, 25, 0));
        row1.setBackground(BACKGROUND_COLOR);

        // Chart Panel with enhanced shadow
        JPanel chartPanel = createRoundedPanel(18, new Color(255, 255, 255));
        chartPanel.setLayout(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel chartTitle = new JLabel("Patient Statistics");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chartTitle.setForeground(new Color(33, 37, 41));
        chartTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel chartContent = new ModernBarChart();
        chartContent.setBackground(Color.WHITE);

        chartPanel.add(chartTitle, BorderLayout.NORTH);
        chartPanel.add(chartContent, BorderLayout.CENTER);

        // Appointments Panel with enhanced design
        JPanel appointmentsPanel = createRoundedPanel(18, new Color(255, 255, 255));
        appointmentsPanel.setLayout(new BorderLayout());
        appointmentsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel appointmentsTitle = new JLabel("Today's Appointments");
        appointmentsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appointmentsTitle.setForeground(new Color(33, 37, 41));
        appointmentsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel appointmentsList = new JPanel();
        appointmentsList.setLayout(new BoxLayout(appointmentsList, BoxLayout.Y_AXIS));
        appointmentsList.setBackground(Color.WHITE);

        // Load appointments from database
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.name, a.time, a.description " +
                             "FROM appointments a " +
                             "JOIN patients p ON a.patient_id = p.id " +
                             "WHERE a.date = CURDATE() " +
                             "ORDER BY a.time");
             ResultSet rs = stmt.executeQuery()) {

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

            while (rs.next()) {
                String name = rs.getString("name");
                Time time = rs.getTime("time");
                String formattedTime = timeFormat.format(time);
                String description = rs.getString("description");
                appointmentsList.add(createModernAppointmentItem(name, formattedTime, description));
                appointmentsList.add(Box.createRigidArea(new Dimension(0, 12)));
            }

            if (appointmentsList.getComponentCount() == 0) {
                JLabel noAppointments = new JLabel("No appointments scheduled for today");
                noAppointments.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                noAppointments.setForeground(new Color(108, 117, 125));
                noAppointments.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
                appointmentsList.add(noAppointments);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading appointments");
            errorLabel.setForeground(new Color(220, 53, 69));
            appointmentsList.add(errorLabel);
        }

        JScrollPane appointmentsScrollPane = new JScrollPane(appointmentsList);
        appointmentsScrollPane.setBorder(null);
        appointmentsScrollPane.getViewport().setBackground(Color.WHITE);

        appointmentsPanel.add(appointmentsTitle, BorderLayout.NORTH);
        appointmentsPanel.add(appointmentsScrollPane, BorderLayout.CENTER);

        row1.add(chartPanel);
        row1.add(appointmentsPanel);

        // Row 2 - Patients and Pending Reports
        JPanel row2 = new JPanel(new GridLayout(1, 2, 25, 0));
        row2.setBackground(BACKGROUND_COLOR);

        // Recent Patients Panel with modern styling
        JPanel patientsPanel = createRoundedPanel(18, new Color(255, 255, 255));
        patientsPanel.setLayout(new BorderLayout());
        patientsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel patientsTitle = new JLabel("Recent Patients");
        patientsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        patientsTitle.setForeground(new Color(33, 37, 41));
        patientsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        String[] columnNames = {"Patient Name", "Blood Group", "Age", "Gender"};
        Object[][] patientsData = getRecentPatientsData();

        DefaultTableModel patientsModel = new DefaultTableModel(patientsData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable patientsTable = new JTable(patientsModel);
        patientsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        patientsTable.setRowHeight(45);
        patientsTable.setShowVerticalLines(false);
        patientsTable.setGridColor(new Color(240, 240, 240));
        patientsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        patientsTable.getTableHeader().setBackground(new Color(248, 249, 250));
        patientsTable.getTableHeader().setForeground(new Color(73, 80, 87));
        patientsTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        patientsTable.setSelectionBackground(new Color(236, 242, 248));
        patientsTable.setSelectionForeground(new Color(33, 37, 41));

        // Custom renderer for all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(236, 242, 248));
                    c.setForeground(new Color(33, 37, 41));
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                    c.setForeground(new Color(73, 80, 87));
                }
                ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                return c;
            }
        };

        // Apply center alignment to all columns
        for (int i = 0; i < patientsTable.getColumnCount(); i++) {
            patientsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane patientsScrollPane = new JScrollPane(patientsTable);
        patientsScrollPane.setBorder(null);
        patientsScrollPane.getViewport().setBackground(Color.WHITE);

        patientsPanel.add(patientsTitle, BorderLayout.NORTH);
        patientsPanel.add(patientsScrollPane, BorderLayout.CENTER);

        // Pending Reports Panel with modern styling
        JPanel reportsPanel = createRoundedPanel(18, new Color(255, 255, 255));
        reportsPanel.setLayout(new BorderLayout());
        reportsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel reportsTitle = new JLabel("Pending Reports");
        reportsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        reportsTitle.setForeground(new Color(33, 37, 41));
        reportsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel reportsList = new JPanel();
        reportsList.setLayout(new BoxLayout(reportsList, BoxLayout.Y_AXIS));
        reportsList.setBackground(Color.WHITE);

        // Load pending reports from database
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT r.report_id, p.name AS patient_name, r.report_type, r.report_date, " +
                             "r.description, r.uploaded_by, r.file_name, r.file_size " +
                             "FROM reports r " +
                             "JOIN patients p ON r.patient_id = p.id " +
                             "WHERE r.status = 'pending' " +
                             "ORDER BY r.report_date DESC LIMIT 4");
             ResultSet rs = stmt.executeQuery()) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

            while (rs.next()) {
                int reportId = rs.getInt("report_id");
                String patientName = rs.getString("patient_name");
                String reportType = rs.getString("report_type");
                Date reportDate = rs.getDate("report_date");
                String formattedDate = dateFormat.format(reportDate);
                String description = rs.getString("description");
                String uploadedBy = rs.getString("uploaded_by");
                String fileName = rs.getString("file_name");
                int fileSize = rs.getInt("file_size");

                reportsList.add(createModernReportItem(reportId, patientName, reportType, formattedDate,
                        description, uploadedBy, fileName, fileSize));
                reportsList.add(Box.createRigidArea(new Dimension(0, 12)));
            }

            if (reportsList.getComponentCount() == 0) {
                JLabel noReports = new JLabel("No pending reports");
                noReports.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                noReports.setForeground(new Color(108, 117, 125));
                noReports.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
                reportsList.add(noReports);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading reports");
            errorLabel.setForeground(new Color(220, 53, 69));
            reportsList.add(errorLabel);
        }

        JScrollPane reportsScrollPane = new JScrollPane(reportsList);
        reportsScrollPane.setBorder(null);
        reportsScrollPane.getViewport().setBackground(Color.WHITE);

        reportsPanel.add(reportsTitle, BorderLayout.NORTH);
        reportsPanel.add(reportsScrollPane, BorderLayout.CENTER);

        row2.add(patientsPanel);
        row2.add(reportsPanel);

        mainContent.add(row1);
        mainContent.add(row2);

        // Assemble components in the panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        panel.add(mainContent, BorderLayout.SOUTH);

        // Create a scroll pane for the entire dashboard
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        // Add the scroll pane to the container panel
        containerPanel.add(scrollPane, BorderLayout.CENTER);

        return containerPanel;
    }

    // Helper method to create rounded panels with shadow
    private JPanel createRoundedPanel(int radius, Color background) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                int shadowSize = 5;
                g2.setColor(new Color(0, 0, 0, 20));
                for (int i = 0; i < shadowSize; i++) {
                    g2.fillRoundRect(i, i, getWidth() - 2 * i, getHeight() - 2 * i, radius, radius);
                }

                // Draw panel
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth() - shadowSize, getHeight() - shadowSize, radius, radius);

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    // Modern stat card with icon and better styling
    private JPanel createModernStatCard(String title, String value, Color color, Icon icon) {
        JPanel cardPanel = createRoundedPanel(15, Color.WHITE);
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // Icon with colored background
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        iconPanel.setBackground(Color.WHITE);
        iconPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        iconLabel.setForeground(color);

        iconPanel.add(iconLabel);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(new Color(51, 51, 51));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(119, 119, 119));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(iconPanel);
        contentPanel.add(valueLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(titleLabel);

        cardPanel.add(contentPanel, BorderLayout.CENTER);

        return cardPanel;
    }

    // Modern bar chart implementation
    class ModernBarChart extends JPanel {
        private final int[] data = {12, 15, 10, 18, 14, 9, 13}; // Sample data
        private final String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"};

        public ModernBarChart() {
            setPreferredSize(new Dimension(400, 250));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw background
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            int maxValue = Arrays.stream(data).max().getAsInt();
            int barWidth = 40;
            int spacing = 20;
            int chartHeight = getHeight() - 80;
            int chartWidth = getWidth() - 80;
            int startX = 50;
            int startY = 30;

            // Draw grid lines
            g2.setColor(new Color(238, 238, 238));
            for (int i = 0; i <= 5; i++) {
                int y = startY + (i * chartHeight / 5);
                g2.drawLine(startX, y, startX + chartWidth, y);

                // Draw Y-axis labels
                g2.setColor(new Color(153, 153, 153));
                g2.drawString(String.valueOf(maxValue - (i * maxValue / 5)), startX - 30, y + 5);
                g2.setColor(new Color(238, 238, 238));
            }

            // Draw bars with gradient
            for (int i = 0; i < data.length; i++) {
                int barHeight = (int) ((data[i] / (double) maxValue) * chartHeight);
                int x = startX + (barWidth + spacing) * i;
                int y = startY + (chartHeight - barHeight);

                // Create gradient for bars
                GradientPaint gradient = new GradientPaint(x, y, new Color(70, 130, 180),
                        x, y + barHeight, new Color(100, 160, 210));
                g2.setPaint(gradient);
                g2.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

                // Draw value above bar
                g2.setColor(new Color(51, 51, 51));
                g2.drawString(String.valueOf(data[i]), x + barWidth/2 - 5, y - 5);
            }

            // Draw X-axis labels
            g2.setColor(new Color(119, 119, 119));
            for (int i = 0; i < months.length; i++) {
                int x = startX + (barWidth + spacing) * i + barWidth/2;
                g2.drawString(months[i], x - 10, startY + chartHeight + 20);
            }

            // Draw title
            g2.setColor(new Color(51, 51, 51));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString("Monthly Patient Visits", startX, 20);
        }
    }

    // Modern appointment item
    private JPanel createModernAppointmentItem(String name, String time, String type) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(238, 238, 238)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        nameLabel.setForeground(new Color(51, 51, 51));

        JLabel typeLabel = new JLabel(type);
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeLabel.setForeground(new Color(119, 119, 119));

        leftPanel.add(nameLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(typeLabel);

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        timeLabel.setForeground(new Color(70, 130, 180));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        panel.add(leftPanel);
        panel.add(Box.createHorizontalGlue());
        panel.add(timeLabel);

        return panel;
    }

    // Modern report item
    private JPanel createModernReportItem(int reportId, String patientName, String reportType,
                                          String reportDate, String description, String uploadedBy,
                                          String fileName, int fileSize) {
        JPanel item = new JPanel();
        item.setLayout(new BorderLayout(10, 0));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(238, 238, 238)));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(3, 1, 0, 5));
        infoPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(patientName + " • " + reportType);
        nameLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        nameLabel.setForeground(new Color(51, 51, 51));

        JLabel detailsLabel = new JLabel("Uploaded on " + reportDate + " by " + uploadedBy);
        detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailsLabel.setForeground(new Color(119, 119, 119));

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descriptionLabel.setForeground(new Color(119, 119, 119));

        infoPanel.add(nameLabel);
        infoPanel.add(detailsLabel);
        infoPanel.add(descriptionLabel);

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filePanel.setBackground(Color.WHITE);

        JLabel fileIcon = new JLabel(UIManager.getIcon("FileView.fileIcon"));
        fileIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        JLabel fileLabel = new JLabel(fileName);
        fileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fileLabel.setForeground(new Color(119, 119, 119));

        // Format file size nicely (KB)
        String fileSizeText = String.format("%.1f KB", fileSize / 1024.0);
        JLabel sizeLabel = new JLabel("• " + fileSizeText);
        sizeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sizeLabel.setForeground(new Color(119, 119, 119));

        filePanel.add(fileIcon);
        filePanel.add(fileLabel);
        filePanel.add(sizeLabel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.WHITE);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(filePanel, BorderLayout.NORTH);
        rightPanel.add(actionPanel, BorderLayout.SOUTH);

        item.add(infoPanel, BorderLayout.CENTER);
        item.add(rightPanel, BorderLayout.EAST);

        return item;
    }

    // Helper method to create modern buttons
    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    // Helper method to get doctor's name (you'll need to implement this)
    private String getDoctorName() {
        // Implement this to return the logged-in doctor's name
        return "Priya Sharma"; // Example
    }

    private void selectComboItemByName(JComboBox<String> comboBox, String name) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String item = comboBox.getItemAt(i);
            if (item.contains(name)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }






    // Method to refresh the dashboard
    private void refreshDashboard() {
        // Remove the current panel
        Container parent = this.getParent();
        parent.remove(this);

        // Add a new dashboard panel
        JPanel newDashboard = createDashboardPanel();
        parent.add(newDashboard);
        parent.revalidate();
        parent.repaint();
    }


    // Method to handle view lab result action
    private void viewLabResult(String patientName, String testType) {
        // Implement the action to view the lab result details
        JOptionPane.showMessageDialog(
                this,
                "Viewing lab result for " + patientName + "\nTest: " + testType,
                "Lab Result Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1, true));

        JPanel colorStrip = new JPanel();
        colorStrip.setPreferredSize(new Dimension(5, 0));
        colorStrip.setBackground(color);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(DARK_COLOR);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(REGULAR_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(valueLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(titleLabel);

        cardPanel.add(colorStrip, BorderLayout.WEST);
        cardPanel.add(contentPanel, BorderLayout.CENTER);

        return cardPanel;
    }

    // Simple custom bar chart replacement for JFreeChart
    class SimpleBarChart extends JPanel {
        private final int[] data = {12, 15, 10, 18}; // Sample data
        private final String[] months = {"Jan", "Feb", "Mar", "Apr"};

        public SimpleBarChart() {
            setPreferredSize(new Dimension(400, 200));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            int maxValue = Arrays.stream(data).max().getAsInt();
            int barWidth = 60;
            int spacing = 20;
            int chartHeight = getHeight() - 40;

            g2.setColor(PRIMARY_COLOR);
            for (int i = 0; i < data.length; i++) {
                int barHeight = (int) ((data[i] / (double) maxValue) * chartHeight);
                int x = 50 + (barWidth + spacing) * i;
                int y = getHeight() - barHeight - 20;

                g2.fillRect(x, y, barWidth, barHeight);

                g2.setColor(DARK_COLOR);
                g2.drawString(months[i], x + barWidth/2 - 10, getHeight() - 5);
                g2.drawString(String.valueOf(data[i]), x + barWidth/2 - 5, y - 5);
                g2.setColor(PRIMARY_COLOR);
            }

            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(40, 20, 40, getHeight() - 20); // Y-axis
            g2.drawLine(40, getHeight() - 20, getWidth() - 20, getHeight() - 20); // X-axis
        }
    }

    private JPanel createAppointmentItem(String name, String time, String type) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, SECONDARY_COLOR),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(DARK_COLOR);

        JLabel typeLabel = new JLabel(type);
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeLabel.setForeground(PRIMARY_COLOR);

        leftPanel.add(nameLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(typeLabel);

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        timeLabel.setForeground(INFO_COLOR);

        panel.add(leftPanel);
        panel.add(Box.createHorizontalGlue());
        panel.add(timeLabel);

        return panel;
    }


    // Database methods
    private int getTodaysPatientsCount() {
        String query = "SELECT COUNT(DISTINCT patient_id) FROM appointments WHERE date = CURDATE()";
        // Using DISTINCT to count unique patients rather than all appointments
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int getTodaysAppointmentsCount() {
        String query = "SELECT COUNT(*) FROM appointments WHERE date = CURDATE() AND status = 'Scheduled'";
        // Perfect - clearly counts scheduled appointments for today
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int getPendingReportsCount() {
        String query = "SELECT COUNT(*) FROM reports WHERE status = 'Pending'";
        // If you want only recent pending reports, add:
        // "AND created_date > DATE_SUB(CURDATE(), INTERVAL 30 DAY)"
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int getNewMessagesCount() {
        try {
            // First check if the messages table exists
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "messages", null);

            if (!tables.next()) {
                // Table doesn't exist, return 0
                return 0;
            }

            // Table exists, proceed with query
            String query = "SELECT COUNT(*) FROM messages WHERE status = 'Unread' AND receiver_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, doctorId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0; // Return 0 if there's any error
        }
    }



    private Object[][] getRecentPatientsData() {
        String query = "SELECT p.name, p.blood_group, p.dob, p.gender " +
                "FROM patients p " +
                "ORDER BY p.id DESC LIMIT 5"; // or ORDER BY p.registration_date DESC

        List<Object[]> dataList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String dobStr = rs.getString("dob"); // Assuming DOB is in YYYY-MM-DD format
                int age = calculateAge(dobStr); // Calculate age from DOB

                Object[] row = {
                        rs.getString("name"),         // Patient name
                        rs.getString("blood_group"),  // Blood group
                        age,                        // Age (instead of DOB)
                        rs.getString("gender")       // Gender
                };
                dataList.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataList.toArray(new Object[0][]);
    }

    // Helper method to calculate age from DOB (String format: "YYYY-MM-DD")
    private int calculateAge(String dobStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dob = sdf.parse(dobStr);

            Calendar dobCal = Calendar.getInstance();
            dobCal.setTime(dob);

            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);

            // Adjust age if birthday hasn't occurred yet this year
            if (today.get(Calendar.MONTH) < dobCal.get(Calendar.MONTH) ||
                    (today.get(Calendar.MONTH) == dobCal.get(Calendar.MONTH) &&
                            today.get(Calendar.DAY_OF_MONTH) < dobCal.get(Calendar.DAY_OF_MONTH))) {
                age--;
            }

            return age;
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Return -1 if DOB parsing fails
        }
    }



    // Helper classes


    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void loadComboBox(JComboBox<String> comboBox, String query) throws SQLException {
        comboBox.removeAllItems();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                comboBox.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        }
    }



    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220)),
                        title,
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 14),
                        new Color(41, 128, 185)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return panel;
    }

    private JPanel createLabelPanel(String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
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

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(120, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }



    private JPanel createAppointmentPanel() {
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

        JLabel headerLabel = new JLabel("Appointment Management");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerLabel.setForeground(DARK_COLOR);
        titlePanel.add(headerLabel, BorderLayout.CENTER);

        // Right side search panel with period filter
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(BACKGROUND_COLOR);

        // Time period filter combo - Updated options
        JComboBox<String> periodCombo = new JComboBox<>(new String[]{"All", "Daily", "Weekly", "Monthly", "Calendar View"});
        styleComboBox(periodCombo);
        periodCombo.setPreferredSize(new Dimension(120, 35));

        JTextField searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(searchField.getPreferredSize().width, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search appointments...");

        JButton searchButton = createModernButton("Search", new Color(7, 86, 154), Color.WHITE);

        searchPanel.add(new JLabel("View:"));
        searchPanel.add(periodCombo);
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
        model.setColumnIdentifiers(new String[]{"ID", "Patient", "Doctor", "Date", "Time", "Status", "Description"});

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

        // Specialized renderer for colored status badges
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                if (column == 5) { // Status column
                    label.setOpaque(true);
                    String status = value.toString();

                    if (status.equals("Scheduled")) {
                        label.setBackground(new Color(230, 240, 255));
                        label.setForeground(new Color(0, 100, 200));
                    } else if (status.equals("Completed")) {
                        label.setBackground(new Color(230, 255, 230));
                        label.setForeground(new Color(0, 150, 0));
                    } else if (status.equals("Cancelled")) {
                        label.setBackground(new Color(255, 230, 230));
                        label.setForeground(new Color(200, 0, 0));
                    } else if (status.equals("No-Show")) {
                        label.setBackground(new Color(255, 240, 230));
                        label.setForeground(new Color(200, 100, 0));
                    }

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
            if (i == 5) { // Status column
                table.getColumnModel().getColumn(i).setCellRenderer(statusRenderer);
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
        JLabel emptyLabel = new JLabel("No appointments found", JLabel.CENTER);
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

        JLabel totalAppointmentsLabel = new JLabel("Total Appointments: 0");
        totalAppointmentsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalAppointmentsLabel.setForeground(DARK_COLOR);

        statsPanel.add(totalAppointmentsLabel);

        // Action buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(BACKGROUND_COLOR);

        // Modern styled buttons
        JButton addButton = createModernButton("Add Appointment", new Color(76, 175, 80), Color.WHITE);
        JButton editButton = createModernButton("Edit", new Color(255, 152, 0), Color.WHITE);
        JButton deleteButton = createModernButton("Delete", new Color(244, 67, 54), Color.WHITE);
        JButton refreshButton = createModernButton("Refresh", new Color(0, 120, 215), Color.WHITE);
        JButton doctorViewButton = createModernButton("My Appointments", new Color(102, 51, 153), Color.WHITE);
        JButton calendarViewButton = createModernButton("Calendar View", new Color(155, 89, 182), Color.WHITE);

        // Button actions
        addButton.addActionListener(e -> showAppointmentDialog(null, model));
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Vector<Object> rowData = new Vector<>();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    rowData.add(model.getValueAt(selectedRow, i));
                }
                showAppointmentDialog(rowData, model);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select an appointment to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (Integer) model.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(panel,
                        "Are you sure you want to delete this appointment?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        PreparedStatement stmt = connection.prepareStatement("DELETE FROM appointments WHERE id = ?");
                        stmt.setInt(1, id);
                        int result = stmt.executeUpdate();

                        if (result > 0) {
                            model.removeRow(selectedRow);
                            totalAppointmentsLabel.setText("Total Appointments: " + model.getRowCount());
                            JOptionPane.showMessageDialog(panel, "Appointment deleted successfully");
                        }
                    } catch (SQLException ex) {
                        showError("Failed to delete appointment: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select an appointment to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshButton.addActionListener(e -> {
            try {
                refreshAppointmentTable(model, (String) periodCombo.getSelectedItem());
                totalAppointmentsLabel.setText("Total Appointments: " + model.getRowCount());
            } catch (Exception ex) {
                showError("Failed to refresh appointments: " + ex.getMessage());
            }
        });

        doctorViewButton.addActionListener(e -> {
            // Show dialog to select doctor
            JComboBox<String> doctorCombo = new JComboBox<>();
            styleComboBox(doctorCombo);
            try {
                loadComboBox(doctorCombo, "SELECT id, name FROM doctors ORDER BY name");
            } catch (SQLException ex) {
                showError("Failed to load doctors: " + ex.getMessage());
            }

            int result = JOptionPane.showConfirmDialog(panel, doctorCombo, "Select Your Doctor Profile",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                if (doctorCombo.getSelectedItem() != null) {
                    String doctor = doctorCombo.getSelectedItem().toString();
                    int doctorId = Integer.parseInt(doctor.split(" - ")[0]);
                    toggleDoctorView(doctorId);
                    refreshAppointmentTable(model, (String) periodCombo.getSelectedItem());
                }
            }
        });

        calendarViewButton.addActionListener(e -> {
            showCalendarView();
        });

        // Add buttons to panel
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(addButton);
        buttonsPanel.add(doctorViewButton);
        buttonsPanel.add(calendarViewButton);

        // Add components to action panel
        actionPanel.add(statsPanel, BorderLayout.WEST);
        actionPanel.add(buttonsPanel, BorderLayout.EAST);

        // Load initial data
        refreshAppointmentTable(model, "All");
        totalAppointmentsLabel.setText("Total Appointments: " + model.getRowCount());

        // Add search functionality
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            String period = (String) periodCombo.getSelectedItem();

            if (period.equals("Calendar View")) {
                showCalendarView();
                return;
            }

            try {
                String baseQuery = "SELECT a.id, p.name AS patient, d.name AS doctor, " +
                        "DATE_FORMAT(a.date, '%Y-%m-%d') AS date, " +
                        "TIME_FORMAT(a.time, '%H:%i') AS time, a.status, a.description " +
                        "FROM appointments a " +
                        "LEFT JOIN patients p ON a.patient_id = p.id " +
                        "LEFT JOIN doctors d ON a.doctor_id = d.id " +
                        "WHERE (p.name LIKE ? OR d.name LIKE ? OR a.date LIKE ? OR a.status LIKE ? " +
                        "OR a.description LIKE ?) ";

                // Add period filter condition
                if (!period.equals("All")) {
                    baseQuery += "AND " + getPeriodCondition(period) + " ";
                }

                baseQuery += "ORDER BY a.date DESC, a.time DESC LIMIT 100";

                PreparedStatement stmt = connection.prepareStatement(baseQuery);
                String pattern = "%" + searchTerm + "%";
                stmt.setString(1, pattern);
                stmt.setString(2, pattern);
                stmt.setString(3, pattern);
                stmt.setString(4, pattern);
                stmt.setString(5, pattern);

                ResultSet rs = stmt.executeQuery();
                model.setRowCount(0); // Clear existing data

                while (rs.next()) {
                    String doctorName = rs.getString("doctor");
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("patient"),
                            doctorName != null ? doctorName : "Not Assigned",
                            rs.getString("date"),
                            rs.getString("time"),
                            rs.getString("status"),
                            rs.getString("description")
                    });
                }

                totalAppointmentsLabel.setText("Total Appointments: " + model.getRowCount());
            } catch (SQLException ex) {
                showError("Failed to search appointments: " + ex.getMessage());
            }
        });

        // Add period filter functionality
        periodCombo.addActionListener(e -> {
            String period = (String) periodCombo.getSelectedItem();
            if (period.equals("Calendar View")) {
                showCalendarView();
            } else {
                refreshAppointmentTable(model, period);
                totalAppointmentsLabel.setText("Total Appointments: " + model.getRowCount());
            }
        });

        // Add all components to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tableContainer, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Helper method to get SQL condition for period filter
    private String getPeriodCondition(String period) {
        String condition = "";

        // Add period condition
        switch (period) {
            case "Daily":
                condition = "a.date = CURDATE()";
                break;
            case "Weekly":
                condition = "YEARWEEK(a.date, 1) = YEARWEEK(CURDATE(), 1)";
                break;
            case "Monthly":
                condition = "MONTH(a.date) = MONTH(CURDATE()) AND YEAR(a.date) = YEAR(CURDATE())";
                break;
            default:
                condition = "1=1"; // Returns all records
        }

        // Add doctor filter if in doctor view mode
        if (doctorViewMode && currentDoctorId != -1) {
            condition += " AND a.doctor_id = " + currentDoctorId;
        }

        return condition;
    }

    // Modified refreshAppointmentTable to include period filter
    private void refreshAppointmentTable(DefaultTableModel model, String period) {
        model.setRowCount(0);
        try {
            String query = "SELECT a.id, p.name AS patient, d.name AS doctor, " +
                    "DATE_FORMAT(a.date, '%Y-%m-%d') AS date, " +
                    "TIME_FORMAT(a.time, '%H:%i') AS time, a.status, a.description " +
                    "FROM appointments a " +
                    "LEFT JOIN patients p ON a.patient_id = p.id " +
                    "LEFT JOIN doctors d ON a.doctor_id = d.id ";

            if (!period.equals("All")) {
                query += "WHERE " + getPeriodCondition(period) + " ";
            }

            query += "ORDER BY a.date DESC, a.time DESC LIMIT 100";

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    String doctorName = rs.getString("doctor");
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("patient"),
                            doctorName != null ? doctorName : "Not Assigned",
                            rs.getString("date"),
                            rs.getString("time"),
                            rs.getString("status"),
                            rs.getString("description")
                    });
                }
            }
        } catch (SQLException e) {
            showError("Failed to load appointments: " + e.getMessage());
        }
    }

    // Doctor view mode toggle
    private boolean doctorViewMode = false;


    private void toggleDoctorView(int doctorId) {
        doctorViewMode = (doctorId != -1);
        currentDoctorId = doctorId;
    }

    // Calendar view implementation
    private void showCalendarView() {
        JDialog calendarDialog = new JDialog(this, "Calendar View", false);
        calendarDialog.setSize(1000, 700);
        calendarDialog.setLocationRelativeTo(this);
        calendarDialog.getContentPane().setBackground(new Color(245, 245, 245));

        // Create modern navigation panel
        JPanel navPanel = new JPanel(new BorderLayout(10, 0));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        navPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Navigation buttons with modern style
        JButton prevMonth = createModernButton("<", new Color(0, 120, 215), Color.WHITE);
        prevMonth.setPreferredSize(new Dimension(40, 40));
        JButton nextMonth = createModernButton(">", new Color(0, 120, 215), Color.WHITE);
        nextMonth.setPreferredSize(new Dimension(40, 40));

        // Month label with modern typography
        JLabel monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        monthLabel.setForeground(DARK_COLOR);

        navPanel.add(prevMonth, BorderLayout.WEST);
        navPanel.add(monthLabel, BorderLayout.CENTER);
        navPanel.add(nextMonth, BorderLayout.EAST);

        // Create calendar grid with card-like appearance
        JPanel calendarGrid = new JPanel(new GridLayout(0, 7, 8, 8));
        calendarGrid.setBackground(new Color(245, 245, 245));
        calendarGrid.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Initialize with current month
        Calendar calendar = Calendar.getInstance();
        updateCalendarView(calendar, monthLabel, calendarGrid);

        // Navigation listeners
        prevMonth.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendarView(calendar, monthLabel, calendarGrid);
        });

        nextMonth.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendarView(calendar, monthLabel, calendarGrid);
        });

        calendarDialog.add(navPanel, BorderLayout.NORTH);
        calendarDialog.add(new JScrollPane(calendarGrid,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
            {
                setBorder(BorderFactory.createEmptyBorder());
                getViewport().setBackground(new Color(245, 245, 245));
            }
        }, BorderLayout.CENTER);
        calendarDialog.setVisible(true);
    }

    private void updateCalendarView(Calendar calendar, JLabel monthLabel, JPanel calendarGrid) {
        calendarGrid.removeAll();

        // Set month label
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy");
        monthLabel.setText(monthFormat.format(calendar.getTime()));

        // Get first day of month and days in month
        Calendar tempCal = (Calendar) calendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add day headers with modern styling
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayNames) {
            JLabel header = new JLabel(day, JLabel.CENTER);
            header.setFont(new Font("Segoe UI", Font.BOLD, 14));
            header.setForeground(new Color(100, 100, 100));
            header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            calendarGrid.add(header);
        }

        // Add empty cells for days before first day of month
        for (int i = 1; i < firstDayOfWeek; i++) {
            calendarGrid.add(new JPanel());
        }

        // Add day cells with modern card-like design
        for (int day = 1; day <= daysInMonth; day++) {
            final int currentDay = day;
            JPanel dayPanel = new JPanel(new BorderLayout());
            dayPanel.setBackground(Color.WHITE);
            dayPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230)),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
            dayPanel.setPreferredSize(new Dimension(120, 120));

            // Add hover effect
            dayPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    dayPanel.setBackground(new Color(245, 245, 245));
                    dayPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    Calendar today = Calendar.getInstance();
                    if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                            currentDay == today.get(Calendar.DAY_OF_MONTH)) {
                        dayPanel.setBackground(new Color(230, 240, 255));
                    } else {
                        dayPanel.setBackground(Color.WHITE);
                    }
                    dayPanel.setCursor(Cursor.getDefaultCursor());
                }
            });

            // Day number label
            JLabel dayLabel = new JLabel(String.valueOf(day), JLabel.RIGHT);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            dayLabel.setForeground(DARK_COLOR);
            dayPanel.add(dayLabel, BorderLayout.NORTH);

            // Appointments list with modern styling
            JPanel appointmentsPanel = new JPanel();
            appointmentsPanel.setLayout(new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS));
            appointmentsPanel.setBackground(Color.WHITE);

            // Get appointments for this day
            tempCal.set(Calendar.DAY_OF_MONTH, day);
            Date dayDate = tempCal.getTime();
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = dbFormat.format(dayDate);

            try {
                String query = "SELECT TIME_FORMAT(time, '%H:%i') as time, p.name as patient, a.status " +
                        "FROM appointments a " +
                        "LEFT JOIN patients p ON a.patient_id = p.id " +
                        "WHERE date = ? ";

                if (doctorViewMode && currentDoctorId != -1) {
                    query += "AND a.doctor_id = " + currentDoctorId;
                }

                query += "ORDER BY time LIMIT 3"; // Limit to 3 appointments for display

                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, dateStr);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    JPanel apptPanel = new JPanel(new BorderLayout());
                    apptPanel.setBackground(getStatusColor(rs.getString("status")));
                    apptPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                    apptPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

                    JLabel apptLabel = new JLabel(rs.getString("time") + " - " + rs.getString("patient"));
                    apptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    apptLabel.setForeground(new Color(60, 60, 60));
                    apptPanel.add(apptLabel, BorderLayout.CENTER);

                    appointmentsPanel.add(apptPanel);
                    appointmentsPanel.add(Box.createVerticalStrut(4));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(10);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            dayPanel.add(scrollPane, BorderLayout.CENTER);

            // Highlight current day
            Calendar today = Calendar.getInstance();
            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    day == today.get(Calendar.DAY_OF_MONTH)) {
                dayPanel.setBackground(new Color(230, 240, 255));
                dayPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 120, 215)),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));
            }

            calendarGrid.add(dayPanel);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    // Helper method to get status color
    private Color getStatusColor(String status) {
        switch (status) {
            case "Scheduled":
                return new Color(230, 240, 255);
            case "Completed":
                return new Color(230, 255, 230);
            case "Cancelled":
                return new Color(255, 230, 230);
            case "No-Show":
                return new Color(255, 240, 230);
            default:
                return Color.WHITE;
        }
    }

    private void showAppointmentDialog(Vector<Object> data, DefaultTableModel model) {
        // Create dialog with modern look
        JDialog dialog = new JDialog(this, "Appointment Details", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(650, 550);
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
        JLabel titleLabel = new JLabel("Appointment Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));
        headerPanel.add(titleLabel);

        // Form panel with grouped sections
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Participants section
        JPanel participantsPanel = createSectionPanel("Participants");
        JPanel participantsFieldsPanel = new JPanel(new GridLayout(2, 2, 15, 10));

        JComboBox<String> patientCombo = new JComboBox<>();
        JComboBox<String> doctorCombo = new JComboBox<>();

        // Style comboboxes
        styleComboBox(patientCombo);
        styleComboBox(doctorCombo);

        // Load patients and doctors
        try {
            loadComboBox(patientCombo, "SELECT id, name FROM patients ORDER BY name");
            loadComboBox(doctorCombo, "SELECT id, name FROM doctors ORDER BY name");
        } catch (SQLException e) {
            showError("Failed to load patients/doctors: " + e.getMessage());
        }

        participantsFieldsPanel.add(createLabelPanel("Patient:"));
        participantsFieldsPanel.add(patientCombo);
        participantsFieldsPanel.add(createLabelPanel("Doctor:"));
        participantsFieldsPanel.add(doctorCombo);
        participantsPanel.add(participantsFieldsPanel);

        // Appointment details section
        JPanel detailsPanel = createSectionPanel("Appointment Details");
        JPanel detailsFieldsPanel = new JPanel(new GridLayout(3, 2, 15, 10));

        JTextField dateField = createStyledTextField();
        JTextField timeField = createStyledTextField();
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Scheduled", "Completed", "Cancelled", "No-Show"});
        styleComboBox(statusCombo);

        detailsFieldsPanel.add(createLabelPanel("Date (YYYY-MM-DD):"));
        detailsFieldsPanel.add(dateField);
        detailsFieldsPanel.add(createLabelPanel("Time (HH:MM):"));
        detailsFieldsPanel.add(timeField);
        detailsFieldsPanel.add(createLabelPanel("Status:"));
        detailsFieldsPanel.add(statusCombo);
        detailsPanel.add(detailsFieldsPanel);

        // Description section
        JPanel descriptionPanel = createSectionPanel("Description");
        JTextArea descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        descriptionPanel.add(descriptionScroll);

        // Add sections to form panel
        formPanel.add(participantsPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(detailsPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(descriptionPanel);

        // Scroll pane for form
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Populate fields if editing existing appointment
        if (data != null && data.size() > 1) {
            String patientName = data.get(1).toString();
            String doctorName = data.get(2).toString();

            // Find and select patient and doctor in comboboxes
            selectComboItemByName(patientCombo, patientName);
            selectComboItemByName(doctorCombo, doctorName.equals("Not Assigned") ? "" : doctorName);

            dateField.setText(data.get(3).toString());
            timeField.setText(data.get(4).toString());
            statusCombo.setSelectedItem(data.get(5).toString());
            descriptionArea.setText(data.size() > 6 ? data.get(6).toString() : "");
        }

        // Buttons Panel with modern styling
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(189, 195, 199), Color.WHITE);

        JButton saveButton = new JButton(data == null ? "Create Appointment" : "Save Changes");
        styleButton(saveButton, new Color(41, 128, 185), Color.WHITE);

        // Save button action listener
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                if (patientCombo.getSelectedItem() == null ||
                        dateField.getText().isEmpty() || timeField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Get selected patient and doctor IDs
                String patient = patientCombo.getSelectedItem().toString();
                int patientId = Integer.parseInt(patient.split(" - ")[0]);

                // Doctor is optional - handle null case
                Integer doctorId = null;
                if (doctorCombo.getSelectedItem() != null && !doctorCombo.getSelectedItem().toString().isEmpty()) {
                    String doctor = doctorCombo.getSelectedItem().toString();
                    doctorId = Integer.parseInt(doctor.split(" - ")[0]);
                }

                // Prepare SQL query
                String sql;
                if (data == null) {
                    sql = "INSERT INTO appointments (patient_id, doctor_id, date, time, status, description) VALUES (?, ?, ?, ?, ?, ?)";
                } else {
                    sql = "UPDATE appointments SET patient_id=?, doctor_id=?, date=?, time=?, status=?, description=? WHERE id=?";
                }

                try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, patientId);
                    if (doctorId != null) {
                        stmt.setInt(2, doctorId);
                    } else {
                        stmt.setNull(2, Types.INTEGER);
                    }
                    stmt.setString(3, dateField.getText());
                    stmt.setString(4, timeField.getText());
                    stmt.setString(5, statusCombo.getSelectedItem().toString());
                    stmt.setString(6, descriptionArea.getText());

                    if (data != null) {
                        stmt.setInt(7, (Integer) data.get(0));
                    }

                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows > 0) {
                        refreshAppointmentTable(model, "All");
                        dialog.dispose();

                        JOptionPane.showMessageDialog(
                                null,
                                data == null ? "Appointment created successfully!" : "Appointment updated successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving appointment: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // Cancel button action
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



    private void styleComboBox(JComboBox<String> combo) {
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        combo.setBackground(Color.WHITE);
    }







    private JPanel createPatientPanel() {
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

        JButton searchButton = createModernButton("Search", new Color(7, 86, 154), Color.WHITE);

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

        JButton editButton = createModernButtons("Edit", new Color(255, 152, 0), Color.WHITE);

        JButton refreshButton = createModernButtons("Refresh", new Color(0, 120, 215), Color.WHITE);

        // Button actions

        editButton.addActionListener(e -> editPatient(table, model));

        refreshButton.addActionListener(e -> {
            refreshPatientTable(model);
            totalPatientsLabel.setText("Total Patients: " + model.getRowCount());
        });

        // Add buttons to panel
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(editButton);


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

        return panel;
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
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
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

    private JPanel createMessagesPanel() {
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

        // Modern button with rounded corners and hover effect
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
                g2d.setColor(SECONDARY_COLOR);
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

        return panel;
    }

    private void loadConversations(JPanel messageListContent) {
        messageListContent.removeAll();
        messageListContent.setLayout(new BoxLayout(messageListContent, BoxLayout.Y_AXIS));

        try {
            System.out.println("Loading conversations for user ID: " + currentUserId); // Debug

            String query = "SELECT u.user_id, CONCAT(u.first_name, ' ', u.last_name) as name, " +
                    "m.message_text, m.timestamp, m.is_read, m.sender_id, " +
                    "CASE WHEN m.sender_id = ? THEN m.receiver_id ELSE m.sender_id END as other_id " +
                    "FROM messages m " +
                    "JOIN users u ON u.user_id = CASE WHEN m.sender_id = ? THEN m.receiver_id ELSE m.sender_id END " +
                    "JOIN (" +
                    "   SELECT CASE WHEN sender_id = ? THEN receiver_id ELSE sender_id END as other_id, " +
                    "   MAX(timestamp) as max_time " +
                    "   FROM messages " +
                    "   WHERE ? IN (sender_id, receiver_id) " +
                    "   GROUP BY other_id" +
                    ") latest ON (m.sender_id = ? AND m.receiver_id = latest.other_id OR " +
                    "            m.receiver_id = ? AND m.sender_id = latest.other_id) " +
                    "           AND m.timestamp = latest.max_time " +
                    "ORDER BY m.timestamp DESC";

            PreparedStatement stmt = connection.prepareStatement(query);
            for (int i = 1; i <= 6; i++) {
                stmt.setInt(i, currentUserId);
            }

            ResultSet rs = stmt.executeQuery();
            boolean hasResults = false;

            while (rs.next()) {
                hasResults = true;
                int otherUserId = rs.getInt("other_id");
                String senderName = rs.getString("name");
                String lastMessage = rs.getString("message_text");
                String time = new SimpleDateFormat("h:mm a").format(rs.getTimestamp("timestamp"));
                boolean unread = !rs.getBoolean("is_read") && rs.getInt("sender_id") != currentUserId;

                System.out.println("Found conversation with: " + senderName); // Debug

                JPanel messageItem = createMessageListItem(
                        senderName,
                        lastMessage != null && lastMessage.length() > 30 ?
                                lastMessage.substring(0, 30) + "..." : lastMessage,
                        time,
                        unread,
                        otherUserId
                );

                messageListContent.add(messageItem);
                messageListContent.add(Box.createRigidArea(new Dimension(0, 5)));
            }

            if (!hasResults) {
                System.out.println("No conversations found in database"); // Debug
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
            JLabel errorLabel = new JLabel("Error loading conversations");
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            messageListContent.add(errorLabel);
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
                BorderFactory.createMatteBorder(0, 0, 1, 0, SECONDARY_COLOR),
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
                    g2d.setColor(SECONDARY_COLOR);
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
    static class ShadowBorder extends AbstractBorder {
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

        JComboBox<UserComboItem> recipientCombo = new JComboBox<>();
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
                recipientCombo.addItem(new UserComboItem(
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

            UserComboItem recipient = (UserComboItem) recipientCombo.getSelectedItem();
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



    // Helper class for user dropdown
    public static class UserComboItem {
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

    private JPanel createReportsPanel() {
        // Main panel setup
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Initialize table model first
        tableModel = new DefaultTableModel(
                new Object[]{"Report ID", "Patient", "Type", "Date", "Status", "Actions"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only the Actions column is editable
            }
        };

        // Initialize table
        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(35);
        reportTable.setFont(NORMAL_FONT);
        reportTable.getTableHeader().setFont(TITLE_FONT);
        reportTable.setShowGrid(false);
        reportTable.setIntercellSpacing(new Dimension(0, 0));
        reportTable.setSelectionBackground(new Color(225, 240, 255));
        reportTable.setSelectionForeground(DARK_COLOR);

        // Set up action column after table is initialized
        TableColumn actionColumn = reportTable.getColumnModel().getColumn(5);
        actionColumn.setCellRenderer(new ButtonRenderer());
        actionColumn.setCellEditor(new ButtonEditor(new JCheckBox()));

        // Header panel
        JPanel headerPanel = createPanelHeader("Patient Reports", "View and manage patient reports");
        panel.add(headerPanel, BorderLayout.NORTH);

        // Content panel with search and table
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);

        // Search panel with multiple filters
        JPanel searchPanel = createSearchPanel();
        contentPanel.add(searchPanel, BorderLayout.NORTH);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(tablePanel, BorderLayout.CENTER);

        // Action buttons panel
        JPanel actionPanel = createReportsActionPanel();
        contentPanel.add(actionPanel, BorderLayout.SOUTH);

        // Add content to main panel
        panel.add(contentPanel, BorderLayout.CENTER);

        // Add to card layout
        this.contentPanel.add(panel, "reports");

        // Initial load of reports
        loadReports();
        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(BACKGROUND_COLOR);

        // Patient name filter
        JLabel nameLabel = new JLabel("Patient Name:");
        nameLabel.setFont(NORMAL_FONT);
        JTextField nameField = new JTextField(15);
        nameField.setFont(NORMAL_FONT);

        // Date range filter




        // Search button
        JButton searchButton = createSolidButton("Search", WARNING_COLOR);
        JButton clearButton = createSolidButton("Clear", DARK_COLOR);

        searchPanel.add(nameLabel);
        searchPanel.add(nameField);

        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        // Event listeners
        searchButton.addActionListener(e -> {
            String patientName = nameField.getText().trim();


        });

        clearButton.addActionListener(e -> {
            nameField.setText("");

            loadReports(); // Load all reports
        });

        return searchPanel;
    }



    private JPanel createReportsActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setBackground(BACKGROUND_COLOR);

        JButton refreshButton = createSolidButton("Refresh",WARNING_COLOR);
        JButton printButton = createSolidButton("Print Selected", DARK_COLOR);

        actionPanel.add(refreshButton);
        actionPanel.add(printButton);

        // Event listeners
        refreshButton.addActionListener(e -> loadReports());
        printButton.addActionListener(e -> printSelectedReport());

        return actionPanel;
    }

    private void loadReports() {
        loadFilteredReports("", "All", "All");
    }

    private void viewSelectedReport() {
        int selectedRow = reportTable.getSelectedRow();
        if (selectedRow >= 0) {
            int reportId = (int) tableModel.getValueAt(selectedRow, 0);
            String patientName = (String) tableModel.getValueAt(selectedRow, 1);
            String reportType = (String) tableModel.getValueAt(selectedRow, 2);

            // Create a custom dialog to show report details
            JDialog reportDialog = new JDialog();
            reportDialog.setTitle("Report Viewer - " + patientName);
            reportDialog.setSize(800, 600);
            reportDialog.setLocationRelativeTo(this);
            reportDialog.setLayout(new BorderLayout());

            // Header panel
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            headerPanel.setBackground(BACKGROUND_COLOR);

            JLabel titleLabel = new JLabel(reportType + " Report for " + patientName);
            titleLabel.setFont(TITLE_FONT);
            headerPanel.add(titleLabel);

            // Content panel
            JPanel contentPanel = new JPanel(new BorderLayout());

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT file_name, file_data, description FROM reports WHERE report_id = ?")) {

                stmt.setInt(1, reportId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // Text content area
                    JTextArea descriptionArea = new JTextArea(rs.getString("description"));
                    descriptionArea.setFont(NORMAL_FONT);
                    descriptionArea.setEditable(false);
                    descriptionArea.setLineWrap(true);
                    descriptionArea.setWrapStyleWord(true);
                    descriptionArea.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

                    JScrollPane textScroll = new JScrollPane(descriptionArea);
                    textScroll.setBorder(BorderFactory.createTitledBorder("Report Description"));

                    // PDF viewer panel
                    JPanel pdfPanel = new JPanel(new BorderLayout());
                    pdfPanel.setBorder(BorderFactory.createTitledBorder("Report Document"));

                    byte[] fileData = rs.getBytes("file_data");
                    if (fileData != null && fileData.length > 0) {
                        // Create PDF viewer component
                        JLabel pdfLabel = new JLabel("PDF Viewer Placeholder", SwingConstants.CENTER);
                        pdfLabel.setPreferredSize(new Dimension(600, 400));

                        JButton openExternalButton = createSolidButton("Open in External Viewer", SECONDARY_COLOR);
                        byte[] finalFileData1 = fileData;
                        openExternalButton.addActionListener(e -> {
                            try {
                                File tempFile = File.createTempFile("report_", ".pdf");
                                Files.write(tempFile.toPath(), finalFileData1);
                                Desktop.getDesktop().open(tempFile);
                                tempFile.deleteOnExit();
                            } catch (Exception ex) {
                                showError("Error opening PDF: " + ex.getMessage());
                            }
                        });

                        // First, extract all needed data from the ResultSet before it closes
                        String fileName = null;
                        fileData = null;

                        try {
                            fileName = rs.getString("file_name");
                            fileData = rs.getBytes("file_data");
                        } catch (SQLException ex) {
                            showError("Error reading report data: " + ex.getMessage());
                            return;
                        }

// If no file data, show error
                        if (fileData == null || fileData.length == 0) {
                            showError("No PDF data available for this report");
                            return;
                        }

// Set default filename if none provided
                        if (fileName == null || fileName.trim().isEmpty()) {
                            fileName = "report_" + reportId + ".pdf";
                        }

// Ensure .pdf extension
                        if (!fileName.toLowerCase().endsWith(".pdf")) {
                            fileName += ".pdf";
                        }

// Create the download button
                        JButton downloadButton = createSolidButton("Download PDF", PRIMARY_COLOR);
                        String finalFileName = fileName;
                        byte[] finalFileData = fileData;
                        downloadButton.addActionListener(e -> {
                            try {
                                JFileChooser fileChooser = new JFileChooser();
                                fileChooser.setDialogTitle("Save Report PDF");
                                fileChooser.setSelectedFile(new File(finalFileName));

                                // Set PDF file filter
                                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                                    @Override
                                    public boolean accept(File f) {
                                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".pdf");
                                    }

                                    @Override
                                    public String getDescription() {
                                        return "PDF Files (*.pdf)";
                                    }
                                });

                                if (fileChooser.showSaveDialog(DoctorDashboard.this) == JFileChooser.APPROVE_OPTION) {
                                    File fileToSave = fileChooser.getSelectedFile();

                                    // Ensure .pdf extension
                                    if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                                        fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
                                    }

                                    Files.write(fileToSave.toPath(), finalFileData);

                                    JOptionPane.showMessageDialog(
                                            DoctorDashboard.this,
                                            "PDF saved successfully to:\n" + fileToSave.getAbsolutePath(),
                                            "Download Complete",
                                            JOptionPane.INFORMATION_MESSAGE
                                    );
                                }
                            } catch (IOException ex) {
                                showError("Error saving PDF file: " + ex.getMessage());
                            }
                        });

                        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
                        buttonPanel.add(openExternalButton);
                        buttonPanel.add(downloadButton);

                        pdfPanel.add(pdfLabel, BorderLayout.CENTER);
                        pdfPanel.add(buttonPanel, BorderLayout.SOUTH);
                    } else {
                        pdfPanel.add(new JLabel("No PDF document available", SwingConstants.CENTER), BorderLayout.CENTER);
                    }

                    // Add components to content panel
                    contentPanel.add(textScroll, BorderLayout.NORTH);
                    contentPanel.add(pdfPanel, BorderLayout.CENTER);
                }
            } catch (SQLException ex) {
                showError("Error loading report details: " + ex.getMessage());
                return;
            }

            // Footer with close button
            JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
            footerPanel.setBackground(BACKGROUND_COLOR);

            JButton closeButton = createSolidButton("Close", PRIMARY_COLOR);
            closeButton.addActionListener(e -> reportDialog.dispose());
            footerPanel.add(closeButton);

            // Add all components to dialog
            reportDialog.add(headerPanel, BorderLayout.NORTH);
            reportDialog.add(contentPanel, BorderLayout.CENTER);
            reportDialog.add(footerPanel, BorderLayout.SOUTH);

            reportDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a report to view",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void loadFilteredReports(String patientName, String dateRange, String status) {
        tableModel.setRowCount(0);

        StringBuilder sql = new StringBuilder(
                "SELECT r.report_id, p.name, r.report_type, r.report_date, r.status " +
                        "FROM reports r JOIN patients p ON r.patient_id = p.id WHERE 1=1"
        );

        // Add filters based on input
        if (!patientName.isEmpty()) {
            sql.append(" AND p.name LIKE ?");
        }
        if (!"All".equals(status)) {
            sql.append(" AND r.status = ?");
        }

        // Date range filter
        if (!"All".equals(dateRange)) {
            sql.append(" AND r.report_date >= ?");
        }

        sql.append(" ORDER BY r.report_date DESC");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (!patientName.isEmpty()) {
                stmt.setString(paramIndex++, "%" + patientName + "%");
            }
            if (!"All".equals(status)) {
                stmt.setString(paramIndex++, status);
            }
            if (!"All".equals(dateRange)) {
                LocalDate startDate = getStartDateForRange(dateRange);
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
            }

            ResultSet rs = stmt.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("report_id"),
                        rs.getString("name"),
                        rs.getString("report_type"),
                        dateFormat.format(rs.getDate("report_date")),
                        rs.getString("status"),
                        "View"
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No reports found with the current filters",
                        "No Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            showError("Error loading reports: " + ex.getMessage());
        }
    }

    private LocalDate getStartDateForRange(String dateRange) {
        LocalDate today = LocalDate.now();
        return switch (dateRange) {
            case "Today" -> today;
            case "This Week" -> today.with(DayOfWeek.MONDAY);
            case "This Month" -> today.withDayOfMonth(1);
            default -> LocalDate.of(1900, 1, 1); // Default to very old date if custom
        };
    }

    private void printSelectedReport() {
        int selectedRow = reportTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Implement print functionality here
            JOptionPane.showMessageDialog(this, "Print functionality would be implemented here",
                    "Print Report", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a report to print",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
        }
    }




    // Button renderer for the table
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Button editor for the table
    class ButtonEditor extends DefaultCellEditor {
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                viewSelectedReport(); // Call your view method when button is clicked
            }
            isPushed = false;
            return label;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
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
    private JPanel createSettingsPanel() {
        // Main panel with modern flat design
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Modern header with flat design
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Doctor Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 37, 41));

        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Modern flat tabbed pane with tabs on the left
        JTabbedPane tabbedPane = createModernTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.LEFT);
        tabbedPane.addTab("Profile", createProfileSettingsTab());
        tabbedPane.addTab("Account", createAccountSettingsTab());

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JTabbedPane createModernTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(new Color(245, 245, 245)); // Light gray background for tab area
        tabbedPane.setForeground(new Color(33, 37, 41));

        // Add spacing around tabs
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)), // Right border
                BorderFactory.createEmptyBorder(15, 10, 15, 10) // Padding around tabs
        ));

        // Custom UI for modern flat tabs with vertical spacing
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
                if (isSelected) {
                    g.setColor(new Color(79, 70, 229)); // Indigo accent color
                    g.fillRect(x + w - 3, y, 3, h); // Right border for selected tab
                }
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                              int x, int y, int w, int h, boolean isSelected) {
                g.setColor(isSelected ? Color.WHITE : new Color(250, 250, 252));
                g.fillRect(x, y, w, h);

                // Add subtle separator between tabs
                if (tabIndex > 0) {
                    g.setColor(new Color(230, 230, 230));
                    g.drawLine(x, y - 5, x + w, y - 5);
                }
            }

            @Override
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                return 120; // Fixed width for side tabs
            }

            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                return 50; // Increased height for better spacing
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                                     int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int x = textRect.x + (textRect.width - metrics.stringWidth(title)) / 2;
                int y = textRect.y + (textRect.height - metrics.getHeight()) / 2 + metrics.getAscent();

                g2.setColor(isSelected ? new Color(79, 70, 229) : new Color(100, 100, 100));
                g2.drawString(title, x, y);
            }

            @Override
            protected LayoutManager createLayoutManager() {
                return new BasicTabbedPaneUI.TabbedPaneLayout() {
                    @Override
                    protected void calculateTabRects(int tabPlacement, int tabCount) {
                        super.calculateTabRects(tabPlacement, tabCount);

                        // Add extra vertical spacing between tabs
                        for (int i = 0; i < tabCount; i++) {
                            rects[i].y += i * 5; // 5px additional spacing between tabs
                        }
                    }
                };
            }
        });

        return tabbedPane;
    }

    // Class variable to store the current profile image
    private ImageIcon profileImage;

    private JPanel createProfileSettingsTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Profile card with shadow effect
        JPanel profileCard = createCardPanel();
        profileCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
        profileCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Initialize default profile image if not already set
        if (profileImage == null) {
            profileImage = createCircularProfileIcon();
        }

        // Circular avatar with border
        JLabel pictureLabel = new JLabel(profileImage);
        pictureLabel.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(new Color(79, 70, 229), 2, 50),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        JButton changePictureBtn = createPrimaryButton("Change Photo", new Color(79, 70, 229));

        // Add action listener to the change photo button
        changePictureBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Profile Picture");

            // Set file filter to only show image files
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Image Files", "jpg", "jpeg", "png", "gif");
            fileChooser.setFileFilter(filter);

            int result = fileChooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    // Load and process the selected image
                    BufferedImage originalImage = ImageIO.read(selectedFile);
                    if (originalImage != null) {
                        // Save the image path to the database
                        saveProfileImagePath(selectedFile.getAbsolutePath());

                        // Update the displayed image
                        profileImage = createCircularImageIcon(originalImage);
                        pictureLabel.setIcon(profileImage);
                        panel.revalidate();
                        panel.repaint();

                        showSuccess("Profile picture updated successfully!");
                    }
                } catch (IOException ex) {
                    showError("Error loading image: " + ex.getMessage());
                }
            }
        });

        profileCard.add(pictureLabel);
        profileCard.add(changePictureBtn);

        // Form fields with modern styling
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 2, 20, 15));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        String name = (doctorData != null) ? doctorData.getOrDefault("name", "") : "";
        String[] nameParts = name.split(" ", 2);

        formPanel.add(createFormLabel("First Name"));
        JTextField firstNameField = createModernTextField(nameParts.length > 0 ? nameParts[0] : "");
        formPanel.add(firstNameField);

        formPanel.add(createFormLabel("Last Name"));
        JTextField lastNameField = createModernTextField(nameParts.length > 1 ? nameParts[1] : "");
        formPanel.add(lastNameField);

        formPanel.add(createFormLabel("Specialization"));
        JTextField specializationField = createModernTextField(
                (doctorData != null) ? doctorData.getOrDefault("specialization", "") : ""
        );
        formPanel.add(specializationField);

        formPanel.add(createFormLabel("Email"));
        JTextField emailField = createModernTextField(
                (doctorData != null) ? doctorData.getOrDefault("email", "") : ""
        );
        formPanel.add(emailField);

        // Action button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveBtn = createPrimaryButton("Save Changes", new Color(16, 185, 129));
        saveBtn.addActionListener(e -> saveProfileChanges(firstNameField, lastNameField, specializationField, emailField));

        buttonPanel.add(saveBtn);

        panel.add(profileCard);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(formPanel);
        panel.add(buttonPanel);

        return panel;
    }

    private JPanel createAccountSettingsTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Security section title
        JLabel securityTitle = new JLabel("Security Settings");
        securityTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        securityTitle.setForeground(new Color(33, 37, 41));
        securityTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        securityTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Password form panel
        JPanel passwordPanel = createCardPanel();
        passwordPanel.setLayout(new GridLayout(0, 2, 20, 15));
        passwordPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        passwordPanel.add(createFormLabel("Current Password"));
        JPasswordField currentPassField = createModernPasswordField();
        passwordPanel.add(currentPassField);

        passwordPanel.add(createFormLabel("New Password"));
        JPasswordField newPassField = createModernPasswordField();
        passwordPanel.add(newPassField);

        passwordPanel.add(createFormLabel("Confirm Password"));
        JPasswordField confirmPassField = createModernPasswordField();
        passwordPanel.add(confirmPassField);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JButton updateBtn = createPrimaryButton("Update Password", new Color(79, 70, 229));
        updateBtn.addActionListener(e -> updatePassword(currentPassField, newPassField, confirmPassField));

        buttonPanel.add(updateBtn);

        panel.add(securityTitle);
        panel.add(passwordPanel);
        panel.add(buttonPanel);

        return panel;
    }

    // Helper methods for UI components
    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }



    private JTextField createModernTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private JPasswordField createModernPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private JButton createPrimaryButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(darken(bgColor, 0.1f));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    // Creates circular profile icon (placeholder)
    private ImageIcon createCircularProfileIcon() {
        // Create a blank 64x64 image for the profile picture placeholder
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        // Enable anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw circle background
        g2.setColor(new Color(229, 231, 235));
        g2.fillOval(0, 0, 64, 64);

        // Draw person silhouette
        g2.setColor(new Color(156, 163, 175));
        // Head
        g2.fillOval(22, 12, 20, 20);
        // Body
        g2.fillRect(18, 34, 28, 22);

        g2.dispose();

        return new ImageIcon(img);
    }

    // Create circular image from loaded image
    private ImageIcon createCircularImageIcon(BufferedImage image) {
        int size = 64;
        BufferedImage output = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();

        // Enable anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Create a circular mask
        Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, size, size);
        g2.setClip(circle);

        // Scale the original image to fit
        double scale = Math.max((double)size / image.getWidth(), (double)size / image.getHeight());
        int scaleWidth = (int)(image.getWidth() * scale);
        int scaleHeight = (int)(image.getHeight() * scale);

        // Center the image
        int x = (size - scaleWidth) / 2;
        int y = (size - scaleHeight) / 2;

        // Draw the scaled image
        g2.drawImage(image, x, y, scaleWidth, scaleHeight, null);
        g2.dispose();

        return new ImageIcon(output);
    }

    // Custom round border for profile picture
    private static class RoundBorder extends AbstractBorder {
        private Color color;
        private int thickness;
        private int radius;

        public RoundBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    // Helper method to darken colors for hover effects
    private Color darken(Color color, float fraction) {
        int red = Math.max(0, Math.round(color.getRed() * (1 - fraction)));
        int green = Math.max(0, Math.round(color.getGreen() * (1 - fraction)));
        int blue = Math.max(0, Math.round(color.getBlue() * (1 - fraction)));
        return new Color(red, green, blue);
    }

    // Save profile image path to database
    private void saveProfileImagePath(String imagePath) {
        if (connection == null) {
            showError("No database connection available");
            return;
        }

        try {
            String query = "UPDATE doctors SET profile_image = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, imagePath);
            stmt.setInt(2, currentDoctorId);

            int updated = stmt.executeUpdate();
            if (updated > 0 && doctorData != null) {
                doctorData.put("profile_image", imagePath);
            }
            stmt.close();
        } catch (SQLException ex) {
            showError("Error saving profile image: " + ex.getMessage());
        }
    }

    // Action methods
    private void saveProfileChanges(JTextField firstNameField, JTextField lastNameField,
                                    JTextField specializationField, JTextField emailField) {
        if (connection == null) {
            showError("No database connection available");
            return;
        }

        try {
            String query = "UPDATE doctors SET name = ?, specialization = ?, email = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, firstNameField.getText() + " " + lastNameField.getText());
            stmt.setString(2, specializationField.getText());
            stmt.setString(3, emailField.getText());
            stmt.setInt(4, currentDoctorId);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                if (doctorData != null) {
                    doctorData.put("name", firstNameField.getText() + " " + lastNameField.getText());
                    doctorData.put("specialization", specializationField.getText());
                    doctorData.put("email", emailField.getText());
                }
                showSuccess("Profile updated successfully!");
            }
            stmt.close();
        } catch (SQLException ex) {
            showError("Error updating profile: " + ex.getMessage());
        }
    }

    private void updatePassword(JPasswordField currentPassField, JPasswordField newPassField,
                                JPasswordField confirmPassField) {
        String currentPass = new String(currentPassField.getPassword());
        String newPass = new String(newPassField.getPassword());
        String confirmPass = new String(confirmPassField.getPassword());

        String storedPassword = (doctorData != null) ? doctorData.getOrDefault("password", "") : "";
        if (!currentPass.equals(storedPassword)) {
            showError("Current password is incorrect");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showError("New passwords don't match");
            return;
        }

        if (connection == null) {
            showError("No database connection available");
            return;
        }

        try {
            String query = "UPDATE doctors SET password = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, newPass);
            stmt.setInt(2, currentDoctorId);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                if (doctorData != null) {
                    doctorData.put("password", newPass);
                }
                showSuccess("Password updated successfully!");
                currentPassField.setText("");
                newPassField.setText("");
                confirmPassField.setText("");
            }
            stmt.close();
        } catch (SQLException ex) {
            showError("Error updating password: " + ex.getMessage());
        }
    }

    // Modern message dialogs
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(null, message, "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }


    private JPanel createLogoutPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_COLOR);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(LIGHT_COLOR);

        JLabel titleLabel = new JLabel("Logout");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(DARK_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("Are you sure you want to logout?");
        messageLabel.setFont(SUBHEADER_FONT);
        messageLabel.setForeground(PRIMARY_COLOR);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(LIGHT_COLOR);

        JButton logoutBtn = createStyledButton("Logout", DANGER_COLOR);
        JButton cancelBtn = createStyledButton("Cancel", SECONDARY_COLOR);

        buttonPanel.add(logoutBtn);
        buttonPanel.add(cancelBtn);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(messageLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(buttonPanel);
        centerPanel.add(Box.createVerticalGlue());

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private void createStatusBar() {
        statusBarPanel = new JPanel(new BorderLayout());
        statusBarPanel.setBackground(SUCCESS_COLOR);
        statusBarPanel.setPreferredSize(new Dimension(getWidth(), 40)); // Slightly taller
        statusBarPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // Left and right padding

        // Left side content with additional padding
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Right margin

        JLabel statusLabel = new JLabel("MediCare System Hospital");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(Color.WHITE);
        leftPanel.add(statusLabel);

        // Right side content
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);

        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Slightly larger font
        dateTimeLabel.setForeground(Color.WHITE);
        rightPanel.add(dateTimeLabel);

        // Add both panels to the status bar
        statusBarPanel.add(leftPanel, BorderLayout.WEST);
        statusBarPanel.add(rightPanel, BorderLayout.EAST);

        // Add subtle separator between the two sections
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setForeground(new Color(255, 255, 255, 80)); // Semi-transparent white
        separator.setPreferredSize(new Dimension(1, 20));
        rightPanel.add(separator, 0); // Add at beginning
        rightPanel.add(Box.createHorizontalStrut(10)); // Add spacing after separator
    }

    private void startTimeUpdater() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy | hh:mm:ss a");
            dateTimeLabel.setText(dateFormat.format(new Date()));
        });
        timer.start();
    }

    private void showPanel(String panelName) {
        if ("logout".equals(panelName)) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose(); // Close the current DoctorDashboard
                SwingUtilities.invokeLater(() -> {
                    ConnectPage connectPage = new ConnectPage();
                    connectPage.setVisible(true); // Open the ConnectPage
                });
            }
        } else {
            cardLayout.show(contentPanel, panelName);
        }
    }


    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(SECONDARY_COLOR);
        button.setForeground(DARK_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(REGULAR_FONT);
        label.setForeground(DARK_COLOR);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private JTextField createTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(REGULAR_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(REGULAR_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return field;
    }





    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            DoctorDashboard dashboard = new DoctorDashboard(102);
            dashboard.setVisible(true);
        });
    }
}