
package nsga2;

import functions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import metrics.Hypervolume;

public class NSGA2 {
    
    public static final int POPULATION = 100;
    public static final int ITERATIONS = 250;
    
    public static F func = new functions.ZDT.ZDT6();
    
    // --------------------------------------------------------------------- //
    
    public void run() {
        // Initial population
        List<double[]> P = new ArrayList<>();
        for (int i=0; i<POPULATION; i++)
            P.add(func.generate());
        // Q is generated with genetic operators
        List<double[]> Q = selection(P);
        // Main Loop
        for (int i=0; i<ITERATIONS; i++) {
            // R is the reunion of P with Q
            List<double[]> R = new ArrayList<>(P);
            R.addAll(Q);
            clearDuplicates(R);
            // Get fronts from R and put the first POP_SIZE into the new P
            List<List<double[]>> fronts = fast_nondominated_sort(R);
            P = new ArrayList<>();
            for (List<double[]> front : fronts) {
                if (front.size() + P.size() > POPULATION) {
                    P.addAll(front.subList(0, POPULATION - P.size()));
                    // TO DO crowding_distance_assignment
    //                crowding_distance_assignment(front,(100-P.size()));
                    break;
                } else { P.addAll(front); }
            }
            // GA Operators
            Q = selection(P);
        }
        // Get First Front
        List<double[]> R = P; R.addAll(Q);
        clearDuplicates(R);
        List<List<double[]>> fronts = fast_nondominated_sort(R);
        List<double[]> first_front = fronts.get(0);
        // Print stuff
        printFirstFrontFormated(first_front);
        printDelta(first_front);
        printHypervolume(first_front);
    }
    
    // --------------------------------------------------------------------- //
    
    public void printPopulation(List<double[]> pop) {
        for (double[] p : pop) {
            for (int i = 0; i < p.length; i++)
                System.out.print(p[i] + " ");
            System.out.println();
        }
        System.out.println("\n\n");
    }
    
    public void printHypervolume(List<double[]> pop) {
        List<double[]> front = new ArrayList<>();
        for (int i = 0; i < func.getNoObjectives(); i++)
            front.add(new double[pop.size()]);
        for (int i = 0; i < pop.size(); i++) {
            func.set(pop.get(i));
            double[] fitness = func.evaluate();
            for (int j = 0; j < func.getNoObjectives(); j++)
                front.get(j)[i] = fitness[j];
        }
        double hypervolume = new Hypervolume(func).calculateHypervolume(front, front.get(0).length, front.size());
        System.out.println("HYPERVOLUME VALUE: " + hypervolume);
        System.out.println("-------\n");
    }
    
    public void printFirstFrontFormated(List<double[]> pop) {
        List<double[]> graphic = new ArrayList<>();
        for (int i = 0; i < func.getNoObjectives(); i++)
            graphic.add(new double[pop.size()]);
        for (int i = 0; i < pop.size(); i++) {
            func.set(pop.get(i));
            double[] fitness = func.evaluate();
            for (int j = 0; j < func.getNoObjectives(); j++)
                graphic.get(j)[i] = fitness[j];
        }
        System.out.println("Number of objectives: " + graphic.size());
        System.out.println("Population: " + pop.size());
        System.out.println("-------");
        for (int i = 0; i < graphic.size(); i++) {
            double[] f = graphic.get(i);
            System.out.print("f" + (i+1) + " <- c(");
            for (int j = 0; j < f.length; j++) {
                System.out.print(f[j]);
                if (j!=f.length-1) System.out.print(", ");
            }
            System.out.println(");");
        }
        System.out.println("-------\n");
    }
    
    public void clearDuplicates(List<double[]> R) {
        for (int i=0; i<R.size()-1; i++)
            for (int j=i+1; j<R.size(); j++) {
                double[] p = R.get(i);
                double[] q = R.get(j);
                boolean duplicate = true;
                if (p[0] != q[0])
                    duplicate = false;
                if (duplicate)
                    R.set(i, func.generate());
            }
    }
    
