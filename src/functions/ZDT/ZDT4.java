
package functions.ZDT;

import functions.F;

public class ZDT4 extends F { // Not working right
    
    private final int MIN = -5;
    private final int MAX = 5;
    private static int M;
    private static int n;
    private double[] sol;
    
    // --------------------------------------------------------------------- //
    
    public ZDT4() {
        ZDT4.M = 2;
        ZDT4.n = 10;
        sol = new double[n];
        sol[0] = Math.random();
        for (int i = 1; i < n; i++)
            sol[i] = MIN + (MAX - MIN) * Math.random();
    }
    public ZDT4(int noVariables) {
        ZDT4.M = 2;
        ZDT4.n = noVariables;
        sol = new double[n];
        sol[0] = Math.random();
        for (int i = 1; i < n; i++)
            sol[i] = MIN + (MAX - MIN) * Math.random();
    }
    
    @Override
    public double[] generate() {
        double[] solution = new double[n];
        solution[0] = Math.random();
        for (int i = 1; i < n; i++)
            solution[i] = MIN + (MAX - MIN) * Math.random();
        return solution;
    }
    
    // --------------------------------------------------------------------- //
    
    @Override
    public double[] evaluate() {
        double[] f = new double[M];
        f[0] = sol[0];
        double g = this.evalG();
        double h = this.evalH(f[0],g);
        f[1] = h * g;
        return f;
    }
    
    private double evalG() {
        double g = 0.0;
        for (int i = 1; i < sol.length; i++)
            g += Math.pow(sol[i],2.0) + -10.0 * Math.cos(4.0 * Math.PI * sol[i]);
        g += 1.0 + 10 * (sol.length - 1);
        return g;
    }
    
    private double evalH(double f, double g) {
        double h;
        h = 1.0 - Math.pow(f*g, 2.0);
        return h;
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
