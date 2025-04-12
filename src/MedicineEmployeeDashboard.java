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

public class MedicineEmployeeDashboard extends JFrame {
    private static final Color PRIMARY_COLOR = new Color(0, 120, 150);
    private static final Color SECONDARY_COLOR = new Color(200, 230, 240);
    private static final Color SUCCESS_COLOR = new Color(0, 100, 140);
    private static final Color DANGER_COLOR = new Color(230, 60, 80);
    private static final Color WARNING_COLOR = new Color(255, 180, 50);
    private static final Color INFO_COLOR = new Color(0, 160, 180);
    private static final Color LIGHT_COLOR = new Color(240, 242, 245);
    private static final Color DARK_COLOR = new Color(20, 40, 80);
    private static final Color BACKGROUND_COLOR = new Color(210, 235, 250);

    private static final Font HEADER_FONT = new Font("Montserrat", Font.BOLD, 20);
    private static final Font TITLE_FONT = new Font("Montserrat", Font.BOLD, 16);
    private static final Font NORMAL_FONT = new Font("Montserrat", Font.PLAIN, 14);
    private static final Font SMALL_FONT = new Font("Montserrat", Font.PLAIN, 12);

    private static final String DB_URL = "jdbc:mysql://localhost:3306/HMsystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Ashish030406";

    private String userId;
    private Connection connection;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private Map<String, String> employeeDetails = new HashMap<>();
    private JLabel currentTimeLabel;
    private Timer timeUpdateTimer;
    private JButton activeSidebarButton;



    public MedicineEmployeeDashboard(String userId) {
        initializeDB();
        this.userId = userId;
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadEmployeeDetails();
        initializeUI();
        startTimeUpdater();
        setLocationRelativeTo(null);
    }

