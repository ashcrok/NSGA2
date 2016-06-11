/*
 * File created by Mihai Pricop
 *                 (ashcrok)
 */
package moead;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import metrics.Hypervolume;

/**
 *
 * @author mihaipricop
 */
public class MOEAD {
    
    public static final int N = 20; // Population size - the number of subproblems
    public static final int T = 5; // Number of weight vectors in the neighborhood
    public static final int ITERATIONS = 1000;
    
    public static functions.F func = new functions.MOP.MOP2();
    
    // --------------------------------------------------------------------- //
    
    public void run() {
        // Step 1 - Initialization
        List<double[]> l = getLambdaDistribution();
        List<List<double[]>> B = closestWeightVectors(l);
        List<double[]> P = new ArrayList<>();
        List<double[]> FV = new ArrayList<>();
        for (int i = 0; i < N; i++)
            P.add(func.generate());
        for (double[] sol : P) {
            func.set(sol); FV.add(func.evaluate()); }
        List<double[]> Z = initZ();
        
        // Step 2 - Update
        for (int i = 0; i < N; i++) {
            
        }
        
        
    }
    
    // --------------------------------------------------------------------- //
    
    private List<double[]> getLambdaDistribution() {
        List<double[]> l = new ArrayList<>();
        for (int k = 0; k < N; k++) {
            double[] lambda = new double[2];
            lambda[0] = (double) k / (double) (N - 1);
            lambda[1] = 1 - lambda[0];
            l.add(lambda);
        }
        return l;
    }
    
    private List<List<double[]>> closestWeightVectors(List<double[]> l) {
        List<List<double[]>> B = new ArrayList<>();
        l.stream().map((wv) -> {
            double[] d = new double[l.size()];
            for (int i=0; i<l.size(); i++)
                d[i] = d(wv,l.get(i));
            List<double[]> b = new ArrayList<>();
            for (int k=0; k<T; k++) {
                double minDistance = Integer.MAX_VALUE;
                int minDistanceIndex = -1;
                for (int i=0; i<d.length; i++)
                    if (d[i] < minDistance) {
                        minDistance = d[i];
                        minDistanceIndex = i;
                    }
                d[minDistanceIndex] = Double.MAX_VALUE;
                b.add(l.get(minDistanceIndex));
            }
            return b;
        }).forEach((b) -> {
            B.add(b);
        });
        return B;
    }
    
    private List<double[]> initZ() {
        List<double[]> z = new ArrayList<>();
        for (int i = 0; i < func.getNoObjectives(); i++) {
            double[] sol = null;
            double min = Double.MAX_VALUE;
            for (int j = 0; j < 100; j++) {
                double[] new_sol = func.generate();
                func.set(new_sol);
                double[] fitness = func.evaluate();
                if (fitness[i] < min) {
                    sol = new_sol;
                    min = fitness[i];
                }
            }
            z.add(sol);
        }
        return z;
    }
    
    private double gws(double x[], double[] l) {
        func.set(x); double[] fitness = func.evaluate();
        return fitness[0] * l[0] + fitness[1] * l[1];
    }
    
    // --------------------------------------------------------------------- //
    
    public List<double[]> gaInitPopulation() {
        List<double[]> pop = new ArrayList<>();
        for (int i = 0; i < 30; i++)
            pop.add(func.generate());
        return pop;
    }
    
    public List<double[]> gaAlgorithm(List<double[]> pop) {
        List<double[]> Q = new ArrayList<>();
        List<double[]> new_pop = new ArrayList<>();
        for (int i=0; i<Q.size()-1; i=i+2) {
            double[] parent1 = Q.get(i);
            double[] parent2 = Q.get(i+1);
        }
        return Q;
    }
    
    // --------------------------------------------------------------------- //
    
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
    
    public void printDelta(List<double[]> pop) {
        double DELTA = 0.0;
        double davg = 0.0;
        for (int k = 1; k < pop.size(); k++)
            davg += d(pop.get(k-1), pop.get(k));
        davg = davg / (pop.size() - 1);
        for (int k = 1; k < pop.size(); k++)
            DELTA += Math.abs(d(pop.get(k-1),pop.get(k)) - davg) / N;
        System.out.println("DELTA VALUE: " + DELTA);
        System.out.println("-------\n");
    }
    
    public double d(double[] x1, double[] x2) {
        double sum = 0.0;
        for (int i=0; i<x1.length; i++)
            sum += Math.pow(x1[i] - x2[i], 2.0);
        return Math.sqrt(sum);
    }
    
}
