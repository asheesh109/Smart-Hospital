import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class HospitalManagementLogin extends JFrame {
    private JPanel mainPanel;
    private JPanel loginPanel;
    private CardLayout cardLayout;

    // Sample user credentials (in a real application, these would be in a database)
    private HashMap<String, String> doctorCredentials = new HashMap<>();
    private HashMap<String, String> adminCredentials = new HashMap<>();
    private HashMap<String, String> employeeCredentials = new HashMap<>();
    private HashMap<String, String> patientCredentials = new HashMap<>();

    public HospitalManagementLogin() {
        // Set up the frame
        setTitle("Hospital Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        // Initialize sample credentials
        initializeCredentials();

        // Create the main panel with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create the user selection panel
        JPanel selectionPanel = createSelectionPanel();

        // Create login panels for each user type
        JPanel doctorLoginPanel = createLoginPanel("Doctor");
        JPanel adminLoginPanel = createLoginPanel("Admin");
        JPanel employeeLoginPanel = createLoginPanel("Employee");
        JPanel patientLoginPanel = createLoginPanel("Patient");

        // Add panels to card layout
        mainPanel.add(selectionPanel, "selection");
        mainPanel.add(doctorLoginPanel, "doctor");
        mainPanel.add(adminLoginPanel, "admin");
        mainPanel.add(employeeLoginPanel, "employee");
        mainPanel.add(patientLoginPanel, "patient");

        // Add the main panel to the frame
        add(mainPanel);

        // Show the frame
        setVisible(true);
    }

    private void initializeCredentials() {
        // Sample credentials - in a real application, these would come from a database
        doctorCredentials.put("doc123", "password");
        doctorCredentials.put("dr.smith", "medic2025");

        adminCredentials.put("admin", "admin123");
        adminCredentials.put("superadmin", "hospital2025");

        employeeCredentials.put("nurse1", "staff123");
        employeeCredentials.put("lab001", "labtech");

        patientCredentials.put("patient001", "pat123");
        patientCredentials.put("john.doe", "patient2025");
    }

    private JPanel createSelectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Hospital logo and title
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));

        JLabel titleLabel = new JLabel("HOSPITAL MANAGEMENT SYSTEM");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // User selection buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel selectLabel = new JLabel("Select Your Role:");
        selectLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        buttonPanel.add(selectLabel, gbc);

        String[] userTypes = {"Doctor", "Admin", "Employee", "Patient"};
        Color[] buttonColors = {
                new Color(46, 204, 113),  // Doctor - Green
                new Color(231, 76, 60),   // Admin - Red
                new Color(241, 196, 15),  // Employee - Yellow
                new Color(52, 152, 219)   // Patient - Blue
        };

        for (int i = 0; i < userTypes.length; i++) {
            final String userType = userTypes[i].toLowerCase();
            JButton button = new JButton(userTypes[i] + " Login");
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setBackground(buttonColors[i]);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setPreferredSize(new Dimension(200, 60));

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cardLayout.show(mainPanel, userType);
                }
            });

            gbc.gridx = i % 2;
            gbc.gridy = (i / 2) + 1;
            gbc.gridwidth = 1;
            buttonPanel.add(button, gbc);
        }

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(41, 128, 185));
        JLabel footerLabel = new JLabel("© 2025 Hospital Management System");
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);

        // Add components to the panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLoginPanel(final String userType) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));

        JButton backButton = new JButton("← Back");
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "selection");
            }
        });

        JLabel titleLabel = new JLabel(userType + " Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Login form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setBackground(Color.WHITE);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameLabel.setBounds(250, 80, 100, 25);

        final JTextField usernameField = new JTextField();
        usernameField.setBounds(250, 110, 300, 35);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordLabel.setBounds(250, 160, 100, 25);

        final JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(250, 190, 300, 35);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBounds(350, 260, 100, 40);
        loginButton.setBackground(new Color(41, 128, 185));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setBounds(250, 320, 300, 25);

        // Add action listener to login button
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                boolean loginSuccess = authenticateUser(userType, username, password);

                if (loginSuccess) {
                    statusLabel.setText("Login successful!");
                    statusLabel.setForeground(new Color(46, 204, 113));

                    // Show a success message and redirect to the respective dashboard
                    // In a real application, this would launch the specific user interface
                    JOptionPane.showMessageDialog(panel,
                            "Welcome to Hospital Management System!\nYou are logged in as " + userType,
                            "Login Successful",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Clear the fields for security
                    usernameField.setText("");
                    passwordField.setText("");

                    // Return to selection screen (in a real app, would go to dashboard)
                    cardLayout.show(mainPanel, "selection");
                } else {
                    statusLabel.setText("Invalid username or password!");
                    statusLabel.setForeground(new Color(231, 76, 60));
                }
            }
        });

        // Add components to the form panel
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(loginButton);
        formPanel.add(statusLabel);

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(41, 128, 185));
        JLabel footerLabel = new JLabel("© 2025 Hospital Management System");
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);

        // Add components to the panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private boolean authenticateUser(String userType, String username, String password) {
        HashMap<String, String> credentials;

        switch (userType.toLowerCase()) {
            case "doctor":
                credentials = doctorCredentials;
                break;
            case "admin":
                credentials = adminCredentials;
                break;
            case "employee":
                credentials = employeeCredentials;
                break;
            case "patient":
                credentials = patientCredentials;
                break;
            default:
                return false;
        }

        return credentials.containsKey(username) && credentials.get(username).equals(password);
    }

    public static void main(String[] args) {
        // Set the look and feel to the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new HospitalManagementLogin();
            }
        });
    }
}
