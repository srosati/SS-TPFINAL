package main.java.ar.edu.itba.ss.utils;

import main.java.ar.edu.itba.ss.models.DoublePair;
import main.java.ar.edu.itba.ss.models.DoubleTriad;

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
//    public static DoublePair[] closestPointsBetweenSegments(DoublePair p1, DoublePair p2, DoublePair p3, DoublePair p4) {
//        DoublePair closestPoint1 = closestPointOnSegment(p1, p2, p3);
//        DoublePair closestPoint2 = closestPointOnSegment(p3, p4, closestPoint1);
//        double distance = closestPoint1.distanceTo(closestPoint2);
//
//        double minDistance = distance;
//        DoublePair[] closestPoints = new DoublePair[]{closestPoint1, closestPoint2};
//
//        DoublePair closestPoint3 = closestPointOnSegment(p1, p2, p4);
//        DoublePair closestPoint4 = closestPointOnSegment(p3, p4, closestPoint3);
//        distance = closestPoint3.distanceTo(closestPoint4);
//        if (distance < minDistance) {
//            minDistance = distance;
//            closestPoints = new DoublePair[]{closestPoint3, closestPoint4};
//        }
//
//        DoublePair closestPoint5 = closestPointOnSegment(p3, p4, p1);
//        DoublePair closestPoint6 = closestPointOnSegment(p1, p2, closestPoint5);
//        distance = closestPoint5.distanceTo(closestPoint6);
//        if (distance < minDistance) {
//            minDistance = distance;
//            closestPoints = new DoublePair[]{closestPoint5, closestPoint6};
//        }
//
//        DoublePair closestPoint7 = closestPointOnSegment(p3, p4, p2);
//        DoublePair closestPoint8 = closestPointOnSegment(p1, p2, closestPoint7);
//        distance = closestPoint7.distanceTo(closestPoint8);
//        if (distance < minDistance)
//            closestPoints = new DoublePair[]{closestPoint7, closestPoint8};
//
//        return closestPoints;
//    }

    //TODO: mirar
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

    public static DoublePair closestPointOnSegment(DoublePair p1, DoublePair p2, DoublePair p3) {
        double xDelta = p2.getFirst() - p1.getFirst();
        double yDelta = p2.getSecond() - p1.getSecond();
        double d2 = xDelta * xDelta + yDelta * yDelta;
        if (d2 == 0)
            return p1;

        double u = ((p3.getFirst() - p1.getFirst()) * xDelta + (p3.getSecond() - p1.getSecond()) * yDelta) / d2;
        if (u < 0)
            return p1;

        if (u > 1)
            return p2;

        return new DoublePair(p1.getFirst() + u * xDelta, p1.getSecond() + u * yDelta);
    }
}
