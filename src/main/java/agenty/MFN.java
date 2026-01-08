package agenty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import lombok.Getter;

@Getter
public class MFN {
    //  the number of links - int m
    private int m;
    //  the component number vector -int[] W
    private int[] W;
    //  the component capacity vector - double[] C
    private double[] C;
    //  the lead time vector - int[] L
    private int[] L;
    //  the component reliability vector - double[] R,
    private double[] R;
    //  the vector of the correlation between the faults of the components - double[] rho
    private double[] rho;
    //  the beta vector– double[] beta
    private double[] beta;
    //  the list of minimal paths - ArrayList<int[]> MPs
    private ArrayList<int[]> MPs;

    public MFN(int m, int[] W, double[] C, int[] L, double[] R, double[] rho) {
        this.m = m;
        this.W = W;
        this.C = C;
        this.L = L;
        this.R = R;
        this.rho = rho;
        this.MPs = new ArrayList<>();

        if (W.length != m || C.length != m || L.length != m || R.length != m || rho.length != m) {
            throw new IllegalArgumentException("The length of all vectors (W, C, L, R, rho) must be equal to m.");
        }

        for (double value : R) {
            if (value < 0 || value > 1) {
                throw new IllegalArgumentException("All values in R must be between 0 and 1.");
            }
        }

        for (double value : rho) {
            if (value < 0 || value > 1) {
                throw new IllegalArgumentException("All values in rho must be between 0 and 1.");
            }
        }

        this.beta = new double[m];
        for (int i = 0; i < m; i++) {
            this.beta[i] = 1.0 + (this.rho[i]*(1-this.R[i]))/this.R[i];
        }
    }

    public class Combinatorial {
        public long factorial(int n) {
            if (n < 0) {
                throw new IllegalArgumentException("Factorial is not defined for negative numbers.");
            }
            long result = 1;
            for (int i = 2; i <= n; i++) {
                result *= i;
            }
            return result;
        }
        public long binomialCoefficient(int n, int k) {
            if (k < 0 || k > n) {
                return 0;
            }
            if (k == 0 || k == n) {
                return 1;
            }
            return factorial(n)/(factorial(k)*factorial(n-k));
        }

        public double doubleFactorial(int n) {
            if (n < 0) return 1.0; // Handle edge case
            if (n == 0 || n == 1) return 1.0;
            
            double result = 1.0;
            // n!! = n * (n-2) * (n-4) ... 
            for (int i = n; i > 1; i -= 2) {
                result *= i;
            }
            return result;
        }
    }

    // (1)
    // Probability mass function
    public double[][] PMF() {
        Combinatorial combinatorial = new Combinatorial();

        int maxW = 0;
        for (int w : this.W) {
            if (w > maxW) maxW = w;
        }

        double[][] Pr = new double[this.m][maxW + 1];

        for (int i = 0; i < this.m; i++) { // For each link i
            int wi = this.W[i];
            double ri = this.R[i];
            double betai = this.beta[i];
            double rbeta = ri * betai;


            for (int k = 0; k <= wi; k++) { // For each state k (0 to w_i)
                if (k == 0) {
                    Pr[i][0] = 1.0 - (1.0 / betai) * (1.0 - Math.pow((1-rbeta), wi));
                } else {
                    long binomial = combinatorial.binomialCoefficient(wi, k);
                    Pr[i][k]  = (1.0 / betai) * binomial * Math.pow(rbeta, k) * Math.pow((1-rbeta), wi - k);
                }
            }
        }
        return Pr;
    }

    // (4)
    public int calculatePathLeadTime(int[] path) {
        int pathLeadTime = 0;
        for (int linkIndex : path) {
            pathLeadTime += this.L[linkIndex];
        }
        return pathLeadTime;
    }

    // (5)
    public double calculatePathCapacity(int[] path, double[] X) {
        if (path.length == 0) return 0.0;

        double minCapacity = Double.MAX_VALUE;
        for (int linkIndex : path) {
            if (X[linkIndex] < minCapacity) {
                minCapacity = X[linkIndex];
            }
        }
        return minCapacity;
    }

    // (3)
    public double calculatePathTransmissionTime(double d, int[] path, double[] X) {
        double pathCapacity = calculatePathCapacity(path, X);

        if (pathCapacity > 0) {
            int pathLeadTime = calculatePathLeadTime(path);

            double flowDuration = Math.ceil(d / pathCapacity);

            return pathLeadTime + flowDuration;
        } else {
            return Double.MAX_VALUE;
        }
    }

