package agenty;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SSVGeneratorGui extends JFrame {
    private SSVGenerator myAgent;
    private JTextField[] params = new JTextField[6];
    private String[] labels = {"Links", "Numbers Vector", "Capacities Vector", "Lead Time Vector", "Reliabilities Vector", "Correlation Vector"};
    private String selectedFilePath = "";

    public SSVGeneratorGui(SSVGenerator a) {
        super("SSVGeneratorGui");
        myAgent = a;

        JPanel mainPanel = new JPanel(new GridLayout(0, 1));

        // File selection
        JButton fileBtn = new JButton("Choose .csv File");
        JLabel fileLabel = new JLabel("No file selected");
        fileBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFilePath = fc.getSelectedFile().getAbsolutePath();
                fileLabel.setText(fc.getSelectedFile().getName());
            }
        });

        mainPanel.add(fileBtn);
        mainPanel.add(fileLabel);

        // Parameter fields
        for (int i = 0; i < labels.length; i++) {
            mainPanel.add(new JLabel(labels[i]));
            params[i] = new JTextField();
            mainPanel.add(params[i]);
        }

        JButton sendBtn = new JButton("Send Data");
        sendBtn.addActionListener(e -> {
            // Collect data and trigger agent logic
            myAgent.processData(selectedFilePath, params);
        });

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(sendBtn, BorderLayout.SOUTH);

        setSize(400, 600);
        setVisible(true);
    }
}