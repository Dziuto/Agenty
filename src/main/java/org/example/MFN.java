import java.util.ArrayList;


public class MFN {

    private int m;
    private int[] W;
    private double[] C;
    private int[] L;
    private double[] R;
    private double[] rho;
    private double[] beta;
    private ArrayList<int[]> MPs;

    // --- Constructor ---

    public MFN(int m, int[] W, double[] C, int[] L, double[] R, double[] rho, ArrayList<int[]> MPs) {
        if (W.length != m || C.length != m || L.length != m || R.length != m || rho.length != m) {
            throw new IllegalArgumentException("Error: All vectors (W, C, L, R, rho) must have length equal to m (" + m + ").");
        }

        for (int i = 0; i < m; i++) {
            if (R[i] < 0.0 || R[i] > 1.0) {
                throw new IllegalArgumentException("Error: R[" + i + "] is out of bounds (0-1): " + R[i]);
            }
            if (rho[i] < 0.0 || rho[i] > 1.0) {
                throw new IllegalArgumentException("Error: rho[" + i + "] is out of bounds (0-1): " + rho[i]);
            }
        }

        this.m = m;
        this.W = W;
        this.C = C;
        this.L = L;
        this.R = R;
        this.rho = rho;
        this.MPs = MPs;

        this.beta = new double[m];
        createBetaVector();
    }

    // --- Getters and Setters ---

    public int getM() { return m; }
    public void setM(int m) { this.m = m; }

    public int[] getW() { return W; }
    public void setW(int[] w) { W = w; }

    public double[] getC() { return C; }
    public void setC(double[] c) { C = c; }

    public int[] getL() { return L; }
    public void setL(int[] l) { L = l; }

    public double[] getR() { return R; }
    public void setR(double[] r) { R = r; }

    public double[] getRho() { return rho; }
    public void setRho(double[] rho) { this.rho = rho; }

    public double[] getBeta() { return beta; }
    public void setBeta(double[] beta) { this.beta = beta; }

    public ArrayList<int[]> getMPs() { return MPs; }
    public void setMPs(ArrayList<int[]> MPs) { this.MPs = MPs; }

    public void getMPs(String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            this.MPs = new ArrayList<>(); // Ensure list is initialized/cleared

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("[,\\s]+");

                int[] path = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    try {
                        path[i] = Integer.parseInt(parts[i]);
                    } catch (NumberFormatException nfe) {
                        // Skip non-integer tokens (like headers if they exist)
                        continue;
                    }
                }
                this.MPs.add(path);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: Minimal Paths file not found: " + fileName);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error reading MP file: " + e.getMessage());
            e.printStackTrace();
        }
    }

        // inner class combinatorial and binomialCoefficient
    public static class Combinatorial {
        public static long factorial(int n) {
            if (n < 0) throw new IllegalArgumentException("Factorial undefined for negative numbers");
            if (n <= 1) return 1;

            long result = 1;
            for (int i = 2; i <= n; i++) {
                result *= i;
            }
            return result;
        }

        /**
         * Computes Binomial Coefficient (n choose k).
         */
        public static long binomialCoefficient(int n, int k) {
            if (k < 0 || k > n) return 0;
            if (k == 0 || k == n) return 1;
            if (k > n / 2) k = n - k;

            long result = 1;
            for (int i = 1; i <= k; i++) {
                result = result * (n - i + 1) / i;
            }
            return result;
        }
    }

}
