
import functions.F;
import functions.ZDT.ZDT1;
import org.rosuda.JRI.Rengine;


public class test {
    
    // --------------------------------------------------------------------- //
    
    // .jcall(.jnew("test"),,"t1");
    public void t1() {
        System.out.println("Primul test");
        System.out.println("Al doilea test");
        System.out.println("Si un al treilea test");
    }
    
    // --------------------------------------------------------------------- //
    
    // .jcall(.jnew("test"),,"t2");
    public void t2() {
        Function function = (double[] sol) -> {
            double[] f = new double[2];
            f[0] = sol[0];
            // Calculate G
            double g = 0.0;
            for (int i = 1; i < sol.length; i++)
                g += sol[i];
            double constant = 9.0 / (sol.length - 1);
            g = constant * g;
            g = g + 1.0;
            // Calculate H
            double h = 1.0 - Math.sqrt(f[0] / g);;
            f[1] = h * g;
            return f;
        };
        
        double[] result = compute(new double[]{1.0,2.0,3.0},function);
        System.out.println(result[0] + " " + result[1]);
        
        F f = new ZDT1();
        f.set(new double[]{1.0,2.0,3.0});
        System.out.println(f.evaluate()[0] + " " + f.evaluate()[1]);
    }
    
    interface Function {
        double[] computation(double[] sol);
    }
    
    private double[] compute(double[] sol, Function function) {
        return function.computation(sol);
    }
    
    // --------------------------------------------------------------------- //
    
    // .jcall(.jnew("test"),"[D","t3",1.0,2.0);
    public double[] t3(double x, double y) {
        double[] result = new double[2];
        result[0] = x;
        result[1] = y;
        return result;
    }
    
    // --------------------------------------------------------------------- //
    
    
    // .jcall(.jnew("test"),,"t3","string");
    public void t4(String input) {
        System.out.println(input);
    }
    
}
