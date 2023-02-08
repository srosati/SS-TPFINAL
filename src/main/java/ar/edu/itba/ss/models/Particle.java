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
        double fx = 0;
        double fy = mass * (Constants.DESIRED_VELOCITY - predV.getSecond()) / Constants.PROP_FACTOR;
        double fw = 0;

        if (Double.isNaN(predV.getSecond())) {
            System.out.println("NAN");
        }

        for (Particle neighbour : neighbours) {
//            DoublePair[] closestPoints = MathUtils.closestPointsBetweenSegments(curr[R.POS], length,
//                    neighbour.curr[R.POS], neighbour.length);

            DoublePair p1Center = new DoublePair(next[R.POS].getFirst(), next[R.POS].getSecond());
            double dx = length * Math.cos(next[R.POS].getThird()) + radius;
            double dy = length * Math.sin(next[R.POS].getThird()) + radius;
            DoublePair[] p1V = new DoublePair[]{
                    new DoublePair(p1Center.getFirst() + dx, p1Center.getSecond() + dy),
                    new DoublePair(p1Center.getFirst() - dx, p1Center.getSecond() - dy)
            };

            DoublePair p2Center = new DoublePair(neighbour.next[R.POS].getFirst(), neighbour.next[R.POS].getSecond());
            dx = neighbour.length * Math.cos(neighbour.next[R.POS].getThird()) + neighbour.radius;
            dy = neighbour.length * Math.sin(neighbour.next[R.POS].getThird()) + neighbour.radius;
            DoublePair[] p2V = new DoublePair[]{
                    new DoublePair(p2Center.getFirst() + dx, p2Center.getSecond() + dy),
                    new DoublePair(p2Center.getFirst() - dx, p2Center.getSecond() - dy)
            };


            for (DoublePair v : p1V) {
                DoubleTriad forces = getForces(p2V, v, radius, neighbour.radius, predV, neighbour.predV);
                fx += forces.getFirst();
                fy += forces.getSecond();
                fw += forces.getThird();
            }

            for (DoublePair v : p2V) {
                DoubleTriad forces = getForces(p1V, v, radius, neighbour.radius, neighbour.predV, predV);
                fx += forces.getFirst();
                fy += forces.getSecond();
                fw += forces.getThird();
            }

//            DoublePair normalVerser = getCollisionVerser(closestPoints);
//
//            double overlap = getOverlap(closestPoints, neighbour);
//            if (overlap < 0) {
//                continue;
//            }
//            double fn = -Constants.KN * overlap;
//            double ft = tangentialForce(neighbour, normalVerser, overlap);
//
//            double forceX = fn * normalVerser.getFirst() - ft * normalVerser.getSecond();
//            double forceY = fn * normalVerser.getSecond() + ft * normalVerser.getFirst();
//
//            fx += forceX;
//            fy += forceY;
//
//            double overlapX = (closestPoints[0].getFirst() + closestPoints[1].getFirst()) / 2;
//            double overlapY = (closestPoints[0].getSecond() + closestPoints[1].getSecond()) / 2;
//
////            double rx = next[R.POS].getFirst() - closestPoints[0].getFirst();
////            double ry = next[R.POS].getSecond() - closestPoints[0].getSecond();
//
//            double rx = overlapX - next[R.POS].getFirst();
//            double ry = overlapY - next[R.POS].getSecond();
//
//            double forceAngle = Math.atan2(forceY, forceX);
//            double collisionAngle = Math.atan2(ry, rx);
//            double angle = forceAngle - collisionAngle;
//
//            fw += (forceX * rx + forceY * ry) * Math.sin(angle);
            // FIXME: Esto no anda bien
//            double dist = closestPoints[0].distanceTo(next[R.POS]);
//            fw += fn * dist;
        }

        return new DoubleTriad(fx, fy, fw);
    }

    private DoubleTriad getForces(DoublePair[] edge, DoublePair vertex, double r1, double r2, DoublePair v1, DoublePair v2) {
        DoublePair closestPoint = MathUtils.closestPointOnSegment(edge[0], edge[1], vertex);
        double distance = vertex.distanceTo(closestPoint);
        DoublePair normalVerser = new DoublePair((closestPoint.getFirst() - vertex.getFirst()) / distance,
                (closestPoint.getSecond() - vertex.getSecond()) / distance);
        double overlap = Math.max(r1 + r2 - vertex.distanceTo(closestPoint), 0);
        if (overlap == 0)
            return new DoubleTriad(0,0,0);

        double normalForce = Constants.KN * overlap;
//        double tangentialForce = -tangentialForce(neighbour, normalVerser, overlap); //TODO: ver si el menos es equivalente (entendemos que si)

        double rVx = v1.getFirst() - v2.getFirst();
        double rVy = v1.getSecond() - v2.getSecond();

        double relativeVt = -rVx * normalVerser.getSecond() + rVy * normalVerser.getFirst();

        double tangentialForce = -Constants.KT * overlap * relativeVt;

        double forceX = normalForce * normalVerser.getFirst() - tangentialForce * normalVerser.getSecond();
        double forceY = normalForce * normalVerser.getSecond() + tangentialForce * normalVerser.getFirst();

        double overlapCenterX = (vertex.getFirst() + closestPoint.getFirst()) / 2;
        double overlapCenterY = (vertex.getSecond() + closestPoint.getSecond()) / 2;

        double dx = overlapCenterX - next[R.POS].getFirst();
        double dy = overlapCenterY - next[R.POS].getSecond();

        double dist = Math.sqrt(dx * dx + dy * dy);
        double force = Math.sqrt(forceX * forceX + forceY * forceY);

        double angle = Math.acos((forceX * dx + forceY * dy) / (dist * force));
        double torque = dist * force * Math.sin(angle);

        return new DoubleTriad(forceX, forceY, torque);
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

    public double getOverlap(DoublePair[] closestPoints, Particle other) {
        double distance = closestPoints[0].distanceTo(closestPoints[1]);
        return radius + other.getRadius() - distance;
    }

    public void addNeighbour(Particle neighbour) {
        neighbours.add(neighbour);
    }

    public void removeAllNeighbours() {
        neighbours.clear();
    }

    public DoublePair getCollisionVerser(DoublePair[] closestPoints) {
        DoublePair first = closestPoints[0];
        DoublePair second = closestPoints[1];
        double dx = second.getFirst() - first.getFirst();
        double dy = second.getSecond() - first.getSecond();

        double dR = Math.sqrt(dx * dx + dy * dy);

        return new DoublePair(dx / dR, dy / dR);
    }

    public double getMomentOfInertia() {
        // Calculate mass for rectangle and circle section
        double circleArea = Math.PI * radius * radius;
        double rectArea = 2 * length * radius;
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

