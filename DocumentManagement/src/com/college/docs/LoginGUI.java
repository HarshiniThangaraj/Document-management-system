package com.college.docs;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginGUI extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;
    private JLabel feedbackLabel;

    public LoginGUI() {
        // Ensure placeholder images exist so the UI shows a background and overlay
        ImagePlaceholderGenerator.generateBackgroundIfMissing("resources/background.jpg");
        ImagePlaceholderGenerator.generateIfMissing("resources/document.png");
        setTitle("📂 College Document Management System");
        setSize(1200, 700); // Large window
        setLocationRelativeTo(null);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel background = new JLabel(new ImageIcon("resources/background.jpg"));
        background.setLayout(new BorderLayout());
        setContentPane(background);

        JLabel logo = new JLabel(new ImageIcon("resources/logo.png"));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        background.add(logo, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        background.add(formPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("LOGIN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 72));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 32));
        emailLabel.setForeground(Color.WHITE);
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        emailField = new JTextField(30);
        emailField.setFont(new Font("Arial", Font.PLAIN, 32));
        emailField.setToolTipText("Enter your registered email address");
        formPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 32));
        passwordLabel.setForeground(Color.WHITE);
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(30);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 32));
        passwordField.setToolTipText("Enter your password");
        formPanel.add(passwordField, gbc);

        // Feedback label for validation errors
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        feedbackLabel = new JLabel("", SwingConstants.CENTER);
        feedbackLabel.setFont(new Font("Arial", Font.ITALIC, 24));
        feedbackLabel.setForeground(Color.RED);
        feedbackLabel.setVisible(false); // Initially hidden
        formPanel.add(feedbackLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 4;
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 32));
        loginButton.setBackground(new Color(255, 140, 0));
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(200, 50));
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginButton.setFocusPainted(false);
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(255, 165, 0));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(255, 140, 0));
            }
        });
        formPanel.add(loginButton, gbc);

        gbc.gridx = 1;
        registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.BOLD, 24));
        registerButton.setBackground(new Color(70, 130, 180));
        registerButton.setForeground(Color.WHITE);
        registerButton.setPreferredSize(new Dimension(200, 50));
        registerButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        registerButton.setFocusPainted(false);
        registerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerButton.setBackground(new Color(100, 149, 237));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerButton.setBackground(new Color(70, 130, 180));
            }
        });
        formPanel.add(registerButton, gbc);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> new RegistrationGUI().setVisible(true));

        // Fade-in animation for buttons
        loginButton.setOpaque(false);
        registerButton.setOpaque(false);
        Timer timer = new Timer(50, new java.awt.event.ActionListener() {
            float alpha = 0.0f;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                alpha += 0.05f;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    ((Timer) e.getSource()).stop();
                }
                loginButton.setBackground(new Color(255, 140, 0, (int) (alpha * 255)));
                registerButton.setBackground(new Color(70, 130, 180, (int) (alpha * 255)));
                loginButton.repaint();
                registerButton.repaint();
            }
        });
        timer.start();
    }

    private void login() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter email and password!");
            return;
        }
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT * FROM users WHERE email=? AND password=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                if (role.equalsIgnoreCase("ADMIN")) {
                    // Check verification store (file-based) before allowing admin access
                    if (VerifiedAdminStore.isVerified(email)) new AdminDashboard(email).setVisible(true);
                    else JOptionPane.showMessageDialog(this, "Your admin account is pending verification. Please contact another admin to verify your account.");
                } else new UserDashboard(email).setVisible(true);
                dispose();
            } else JOptionPane.showMessageDialog(this, "Invalid email or password!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection error!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}