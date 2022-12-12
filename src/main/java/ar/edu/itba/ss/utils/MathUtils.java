package main.java.ar.edu.itba.ss.utils;

import main.java.ar.edu.itba.ss.models.DoublePair;

public class MathUtils {
    public static double minDistanceBetweenSegments(DoublePair center1, double length1, double rotation1, DoublePair center2, double length2, double rotation2) {
        DoublePair[] points1 = getPoints(center1, length1, rotation1);
        DoublePair[] points2 = getPoints(center2, length2, rotation2);
        return minDistanceBetweenSegments(points1[0], points1[1], points2[0], points2[1]);
    }

    public static double minDistanceBetweenSegments(DoublePair center, double length, double rotation, DoublePair p1, DoublePair p2) {
        DoublePair[] points = getPoints(center, length, rotation);
        return minDistanceBetweenSegments(points[0], points[1], p1, p2);
    }

    private static DoublePair[] getPoints(DoublePair center, double length, double rotation) {
        double dX = length * Math.cos(rotation) / 2;
        double dY = length * Math.sin(rotation) / 2;
        DoublePair p1 = new DoublePair(center.getFirst() + dX, center.getSecond() + dY);
        DoublePair p2 = new DoublePair(center.getFirst() - dX, center.getSecond() - dY);
        return new DoublePair[]{p1, p2};
    }

    public static double minDistanceBetweenSegments(DoublePair p1, DoublePair p2, DoublePair p3, DoublePair p4) {
        double minDistance = Double.MAX_VALUE;
        double distance = minDistanceBetweenSegments(p1, p2, p3);
        if (distance < minDistance) {
            minDistance = distance;
        }
        distance = minDistanceBetweenSegments(p1, p2, p4);
        if (distance < minDistance) {
            minDistance = distance;
        }
        distance = minDistanceBetweenSegments(p3, p4, p1);
        if (distance < minDistance) {
            minDistance = distance;
        }
        distance = minDistanceBetweenSegments(p3, p4, p2);
        if (distance < minDistance) {
            minDistance = distance;
        }
        return minDistance;
    }

    public static double minDistanceBetweenSegments(DoublePair p1, DoublePair p2, DoublePair p3) {
        double xDelta = p2.getFirst() - p1.getFirst();
        double yDelta = p2.getSecond() - p1.getSecond();

        if (xDelta == 0 && yDelta == 0) {
            throw new IllegalArgumentException("p1 and p2 cannot be the same point");
        }

        double u = ((p3.getFirst() - p1.getFirst()) * xDelta + (p3.getSecond() - p1.getSecond()) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

        final DoublePair closestPoint;
        if (u < 0) {
            closestPoint = p1;
        } else if (u > 1) {
            closestPoint = p2;
        } else {
            closestPoint = new DoublePair(p1.getFirst() + u * xDelta, p1.getSecond() + u * yDelta);
        }

        return distance(p3, closestPoint);
    }

    public static double distance(DoublePair p1, DoublePair p2) {
        double xDiff = p2.getFirst() - p1.getFirst();
        double yDiff = p2.getSecond() - p1.getSecond();
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }
}
