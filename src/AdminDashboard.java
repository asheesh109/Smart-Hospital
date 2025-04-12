import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import java.awt.geom.Arc2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminDashboard extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private Connection connection;
    private JLabel totalAppointmentsLabel;

    // Modern color palette
    private static final Color PRIMARY_COLOR = new Color(0, 120, 150);  // Teal Blue
    private static final Color SECONDARY_COLOR = new Color(200, 230, 240); // Light Teal Blue
    private static final Color SUCCESS_COLOR = new Color(0, 100, 140);  // Deep Teal Blue
    private static final Color DANGER_COLOR = new Color(230, 60, 80);   // Soft Red for alerts
    private static final Color WARNING_COLOR = new Color(255, 180, 50);  // Golden Yellow from logo
    private static final Color INFO_COLOR = new Color(0, 160, 180);      // Sky Blue variant
    private static final Color LIGHT_COLOR = new Color(240, 242, 245);  // Light Grayish White
    private static final Color DARK_COLOR = new Color(20, 40, 80);     // Darker Blue for contrast
    private static final Color BACKGROUND_COLOR = new Color(210, 235, 250); // Very Light Blue
// Very light blue background


    public AdminDashboard() {
        // Initialize database connection
        initializeDB();

        // Frame setup
        setTitle("Hospital Management System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Main panel with card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Create sidebar
        JPanel sidebar = createSidebar();

        // Create content panels
        mainPanel.add(createDashboardPanel(), "Dashboard");
        mainPanel.add(createPatientPanel(), "Patients");
        mainPanel.add(createDoctorPanel(), "Doctors");
        mainPanel.add(createEmployeePanel(), "Employees");
        mainPanel.add(createAppointmentPanel(), "Appointments");
        mainPanel.add(createMedicalPanel(), "Medical Store");

        // Add components to frame
        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);
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

    private JPanel createSidebar() {
        Color backgroundColor = new Color(235, 235, 235); // Light Grayish White for background
        Color primaryColor = new Color(0, 120, 150); // Teal-like Blue from logo
        Color hoverColor = new Color(0, 160, 180); // Lighter Blue for hover effect
        Color textColor = new Color(255, 255, 255); // White for better contrast
        Color accentColor = new Color(0, 120, 150); // Teal Blue for accents
        Color logoutColor = new Color(230, 160, 50); // Golden Yellow from logo for logout button


        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(backgroundColor);
        sidebar.setPreferredSize(new Dimension(260, getHeight()));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setBackground(backgroundColor);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 30, 25));

