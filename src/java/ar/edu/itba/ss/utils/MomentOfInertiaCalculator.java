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

        System.out.println("Calculating moments of inertia");
        // Definir las longitudes para las que calcular el momento de inercia
        double min_radius = 0.55;
        double max_radius = 1.05;
        double min_length = 0.01;
        double max_length = 3;
        double step = 0.05;

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

        // Interpolar para obtener una función continua
        return interpolate(lengthsToCalculate, radiusesToCalculate, momentsOfInertia);

//        // Ejemplo de evaluación de la función interpolada para una longitud específica
//        double lengthToEvaluate = 0.92;
//        double radiusToEvaluate = 0.88;
//        double result = interpolationFunction.value(lengthToEvaluate, radiusToEvaluate);
//
//        System.out.println("El momento de inercia interpolado para longitud " + lengthToEvaluate + " y radio " + radiusToEvaluate + " es: " + result);
//        System.out.println("El momento de inercia numerico es " + calculateMomentOfInertia(lengthToEvaluate, radiusToEvaluate));
    }
}