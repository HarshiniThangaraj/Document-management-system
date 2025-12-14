package com.college.docs;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
// java.awt.event imports are used fully-qualified in listeners; avoid unused-import warnings
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.sql.*;
// Image IO handled via ImageIcon path-based loading; explicit ImageIO/BufferedImage imports removed

public class UserDashboard extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel tableModel;
    private String userEmail;

    private JButton uploadButton;
    private JButton downloadButton;
    private JLabel userApprovedLabel;
    private JLabel userRejectedLabel;

    public UserDashboard(String email) {
        this.userEmail = email;

        setTitle("User Dashboard - Documents");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Ensure programmatic background and document overlay exist and set background as content pane
        ImagePlaceholderGenerator.generateBackgroundIfMissing("resources/background.jpg");
        ImagePlaceholderGenerator.generateIfMissing("resources/document.png");
        try {
            ImageIcon bgIcon = new ImageIcon("resources/background.jpg");
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Image bg = bgIcon.getImage().getScaledInstance(screen.width, screen.height, Image.SCALE_SMOOTH);
            JLabel background = new JLabel(new ImageIcon(bg));
            background.setLayout(new BorderLayout());

            // Prepare right-side overlay: document image + white stats box
            try {
                ImageIcon docIcon = new ImageIcon("resources/document.png");
                int docW = (int) (screen.width * 0.28);
                int docH = (int) (screen.height * 0.56);
                Image doc = docIcon.getImage().getScaledInstance(docW, docH, Image.SCALE_SMOOTH);
                JLabel docLabel = new JLabel(new ImageIcon(doc));
                docLabel.setOpaque(false);

                JPanel eastPanel = new JPanel(new BorderLayout());
                eastPanel.setOpaque(false);
                eastPanel.setPreferredSize(new Dimension(docW + 80, screen.height));

                // White stats box at the top-right
                JPanel statsBox = new JPanel();
                statsBox.setOpaque(true);
                statsBox.setBackground(new Color(255, 255, 255, 230));
                statsBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
                statsBox.setLayout(new BoxLayout(statsBox, BoxLayout.Y_AXIS));

                // Header inside stats box with Back button aligned to right
                JPanel statsHeader = new JPanel(new BorderLayout());
                statsHeader.setOpaque(false);
                JButton backButton = new JButton("\u2190 Back");
                backButton.setFont(new Font("Arial", Font.PLAIN, 12));
                backButton.setFocusPainted(false);
                backButton.setBackground(new Color(240, 240, 240));
                backButton.addActionListener(ev -> {
                    new LoginGUI().setVisible(true);
                    UserDashboard.this.dispose();
                });
                statsHeader.add(backButton, BorderLayout.EAST);
                statsBox.add(statsHeader);

                // Initialize the user stat labels and insert into right white box
                userApprovedLabel = createUserStatLabel("APPROVED", 0, new Color(60, 179, 113));
                userRejectedLabel = createUserStatLabel("REJECTED", 0, new Color(255, 99, 71));
                userApprovedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                userRejectedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                statsBox.add(Box.createRigidArea(new Dimension(0, 6)));
                statsBox.add(userApprovedLabel);
                statsBox.add(Box.createRigidArea(new Dimension(0, 8)));
                statsBox.add(userRejectedLabel);

                JPanel docPanel = new JPanel(new GridBagLayout());
                docPanel.setOpaque(false);
                docPanel.add(docLabel, new GridBagConstraints());

                eastPanel.add(statsBox, BorderLayout.NORTH);
                eastPanel.add(docPanel, BorderLayout.CENTER);
                background.add(eastPanel, BorderLayout.EAST);
            } catch (Exception ignore) {}

            setContentPane(background);
        } catch (Exception ex) {
            setLayout(new BorderLayout());
        }

        // Header and search combined in a top container
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftHeader.setOpaque(false);
        JLabel userLabel = new JLabel("Welcome, " + userEmail);
        userLabel.setFont(new Font("Arial", Font.BOLD, 24));
        userLabel.setForeground(Color.WHITE);
        leftHeader.add(userLabel);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 18));
        searchField.setToolTipText("Search by filename or status");
        JButton searchButton = new JButton("🔍 Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 18));
        searchButton.setBackground(new Color(30, 144, 255));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(leftHeader, BorderLayout.WEST);
        topRow.add(searchPanel, BorderLayout.EAST);
        topContainer.add(topRow);

        // Note: user-approved/rejected stat labels are shown in a right-side white box
        // (they were moved from the top into the right overlay area)

        add(topContainer, BorderLayout.NORTH);

        // Table setup (include Assigned To column)
        String[] columns = {"ID", "Filename", "File Path", "Assigned To", "Status", "Uploaded At"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 18));
        table.setRowHeight(30);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        add(tableScroll, BorderLayout.CENTER);

        // Row coloring
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String status = (String) table.getValueAt(row, 4); // Status column
                if (status.equalsIgnoreCase("APPROVED")) c.setBackground(new Color(144, 238, 144));
                else if (status.equalsIgnoreCase("PENDING")) c.setBackground(new Color(255, 255, 153));
                else if (status.equalsIgnoreCase("REJECTED")) c.setBackground(new Color(255, 160, 122));
                else c.setBackground(Color.WHITE);

                if (isSelected) c.setBackground(c.getBackground().darker());
                return c;
            }
        });

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        uploadButton = new JButton("📤 Upload Document");
        uploadButton.setFont(new Font("Arial", Font.BOLD, 24));
        uploadButton.setBackground(new Color(30, 144, 255));
        uploadButton.setForeground(Color.WHITE);
        uploadButton.setFocusPainted(false);
        uploadButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        uploadButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                uploadButton.setBackground(new Color(65, 105, 225));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                uploadButton.setBackground(new Color(30, 144, 255));
            }
        });

        downloadButton = new JButton("📥 Download Selected");
        downloadButton.setFont(new Font("Arial", Font.BOLD, 24));
        downloadButton.setBackground(new Color(255, 69, 0));
        downloadButton.setForeground(Color.WHITE);
        downloadButton.setFocusPainted(false);
        downloadButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        downloadButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                downloadButton.setBackground(new Color(220, 20, 60));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                downloadButton.setBackground(new Color(255, 69, 0));
            }
        });

        buttonPanel.add(uploadButton);
        buttonPanel.add(downloadButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        uploadButton.addActionListener(e -> uploadDocument());
        downloadButton.addActionListener(e -> downloadDocument());

        // Search action
        searchButton.addActionListener(e -> {
            String text = searchField.getText().trim();
            if (text.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 4)); // Filter by filename (col 1) or status (col 4)
            }
        });

        loadDocuments();
    }

    private void loadDocuments() {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT id, filename, file_path, status, uploaded_at FROM documents WHERE user_email=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, userEmail);
            ResultSet rs = pst.executeQuery();

            tableModel.setRowCount(0);
            int approvedCount = 0;
            int rejectedCount = 0;
            while (rs.next()) {
                String assigned = AssignmentStore.getAssignedAdmin(rs.getInt("id"));
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("filename"),
                    rs.getString("file_path"),
                    assigned,
                    rs.getString("status"),
                    rs.getTimestamp("uploaded_at")
                };
                tableModel.addRow(row);

                // Notify user if status changed
                String status = rs.getString("status");
                if (!status.equalsIgnoreCase("PENDING")) {
                    JOptionPane.showMessageDialog(this,
                            "Document \"" + rs.getString("filename") + "\" has been " + status + "!");
                }
                if (status.equalsIgnoreCase("APPROVED")) approvedCount++;
                else if (status.equalsIgnoreCase("REJECTED")) rejectedCount++;
            }

            // update user stats
            userApprovedLabel.setText(formatUserStatText("APPROVED", approvedCount));
            userRejectedLabel.setText(formatUserStatText("REJECTED", rejectedCount));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading documents!");
        }
    }

    private JLabel createUserStatLabel(String title, int value, Color bg) {
        JLabel label = new JLabel(formatUserStatText(title, value));
        label.setOpaque(true);
        label.setBackground(bg);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return label;
    }

    private String formatUserStatText(String title, int value) {
        return "<html><div style='text-align:center'><b>" + value + "</b> " + title + "</div></html>";
    }

    private void uploadDocument() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = fileChooser.getSelectedFile();
        try {
            String hash = calculateFileHash(selectedFile);

            Connection con = DBConnection.getConnection();
            String checkSql = "SELECT * FROM documents WHERE hash_value=?";
            PreparedStatement checkStmt = con.prepareStatement(checkSql);
            checkStmt.setString(1, hash);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "⚠ This document already exists!");
                return;
            }

            // Save file
            File folder = new File("uploaded_docs");
            if (!folder.exists()) folder.mkdir();
            Path destination = Paths.get(folder.getAbsolutePath(), selectedFile.getName());
            Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Insert DB record and retrieve generated ID
            String insertSql = "INSERT INTO documents (user_email, filename, file_path, hash_value, status) VALUES (?, ?, ?, ?, 'PENDING')";
            PreparedStatement pst = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, userEmail);
            pst.setString(2, selectedFile.getName());
            pst.setString(3, destination.toString());
            pst.setString(4, hash);
            pst.executeUpdate();
            ResultSet gk = pst.getGeneratedKeys();
            int docId = -1;
            if (gk != null && gk.next()) docId = gk.getInt(1);

            JOptionPane.showMessageDialog(this, "✅ Document uploaded successfully!");
            // Ask the user to choose an admin to verify this document
            try {
                PreparedStatement pstAdmins = con.prepareStatement("SELECT email FROM users WHERE role='ADMIN'");
                ResultSet rsAdmins = pstAdmins.executeQuery();
                java.util.List<String> admins = new java.util.ArrayList<>();
                while (rsAdmins.next()) admins.add(rsAdmins.getString("email"));
                if (!admins.isEmpty() && docId != -1) {
                    JComboBox<String> combo = new JComboBox<>(admins.toArray(new String[0]));
                    combo.setSelectedIndex(0);
                    int resp = JOptionPane.showConfirmDialog(this, combo, "Assign admin to verify this document:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (resp == JOptionPane.OK_OPTION) {
                        String assigned = (String) combo.getSelectedItem();
                        AssignmentStore.setAssignedAdmin(docId, assigned);
                    }
                }
            } catch (Exception ignore) {
            }
            loadDocuments();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error uploading document!");
        }
    }

    private void downloadDocument() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a document to download!");
            return;
        }

        String status = (String) table.getValueAt(selectedRow, 4);
        if (!status.equalsIgnoreCase("APPROVED")) {
            JOptionPane.showMessageDialog(this, "⚠ Only APPROVED documents can be downloaded!");
            return;
        }

        String filePath = (String) table.getValueAt(selectedRow, 2);
        File sourceFile = new File(filePath);
        if (!sourceFile.exists()) {
            JOptionPane.showMessageDialog(this, "File not found on server!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(sourceFile.getName()));
        int option = fileChooser.showSaveDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) return;

        File destFile = fileChooser.getSelectedFile();
        try {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(this, "✅ File downloaded successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error downloading file!");
        }
    }

    private String calculateFileHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        fis.close();

        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static void main(String[] args) {
        new UserDashboard("user1@example.com").setVisible(true);
    }
}