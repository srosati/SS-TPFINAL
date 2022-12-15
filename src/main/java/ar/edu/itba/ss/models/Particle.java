package main.java.ar.edu.itba.ss.models;

import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.Integration;
import main.java.ar.edu.itba.ss.utils.MathUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Particle {
    private static int SEQ = 0;
    private final int id;
    private final double radius;
    private final double length;
    private final double mass;

    private final Set<Particle> neighbours = new HashSet<>();

    private final DoubleTriad[] curr = new DoubleTriad[3];
    private final DoubleTriad[] prev = new DoubleTriad[3];
    private final DoubleTriad[] next = new DoubleTriad[3];
    private DoubleTriad predV;

    public Particle(double radius, double length, DoubleTriad position) {
        this.id = SEQ++;
        this.mass = Constants.MASS;
        this.radius = radius;
        this.length = length;
        this.setCurr(R.POS, position);
        predV = new DoubleTriad(0.0, 0.0, 0.0);
    }

    public Particle(int id, double radius, double length, DoubleTriad position) {
        this.id = id;
        SEQ++;
        this.mass = Constants.MASS;
        this.radius = radius;
        this.length = length;
        this.setCurr(R.POS, position);
        predV = new DoubleTriad(0.0, 0.0, 0.0);
    }

    public void initRs() {
        curr[R.VEL] = new DoubleTriad(0.0, 0.0, 0.0);
        double acc = Constants.DESIRED_VELOCITY / Constants.PROP_FACTOR;
        curr[R.ACC] = new DoubleTriad(0.0, acc, 0.0);

        prev[R.POS] = new DoubleTriad(
                Integration.eulerR(curr[R.POS].getFirst(), 0.0, -Constants.STEP, mass, 0),
                Integration.eulerR(curr[R.POS].getSecond(), 0.0, -Constants.STEP, mass, mass * acc),
                Integration.eulerR(curr[R.POS].getThird(), 0.0, -Constants.STEP, mass, 0)
        );

        prev[R.VEL] = new DoubleTriad(
                Integration.eulerV(0.0, -Constants.STEP, mass, 0),
                Integration.eulerV(0.0, -Constants.STEP, mass, acc * mass),
                Integration.eulerV(0.0, -Constants.STEP, mass, 0)
        );

        prev[R.ACC] = new DoubleTriad(0.0, acc, 0.0);
    }

    public double distanceTo(Particle other) {
        return MathUtils.minDistanceBetweenSegments(curr[R.POS], length, other.curr[R.POS], other.length);
    }
    public boolean isColliding(Particle other) {
        if (this.equals(other))
            return false;

        double distance = this.distanceTo(other);
        return distance <= radius + other.radius;
    }

    public DoubleTriad calculateForces() {
        double fx = 0;
        double fy = mass * (Constants.DESIRED_VELOCITY - predV.getSecond()) / Constants.PROP_FACTOR;
        double fw = 0; // TODO: Impl

        if (Double.isNaN(predV.getSecond()) ) {
            System.out.println("NAN");
        }

        for (Particle neighbour : neighbours) {
            DoublePair normalVerser = getCollisionVerser(neighbour);
            double overlap = getOverlap(neighbour);
            double fn = -Constants.KN * overlap;
            double ft = tangentialForce(neighbour, normalVerser, overlap);

            fx += fn * normalVerser.getFirst() - ft * normalVerser.getSecond();
            fy += fn * normalVerser.getSecond() + ft * normalVerser.getFirst();
        }

        return new DoubleTriad(fx, fy, fw);
    }

    private double tangentialForce(double rVx, double rVy, DoublePair normalVerser, double overlap) {
        double relativeVt = -rVx * normalVerser.getSecond() + rVy * normalVerser.getFirst();
        return -Constants.KT * overlap * relativeVt;
    }

    private double tangentialForce(Particle other, DoublePair normalVerser, double overlap) {
        double relativeVx = predV.getFirst() - other.getPredV().getFirst();
        double relativeVy = predV.getSecond() - other.getPredV().getSecond();
        return tangentialForce(relativeVx, relativeVy, normalVerser, overlap);
    }

    public double getOverlap(Particle other) {
        double distance = this.distanceTo(other);
        return Math.abs(radius + other.getRadius() - distance);
    }

    public void addNeighbour(Particle neighbour) {
        neighbours.add(neighbour);
    }

    public void removeAllNeighbours() {
        neighbours.clear();
    }

    public DoublePair getCollisionVerser(Particle other) {
        DoublePair[] closestPoints = MathUtils.closestPointsBetweenSegments(next[R.POS], length, other.next[R.POS],
                other.length);

        DoublePair first = closestPoints[0];
        DoublePair second = closestPoints[1];
        double dx = second.getFirst() - first.getFirst();
        double dy = second.getSecond() - first.getSecond();
        // FIXME: La colision no es de centro a centro, pero si descomento lo de arriba tira NANs

//        double dx = other.getNext(R.POS).getFirst() - next[R.POS].getFirst();
//        double dy = other.getNext(R.POS).getSecond() - next[R.POS].getSecond();

        double dR = Math.sqrt(dx * dx + dy * dy);

        return new DoublePair(dx / dR, dy / dR);
    }

    public int getId() {
        return id;
    }

    public double getRadius() {
        return radius;
    }

    public double getMass() {
        return mass;
    }

    public Set<Particle> getNeighbours() {
        return neighbours;
    }

    public DoubleTriad getNext(int index) {
        return next[index];
    }

    public DoubleTriad getPredV() {
        return predV;
    }

    public void setPredV(DoubleTriad predV) {
        this.predV = predV;
    }

    public void setPrev(int index, DoubleTriad pair) {
        prev[index] = pair;
    }

    public void setNextR(int index, DoubleTriad pair) {
        this.next[index] = pair;
    }

    public DoubleTriad getCurrent(int index) {
        return curr[index];
    }

    public void setCurr(int index, DoubleTriad triad) {
        curr[index] = triad;
    }

    public DoubleTriad getPrev(int index) {
        return prev[index];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Particle particle = (Particle) o;
        return getId() == particle.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public double getLength() {
        return length;
    }
}

