package ar.edu.itba.ss.utils;

import org.apache.commons.math3.analysis.interpolation.BicubicInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolator;

public class MomentOfInertiaCalculator {
    public static BicubicInterpolatingFunction INTERPOLATION_FUNCTION = getInterpolatingFunction();

    private static final double STEP = 0.0005;

    private static boolean isWithin(double length, double radius, double x, double y) {
        if (x <= length/2 && x >= -length/2)
            return true;

        double dx = Math.abs(x) - length/2; // distance from center of circle
        return Math.sqrt(dx*dx + y*y) <= radius;
    }

    private static double calculateMomentOfInertia(double length, double radius) {
        double tot = 0;
        double maxX = length / 2 + radius;
        int count = 0;
        for (double x = -maxX; x < maxX; x += STEP) {
            for (double y = -radius; y < radius; y += STEP) {
                if (!isWithin(length, radius, x, y))
                    continue;

                tot += x*x + y*y;
                count++;
            }
        }

        return tot / (count);
    }

    private static BicubicInterpolatingFunction interpolate(double[] lengths, double[] radiuses, double[][] momentsOfInertia) {
        BicubicInterpolator interpolator = new BicubicInterpolator();
        return interpolator.interpolate(lengths, radiuses, momentsOfInertia);
    }

    private static BicubicInterpolatingFunction getInterpolatingFunction() {
        System.out.println("Interpolating moments of inertia");
        double step = 0.1;

        double min_radius = Constants.MIN_RADIUS - Constants.RADIUS_AMPLITUDE - step;
        double max_radius = Constants.MAX_RADIUS + Constants.RADIUS_AMPLITUDE + step;

        double min_length = 0;
        double max_length = 3.1;

        int lengthCount = (int) ((max_length - min_length) / step) + 1;
        int radiusCount = (int) ((max_radius - min_radius) / step) + 1;
        double[] lengthsToCalculate = new double[lengthCount];
        double[] radiusesToCalculate = new double[radiusCount];
        for (int i = 0; i < lengthCount; i++) {
            lengthsToCalculate[i] = min_length + i * step;
        }

        for (int i = 0; i < radiusCount; i++) {
            radiusesToCalculate[i] = min_radius + i * step;
        }

        double[][] momentsOfInertia = new double[lengthCount][radiusCount];

        for (int i = 0; i < lengthCount; i++) {
            for (int j = 0; j < radiusCount; j++) {
                momentsOfInertia[i][j] = calculateMomentOfInertia(lengthsToCalculate[i], radiusesToCalculate[j]);
            }
        }

        BicubicInterpolatingFunction interpolatingFunction = interpolate(lengthsToCalculate, radiusesToCalculate, momentsOfInertia);

        System.out.println("Done interpolating moments of inertia");
        // Interpolar para obtener una función continua
        return interpolatingFunction;

    }
}