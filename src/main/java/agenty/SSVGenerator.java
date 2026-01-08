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

// Wklej to do klasy SSVGenerator w miejsce starej metody processData
    public void processData(String filePath, javax.swing.JTextField[] fields) {
        System.out.println("Processing data...");

        try {
            // 1. Parsowanie pól z GUI
            int m = Integer.parseInt(fields[0].getText().trim());
            int[] W = parseIntArray(fields[1].getText());
            double[] C = parseDoubleArray(fields[2].getText());
            int[] L = parseIntArray(fields[3].getText());
            double[] R = parseDoubleArray(fields[4].getText());
            double[] rho = parseDoubleArray(fields[5].getText());

            // 2. Utworzenie obiektu MFN i generacja wektorów
            MFN mfn = new MFN(m, W, C, L, R, rho);
            mfn.getMPs(filePath); // Wczytanie ścieżek, potrzebne do obliczeń w MFN, ale TT też będzie tego potrzebować
            
            double[][] pmf = mfn.PMF();
            double[][] cdf = mfn.CDF(pmf);
            
            // Generowanie N wektorów (N obliczone w setup())
            double[][] ssvs = mfn.randomSSV((int)this.N, cdf);
            System.out.println("Generated " + ssvs.length + " SSVs.");

            // 3. Wysłanie pakietu danych do agenta TT
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("tt", AID.ISLOCALNAME)); // Zakładamy, że agent TT nazywa się "tt"

            // Pakowanie wszystkich danych do tablicy Object[], bo nie możemy mieć osobnej klasy SimulationData
            // Kolejność indeksów musi być taka sama przy odbiorze w TT!
            Object[] dataPackage = new Object[8];
            dataPackage[0] = m;           // int
            dataPackage[1] = W;           // int[]
            dataPackage[2] = C;           // double[]
            dataPackage[3] = L;           // int[]
            dataPackage[4] = R;           // double[]
            dataPackage[5] = rho;         // double[]
            dataPackage[6] = filePath;    // String (ścieżka do pliku MPs)
            dataPackage[7] = ssvs;        // double[][] (wygenerowane wektory)

            try {
                msg.setContentObject(dataPackage); // Serializacja tablicy
            } catch (java.io.IOException ex) {
                System.err.println("Error serializing data: " + ex.getMessage());
                return;
            }
            
            send(msg);
            System.out.println("Data package sent to TT agent.");

            // 4. Oczekiwanie na wynik (niezawodność)
            addBehaviour(new CyclicBehaviour(this) {
                public void action() {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        String result = reply.getContent();
                        JOptionPane.showMessageDialog(null, "Reliability Result from TT: " + result);
                        
                        // Koniec pracy po otrzymaniu wyniku
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