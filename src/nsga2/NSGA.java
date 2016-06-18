/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nsga2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author ashcrok
 */
public class NSGA {
    
    public String[] generateFirstPopulation(double[][] population) {
        List<double[]> P = matrixToList(population);
        List<double[]> Q = selection(P);
        List<double[]> R = new ArrayList<>(P); R.addAll(Q);
        return genRCode(R);
    }
    
    public String[] generateNewPopulation(double[][] population, double[][] fitness) {
        List<double[]> R = matrixToList(population);
        List<double[]> fit = matrixToList(fitness);
        List<List<double[]>> fronts = fast_nondominated_sort(R,fit);
        List<double[]> P = new ArrayList<>();
        for (List<double[]> front : fronts) {
            if (front.size() + P.size() > R.size()/2) {
                P.addAll(front.subList(0, R.size()/2 - P.size()));
                // TO DO crowding_distance_assignment
//                crowding_distance_assignment(front,(100-P.size()));
                break;
            } else { P.addAll(front); }
        }
        // GA Operators
        List<double[]> Q = selection(P);
        R = new ArrayList<>(P); R.addAll(Q);
        return genRCode(R);
    }
    
    public String[] getFinalPopulation(double[][] population, double[][] fitness) {
        List<double[]> R = matrixToList(population);
        List<double[]> fit = matrixToList(fitness);
        List<List<double[]>> fronts = fast_nondominated_sort(R,fit);
        System.out.println("Version 0.0.1");
        return genRCode(fronts.get(0));
    }
    
    // --------------------------------------------------------------------- //
    
    // Fast non-dominated sort
    private List<List<double[]>> fast_nondominated_sort(List<double[]> P, List<double[]> fit) {
        List<double[]> F1 = new ArrayList<>();
        List<List<double[]>> S = new ArrayList<>();
        List<Integer> n = new ArrayList<>();
        
        // First phase
        P.stream().forEach((p) -> {
            int np = 0;
            List<double[]> Sp = new ArrayList<>();
            for (double[] q : P) {
                if (p != q) {
                    if (op(fit.get(P.indexOf(p)),fit.get(P.indexOf(q)))) Sp.add(q); 
                    else if (op(fit.get(P.indexOf(q)),fit.get(P.indexOf(p)))) np += 1;
                }
            }
            if (np == 0) F1.add(p);
            S.add(Sp);
            n.add(np);
        });
        
        List<double[]> F = new ArrayList<>(F1);
        List<List<double[]>> fronts = new ArrayList<>();
        fronts.add(F1);
        
        // Second phase
        while (!F.isEmpty()) {
            List<double[]> H = new ArrayList<>();
            for (double[] p : F) {
                S.get(P.indexOf(p)).stream().map((q) -> {
                    n.set(P.indexOf(q), n.get(P.indexOf(q))-1);
                    return q;
                }).filter((q) -> (n.get(P.indexOf(q)) == 0)).forEach((q) -> {
                    H.add(q);
                });
            }
            if (!H.isEmpty()) fronts.add(H);
            F = H;
        }
        
        List<double[]> last_front = new ArrayList<>();
        for (int i=0; i<n.size(); i++)
            if (n.get(i) > 0)
                last_front.add(P.get(i));
        if (!last_front.isEmpty()) fronts.add(last_front);
        
        return fronts;
    }
    
    // Dominance operator
    private boolean op(double[] f, double[] g) {
        return (f[0] <= g[0] && f[1] <= g[1] && (f[0] < g[0] || f[1] < g[1]));
    }
    
    // --------------------------------------------------------------------- //
    
    public double[] mutation(double[] p) {
        double q[] = new double[p.length];
        for (int i=0; i<p.length; i++) {
            String bval = doubleToBinary(p[i]);
            int cut = (new Random()).nextInt(bval.length() - 1);
            char c = bval.charAt(cut);
            if (c == '0') c = '1'; else c = '0';
            bval = bval.substring(0,cut) + c + bval.substring(cut+1);
            q[i] = binaryToDouble(bval);
        }
        return q;
    }
    public double[] crossover(double[] p1, double[] p2) {
        double[] kid = new double[p1.length];
        for (int i=0; i<p1.length; i++) {
            String binaryString1 = doubleToBinary(p1[i]);
            String binaryString2 = doubleToBinary(p2[i]);
            int limit; if (binaryString1.length() < binaryString2.length()) limit = binaryString1.length(); else limit = binaryString2.length();
            int cutPoint1 = (new Random()).nextInt(limit/2-1);
            int cutPoint2 = (new Random()).nextInt(limit/2-1) + limit/2;
            String child = binaryString1.substring(0,cutPoint1) + binaryString2.substring(cutPoint1, cutPoint2)
                     + binaryString1.substring(cutPoint2);
            kid[i] = binaryToDouble(child);
        }
        return kid;
    }
    public List<double[]> selection(List<double[]> P) {
        List<double[]> Q = new ArrayList<>(P);
        List<double[]> new_pop = new ArrayList<>();
        for (int i=0; i<Q.size()-1; i=i+2) {
            double[] parent1 = Q.get(i);
            double[] parent2 = Q.get(i+1);
            double[] bro = crossover(parent1,parent2);
            double[] sis = crossover(parent2,parent1);
            new_pop.add(mutation(bro));
            new_pop.add(mutation(sis));
        }
        return new_pop;
    }
    
    // --------------------------------------------------------------------- //
    
    private String[] genRCode(List<double[]> R) {
        List<String> r = new ArrayList<>();
        r.add("R <- matrix(nrow=0,ncol=" + N + ");");
        r.add("fit <- matrix(nrow=0,ncol=" + M + ");");
        for (int k = 0; k < R.size(); k++) {
            double[] sol = R.get(k);
            String v = "c(";
            for (int i = 0; i < sol.length; i++) {
                v += sol[i];
                if (i != sol.length - 1)
                    v+= ",";
            }
            v += ")";
            r.add("R <- rbind(R," + v + ");");
            r.add("fit <- rbind(fit,f(" + v + "));");
        }
        String result[] = new String[r.size()];
        for (int i = 0; i < r.size(); i++)
            result[i] = r.get(i);
        return result;
    }
    
    private List<double[]> matrixToList(double[][] matrix) {
        List<double[]> list = new ArrayList<>();
        list.addAll(Arrays.asList(matrix));
        return list;
    }
    
    // DOUBLE TO BITS AND BACK
    private String doubleToBinary(double x) {
        int d = 6;
        double N = (this.MAX - this.MIN) * Math.pow(10,d);
        int n = (int) Math.ceil(Math.log(N) / Math.log(2));
        long decimal = (long) ((x - this.MIN) * (Math.pow(2,n) - 1) / (this.MAX - this.MIN));
        String binary = Long.toBinaryString(decimal);
        if (binary.length() < 5) binary = "000" + binary;
        return binary;
    }
    private double binaryToDouble(String b) {
        int d = 6;
        double N = (this.MAX - this.MIN) * Math.pow(10,d);
        int n = (int) Math.ceil(Math.log(N) / Math.log(2));
        long decimal = Long.parseLong(b, 2);
        return (this.MIN + decimal * (this.MAX - this.MIN) / (Math.pow(2,n) - 1));
    }
    
    // --------------------------------------------------------------------- //
    
    private final int MIN;
    private final int MAX;
    private final int N;
    private final int M;
    public NSGA(int n, int m, int min, int max) {
        this.MIN = min;
        this.MAX = max;
        this.N = n;
        this.M = m;
    }
    
    // --------------------------------------------------------------------- //
    
}
