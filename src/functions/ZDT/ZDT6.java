
package functions.ZDT;

import functions.F;

public class ZDT6 extends F { // not working right
    
    private final int MIN = 0;
    private final int MAX = 1;
    private static int M;
    private static int n;
    private double[] sol;
    
    // --------------------------------------------------------------------- //
    
    public ZDT6() {
        ZDT6.M = 2;
        ZDT6.n = 30;
        sol = new double[n];
        for (int i = 0; i < n; i++)
            sol[i] = MIN + (MAX - MIN) * Math.random();
    }
    public ZDT6(int noVariables) {
        ZDT6.M = 2;
        ZDT6.n = noVariables;
        sol = new double[n];
        for (int i = 0; i < n; i++)
            sol[i] = MIN + (MAX - MIN) * Math.random();
    }
    
    @Override
    public double[] generate() {
        double[] solution = new double[n];
        for (int i = 0; i < n; i++)
            solution[i] = MIN + (MAX - MIN) * Math.random();
        return solution;
    }
    
    // --------------------------------------------------------------------- //
    
    @Override
    public double[] evaluate() {
        double[] f = new double[M];
        f[0] = 1 - Math.pow(Math.E, -4 * sol[0]) * Math.pow(Math.sin(6 * Math.PI * sol[0]), 6);
        double g = 0.0;
        for(int i = 1; i < sol.length; i++)
            g += sol[i];
        g = 1 + 9 * Math.pow(g / 9, 1 / 4);
        double h = 1 - f[0] / g;
        f[1] = g * h;
        return f;
    }
    
    // --------------------------------------------------------------------- //
    
    @Override
    public double[] get() { return this.sol; }
    @Override
    public void set(double[] sol) { this.sol = sol; }
    @Override
    public int getMinValue() { return this.MIN; }
    @Override
    public int getMaxValue() { return this.MAX; }
    @Override
    public int getNoObjectives() { return M; }
    
}
