import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class HospitalLandingPage extends JFrame {

    // Modern color scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color ACCENT_COLOR = new Color(46, 204, 113);
    private final Color BACKGROUND_COLOR = new Color(240, 245, 250);
    private final Color TEXT_COLOR = new Color(44, 62, 80);
    private final Color CARD_COLOR = new Color(255, 255, 255);

    // Font settings
    private final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 36);
    private final Font SUBHEADING_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private final Font CARD_TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);

    // Background image
    private Image backgroundImage;

    public HospitalLandingPage() {
        // Set up the frame
        setTitle("MediCare Plus - Modern Hospital Management System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load background image
        try {
            backgroundImage = new ImageIcon("./images/background.jpg").getImage();
        } catch (Exception e) {
            System.out.println("Background image not found, using fallback design");
            backgroundImage = null;
        }

        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        // Create main content panel
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    g2d.dispose();
                }
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Create header
        JPanel headerPanel = createHeader();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Create scrollable content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Add hero section
        contentPanel.add(createHeroSection());

        // Add features section
        contentPanel.add(createFeaturesSection());

        // Add additional content sections
        contentPanel.add(createStatsSection());
        contentPanel.add(createTestimonialsSection());

        // Add footer
        JPanel footerPanel = createFooter();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // Wrap content panel in another panel for proper centering
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.add(contentPanel, BorderLayout.NORTH);

        scrollPane.setViewportView(contentWrapper);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR, getWidth(), 0, SECONDARY_COLOR);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));

        // Logo and title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));

        JLabel logoLabel = createModernLogo();
        JLabel titleLabel = new JLabel("MEDICARE PLUS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        titlePanel.add(logoLabel);
        titlePanel.add(Box.createHorizontalStrut(15));
        titlePanel.add(titleLabel);

        // Navigation menu
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navPanel.setOpaque(false);
        navPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));


        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createHeroSection() {
        JPanel heroPanel = new JPanel();
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        heroPanel.setOpaque(false);
        heroPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 60, 60));
        heroPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 500));

        // Semi-transparent overlay
        JPanel overlayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
            }
        };
        overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));
        overlayPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        overlayPanel.setOpaque(false);

        // Main heading
        JLabel mainHeading = new JLabel("Advanced Hospital Management System");
        mainHeading.setFont(HEADING_FONT);
        mainHeading.setForeground(PRIMARY_COLOR);
        mainHeading.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subheading
        JLabel subHeading = new JLabel("Streamline your healthcare operations with our comprehensive solution");
        subHeading.setFont(SUBHEADING_FONT);
        subHeading.setForeground(TEXT_COLOR);
        subHeading.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Get Started button
        JButton getStartedBtn = createModernButton("GET STARTED", ACCENT_COLOR);
        getStartedBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        getStartedBtn.addActionListener(e -> {
            // Create and display the ConnectPage
            ConnectPage connectPage = new ConnectPage();
            connectPage.setVisible(true); // Ensure the ConnectPage is visible
            this.dispose(); // Close the current window (e.g., HospitalLandingPage)
        });




        // Add components with spacing
        overlayPanel.add(Box.createVerticalStrut(20));
        overlayPanel.add(mainHeading);
        overlayPanel.add(Box.createVerticalStrut(15));
        overlayPanel.add(subHeading);
        overlayPanel.add(Box.createVerticalStrut(30));
        overlayPanel.add(getStartedBtn);
        overlayPanel.add(Box.createVerticalStrut(20));

        heroPanel.add(overlayPanel);
        return heroPanel;
    }

    private JPanel createFeaturesSection() {
        JPanel featuresPanel = new JPanel();
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setOpaque(false);
        featuresPanel.setBorder(BorderFactory.createEmptyBorder(0, 60, 60, 60));

        // Section title
        JLabel sectionTitle = new JLabel("OUR FEATURES");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        sectionTitle.setForeground(PRIMARY_COLOR);
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // Features cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 30, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setMaximumSize(new Dimension(1200, 400));

        addFeatureCard(cardsPanel, "Patients", "Efficiently manage patient records and medical history");
        addFeatureCard(cardsPanel, "Appointments", "Easy booking and tracking of appointments");
        addFeatureCard(cardsPanel, "Employees", "Easy employee management for better experience");

        featuresPanel.add(sectionTitle);
        featuresPanel.add(cardsPanel);
        return featuresPanel;
    }

    private JPanel createStatsSection() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 60, 60, 60));

        // Section title
        JLabel sectionTitle = new JLabel("BY THE NUMBERS");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        sectionTitle.setForeground(PRIMARY_COLOR);
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // Stats cards
        JPanel statsCards = new JPanel(new GridLayout(1, 4, 20, 0));
        statsCards.setOpaque(false);
        statsCards.setMaximumSize(new Dimension(1200, 200));

        addStatCard(statsCards, "500+", "Hospitals");
        addStatCard(statsCards, "10M+", "Patients");
        addStatCard(statsCards, "99.9%", "Uptime");
        addStatCard(statsCards, "24/7", "Support");

        statsPanel.add(sectionTitle);
        statsPanel.add(statsCards);
        return statsPanel;
    }

    private JPanel createTestimonialsSection() {
        JPanel testimonialsPanel = new JPanel();
        testimonialsPanel.setLayout(new BoxLayout(testimonialsPanel, BoxLayout.Y_AXIS));
        testimonialsPanel.setOpaque(false);
        testimonialsPanel.setBorder(BorderFactory.createEmptyBorder(0, 60, 60, 60));

        // Section title
        JLabel sectionTitle = new JLabel("WHAT OUR CLIENTS SAY");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        sectionTitle.setForeground(PRIMARY_COLOR);
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // Testimonial cards
        JPanel testimonialCards = new JPanel(new GridLayout(1, 2, 30, 0));
        testimonialCards.setOpaque(false);
        testimonialCards.setMaximumSize(new Dimension(1200, 300));

        addTestimonialCard(testimonialCards,
                "The system transformed our hospital operations completely.",
                "Dr. Sarah Johnson",
                "Chief Medical Officer, City Hospital");

        addTestimonialCard(testimonialCards,
                "Best decision we made for our healthcare facility.",
                "Michael Brown",
                "Administrator, Regional Medical Center");

        testimonialsPanel.add(sectionTitle);
        testimonialsPanel.add(testimonialCards);
        return testimonialsPanel;
    }

    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                GradientPaint gradient = new GradientPaint(0, 0, new Color(52, 73, 94), getWidth(), 0, new Color(44, 62, 80));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        footerPanel.setPreferredSize(new Dimension(getWidth(), 50));

        // Footer content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        contentPanel.setOpaque(false);

        // Copyright info
        JLabel copyrightLabel = new JLabel("© 2025 MediCare Plus Hospital Management System");
        copyrightLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        copyrightLabel.setForeground(Color.WHITE);
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Contact info
        JLabel contactLabel = new JLabel("Contact: info@medicareplus.com | +1 (555) 123-4567");
        contactLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contactLabel.setForeground(new Color(189, 195, 199));
        contactLabel.setAlignmentX(Component.CENTER_ALIGNMENT);




        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(copyrightLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(contactLabel);

        footerPanel.add(contentPanel, BorderLayout.CENTER);
        return footerPanel;
    }

    private void addFeatureCard(JPanel panel, String title, String description) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        // Icon with circular background
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(PRIMARY_COLOR);
                g2d.fillOval(10, 0, 60, 60);
                g2d.setColor(Color.WHITE);
                if (title.contains("Patient")) {
                    drawPatientIcon(g2d, 40, 30);
                } else if (title.contains("Appointment")) {
                    drawCalendarIcon(g2d, 40, 30);
                } else {
                    drawBillingIcon(g2d, 40, 30);
                }
                g2d.dispose();
            }

            private void drawPatientIcon(Graphics2D g, int x, int y) {
                g.fillOval(x - 10, y - 15, 20, 20);
                g.fillRoundRect(x - 15, y + 5, 30, 25, 5, 5);
            }

            private void drawCalendarIcon(Graphics2D g, int x, int y) {
                g.fillRoundRect(x - 15, y - 15, 30, 30, 5, 5);
                g.setColor(PRIMARY_COLOR);
                g.drawLine(x - 5, y - 5, x + 5, y - 5);
                g.drawLine(x - 5, y + 5, x + 5, y + 5);
            }

            private void drawBillingIcon(Graphics2D g, int x, int y) {
                g.fillRoundRect(x - 15, y - 10, 30, 20, 5, 5);
                g.setColor(PRIMARY_COLOR);
                g.drawString("$", x - 5, y + 5);
            }
        };
        iconPanel.setPreferredSize(new Dimension(80, 70));
        iconPanel.setOpaque(false);

        // Title with bottom border
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(CARD_TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(5, 0, 10, 0)
        ));

        // Description text
        JTextArea descLabel = new JTextArea(description);
        descLabel.setFont(CARD_TEXT_FONT);
        descLabel.setForeground(TEXT_COLOR);
        descLabel.setLineWrap(true);
        descLabel.setWrapStyleWord(true);
        descLabel.setEditable(false);
        descLabel.setBackground(new Color(0, 0, 0, 0));
        descLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        descLabel.setOpaque(false);

        // Learn more link
        JLabel learnMoreLabel = new JLabel("Learn more →");
        learnMoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        learnMoreLabel.setForeground(ACCENT_COLOR);
        learnMoreLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        learnMoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // Arrange components
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.add(titleLabel);
        contentPanel.add(descLabel);
        contentPanel.add(learnMoreLabel);

        card.add(iconPanel, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);

        panel.add(card);
    }

    private void addStatCard(JPanel panel, String value, String label) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(PRIMARY_COLOR);

        JLabel descLabel = new JLabel(label, SwingConstants.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        descLabel.setForeground(TEXT_COLOR);

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);
        panel.add(card);
    }

    private void addTestimonialCard(JPanel panel, String quote, String name, String position) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Quote text
        JTextArea quoteText = new JTextArea(quote);
        quoteText.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        quoteText.setForeground(TEXT_COLOR);
        quoteText.setLineWrap(true);
        quoteText.setWrapStyleWord(true);
        quoteText.setEditable(false);
        quoteText.setBackground(new Color(0, 0, 0, 0));
        quoteText.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Author info
        JPanel authorPanel = new JPanel(new BorderLayout());
        authorPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(PRIMARY_COLOR);

        JLabel positionLabel = new JLabel(position);
        positionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        positionLabel.setForeground(ACCENT_COLOR);

        authorPanel.add(nameLabel, BorderLayout.NORTH);
        authorPanel.add(positionLabel, BorderLayout.SOUTH);

        card.add(quoteText, BorderLayout.CENTER);
        card.add(authorPanel, BorderLayout.SOUTH);
        panel.add(card);
    }

    private JLabel createModernLogo() {
        JLabel logoLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillOval(0, 0, 40, 40);
                g2d.setColor(PRIMARY_COLOR);
                g2d.setStroke(new BasicStroke(4));
                g2d.drawLine(20, 10, 20, 30);
                g2d.drawLine(10, 20, 30, 20);
                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(40, 40);
            }
        };
        return logoLabel;
    }

    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                FontMetrics metrics = g2d.getFontMetrics(getFont());
                int x = (getWidth() - metrics.stringWidth(getText())) / 2;
                int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };

        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 50));

        return button;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            HospitalLandingPage frame = new HospitalLandingPage();
            frame.setVisible(true);
        });
    }
}