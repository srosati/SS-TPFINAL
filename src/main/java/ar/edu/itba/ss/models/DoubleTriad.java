package main.java.ar.edu.itba.ss.models;

public class DoubleTriad extends DoublePair {
    private double third;

    public DoubleTriad(double first, double second, double third) {
        super(first, second);
        this.third = third;
    }

    // Distance to another DoubleTriad
    public double distanceTo(DoubleTriad other) {
        return Math.sqrt(Math.pow(other.getFirst() - getFirst(), 2) +
                    Math.pow(other.getSecond() - getSecond(), 2) + 
                    Math.pow(other.getThird() - third, 2));
    }

    public double getThird() {
        return third;
    }

    public void setThird(double third) {
        this.third = third;
    }


}
