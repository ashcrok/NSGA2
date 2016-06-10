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
    
    public static final int POPULATION = 20;
    public static final int ITERATIONS = 100;
    
    public static functions.F func = new functions.ZDT.ZDT1();
    
    // --------------------------------------------------------------------- //
    
    public void run() {
        List<double[]> P = new ArrayList<>();
        // Get Lambda distribution
        List<double[]> l = getLamdbdaDistribution();
        for (int i = 0; i < POPULATION; i++) {
            // Get subproblem and optimize
            double min = Double.MAX_VALUE;
            double sol[] = null;
            for (int j = 0; j < ITERATIONS; j++) {
                double[] x = func.generate();
                if (gws(x,l.get(i)) < min) {
                    sol = x;
                    min = gws(x,l.get(i));
                }
            }
            P.add(sol);
        }
        
        printFirstFrontFormated(P);
        printDelta(P);
        printHypervolume(P);
        
    }
    
    // --------------------------------------------------------------------- //
    
    private static List<double[]> getLamdbdaDistribution() {
        List<double[]> l = new ArrayList<>();
        for (int k = 0; k < POPULATION; k++) {
            double[] lambda = new double[2];
            lambda[0] = (double) k / (double) (POPULATION - 1);
            lambda[1] = 1 - lambda[0];
            l.add(lambda);
        }
        return l;
    }
    
    private static double gws(double x[], double[] l) {
        func.set(x); double[] fitness = func.evaluate();
        return fitness[0] * l[0] + fitness[1] * l[1];
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
    
}