    // (8)
    public double calculateNetworkTransmissionTime(double d, double[] X) {
        double minTransmissionTime = Double.MAX_VALUE;

        for (int[] path : this.MPs) {
            // Note: The budget constraint is ignored.
            double pathTime = calculatePathTransmissionTime(d, path, X);

            if (pathTime < minTransmissionTime) {
                minTransmissionTime = pathTime;
            }
        }
        return minTransmissionTime;
    }

        
    //()
    public double[][] CDF(double[][] arPMF) {
        int rows = arPMF.length;
        int cols = arPMF[0].length;
        double[][] cdf = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            double cumulativeSum = 0.0;
            for (int k = 0; k < cols; k++) {
                cumulativeSum += arPMF[i][k];
                cdf[i][k] = cumulativeSum;
            }
            cdf[i][cols - 1] = 1.0;
        }
        return cdf;
    }


    public static double normalCDF(double z) {
        MFN.Combinatorial combinatorial = new MFN(0, new int[0], new double[0], new int[0], new double[0], new double[0]).new Combinatorial();
        
        double sum = 0.0;
        int n = 100; 

        for (int k = 0; k <= n; k++) {
            double numerator = Math.pow(z, 2 * k + 1);
            double denominator = combinatorial.doubleFactorial(2 * k + 1);
            
            sum += numerator / denominator;
        }

        double constant = 1.0 / Math.sqrt(2 * Math.PI);
        double expPart = Math.exp(-1.0 * (z * z) / 2.0);
        
        return 0.5 + (constant * expPart * sum);
    }


    public static double normalICDF(double u) {
        if (u <= 0.0 || u >= 1.0) {
            throw new IllegalArgumentException("Input u must be strictly between 0 and 1 for Inverse CDF.");
        }

        double min = -20.0;
        double max = 20.0;
        double mid = 0.0;
        double tolerance = 1e-10; 

        while (max - min > tolerance) { 
            mid = (min + max) / 2.0;
            double val = normalCDF(mid);
            
            double diff = val - u;
            if (Math.abs(diff) <= tolerance) {
                return mid;
            }

            if (val < u) {
                min = mid;
            } 
            else {
                max = mid;
            }
        }
        
        return mid;
    }

    public double[][] randomSSV(int N, double[][] arCDF) {
        if (N <= 0) {
            throw new IllegalArgumentException("N must be greater than 0");
        }
        
        // N rows (simulations), m columns (components)
        double[][] ssv = new double[N][this.m];
        Random random = new Random();

        for (int n = 0; n < N; n++) {
            for (int i = 0; i < this.m; i++) { 
                double u = random.nextDouble();

                // Find min k such that arCDF[i][k] >= u
                int stateK = 0;
                
                for (int k = 0; k < arCDF[i].length; k++) {
                    if (arCDF[i][k] >= u) {
                        stateK = k;
                        break;
                    }
                }

                ssv[n][i] = stateK * this.C[i];
            }
        }

        return ssv;
    }

    public void getMPs(String fileName) {
        this.MPs.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(new File(fileName)))) {
            String line;

            while ((line = br.readLine()) != null) {

                String[] linkParts = line.split(",");

                int[] path = new int[linkParts.length];

                for (int i = 0; i < linkParts.length; i++) {
                    path[i] = Integer.parseInt(linkParts[i].trim());
                }

                this.MPs.add(path);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static long getWorstCaseNormalSampleSize(double epsilon, double delta) {
        if (epsilon <= 0 || delta <= 0 || delta >= 1) {
            throw new IllegalArgumentException("Epsilon must be > 0 and Delta must be between 0 and 1.");
        }

        // 1. Calculate the probability argument p = 1 - delta / 2
        double p = 1.0 - (delta / 2.0);

        // 2. Calculate Inverse Normal CDF (Phi^-1) using your existing static method
        double z = normalICDF(p);

        // 3. Apply Formula (12b): n = ceil( [z / (2 * epsilon)]^2 )
        double term = z / (2.0 * epsilon);
        
        // Return the result as a long integer
        return (long) Math.ceil(Math.pow(term, 2));
    }

}