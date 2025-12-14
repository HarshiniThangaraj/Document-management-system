package com.college.docs;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegistrationGUI extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;
    private JButton registerButton;

    public RegistrationGUI() {
        setTitle("📂 Register - College Docs");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

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

        JLabel title = new JLabel("REGISTER", SwingConstants.CENTER);
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
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 32));
        roleLabel.setForeground(Color.WHITE);
        formPanel.add(roleLabel, gbc);
        gbc.gridx = 1;
        roleCombo = new JComboBox<>(new String[]{"USER", "ADMIN"});
        roleCombo.setFont(new Font("Arial", Font.PLAIN, 32));
        formPanel.add(roleCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.BOLD, 32));
        registerButton.setBackground(new Color(255, 140, 0));
        registerButton.setForeground(Color.WHITE);
        registerButton.setPreferredSize(new Dimension(260, 64));
        formPanel.add(registerButton, gbc);

        registerButton.addActionListener(e -> registerUser());
    }

    private void registerUser() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter email and password!");
            return;
        }
        try {
            Connection con = DBConnection.getConnection();
            String checkSql = "SELECT * FROM users WHERE email=?";
            PreparedStatement pst = con.prepareStatement(checkSql);
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Email already registered!");
                return;
            }
            String role = roleCombo.getSelectedItem() == null ? "USER" : String.valueOf(roleCombo.getSelectedItem());
            String insertSql = "INSERT INTO users (email, password, role) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = con.prepareStatement(insertSql);
            insertStmt.setString(1, email);
            insertStmt.setString(2, password);
            insertStmt.setString(3, role);
            insertStmt.executeUpdate();

            // If an admin was registered, ensure they are not auto-verified and notify main admin via pending store
            if ("ADMIN".equalsIgnoreCase(role)) {
                // Do not add to verified list. Store pending request for super-admin review.
                PendingAdminStore.addPending(email);
                JOptionPane.showMessageDialog(this, "Admin registration successful! The main admin (admin@example.com) will be asked to verify your account.");
                dispose();
                return;
            }

            JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
            dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error!");
        }
    }
}