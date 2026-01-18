package agenty;

import jade.core.Agent;

import java.util.Arrays;

import javax.swing.JOptionPane;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class SSVGenerator extends Agent {
    private int N; 
    private SSVGeneratorGui myGui;

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length >= 2) {
            try {
                double epsilon = Double.parseDouble((String) args[0]);
                double delta = Double.parseDouble((String) args[1]);

                if (epsilon <= 0 || epsilon >= 1 || delta <= 0 || delta >= 1) {
                    System.err.println("Arguments must be between 0 and 1.");
                    doDelete();
                    return;
                }

                this.N = calculateN(epsilon, delta);
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
        return (int) MFN.getWorstCaseNormalSampleSize(epsilon, delta);
    }

    public void processData(String filePath, javax.swing.JTextField[] fields) {
        System.out.println("Processing data...");

        try {
            int m = Integer.parseInt(fields[0].getText().trim());
            int[] W = parseIntArray(fields[1].getText());
            double[] C = parseDoubleArray(fields[2].getText());
            int[] L = parseIntArray(fields[3].getText());
            double[] R = parseDoubleArray(fields[4].getText());
            double[] rho = parseDoubleArray(fields[5].getText());

            MFN mfn = new MFN(m, W, C, L, R, rho);

            mfn.getMPs(filePath); 
            
            double[][] pmf = mfn.PMF();
            double[][] cdf = mfn.CDF(pmf);

            
            
            double[][] ssvs = mfn.randomSSV((int)this.N, cdf);
            System.out.println("Generated " + ssvs.length + " SSVs.");

            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("tt", AID.ISLOCALNAME));

            Object[] dataPackage = new Object[8];
            dataPackage[0] = m;
            dataPackage[1] = W;
            dataPackage[2] = C;
            dataPackage[3] = L;
            dataPackage[4] = R;
            dataPackage[5] = rho;
            dataPackage[6] = filePath;
            dataPackage[7] = ssvs;



            try {
                msg.setContentObject(dataPackage);
            } catch (java.io.IOException ex) {
                System.err.println("Error sending data: " + ex.getMessage());
                return;
            }
            send(msg);
            System.out.println("Data package sent to TT agent.");

            // Wait for respond
            addBehaviour(new CyclicBehaviour(this) {
                public void action() {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        String result = reply.getContent();
                        JOptionPane.showMessageDialog(null, "Reliability Result from TT: " + result);
                        
                        if (myGui != null) myGui.dispose();
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