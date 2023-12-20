package ar.edu.itba.ss.models;

import java.util.Objects;

public class DoublePair {
    private double first;
    private double second;

    public DoublePair(double first, double second) {
        this.first = first;
        this.second = second;
    }

    public double distanceTo(DoublePair other) {
        return Math.sqrt(Math.pow(other.getFirst() - first, 2) + Math.pow(other.getSecond() - second, 2));
    }
    
    public DoublePair plus(DoublePair other) {
        return new DoublePair(first + other.getFirst(), second + other.getSecond());
    }

    public DoublePair minus(DoublePair other) {
        return new DoublePair(first - other.getFirst(), second - other.getSecond());
    }

    public DoublePair times(double scalar) {
        return new DoublePair(first * scalar, second * scalar);
    }

    public double module() {
        return Math.sqrt(Math.pow(first, 2) + Math.pow(second, 2));
    }

    public double crossProduct(DoublePair other) {
        // (x1, y1, 0) x (x2, y2, 0) = (0, 0, x1y2 - x2y1)
        return first * other.getSecond() - second * other.getFirst();
    }

    public DoublePair asVerser() {
        double norm = module();
        return new DoublePair(first / norm, second / norm);
    }

    public double getFirst() {
        return first;
    }

    public void setFirst(double first) {
        this.first = first;
    }

    public double getSecond() {
        return second;
    }

    public void setSecond(double second) {
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoublePair that = (DoublePair) o;
        return Double.compare(that.first, first) == 0 && Double.compare(that.second, second) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
