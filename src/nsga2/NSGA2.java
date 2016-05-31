
package nsga2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class NSGA2 {
    
    public static final int POPULATION = 50;
    public static final int ITERATIONS = 50;
    
    public static final int MIN = -4;
    public static final int MAX = 4;
    
    
    public static void main(String[] args) {
        
        long startTime = System.currentTimeMillis();
        
        // Initial population
        List<double[]> P = new ArrayList<>();
        for (int i=0; i<POPULATION; i++) {
            double x1 = (Math.random() * (MAX-MIN)) - MAX;
            double x2 = (Math.random() * (MAX-MIN)) - MAX;
            double x3 = (Math.random() * (MAX-MIN)) - MAX;
            double x[] = {x1,x2,x3};
            P.add(x);
        }
        
        // Q is generated with genetic operators
        List<double[]> Q = selection2(P);
        
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
                } else {
                    P.addAll(front);
                }
            }
            
            // GA Operators
            Q = selection2(P);
        }
        
        // Display la rezulate si statistici peste rezultalte
        List<double[]> R = P; R.addAll(Q);
        clearDuplicates(R);
        List<List<double[]>> fronts = fast_nondominated_sort(R);
        List<double[]> first_front = fronts.get(0);
        long endTime = System.currentTimeMillis();
        System.out.println("size: " + first_front.size());
        System.out.println("time: " + ((endTime - startTime) * 0.001));
        System.out.println("----------");
        Set<Double> diff_values = new TreeSet<>();
        
        String o1 = "o1 <- c(";
        for (double[] x : first_front) {
            diff_values.add(x[0]);
            String r = "DTLZ5_f1(c(";
            for (int i=0; i<x.length; i++) {
                r += x[i] + ", ";
            }
            r = r.substring(0,r.length()-2);
            r += "))";
            o1 += r + ",\n";
        }
        o1 = o1.substring(0,o1.length()-2);
        o1 += ")";
        
        String o2 = "o2 <- c(";
        for (double[] x : first_front) {
            String r = "DTLZ5_f2(c(";
            for (int i=0; i<x.length; i++) {
                r += x[i] + ", ";
            }
            r = r.substring(0,r.length()-2);
            r += "))";
            o2 += r + ",\n";
        }
        o2 = o2.substring(0,o2.length()-2);
        o2 += ")";
        
        System.out.println(o1 + "\n\n");
        System.out.println(o2 + "\n\n");
        
        System.out.println("----------");
        System.out.println("different values: " + diff_values.size());
        
        // Calculate DELTA
        double DELTA = 0;
        double davg = 0;
        for (int k=1; k<first_front.size(); k++)
            davg += d(first_front.get(k-1),first_front.get(k));
        davg = davg / (first_front.size()-1);
        for (int k=1; k<first_front.size(); k++)
            DELTA += Math.abs(d(first_front.get(k-1),first_front.get(k)) - davg) / POPULATION;
        System.out.println("DELTA: " + DELTA);
        
    }
    
    public static void clearDuplicates(List<double[]> R) {
        for (int i=0; i<R.size()-1; i++) {
            for (int j=i+1; j<R.size(); j++) {
                double[] p = R.get(i);
                double[] q = R.get(j);
                boolean duplicate = true;
                if (p[0] != q[0])
                    duplicate = false;
                if (duplicate) {
                    double x1 = (Math.random() * (MAX-MIN)) - MAX;
                    double x2 = (Math.random() * (MAX-MIN)) - MAX;
                    double x3 = (Math.random() * (MAX-MIN)) - MAX;
                    double x[] = {x1,x2,x3};
                    R.set(i, x);
                }
            }
        }
    }
    
    public static double d(double[] x1, double[] x2) {
        double sum = 0.0;
        for (int i=0; i<x1.length; i++)
            sum += Math.pow(x1[i] - x2[i], 2.0);
        return Math.sqrt(sum);
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
        
        // f1
        List<Double> frontF1 = new ArrayList<>(); 
        for (double[] sol : front) frontF1.add(MOP2f1(sol));
        Collections.sort(frontF1);
        
        
        // f2
    }
    
    // Operatia dubioasa
    public static boolean op(double[] x, double[] y) {
        double[] f = DTLZ5(x,2); // f[0] = MOP2f1(x); f[1] = MOP2f2(x)
        double[] g = DTLZ5(y,2); // g[0] = MOP2f1(y); g[1] = MOP2f2(y)
        return (f[0] <= g[0] && f[1] <= g[1] && (f[0] < g[0] || f[1] < g[1]));
    }
    
    public static void sort(List<double[]> P) {
        P.stream().forEach((p) -> {
            P.stream().filter((q) -> (!op(p,q))).forEach((q) -> {
                double[] temp = p;
                P.set(P.indexOf(p), q);
                P.set(P.indexOf(q), temp);
            });
        });
    }
    
    /*-------------------------------------*/
    
    // Mutatie cu sansa de 1/n
    public static List<double[]> mutation(List<double[]> Q) {
        int C = 4;
        double[] q = Q.get((new Random()).nextInt(Q.size()));
        for (int i=0;i<q.length;i++)
            System.out.print(q[i] + " ");
        double[] q_new = new double[q.length];
        for (int i=0; i<q.length; i++) { do {
            double n = q[i] + C;
            String bval = Long.toBinaryString(Double.doubleToLongBits(n));
            int cut = (new Random()).nextInt(bval.length() - 1);
            char c = bval.charAt(cut);
            if (c == '0') c = '1'; else c = '0';
            bval = bval.substring(0,cut) + c + bval.substring(cut+1);
            q_new[i] = Double.longBitsToDouble(Long.parseLong(bval, 2)) - C;
        } while(q_new[i] < -4 || q_new[i] > 4); }
        Q.add(q_new);
        return Q;
    }
    
    // Incrucisare prin 2 puncte de taiere
    public static List<double[]> crossover(List<double[]> P) {
        List<double[]> Q = new ArrayList<>(P);
        int index1 = (new Random()).nextInt(Q.size());
        int index2 = (new Random()).nextInt(Q.size());
        double[] parent1 = Q.get(index1);
        double[] parent2 = Q.get(index2);
        double[] child1 = new double[parent1.length], child2 = new double[parent2.length];
        for (int i=0; i<parent1.length; i++) { do {
            child1[i] = getChild1(parent1[i]+4.0,parent2[i]+4.0)-4.0;
            child2[i] = getChild2(parent1[i]+4.0,parent2[i]+4.0)-4.0;
        } while(child1[i] < -4 || child1[i] > 4 || child2[i] < -4 || child2[i] > 4); }
        Q.remove(index1); Q.add(child1);
        Q.remove(index2); Q.add(child2);
        return Q;
    }
    public static double getChild1(double parent1, double parent2) {
        String binaryString1 = Long.toBinaryString(Double.doubleToLongBits(parent1));
        String binaryString2 = Long.toBinaryString(Double.doubleToLongBits(parent2));
        int cutPoint1 = (new Random()).nextInt(binaryString1.length()/2-1);
        int cutPoint2 = (new Random()).nextInt(binaryString1.length()/2-1) + binaryString1.length()/2;
        String child = binaryString1.substring(0,cutPoint1) + binaryString2.substring(cutPoint1, cutPoint2)
                 + binaryString1.substring(cutPoint2);
        return Double.longBitsToDouble(Long.parseLong(child,2));
    }
    public static double getChild2(double parent1, double parent2) {
        String binaryString1 = Long.toBinaryString(Double.doubleToLongBits(parent1));
        String binaryString2 = Long.toBinaryString(Double.doubleToLongBits(parent2));
        int cutPoint1 = (new Random()).nextInt(binaryString1.length()/2-1);
        int cutPoint2 = (new Random()).nextInt(binaryString1.length()/2-1) + binaryString1.length()/2;
        String child = binaryString2.substring(0,cutPoint1) + binaryString1.substring(cutPoint1, cutPoint2)
                 + binaryString2.substring(cutPoint2);
        return Double.longBitsToDouble(Long.parseLong(child,2));
    }
    
    public static List<double[]> selection(List<double[]> P) {
        List<double[]> Q = new ArrayList<>();
        double k = 0.30; // aproximativ 70% din populatie va fi selectata mai departe
        P.stream().filter((indiv) -> ((new Random()).nextDouble() < k)).forEach((indiv) -> {
            Q.add(indiv);
        });
        double j = 0.30; // cei mai buni 70% vor fi selectati pentru supravietuire
        sort(Q);
        List<double[]> new_pop = new ArrayList<>();
        for (int i=0; i<Q.size()*j; i++)
            new_pop.add(mutation(Q.get(i)));
        //  Adaugam indivizi pana umplem populatia; facem asta aleatoriu, 
        // dar trebuie facut cu mutatie si incrucisare
        while (new_pop.size() < P.size()) {
            double x1 = (Math.random() * (MAX-MIN)) - MAX;
            double x2 = (Math.random() * (MAX-MIN)) - MAX;
            double x3 = (Math.random() * (MAX-MIN)) - MAX;
            double x[] = {x1,x2,x3};
            new_pop.add(x);
            // mutatie
            // incrucisare
        }
        return new_pop;
    }
    
    /*-------------------------------------*/
    
    public static double[] mutation(double[] p) {
        double q[] = new double[p.length];
        for (int i=0; i<p.length; i++) {
            String bval = doubleToBinary(q[i]);
            int cut = (new Random()).nextInt(bval.length() - 1);
            char c = bval.charAt(cut);
            if (c == '0') c = '1'; else c = '0';
            bval = bval.substring(0,cut) + c + bval.substring(cut+1);
            q[i] = binaryToDouble(bval);
        }
        return q;
    }
    public static double[] crossover(double[] p1, double[] p2) {
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
    public static List<double[]> selection2(List<double[]> P) {
        List<double[]> Q = new ArrayList<>(P);
        List<double[]> new_pop = new ArrayList<>();
        for (int i=0; i<Q.size()-1; i=i+2) {
            double[] parent1 = Q.get(i);
            double[] parent2 = Q.get(i+1);
            double[] bro = crossover(parent1,parent2);
            double[] sis = crossover(parent2,parent1);
            new_pop.add(bro);
            new_pop.add(sis);
        }
        return new_pop;
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
    
    /*-------------------------------------*/
    
    public static double MOP4f1(double x[]) {
        double result = 0;
        for (int i=0; i<x.length-1; i++)
            result += -10.0 * Math.exp(-0.2 * Math.sqrt(Math.pow(x[i],2) + Math.pow(x[i+1],2)));
        return result;
    }
    public static double MOP4f2(double x[]) {
        double result = 0;
        for (int i=0; i<x.length; i++)
            result += Math.pow(Math.abs(x[i]),0.8) + 5 * Math.pow(Math.sin(x[i]),3.0);
        return result;
    }
    
    /*-------------------------------------*/
    
    public static double[] DTLZ5(double[] x, int noObjectives) {
        double[] f = new double[noObjectives];
        
        int k = x.length - noObjectives + 1;
        double g = 0.0;
        for (int i=x.length-k; i<x.length; i++)
            g += Math.pow((x[i] - 0.5),2);
        
        double t = Math.PI / (4.0 * (1.0 + g));
        
        double[] theta = new double[noObjectives - 1];
        theta[0] = x[0] * Math.PI / 2;
        for (int i=1; i<noObjectives-1; i++)
            theta[i] = t * (1.0 + 2.0 * g * x[i]);
        
        for (int i=0; i<noObjectives; i++) {
            f[i] = 1.0 + g;
        }
        for (int i=0; i<noObjectives; i++) {
            for (int j = 0; j < noObjectives - (i + 1); j++) {
                if (i==1) System.out.println("asd");
                f[i] *= Math.cos(theta[j]);
            }
            if (i != 0) {
                int aux = noObjectives - (i + 1);
                f[i] *= Math.sin(theta[aux]);
            }
        }
        return f;
    }
    
    /*-------------------------------------*/
    
    // DOUBLE TO BITS AND BACK
    private static String doubleToBinary(double x) {
        int d = 6;
        double N = (MAX - MIN) * Math.pow(10,d);
        int n = (int) Math.ceil(Math.log(N) / Math.log(2));
        long decimal = (long) ((x - MIN) * (Math.pow(2,n) - 1) / (MAX - MIN));
        return Long.toBinaryString(decimal);
    }
    private static double binaryToDouble(String b) {
        int d = 6;
        double N = (MAX - MIN) * Math.pow(10,d);
        int n = (int) Math.ceil(Math.log(N) / Math.log(2));
        long decimal = Long.parseLong(b, 2);
        return (MIN + decimal * (MAX - MIN) / (Math.pow(2,n) - 1));
    }
    
    /*-------------------------------------*/
    
}