    public void printDelta(List<double[]> pop) {
        double DELTA = 0.0;
        double davg = 0.0;
        for (int k = 1; k < pop.size(); k++)
            davg += d(pop.get(k-1), pop.get(k));
        davg = davg / (pop.size() - 1);
        for (int k = 1; k < pop.size(); k++)
            DELTA += Math.abs(d(pop.get(k-1),pop.get(k)) - davg) / POPULATION;
        System.out.println("DELTA VALUE: " + DELTA);
        System.out.println("-------\n");
    }
    
    public double d(double[] x1, double[] x2) {
        double sum = 0.0;
        for (int i=0; i<x1.length; i++)
            sum += Math.pow(x1[i] - x2[i], 2.0);
        return Math.sqrt(sum);
    }
    
    // --------------------------------------------------------------------- //
    
    // Fast non-dominated sort
    public static List<List<double[]>> fast_nondominated_sort(List<double[]> P) {
        List<double[]> F1 = new ArrayList<>();
        List<List<double[]>> S = new ArrayList<>();
        List<Integer> n = new ArrayList<>();
        
        // First phase
        for (double[] p : P) {
            int np = 0;
            List<double[]> Sp = new ArrayList<>();
            for (double[] q : P) {
                if (p != q) {
                    if (op(p,q)) Sp.add(q); else if (op(q,p)) np += 1;
                }
            }
            if (np == 0) F1.add(p);
            S.add(Sp);
            n.add(np);
        }
        
        List<double[]> F = new ArrayList<>(F1);
        List<List<double[]>> fronts = new ArrayList<>();
        fronts.add(F1);
        
        // Second phase
        while (!F.isEmpty()) {
            List<double[]> H = new ArrayList<>();
            for (double[] p : F) {
                for (double[] q : S.get(P.indexOf(p))) {
                    n.set(P.indexOf(q), n.get(P.indexOf(q))-1);
                    if (n.get(P.indexOf(q)) == 0)
                        H.add(q);
                }
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
    
    // Crowding distance assignment
    public static void crowding_distance_assignment(List<double[]> front, int size) {
        int l = front.size();
        List<Integer> L = new ArrayList<>();
        for (int i=0; i<l; i++) L.add(0);
        // TO DO
    }
    
    // Dominance operator
    public static boolean op(double[] x, double[] y) {
        func.set(x);
        double[] f = func.evaluate();
        func.set(y);
        double[] g = func.evaluate();
        
        return (f[0] <= g[0] && f[1] <= g[1] && (f[0] < g[0] || f[1] < g[1]));
    }
    
    // Sort population after dominance operator
    public static void sort(List<double[]> P) {
        P.stream().forEach((p) -> {
            P.stream().filter((q) -> (!op(p,q))).forEach((q) -> {
                double[] temp = p;
                P.set(P.indexOf(p), q);
                P.set(P.indexOf(q), temp);
            });
        });
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
    
    // DOUBLE TO BITS AND BACK
    private static String doubleToBinary(double x) {
        int d = 6;
        double N = (func.getMaxValue() - func.getMinValue()) * Math.pow(10,d);
        int n = (int) Math.ceil(Math.log(N) / Math.log(2));
        long decimal = (long) ((x - func.getMinValue()) * (Math.pow(2,n) - 1) / (func.getMaxValue() - func.getMinValue()));
        String binary = Long.toBinaryString(decimal);
        if (binary.length() < 5) binary = "000" + binary;
        return binary;
    }
    private static double binaryToDouble(String b) {
        int d = 6;
        double N = (func.getMaxValue() - func.getMinValue()) * Math.pow(10,d);
        int n = (int) Math.ceil(Math.log(N) / Math.log(2));
        long decimal = Long.parseLong(b, 2);
        return (func.getMinValue() + decimal * (func.getMaxValue() - func.getMinValue()) / (Math.pow(2,n) - 1));
    }
    
    // --------------------------------------------------------------------- //
    
}
