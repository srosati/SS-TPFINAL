package main.java.ar.edu.itba.ss.models;

public class DoubleTriad extends DoublePair {
    private double third;

    public DoubleTriad(double first, double second, double third) {
        super(first, second);
        this.third = third;
    }

    public double getThird() {
        return third;
    }

    public void setThird(double third) {
        this.third = third;
    }


}
