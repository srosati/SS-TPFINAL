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
    private double radius;
    private double length;
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
        return Double.compare(distance, radius + other.radius) <= 0;
    }

    public DoubleTriad calculateForces() {
        DoubleTriad totForce = new DoubleTriad(0, mass * (Constants.DESIRED_VELOCITY - predV.getSecond()) / Constants.PROP_FACTOR, 0);

        if (Double.isNaN(predV.getSecond())) {
            System.out.println("NaN");
        }

        for (Particle neighbour : neighbours) {
            DoubleTriad force = getForcesWith(neighbour);
            totForce = totForce.plus(force);

            // Me podria guardar de alguna forma este calculo para el choque inverso
            // Fij = -Fji
            // Tarda mucho asi como esta
//            DoubleTriad otherForce = neighbour.getForcesWith(this);
//            System.out.printf("(%f, %f),  (%f, %f),  (%f, %f)\n", force.getFirst(), otherForce.getFirst(), force.getSecond(), otherForce.getSecond(), force.getThird(), otherForce.getThird());
        }

        return totForce;
    }

    private DoublePair[] getVertices() {
        double dx = (length / 2) * Math.cos(next[R.POS].getThird());
        double dy = (length / 2) * Math.sin(next[R.POS].getThird());
        return new DoublePair[] {
                new DoublePair(next[R.POS].getFirst() - dx, next[R.POS].getSecond() - dy),
                new DoublePair(next[R.POS].getFirst() + dx, next[R.POS].getSecond() + dy)
        };
    }

    private DoubleTriad getForcesWith(Particle other) {
        DoublePair[] vertices = getVertices();
        DoublePair[] otherVertices = other.getVertices();

        DoubleTriad totForce = new DoubleTriad(0, 0, 0);
        for (DoublePair vertex: vertices) {
            DoubleTriad force = getForceBetween(vertex, otherVertices, other, false);
            totForce = totForce.plus(force);
        }

        for (DoublePair otherVertex: otherVertices) {
            DoubleTriad force = getForceBetween(otherVertex, vertices, other, true);
            totForce = totForce.plus(force);
        }

        return totForce;
    }

    private DoubleTriad getForceBetween(DoublePair vertex, DoublePair[] edge, Particle other, boolean isOther) {
        DoublePair closestPoint = MathUtils.closestPointOnSegment(edge[0], edge[1], vertex);
        double overlap = radius + other.radius - vertex.distanceTo(closestPoint);
        if (overlap <= 0)
            return new DoubleTriad(0, 0, 0);

        DoublePair normalVec = isOther ? vertex.minus(closestPoint) : closestPoint.minus(vertex);
        DoublePair normalVerser = normalVec.asVerser();

        DoublePair overlapCenter = vertex.plus(normalVerser.times((radius + other.radius) / 2).times(isOther ? -1 : 1));

        double normalForce = -Constants.KN * overlap;
        double tanForce = tangentialForce(other, normalVerser, overlap, overlapCenter);

        double fx = normalForce * normalVerser.getFirst() - tanForce * normalVerser.getSecond();
        double fy = normalForce * normalVerser.getSecond() + tanForce * normalVerser.getFirst();

        DoubleTriad force = new DoubleTriad(fx, fy, 0);
        DoublePair distanceToCenter = overlapCenter.minus(next[R.POS]);

        // (x1, y1, 0) x (x2, y2, 0) = (0, 0, x1y2 - x2y1)
        double torque = distanceToCenter.crossProduct(force);
//        System.out.println("Torque: " + torque);
        force.setThird(torque);
        return force;
    }

    private double tangentialForce(double rVx, double rVy, DoublePair normalVerser, double overlap) {
        double relativeVt = -rVx * normalVerser.getSecond() + rVy * normalVerser.getFirst();
        return -Constants.KT * overlap * relativeVt;
    }

    private double tangentialForce(Particle other, DoublePair normalVerser, double overlap, DoublePair collisionCenter) {
        DoublePair relativeSpeed = getRelativeVelocity(other, collisionCenter);
        return tangentialForce(relativeSpeed.getFirst(), relativeSpeed.getSecond(), normalVerser, overlap);
    }

    //CHECK
    private DoublePair getRelativeVelocity(Particle other, DoublePair point) {
        DoublePair v1AtPoint = velocityAtPoint(point);
        DoublePair v2AtPoint = other.velocityAtPoint(point);

        return v1AtPoint.minus(v2AtPoint);
    }

    //CHECK
    private DoublePair velocityAtPoint(DoublePair point) {
        DoublePair distance = point.minus(next[R.POS]);
        DoublePair distanceVerser = distance.asVerser();
        double dist = distance.module();

        double linearSpeed = predV.getThird() * dist;

        return new DoublePair(predV.getFirst() - linearSpeed * distanceVerser.getSecond(),
                predV.getSecond() + linearSpeed * distanceVerser.getFirst());
    }

    public void addNeighbour(Particle neighbour) {
        neighbours.add(neighbour);
    }

    public void removeAllNeighbours() {
        neighbours.clear();
    }

    public double getMomentOfInertia() {
        // Calculate mass for rectangle and circle section
        double circleArea = Math.PI * radius * radius;
        double rectArea =  length * 2 * radius;
        double rectMassPercent = rectArea / (circleArea + rectArea);

        // Moment of inertia for rectangle shaped part
        // width is length, height is radius
        double rectMass = mass * rectMassPercent;
        double rectMoment = rectMass * (length * length + 4 * radius * radius) / 12;

        // Moment of inertia for semicircles at each end
        // each semicircle is at length/2 from the center of mass
        double circleMass = mass * (1 - rectMassPercent);
        double circleMoment = circleMass * (radius * radius / 2 + length * length / 4); // Teorema de Steiner

        // Aditive moments of inertia
        return rectMoment + circleMoment;
    }

    public int getId() {
        return id;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
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

}