// Load the logo image
        ImageIcon logoIcon = new ImageIcon("./images/logo.png"); // Adjust path if needed
        Image scaledLogo = logoIcon.getImage().getScaledInstance(200, 230, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));

        logoPanel.add(logoLabel);
        sidebar.add(logoPanel);


        String[] menuItems = {"Dashboard", "Patients", "Doctors", "Employees", "Appointments", "Medical Store"};
        Icon[] icons = {
                createScaledIcon("/dashboard.png", 24, 24),
                createScaledIcon("/patient.png", 24, 24),
                createScaledIcon("/doctor.png", 24, 24),
                createScaledIcon("/employee.png", 24, 24),
                createScaledIcon("/appointment.png", 24, 24),
                createScaledIcon("/medicines.png", 24, 24)
        };

        for (int i = 0; i < menuItems.length; i++) {
            JButton menuButton = createStyledMenuButton(menuItems[i], icons[i], primaryColor, textColor, accentColor);
            final String panelName = menuItems[i];
            menuButton.addActionListener(e -> cardLayout.show(mainPanel, panelName));
            sidebar.add(menuButton);
            sidebar.add(Box.createVerticalStrut(5));
        }

        sidebar.add(Box.createVerticalGlue());
        JButton logoutButton = createLogoutButton("Logout", createScaledIcon("/logout.png", 24, 24), logoutColor, textColor);
        sidebar.add(logoutButton);
        return sidebar;
    }

    private JButton createStyledMenuButton(String text, Icon icon, Color bgColor, Color textColor, Color hoverColor) {
        JButton button = new JButton(text, icon);
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(220, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setIconTextGap(15);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(button.getBackground().darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private JButton createLogoutButton(String text, Icon icon, Color bgColor, Color textColor) {
        JButton logoutButton = new JButton(text, icon);
        logoutButton.setForeground(textColor);
        logoutButton.setBackground(bgColor);
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoutButton.setHorizontalAlignment(SwingConstants.CENTER);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        logoutButton.setFocusPainted(false);
        logoutButton.setMaximumSize(new Dimension(220, 50));
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setIconTextGap(15);
        logoutButton.setOpaque(true);
        logoutButton.setBorderPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutButton.addActionListener(e -> {
            // Show confirmation dialog
            int choice = JOptionPane.showConfirmDialog(
                    null, // or pass a parent component
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                // Close AdminDashboard
                Window adminDashboardWindow = SwingUtilities.getWindowAncestor((Component) e.getSource());
                adminDashboardWindow.dispose();

                // Open ConnectPage (login screen)
                SwingUtilities.invokeLater(() -> new ConnectPage().setVisible(true));
            }
            // If "NO", do nothing
        });
        logoutButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                logoutButton.setBackground(logoutButton.getBackground().darker());
            }
            public void mouseExited(MouseEvent e) {
                logoutButton.setBackground(bgColor);
            }
        });
        return logoutButton;
    }

    private ImageIcon createScaledIcon(String path, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(path));
            Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch (IOException e) {
            e.printStackTrace();
            return new ImageIcon(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        }
    }

    public JPanel createDashboardPanel() {
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(BACKGROUND_COLOR);
        dashboard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        dashboard.add(headerPanel, BorderLayout.NORTH);

        // Main Content Area
        JPanel mainContentPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        mainContentPanel.setBackground(BACKGROUND_COLOR);

        // Add different components to main content
        mainContentPanel.add(createStatsPanel());
        mainContentPanel.add(new CustomBarChart());
        mainContentPanel.add(new CustomPieChart());
        mainContentPanel.add(createRecentPatientsTable());

        dashboard.add(mainContentPanel, BorderLayout.CENTER);

        return dashboard;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);

        JLabel header = new JLabel("Hospital Management Dashboard", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(LIGHT_COLOR);

        // Add current date
        JLabel dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")), SwingConstants.CENTER);
        dateLabel.setForeground(SECONDARY_COLOR);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        headerPanel.add(header, BorderLayout.CENTER);
        headerPanel.add(dateLabel, BorderLayout.SOUTH);

        return headerPanel;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        statsPanel.setBackground(Color.white);

        try {
            // Mock data - replace with actual database queries
            int patients = getCount("SELECT COUNT(*) FROM patients");
            int doctors = getCount("SELECT COUNT(*) FROM doctors");
            int employees = getCount("SELECT COUNT(*) FROM employees");
            int appointments = getCount("SELECT COUNT(*) FROM appointments");
            int medicines = getCount("SELECT COUNT(*) FROM medicines");
            int departments = getCount("SELECT COUNT(DISTINCT specialization) FROM doctors");

            statsPanel.add(createStatCard("Patients", patients, INFO_COLOR));
            statsPanel.add(createStatCard("Doctors", doctors, SUCCESS_COLOR));
            statsPanel.add(createStatCard("Employees", employees, WARNING_COLOR));
            statsPanel.add(createStatCard("Appointments", appointments, PRIMARY_COLOR));
            statsPanel.add(createStatCard("Medicines", medicines, INFO_COLOR));
            statsPanel.add(createStatCard("Departments", departments, DANGER_COLOR));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to load dashboard data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Create title label
        // Create and add separate title label
        JLabel titleLabel = new JLabel("Stats Panel", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));  // Explicit font family
        titleLabel.setForeground(DARK_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
        titleLabel.setBackground(Color.WHITE);
        titleLabel.setOpaque(true);// Add some padding

        JPanel cardContainer = new JPanel(new BorderLayout());
        cardContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2), // White outer border
                BorderFactory.createEmptyBorder(10, 10, 10, 10) // Inner padding
        ));
        cardContainer.setBackground(Color.white);
        cardContainer.add(titleLabel, BorderLayout.NORTH);
        cardContainer.add(statsPanel, BorderLayout.CENTER);

        return cardContainer;
    }
    private JPanel createStatCard(String title, int count, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(LIGHT_COLOR);
        card.setBorder(BorderFactory.createLineBorder(color, 2));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(color);

        JLabel countLabel = new JLabel(String.valueOf(count), SwingConstants.CENTER);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        countLabel.setForeground(DARK_COLOR);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(countLabel, BorderLayout.CENTER);
        return card;
    }



    private JScrollPane createRecentPatientsTable() {
        // Column names
        String[] columnNames = {"ID", "Patient Name", "Contact", "Blood Type", "Gender", "Birth Date"};

        // Custom table model
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Integer.class : String.class;
            }
        };

        // Load data
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, phone, blood_group, gender, dob FROM patients ORDER BY id DESC LIMIT 15")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        formatPhoneNumber(rs.getString("phone")),
                        rs.getString("blood_group"),
                        rs.getString("gender"),
                        formatDate(rs.getDate("dob"))
                });
            }
        } catch (SQLException e) {
            showError("Failed to load patient data: " + e.getMessage());
        }

        // Create table
        JTable table = new JTable(model);

        // Table styling
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Selection style
        table.setSelectionBackground(new Color(225, 245, 254));
        table.setSelectionForeground(Color.BLACK);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        header.setBackground(new Color(250, 250, 250));
        header.setForeground(new Color(80, 80, 80));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));

        // Column renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Blood type styling
        DefaultTableCellRenderer bloodTypeRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(getFont().deriveFont(Font.BOLD));
                setHorizontalAlignment(CENTER);
                if (!isSelected) {
                    setForeground(new Color(0, 100, 0)); // Dark green
                }
                return this;
            }
        };

        // Apply renderers
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
        table.getColumnModel().getColumn(3).setCellRenderer(bloodTypeRenderer); // Blood type
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Gender

        // Column sizing
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(180); // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Contact
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Blood Type
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Gender
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Birth Date

        // Create title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Recent patient Admission", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));  // Explicit font family
        titleLabel.setForeground(DARK_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
        titleLabel.setBackground(Color.WHITE);
        titleLabel.setOpaque(true);// Add some padding
        titlePanel.add(titleLabel);

        // Create container panel for table and title
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        container.add(titlePanel, BorderLayout.NORTH);
        container.add(new JScrollPane(table), BorderLayout.CENTER);

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240)));

        return scrollPane;
    }

    // Helper methods remain the same
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.length() < 10) return phone;
        return phone.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "($1) $2-$3");
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
        return sdf.format(date);
    }
    // Custom Bar Chart Component
    private class CustomBarChart extends JPanel {
        public CustomBarChart() {
            setBackground(Color.WHITE);  // White background
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),  // Light gray border
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)    // Padding
            ));

            // Create and add separate title label
            JLabel titleLabel = new JLabel("Monthly Patient Admissions", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            setLayout(new BorderLayout());
            add(titleLabel, BorderLayout.NORTH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            int[] admissions = {45, 60, 55, 70, 65, 80};
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};

            int width = getWidth();
            int height = getHeight() - 40; // Account for title space
            int barWidth = width / (admissions.length * 2);
            int maxAdmission = 80; // Max value for scaling

            // Draw grid lines
            // Draw grid lines
            g2d.setColor(new Color(240, 240, 240)); // Very light gray grid
            for (int i = 0; i <= 4; i++) {
                int y = height - 30 - (i * (height - 50) / 4);
                g2d.drawLine(30, y, width - 10, y);

                // Y-axis labels
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString(String.valueOf(i * maxAdmission / 4), 5, y + 5);
                g2d.setColor(new Color(240, 240, 240)); // Reset to grid color
            }

            g2d.setColor(PRIMARY_COLOR);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw bars
            for (int i = 0; i < admissions.length; i++) {
                int barHeight = (int)((double)admissions[i] / maxAdmission * (height - 50));
                int x = 30 + i * (barWidth * 2) + barWidth/2;
                int y = height - barHeight - 30;

                // Bar with shadow effect
                GradientPaint gradient = new GradientPaint(x, y, PRIMARY_COLOR, x, y + barHeight, PRIMARY_COLOR.darker());
                g2d.setPaint(gradient);
                g2d.fillRoundRect(x, y, barWidth, barHeight, 5, 5);

                // Value label on top of bar
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.valueOf(admissions[i]), x + barWidth/2 - 5, y - 5);

                // Month labels
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString(months[i], x + barWidth/2 - 10, height - 10);
            }

            // X-axis line
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawLine(30, height - 30, width - 10, height - 30);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(400, 250); // Recommended size
        }
    }

    // Custom Pie Chart Component
    private class CustomPieChart extends JPanel {
        private int[] departmentData = {30, 20, 25, 15, 10};
        private Color[] departmentColors = {
                INFO_COLOR, SUCCESS_COLOR, WARNING_COLOR,
                DANGER_COLOR, SECONDARY_COLOR
        };
        private String[] departments = {"Cardiology", "Neurology", "Pediatrics", "Oncology", "Other"};

        public CustomPieChart() {
            setBackground(LIGHT_COLOR);
            setLayout(null); // Allow precise positioning
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Enable antialiasing for smoother rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int width = getWidth();
            int height = getHeight();
            int diameter = Math.min(width, height) - 100; // Increased margin
            int x = (width - diameter) / 2;
            int y = (height - diameter) / 2 + 30; // Shifted down to make space for title

            // Draw the title above the pie chart
            String title = "Department Distribution";
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Bold title font
            FontMetrics fm = g2d.getFontMetrics();
            int titleWidth = fm.stringWidth(title);
            g2d.setColor(DARK_COLOR);
            g2d.drawString(title, (width - titleWidth) / 2, 25); // Position title at the top center

            // Calculate total for percentage
            int total = 0;
            for (int value : departmentData) {
                total += value;
            }

            // Precise pie chart rendering
            int startAngle = 0;
            for (int i = 0; i < departmentData.length; i++) {
                double sweepAngle = (departmentData[i] / (double) total) * 360;

                // Draw pie slice
                g2d.setColor(departmentColors[i]);
                g2d.fill(new Arc2D.Double(x, y, diameter, diameter, startAngle, sweepAngle, Arc2D.PIE));

                // Calculate label position
                double midAngle = Math.toRadians(startAngle + sweepAngle / 2);
                int labelRadius = diameter / 2 + 20;

                int labelX = (int) (x + diameter / 2 + Math.cos(midAngle) * labelRadius);
                int labelY = (int) (y + diameter / 2 + Math.sin(midAngle) * labelRadius);

                // Prepare label with percentage
                double percentage = (departmentData[i] / (double) total) * 100;
                String label = String.format("%s (%.1f%%)", departments[i], percentage);

                // Adjust label position
                fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(label);
                if (midAngle > Math.PI / 2 && midAngle < 3 * Math.PI / 2) {
                    labelX -= textWidth;
                }

                // Draw label
                g2d.setColor(DARK_COLOR);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2d.drawString(label, labelX, labelY);

                // Draw connecting line
                g2d.setColor(new Color(150, 150, 150, 100));
                g2d.drawLine(
                        x + diameter / 2 + (int) (Math.cos(midAngle) * diameter / 2),
                        y + diameter / 2 + (int) (Math.sin(midAngle) * diameter / 2),
                        labelX,
                        labelY
                );

                startAngle += sweepAngle;
            }
        }
    }


    // Placeholder for database connection method
    private int getCount(String query) throws SQLException {
        // In a real application, this would connect to your database
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
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

        return panel;
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

        // Save button action listener with updated insert logic
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                if (usernameField.getText().isEmpty() || passwordField.getPassword().length == 0 ||
                        nameField.getText().isEmpty() || emailField.getText().isEmpty() ||
                        phoneField.getText().isEmpty() || dobField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Connection conn = connection; // Assuming connection is a field in your class
                conn.setAutoCommit(false); // Start transaction

                try (PreparedStatement patientsStmt = conn.prepareStatement(
                        "INSERT INTO patients (username, password, name, email, phone, address, blood_group, gender, dob, account_status, last_login, failed_attempts) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, 0)",
                        Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement usersStmt = conn.prepareStatement(
                             "INSERT INTO users (user_id, email, password, first_name, last_name, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())")) {

                    if (data == null) {
                        // Insert into patients table first
                        patientsStmt.setString(1, usernameField.getText());
                        patientsStmt.setString(2, new String(passwordField.getPassword()));
                        patientsStmt.setString(3, nameField.getText());
                        patientsStmt.setString(4, emailField.getText());
                        patientsStmt.setString(5, phoneField.getText());
                        patientsStmt.setString(6, addressField.getText());
                        patientsStmt.setString(7, bloodGroupCombo.getSelectedItem().toString());
                        patientsStmt.setString(8, genderCombo.getSelectedItem().toString());
                        patientsStmt.setString(9, dobField.getText());
                        patientsStmt.setString(10, "Active");
                        patientsStmt.executeUpdate();

                        // Get the generated patient_id
                        ResultSet generatedKeys = patientsStmt.getGeneratedKeys();
                        int patientId = -1;
                        if (generatedKeys.next()) {
                            patientId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Failed to retrieve generated patient_id");
                        }

                        // Insert into users table using patient_id as user_id
                        usersStmt.setInt(1, patientId);
                        usersStmt.setString(2, emailField.getText());
                        usersStmt.setString(3, new String(passwordField.getPassword()));
                        String[] nameParts = nameField.getText().split("\\s+", 2); // Split into first and last name
                        usersStmt.setString(4, nameParts.length > 0 ? nameParts[0] : "");
                        usersStmt.setString(5, nameParts.length > 1 ? nameParts[1] : "");
                        usersStmt.setString(6, "patient");
                        usersStmt.executeUpdate();
                    } else {
                        // Update existing patient
                        String updateSql = "UPDATE patients SET username=?, password=?, name=?, email=?, phone=?, address=?, blood_group=?, gender=?, dob=? WHERE id=?";
                        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                            stmt.setString(1, usernameField.getText());
                            stmt.setString(2, new String(passwordField.getPassword()));
                            stmt.setString(3, nameField.getText());
                            stmt.setString(4, emailField.getText());
                            stmt.setString(5, phoneField.getText());
                            stmt.setString(6, addressField.getText());
                            stmt.setString(7, bloodGroupCombo.getSelectedItem().toString());
                            stmt.setString(8, genderCombo.getSelectedItem().toString());
                            stmt.setString(9, dobField.getText());
                            stmt.setInt(10, (Integer) data.get(0));
                            stmt.executeUpdate();
                        }
                    }

                    conn.commit(); // Commit transaction
                    dialog.dispose();
                    refreshPatientTable(model);

                    // Improved success message
                    JOptionPane.showMessageDialog(
                            null,
                            data == null ? "Patient added successfully!" : "Patient updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (SQLException ex) {
                    conn.rollback(); // Rollback transaction on error
                    JOptionPane.showMessageDialog(dialog, "Error saving patient: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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
        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 14));

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


    private JPanel createDoctorPanel() {
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

        JLabel headerLabel = new JLabel("Doctor Management");
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
        searchField.putClientProperty("JTextField.placeholderText", "Search doctors...");

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
        model.setColumnIdentifiers(new String[]{"ID", "Name", "Specialization", "Email", "Phone", "Qualification"});

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
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getPreferredSize().width, 40));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Custom scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel actionPanel = new JPanel(new BorderLayout(0, 15));
        actionPanel.setBackground(BACKGROUND_COLOR);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Stats panel showing counts
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.setBackground(BACKGROUND_COLOR);

        JLabel totalDoctorsLabel = new JLabel("Total Doctors: 0");
        totalDoctorsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalDoctorsLabel.setForeground(DARK_COLOR);

        statsPanel.add(totalDoctorsLabel);

        // Action buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(BACKGROUND_COLOR);

        JButton addButton = createModernButton("Add Doctor", new Color(76, 175, 80), Color.WHITE);
        JButton editButton = createModernButton("Edit", new Color(255, 152, 0), Color.WHITE);
        JButton deleteButton = createModernButton("Delete", new Color(244, 67, 54), Color.WHITE);
        JButton refreshButton = createModernButton("Refresh", new Color(0, 120, 215), Color.WHITE);

        addButton.addActionListener(e -> showDoctorDialog(null, model));
        editButton.addActionListener(e -> editDoctor(table, model));
        deleteButton.addActionListener(e -> deleteDoctor(model, table));
        refreshButton.addActionListener(e -> {
            refreshTable(model, "SELECT id, name, specialization, email, phone, qualification FROM doctors LIMIT 100");
            totalDoctorsLabel.setText("Total Doctors: " + model.getRowCount());
        });

        buttonsPanel.add(refreshButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(addButton);

        actionPanel.add(statsPanel, BorderLayout.WEST);
        actionPanel.add(buttonsPanel, BorderLayout.EAST);

        // Load initial data
        try {
            loadTableData(model, "SELECT id, name, specialization, email, phone, qualification FROM doctors LIMIT 100");
            totalDoctorsLabel.setText("Total Doctors: " + model.getRowCount());
        } catch (SQLException e) {
            showError("Failed to load doctors: " + e.getMessage());
        }

        // Add search functionality
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            if (searchTerm.isEmpty()) {
                try {
                    loadTableData(model, "SELECT id, name, specialization, email, phone, qualification FROM doctors LIMIT 100");
                } catch (SQLException ex) {
                    showError("Failed to load doctors: " + ex.getMessage());
                }
            } else {
                try {
                    String query = "SELECT id, name, specialization, email, phone, qualification FROM doctors " +
                            "WHERE name LIKE ? OR specialization LIKE ? OR email LIKE ? LIMIT 100";
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
                        row.add(rs.getString("specialization"));
                        row.add(rs.getString("email"));
                        row.add(rs.getString("phone"));
                        row.add(rs.getString("qualification"));
                        model.addRow(row);
                    }

                    totalDoctorsLabel.setText("Total Doctors: " + model.getRowCount());
                } catch (SQLException ex) {
                    showError("Failed to search doctors: " + ex.getMessage());
                }
            }
        });

        // Add all components to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tableContainer, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }


    // Helper method to create modern buttons without icons
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
    private void showDoctorDialog(Vector<Object> data, DefaultTableModel model) {
        // Create dialog with modern look
        JDialog dialog = new JDialog(this, "Doctor Details", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 650);
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
        JLabel titleLabel = new JLabel("Doctor Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));
        headerPanel.add(titleLabel);

        // Form panel with grouped sections
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Personal information section
        JPanel personalPanel = createSectionPanel("Personal Information");
        JPanel personalFieldsPanel = new JPanel(new GridLayout(3, 2, 15, 10));

        JTextField nameField = createStyledTextField();
        JTextField emailField = createStyledTextField();
        JTextField phoneField = createStyledTextField();

        personalFieldsPanel.add(createLabelPanel("Full Name:"));
        personalFieldsPanel.add(nameField);
        personalFieldsPanel.add(createLabelPanel("Email:"));
        personalFieldsPanel.add(emailField);
        personalFieldsPanel.add(createLabelPanel("Phone:"));
        personalFieldsPanel.add(phoneField);
        personalPanel.add(personalFieldsPanel);

        // Professional information section
        JPanel professionalPanel = createSectionPanel("Professional Information");
        JPanel professionalFieldsPanel = new JPanel(new GridLayout(4, 2, 15, 10));

        JTextField specializationField = createStyledTextField();
        JTextField qualificationField = createStyledTextField();
        JTextField joiningDateField = createStyledTextField();
        JTextField addressField = createStyledTextField();

        professionalFieldsPanel.add(createLabelPanel("Specialization:"));
        professionalFieldsPanel.add(specializationField);
        professionalFieldsPanel.add(createLabelPanel("Qualification:"));
        professionalFieldsPanel.add(qualificationField);
        professionalFieldsPanel.add(createLabelPanel("Joining Date (YYYY-MM-DD):"));
        professionalFieldsPanel.add(joiningDateField);
        professionalFieldsPanel.add(createLabelPanel("Address:"));
        professionalFieldsPanel.add(addressField);
        professionalPanel.add(professionalFieldsPanel);

        // Add sections to form panel
        formPanel.add(personalPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(professionalPanel);

        // Scroll pane for form
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Populate fields if editing an existing doctor
        if (data != null && data.size() > 1) {
            nameField.setText(data.get(1).toString());
            specializationField.setText(data.get(2).toString());
            emailField.setText(data.get(3).toString());
            phoneField.setText(data.get(4).toString());
            addressField.setText(data.get(5).toString());
            qualificationField.setText(data.get(6).toString());
            joiningDateField.setText(data.get(7).toString());
        }

        // Buttons Panel with modern styling
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(189, 195, 199), Color.WHITE);

        JButton saveButton = new JButton("Save Doctor");
        styleButton(saveButton, new Color(41, 128, 185), Color.WHITE);

        // Save button action listener with updated insert logic
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                if (nameField.getText().isEmpty() || specializationField.getText().isEmpty() ||
                        emailField.getText().isEmpty() || phoneField.getText().isEmpty() ||
                        joiningDateField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Connection conn = connection; // Assuming connection is a field in your class
                conn.setAutoCommit(false); // Start transaction

                try (PreparedStatement usersStmt = conn.prepareStatement(
                        "INSERT INTO users (user_id,email, password, first_name, last_name, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())",
                        Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement doctorsStmt = conn.prepareStatement(
                             "INSERT INTO doctors (user_id, name, specialization, email, phone, address, qualification, joining_date, salary, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                    if (data == null) {
                        // Insert into users table
                        usersStmt.setString(1, emailField.getText());
                        usersStmt.setString(2, "doc123"); // Default password for doctors (adjust as needed)
                        String[] nameParts = nameField.getText().split("\\s+", 2); // Split into first and last name
                        usersStmt.setString(3, nameParts.length > 0 ? nameParts[0] : "");
                        usersStmt.setString(4, nameParts.length > 1 ? nameParts[1] : "");
                        usersStmt.setString(5, "doctor");
                        usersStmt.executeUpdate();

                        // Get the generated user_id
                        ResultSet generatedKeys = usersStmt.getGeneratedKeys();
                        int userId = -1;
                        if (generatedKeys.next()) {
                            userId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Failed to retrieve generated user_id");
                        }

                        // Insert into doctors table
                        doctorsStmt.setInt(1, userId);
                        doctorsStmt.setString(2, nameField.getText());
                        doctorsStmt.setString(3, specializationField.getText());
                        doctorsStmt.setString(4, emailField.getText());
                        doctorsStmt.setString(5, phoneField.getText());
                        doctorsStmt.setString(6, addressField.getText());
                        doctorsStmt.setString(7, qualificationField.getText());
                        doctorsStmt.setString(8, joiningDateField.getText());
                        doctorsStmt.setDouble(9, 150000.00); // Default salary (adjust as needed)
                        doctorsStmt.setString(10, "doc123"); // Default password (adjust as needed)
                        doctorsStmt.executeUpdate();
                    } else {
                        // Update existing doctor
                        String updateSql = "UPDATE doctors SET name=?, specialization=?, email=?, phone=?, address=?, qualification=?, joining_date=? WHERE id=?";
                        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                            stmt.setString(1, nameField.getText());
                            stmt.setString(2, specializationField.getText());
                            stmt.setString(3, emailField.getText());
                            stmt.setString(4, phoneField.getText());
                            stmt.setString(5, addressField.getText());
                            stmt.setString(6, qualificationField.getText());
                            stmt.setString(7, joiningDateField.getText());
                            stmt.setInt(8, (Integer) data.get(0));
                            stmt.executeUpdate();
                        }
                    }

                    conn.commit(); // Commit transaction
                    dialog.dispose();
                    refreshDoctorTable(model);

                    // Improved success message
                    JOptionPane.showMessageDialog(
                            null,
                            data == null ? "Doctor added successfully!" : "Doctor updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (SQLException ex) {
                    conn.rollback(); // Rollback transaction on error
                    JOptionPane.showMessageDialog(dialog, "Error saving doctor: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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

    // Helper method to refresh doctor table
    private void refreshDoctorTable(DefaultTableModel model) {
        model.setRowCount(0);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM doctors LIMIT 100")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("qualification"),
                        rs.getString("joining_date")
                });
            }
        } catch (SQLException e) {
            showError("Failed to load doctors: " + e.getMessage());
        }
    }
    private void editDoctor(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a doctor to edit",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Get the doctor ID from the first column
            int doctorId = (Integer) model.getValueAt(selectedRow, 0);

            // Fetch complete doctor data from database
            String query = "SELECT * FROM doctors WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, doctorId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Vector<Object> doctorData = new Vector<>();
                    doctorData.add(rs.getInt("id"));
                    doctorData.add(rs.getString("name"));
                    doctorData.add(rs.getString("specialization"));
                    doctorData.add(rs.getString("email"));
                    doctorData.add(rs.getString("phone"));
                    doctorData.add(rs.getString("address"));
                    doctorData.add(rs.getString("qualification"));
                    doctorData.add(rs.getString("joining_date"));

                    showDoctorDialog(doctorData, model);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading doctor data: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void deleteDoctor( DefaultTableModel model, JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            showError("Please select a doctor to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this doctor?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = (Integer) model.getValueAt(row, 0);
                try (PreparedStatement stmt = connection.prepareStatement(
                        "DELETE FROM doctors WHERE id = ?")) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    model.removeRow(row);
                    JOptionPane.showMessageDialog(this, "Doctor deleted successfully");
                }
            } catch (SQLException e) {
                showError("Failed to delete doctor: " + e.getMessage());
            }
        }
    }


    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Header Section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel titlePanel = new JPanel(new BorderLayout(15, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(5, 35));
        accentBar.setBackground(PRIMARY_COLOR);
        titlePanel.add(accentBar, BorderLayout.WEST);

        JLabel headerLabel = new JLabel("Employee Management");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerLabel.setForeground(DARK_COLOR);
        titlePanel.add(headerLabel, BorderLayout.CENTER);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(BACKGROUND_COLOR);

        JTextField searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(searchField.getPreferredSize().width, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search employees...");

        JButton searchButton = createModernButton("Search", new Color(7, 86, 154), Color.WHITE);

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);

        // Table Setup
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);

        tableContainer.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Updated Table Columns
        model.setColumnIdentifiers(new String[]{"ID", "Name", "Role", "Email", "Phone", "Joining Date", "Salary", "Address"});

        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setForeground(Color.black);
        table.setIntercellSpacing(new Dimension(10, 10));
        table.setSelectionBackground(new Color(165, 165, 234));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Renderer for Centered Text
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Role Column Styled Renderer
        DefaultTableCellRenderer roleRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setOpaque(true);
                label.setBackground(new Color(230, 240, 255));
                label.setForeground(PRIMARY_COLOR);
                label.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 255), 1, true));
                return label;
            }
        };

        // Apply Renderers
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 2) {
                table.getColumnModel().getColumn(i).setCellRenderer(roleRenderer);
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Stats & Actions Panel
        JPanel actionPanel = new JPanel(new BorderLayout(0, 15));
        actionPanel.setBackground(BACKGROUND_COLOR);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.setBackground(BACKGROUND_COLOR);

        // Create the label with initial value
        JLabel totalEmployeesLabel = new JLabel("Total Employees: 0");
        totalEmployeesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalEmployeesLabel.setForeground(DARK_COLOR);
        statsPanel.add(totalEmployeesLabel);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(BACKGROUND_COLOR);

        JButton addButton = createModernButton("Add Employee", new Color(76, 175, 80), Color.WHITE);
        JButton editButton = createModernButton("Edit", new Color(255, 152, 0), Color.WHITE);
        JButton deleteButton = createModernButton("Delete", new Color(244, 67, 54), Color.WHITE);
        JButton refreshButton = createModernButton("Refresh", new Color(0, 120, 215), Color.WHITE);

        addButton.addActionListener(e -> showEmployeeDialog(null, model, totalEmployeesLabel));
        editButton.addActionListener(e -> editEmployee(table, model, totalEmployeesLabel));
        deleteButton.addActionListener(e -> deleteEmployee(table, model, totalEmployeesLabel));
        refreshButton.addActionListener(e -> refreshTable(model, totalEmployeesLabel));

        buttonsPanel.add(refreshButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(addButton);

        actionPanel.add(statsPanel, BorderLayout.WEST);
        actionPanel.add(buttonsPanel, BorderLayout.EAST);

        // Load Initial Data
        refreshTable(model, totalEmployeesLabel);

        // Search Functionality
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            if (searchTerm.isEmpty()) {
                refreshTable(model, totalEmployeesLabel);
            } else {
                searchEmployees(model, searchTerm, totalEmployeesLabel);
            }
        });

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tableContainer, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Refresh Table Data
    private void refreshTable(DefaultTableModel model, JLabel totalEmployeesLabel) {
        model.setRowCount(0);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, role, email, phone, joining_date, salary, address FROM employees LIMIT 100")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("joining_date"),
                        rs.getDouble("salary"),
                        rs.getString("address")
                });
            }

            // Update the total employees label with the current row count
            totalEmployeesLabel.setText("Total Employees: " + model.getRowCount());

        } catch (SQLException e) {
            showError("Failed to load employees: " + e.getMessage());
        }
    }

    // Updated search method to include total count update
    private void searchEmployees(DefaultTableModel model, String searchTerm, JLabel totalEmployeesLabel) {
        model.setRowCount(0);
        try {
            String query = "SELECT id, name, role, email, phone, joining_date, salary, address " +
                    "FROM employees " +
                    "WHERE name LIKE ? OR role LIKE ? OR email LIKE ? " +
                    "LIMIT 100";

            PreparedStatement stmt = connection.prepareStatement(query);
            String pattern = "%" + searchTerm + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("joining_date"),
                        rs.getDouble("salary"),
                        rs.getString("address")
                });
            }

            // Update the total count after search
            totalEmployeesLabel.setText("Total Employees: " + model.getRowCount());

        } catch (SQLException e) {
            showError("Failed to search employees: " + e.getMessage());
        }
    }

    // Update the edit employee method to also update the count
    private void editEmployee(JTable table, DefaultTableModel model, JLabel totalEmployeesLabel) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            Vector<Object> rowData = new Vector<>();
            for (int i = 0; i < model.getColumnCount(); i++) {
                rowData.add(model.getValueAt(selectedRow, i));
            }
            showEmployeeDialog(rowData, model, totalEmployeesLabel);
        } else {
            JOptionPane.showMessageDialog(null, "Please select an employee to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Update the delete employee method to also update the count
    private void deleteEmployee(JTable table, DefaultTableModel model, JLabel totalEmployeesLabel) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (Integer) model.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete this employee?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    PreparedStatement stmt = connection.prepareStatement("DELETE FROM employees WHERE id = ?");
                    stmt.setInt(1, id);
                    int result = stmt.executeUpdate();

                    if (result > 0) {
                        model.removeRow(selectedRow);
                        // Update the count after deletion
                        totalEmployeesLabel.setText("Total Employees: " + model.getRowCount());
                        JOptionPane.showMessageDialog(null, "Employee deleted successfully");
                    }
                } catch (SQLException ex) {
                    showError("Failed to delete employee: " + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select an employee to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    private void showEmployeeDialog(Vector<Object> data, DefaultTableModel model, JLabel totalEmployeesLabel) {
        // Create dialog with modern look
        JDialog dialog = new JDialog(this, "Employee Details", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 600);
        dialog.setLocationRelativeTo(this);
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create main panel with better spacing
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header panel with title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Employee Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));
        headerPanel.add(titleLabel);

        // Form panel with grouped sections
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Personal information section
        JPanel personalPanel = createSectionPanel("Personal Information");
        JPanel personalFieldsPanel = new JPanel(new GridLayout(5, 2, 15, 10));

        JTextField userIdField = createStyledTextField();
        JTextField nameField = createStyledTextField();
        JTextField emailField = createStyledTextField();
        JTextField phoneField = createStyledTextField();
        JTextField addressField = createStyledTextField();

        personalFieldsPanel.add(createLabelPanel("User ID:"));
        personalFieldsPanel.add(userIdField);
        personalFieldsPanel.add(createLabelPanel("Full Name:"));
        personalFieldsPanel.add(nameField);
        personalFieldsPanel.add(createLabelPanel("Email:"));
        personalFieldsPanel.add(emailField);
        personalFieldsPanel.add(createLabelPanel("Phone:"));
        personalFieldsPanel.add(phoneField);
        personalFieldsPanel.add(createLabelPanel("Address:"));
        personalFieldsPanel.add(addressField);
        personalPanel.add(personalFieldsPanel);

        // Professional information section
        JPanel professionalPanel = createSectionPanel("Professional Information");
        JPanel professionalFieldsPanel = new JPanel(new GridLayout(4, 2, 15, 10));

        JTextField roleField = createStyledTextField();
        JTextField joiningDateField = createStyledTextField();
        JPasswordField passwordField = new JPasswordField();
        styleTextField(passwordField);
        JTextField salaryField = createStyledTextField();

        professionalFieldsPanel.add(createLabelPanel("Role:"));
        professionalFieldsPanel.add(roleField);
        professionalFieldsPanel.add(createLabelPanel("Joining Date (YYYY-MM-DD):"));
        professionalFieldsPanel.add(joiningDateField);
        professionalFieldsPanel.add(createLabelPanel("Password:"));
        professionalFieldsPanel.add(passwordField);
        professionalFieldsPanel.add(createLabelPanel("Salary:"));
        professionalFieldsPanel.add(salaryField);
        professionalPanel.add(professionalFieldsPanel);

        // Add sections to form panel
        formPanel.add(personalPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(professionalPanel);

        // Scroll pane for form
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Populate fields if editing an existing employee
        if (data != null && data.size() > 1) {
            userIdField.setText(data.get(1).toString());
            nameField.setText(data.get(2).toString());
            emailField.setText(data.get(3).toString());
            phoneField.setText(data.get(4).toString());
            addressField.setText(data.get(5).toString());
            roleField.setText(data.get(6).toString());
            joiningDateField.setText(data.get(7).toString());
            passwordField.setText(data.get(8).toString());
            salaryField.setText(data.get(9).toString());

            userIdField.setEnabled(false);
        } else {
            // Leave password and salary fields blank for new employees
            passwordField.setText("");
            salaryField.setText("");
        }

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(189, 195, 199), Color.WHITE);

        JButton saveButton = new JButton(data == null ? "Add Employee" : "Save Changes");
        styleButton(saveButton, new Color(41, 128, 185), Color.WHITE);

        // Save button action
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                if (userIdField.getText().isEmpty() || nameField.getText().isEmpty() ||
                        roleField.getText().isEmpty() || emailField.getText().isEmpty() ||
                        phoneField.getText().isEmpty() || joiningDateField.getText().isEmpty() ||
                        passwordField.getPassword().length == 0 || salaryField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Please fill all required fields",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate user_id is numeric
                try {
                    Integer.parseInt(userIdField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "User ID must be a number",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate salary format
                try {
                    Double.parseDouble(salaryField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Please enter a valid salary amount",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate date format
                if (!joiningDateField.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                    JOptionPane.showMessageDialog(dialog,
                            "Please enter date in YYYY-MM-DD format",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Prepare SQL query
                String sql;
                if (data == null) {
                    sql = "INSERT INTO employees (user_id, name, email, phone, address, role, joining_date, password, salary) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                } else {
                    sql = "UPDATE employees SET name=?, email=?, phone=?, address=?, role=?, joining_date=?, password=?, salary=? WHERE id=?";
                }

                try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    if (data == null) {
                        // Insert new employee
                        int paramIndex = 1;
                        stmt.setInt(paramIndex++, Integer.parseInt(userIdField.getText()));
                        stmt.setString(paramIndex++, nameField.getText());
                        stmt.setString(paramIndex++, emailField.getText());
                        stmt.setString(paramIndex++, phoneField.getText());
                        stmt.setString(paramIndex++, addressField.getText());
                        stmt.setString(paramIndex++, roleField.getText());
                        stmt.setString(paramIndex++, joiningDateField.getText());
                        stmt.setString(paramIndex++, new String(passwordField.getPassword()));
                        stmt.setDouble(paramIndex++, Double.parseDouble(salaryField.getText()));
                    } else {
                        // Update existing employee
                        int paramIndex = 1;
                        stmt.setString(paramIndex++, nameField.getText());
                        stmt.setString(paramIndex++, emailField.getText());
                        stmt.setString(paramIndex++, phoneField.getText());
                        stmt.setString(paramIndex++, addressField.getText());
                        stmt.setString(paramIndex++, roleField.getText());
                        stmt.setString(paramIndex++, joiningDateField.getText());
                        stmt.setString(paramIndex++, new String(passwordField.getPassword()));
                        stmt.setDouble(paramIndex++, Double.parseDouble(salaryField.getText()));
                        stmt.setInt(paramIndex++, (Integer) data.get(0)); // id
                    }

                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows > 0) {
                        dialog.dispose();
                        refreshEmployeeTable(model, totalEmployeesLabel);
                        JOptionPane.showMessageDialog(
                                null,
                                data == null ? "Employee added successfully!" : "Employee updated successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                }
            } catch (SQLException ex) {
                if (ex.getMessage().contains("Duplicate entry") && ex.getMessage().contains("user_id")) {
                    JOptionPane.showMessageDialog(dialog,
                            "User ID already exists. Please choose a different ID.",
                            "Duplicate User ID",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Error saving employee: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                ex.printStackTrace();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        // Assemble the dialog
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Set default button and make visible
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.setVisible(true);
    }

    private void styleTextField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        field.setPreferredSize(new Dimension(200, 30));
    }


    private void refreshEmployeeTable(DefaultTableModel model, JLabel totalEmployeesLabel) {
        model.setRowCount(0);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, role, email, phone, joining_date FROM employees LIMIT 100")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("joining_date")
                });
            }
            totalEmployeesLabel.setText("Total Employees: " + model.getRowCount());
        } catch (SQLException e) {
            showError("Failed to load employees: " + e.getMessage());
        }
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

        // Right side search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(BACKGROUND_COLOR);

        JTextField searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(searchField.getPreferredSize().width, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search appointments...");

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
                refreshAppointmentTable(model);
                totalAppointmentsLabel.setText("Total Appointments: " + model.getRowCount());
            } catch (Exception ex) {
                showError("Failed to refresh appointments: " + ex.getMessage());
            }
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
        refreshAppointmentTable(model);
        totalAppointmentsLabel.setText("Total Appointments: " + model.getRowCount());

        // Add search functionality
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            try {
                String query = "SELECT a.id, p.name AS patient, d.name AS doctor, " +
                        "DATE_FORMAT(a.date, '%Y-%m-%d') AS date, " +
                        "TIME_FORMAT(a.time, '%H:%i') AS time, a.status, a.description " +
                        "FROM appointments a " +
                        "LEFT JOIN patients p ON a.patient_id = p.id " +
                        "LEFT JOIN doctors d ON a.doctor_id = d.id " +
                        "WHERE p.name LIKE ? OR d.name LIKE ? OR a.date LIKE ? OR a.status LIKE ? " +
                        "OR a.description LIKE ? " +
                        "ORDER BY a.date DESC, a.time DESC " +
                        "LIMIT 100";

                PreparedStatement stmt = connection.prepareStatement(query);
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
                showError("Failed to search appointments: " + ex.getMessage() + "\nSQL State: " + ex.getSQLState());
                ex.printStackTrace();
            }
        });

        // Add all components to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tableContainer, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshAppointmentTable(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            String query = "SELECT a.id, p.name AS patient, d.name AS doctor, " +
                    "DATE_FORMAT(a.date, '%Y-%m-%d') AS date, " +
                    "TIME_FORMAT(a.time, '%H:%i') AS time, a.status, a.description " +
                    "FROM appointments a " +
                    "LEFT JOIN patients p ON a.patient_id = p.id " +
                    "LEFT JOIN doctors d ON a.doctor_id = d.id " +
                    "ORDER BY a.date DESC, a.time DESC " +
                    "LIMIT 100";

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
            showError("Failed to load appointments: " + e.getMessage() + "\nSQL State: " + e.getSQLState());
            e.printStackTrace();
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
                        refreshAppointmentTable(model);
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





    // Helper method to select combo box item by name
    private void selectComboItemByName(JComboBox<String> comboBox, String name) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String item = comboBox.getItemAt(i);
            if (item.contains(name)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }



    private JPanel createMedicalPanel() {
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

        JLabel headerLabel = new JLabel("Medical Store Management");
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
        searchField.putClientProperty("JTextField.placeholderText", "Search medicines...");

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
        model.setColumnIdentifiers(new String[]{"ID", "Name", "Description", "Quantity", "Price", "Expiry Date", "Supplier"});

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

        // Apply renderers
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Custom scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Add empty state panel (shown when no data)
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(Color.WHITE);
        JLabel emptyLabel = new JLabel("No medicines found", JLabel.CENTER);
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

        JLabel totalMedicinesLabel = new JLabel("Total Medicines: 0");
        totalMedicinesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalMedicinesLabel.setForeground(DARK_COLOR);

        statsPanel.add(totalMedicinesLabel);

        // Action buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(BACKGROUND_COLOR);

        // Modern styled buttons
        JButton addButton = createModernButton("Add Medicine", new Color(76, 175, 80), Color.WHITE);
        JButton editButton = createModernButton("Edit", new Color(255, 152, 0), Color.WHITE);
        JButton deleteButton = createModernButton("Delete", new Color(244, 67, 54), Color.WHITE);
        JButton refreshButton = createModernButton("Refresh", new Color(0, 120, 215), Color.WHITE);

        // Button actions
        addButton.addActionListener(e -> showMedicineDialog(null, model));

        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Vector<Object> rowData = new Vector<>();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    rowData.add(model.getValueAt(selectedRow, i));
                }
                showMedicineDialog(rowData, model);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a medicine to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (Integer) model.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(panel,
                        "Are you sure you want to delete this medicine?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        PreparedStatement stmt = connection.prepareStatement("DELETE FROM medicines WHERE id = ?");
                        stmt.setInt(1, id);
                        int result = stmt.executeUpdate();

                        if (result > 0) {
                            model.removeRow(selectedRow);
                            totalMedicinesLabel.setText("Total Medicines: " + model.getRowCount());
                            JOptionPane.showMessageDialog(panel, "Medicine deleted successfully");
                        }
                    } catch (SQLException ex) {
                        showError("Failed to delete medicine: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a medicine to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshButton.addActionListener(e -> {
            try {
                refreshTable(model, "SELECT * FROM medicines LIMIT 100");
                totalMedicinesLabel.setText("Total Medicines: " + model.getRowCount());
            } catch (Exception ex) {
                showError("Failed to refresh medicines: " + ex.getMessage());
            }
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
            loadTableData(model, "SELECT * FROM medicines LIMIT 100");
            totalMedicinesLabel.setText("Total Medicines: " + model.getRowCount());
        } catch (SQLException e) {
            showError("Failed to load medicines: " + e.getMessage());
        }

        // Add search functionality
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            if (searchTerm.isEmpty()) {
                try {
                    refreshTable(model, "SELECT * FROM medicines LIMIT 100");
                } catch (Exception ex) {
                    showError("Failed to load medicines: " + ex.getMessage());
                }
            } else {
                try {
                    String query = "SELECT * FROM medicines WHERE name LIKE ? OR description LIKE ? OR supplier LIKE ? LIMIT 100";

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
                        row.add(rs.getString("description"));
                        row.add(rs.getInt("quantity"));
                        row.add(rs.getDouble("price"));
                        row.add(rs.getString("expiry_date"));
                        row.add(rs.getString("supplier"));
                        model.addRow(row);
                    }

                    totalMedicinesLabel.setText("Total Medicines: " + model.getRowCount());
                } catch (SQLException ex) {
                    showError("Failed to search medicines: " + ex.getMessage());
                }
            }
        });

        // Add all components to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tableContainer, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }
    private void showMedicineDialog(Vector<Object> data, DefaultTableModel model) {
        // Create dialog with modern look
        JDialog dialog = new JDialog(this, "Medicine Details", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 550);
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
        JLabel titleLabel = new JLabel("Medicine Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));
        headerPanel.add(titleLabel);

        // Form panel with grouped sections
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Basic Information section
        JPanel basicInfoPanel = createSectionPanel("Basic Information");
        JPanel basicFieldsPanel = new JPanel(new GridLayout(2, 2, 15, 10));

        JTextField nameField = createStyledTextField();
        JTextArea descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        basicFieldsPanel.add(createLabelPanel("Name:"));
        basicFieldsPanel.add(nameField);
        basicFieldsPanel.add(createLabelPanel("Description:"));
        basicFieldsPanel.add(descriptionScroll);
        basicInfoPanel.add(basicFieldsPanel);

        // Inventory Details section
        JPanel inventoryPanel = createSectionPanel("Inventory Details");
        JPanel inventoryFieldsPanel = new JPanel(new GridLayout(2, 2, 15, 10));

        JTextField quantityField = createStyledTextField();
        JTextField priceField = createStyledTextField();

        inventoryFieldsPanel.add(createLabelPanel("Quantity:"));
        inventoryFieldsPanel.add(quantityField);
        inventoryFieldsPanel.add(createLabelPanel("Price:"));
        inventoryFieldsPanel.add(priceField);
        inventoryPanel.add(inventoryFieldsPanel);

        // Supplier Information section
        JPanel supplierPanel = createSectionPanel("Supplier Information");
        JPanel supplierFieldsPanel = new JPanel(new GridLayout(2, 2, 15, 10));

        JTextField expiryDateField = createStyledTextField();
        JTextField supplierField = createStyledTextField();

        supplierFieldsPanel.add(createLabelPanel("Expiry Date (YYYY-MM-DD):"));
        supplierFieldsPanel.add(expiryDateField);
        supplierFieldsPanel.add(createLabelPanel("Supplier:"));
        supplierFieldsPanel.add(supplierField);
        supplierPanel.add(supplierFieldsPanel);

        // Add sections to form panel
        formPanel.add(basicInfoPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(inventoryPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(supplierPanel);

        // Scroll pane for form
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Populate fields if editing existing medicine
        if (data != null && data.size() > 1) {
            nameField.setText(data.get(1).toString());
            descriptionArea.setText(data.get(2).toString());
            quantityField.setText(data.get(3).toString());
            priceField.setText(data.get(4).toString());
            expiryDateField.setText(data.get(5).toString());
            supplierField.setText(data.get(6).toString());
        }

        // Buttons Panel with modern styling
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(189, 195, 199), Color.WHITE);

        JButton saveButton = new JButton(data == null ? "Add Medicine" : "Save Changes");
        styleButton(saveButton, new Color(41, 128, 185), Color.WHITE);

        // Save button action listener
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                if (nameField.getText().isEmpty() || quantityField.getText().isEmpty() ||
                        priceField.getText().isEmpty() || expiryDateField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Parse numeric values
                int quantity = Integer.parseInt(quantityField.getText());
                double price = Double.parseDouble(priceField.getText());

                // Prepare SQL query
                String sql;
                if (data == null) {
                    sql = "INSERT INTO medicines (name, description, quantity, price, expiry_date, supplier) VALUES (?, ?, ?, ?, ?, ?)";
                } else {
                    sql = "UPDATE medicines SET name=?, description=?, quantity=?, price=?, expiry_date=?, supplier=? WHERE id=?";
                }

                try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, nameField.getText());
                    stmt.setString(2, descriptionArea.getText());
                    stmt.setInt(3, quantity);
                    stmt.setDouble(4, price);
                    stmt.setString(5, expiryDateField.getText());
                    stmt.setString(6, supplierField.getText());

                    if (data != null) {
                        stmt.setInt(7, (Integer) data.get(0));
                    }

                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows > 0) {
                        refreshMedicineTable(model);
                        dialog.dispose();

                        JOptionPane.showMessageDialog(
                                null,
                                data == null ? "Medicine added successfully!" : "Medicine updated successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for quantity and price", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving medicine: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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

    private void refreshMedicineTable(DefaultTableModel model) {
        model.setRowCount(0);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM medicines LIMIT 100")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("expiry_date"),
                        rs.getString("supplier")
                });
            }
        } catch (SQLException e) {
            showError("Failed to load medicines: " + e.getMessage());
        }
    }


    // Utility method to load comboboxes from database
    private void loadComboBox(JComboBox<String> comboBox, String query) throws SQLException {
        comboBox.removeAllItems();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                comboBox.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        }
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(DARK_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(PRIMARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(222, 226, 230));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
    }

    private JButton createActionButton(String text, Color color, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.addActionListener(listener);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void loadTableData(DefaultTableModel model, String query) throws SQLException {
        model.setRowCount(0); // Clear existing data

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                model.addRow(row);
            }
        }
    }

    private void refreshTable(DefaultTableModel model, String query) {
        try {
            loadTableData(model, query);
        } catch (SQLException e) {
            showError("Failed to refresh data: " + e.getMessage());
        }
    }





    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminDashboard dashboard = new AdminDashboard();
            dashboard.setVisible(true);
        });
    }

    @Override
    public void dispose() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.dispose();
    }
}