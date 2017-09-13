package com.dabomstew.pkrandom;

//Simple quadratic interpolation
public class Curve {
    public Curve(int[] pts, int[] values) {
        x = pts;
        if (pts.length != values.length) {
            values = pts;
            System.out.println("Curve error, number of points does not match number of values");
        }
        y = new double[x.length];
        for (int k = 0; k < x.length; k++)
            y[k] = (double) values[k];
    }

    public int eval(double t) {
        if(t >= 100) return (int) Math.round(t);
        int k = 0;
        while (k < x.length && t >= x[k])
            k++;
        k--;
        double a;
        if (k == x.length - 1)
            a = (double) y[k];
        else
            a = linear_eval(t, x[k], x[k + 1], y[k], y[k + 1]);
        return convert(a);
    }

    double linear_eval(double t, double lo, double hi, double a, double b) {
        // Map t to value in [0, 1] depending on location in interval
        double t0 = (t - lo) / (hi - lo);
        // Use that value as a weight for the two linear multipliers
        return a * (1 - t0) + b * t0;
    }

    int convert(double x) {
        int k = (int) Math.round(x);
        if (k < 2)
            return 2;
        if (k > 100)
            return 100;
        return k;
    }

    int[] x;
    double[] y;
}
