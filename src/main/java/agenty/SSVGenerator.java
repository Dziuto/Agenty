import jade.core.Agent;
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

                // Calculate N based on formula (12b) - Placeholder calculation
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

    private int calculateN(double e, double d) {
        // Implement formula (12b) here
        // Example: N = (int)(Math.log(2/d) / (2 * Math.pow(e, 2)));
        return 100;
    }

    public void processData(String filePath, javax.swing.JTextField[] fields) {
        // Create MFN object and display it (Logic simplified for brevity)
        System.out.println("Creating MFN object with provided parameters...");

        // Generate random SSVs (Placeholder for randomSSV method)
        String ssvData = "Generated " + N + " SSVs";

        // 3. Look for TT Agent and Send Message
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        AID ttAgent = new AID("TTAgent", AID.ISLOCALNAME); // Ensure TT agent name matches
        msg.addReceiver(ttAgent);
        msg.setContent("MFN_Params| " + filePath + " | " + ssvData);
        send(msg);

        // 4. Wait for response
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage reply = receive(mt);
                if (reply != null) {
                    JOptionPane.showMessageDialog(null, "Reliability Result: " + reply.getContent());
                    myGui.dispose();
                    doDelete(); // Terminate agent
                } else {
                    block();
                }
            }
        });
    }
}