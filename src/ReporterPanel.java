import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporterPanel extends JPanel {
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

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/HMsystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Ashish030406";

    // UI Components
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JComboBox<Patient> patientCombo;
    private JComboBox<String> reportTypeCombo;
    private JTextArea descriptionArea;
    private JButton uploadBtn;
    private JLabel statusLabel;
    private JLabel dropZoneLabel;
    private File currentPdfFile;
    private DefaultTableModel recentReportsModel;
    private JButton activeSidebarButton;
    private JLabel currentTimeLabel;
    private Timer timeUpdateTimer;
    private String userId;
    private String userName=getReporterName();

    public ReporterPanel(String userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        setBackground(BACKGROUND_COLOR);
        add(createStatusBar(), BorderLayout.NORTH);
        initializeUI();
        startTimeUpdater();
    }

    public String getReporterName() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM employees WHERE user_id = ? AND role = 'Reporter'")) {
            stmt.setString(1, userId); // userId is String, but database handles conversion to integer
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            } else {
                return "Unknown Reporter";
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error fetching reporter name: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return "Unknown Reporter";
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

    private void initializeUI() {
        // Create sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Content Panel with CardLayout
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(BACKGROUND_COLOR);

        // Add panels to CardLayout
        contentPanel.add(createHomePanel(), "home");
        contentPanel.add(createPdfInsertPanel(), "pdfInsert");
        contentPanel.add(createPatientPanel(), "patients");

        add(contentPanel, BorderLayout.CENTER);

        // Show home panel by default
        cardLayout.show(contentPanel, "home");
    }

    private JPanel createSidebar() {
        // Main sidebar panel now uses BorderLayout
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0, 80, 120)));

        // Create a panel for the top content (profile + navigation)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(Color.WHITE);

        // Profile section
        topPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        JPanel profileWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        profileWrapper.setBackground(Color.WHITE);
        profileWrapper.add(createProfileSection());
        topPanel.add(profileWrapper);
        topPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Navigation buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        JButton homeBtn = createSidebarButton("Home", "home");
        JButton pdfBtn = createSidebarButton("Insert PDF", "pdfInsert");
        JButton patientsBtn = createSidebarButton("Patients", "patients");

        buttonPanel.add(homeBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanel.add(pdfBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanel.add(patientsBtn);

        topPanel.add(buttonPanel);
        sidebar.add(topPanel, BorderLayout.NORTH);

        // Logout button panel (will stick to bottom)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 20, 15));

        JButton logoutButton = createSidebarButton("Logout", "logout");
        logoutButton.setBackground(WARNING_COLOR);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setMaximumSize(new Dimension(200, 45)); // Same width as other buttons
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.addActionListener(e -> performLogout());

        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setBackground(WARNING_COLOR.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setBackground(WARNING_COLOR);
            }
        });

        bottomPanel.add(logoutButton, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createSidebarButton(String text, String card) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 45)); // Consistent width for all buttons
        button.setPreferredSize(new Dimension(200, 45));
        button.setFont(NORMAL_FONT);
        button.setBackground(card.equals("logout") ? WARNING_COLOR : SUCCESS_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (!card.equals("logout")) {
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (button != activeSidebarButton) {
                        button.setBackground(PRIMARY_COLOR);
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (button != activeSidebarButton) {
                        button.setBackground(SUCCESS_COLOR);
                    }
                }
            });

            button.addActionListener(e -> {
                cardLayout.show(contentPanel, card);
                activateSidebarButton(button);
            });
        }

        return button;
    }



    private JPanel createProfileSection() {
        JPanel profileSection = new JPanel();
        profileSection.setLayout(new BoxLayout(profileSection, BoxLayout.Y_AXIS));
        profileSection.setBackground(Color.WHITE);
        profileSection.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(SECONDARY_COLOR);
                g2d.fillOval(0, 0, 80, 80);
                g2d.setColor(PRIMARY_COLOR);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 32));
                UserSession.User user = UserSession.getCurrentUser();
                String initials = user != null ? user.getUsername().substring(0, 1).toUpperCase() : "?";
                FontMetrics fm = g2d.getFontMetrics();
                int stringWidth = fm.stringWidth(initials);
                int stringHeight = fm.getHeight();
                g2d.drawString(initials, (80 - stringWidth) / 2, (80 - stringHeight) / 2 + fm.getAscent());
                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(80, 80);
            }
        };
        avatarPanel.setMaximumSize(new Dimension(80, 80));
        avatarPanel.setOpaque(false);
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        UserSession.User user = UserSession.getCurrentUser();
        String username = user != null ? user.getUsername() : "Unknown";
        String role = user != null ? user.getRole() : "Unknown";

        JLabel nameLabel = new JLabel(username);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(DARK_COLOR);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(100, 100, 100));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileSection.add(avatarPanel);
        profileSection.add(Box.createRigidArea(new Dimension(0, 10)));
        profileSection.add(nameLabel);
        profileSection.add(Box.createRigidArea(new Dimension(0, 5)));
        profileSection.add(roleLabel);

        return profileSection;
    }

    private JPanel createStatusCard(String title, int count, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        card.setPreferredSize(new Dimension(200, 120)); // Set fixed size for consistency

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(NORMAL_FONT);
        titleLabel.setForeground(DARK_COLOR);

        JLabel countLabel = new JLabel(String.valueOf(count), JLabel.CENTER);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        countLabel.setForeground(color);

        // Add some spacing between elements
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        contentPanel.add(countLabel);
        contentPanel.add(Box.createVerticalGlue());

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header panel with welcome message
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);

        JLabel welcomeLabel = new JLabel("Welcome, " + userName);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(DARK_COLOR);

        JLabel subtitleLabel = new JLabel("Report Management Dashboard");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 100, 100));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BACKGROUND_COLOR);
        titlePanel.add(welcomeLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subtitleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Status Cards
        JPanel statusPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statusPanel.setBackground(BACKGROUND_COLOR);
        statusPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        statusPanel.add(createStatusCard("Pending Reports", getReportCount("Pending"), PRIMARY_COLOR));
        statusPanel.add(createStatusCard("Published Reports", getReportCount("Published"), SUCCESS_COLOR));
        statusPanel.add(createStatusCard("Reviewed Reports", getReportCount("Reviewed"), INFO_COLOR));
        panel.add(statusPanel, BorderLayout.CENTER);

        // Recent Reports Table
        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setBackground(Color.WHITE);
        recentPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 0, 0),
                new TitledBorder("Recent Reports")
        ));

        recentReportsModel = new DefaultTableModel(
                new Object[]{"ID", "Patient", "Type", "Date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable recentTable = new JTable(recentReportsModel);
        recentTable.setRowHeight(35);
        recentTable.setFont(NORMAL_FONT);
        recentTable.getTableHeader().setFont(TITLE_FONT);
        recentTable.setShowGrid(false);
        recentTable.setIntercellSpacing(new Dimension(0, 0));
        recentTable.setSelectionBackground(new Color(225, 240, 255));
        recentTable.setSelectionForeground(DARK_COLOR);

        JScrollPane scrollPane = new JScrollPane(recentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        recentPanel.add(scrollPane, BorderLayout.CENTER);

        loadRecentReports();
        panel.add(recentPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPdfInsertPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header
        JPanel headerPanel = createPanelHeader("Upload Report", "Add new patient reports");
        panel.add(headerPanel, BorderLayout.NORTH);

        // Main content with modern card layout
        JPanel mainCard = new JPanel(new GridBagLayout());
        mainCard.setBackground(Color.WHITE);
        mainCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 235, 240)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form components with modern styling
        patientCombo = new JComboBox<>(loadPatients());
        styleComboBox(patientCombo);

        reportTypeCombo = new JComboBox<>(new String[]{"Blood Test", "X-Ray", "MRI", "Ultrasound"});
        styleComboBox(reportTypeCombo);

        descriptionArea = new JTextArea(5, 30);
        descriptionArea.setFont(NORMAL_FONT);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Drop zone with modern design
        JPanel dropPanel = new JPanel(new BorderLayout());
        dropPanel.setBackground(new Color(245, 247, 250));
        dropPanel.setBorder(BorderFactory.createDashedBorder(
                new Color(150, 160, 170), 2, 5, 5, false
        ));
        dropPanel.setPreferredSize(new Dimension(0, 200));

        dropZoneLabel = new JLabel("Drag & Drop PDF Here or Click to Browse", JLabel.CENTER);
        dropZoneLabel.setFont(NORMAL_FONT);
        dropZoneLabel.setForeground(new Color(100, 110, 120));
        dropPanel.add(dropZoneLabel, BorderLayout.CENTER);

        // Add click functionality to drop zone
        dropPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    currentPdfFile = fileChooser.getSelectedFile();
                    dropZoneLabel.setText(currentPdfFile.getName());
                    dropZoneLabel.setForeground(PRIMARY_COLOR);
                }
            }
        });

        configureDropTarget(dropPanel);

        // Layout components
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        mainCard.add(createFormLabel("Patient:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        mainCard.add(patientCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        mainCard.add(createFormLabel("Report Type:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        mainCard.add(reportTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        mainCard.add(createFormLabel("Description:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        mainCard.add(new JScrollPane(descriptionArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        mainCard.add(dropPanel, gbc);

        // Bottom panel with upload button and status
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(BACKGROUND_COLOR);

        uploadBtn = createSolidButton("Upload Report", PRIMARY_COLOR);
        uploadBtn.setPreferredSize(new Dimension(200, 45));
        uploadBtn.addActionListener(e -> uploadReport());

        statusLabel = new JLabel(" ", JLabel.CENTER);
        statusLabel.setFont(NORMAL_FONT);

        bottomPanel.add(uploadBtn);
        bottomPanel.add(statusLabel);

        panel.add(mainCard, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Helper method to style combo boxes
    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(NORMAL_FONT);
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(NORMAL_FONT);
        label.setForeground(DARK_COLOR);
        return label;
    }

    private JPanel createPatientPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = createPanelHeader("Patient Management", "View patient information");
        panel.add(headerPanel, BorderLayout.NORTH);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(BACKGROUND_COLOR);
        JTextField searchField = new JTextField(25);  // Increased size
        searchField.setFont(NORMAL_FONT);
        JButton searchBtn = createSolidButton("Search", PRIMARY_COLOR);

        // Create larger refresh button
        JButton refreshBtn = createSolidButton("Refresh", INFO_COLOR);
        refreshBtn.setPreferredSize(new Dimension(120, 30));  // Larger size
        refreshBtn.setFont(NORMAL_FONT.deriveFont(Font.BOLD));
        refreshBtn.setToolTipText("Reload all patient data");

        // Optional: Add refresh icon
        try {
            ImageIcon refreshIcon = new ImageIcon("resources/refresh.png"); // Your icon path
            refreshIcon = new ImageIcon(refreshIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            refreshBtn.setIcon(refreshIcon);
        } catch (Exception e) {
            // Icon not found, proceed without it
        }

        searchPanel.add(createFormLabel("Search Patients:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);

        // Patient Table with all columns from your database
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Name", "Email", "Phone", "Address", "Blood Group", "Gender", "Date of Birth"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable patientTable = new JTable(model);
        patientTable.setRowHeight(35);
        patientTable.setFont(NORMAL_FONT);
        patientTable.getTableHeader().setFont(TITLE_FONT);
        patientTable.setShowGrid(false);
        patientTable.setIntercellSpacing(new Dimension(0, 0));
        patientTable.setSelectionBackground(new Color(225, 240, 255));
        patientTable.setSelectionForeground(DARK_COLOR);
        patientTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Action listeners
        ActionListener searchAction = e -> loadPatientData(model, searchField.getText().trim());
        searchBtn.addActionListener(searchAction);
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadPatientData(model, "");
        });
        searchField.addActionListener(searchAction);

        // Initial data load
        loadPatientData(model, "");

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadPatientData(DefaultTableModel model, String searchQuery) {
        model.setRowCount(0); // Clear existing data

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
                    model.addRow(new Object[]{
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
            JOptionPane.showMessageDialog(null,
                    "Error loading patient data: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
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

    private void configureDropTarget(JPanel dropPanel) {
        new DropTarget(dropPanel, new DropTargetAdapter() {
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dtde.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> files = (List<File>) transferable.getTransferData(
                                DataFlavor.javaFileListFlavor);
                        if (files.size() == 1 && files.get(0).getName().toLowerCase().endsWith(".pdf")) {
                            currentPdfFile = files.get(0);
                            dropZoneLabel.setText(currentPdfFile.getName());
                            dropZoneLabel.setForeground(Color.BLUE);
                        } else {
                            dropZoneLabel.setText("Only single PDF files accepted");
                            dropZoneLabel.setForeground(Color.RED);
                        }
                    }
                } catch (Exception ex) {
                    dropZoneLabel.setText("Error: " + ex.getMessage());
                    dropZoneLabel.setForeground(Color.RED);
                }
            }
        });
    }

    private void uploadReport() {
        if (currentPdfFile == null) {
            statusLabel.setText("Please select a PDF file first!");
            statusLabel.setForeground(DANGER_COLOR);
            return;
        }

        Patient selectedPatient = (Patient) patientCombo.getSelectedItem();
        if (selectedPatient == null) {
            statusLabel.setText("Please select a patient!");
            statusLabel.setForeground(DANGER_COLOR);
            return;
        }

        String reportType = (String) reportTypeCombo.getSelectedItem();
        String description = descriptionArea.getText();
        UserSession.User currentUser = UserSession.getCurrentUser();

        try {
            // Read PDF file
            byte[] fileData = Files.readAllBytes(currentPdfFile.toPath());
            int fileSize = (int) currentPdfFile.length();

            // Prepare SQL query
            String sql = "INSERT INTO reports (patient_id, report_type, report_date, description, " +
                    "uploaded_by, uploaded_by_id, file_name, file_data, file_size, status) " +
                    "VALUES (?, ?, CURDATE(), ?, ?, ?, ?, ?, ?, 'Pending')";

            // Execute query
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, selectedPatient.getId());
                stmt.setString(2, reportType);
                stmt.setString(3, description);
                stmt.setString(4, currentUser.getUsername());
                stmt.setInt(5, currentUser.getUserId());
                stmt.setString(6, currentPdfFile.getName());
                stmt.setBytes(7, fileData);
                stmt.setInt(8, fileSize);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Success - update UI
                    statusLabel.setText("Report uploaded successfully!");
                    statusLabel.setForeground(SUCCESS_COLOR);

                    // Reset form
                    currentPdfFile = null;
                    dropZoneLabel.setText("Drag & Drop PDF Here or Click to Browse");
                    dropZoneLabel.setForeground(new Color(100, 110, 120));
                    descriptionArea.setText("");

                    // Refresh recent reports
                    loadRecentReports();

                    // Show success message
                    JOptionPane.showMessageDialog(this,
                            "Report uploaded successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("Failed to upload report!");
                    statusLabel.setForeground(DANGER_COLOR);
                }
            }
        } catch (IOException ex) {
            statusLabel.setText("Error reading PDF file: " + ex.getMessage());
            statusLabel.setForeground(DANGER_COLOR);
        } catch (SQLException ex) {
            statusLabel.setText("Database error: " + ex.getMessage());
            statusLabel.setForeground(DANGER_COLOR);
            ex.printStackTrace();
        } catch (Exception ex) {
            statusLabel.setText("Unexpected error: " + ex.getMessage());
            statusLabel.setForeground(DANGER_COLOR);
            ex.printStackTrace();
        } finally {
            // Re-enable upload button if it was disabled
            uploadBtn.setEnabled(true);
        }
    }

    private Patient[] loadPatients() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM patients")) {
            java.util.List<Patient> patients = new java.util.ArrayList<>();
            while (rs.next()) {
                patients.add(new Patient(rs.getInt("id"), rs.getString("name")));
            }
            return patients.toArray(new Patient[0]);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + ex.getMessage());
            return new Patient[0];
        }
    }



    private void loadRecentReports() {
        recentReportsModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT r.report_id, p.name, r.report_type, r.report_date, r.status " +
                             "FROM reports r JOIN patients p ON r.patient_id = p.id " +
                             "ORDER BY r.report_date DESC LIMIT 5")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            while (rs.next()) {
                recentReportsModel.addRow(new Object[]{
                        rs.getInt("report_id"),
                        rs.getString("name"),
                        rs.getString("report_type"),
                        dateFormat.format(rs.getDate("report_date")),
                        rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading recent reports: " + ex.getMessage());
        }
    }
    private void activateSidebarButton(JButton button) {
        if (activeSidebarButton != null) {
            activeSidebarButton.setBackground(SUCCESS_COLOR);
            activeSidebarButton.setFont(NORMAL_FONT);
        }
        activeSidebarButton = button;
        button.setBackground(PRIMARY_COLOR);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private int getReportCount(String status) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM reports WHERE status = ?")) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            return 0;
        }
    }






    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // Dispose of the ReporterPanel's window
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
            // Show the Connectpage
            SwingUtilities.invokeLater(() -> {
                ConnectPage connectPage = new ConnectPage(); // Instantiate the separate Connectpage class
                connectPage.setVisible(true); // Display the Connectpage
            });
        }
    }

    // Patient and UserSession classes remain the same
    public static class Patient {
        private int id;
        private String name;

        public Patient(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return name + " (ID: " + id + ")";
        }
    }

    public static class UserSession {
        private static User currentUser;
        private static final String DB_URL = "jdbc:mysql://localhost:3306/HMsystem";
        private static final String DB_USER = "root";
        private static final String DB_PASS = "Ashish030406";

        public static void login(int userId) throws SQLException {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT name, role FROM employees WHERE user_id = ?")) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    currentUser = new User(userId, rs.getString("name"), rs.getString("role"));
                } else {
                    throw new SQLException("User not found");
                }
            }
        }

        public static User getCurrentUser() {
            return currentUser;
        }

        public static class User {
            private int userId;
            private String username;
            private String role;

            public User(int userId, String username, String role) {
                this.userId = userId;
                this.username = username;
                this.role = role;
            }

            public int getUserId() { return userId; }
            public String getUsername() { return username; }
            public String getRole() { return role; }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Simulate login with user_id 106 (Priya Malhotra)
                UserSession.login(106);

                JFrame frame = new JFrame("Reporter Dashboard");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1280, 800);
                frame.add(new ReporterPanel("106"));
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Login failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}