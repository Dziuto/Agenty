package agenty;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TT extends Agent {

    // units of flow
    private double d; 
    // max transmission time
    private double T; 

    @Override
    protected void setup() {
        System.out.println("TT Agent " + getLocalName() + " is ready.");

        // Get arguments 
        Object[] args = getArguments();
        if (args != null && args.length >= 2) {
            try {
                this.d = Double.parseDouble(args[0].toString());
                this.T = Double.parseDouble(args[1].toString());
            } catch (Exception e) {
                System.err.println("TT Error: d and T must be doubles.");
                doDelete();
                return;
            }
        } else {
            System.err.println("TT Error: Missing arguments.");
            doDelete();
            return;
        }

        // Wait for data from SSV
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {
                    try {
                        Object content = msg.getContentObject();
                        
                        if (content instanceof Object[]) {
                            Object[] data = (Object[]) content;
                            System.out.println("TT received data package.");

                            int m = (int) data[0];
                            int[] W = (int[]) data[1];
                            double[] C = (double[]) data[2];
                            int[] L = (int[]) data[3];
                            double[] R = (double[]) data[4];
                            double[] rho = (double[]) data[5];
                            String csvFilePath = (String) data[6];
                            double[][] generatedSSVs = (double[][]) data[7];

                            saveSSVtoCSV(generatedSSVs, "SSV.csv");
                            double reliability = calculateReliability(m, W, C, L, R, rho, csvFilePath, generatedSSVs);

                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent(String.valueOf(reliability));
                            myAgent.send(reply);

                            System.out.println("Reliability calculated: " + reliability + ". Sent reply.");
                            doDelete(); 
                        }
                    } catch (UnreadableException e) {
                        System.err.println("TT Error: Failed to read message object.");
                        e.printStackTrace();
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void saveSSVtoCSV(double[][] ssvs, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (double[] vector : ssvs) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < vector.length; i++) {
                    sb.append(vector[i]);
                    if (i < vector.length - 1) sb.append(",");
                }
                writer.println(sb.toString());
            }
            System.out.println("SSVs saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double calculateReliability(int m, int[] W, double[] C, int[] L, double[] R, double[] rho, 
                                        String filePath, double[][] ssvs) {
        
        MFN mfn = new MFN(m, W, C, L, R, rho);
        mfn.getMPs(filePath);

        int successCount = 0;
        int totalSimulations = ssvs.length;

        for (double[] X : ssvs) {
            double time = mfn.calculateNetworkTransmissionTime(this.d, X);
            
            if (time <= this.T) {
                successCount++;
            }
        }

        if (totalSimulations == 0) return 0.0;
        return (double) successCount / totalSimulations;
    }
}