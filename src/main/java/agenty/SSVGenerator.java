package agenty;

import jade.core.Agent;
import java.util.Arrays;

public class SSVGenerator extends Agent {

    @Override
    protected void setup() {
        System.out.println("Agent " + getLocalName() + " started.");

        // 1. Retrieve arguments passed during activation (epsilon and delta)
        //
        Object[] args = getArguments();

        if (args != null && args.length >= 2) {
            try {
                // Parse arguments
                double epsilon = Double.parseDouble(args[0].toString());
                double delta = Double.parseDouble(args[1].toString());

                // 2. Check whether these values are between 0 and 1
                //
                if (epsilon <= 0 || epsilon >= 1 || delta <= 0 || delta >= 1) {
                    System.err.println("Error: Epsilon and Delta must be strictly between 0 and 1.");
                    System.out.println("Agent terminating due to invalid arguments.");
                    doDelete(); // Terminate the agent
                    return;
                }

                System.out.println("Arguments accepted: epsilon=" + epsilon + ", delta=" + delta);

                // 3. Determine the number N of SSVs based on formula (12b)
                // This uses the static method implemented in MFN class.
                //
                long N = MFN.getWorstCaseNormalSampleSize(epsilon, delta);
                System.out.println("Calculated Sample Size (N) based on Formula 12b: " + N);

                // --- OPTIONAL: Initialize MFN and Generate Data ---
                // The prompt asks to determine N, but typically this agent would then 
                // proceed to actually generate the vectors. 
                // You would need to initialize your specific MFN data here.
                
                /*
                // Example MFN initialization (Replace with your actual data/loading logic)
                int m = 3; 
                int[] W = {1, 1, 1}; 
                double[] C = {10.0, 10.0, 10.0};
                int[] L = {1, 1, 1}; 
                double[] R = {0.9, 0.9, 0.9}; 
                double[] rho = {0.0, 0.0, 0.0};

                MFN network = new MFN(m, W, C, L, R, rho);
                
                // Generate the Probability Mass Function (PMF)
                double[][] pmf = network.PMF();
                
                // Generate the Cumulative Distribution Function (CDF)
                double[][] cdf = network.CDF(pmf);
                
                // Generate the Random System State Vectors
                double[][] ssvs = network.randomSSV((int) N, cdf);
                
                System.out.println("Generated " + ssvs.length + " System State Vectors.");
                */

            } catch (NumberFormatException e) {
                System.err.println("Error: Arguments must be valid double numbers.");
                doDelete();
            }
        } else {
            System.err.println("Error: No arguments specified. Please pass epsilon and delta.");
            doDelete();
        }
    }
}