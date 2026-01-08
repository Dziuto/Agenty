package agenty;

import jade.core.Agent;

import java.util.Arrays;

import javax.swing.JOptionPane;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class SSVGenerator extends Agent {
    private int N; // Number of SSVs
    private SSVGeneratorGui myGui;

    protected void setup() {
        // 1. Get arguments epsilon and delta
        Object[] args = getArguments();
        if (args != null && args.length >= 2) {
            try {
                double epsilon = Double.parseDouble((String) args[0]);
                double delta = Double.parseDouble((String) args[1]);

                // Validate range (0, 1)
                if (epsilon <= 0 || epsilon >= 1 || delta <= 0 || delta >= 1) {
                    System.err.println("Arguments must be between 0 and 1.");
                    doDelete();
                    return;
                }

                this.N = calculateN(epsilon, delta);
                // 2. Show GUI
                myGui = new SSVGeneratorGui(this);

            } catch (Exception e) {
                System.err.println("Invalid arguments.");
                doDelete();
            }
        } else {
            System.err.println("Missing epsilon and delta arguments.");
            doDelete();
        }
    }

    private int calculateN(double epsilon, double delta) {
        if (epsilon <= 0 || delta <= 0 || delta >= 1) {
            throw new IllegalArgumentException("Epsilon must be > 0 and Delta must be between 0 and 1.");
        }

        return (int) MFN.getWorstCaseNormalSampleSize(epsilon, delta);
    }

public void processData(String filePath, javax.swing.JTextField[] fields) {
        System.out.println("Creating MFN object with provided parameters...");

        try {
            // 1. Parse GUI fields to create MFN parameters
            // Field order based on your GUI screenshot: m, W, C, L, R, rho
            int m = Integer.parseInt(fields[0].getText().trim());
            int[] W = parseIntArray(fields[1].getText());
            double[] C = parseDoubleArray(fields[2].getText());
            int[] L = parseIntArray(fields[3].getText());
            double[] R = parseDoubleArray(fields[4].getText());
            double[] rho = parseDoubleArray(fields[5].getText());

            // 2. Instantiate MFN Object (The Class)
            //
            MFN mfn = new MFN(m, W, C, L, R, rho);
            
            // Load MPs from the selected CSV file
            mfn.getMPs(filePath);
            // 3. Generate SSVs using the pre-calculated N
            //
            double[][] pmf = mfn.PMF();
            double[][] cdf = mfn.CDF(pmf);
            double[][] ssvs = mfn.randomSSV((int)this.N, cdf);

            System.out.println("Generated " + ssvs.length + " SSVs.");

            // 4. Send Message to TT Agent
            // We usually serialize the object or send a string. 
            // Here we send a simple string as a placeholder, or you could use setContentObject(ssvs)
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("tt", AID.ISLOCALNAME)); // Assuming TT agent is named "tt"
            
            // For simplicity, we are sending a confirmation string. 
            // In a real implementation, you would Serialize the 'ssvs' array or the 'mfn' object.
            msg.setContent("Generated " + ssvs.length + " vectors for file: " + filePath);
            send(msg);

            // 5. Wait for Response from TT
            addBehaviour(new CyclicBehaviour(this) {
                public void action() {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        String result = reply.getContent();
                        JOptionPane.showMessageDialog(null, "Reliability Result from TT: " + result);
                        
                        // Cleanup
                        myGui.dispose();
                        myAgent.doDelete();
                    } else {
                        block();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error processing data: " + e.getMessage());
        }
    }

    private int[] parseIntArray(String text) {
        return Arrays.stream(text.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

    private double[] parseDoubleArray(String text) {
        return Arrays.stream(text.split(",")).map(String::trim).mapToDouble(Double::parseDouble).toArray();
    }
}