
package paes;

import functions.F;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PAES {
    
    private final int ITERATIONS = 100000;
    private final int MAX_SIZE = 100;
        
    private final F func = new functions.DTLZ.DTLZ5();
    
    // --------------------------------------------------------------------- //
    
    public void run() {
        
        List<double[]> archive = new ArrayList<>();
        
        archive.add(func.generate());
        for (int i = 0; i < ITERATIONS || archive.size() < MAX_SIZE; i++) {
//            double[] current_solution = archive.get(new Random().nextInt(archive.size()));
            double[] current_solution;
            if (archive.size() > 5) current_solution = this.getBestCandidate(archive);
            else current_solution = archive.get(new Random().nextInt(archive.size()));
            double[] new_solution = mutation(current_solution);
            if (dominates(current_solution,new_solution)) {
                // do nothing
            } else if (dominates(new_solution,current_solution)) {
                archive.remove(current_solution);
                archive.add(new_solution);
                updateArchive(archive,new_solution);
            } else if (isDominatedByArchive(new_solution,archive)) {
                // do nothing
            } else {
                archive.add(new_solution);
                updateArchive(archive,new_solution);
                if (archive.size() > MAX_SIZE)
                    archive.remove(getWorstCandidate(archive));
            }
        }
        
        this.printFormated(archive);
        
    }
    
    // --------------------------------------------------------------------- //
    
    private boolean isDominatedByArchive(double[] solution, List<double[]> archive) {
        return archive.stream().anyMatch((x) -> (dominates(x,solution)));
    }
    
    private void updateArchive(List<double[]> archive, double[] solution) {
        List<double[]> clone = new ArrayList<>(archive);
        clone.stream().filter((x) -> (dominates(solution,x))).forEach((x) -> {
            archive.remove(x);
        });
    }
    
    private double[] getBestCandidate(List<double[]> archive) {
        List<double[]> fitness = new ArrayList<>();
        for (double[] sol : archive) {
            func.set(sol); fitness.add(func.evaluate());
        }
        List<Double> crowd = new ArrayList<>();
        for (int i = 0; i < archive.size(); i++)
            crowd.add(0.0);
        for (int i = 0; i < func.getNoObjectives(); i++) {
            double[] x = new double[archive.size()];
            for (int j = 0; j < fitness.size(); j++)
                x[j] = fitness.get(j)[i];
            double[] localCrowd = crowd(x);
            for (int j = 0 ; j < crowd.size(); j++)
                crowd.set(j,crowd.get(j) + localCrowd[j]);
        }
        double max = Double.MIN_VALUE;
        int index = -1;
        for (int i = 0; i < crowd.size(); i++) {
            if (crowd.get(i) > max) {
                max = crowd.get(i);
                index = i;
            }
        }
        return archive.get(index);
    }
    
    private double[] getWorstCandidate(List<double[]> archive) {
        List<double[]> fitness = new ArrayList<>();
        for (double[] sol : archive) {
            func.set(sol); fitness.add(func.evaluate());
        }
        List<Double> crowd = new ArrayList<>();
        for (int i = 0; i < archive.size(); i++)
            crowd.add(0.0);
        for (int i = 0; i < func.getNoObjectives(); i++) {
            double[] x = new double[archive.size()];
            for (int j = 0; j < fitness.size(); j++)
                x[j] = fitness.get(j)[i];
            double[] localCrowd = crowd(x);
            for (int j = 0 ; j < crowd.size(); j++)
                crowd.set(j,crowd.get(j) + localCrowd[j]);
        }
        double min = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < crowd.size(); i++) {
            if (crowd.get(i) < min) {
                min = crowd.get(i);
                index = i;
            }
        }
        return archive.get(index);
    }
    
    private double[] crowd(double[] fit) {
        // init
        double[] fit2 = new double[fit.length];
        List<Integer> mid = new ArrayList<>();
        for (int i = 0; i < fit.length; i++) {
            mid.add(i);
            fit2[i] = fit[i];
        }
        // Bubble sort
        boolean swapped = true;
        while(swapped) {
            swapped = false;
            for (int i = 1; i < fit2.length; i++) {
                if (fit2[i-1] > fit2[i]) {
                    swapped = true;
                    double temp = fit2[i-1];
                    fit2[i-1] = fit2[i];
                    fit2[i] = temp;
                    int temp2 = mid.get(i-1);
                    mid.set(i-1,mid.get(i));
                    mid.set(i,temp2);
                }
            }
        }
        // crowd
        double[] score = new double[fit2.length];
        score[0] = fit2[1] - fit2[0];
        score[score.length-1] = fit2[score.length-1] - fit2[score.length-2];
        for (int i = 1; i < fit2.length - 1; i++) {
            score[i] = fit2[i+1] - fit2[i-1];
        }
        // final crowd
        double[] crowd = new double[fit2.length];
        for (int i = 0; i < crowd.length; i++) {
            crowd[i] = score[mid.indexOf(i)];
        }
        return crowd;
    }
    
    // --------------------------------------------------------------------- //
    
    public void printFormated(List<double[]> pop) {
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
    
    // Dominance operator
    private boolean dominates(double[] x, double[] y) {
        func.set(x); double[] f = func.evaluate();
        func.set(y); double[] g = func.evaluate();
        return (f[0] <= g[0] && f[1] <= g[1] && (f[0] < g[0] || f[1] < g[1]));
    }
    
    private double[] mutation(double[] p) {
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
    
    // DOUBLE TO BITS AND BACK
    private String doubleToBinary(double x) {
        int d = 6;
        double N = (func.getMaxValue() - func.getMinValue()) * Math.pow(10,d);
        int n = (int) Math.ceil(Math.log(N) / Math.log(2));
        long decimal = (long) ((x - func.getMinValue()) * (Math.pow(2,n) - 1) / (func.getMaxValue() - func.getMinValue()));
        String binary = Long.toBinaryString(decimal);
        if (binary.length() < 5) binary = "000" + binary;
        return binary;
    }
    private double binaryToDouble(String b) {
        int d = 6;
        double N = (func.getMaxValue() - func.getMinValue()) * Math.pow(10,d);
        int n = (int) Math.ceil(Math.log(N) / Math.log(2));
        long decimal = Long.parseLong(b, 2);
        return (func.getMinValue() + decimal * (func.getMaxValue() - func.getMinValue()) / (Math.pow(2,n) - 1));
    }
    
    // --------------------------------------------------------------------- //
    
}