    private void initializeDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void loadEmployeeDetails() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT name, email, phone FROM employees WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                employeeDetails.put("name", rs.getString("name"));
                employeeDetails.put("email", rs.getString("email"));
                employeeDetails.put("phone", rs.getString("phone"));
            }
        } catch (SQLException ex) {
            showError("Error loading employee details: " + ex.getMessage());
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
        setTitle("MediCare Hospital - Medicine Employee Portal");
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

        createHomePanel();
        createTimePunchPanel();
        createMedicineStorePanel();
        createMedicineTransactionsPanel();

        JButton homeButton = (JButton) ((JPanel) sidePanel.getComponent(1)).getComponent(0);
        activateSidebarButton(homeButton);

        setVisible(true);
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(SUCCESS_COLOR);
        statusBar.setPreferredSize(new Dimension(getWidth(), 40));
        statusBar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        JLabel hospitalLabel = new JLabel("MediCare Hospital Medicine Management System");
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
        sidePanel.setBackground(Color.WHITE);
        sidePanel.setPreferredSize(new Dimension(250, getHeight()));
        sidePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0, 80, 120)));

        JPanel profileSection = new JPanel();
        profileSection.setLayout(new BoxLayout(profileSection, BoxLayout.Y_AXIS));
        profileSection.setBackground(Color.WHITE);
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
                String initials = employeeDetails.containsKey("name") && employeeDetails.get("name").length() > 0
                        ? employeeDetails.get("name").substring(0, 1).toUpperCase()
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

        JLabel nameLabel = new JLabel(employeeDetails.get("name"));
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("Med Analyzer");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(Color.BLACK);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileSection.add(avatarPanel);
        profileSection.add(Box.createRigidArea(new Dimension(0, 10)));
        profileSection.add(nameLabel);
        profileSection.add(Box.createRigidArea(new Dimension(0, 5)));
        profileSection.add(roleLabel);

        sidePanel.add(profileSection);
        sidePanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        sidePanel.add(createSidebarButtonPanel("Dashboard", "home", "/icons/dashboard.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidePanel.add(createSidebarButtonPanel("Time Punch", "time", "/icons/clock.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidePanel.add(createSidebarButtonPanel("Medicine Store", "medicines", "/icons/medicine.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidePanel.add(createSidebarButtonPanel("Transactions", "transactions", "/icons/transaction.png"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidePanel.add(Box.createVerticalGlue());

        JPanel logoutPanel = createSidebarButtonPanel("Logout", "logout", "/icons/logout.png");
        logoutPanel.setBackground(WARNING_COLOR);

        MouseListener[] listeners = logoutPanel.getMouseListeners();
        for (MouseListener ml : listeners) {
            logoutPanel.removeMouseListener(ml);
        }

        JLabel logoutLabel = null;
        for (Component comp : logoutPanel.getComponents()) {
            if (comp instanceof JLabel && comp.getParent() == logoutPanel) {
                logoutLabel = (JLabel) comp;
                break;
            }
        }

        JLabel finalLogoutLabel = logoutLabel;
        logoutPanel.addMouseListener(new MouseAdapter() {
            private final Color originalBg = WARNING_COLOR;
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

        sidePanel.add(logoutPanel);
        return sidePanel;
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
            // Fallback to text-only
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
                        btn.setBackground(DARK_COLOR);
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

    private void createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel welcomePanel = new JPanel(new BorderLayout(15, 0));
        welcomePanel.setOpaque(false);

        JLabel profileIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(100, 170, 255));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                String initials = employeeDetails.get("name").substring(0, 1) +
                        (employeeDetails.get("name").contains(" ") ?
                                employeeDetails.get("name").substring(employeeDetails.get("name").indexOf(" ") + 1, employeeDetails.get("name").indexOf(" ") + 2) : "");
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initials)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initials, x, y);
                g2.dispose();
            }
        };
        profileIcon.setPreferredSize(new Dimension(50, 50));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back,");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeLabel.setForeground(new Color(120, 120, 120));

        JLabel nameLabel = new JLabel(employeeDetails.get("name"));
        nameLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 24));
        nameLabel.setForeground(new Color(50, 50, 50));

        textPanel.add(welcomeLabel);
        textPanel.add(nameLabel);

        welcomePanel.add(profileIcon, BorderLayout.WEST);
        welcomePanel.add(textPanel, BorderLayout.CENTER);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        JLabel dateLabel = new JLabel(dateFormat.format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(150, 150, 150));
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        headerPanel.add(welcomePanel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setOpaque(false);

        SwingWorker<Map<String, String>, Void> dataLoader = new SwingWorker<>() {
            @Override
            protected Map<String, String> doInBackground() {
                Map<String, String> data = new HashMap<>();
                data.put("lowStock", getLowStockMedicines());
                data.put("totalMedicines", getTotalMedicines());  // Fixed this line
                data.put("totalSuppliers", getTotalSuppliers());
                data.put("expiringSoon", getExpiringSoonMedicines());
                return data;
            }

            @Override
            protected void done() {
                try {
                    Map<String, String> data = get();

                    JPanel lowStockCard = createModernStatCard("Low Stock", data.get("lowStock"), new Color(255, 140, 50), "⚠");
                    JPanel totalMedicinesCard = createModernStatCard("Total Medicines", data.get("totalMedicines"), new Color(40, 180, 130), "💊");
                    JPanel totalSuppliersCard = createModernStatCard("Total Suppliers", data.get("totalSuppliers"), new Color(100, 170, 255), "🏭");
                    JPanel expiringSoonCard = createModernStatCard("Expiring Soon", data.get("expiringSoon"), new Color(220, 90, 120), "⏳");

                    statsPanel.removeAll();
                    statsPanel.add(lowStockCard);
                    statsPanel.add(totalMedicinesCard);
                    statsPanel.add(totalSuppliersCard);
                    statsPanel.add(expiringSoonCard);

                    statsPanel.revalidate();
                    statsPanel.repaint();
                } catch (Exception e) {
                    showError("Error loading dashboard data: " + e.getMessage());
                }
            }
        };
        dataLoader.execute();

        for (int i = 0; i < 4; i++) {
            statsPanel.add(createModernLoadingCard());
        }
        panel.add(statsPanel, BorderLayout.CENTER);

        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setOpaque(false);
        recentPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        JLabel sectionTitle = new JLabel("Recently Added Medicines");
        sectionTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(60, 60, 60));
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        recentPanel.add(sectionTitle, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Name", "Quantity", "Expiry Date", "Supplier"};
        Object[][] recentMedicines = getRecentMedicinesFromDatabase();

        JTable recentTable = new JTable(recentMedicines, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recentTable.setRowHeight(40);
        recentTable.setShowGrid(false);
        recentTable.setIntercellSpacing(new Dimension(0, 0));
        recentTable.setFillsViewportHeight(true);

        recentTable.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        recentTable.getTableHeader().setBackground(new Color(240, 242, 245));
        recentTable.getTableHeader().setForeground(new Color(100, 100, 100));
        recentTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        recentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(recentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        recentPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(recentPanel, BorderLayout.SOUTH);

        contentPanel.add(panel, "home");
    }

    private Object[][] getRecentMedicinesFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT id, name, quantity, expiry_date, supplier FROM medicines ORDER BY id DESC LIMIT 5";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getString("expiry_date"),
                        rs.getString("supplier")
                });
            }
            return rows.toArray(new Object[0][]);
        } catch (SQLException ex) {
            showError("Error fetching recent medicines: " + ex.getMessage());
            return new Object[0][5];
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

    private void createMedicineStorePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = createPanelHeader("Medicine Store", "Manage medicine inventory");
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBackground(BACKGROUND_COLOR);

        JTextField searchField = new JTextField(20);
        searchField.setFont(NORMAL_FONT);
        JButton searchButton = createSolidButton("Search", PRIMARY_COLOR);
        JButton addButton = createSolidButton("Add", SUCCESS_COLOR);
        JButton updateButton = createSolidButton("Update", WARNING_COLOR);
        JButton deleteButton = createSolidButton("Delete", DANGER_COLOR);
        JButton refreshButton = createSolidButton("Refresh", INFO_COLOR);

        controlPanel.add(new JLabel("Search:"));
        controlPanel.add(searchField);
        controlPanel.add(searchButton);
        controlPanel.add(addButton);
        controlPanel.add(updateButton);
        controlPanel.add(deleteButton);
        controlPanel.add(refreshButton);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Define the table model with the same columns as in the image
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Name", "Description", "Quantity", "Price", "Expiry Date", "Supplier"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // For the index column
                if (columnIndex == 3) return Integer.class; // Quantity
                if (columnIndex == 4) return Double.class;  // Price
                return String.class;
            }
        };

        // Load data into the table
        loadMedicineData(model);

        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                }
                return c;
            }
        };

        // Style the table
        table.setRowHeight(35);
        table.setFont(NORMAL_FONT);
        table.setShowGrid(false);

        table.setIntercellSpacing(new Dimension(0, 0));

        // Make column headers bold
        Font headerFont = TITLE_FONT.deriveFont(Font.BOLD);
        table.getTableHeader().setFont(headerFont);
        table.getTableHeader().setBackground(new Color(200, 200, 200)); // Light gray background for header
        table.getTableHeader().setForeground(Color.BLACK);

        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Set preferred column widths to match the image proportions
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(30);  // Index
        columnModel.getColumn(1).setPreferredWidth(150); // Name
        columnModel.getColumn(2).setPreferredWidth(200); // Description
        columnModel.getColumn(3).setPreferredWidth(80);  // Quantity
        columnModel.getColumn(4).setPreferredWidth(80);  // Price
        columnModel.getColumn(5).setPreferredWidth(100); // Expiry Date
        columnModel.getColumn(6).setPreferredWidth(150); // Supplier

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Event listeners
        searchButton.addActionListener(e -> performSearch(searchField.getText(), model, table));
        addButton.addActionListener(e -> addMedicine(model));
        updateButton.addActionListener(e -> updateMedicine(table, model));
        deleteButton.addActionListener(e -> deleteMedicine(table, model));
        refreshButton.addActionListener(e -> loadMedicineData(model));

        contentPanel.add(controlPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        this.contentPanel.add(panel, "medicines");
    }

    private void loadMedicineData(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM medicines")) {
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
            showError("Error loading medicine data: " + e.getMessage());
        }
    }

    private void performSearch(String searchText, DefaultTableModel model, JTable table) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        if (searchText.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

    private void addMedicine(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Add New Medicine", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        // Input panel with titled border
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PRIMARY_COLOR),
                        "Medicine Details", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                        TITLE_FONT, DARK_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fields
        JTextField nameField = createStyledTextField("Enter medicine name");
        JTextField descField = createStyledTextField("Enter description");
        JTextField qtyField = createStyledTextField("Enter quantity (e.g., 100)");
        JTextField priceField = createStyledTextField("Enter price (e.g., 10.99)");
        JTextField expiryField = createStyledTextField("YYYY-MM-DD");
        JTextField supplierField = createStyledTextField("Enter supplier name");

        // Add components
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(descField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(qtyField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Price ($):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Expiry Date:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(expiryField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        inputPanel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(supplierField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton saveButton = createSolidButton("Save", SUCCESS_COLOR);
        JButton cancelButton = createSolidButton("Cancel", DANGER_COLOR);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Action listeners
        saveButton.addActionListener(e -> {
            if (validateInputs(nameField.getText(), qtyField.getText(), priceField.getText(), expiryField.getText())) {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    String sql = "INSERT INTO medicines (name, description, quantity, price, expiry_date, supplier) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, nameField.getText());
                    stmt.setString(2, descField.getText());
                    stmt.setInt(3, Integer.parseInt(qtyField.getText()));
                    stmt.setDouble(4, Double.parseDouble(priceField.getText()));
                    stmt.setString(5, expiryField.getText());
                    stmt.setString(6, supplierField.getText());
                    stmt.executeUpdate();
                    loadMedicineData(model);
                    dialog.dispose();
                } catch (SQLException ex) {
                    showError("Error adding medicine: " + ex.getMessage());
                }
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateMedicine(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a medicine to update",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get existing values from the selected row
        int id = (int) model.getValueAt(row, 0);
        String currentName = model.getValueAt(row, 1).toString();
        String currentDesc = model.getValueAt(row, 2).toString();
        String currentQty = model.getValueAt(row, 3).toString();
        String currentPrice = model.getValueAt(row, 4).toString();
        String currentExpiry = model.getValueAt(row, 5).toString();
        String currentSupplier = model.getValueAt(row, 6).toString();

        JDialog dialog = new JDialog(this, "Update Medicine - ID: " + id, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PRIMARY_COLOR),
                        "Medicine Details", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                        TITLE_FONT, DARK_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize fields with current values
        JTextField nameField = createStyledTextField("Enter medicine name");
        nameField.setText(currentName);

        JTextField descField = createStyledTextField("Enter description");
        descField.setText(currentDesc);

        JTextField qtyField = createStyledTextField("Enter quantity (e.g., 100)");
        qtyField.setText(currentQty);

        JTextField priceField = createStyledTextField("Enter price (e.g., 10.99)");
        priceField.setText(currentPrice);

        JTextField expiryField = createStyledTextField("YYYY-MM-DD");
        expiryField.setText(currentExpiry);

        JTextField supplierField = createStyledTextField("Enter supplier name");
        supplierField.setText(currentSupplier);

        // Add fields to panel
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(descField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(qtyField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Price ($):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Expiry Date:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(expiryField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        inputPanel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(supplierField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton saveButton = createSolidButton("Update", SUCCESS_COLOR);
        JButton cancelButton = createSolidButton("Cancel", DANGER_COLOR);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> {
            if (validateInputs(nameField.getText(), qtyField.getText(), priceField.getText(), expiryField.getText())) {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    String sql = "UPDATE medicines SET name=?, description=?, quantity=?, price=?, expiry_date=?, supplier=? WHERE id=?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, nameField.getText());
                    stmt.setString(2, descField.getText());
                    stmt.setInt(3, Integer.parseInt(qtyField.getText()));
                    stmt.setDouble(4, Double.parseDouble(priceField.getText()));
                    stmt.setString(5, expiryField.getText());
                    stmt.setString(6, supplierField.getText());
                    stmt.setInt(7, id);
                    stmt.executeUpdate();
                    loadMedicineData(model);
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Medicine updated successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    showError("Error updating medicine: " + ex.getMessage());
                }
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteMedicine(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int id = (int) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this medicine?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    String sql = "DELETE FROM medicines WHERE id=?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    loadMedicineData(model);
                } catch (SQLException ex) {
                    showError("Error deleting medicine: " + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a medicine to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JTextField createStyledTextField(String tooltip) {
        JTextField field = new JTextField(20);
        field.setFont(NORMAL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        field.setToolTipText(tooltip);
        return field;
    }

    private boolean validateInputs(String name, String qty, String price, String expiry) {
        if (name.trim().isEmpty()) {
            showError("Medicine name cannot be empty");
            return false;
        }

        try {
            int quantity = Integer.parseInt(qty);
            if (quantity < 0) {
                showError("Quantity cannot be negative");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Quantity must be a valid number");
            return false;
        }

        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue < 0) {
                showError("Price cannot be negative");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Price must be a valid number");
            return false;
        }

        if (!expiry.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showError("Expiry date must be in YYYY-MM-DD format");
            return false;
        }

        return true;
    }

    private void createMedicineTransactionsPanel() {
        // Main panel with clean, modern spacing
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Modern header with icon
        JPanel headerPanel = createPanelHeader("Medicine Transactions", "Track inventory changes and movement");
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(BACKGROUND_COLOR);

        // Improved form panel layout - more structured and aligned
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Create clean, modern styled components
        JTextField medicineIdField = createStyledTextField();
        JTextField quantityField = createStyledTextField();

        // Dropdown for transaction type
        String[] transactionTypes = {"IN", "OUT"};
        JComboBox<String> transactionTypeCombo = new JComboBox<>(transactionTypes);
        transactionTypeCombo.setBackground(Color.WHITE);
        transactionTypeCombo.setFont(NORMAL_FONT);
        transactionTypeCombo.setPreferredSize(new Dimension(transactionTypeCombo.getPreferredSize().width, 30));


        JButton submitButton = createSolidButton("Submit Transaction", PRIMARY_COLOR);

        // GridBagLayout for precise control of form layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.weightx = 0.3;

        // Form title
        JLabel formTitle = new JLabel("Record New Transaction");
        formTitle.setFont(new Font(TITLE_FONT.getFontName(), Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 15, 5);
        inputPanel.add(formTitle, gbc);

        // Reset insets for form fields
        gbc.insets = new Insets(8, 5, 8, 5);

        // Medicine ID
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        JLabel medicineIdLabel = new JLabel("Medicine ID:");
        medicineIdLabel.setFont(new Font(NORMAL_FONT.getFontName(), Font.BOLD, 12));
        inputPanel.add(medicineIdLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        inputPanel.add(medicineIdField, gbc);

        // Quantity
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setFont(new Font(NORMAL_FONT.getFontName(), Font.BOLD, 12));
        inputPanel.add(quantityLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        inputPanel.add(quantityField, gbc);

        // Transaction Type
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        JLabel typeLabel = new JLabel("Transaction Type:");
        typeLabel.setFont(new Font(NORMAL_FONT.getFontName(), Font.BOLD, 12));
        inputPanel.add(typeLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        inputPanel.add(transactionTypeCombo, gbc);





        // Submit button - centered and with more space above
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(20, 5, 8, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(submitButton, gbc);

        // Enhanced table panel with cleaner borders
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        // Table model with action column
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Medicine ID", "Quantity", "Type", "Date", "Actions"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only actions column is editable
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 5 ? JButton.class : Object.class;
            }
        };
        loadTransactionData(model);

        // Modern table with improved styling
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(NORMAL_FONT);
        table.setShowGrid(false);
        table.setSelectionBackground(new Color(230, 240, 250));
        table.setSelectionForeground(Color.BLACK);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Set bold headers with improved styling and black text
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font(TITLE_FONT.getFontName(), Font.BOLD, 13));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.BLACK); // Changed to black
        header.setPreferredSize(new Dimension(header.getWidth(), 45));

        // Set centered text for all columns except the action column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Custom button renderer for the actions column
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

        // Set optimal column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        // Add zebra striping to table rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    comp.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 250));
                    comp.setForeground(Color.BLACK);
                }

                setHorizontalAlignment(SwingConstants.CENTER);
                return comp;
            }
        });

        // Clean, modern search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font(NORMAL_FONT.getFontName(), Font.BOLD, 12));

        JTextField searchField = createStyledTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.putClientProperty("JTextField.placeholderText", "Search transactions...");

        JButton searchButton = createSolidButton("Search", DARK_COLOR);
        searchButton.setPreferredSize(new Dimension(100, 30));

        // Refresh button with icon
        JButton refreshButton = createSolidButton("Refresh", new Color(255, 180, 50));
        refreshButton.setPreferredSize(new Dimension(100, 30));

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(Box.createHorizontalStrut(10));
        searchPanel.add(refreshButton);

        tablePanel.add(searchPanel, BorderLayout.NORTH);

        // Clean scrollpane for table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Filter and pagination controls in a cleaner layout
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(Color.WHITE);

        JLabel filterLabel = new JLabel("Filter by type:");
        filterLabel.setFont(new Font(NORMAL_FONT.getFontName(), Font.BOLD, 12));
        filterPanel.add(filterLabel);

        JComboBox<String> filterCombo = new JComboBox<>(new String[]{"All", "IN", "OUT"});
        filterCombo.setPreferredSize(new Dimension(100, 30));
        filterPanel.add(filterCombo);

        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        paginationPanel.setBackground(Color.WHITE);

        JLabel pageSizeLabel = new JLabel("Items per page:");
        pageSizeLabel.setFont(new Font(NORMAL_FONT.getFontName(), Font.BOLD, 12));
        paginationPanel.add(pageSizeLabel);

        JComboBox<Integer> pageSizeCombo = new JComboBox<>(new Integer[]{10, 25, 50});
        pageSizeCombo.setPreferredSize(new Dimension(70, 30));
        paginationPanel.add(pageSizeCombo);

        controlPanel.add(filterPanel, BorderLayout.WEST);
        controlPanel.add(paginationPanel, BorderLayout.EAST);
        tablePanel.add(controlPanel, BorderLayout.SOUTH);

        // Action listeners
        submitButton.addActionListener(e -> {
            String type = (String) transactionTypeCombo.getSelectedItem();
            recordTransaction(medicineIdField.getText(), quantityField.getText(),
                    type,model);
            medicineIdField.setText("");
            quantityField.setText("");

        });

        refreshButton.addActionListener(e -> loadTransactionData(model));

        searchButton.addActionListener(e -> searchTransactions(searchField.getText(), model));

        filterCombo.addActionListener(e -> filterTransactions((String) filterCombo.getSelectedItem(), model));

        pageSizeCombo.addActionListener(e -> {
            int pageSize = (Integer) pageSizeCombo.getSelectedItem();
            updatePageSize(pageSize, model);
        });

        // Split pane for responsive layout with improved proportions and gap
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(BACKGROUND_COLOR);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Add right padding for gap
        leftPanel.add(inputPanel, BorderLayout.NORTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BACKGROUND_COLOR);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Add left padding for gap
        rightPanel.add(tablePanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(350); // Slightly increased divider location for better spacing
        splitPane.setDividerSize(10); // Increased divider size for a visible gap
        splitPane.setBackground(BACKGROUND_COLOR);
        splitPane.setBorder(null);

        contentPanel.add(splitPane, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        this.contentPanel.add(panel, "transactions");
    }

    // Updated solid button instead of gradient
    private JButton createSolidButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(TITLE_FONT.getFontName(), Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 35));

        // Add hover and press effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(brighten(bgColor, 0.1f));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(darken(bgColor, 0.1f));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(brighten(bgColor, 0.1f));
            }
        });

        return button;
    }

    // Helper method for button color adjustments
    private Color brighten(Color color, float fraction) {
        int r = Math.min(255, (int)(color.getRed() * (1 + fraction)));
        int g = Math.min(255, (int)(color.getGreen() * (1 + fraction)));
        int b = Math.min(255, (int)(color.getBlue() * (1 + fraction)));
        return new Color(r, g, b);
    }

    // Helper method for button color adjustments
    private Color darken(Color color, float fraction) {
        int r = Math.max(0, (int)(color.getRed() * (1 - fraction)));
        int g = Math.max(0, (int)(color.getGreen() * (1 - fraction)));
        int b = Math.max(0, (int)(color.getBlue() * (1 - fraction)));
        return new Color(r, g, b);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(NORMAL_FONT);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return field;
    }

    // Updated button renderer for more solid look
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setBackground(SECONDARY_COLOR);
            setFont(new Font(NORMAL_FONT.getFontName(), Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText("View");
            return this;
        }
    }

    // Updated button editor for more solid look
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean isPushed;
        private JTable table;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setForeground(Color.WHITE);
            button.setBackground(SECONDARY_COLOR);
            button.setFont(new Font(NORMAL_FONT.getFontName(), Font.BOLD, 12));

            button.addActionListener(e -> {
                fireEditingStopped();
                viewTransactionDetails(selectedRow, table);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            button.setText("View");
            isPushed = true;
            this.table = table;
            this.selectedRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return "View";
        }
    }

    private void recordTransaction(String medicineId, String quantity, String type, DefaultTableModel model) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "INSERT INTO medicine_transactions (medicine_id, quantity, type, date, employee_id) VALUES (?, ?, ?, NOW(), ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(medicineId));
            stmt.setInt(2, Integer.parseInt(quantity));
            stmt.setString(3, type.toUpperCase());
            stmt.setString(4, userId);

            stmt.executeUpdate();

            String updateSql = "UPDATE medicines SET quantity = quantity " + (type.equalsIgnoreCase("IN") ? "+" : "-") + " ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, Integer.parseInt(quantity));
            updateStmt.setInt(2, Integer.parseInt(medicineId));
            updateStmt.executeUpdate();

            showSuccessNotification("Transaction recorded successfully!");
            loadTransactionData(model);
        } catch (SQLException ex) {
            showError("Error recording transaction: " + ex.getMessage());
        }
    }

    private void loadTransactionData(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM medicine_transactions WHERE employee_id = '" + userId + "' ORDER BY date DESC LIMIT 10")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("medicine_id"),
                        rs.getInt("quantity"),
                        rs.getString("type"),
                        dateFormat.format(rs.getTimestamp("date")),
                        "View"
                });
            }
        } catch (SQLException e) {
            showError("Error loading transaction data: " + e.getMessage());
        }
    }

    private void searchTransactions(String searchTerm, DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT * FROM medicine_transactions WHERE employee_id = ? AND " +
                    "(id LIKE ? OR medicine_id LIKE ? ) ORDER BY date DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            ResultSet rs = stmt.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("medicine_id"),
                        rs.getInt("quantity"),
                        rs.getString("type"),
                        dateFormat.format(rs.getTimestamp("date")),
                        "View"
                });
            }
        } catch (SQLException e) {
            showError("Error searching transactions: " + e.getMessage());
        }
    }

    private void filterTransactions(String filter, DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "All".equals(filter)
                    ? "SELECT * FROM medicine_transactions WHERE employee_id = ? ORDER BY date DESC LIMIT 10"
                    : "SELECT * FROM medicine_transactions WHERE employee_id = ? AND type = ? ORDER BY date DESC LIMIT 10";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            if (!"All".equals(filter)) {
                stmt.setString(2, filter);
            }

            ResultSet rs = stmt.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("medicine_id"),
                        rs.getInt("quantity"),
                        rs.getString("type"),
                        dateFormat.format(rs.getTimestamp("date")),
                        "View"
                });
            }
        } catch (SQLException e) {
            showError("Error filtering transactions: " + e.getMessage());
        }
    }

    private void updatePageSize(int pageSize, DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT * FROM medicine_transactions WHERE employee_id = ? ORDER BY date DESC LIMIT ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setInt(2, pageSize);

            ResultSet rs = stmt.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("medicine_id"),
                        rs.getInt("quantity"),
                        rs.getString("type"),
                        dateFormat.format(rs.getTimestamp("date")),
                        "View"
                });
            }
        } catch (SQLException e) {
            showError("Error updating page size: " + e.getMessage());
        }
    }

    private void viewTransactionDetails(int row, JTable table) {
        int transactionId = (int) table.getValueAt(row, 0);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT t.*, m.name as medicine_name, e.name as employee_name " +
                    "FROM medicine_transactions t " +
                    "JOIN medicines m ON t.medicine_id = m.id " +
                    "JOIN employees e ON t.employee_id = e.id " +
                    "WHERE t.id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
                String details = "<html><body style='width: 300px'>" +
                        "<h2 style='color:#4682B4'>Transaction Details</h2>" +
                        "<table>" +
                        "<tr><td><b>ID:</b></td><td>" + rs.getInt("id") + "</td></tr>" +
                        "<tr><td><b>Medicine:</b></td><td>" + rs.getString("medicine_name") + "</td></tr>" +
                        "<tr><td><b>Quantity:</b></td><td>" + rs.getInt("quantity") + "</td></tr>" +
                        "<tr><td><b>Type:</b></td><td>" + rs.getString("type") + "</td></tr>" +
                        "<tr><td><b>Date:</b></td><td>" + dateFormat.format(rs.getTimestamp("date")) + "</td></tr>" +
                        "<tr><td><b>Employee:</b></td><td>" + rs.getString("employee_name") + "</td></tr>" +

                        "</table>" +
                        "</body></html>";

                JOptionPane.showMessageDialog(
                        null,
                        new JLabel(details),
                        "Transaction Details",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException e) {
            showError("Error fetching transaction details: " + e.getMessage());
        }
    }

    private void showSuccessNotification(String message) {
        // Create a clean, modern toast notification
        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(60, 179, 113));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(new Font(NORMAL_FONT.getFontName(), Font.BOLD, 14));
        panel.add(label, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.pack();

        // Center on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - dialog.getWidth()) / 2;
        int y = screenSize.height - dialog.getHeight() - 50;
        dialog.setLocation(x, y);

        dialog.setVisible(true);

        // Auto-dismiss after 3 seconds
        new Timer(3000, e -> {
            dialog.dispose();
        }).start();
    }
    private JPanel createModernStatCard(String title, String value, Color color, String emoji) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        card.setUI(new RoundedPanelUI(12, color));

        JLabel emojiLabel = new JLabel(emoji, SwingConstants.CENTER);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

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
        return card;
    }

    private JPanel createModernLoadingCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setUI(new RoundedPanelUI(12, new Color(240, 240, 240)));
        return card;
    }

    private class RoundedPanelUI extends BasicPanelUI {
        private int radius;
        private Color accentColor;

        public RoundedPanelUI(int radius, Color accentColor) {
            this.radius = radius;
            this.accentColor = accentColor;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getBackground());
            g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), radius, radius);
            g2.dispose();
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

    private String getTotalMedicines() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT COUNT(*) FROM medicines";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            return rs.next() ? rs.getString(1) : "0";
        } catch (SQLException ex) {
            return "N/A";
        }
    }

    private String getTotalSuppliers() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT COUNT(DISTINCT supplier) FROM medicines";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            return rs.next() ? rs.getString(1) : "0";
        } catch (SQLException ex) {
            return "N/A";
        }
    }

    private String getExpiringSoonMedicines() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT COUNT(*) FROM medicines WHERE expiry_date <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            return rs.next() ? rs.getString(1) : "0";
        } catch (SQLException ex) {
            return "N/A";
        }
    }

