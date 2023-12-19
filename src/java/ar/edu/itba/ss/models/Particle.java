package ar.edu.itba.ss.models;

import ar.edu.itba.ss.utils.Constants;
import ar.edu.itba.ss.utils.Integration;
import ar.edu.itba.ss.utils.MathUtils;
import ar.edu.itba.ss.utils.MomentOfInertiaCalculator;

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

    public Particle(double radius, double length, double lag, DoubleTriad position, double initial_radius) {
        this.id = SEQ++;
        this.mass = Constants.MASS;
        this.INITIAL_RADIUS = initial_radius;
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

    public double distanceTo(DoubleTriad other, double otherLength) {
        return MathUtils.minDistanceBetweenSegments(curr[R.POS], length, other, otherLength); // TODO: CHECK es next o current ?
    }

    public boolean isColliding(Particle other) {
        if (this.equals(other))
            return false;

        double distance = this.distanceTo(other.curr[R.POS], other.length);
        return Double.compare(distance, radius + other.radius) <= 0;
    }

    public boolean isColliding(DoubleTriad otherPos, double otherR, double otherL) {
        double distance = distanceTo(otherPos, otherL);
        return Double.compare(distance, radius + otherR) <= 0; // TODO: Recontra check, aca uso next y arriba curr
    }



    public DoubleTriad calculateForces() {
        DoubleTriad totForce = new DoubleTriad(
                0,
                mass * (Constants.DESIRED_VELOCITY - predV.getSecond()) / Constants.PROP_FACTOR,
                0);

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
        // Por cada uno de mis vertices, me fijo en los lados del otro
        for (DoublePair vertex : vertices) {
            DoubleTriad force = getForceBetween(vertex, otherVertices, other, false);
            totForce = totForce.plus(force);
        }

        // Por cada uno de mis lados, me fijo en los vertices del otro
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
        force.setThird(distanceToCenter.crossProduct(force));
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
        this.nextRadius = MathUtils.getNewRadius(elapsed, INITIAL_RADIUS, lag);
        this.nextLength = MathUtils.getNewLength(area, nextRadius);
    }

    public void updateSize() {
        radius = nextRadius;
        length = nextLength;
    }

    public double getMomentOfInertia() {
        return MomentOfInertiaCalculator.INTERPOLATION_FUNCTION.value(nextLength, nextRadius);
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

