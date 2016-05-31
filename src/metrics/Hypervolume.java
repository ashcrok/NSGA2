/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metrics;

/**
 *
 * @author ashcrok
 */
public class Hypervolume {
    
    boolean  dominates(double  x[], double  y[]) {
        return (MOP2f1(x) <= MOP2f1(y) && MOP2f2(x) <= MOP2f2(y) && (MOP2f1(x) < MOP2f1(y) || MOP2f2(x) < MOP2f2(y)));
    } //Dominates
    
    void  swap(double [][] front, int  i, int  j) {
        double[] temp;
        temp = front[i];
        front[i] = front[j];
        front[j] = temp;
    } // Swap 
  
  
    // --------------------------------------------------------------------- //
    
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
    
    // --------------------------------------------------------------------- //
    
}
