package ar.edu.itba.ss.models;

public class DoubleTriad extends DoublePair {
    private double third;

    public DoubleTriad(double first, double second, double third) {
        super(first, second);
        this.third = third;
    }

    public DoubleTriad plus(DoubleTriad other) {
        return new DoubleTriad(getFirst() + other.getFirst(), getSecond() + other.getSecond(), third + other.getThird());
    }

    public double getThird() {
        return third;
    }



    public void setThird(double third) {
        this.third = third;
    }


}
