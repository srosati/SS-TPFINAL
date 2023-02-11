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
    private double nextRadius;
    private double nextLength;
    private final double mass;
    private final double area;
    private final double INITIAL_RADIUS;
    private final double lag;

    private final Set<Particle> neighbours = new HashSet<>();
    private final DoubleTriad[] curr = new DoubleTriad[3];
    private final DoubleTriad[] prev = new DoubleTriad[3];
    private final DoubleTriad[] next = new DoubleTriad[3];
    private DoubleTriad predV;

    public Particle(double radius, double length, double lag, DoubleTriad position) {
        this.id = SEQ++;
        this.mass = Constants.MASS;
        this.INITIAL_RADIUS = radius;
        this.area = (length * 2 * radius) + (Math.PI * radius * radius);
        this.lag = lag;
        this.calculateNewSize(0);
        this.radius = this.nextRadius;
        this.length = this.nextLength;
        this.setCurr(R.POS, position);
        predV = new DoubleTriad(0.0, 0.0, 0.0);
    }

    public Particle(int id, double radius, double length, double lag, DoubleTriad position) {
        this.id = id;
        SEQ++;
        this.mass = Constants.MASS;
        this.INITIAL_RADIUS = radius;
        this.area = (length * 2 * radius) + (Math.PI * radius * radius);
        this.lag = lag;
        this.calculateNewSize(0);
        this.radius = this.nextRadius;
        this.length = this.nextLength;
        this.setCurr(R.POS, position);
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
        return MathUtils.minDistanceBetweenSegments(curr[R.POS], length, other.curr[R.POS], other.length); //CHECK es next o current ?
    }

    public boolean isColliding(Particle other) {
        if (this.equals(other))
            return false;

        double distance = this.distanceTo(other);
        return Double.compare(distance, nextRadius + other.nextRadius) <= 0;
    }

    public DoubleTriad calculateForces() {
        DoubleTriad totForce = new DoubleTriad(0, mass * (Constants.DESIRED_VELOCITY - predV.getSecond()) / Constants.PROP_FACTOR, 0);

        if (Double.isNaN(predV.getSecond())) {
//            System.out.println("NaN");
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
        double dx = (nextLength / 2) * Math.cos(next[R.POS].getThird());
        double dy = (nextLength / 2) * Math.sin(next[R.POS].getThird());
        return new DoublePair[]{
                new DoublePair(next[R.POS].getFirst() - dx, next[R.POS].getSecond() - dy),
                new DoublePair(next[R.POS].getFirst() + dx, next[R.POS].getSecond() + dy)
        };
    }

    private DoubleTriad getForcesWith(Particle other) {
        DoublePair[] vertices = getVertices();
        DoublePair[] otherVertices = other.getVertices();

        DoubleTriad totForce = new DoubleTriad(0, 0, 0);
        for (DoublePair vertex : vertices) {
            DoubleTriad force = getForceBetween(vertex, otherVertices, other, false);
            totForce = totForce.plus(force);
        }

        for (DoublePair otherVertex : otherVertices) {
            DoubleTriad force = getForceBetween(otherVertex, vertices, other, true);
            totForce = totForce.plus(force);
        }

        return totForce;
    }

    private DoubleTriad getForceBetween(DoublePair vertex, DoublePair[] edge, Particle other, boolean isOther) {
        DoublePair closestPoint = MathUtils.closestPointOnSegment(edge[0], edge[1], vertex);
        double overlap = nextRadius + other.nextRadius - vertex.distanceTo(closestPoint);
        if (overlap <= 0)
            return new DoubleTriad(0, 0, 0);

        DoublePair normalVec = isOther ? vertex.minus(closestPoint) : closestPoint.minus(vertex);
        DoublePair normalVerser = normalVec.asVerser();

//        DoublePair overlapCenter = vertex.plus(normalVerser.times((nextRadius + other.nextRadius) / 2).times(isOther ? -1 : 1));

        DoublePair overlapCenter = (vertex.times(isOther ? other.radius : radius)
                .plus(closestPoint.times(isOther ? radius : other.radius)))
                .times(1 / (radius + other.radius));
        double normalForce = -Constants.KN * overlap;

        double tanForce;
        if (!isOther) {
            tanForce = tangentialForce(other, normalVerser, overlap, vertex, closestPoint);
        } else {
            tanForce = tangentialForce(other, normalVerser, overlap, closestPoint, vertex);
        }

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

    private double tangentialForce(Particle other, DoublePair normalVerser, double overlap, DoublePair firstPoint, DoublePair secondPoint) {
        DoublePair relativeSpeed = getRelativeVelocity(other, firstPoint, secondPoint);
        double relativeVt = -relativeSpeed.getFirst() * normalVerser.getSecond() +
                relativeSpeed.getSecond() * normalVerser.getFirst();
        return -Constants.KT * overlap * relativeVt;
    }

    private DoublePair getRelativeVelocity(Particle other, DoublePair firstPoint, DoublePair secondPoint) {
        DoublePair viAtPoint = velocityAtPoint(firstPoint); //!isOther ? vertex : closestPoint
        DoublePair vjAtPoint = other.velocityAtPoint(secondPoint); //isOther ? closestPoint : vertex

        return viAtPoint.minus(vjAtPoint);
    }

    //CHECK
    private DoublePair velocityAtPoint(DoublePair point) {
        DoublePair distance = point.minus(next[R.POS]);
        double dist = distance.module();
        if (dist == 0)
            return predV;

        DoublePair distanceVerser = distance.asVerser();

        double linearSpeed = predV.getThird() * dist;
        DoublePair rotSpeed = new DoublePair(
                -distanceVerser.getSecond() * linearSpeed,
                distanceVerser.getFirst() * linearSpeed
        );

        return rotSpeed.plus(predV);
    }

    public void addNeighbour(Particle neighbour) {
        neighbours.add(neighbour);
    }

    public void removeAllNeighbours() {
        neighbours.clear();
    }

    public void calculateNewSize(double elapsed) {
//        this.nextLength = INITIAL_LENGTH + 0.5 * Math.sin(this.lag + elapsed * Constants.ANGULAR_W);
//
//        //r_1 = r_0 - (A - 2 * l * r_0 - π * r_0^2) / (2 * l + 2 * π * r_0)
//        double prevRadius = radius;
//        //To calculate the new radius, we use the Newton-Raphson method because it is a non-linear equation
//        double nextRadius = prevRadius - getNewtonRaphsonR(prevRadius, length) / getNewtownRaphsonDr(prevRadius, length);
//
//        //We iterate until the difference between the previous and the next radius is less than 0.0001
//        while (Math.abs(nextRadius - prevRadius) > 0.0001) {
//            prevRadius = nextRadius;
//            nextRadius = prevRadius - getNewtonRaphsonR(prevRadius, length) / getNewtownRaphsonDr(prevRadius, length);
//        }
//        this.nextRadius = nextRadius;

        this.nextRadius = INITIAL_RADIUS + 0.1 * Math.sin(this.lag + elapsed * Constants.ANGULAR_W);
        this.nextLength = (area - Math.PI * Math.pow(nextRadius, 2)) / (2 * nextRadius);
    }

    public void updateSize() {
        radius = nextRadius;
        length = nextLength;
    }

//    private double getNewtonRaphsonR(double prevRadius, double length) {
//        return (area - Math.PI * prevRadius * prevRadius) / (2 * length) - prevRadius;
//    }
//
//    private double getNewtownRaphsonDr(double prevRadius, double length) {
//        return -2 * Math.PI * prevRadius / (2 * length) - 1;
//    }

    public double getMomentOfInertia() {
        // Calculate mass for rectangle and circle section
        double circleArea = Math.PI * nextRadius * nextRadius;
        double rectArea = nextLength * 2 * nextRadius;
        double rectMassPercent = rectArea / (circleArea + rectArea);

        // Moment of inertia for rectangle shaped part
        // width is length, height is radius
        double rectMass = mass * rectMassPercent;
        double rectMoment = rectMass * (nextLength * nextLength + 4 * nextRadius * nextRadius) / 12;

        // Moment of inertia for semicircles at each end
        // each semicircle is at length/2 from the center of mass
        double circleMass = mass * (1 - rectMassPercent);
        double circleMoment = circleMass * (nextRadius * nextRadius / 2 + nextLength * nextLength / 4); // Teorema de Steiner

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

    public double getArea() {
        return area;
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

    public double getNextRadius() {
        return nextRadius;
    }

    public void setNextRadius(double nextRadius) {
        this.nextRadius = nextRadius;
    }

    public double getNextLength() {
        return nextLength;
    }

    public void setNextLength(double nextLength) {
        this.nextLength = nextLength;
    }

    public double getLag() {
        return lag;
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