//    private JButton createSolidButton(String text, Color color) {
//        JButton button = new JButton(text);
//        button.setFont(NORMAL_FONT);
//        button.setBackground(color);
//        button.setForeground(Color.WHITE);
//        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
//        button.setFocusPainted(false);
//        button.setOpaque(true);
//        button.setBorderPainted(false);
//        button.setContentAreaFilled(true);
//        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        return button;
//    }

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

    private void performLogout() {
        System.out.println("performLogout called, this: " + this.getClass().getName());
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println("User logged out");
            // Find and close the parent window
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                System.out.println("Disposing window: " + window.getClass().getName());
                window.dispose();
            } else {
                System.out.println("No parent window found");
                // Fallback: Try assuming 'this' is the frame
                if (this instanceof JFrame) {
                    System.out.println("Assuming this is a JFrame, disposing directly");
                    ((JFrame) this).dispose();
                } else {
                    System.out.println("This is not a JFrame, cannot dispose directly");
                }
            }
            // Open ConnectPage
            System.out.println("Attempting to open ConnectPage");
            try {
                ConnectPage connectPage = new ConnectPage();
                connectPage.setVisible(true);
                System.out.println("ConnectPage opened successfully");
            } catch (Exception e) {
                System.err.println("Error opening ConnectPage: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Logout cancelled");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MedicineEmployeeDashboard("103"));
    }
}