import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ReceptionistDashboard extends JFrame {
    // Color scheme
    private static final Color PRIMARY_COLOR = new Color(0, 120, 150);
    private static final Color SECONDARY_COLOR = new Color(200, 230, 240);
    private static final Color SUCCESS_COLOR = new Color(0, 100, 140);
    private static final Color DANGER_COLOR = new Color(230, 60, 80);
    private static final Color WARNING_COLOR = new Color(255, 180, 50);
    private static final Color INFO_COLOR = new Color(0, 160, 180);
    private static final Color LIGHT_COLOR = new Color(240, 242, 245);
    private static final Color DARK_COLOR = new Color(20, 40, 80);
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
    private Map<String, String> receptionistDetails = new HashMap<>();
    private JLabel currentTimeLabel;
    private Timer timeUpdateTimer;
    private JButton activeSidebarButton;
    JTable reportTable;
    DefaultTableModel tableModel;
    private DefaultTableModel patientTableModel;

    public ReceptionistDashboard(String userId) {
        initializeDB();
        this.userId = userId;
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadReceptionistDetails();
        initializeUI();
        startTimeUpdater();
        setLocationRelativeTo(null);
    }

    private void initializeDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void loadReceptionistDetails() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT name, email, phone FROM employees WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                receptionistDetails.put("name", rs.getString("name"));
                receptionistDetails.put("email", rs.getString("email"));
                receptionistDetails.put("phone", rs.getString("phone"));
            }
        } catch (SQLException ex) {
            showError("Error loading receptionist details: " + ex.getMessage());
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
        setTitle("MediCare Hospital - Receptionist Portal");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.NORTH);

        JPanel sidePanel = createSidePanel();
        add(sidePanel, BorderLayout.WEST);

        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(BACKGROUND_COLOR);
        add(contentPanel, BorderLayout.CENTER);

        createReportsPanel();
        createTimePunchPanel();
        createPatientPanel();
        createMedicinePanel();
        createDoctorPanel();
        createAppointmentPanel();

        // Show reports panel by default
        cardLayout.show(contentPanel, "reports");

        // Activate the reports button in sidebar
        JPanel reportsButtonPanel = (JPanel) sidePanel.getComponent(1);
        JButton reportsButton = (JButton) reportsButtonPanel.getComponent(0);
        activateSidebarButton(reportsButton);

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
        currentTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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

        // Profile section
        JPanel profileSection = createProfileSection();
        sidePanel.add(profileSection);
        sidePanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        // Sidebar buttons
        sidePanel.add(createSidebarButtonPanel("Reports", "reports", "/icons/reports.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidePanel.add(createSidebarButtonPanel("Time Punch", "time", "/icons/clock.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidePanel.add(createSidebarButtonPanel("Patients", "patients", "/icons/patient.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidePanel.add(createSidebarButtonPanel("Medicine Stock", "medicines", "/icons/medicine.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidePanel.add(createSidebarButtonPanel("Doctors", "doctors", "/icons/doctor.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidePanel.add(createSidebarButtonPanel("Appointments", "appointments", "/icons/calendar.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidePanel.add(Box.createVerticalGlue());

        // Logout button
        JPanel logoutPanel = createLogoutPanel();
        sidePanel.add(logoutPanel);

        return sidePanel;
    }

    private JPanel createProfileSection() {
        JPanel profileSection = new JPanel();
        profileSection.setLayout(new BoxLayout(profileSection, BoxLayout.Y_AXIS));
        profileSection.setBackground(Color.white);
        profileSection.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        profileSection.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(SECONDARY_COLOR);
                g2d.fillOval(0, 0, 70, 70);
                g2d.setColor(PRIMARY_COLOR);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 28));
                String initials = receptionistDetails.containsKey("name") && !receptionistDetails.get("name").isEmpty()
                        ? receptionistDetails.get("name").substring(0, 1).toUpperCase()
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
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(receptionistDetails.get("name"));
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(Color.black);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("Receptionist");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(Color.black);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileSection.add(avatarPanel);
        profileSection.add(Box.createRigidArea(new Dimension(0, 10)));
        profileSection.add(nameLabel);
        profileSection.add(Box.createRigidArea(new Dimension(0, 5)));
        profileSection.add(roleLabel);

        return profileSection;
    }

    private JPanel createLogoutPanel() {
        JPanel logoutPanel = createSidebarButtonPanel("Logout", "logout", "/icons/logout.png");
        logoutPanel.setBackground(WARNING_COLOR);
        JLabel logoutLabel = null;
        for (Component comp : logoutPanel.getComponents()) {
            if (comp instanceof JLabel) {
                logoutLabel = (JLabel) comp;
                break;
            }
        }
        JLabel finalLogoutLabel = logoutLabel;
        logoutPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (finalLogoutLabel != null) finalLogoutLabel.setForeground(Color.WHITE);
                logoutPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutPanel.setBackground(WARNING_COLOR);
                if (finalLogoutLabel != null) finalLogoutLabel.setForeground(Color.WHITE);
                logoutPanel.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                performLogout();
            }
        });
        return logoutPanel;
    }

    private JPanel createSidebarButtonPanel(String text, String card, String iconPath) {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        Color bgColor = card.equals("logout") ? WARNING_COLOR : SUCCESS_COLOR;
        buttonPanel.setBackground(bgColor);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        buttonPanel.setMaximumSize(new Dimension(250, 50));

        JButton btn = new JButton(text);
        btn.setFont(NORMAL_FONT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);

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
                        btn.setBackground(bgColor);
                    }
                }
            });
        } else {
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
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private void createReportsPanel() {
        // Main panel setup
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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
        JPanel tablePanel = createReportsTablePanel();
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

    private JPanel createReportsTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Table model with uneditable cells
        tableModel = new DefaultTableModel(
                new Object[]{"Report ID", "Patient", "Type", "Date", "Status", "Actions"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only the Actions column is editable
            }
        };

        // Table configuration
        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(35);
        reportTable.setFont(NORMAL_FONT);
        reportTable.getTableHeader().setFont(TITLE_FONT);
        reportTable.setShowGrid(false);
        reportTable.setIntercellSpacing(new Dimension(0, 0));
        reportTable.setSelectionBackground(new Color(225, 240, 255));
        reportTable.setSelectionForeground(DARK_COLOR);

        // Add view button to each row
        TableColumn actionColumn = reportTable.getColumnModel().getColumn(5);
        actionColumn.setCellRenderer(new ButtonRenderer());
        actionColumn.setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
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

                                if (fileChooser.showSaveDialog(ReceptionistDashboard.this) == JFileChooser.APPROVE_OPTION) {
                                    File fileToSave = fileChooser.getSelectedFile();

                                    // Ensure .pdf extension
                                    if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                                        fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
                                    }

                                    Files.write(fileToSave.toPath(), finalFileData);

                                    JOptionPane.showMessageDialog(
                                            ReceptionistDashboard.this,
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

    // Button renderer and editor for the Actions column


     class ButtonEditor extends DefaultCellEditor {
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            editorComponent = new JButton();
            ((JButton) editorComponent).setOpaque(true);
            ((JButton) editorComponent).setBackground(PRIMARY_COLOR);
            ((JButton) editorComponent).setForeground(Color.WHITE);
            ((JButton) editorComponent).addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            ((JButton) editorComponent).setText(label);
            isPushed = true;
            return editorComponent;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                viewSelectedReport();
            }
            isPushed = false;
            return label;
        }
    }

    // Other panel creation methods (TimePunch, Patient, Medicine, Doctor, Appointment) remain the same
    // ... [Previous code for other panels] ...

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





    // Time Punch Panel (View Only)
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
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    return c;
                }
            });

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

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
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
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
        punchButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
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

    // [createMedicineStorePanel, loadMedicineData, performSearch, addMedicine, updateMedicine, deleteMedicine remain unchanged]

    // [createMedicineTransactionsPanel, recordTransaction, loadTransactionData remain unchanged]

    // [createModernStatCard, createModernLoadingCard, RoundedPanelUI remain unchanged]

    // [getLowStockMedicines, getTotalMedicines, getTotalSuppliers, getExpiringSoonMedicines remain unchanged]

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
        return "IN";
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

    // Patient Panel (View and Search Only)
    private void createPatientPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = createPanelHeader("Patients", "View patient information");
        panel.add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(BACKGROUND_COLOR);

        JTextField searchField = new JTextField(20);
        JButton searchButton = createSolidButton("Search", PRIMARY_COLOR);
        JButton refreshButton = createSolidButton("Refresh", INFO_COLOR);

        searchButton.setToolTipText("Search patients by name, ID or contact");
        refreshButton.setToolTipText("Reload all patient data");

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        // Create table model with correct columns
        patientTableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Email", "Phone", "Address", "Blood Group", "Gender", "Date of Birth"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Configure table
        JTable table = new JTable(patientTableModel);
        table.setRowHeight(35);
        table.setFont(NORMAL_FONT);
        table.getTableHeader().setFont(TITLE_FONT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Action listeners
        ActionListener searchAction = e -> loadPatientData(searchField.getText().trim());
        searchButton.addActionListener(searchAction);
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadPatientData("");
        });
        searchField.addActionListener(searchAction);

        // Initial load
        loadPatientData("");

        // Add components
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);
        this.contentPanel.add(panel, "patients");
    }

    private void loadPatientData(String searchQuery) {
        patientTableModel.setRowCount(0); // Clear existing data

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT id, name, email, phone, address, blood_group, gender, dob " +
                    "FROM patients WHERE " +
                    "id LIKE ? OR " +
                    "name LIKE ? OR " +
                    "email LIKE ? OR " +
                    "phone LIKE ? OR " +
                    "address LIKE ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String likeQuery = "%" + searchQuery + "%";
                stmt.setString(1, likeQuery);
                stmt.setString(2, likeQuery);
                stmt.setString(3, likeQuery);
                stmt.setString(4, likeQuery);
                stmt.setString(5, likeQuery);

                ResultSet rs = stmt.executeQuery();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

                while (rs.next()) {
                    patientTableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("blood_group"),
                            rs.getString("gender"),
                            dateFormat.format(rs.getDate("dob"))
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading patient data: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void searchPatients(String searchQuery) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT patient_id, name, email, phone, blood_group, gender, dob " +
                    "FROM patients WHERE " +
                    "patient_id LIKE ? OR " +
                    "name LIKE ? OR " +
                    "email LIKE ? OR " +
                    "phone LIKE ? OR " +
                    "blood_group LIKE ? OR " +
                    "gender LIKE ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String likeQuery = "%" + searchQuery + "%";
                for (int i = 1; i <= 6; i++) {
                    stmt.setString(i, likeQuery);
                }

                patientTableModel.setRowCount(0); // Clear existing data

                ResultSet rs = stmt.executeQuery();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

                while (rs.next()) {
                    patientTableModel.addRow(new Object[]{
                            rs.getInt("patient_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("blood_group"),
                            rs.getString("gender"),
                            dateFormat.format(rs.getDate("dob"))
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error searching patients: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshPatients() {
        searchPatients(""); // Empty search loads all patients
    }

    private void loadPatientData(DefaultTableModel model, String searchQuery) {
        model.setRowCount(0); // Clear existing data

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT patient_id, name, email, phone, blood_group, gender, dob " +
                    "FROM patients WHERE name LIKE ? OR patient_id LIKE ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + searchQuery + "%");
                stmt.setString(2, "%" + searchQuery + "%");

                ResultSet rs = stmt.executeQuery();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("patient_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("blood_group"),
                            rs.getString("gender"),
                            dateFormat.format(rs.getDate("dob"))
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading patient data: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    // Medicine Panel (View and Search Only)
    private void createMedicinePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = createPanelHeader("Medicine Inventory", "View medicine stock");
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(BACKGROUND_COLOR);
        JTextField searchField = new JTextField(20);
        JButton searchButton = createSolidButton("Search", PRIMARY_COLOR);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Name", "Description", "Quantity", "Price ($)", "Expiry Date", "Supplier"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(NORMAL_FONT);
        table.getTableHeader().setFont(TITLE_FONT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(e -> loadMedicineData(model, searchField.getText().trim()));
        loadMedicineData(model, "");

        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);
        this.contentPanel.add(panel, "medicines");
    }

    private void loadMedicineData(DefaultTableModel model, String searchQuery) {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT id, name, description, quantity, price, expiry_date, supplier FROM medicines " +
                    "WHERE name LIKE ? OR description LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            String query = "%" + searchQuery + "%";
            stmt.setString(1, query);
            stmt.setString(2, query);
            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        dateFormat.format(rs.getDate("expiry_date")),
                        rs.getString("supplier")
                });
            }


        } catch (SQLException ex) {
            showError("Error loading medicine data: " + ex.getMessage());
        }
    }

    // Doctor Panel (View and Search Only)
    private void createDoctorPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = createPanelHeader("Doctors", "View doctor information");
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(BACKGROUND_COLOR);
        JTextField searchField = new JTextField(20);
        JButton searchButton = createSolidButton("Search", PRIMARY_COLOR);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Name", "Phone", "Specialization", "Qualification"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(NORMAL_FONT);
        table.getTableHeader().setFont(TITLE_FONT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(e -> loadDoctorData(model, searchField.getText().trim()));
        loadDoctorData(model, "");

        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);
        this.contentPanel.add(panel, "doctors");
    }

    private void loadDoctorData(DefaultTableModel model, String searchQuery) {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT id, name, phone, specialization, qualification FROM doctors " +
                    "WHERE name LIKE ? OR specialization LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            String query = "%" + searchQuery + "%";
            stmt.setString(1, query);
            stmt.setString(2, query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("specialization"),
                        rs.getString("qualification")
                });
            }
        } catch (SQLException ex) {
            showError("Error loading doctor data: " + ex.getMessage());
        }
    }

    // Appointment Panel (View and Search Only)
    private void createAppointmentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = createPanelHeader("Appointments", "View patient appointments");
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(BACKGROUND_COLOR);
        JTextField searchField = new JTextField(20);
        JButton searchButton = createSolidButton("Search", PRIMARY_COLOR);
        searchPanel.add(new JLabel("Search by Patient/Doctor:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Patient", "Doctor", "Date", "Time", "Status", "Description"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(NORMAL_FONT);
        table.getTableHeader().setFont(TITLE_FONT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(e -> loadAppointmentData(model, searchField.getText().trim()));
        loadAppointmentData(model, "");

        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);
        this.contentPanel.add(panel, "appointments");
    }

    private void loadAppointmentData(DefaultTableModel model, String searchQuery) {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT a.id, p.name AS patient_name, d.name AS doctor_name, a.date, a.time, a.status, a.description " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN doctors d ON a.doctor_id = d.id " +
                    "WHERE p.name LIKE ? OR d.name LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            String query = "%" + searchQuery + "%";
            stmt.setString(1, query);
            stmt.setString(2, query);
            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        dateFormat.format(rs.getDate("date")),
                        timeFormat.format(rs.getTime("time")),
                        rs.getString("status"),
                        rs.getString("description")
                });
            }
        } catch (SQLException ex) {
            showError("Error loading appointment data: " + ex.getMessage());
        }
    }

    // Helper Methods



    // Helper Methods
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
        SwingUtilities.invokeLater(() -> new ReceptionistDashboard("101"));
    }
}