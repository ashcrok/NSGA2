
package nsga2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NSGA2 {
    
    public static void main(String[] args) {
        
        // Initial population
        List<double[]> P = new ArrayList<>();
        for (int i=0; i<100; i++) {
            double x1 = (Math.random() * 8) - 4;
            double x2 = (Math.random() * 8) - 4;
            double x3 = (Math.random() * 8) - 4;
            double x[] = {x1,x2,x3};
            P.add(x);
        }
        
        // This Q should be generated with a genetic algorithm from P
        List<double[]> Q = new ArrayList<>();
        for (int i=0; i<100; i++) {
            double x1 = (Math.random() * 8) - 4;
            double x2 = (Math.random() * 8) - 4;
            double x3 = (Math.random() * 8) - 4;
            double x[] = {x1,x2,x3};
            Q.add(x);
        }
        
        // R is the reunion of P with Q
        List<double[]> R = P; R.addAll(Q);
        
        // Get fronts from R and put the first POP_SIZE into the new P
        List<List<double[]>> fronts = fast_nondominated_sort(R);
        P = new ArrayList<>();
        for (List<double[]> front : fronts) {
            if (front.size() + P.size() > 100) {
                crowding_distance_assignment(front,(100-P.size()));
                break;
            } else {
                P.addAll(front);
            }
        }
        
        // Pe-aici am ramas !!!
        
    }
    
    
    
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
        
        List<double[]> F = F1;
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
        
        return fronts;
    }
    
    // Crowding distance assignment
    public static void crowding_distance_assignment(List<double[]> front, int size) {
        int l = front.size();
        List<Integer> L = new ArrayList<>();
        for (int i=0; i<l; i++) L.add(0);
        
        // f1
        List<Double> frontF1 = new ArrayList<>(); 
        for (double[] sol : front) frontF1.add(MOP2f1(sol));
        Collections.sort(frontF1);
        
        
        // f2
    }
    
    // Operatia dubioasa
    public static boolean op(double[] x, double[] y) {
        return (MOP2f1(x) <= MOP2f1(y) && MOP2f2(x) <= MOP2f2(y) && (MOP2f1(x) < MOP2f1(y) || MOP2f2(x) < MOP2f2(y)));
    }
    
    /*-------------------------------------*/
    
    public static void mutation() {
        // TODO
    }
    
    public static void crossover() {
        // TODO
    }
    
    // Selectie de tip Roata Norocului
    public static void selection(List<double[]> P) {
        // TODO
    }
    
    /*-------------------------------------*/
    
    public static void printFronts(List<List<double[]>> fronts) {
        System.out.println("--- FRONTS ---");
        for (int i=0; i<fronts.size(); i++) {
            System.out.println("[FRONT " + (i+1) + "]");
            List<double[]> front = fronts.get(i);
            for (double[] sol : front) {
                for (int k=0; k<sol.length; k++)
                    System.out.print("x" + (k+1) + " = " + sol[k] + "; ");
                System.out.println("");
            }
            System.out.println("");
        }
        System.out.println("--------------");
    }
    
    /*-------------------------------------*/
    
    // MOP2 Functions
    public static double MOP2f1(double x[]) {
        double result = 1;
        double sum = 0;
        for (int i=0; i<x.length; i++) {
            sum += Math.pow((x[i] - 1/Math.sqrt(x.length)),2);
        }
        result -= Math.exp(-sum);
        return result;
    }
    public static double MOP2f2(double x[]) {
        double result = 1;
        double sum = 0;
        for (int i=0; i<x.length; i++) {
            sum += Math.pow((x[i] + 1/Math.sqrt(x.length)),2);
        }
        result -= Math.exp(-sum);
        return result;
    }
    
}
