package main.java.ar.edu.itba.ss.models;

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

    public DoublePair manDistanceTo(DoublePair other) {
        return other.minus(this);
    }

    public DoublePair plus(DoublePair other) {
        return new DoublePair(first + other.getFirst(), second + other.getSecond());
    }

    public DoublePair minus(DoublePair other) {
        return new DoublePair(first - other.getFirst(), second - other.getSecond());
    }

    public double module() {
        return Math.sqrt(Math.pow(first, 2) + Math.pow(second, 2));
    }

    public DoublePair versor() {
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
}
