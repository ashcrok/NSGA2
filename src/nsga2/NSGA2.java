
package nsga2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class NSGA2 {
    
    public static void main(String[] args) {
        
        long startTime = System.currentTimeMillis();
        
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
        List<double[]> Q = selection(P);
        Q = crossover(Q);
        Q = mutation(Q);
        
        for (int i=0; i<100; i++) {
            // R is the reunion of P with Q
            List<double[]> R = P; R.addAll(Q);

            // Get fronts from R and put the first POP_SIZE into the new P
            List<List<double[]>> fronts = fast_nondominated_sort(R);
            P = new ArrayList<>();
            for (List<double[]> front : fronts) {
                if (front.size() + P.size() > 100) {
                    P.addAll(front.subList(0, 100 - P.size()));
                    // TO DO crowding_distance_assignment
    //                crowding_distance_assignment(front,(100-P.size()));
                    break;
                } else {
                    P.addAll(front);
                }
            }
            
            // GA Operators
            Q = selection(P);
            Q = crossover(Q);
            Q = mutation(Q);
        }
        
        // Display la rezulate si statistici peste rezultalte
        List<double[]> R = P; R.addAll(Q);
        List<List<double[]>> fronts = fast_nondominated_sort(R);
        List<double[]> first_front = fronts.get(0);
        long endTime = System.currentTimeMillis();
        System.out.println("size: " + first_front.size());
        System.out.println("time: " + ((endTime - startTime) * 0.001));
        System.out.println("----------");
        Set<Double> diff_values = new TreeSet<>();
        for (double[] x : first_front) {
            diff_values.add(x[0]);
            for (int i=0; i<x.length; i++) {
                System.out.print(x[i] + " ");
            }
            System.out.println("");
        }
        System.out.println("----------");
        System.out.println("different values: " + diff_values.size());
        
        
        /**
         *  Probleme:
         * 1) Apar valori 'NaN' cateodata
         * 2) Uneori da IndexOutOfBound de la niste random-uri
         * 3) Sunt mult prea multe duplicate: aici ar trebui sa verific per 
         * iteratie ca merge totul bine
         * 
         *  TO DO:
         * 1) De reparat toate problemele de mai sus
         * 2) De implementat functia care evalueaza rezultatul si de comparat
         * cu rezultatele lui Deb
         * 
         */
        
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
    
    public static List<double[]> mutation(List<double[]> Q) {
        int C = 4;
        double[] q = Q.get((new Random()).nextInt(Q.size()));
        double[] q_new = new double[q.length];
        for (int i=0; i<q.length; i++) {
            double n = q[i] + C;
            String bval = Long.toBinaryString(Double.doubleToLongBits(n));
            int cut = (new Random()).nextInt(bval.length() - 1);
            char c = bval.charAt(cut);
            if (c == '0') c = '1'; else c = '0';
            bval = bval.substring(0,cut) + c + bval.substring(cut+1);
            q_new[i] = Double.longBitsToDouble(Long.parseLong(bval, 2)) - C;
        }
        Q.set(Q.indexOf(q), q_new);
        return Q;
    }
    
    // Incrucisare printr-un singur punct de taiere; se face pentru fiecare valoare a vectorului solutie
    public static List<double[]> crossover(List<double[]> Q) {
        int C = 4;
        double[] q1 = Q.get((new Random()).nextInt(Q.size()));
        double[] q2 = Q.get((new Random()).nextInt(Q.size()));
        double[] q3 = new double[q1.length], q4 = new double[q1.length];
        for (int i=0; i<q1.length; i++) {
            double n1 = q1[i] + C;
            double n2 = q2[i] + C;
            String bval1 = Long.toBinaryString(Double.doubleToLongBits(n1));
            String bval2 = Long.toBinaryString(Double.doubleToLongBits(n2));
            int cut = (new Random()).nextInt(bval1.length());
            String bval3 = bval1.substring(0,cut) + bval2.substring(cut);
            String bval4 = bval2.substring(0,cut) + bval1.substring(cut);
            q3[i] = Double.longBitsToDouble(Long.parseLong(bval3, 2)) - C;
            q4[i] = Double.longBitsToDouble(Long.parseLong(bval4, 2)) - C;
        }
        Q.set(Q.indexOf(q1), q3);
        Q.set(Q.indexOf(q2), q4);
        return Q;
    }
    
    // Selectie de tip Roata Norocului
    public static List<double[]> selection(List<double[]> P) {
        // Evalueaza P in E si fitnessul sumat S
        List<Double> E = new ArrayList<>();
        double S = 0;
        for (double[] p : P) {
            double eval_solution = MOP2f1(p) + MOP2f2(p);
            E.add(eval_solution);
            S += eval_solution;
        }
        // Prob. selectie individuala probI
        List<Double> probI = new ArrayList<>();
        for (double e : E) {
            double prob = e/S;
            probI.add(prob);
        }
        // Prob. selectie cumulata probC
        List<Double> probC = new ArrayList<>();
        probC.add(probI.get(0));
        for (int i=1; i<probI.size(); i++) {
            probC.add(probC.get(i-1) + probI.get(i));
        }
        // Selectia: Q
        List<double[]> Q = new ArrayList<>();
        for (int i=0; i<P.size(); i++) {
            double rand = (new Random()).nextDouble();
            int index = 0;
            for (int k=0; k<P.size(); k++) {
                if (rand <= probC.get(k)) {
                    index = k;
                    break;
                }
            }
            Q.add(P.get(index));
        }
        return Q;
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
    
    public static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
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
