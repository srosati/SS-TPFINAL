package ar.edu.itba.ss.utils;

import ar.edu.itba.ss.models.DoublePair;
import ar.edu.itba.ss.models.DoubleTriad;

public class MathUtils {
    public static double minDistanceBetweenSegments(DoubleTriad pos1, double length1, DoubleTriad pos2, double length2) {
        DoublePair[] points1 = getPoints(pos1, length1);
        if (length2 == 0.0) // PARED
            return minDistanceBetweenSegments(points1[0], points1[1], pos2);

        DoublePair[] points2 = getPoints(pos2, length2);
        return minDistanceBetweenSegments(points1[0], points1[1], points2[0], points2[1]);
    }

    public static double minDistanceBetweenSegments(DoubleTriad pos, double length, DoublePair p1, DoublePair p2) {
        DoublePair[] points = getPoints(pos, length);
        return minDistanceBetweenSegments(points[0], points[1], p1, p2);
    }

    private static DoublePair[] getPoints(DoubleTriad position, double length) {
        double dX = length * Math.cos(position.getThird()) / 2;
        double dY = length * Math.sin(position.getThird()) / 2;
        DoublePair p1 = new DoublePair(position.getFirst() + dX, position.getSecond() + dY);
        DoublePair p2 = new DoublePair(position.getFirst() - dX, position.getSecond() - dY);
        return new DoublePair[]{p1, p2};
    }

    public static double minDistanceBetweenSegments(DoublePair p1, DoublePair p2, DoublePair p3, DoublePair p4) {
        double minDistance = Double.MAX_VALUE;
        double distance = minDistanceBetweenSegments(p1, p2, p3);
        if (distance < minDistance)
            minDistance = distance;

        distance = minDistanceBetweenSegments(p1, p2, p4);
        if (distance < minDistance)
            minDistance = distance;

        distance = minDistanceBetweenSegments(p3, p4, p1);
        if (distance < minDistance)
            minDistance = distance;

        distance = minDistanceBetweenSegments(p3, p4, p2);
        if (distance < minDistance)
            minDistance = distance;

        return minDistance;
    }

    private static double minDistanceBetweenSegments(DoublePair p1, DoublePair p2, DoublePair p3) {
        DoublePair closestPoint = closestPointOnSegment(p1, p2, p3);
        return p3.distanceTo(closestPoint);
    }



    /*
    * Get closest points on two line segments
    * Segment one starts at p1 and ends at p2
    * Segment two starts at p3 and ends at p4
    * Returns an array of two DoublePair objects
    * The first object is the closest point on segment one
    * The second object is the closest point on segment two
    */
    public static DoublePair[] closestPointsBetweenSegments(DoublePair p1, DoublePair p2, DoublePair p3, DoublePair p4) {
        DoublePair closestPoint1 = closestPointOnSegment(p1, p2, p3);
        double distance = closestPoint1.distanceTo(p3);

        double minDistance = distance;
        DoublePair[] closestPoints = new DoublePair[]{closestPoint1, p3};

        DoublePair closestPoint2 = closestPointOnSegment(p1, p2, p4);
        distance = closestPoint2.distanceTo(p4);
        if (distance < minDistance) {
            minDistance = distance;
            closestPoints = new DoublePair[]{closestPoint2, p4};
        }

        DoublePair closestPoint3 = closestPointOnSegment(p3, p4, p1);
        distance = closestPoint3.distanceTo(p1);
        if (distance < minDistance) {
            minDistance = distance;
            closestPoints = new DoublePair[]{p1, closestPoint3};
        }

        DoublePair closestPoint4 = closestPointOnSegment(p3, p4, p2);
        distance = closestPoint4.distanceTo(p2);
        if (distance < minDistance)
            closestPoints = new DoublePair[]{p2, closestPoint4};

        return closestPoints;
    }

    public static DoublePair[] closestPointsBetweenSegments(DoubleTriad pos1, double length1, DoubleTriad pos2, double length2) {
        DoublePair[] points1 = getPoints(pos1, length1);
        if (length2 == 0.0) { // PARED
            DoublePair closestPoint = closestPointOnSegment(points1[0], points1[1], pos2);
            return new DoublePair[]{closestPoint, pos2};
        }

        DoublePair[] points2 = getPoints(pos2, length2);
        return closestPointsBetweenSegments(points1[0], points1[1], points2[0], points2[1]);
    }

    public static DoublePair[] closestPointsBetweenSegments(DoubleTriad pos, double length, DoublePair p1, DoublePair p2) {
        DoublePair[] points = getPoints(pos, length);
        return closestPointsBetweenSegments(points[0], points[1], p1, p2);
    }

    public static DoublePair closestPointOnSegment(DoublePair segmentP1, 
                                                   DoublePair segmentP2, 
                                                   DoublePair otherPoint) {
        double xDelta = segmentP2.getFirst() - segmentP1.getFirst();
        double yDelta = segmentP2.getSecond() - segmentP1.getSecond();
        double d2 = xDelta * xDelta + yDelta * yDelta;
        if (d2 == 0)
            return segmentP1;

        double u = ((otherPoint.getFirst() - segmentP1.getFirst()) * xDelta + (otherPoint.getSecond() - segmentP1.getSecond()) * yDelta) / d2;
        if (u < 0)
            return segmentP1;

        if (u > 1)
            return segmentP2;

        return new DoublePair(segmentP1.getFirst() + u * xDelta, segmentP1.getSecond() + u * yDelta);
    }

    public static double getNewRadius(double elapsed, double initialRadius, double lag) {
        return initialRadius + Constants.RADIUS_AMPLITUDE * Math.sin(lag + elapsed * 2 * Math.PI * Constants.RADIUS_FREQUENCY);
    }

    public static double getNewLength(double area, double radius) {
        return (area - Math.PI * Math.pow(radius, 2)) / (2 * radius);
    }
}
