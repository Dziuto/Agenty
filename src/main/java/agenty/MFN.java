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

        // Method to compute double factorial (2n+1)!! for use in normalCDF
        public long doubleFactorial(int n) {
            if (n < -1) {
                throw new IllegalArgumentException("Double factorial is not defined for n < -1.");
            }
            if (n == 0 || n == -1) {
                return 1;
            }
            long result = 1;
            for (int i = n; i > 0; i -= 2) {
                result *= i;
            }
            return result;
        }
    }

    // ??? Probability mass function
    public double[][] Pr() {
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
}