/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metrics;

import java.util.List;

/**
 *
 * @author ashcrok
 */
public class Delta {
    
    public void printDelta(List<double[]> pop) {
        double DELTA = 0.0;
        double davg = 0.0;
        for (int k = 1; k < pop.size(); k++)
            davg += d(pop.get(k-1), pop.get(k));
        davg = davg / (pop.size() - 1);
        for (int k = 1; k < pop.size(); k++)
            DELTA += Math.abs(d(pop.get(k-1),pop.get(k)) - davg) / pop.size();
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
