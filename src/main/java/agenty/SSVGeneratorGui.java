package agenty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

//
public class SSVGeneratorGui extends JFrame {

    private SSVGenerator myAgent;
    
    // UI Components matching the screenshot
    private JTextField txtM, txtW, txtC, txtL, txtR, txtRho;
    private JTextField txtFilePath;
    private JButton btnBrowse, btnSendData;

    public SSVGeneratorGui(SSVGenerator agent) {
        this.myAgent = agent;
        
        setTitle("SSV Generator Agent Interface");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(8, 2, 10, 10)); // Grid layout for labels and inputs

        // 1. Initialize Components
        // m (Number of links)
        add(new JLabel("  Number of links (m):"));
        txtM = new JTextField("5");
        add(txtM);

        // W (Component numbers)
        add(new JLabel("  Component numbers vector (W):"));
        txtW = new JTextField("4,3,2,3,2");
        add(txtW);

        // C (Capacities)
        add(new JLabel("  Component capacities vector (C):"));
        txtC = new JTextField("10,15,25,15,20");
        add(txtC);

        // L (Lead times)
        add(new JLabel("  Lead time vector (L):"));
        txtL = new JTextField("5,7,6,5,8");
        add(txtL);

        // R (Reliabilities)
        add(new JLabel("  Reliabilities vector (R):"));
        txtR = new JTextField("0.7, 0.65, 0.67, 0.71, 0.75");
        add(txtR);

        // rho (Correlations)
        add(new JLabel("  Correlation vector (rho):"));
        txtRho = new JTextField("0.1, 0.3, 0.5, 0.7, 0.9");
        add(txtRho);

        // File Selection
        btnBrowse = new JButton("Select MPs.csv File");
        txtFilePath = new JTextField();
        txtFilePath.setEditable(false);
        
        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add(txtFilePath, BorderLayout.CENTER);
        filePanel.add(btnBrowse, BorderLayout.EAST);
        
        add(new JLabel("  Minimal Paths File:"));
        add(filePanel);

        // Send Data Button
        add(new JLabel("")); // Spacer
        btnSendData = new JButton("Send Data");
        add(btnSendData);

        // 2. Add Action Listeners
        
        // Browse Button Logic
        btnBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showOpenDialog(SSVGeneratorGui.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    txtFilePath.setText(file.getAbsolutePath());
                }
            }
        });

        // Send Data Button Logic
        btnSendData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendDataToAgent();
            }
        });
        
        setVisible(true);
    }

    private void sendDataToAgent() {
        try {
            // Parse inputs from TextFields
            int m = Integer.parseInt(txtM.getText().trim());
            
            int[] W = parseIntArray(txtW.getText());
            double[] C = parseDoubleArray(txtC.getText());
            int[] L = parseIntArray(txtL.getText());
            double[] R = parseDoubleArray(txtR.getText());
            double[] rho = parseDoubleArray(txtRho.getText());
            
            String fileName = txtFilePath.getText();

            if (fileName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a file first.");
                return;
            }

            // Call the method in the Agent to process this data
            //
            myAgent.processUserData(fileName, m, W, C, L, R, rho);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error parsing data: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Helper to parse "1,2,3" to int[]
    private int[] parseIntArray(String text) {
        return Arrays.stream(text.split(","))
                     .map(String::trim)
                     .mapToInt(Integer::parseInt)
                     .toArray();
    }

    // Helper to parse "0.1, 0.2" to double[]
    private double[] parseDoubleArray(String text) {
        return Arrays.stream(text.split(","))
                     .map(String::trim)
                     .mapToDouble(Double::parseDouble)
                     .toArray();
    }
}