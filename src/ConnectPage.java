import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.border.EmptyBorder;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConnectPage extends JFrame {

    private JPanel mainPanel;
    private Color primaryColor = new Color(25, 118, 210); // Modern Blue
    private Color secondaryColor = new Color(245, 245, 245); // Light Gray
    private Color accentColor = new Color(66, 165, 245); // Lighter Blue
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 28);
    private Font subTitleFont = new Font("Segoe UI", Font.PLAIN, 16);
    private Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);
    private Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/HMsystem";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Ashish030406";

    // Admin credentials
    private static final String ADMIN_ID = "admin123";
    private static final String ADMIN_PASSWORD = "admin03";
    private ReporterPanel dashboard1;
    private JFrame dashboard;

    public ConnectPage() {
        setTitle("MediCare Plus - Hospital Management System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(1000, 600));

        // Main Panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(secondaryColor);

        // Header Panel
        JPanel headerPanel = createHeaderPanel();

        // Content Panel
        JPanel contentPanel = createContentPanel();

        // Footer Panel
        JPanel footerPanel = createFooterPanel();

        // Add panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);

        // Set a gradient background
        setBackgroundGradient();
    }

    private void setBackgroundGradient() {
        mainPanel.setOpaque(false);
        setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(245, 245, 245), 0, getHeight(), new Color(235, 235, 240));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        });
        ((JPanel) getContentPane()).setLayout(new BorderLayout());
        ((JPanel) getContentPane()).add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(primaryColor);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0, 0, 0, 50)),
                new EmptyBorder(20, 30, 20, 30)
        ));

        // Left side - Logo and Title
        JPanel logoTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoTitlePanel.setOpaque(false);

        ImageIcon logoIcon = createLogoIcon();
        JLabel logoLabel = new JLabel(logoIcon);

        JLabel titleLabel = new JLabel("MediCare Plus");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(Color.WHITE);

        logoTitlePanel.add(logoLabel);
        logoTitlePanel.add(Box.createHorizontalStrut(10));
        logoTitlePanel.add(titleLabel);

        // Right side - Back Button
        ModernButton backButton = new ModernButton("Back", new Color(44, 62, 80), Color.WHITE);
        backButton.addActionListener(e -> {
            // Create and display the HospitalLandingPage
            HospitalLandingPage landingPage = new HospitalLandingPage();
            landingPage.setVisible(true); // Ensure the landing page is visible
            this.dispose(); // Close the current ConnectPage
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(backButton);

        headerPanel.add(logoTitlePanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }



    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setPaint(new Color(0, 0, 0, 10));
                g2d.drawRect(3, 3, getWidth() - 7, getHeight() - 7);
                g2d.setPaint(new Color(0, 0, 0, 5));
                g2d.drawRect(2, 2, getWidth() - 5, getHeight() - 5);
            }
        };

        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(40, 60, 40, 60));
        contentPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        JLabel welcomeLabel = new JLabel("Welcome to MediCare Plus");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(new Color(44, 62, 80));

        JLabel instructionLabel = new JLabel("Please select your role to continue");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        instructionLabel.setForeground(new Color(44, 62, 80));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(welcomeLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 40, 0);
        contentPanel.add(instructionLabel, gbc);

        ModernRoleButton patientButton = new ModernRoleButton("Patient", createImageIcon("/images/patient_icon.png", new Color(46, 204, 113)));
        ModernRoleButton doctorButton = new ModernRoleButton("Doctor", createImageIcon("/images/doctor_icon.png", new Color(52, 152, 219)));
        ModernRoleButton employeeButton = new ModernRoleButton("Employee", createImageIcon("/images/employee_icon.png", new Color(230, 126, 34)));
        ModernRoleButton adminButton = new ModernRoleButton("Administrator", createImageIcon("/images/admin_icon.png", new Color(155, 89, 182)));

        patientButton.addActionListener(e -> openPatientPortal());
        doctorButton.addActionListener(e -> openDoctorLogin());
        employeeButton.addActionListener(e -> openEmployeeLogin());
        adminButton.addActionListener(e -> openAdminLogin());

        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 10, 0, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        contentPanel.add(patientButton, gbc);

        gbc.gridx = 1;
        contentPanel.add(doctorButton, gbc);

        gbc.gridx = 2;
        contentPanel.add(employeeButton, gbc);

        gbc.gridx = 3;
        contentPanel.add(adminButton, gbc);

        return contentPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(44, 62, 80));
        footerPanel.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel copyrightLabel = new JLabel("©️ 2025 MediCare Plus | All Rights Reserved");
        copyrightLabel.setForeground(Color.WHITE);
        copyrightLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        footerPanel.add(copyrightLabel);

        return footerPanel;
    }

    private ImageIcon createLogoIcon() {
        int size = 40;
        BufferedImage logoImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = logoImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradient = new GradientPaint(0, 0, Color.WHITE, size, size, new Color(240, 240, 240));
        g2d.setPaint(gradient);
        g2d.fillOval(0, 0, size, size);

        g2d.setColor(new Color(primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue(), 60));
        int crossWidth = 10;
        g2d.fillRoundRect((size - crossWidth) / 2 + 2, 7, crossWidth, size - 14, 4, 4);
        g2d.fillRoundRect(7, (size - crossWidth) / 2 + 2, size - 14, crossWidth, 4, 4);

        g2d.setColor(primaryColor);
        g2d.fillRoundRect((size - crossWidth) / 2, 5, crossWidth, size - 10, 4, 4);
        g2d.fillRoundRect(5, (size - crossWidth) / 2, size - 10, crossWidth, 4, 4);

        g2d.dispose();
        return new ImageIcon(logoImage);
    }

    private ImageIcon createImageIcon(String path, Color iconColor) {
        int size = 64;
        BufferedImage iconImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = iconImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String label = path.contains("patient") ? "P" : path.contains("doctor") ? "D" : path.contains("employee") ? "E" : "A";

        GradientPaint gradient = new GradientPaint(
                0, 0, iconColor,
                size, size, new Color(
                Math.min(255, iconColor.getRed() + 40),
                Math.min(255, iconColor.getGreen() + 40),
                Math.min(255, iconColor.getBlue() + 40)
        ));
        g2d.setPaint(gradient);
        g2d.fillOval(0, 0, size, size);

        g2d.setPaint(new Color(0, 0, 0, 30));
        g2d.drawOval(3, 3, size - 6, size - 6);

        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 32));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        int textHeight = fm.getHeight();
        g2d.drawString(label, (size - textWidth) / 2 + 1, (size + textHeight / 2) / 2 + 1);

        g2d.setColor(Color.WHITE);
        g2d.drawString(label, (size - textWidth) / 2, (size + textHeight / 2) / 2);

        g2d.dispose();
        return new ImageIcon(iconImage);
    }

    public JFrame getDashboard() {
        return dashboard;
    }

    public void setDashboard(JFrame dashboard) {
        this.dashboard = dashboard;
    }

    class ModernRoleButton extends JPanel {
        private String roleName;
        private ImageIcon roleIcon;
        private Color hoverColor = new Color(245, 245, 245);
        private boolean isHovered = false;
        private boolean isPressed = false;
        private ActionListener actionListener;
        private Color buttonColor;

        public ModernRoleButton(String roleName, ImageIcon roleIcon) {
            this.roleName = roleName;
            this.roleIcon = roleIcon;

            if (roleName.equals("Patient")) {
                buttonColor = new Color(46, 204, 113);
            } else if (roleName.equals("Doctor")) {
                buttonColor = new Color(52, 152, 219);
            } else if (roleName.equals("Employee")) {
                buttonColor = new Color(230, 126, 34);
            } else {
                buttonColor = new Color(155, 89, 182);
            }

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(224, 224, 224), 1, true),
                    BorderFactory.createEmptyBorder(25, 25, 25, 25)
            ));

            JLabel iconLabel = new JLabel(roleIcon);
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel nameLabel = new JLabel(roleName);
            nameLabel.setFont(buttonFont);
            nameLabel.setForeground(buttonColor);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            String description = "Access " + roleName.toLowerCase() + " features and services";
            JLabel descLabel = new JLabel(description);
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            descLabel.setForeground(Color.GRAY);
            descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            add(Box.createVerticalGlue());
            add(iconLabel);
            add(Box.createVerticalStrut(15));
            add(nameLabel);
            add(Box.createVerticalStrut(10));
            add(descLabel);
            add(Box.createVerticalGlue());

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    setBackground(hoverColor);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(buttonColor, 2, true),
                            BorderFactory.createEmptyBorder(24, 24, 24, 24)
                    ));
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    setBackground(Color.WHITE);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(224, 224, 224), 1, true),
                            BorderFactory.createEmptyBorder(25, 25, 25, 25)
                    ));
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    setBackground(new Color(235, 235, 235));
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    if (isHovered) {
                        setBackground(hoverColor);
                    } else {
                        setBackground(Color.WHITE);
                    }
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (actionListener != null) {
                        actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, roleName));
                    }
                }
            });
        }

        public void addActionListener(ActionListener listener) {
            this.actionListener = listener;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isPressed) {
                g2d.setColor(getBackground().darker());
                g2d.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 15, 15));
            } else if (isHovered) {
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fill(new RoundRectangle2D.Float(3, 3, getWidth() - 4, getHeight() - 4, 15, 15));
                g2d.setColor(getBackground());
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 2, 15, 15));
            } else {
                g2d.setColor(new Color(0, 0, 0, 5));
                g2d.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 2, getHeight() - 2, 15, 15));
                g2d.setColor(getBackground());
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
            }

            g2d.dispose();
            super.paintComponent(g);
        }
    }

    class ModernTextField extends JTextField {
        private String placeholder;
        private Color placeholderColor = new Color(180, 180, 180);
        private Color borderColor = new Color(220, 220, 220);
        private Color focusBorderColor = accentColor;
        private boolean isFocused = false;

        public ModernTextField(String placeholder, int columns) {
            super(columns);
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, borderColor),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            setFont(inputFont);

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 2, 0, focusBorderColor),
                            BorderFactory.createEmptyBorder(5, 8, 5, 8)
                    ));
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 2, 0, borderColor),
                            BorderFactory.createEmptyBorder(5, 8, 5, 8)
                    ));
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocused) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(placeholderColor);
                g2d.setFont(getFont());
                int padding = 8;
                g2d.drawString(placeholder, padding, g2d.getFontMetrics().getAscent() +
                        (getHeight() - g2d.getFontMetrics().getHeight()) / 2);
                g2d.dispose();
            }
        }
    }

    class ModernPasswordField extends JPasswordField {
        private String placeholder;
        private Color placeholderColor = new Color(180, 180, 180);
        private Color borderColor = new Color(220, 220, 220);
        private Color focusBorderColor = accentColor;
        private boolean isFocused = false;

        public ModernPasswordField(String placeholder, int columns) {
            super(columns);
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, borderColor),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            setFont(inputFont);

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 2, 0, focusBorderColor),
                            BorderFactory.createEmptyBorder(5, 8, 5, 8)
                    ));
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 2, 0, borderColor),
                            BorderFactory.createEmptyBorder(5, 8, 5, 8)
                    ));
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getPassword().length == 0 && !isFocused) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(placeholderColor);
                g2d.setFont(getFont());
                int padding = 8;
                g2d.drawString(placeholder, padding, g2d.getFontMetrics().getAscent() +
                        (getHeight() - g2d.getFontMetrics().getHeight()) / 2);
                g2d.dispose();
            }
        }
    }

    class ModernButton extends JButton {
        private Color buttonColor;
        private Color textColor;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public ModernButton(String text, Color buttonColor, Color textColor) {
            super(text);
            this.buttonColor = buttonColor;
            this.textColor = textColor;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(buttonFont);
            setForeground(textColor);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color currentColor = buttonColor;
            if (isPressed) {
                currentColor = new Color(
                        Math.max(0, buttonColor.getRed() - 30),
                        Math.max(0, buttonColor.getGreen() - 30),
                        Math.max(0, buttonColor.getBlue() - 30)
                );
            } else if (isHovered) {
                currentColor = new Color(
                        Math.min(255, buttonColor.getRed() + 20),
                        Math.min(255, buttonColor.getGreen() + 20),
                        Math.min(255, buttonColor.getBlue() + 20)
                );
            }

            g2d.setColor(currentColor);
            g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

            FontMetrics fm = g2d.getFontMetrics(getFont());
            Rectangle stringBounds = fm.getStringBounds(getText(), g2d).getBounds();

            int textX = (getWidth() - stringBounds.width) / 2;
            int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();

            g2d.setColor(textColor);
            g2d.setFont(getFont());
            g2d.drawString(getText(), textX, textY);

            g2d.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            return new Dimension(size.width + 20, size.height + 10);
        }
    }

    private JPanel createPatientLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("Patient Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(46, 204, 113));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Please enter your credentials to access patient services");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernTextField emailField = new ModernTextField("Enter your email", 20);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernPasswordField passwordField = new ModernPasswordField("Enter your password", 20);

        JCheckBox rememberMeBox = new JCheckBox("Remember me");
        rememberMeBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMeBox.setForeground(Color.GRAY);
        rememberMeBox.setBackground(Color.WHITE);

        JLabel forgotPasswordLabel = new JLabel("<html><u>Forgot password?</u></html>");
        forgotPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPasswordLabel.setForeground(accentColor);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ModernButton loginButton = new ModernButton("Login", new Color(46, 204, 113), Color.WHITE);
        loginButton.addActionListener(e -> {
            System.out.println("Login button clicked");
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            System.out.println("Attempting login with email: " + email);

            try {
                System.out.println("Connecting to database...");
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connection successful");

                PreparedStatement stmt = conn.prepareStatement("SELECT id FROM patients WHERE email = ? AND password = ?");
                stmt.setString(1, email);
                stmt.setString(2, password);
                System.out.println("Executing query...");
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int patientId = rs.getInt("id");
                    System.out.println("Patient found with ID: " + patientId);
                    try (PreparedStatement detailStmt = conn.prepareStatement(
                            "SELECT COUNT(*) FROM patient_detail WHERE patient_id = ?")) {
                        detailStmt.setInt(1, patientId);
                        ResultSet detailRs = detailStmt.executeQuery();
                        if (detailRs.next()) {
                            int count = detailRs.getInt(1);
                            System.out.println("Patient detail count: " + count);
                            if (count > 0) {
                                System.out.println("Opening ModernPatientPanel");
                                ModernPatientPanel patientFrame = new ModernPatientPanel(patientId);
                                patientFrame.setVisible(true);
                                dispose(); // Close ConnectPage
                            } else {
                                System.out.println("Navigating to patient detail panel");
                                mainPanel.removeAll();
                                mainPanel.add(createPatientDetailPanel(email, patientId), BorderLayout.CENTER);
                                mainPanel.revalidate();
                                mainPanel.repaint();
                            }
                        }
                    }
                } else {
                    System.out.println("No patient found for email: " + email);
                    JOptionPane.showMessageDialog(this,
                            "Invalid email or password. Please try again.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                    emailField.setText("");
                    passwordField.setText("");
                }
                conn.close();
            } catch (SQLException ex) {
                System.out.println("Database error occurred");
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Database error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(ConnectPage.this,
                        "Password reset functionality to be implemented",
                        "Forgot Password",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(emailLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(emailField, gbc);

        gbc.gridy = 2;
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = 3;
        formPanel.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.LINE_START;
        formPanel.add(rememberMeBox, gbc);

        gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(forgotPasswordLabel, gbc);

        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(loginButton, gbc);

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(formPanel);
        contentPanel.add(Box.createVerticalGlue());

        ModernButton backButton = new ModernButton("Back", new Color(44, 62, 80), Color.WHITE);
        backButton.addActionListener(e -> {
            mainPanel.removeAll();
            mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
            mainPanel.add(createContentPanel(), BorderLayout.CENTER);
            mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(10, 10, 0, 10));
        topPanel.add(backButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPatientDetailPanel(String email, int patientId) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("Patient Health Details");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(46, 204, 113));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Please enter your health information");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel weightLabel = new JLabel("Weight (kg)");
        weightLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernTextField weightField = new ModernTextField("Enter your weight", 20);

        JLabel heightLabel = new JLabel("Height (cm)");
        heightLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernTextField heightField = new ModernTextField("Enter your height", 20);

        JLabel bpLabel = new JLabel("Blood Pressure (mmHg)");
        bpLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernTextField bpField = new ModernTextField("e.g., 120/80", 20);

        JLabel bmiLabel = new JLabel("BMI");
        bmiLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernTextField bmiField = new ModernTextField("Enter your BMI", 20);

        JLabel sugarLabel = new JLabel("Blood Sugar (mg/dL)");
        sugarLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernTextField sugarField = new ModernTextField("Enter your blood sugar", 20);

        JLabel oxygenLabel = new JLabel("Oxygen Saturation (%)");
        oxygenLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernTextField oxygenField = new ModernTextField("Enter your oxygen rate", 20);

        JLabel rateLabel = new JLabel("Heart Rate (bpm)");
        rateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernTextField rateField = new ModernTextField("Enter your heart rate", 20);

        ModernButton submitButton = new ModernButton("Submit", new Color(46, 204, 113), Color.WHITE);
        submitButton.addActionListener(e -> {
            try {
                double weight = Double.parseDouble(weightField.getText());
                double height = Double.parseDouble(heightField.getText());
                String bp = bpField.getText();
                double bmi = Double.parseDouble(bmiField.getText());
                double sugar = Double.parseDouble(sugarField.getText());
                double oxygen = Double.parseDouble(oxygenField.getText());
                int rate = Integer.parseInt(rateField.getText());

                // Validations
                if (weight <= 0 || height <= 0 || bmi <= 0 || sugar <= 0 || oxygen <= 0 || rate <= 0) {
                    throw new NumberFormatException("Values must be positive.");
                }
                if (!bp.matches("\\d{2,3}/\\d{2,3}")) {
                    throw new IllegalArgumentException("Blood pressure must be in format like 120/80.");
                }
                if (height < 50 || height > 250) {
                    throw new IllegalArgumentException("Height must be between 50 and 250 cm.");
                }
                if (rate < 30 || rate > 200) {
                    throw new IllegalArgumentException("Heart rate must be between 30 and 200 bpm.");
                }

                // Insert into patient_detail
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                     PreparedStatement stmt = conn.prepareStatement(
                             "INSERT INTO patient_detail (patient_id, weight, height, bp, bpi, sugar, oxygen, rate, record_date) " +
                                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    stmt.setInt(1, patientId);
                    stmt.setDouble(2, weight);
                    stmt.setDouble(3, height);
                    stmt.setString(4, bp);
                    stmt.setDouble(5, bmi);
                    stmt.setDouble(6, sugar);
                    stmt.setDouble(7, oxygen);
                    stmt.setInt(8, rate);
                    stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(this,
                            "Health details submitted successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    System.out.println("Opening ModernPatientPanel after submitting details");
                    ModernPatientPanel patientFrame = new ModernPatientPanel(patientId);
                    patientFrame.setVisible(true);
                    dispose();// Close ConnectPage
                } catch (SQLException ex) {
                    System.out.println("Database error in submit: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Database error: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numeric values for weight, height, BMI, blood sugar, oxygen rate, and heart rate.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(weightLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(weightField, gbc);

        gbc.gridy = 2;
        formPanel.add(heightLabel, gbc);

        gbc.gridy = 3;
        formPanel.add(heightField, gbc);

        gbc.gridy = 4;
        formPanel.add(bpLabel, gbc);

        gbc.gridy = 5;
        formPanel.add(bpField, gbc);

        gbc.gridy = 6;
        formPanel.add(bmiLabel, gbc);

        gbc.gridy = 7;
        formPanel.add(bmiField, gbc);

        gbc.gridy = 8;
        formPanel.add(sugarLabel, gbc);

        gbc.gridy = 9;
        formPanel.add(sugarField, gbc);

        gbc.gridy = 10;
        formPanel.add(oxygenLabel, gbc);

        gbc.gridy = 11;
        formPanel.add(oxygenField, gbc);

        gbc.gridy = 12;
        formPanel.add(rateLabel, gbc);

        gbc.gridy = 13;
        formPanel.add(rateField, gbc);

        gbc.gridy = 14;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(submitButton, gbc);

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(formPanel);
        contentPanel.add(Box.createVerticalGlue());

        ModernButton backButton = new ModernButton("Back", new Color(44, 62, 80), Color.WHITE);
        backButton.addActionListener(e -> {
            mainPanel.removeAll();
            mainPanel.add(createPatientLoginPanel(), BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(10, 10, 0, 10));
        topPanel.add(backButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDoctorLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("Doctor Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(52, 152, 219));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Please enter your credentials to access doctor services");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel userIdLabel = new JLabel("User ID");
        userIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernTextField userIdField = new ModernTextField("Enter your user ID", 20);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernPasswordField passwordField = new ModernPasswordField("Enter your password", 20);

        JCheckBox rememberMeBox = new JCheckBox("Remember me");
        rememberMeBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMeBox.setForeground(Color.GRAY);
        rememberMeBox.setBackground(Color.WHITE);

        JLabel forgotPasswordLabel = new JLabel("<html><u>Forgot password?</u></html>");
        forgotPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPasswordLabel.setForeground(accentColor);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ModernButton loginButton = new ModernButton("Login", new Color(52, 152, 219), Color.WHITE);
        loginButton.addActionListener(e -> {
            String userIdText = userIdField.getText().trim();
            String password = new String(passwordField.getPassword());
            try {
                int userId = Integer.parseInt(userIdText);
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                     PreparedStatement stmt = conn.prepareStatement("SELECT name FROM doctors WHERE user_id = ? AND password = ?")) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, password);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String name = rs.getString("name");
                        JOptionPane.showMessageDialog(this,
                                "Login successful! Welcome, " + name + "!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                         //Open DoctorDashboard
                        DoctorDashboard doctorDashboard = new DoctorDashboard(userId);
                        doctorDashboard.setVisible(true);
                        dispose(); // Close ConnectPage
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Invalid user ID or password. Please try again.",
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                        userIdField.setText("");
                        passwordField.setText("");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Database error: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid user ID.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                userIdField.setText("");
                passwordField.setText("");
            }
        });

        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(ConnectPage.this,
                        "Password reset functionality to be implemented for Doctor",
                        "Forgot Password",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(userIdLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(userIdField, gbc);

        gbc.gridy = 2;
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = 3;
        formPanel.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.LINE_START;
        formPanel.add(rememberMeBox, gbc);

        gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(forgotPasswordLabel, gbc);

        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(loginButton, gbc);

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(formPanel);
        contentPanel.add(Box.createVerticalGlue());

        ModernButton backButton = new ModernButton("Back", new Color(44, 62, 80), Color.WHITE);
        backButton.addActionListener(e -> {
            mainPanel.removeAll();
            mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
            mainPanel.add(createContentPanel(), BorderLayout.CENTER);
            mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(10, 10, 0, 10));
        topPanel.add(backButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }
    private JPanel createEmployeeLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("Employee Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(230, 126, 34));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Please enter your credentials to access employee services");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel userIdLabel = new JLabel("User ID");
        userIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernTextField userIdField = new ModernTextField("Enter your user ID", 20);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernPasswordField passwordField = new ModernPasswordField("Enter your password", 20);

        JCheckBox rememberMeBox = new JCheckBox("Remember me");
        rememberMeBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMeBox.setForeground(Color.GRAY);
        rememberMeBox.setBackground(Color.WHITE);

        JLabel forgotPasswordLabel = new JLabel("<html><u>Forgot password?</u></html>");
        forgotPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPasswordLabel.setForeground(accentColor);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ModernButton loginButton = new ModernButton("Login", new Color(230, 126, 34), Color.WHITE);
        loginButton.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (userId.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a user ID.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                // Validate that userId is numeric (since employees table expects an integer user_id)
                Integer.parseInt(userId); // This ensures userId is a valid number
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                     PreparedStatement stmt = conn.prepareStatement("SELECT name, role FROM employees WHERE user_id = ? AND password = ?")) {
                    stmt.setString(1, userId); // Treat as String for query
                    stmt.setString(2, password);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String name = rs.getString("name");
                        String role = rs.getString("role");
                        JOptionPane.showMessageDialog(this,
                                "Login successful! Welcome, " + name + "!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);

                        // Switch to the appropriate dashboard based on role
                        switch (role) {
                            case "Receptionist":
                                dashboard = new ReceptionistDashboard(userId);

                                break;
                            case "Senior Nurse":
                                dashboard = new NurseDashboard(userId);

                                break;
                            case "Med Analyzer":
                                dashboard = new MedicineEmployeeDashboard(userId);

                                break;
                            case "Reporter":
                                dashboard1 = new ReporterPanel(userId);
                                SwingUtilities.invokeLater(() -> {
                                    JFrame reporterFrame = new JFrame("Reporter Dashboard");
                                    reporterFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                    reporterFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                                    reporterFrame.add(dashboard1);
                                    reporterFrame.setVisible(true);
                                });
                                dispose(); // Close the current frame (e.g., ConnectPage)
                                break;
                            default:
                                JOptionPane.showMessageDialog(this,
                                        "Unknown role: " + role,
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                        }
                        // Dispose of the current frame after switching panels
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Invalid user ID or password. Please try again.",
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                        userIdField.setText("");
                        passwordField.setText("");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Database error: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid numeric user ID.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                userIdField.setText("");
                passwordField.setText("");
            }
        });

        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(ConnectPage.this,
                        "Password reset functionality to be implemented for Employee",
                        "Forgot Password",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(userIdLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(userIdField, gbc);

        gbc.gridy = 2;
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = 3;
        formPanel.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.LINE_START;
        formPanel.add(rememberMeBox, gbc);

        gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(forgotPasswordLabel, gbc);

        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(loginButton, gbc);

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(formPanel);
        contentPanel.add(Box.createVerticalGlue());

        ModernButton backButton = new ModernButton("Back", new Color(44, 62, 80), Color.WHITE);
        backButton.addActionListener(e -> {
            mainPanel.removeAll();
            mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
            mainPanel.add(createContentPanel(), BorderLayout.CENTER);
            mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(10, 10, 0, 10));
        topPanel.add(backButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    // Helper method to switch panels
    private void switchPanel(JPanel newPanel) {
        if (mainPanel != null) {
            mainPanel.removeAll();
            mainPanel.add(newPanel, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        } else {
            System.err.println("mainPanel is null. Ensure it is initialized.");
        }
    }

    private JPanel createAdminLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("Administrator Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(155, 89, 182));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Please enter your credentials to access admin services");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel idLabel = new JLabel("Admin ID");
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernTextField idField = new ModernTextField("Enter your admin ID", 20);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernPasswordField passwordField = new ModernPasswordField("Enter your password", 20);

        JCheckBox rememberMeBox = new JCheckBox("Remember me");
        rememberMeBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMeBox.setForeground(Color.GRAY);
        rememberMeBox.setBackground(Color.WHITE);

        JLabel forgotPasswordLabel = new JLabel("<html><u>Forgot password?</u></html>");
        forgotPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPasswordLabel.setForeground(accentColor);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ModernButton loginButton = new ModernButton("Login", new Color(155, 89, 182), Color.WHITE);
        loginButton.addActionListener(e -> {
            String adminId = idField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (adminId.equals(ADMIN_ID) && password.equals(ADMIN_PASSWORD)) {
                JOptionPane.showMessageDialog(this,
                        "Login successful! Welcome, Administrator!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Dispose the current login window
                Window loginWindow = SwingUtilities.getWindowAncestor((Component) e.getSource());
                loginWindow.dispose();

                // Open AdminDashboard in a new window
                SwingUtilities.invokeLater(() -> {
                    AdminDashboard adminDashboard = new AdminDashboard();
                    adminDashboard.setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid admin ID or password. Please try again.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                idField.setText("");
                passwordField.setText("");
            }
        });

        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(ConnectPage.this,
                        "Password reset functionality to be implemented for Administrator",
                        "Forgot Password",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(idLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(idField, gbc);

        gbc.gridy = 2;
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = 3;
        formPanel.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.LINE_START;
        formPanel.add(rememberMeBox, gbc);

        gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(forgotPasswordLabel, gbc);

        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(loginButton, gbc);

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(formPanel);
        contentPanel.add(Box.createVerticalGlue());

        ModernButton backButton = new ModernButton("Back", new Color(44, 62, 80), Color.WHITE);
        backButton.addActionListener(e -> {
            mainPanel.removeAll();
            mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
            mainPanel.add(createContentPanel(), BorderLayout.CENTER);
            mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(10, 10, 0, 10));
        topPanel.add(backButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createUserIdLoginPanel(String role, Color roleColor, String tableName, String serviceDescription) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel(role + " Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(roleColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Please enter your credentials to access " + serviceDescription);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel userIdLabel = new JLabel("User ID");
        userIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernTextField userIdField = new ModernTextField("Enter your user ID", 20);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModernPasswordField passwordField = new ModernPasswordField("Enter your password", 20);

        JCheckBox rememberMeBox = new JCheckBox("Remember me");
        rememberMeBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMeBox.setForeground(Color.GRAY);
        rememberMeBox.setBackground(Color.WHITE);

        JLabel forgotPasswordLabel = new JLabel("<html><u>Forgot password?</u></html>");
        forgotPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPasswordLabel.setForeground(accentColor);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ModernButton loginButton = new ModernButton("Login", roleColor, Color.WHITE);
        loginButton.addActionListener(e -> {
            String userIdText = userIdField.getText().trim();
            String password = new String(passwordField.getPassword());
            try {
                int userId = Integer.parseInt(userIdText);
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                     PreparedStatement stmt = conn.prepareStatement("SELECT name FROM " + tableName + " WHERE user_id = ? AND password = ?")) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, password);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String name = rs.getString("name");
                        JOptionPane.showMessageDialog(this,
                                "Login successful! Welcome, " + name + "!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        // TODO: Open respective dashboard
                        mainPanel.removeAll();
                        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
                        mainPanel.add(createContentPanel(), BorderLayout.CENTER);
                        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);
                        mainPanel.revalidate();
                        mainPanel.repaint();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Invalid user ID or password. Please try again.",
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                        userIdField.setText("");
                        passwordField.setText("");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Database error: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid user ID.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                userIdField.setText("");
                passwordField.setText("");
            }
        });

        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(ConnectPage.this,
                        "Password reset functionality to be implemented for " + role,
                        "Forgot Password",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(userIdLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(userIdField, gbc);

        gbc.gridy = 2;
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = 3;
        formPanel.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.LINE_START;
        formPanel.add(rememberMeBox, gbc);

        gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(forgotPasswordLabel, gbc);

        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(loginButton, gbc);

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(formPanel);
        contentPanel.add(Box.createVerticalGlue());

        ModernButton backButton = new ModernButton("Back", new Color(44, 62, 80), Color.WHITE);
        backButton.addActionListener(e -> {
            mainPanel.removeAll();
            mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
            mainPanel.add(createContentPanel(), BorderLayout.CENTER);
            mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(10, 10, 0, 10));
        topPanel.add(backButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private void openPatientPortal() {
        mainPanel.removeAll();
        mainPanel.add(createPatientLoginPanel(), BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void openDoctorLogin() {
        mainPanel.removeAll();
        mainPanel.add(createDoctorLoginPanel(), BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void openEmployeeLogin() {
        mainPanel.removeAll();
        mainPanel.add(createEmployeeLoginPanel(), BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void openAdminLogin() {
        mainPanel.removeAll();
        mainPanel.add(createAdminLoginPanel(), BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                ConnectPage frame = new ConnectPage();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}