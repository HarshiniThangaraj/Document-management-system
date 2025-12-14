package com.college.docs;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.Desktop;
// Image IO handled via ImageIcon path-based loading; explicit ImageIO/BufferedImage imports removed

public class AdminDashboard extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel tableModel;

    private JButton approveButton;
    private JButton rejectButton;
    private JButton viewButton; // New: View file
    private JButton manageAdminsButton; // New
    private JLabel pendingCountLabel;
    private JLabel approvedCountLabel;
    private JLabel rejectedCountLabel;
    private String adminEmail;
    private JLabel adminNotifyLabel; // shows number of unverified admins

    public AdminDashboard(String adminEmail) {
        setTitle("Admin Dashboard - Pending Documents");
        this.adminEmail = adminEmail;
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

            // Prepare right-side overlay: white stats box + document image
            try {
                ImageIcon docIcon = new ImageIcon("resources/document.png");
                // Reduce the document image to make more room for the table
                int docW = Math.max(220, (int) (screen.width * 0.18));
                int docH = Math.max(240, (int) (screen.height * 0.38));
                Image doc = docIcon.getImage().getScaledInstance(docW, docH, Image.SCALE_SMOOTH);
                JLabel docLabel = new JLabel(new ImageIcon(doc));
                docLabel.setOpaque(false);
                docLabel.setPreferredSize(new Dimension(docW, docH));

                JPanel eastPanel = new JPanel(new BorderLayout());
                eastPanel.setOpaque(false);
                // Narrow the east panel so the table gets most of the width
                eastPanel.setPreferredSize(new Dimension(docW + 60, screen.height));

                    // White stats box at the top-right for admin counts (with Back button)
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
                    AdminDashboard.this.dispose();
                });
                statsHeader.add(backButton, BorderLayout.EAST);
                statsBox.add(statsHeader);
                // Admin verification notification label
                adminNotifyLabel = new JLabel();
                adminNotifyLabel.setOpaque(false);
                adminNotifyLabel.setForeground(new Color(220, 20, 60));
                adminNotifyLabel.setFont(new Font("Arial", Font.BOLD, 14));
                adminNotifyLabel.setVisible(false);
                adminNotifyLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                adminNotifyLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        openManageAdminsDialog();
                    }
                });
                JPanel notifyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                notifyPanel.setOpaque(false);
                notifyPanel.add(adminNotifyLabel);
                statsBox.add(notifyPanel);

                pendingCountLabel = createStatLabel("PENDING", "0", new Color(255, 215, 0));
                approvedCountLabel = createStatLabel("APPROVED", "0", new Color(60, 179, 113));
                rejectedCountLabel = createStatLabel("REJECTED", "0", new Color(255, 99, 71));

                pendingCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                approvedCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                rejectedCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                statsBox.add(Box.createRigidArea(new Dimension(0, 6)));
                statsBox.add(pendingCountLabel);
                statsBox.add(Box.createRigidArea(new Dimension(0, 8)));
                statsBox.add(approvedCountLabel);
                statsBox.add(Box.createRigidArea(new Dimension(0, 8)));
                statsBox.add(rejectedCountLabel);

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

        // Note: admin counts (Pending/Approved/Rejected) are shown in the right-side white stats box

        // Table setup (include Assigned To and Assigned Verified so users can see if assigned admin is verified)
        String[] columns = {"Select", "ID", "User Email", "Filename", "File Path", "Assigned To", "Assigned Verified", "Status", "Uploaded At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : super.getColumnClass(column);
            }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 20));
        table.setRowHeight(35);
        table.getColumnModel().getColumn(0).setMaxWidth(50); // Checkbox column width
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        // Encourage the table area to use more space than the right-side panel
        try {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int preferW = Math.max(800, screen.width - (table.getColumnModel().getColumnCount() * 80) - 380);
            int preferH = Math.max(400, screen.height - 220);
            tableScroll.setPreferredSize(new Dimension(preferW, preferH));
        } catch (Exception ignore) {}
        add(tableScroll, BorderLayout.CENTER);

        // Row coloring for status
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String status = (String) table.getValueAt(row, 7); // Status column (after assignedVerified addition)
                if (status.equalsIgnoreCase("PENDING")) c.setBackground(new Color(255, 255, 153)); // yellow
                else if (status.equalsIgnoreCase("APPROVED")) c.setBackground(new Color(144, 238, 144)); // green
                else if (status.equalsIgnoreCase("REJECTED")) c.setBackground(new Color(255, 160, 122)); // red
                else c.setBackground(Color.WHITE);

                if (isSelected) c.setBackground(c.getBackground().darker());
                return c;
            }
        });

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);

        approveButton = new JButton("Approve");
        approveButton.setFont(new Font("Arial", Font.BOLD, 20));
        approveButton.setBackground(new Color(60, 179, 113));
        approveButton.setForeground(Color.WHITE);
        approveButton.setFocusPainted(false);

        rejectButton = new JButton("Reject");
        rejectButton.setFont(new Font("Arial", Font.BOLD, 20));
        rejectButton.setBackground(new Color(255, 69, 0));
        rejectButton.setForeground(Color.WHITE);
        rejectButton.setFocusPainted(false);

        viewButton = new JButton("View File"); // New button
        viewButton.setFont(new Font("Arial", Font.BOLD, 20));
        viewButton.setBackground(new Color(70, 130, 180)); // steel blue
        viewButton.setForeground(Color.WHITE);
        viewButton.setFocusPainted(false);

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(viewButton); // Add view button
        manageAdminsButton = new JButton("Manage Admins");
        manageAdminsButton.setFont(new Font("Arial", Font.BOLD, 16));
        manageAdminsButton.setBackground(new Color(128, 128, 128));
        manageAdminsButton.setForeground(Color.WHITE);
        manageAdminsButton.setFocusPainted(false);
        buttonPanel.add(manageAdminsButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        approveButton.addActionListener(e -> updateStatus("APPROVED"));
        rejectButton.addActionListener(e -> updateStatus("REJECTED"));
        viewButton.addActionListener(e -> viewFile()); // Action for viewing file
        manageAdminsButton.addActionListener(e -> openManageAdminsDialog());

        // Restrict "Manage Admins" to the super-admin (first admin in DB)
        String superAdminEmail = getSuperAdminEmail();
        if (superAdminEmail != null && !superAdminEmail.equalsIgnoreCase(this.adminEmail)) {
            manageAdminsButton.setEnabled(false);
            manageAdminsButton.setToolTipText("Only super-admin (" + superAdminEmail + ") can manage admin verifications");
        } else {
            manageAdminsButton.setEnabled(true);
            manageAdminsButton.setToolTipText("Open admin verification manager (super-admin)");
        }

        loadDocuments();
        // Show number of unverified admins to allowed viewers; poll every 30 seconds to update
        checkUnverifiedAdmins();
        javax.swing.Timer tick = new javax.swing.Timer(30000, ev -> checkUnverifiedAdmins());
        tick.setRepeats(true);
        tick.start();
    }

    // Manage admin verification: list admins and toggle verification
    private void openManageAdminsDialog() {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT email FROM users WHERE role='ADMIN'"; // do not change DB schema
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            java.util.List<String> admins = new java.util.ArrayList<>();
            while (rs.next()) admins.add(rs.getString("email"));

            if (admins.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No admin accounts found.");
                return;
            }

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            java.util.Map<String, JCheckBox> map = new java.util.HashMap<>();
            boolean allowEdit = this.adminEmail != null && this.adminEmail.equalsIgnoreCase(getSuperAdminEmail());
            for (String a : admins) {
                boolean v = VerifiedAdminStore.isVerified(a);
                if (allowEdit) {
                    JCheckBox cb = new JCheckBox(a + (v ? " (Verified)" : " (Not Verified)"), v);
                    panel.add(cb);
                    map.put(a, cb);
                } else {
                    JLabel lab = new JLabel(a + (v ? " (Verified)" : " (Not Verified)"));
                    lab.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
                    panel.add(lab);
                }
            }
            if (allowEdit) {
                int res = JOptionPane.showConfirmDialog(this, new JScrollPane(panel), "Manage Admin Verifications", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (res == JOptionPane.OK_OPTION) {
                    for (String a : admins) {
                        JCheckBox cb = map.get(a);
                        try {
                            if (cb.isSelected()) VerifiedAdminStore.addVerified(a);
                            else VerifiedAdminStore.removeVerified(a);
                        } catch (Exception e) {
                            // ignore file errors per design
                            e.printStackTrace();
                        }
                    }
                    // Clear pending registrations for any admin that is now verified
                    for (String a : admins) {
                        if (VerifiedAdminStore.isVerified(a)) PendingAdminStore.clearPendingFor(a);
                    }
                    JOptionPane.showMessageDialog(this, "Verification changes saved.");
                    checkUnverifiedAdmins();
                    loadDocuments();
                }
            } else {
                JOptionPane.showMessageDialog(this, new JScrollPane(panel), "Registered Admins (view-only)", JOptionPane.PLAIN_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading admin list from DB");
        }
    }

    // Load pending documents from DB
    private void loadDocuments() {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT * FROM documents";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            tableModel.setRowCount(0); // clear table
            while (rs.next()) {
                int id = rs.getInt("id");
                String assigned = AssignmentStore.getAssignedAdmin(id);
                Object[] row = {
                    false, // Checkbox for selection
                    id,
                    rs.getString("user_email"),
                    rs.getString("filename"),
                    rs.getString("file_path"),
                    assigned,
                    (assigned == null || assigned.trim().isEmpty()) ? "N/A" : (VerifiedAdminStore.isVerified(assigned) ? "Yes" : "No"),
                    rs.getString("status"),
                    rs.getTimestamp("uploaded_at")
                };
                tableModel.addRow(row);
            }

            updateCounts();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading documents!");
        }
    }

    private void updateCounts() {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT status, COUNT(*) as cnt FROM documents GROUP BY status";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            int pending = 0, approved = 0, rejected = 0;
            while (rs.next()) {
                String status = rs.getString("status");
                int cnt = rs.getInt("cnt");
                if ("PENDING".equalsIgnoreCase(status)) pending = cnt;
                else if ("APPROVED".equalsIgnoreCase(status)) approved = cnt;
                else if ("REJECTED".equalsIgnoreCase(status)) rejected = cnt;
            }

            pendingCountLabel.setText(formatStatText("PENDING", pending));
            approvedCountLabel.setText(formatStatText("APPROVED", approved));
            rejectedCountLabel.setText(formatStatText("REJECTED", rejected));

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String getSuperAdminEmail() {
        try {
            Connection con = DBConnection.getConnection();
            // Prefer explicit 'admin@example.com' if it exists, otherwise fall back to the first admin
            String preferSql = "SELECT email FROM users WHERE role='ADMIN' AND email='admin@example.com' LIMIT 1";
            PreparedStatement prefPst = con.prepareStatement(preferSql);
            ResultSet prefRs = prefPst.executeQuery();
            if (prefRs.next()) return prefRs.getString("email");
            String sql = "SELECT email FROM users WHERE role='ADMIN' ORDER BY email ASC LIMIT 1";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("email");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "admin@example.com"; // fall back to main admin constant to ensure there's someone asked to verify
    }

    private void checkUnverifiedAdmins() {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT email FROM users WHERE role='ADMIN'";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            java.util.List<String> admins = new java.util.ArrayList<>();
            while (rs.next()) admins.add(rs.getString("email"));
            java.util.List<String> unverified = new java.util.ArrayList<>();
            for (String a : admins) if (!VerifiedAdminStore.isVerified(a)) unverified.add(a);
            java.util.List<String> pending = PendingAdminStore.listPending();
            if (unverified.isEmpty()) {
                adminNotifyLabel.setVisible(false);
            } else {
                // Show notifications to verified admins and to super-admin
                boolean isSuper = this.adminEmail != null && this.adminEmail.equalsIgnoreCase(getSuperAdminEmail());
                boolean isVerified = this.adminEmail != null && VerifiedAdminStore.isVerified(this.adminEmail);
                if (isSuper || isVerified) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(unverified.size()).append(" admin(s) require verification - click to review");
                    if (!pending.isEmpty() && isSuper) {
                        sb.append(" - Pending: ");
                        String joined = String.join(", ", pending.stream().map(s -> s.split(",")[0]).collect(java.util.stream.Collectors.toList()));
                        sb.append(joined);
                    }
                    String text = "<html>" + sb.toString().replace("\n", "<br/>") + "</html>";
                    adminNotifyLabel.setText(text);
                    adminNotifyLabel.setVisible(true);
                } else {
                    adminNotifyLabel.setVisible(false);
                }
            }
            // ensure the Manage Admins button is only enabled for the super admin
            String superAdmin = getSuperAdminEmail();
            if (superAdmin != null) {
                boolean allowEdit = this.adminEmail != null && superAdmin.equalsIgnoreCase(this.adminEmail);
                if (manageAdminsButton != null) manageAdminsButton.setEnabled(allowEdit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            adminNotifyLabel.setVisible(false);
        }
    }

    private JLabel createStatLabel(String title, String value, Color bg) {
        JLabel label = new JLabel(formatStatText(title, Integer.parseInt(value)));
        label.setOpaque(true);
        label.setBackground(bg);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        return label;
    }

    private String formatStatText(String title, int value) {
        return "<html><div style='text-align:center'><b>" + value + "</b><br/>" + title + "</div></html>";
    }

    // Update status of selected document
    private void updateStatus(String newStatus) {
        // Collect selected rows via the checkbox column (column 0)
        int rowCount = table.getRowCount();
        List<Integer> idsToUpdate = new ArrayList<>();
        for (int r = 0; r < rowCount; r++) {
            Object sel = table.getValueAt(r, 0);
            boolean checked = sel instanceof Boolean ? (Boolean) sel : false;
            if (checked) {
                Object idObj = table.getValueAt(r, 1); // ID is column 1
                if (idObj instanceof Number) idsToUpdate.add(((Number) idObj).intValue());
                else {
                    try { idsToUpdate.add(Integer.parseInt(String.valueOf(idObj))); }
                    catch (NumberFormatException ignore) {}
                }
            }
        }

        if (idsToUpdate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one document first!");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();
            String sql = "UPDATE documents SET status=? WHERE id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            java.util.List<Integer> allowed = new java.util.ArrayList<>();
            java.util.List<Integer> denied = new java.util.ArrayList<>();
            for (Integer id : idsToUpdate) {
                String assigned = AssignmentStore.getAssignedAdmin(id);
                if (assigned == null || assigned.trim().isEmpty() || assigned.equalsIgnoreCase(this.adminEmail)) {
                    allowed.add(id);
                } else {
                    denied.add(id);
                }
            }
            for (Integer id : allowed) {
                pst.setString(1, newStatus);
                pst.setInt(2, id);
                pst.addBatch();
            }
            pst.executeBatch();
            if (!denied.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Some selected documents are assigned to other admins and were not updated.");
            }

            JOptionPane.showMessageDialog(this, "Document(s) " + newStatus + " successfully!");
            loadDocuments(); // refresh table

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating status!");
        }
    }

    // New: View selected file
    private void viewFile() {
        // Try to open the file for either a highlighted row or the first checked row
        int selectedRow = table.getSelectedRow();
        int rowToOpen = -1;

        if (selectedRow != -1) {
            rowToOpen = selectedRow;
        } else {
            // If no row is highlighted, find first checked checkbox in column 0
            for (int r = 0; r < table.getRowCount(); r++) {
                Object sel = table.getValueAt(r, 0);
                boolean checked = sel instanceof Boolean ? (Boolean) sel : false;
                if (checked) { rowToOpen = r; break; }
            }
        }

        if (rowToOpen == -1) {
            JOptionPane.showMessageDialog(this, "Please select (highlight) or check a document to view!");
            return;
        }

        Object pathObj = table.getValueAt(rowToOpen, 4); // File Path column
        String filePath = pathObj == null ? "" : String.valueOf(pathObj);
        File file = new File(filePath);

        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "File not found on server!\nPath: " + filePath);
            return;
        }

        try {
            if (!Desktop.isDesktopSupported()) {
                JOptionPane.showMessageDialog(this, "Desktop API not supported on this platform.");
                return;
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.OPEN)) {
                JOptionPane.showMessageDialog(this, "Open action is not supported on this platform.");
                return;
            }
            desktop.open(file); // Open file with default system app
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error opening file:\n" + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new AdminDashboard("admin@example.com").setVisible(true);
    }
}