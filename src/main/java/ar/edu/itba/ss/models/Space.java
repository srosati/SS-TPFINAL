package main.java.ar.edu.itba.ss.models;

import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.Integration;
import main.java.ar.edu.itba.ss.utils.MathUtils;
import main.java.ar.edu.itba.ss.utils.ParticleGenerator;

import java.util.List;

public class Space {
    private final static int[][] DIRECTIONS = new int[][]{new int[]{-1, 0}, new int[]{-1, 1},
            new int[]{0, 0}, new int[]{0, 1}, new int[]{1, 1}};
    private static double BOTTOM_WALL_LENGTH, LEFT_BOTTOM_WALL_X, RIGHT_BOTTOM_WALL_X, SIDE_WALL_Y, TOP_WALL_X;

    public static double SLIT_SIZE = 3;

    private final Cell[][] cells;
    private final List<Particle> particleList;

    private final double colSize;
    private final double rowSize;

    private final int gridM;
    private final int gridN;


    public Space(List<Particle> particles) {
        this.particleList = particles;

        double maxRadius = Constants.MAX_LENGTH / 2 + Constants.MAX_RADIUS;

        double l = Constants.LENGTH;
        double w = Constants.WIDTH;
        this.gridM = (int) Math.floor(l / (2 * maxRadius));
        this.gridN = (int) Math.floor(w / (2 * maxRadius));

        this.rowSize = l / gridM;
        this.colSize = w / gridN;
        this.cells = new Cell[gridM][gridN];
    }

    public void getNextRs(double elapsed) {
        // First set nextR[0] and predict nextR[1] for each particle
        for (Particle particle : particleList) {
            DoubleTriad currPos = particle.getCurrent(R.POS);
            DoubleTriad currVel = particle.getCurrent(R.VEL);

            DoubleTriad currAcc = particle.getCurrent(R.ACC);
            DoubleTriad prevAcc = particle.getPrev(R.ACC);

            // Next Position for each particle
            double r0X = Integration.beemanR(currPos.getFirst(), currVel.getFirst(), Constants.STEP,
                    currAcc.getFirst(), prevAcc.getFirst());
            double r0Y = Integration.beemanR(currPos.getSecond(), currVel.getSecond(), Constants.STEP,
                    currAcc.getSecond(), prevAcc.getSecond());
            double r0w = Integration.beemanR(currPos.getThird(), currVel.getThird(), Constants.STEP,
                    currAcc.getThird(), prevAcc.getThird());
            particle.setNextR(R.POS, new DoubleTriad(r0X, r0Y, r0w));

            // Predict Speed for each particle
            double r1X = Integration.beemanPredV(currVel.getFirst(), Constants.STEP, currAcc.getFirst(),
                    prevAcc.getFirst());
            double r1Y = Integration.beemanPredV(currVel.getSecond(), Constants.STEP, currAcc.getSecond(),
                    prevAcc.getSecond());
            double r1w = Integration.beemanPredV(currVel.getThird(), Constants.STEP, currAcc.getThird(),
                    prevAcc.getThird());

            particle.setPredV(new DoubleTriad(r1X, r1Y, r1w));
            particle.calculateNewSize(elapsed);
        }

        calculateNeighbours();

        // Correct R[1] for each particle
        particleList.forEach(p -> {
            DoubleTriad force = p.calculateForces();
            DoubleTriad currVel = p.getCurrent(R.VEL);
            DoubleTriad currAcc = p.getCurrent(R.ACC);
            DoubleTriad prevAcc = p.getPrev(R.ACC);

            double r1X = Integration.beemanV(currVel.getFirst(), Constants.STEP, currAcc.getFirst(),
                    prevAcc.getFirst(), force.getFirst() / p.getMass());
            double r1Y = Integration.beemanV(currVel.getSecond(), Constants.STEP, currAcc.getSecond(),
                    prevAcc.getSecond(), force.getSecond() / p.getMass());
            double r1w = Integration.beemanV(currVel.getThird(), Constants.STEP, currAcc.getThird(),
                    prevAcc.getThird(), force.getThird() / p.getMomentOfInertia());

            p.setNextR(R.VEL, new DoubleTriad(r1X, r1Y, r1w));
        });

        particleList.forEach(p -> p.setPredV(p.getNext(R.VEL)));

        particleList.forEach(p -> {
            DoubleTriad force = p.calculateForces();
            p.setNextR(R.ACC, new DoubleTriad(force.getFirst() / p.getMass(),
                    force.getSecond() / p.getMass(), force.getThird() / p.getMomentOfInertia()));

        });

        particleList.forEach(p -> {
            p.setPrev(R.POS, p.getCurrent(R.POS));
            p.setPrev(R.VEL, p.getCurrent(R.VEL));
            p.setPrev(R.ACC, p.getCurrent(R.ACC));

            p.setCurr(R.POS, p.getNext(R.POS));
            p.setCurr(R.VEL, p.getNext(R.VEL));
            p.setCurr(R.ACC, p.getNext(R.ACC));

            p.updateSize();
        });
    }

    private void positionParticles() {
        for (int i = 0; i < gridM; i++) {
            for (int j = 0; j < gridN; j++) {
                this.cells[i][j] = null;
            }
        }

        for (Particle particle : this.particleList) {
            DoublePair position = particle.getNext(R.POS);
            int row = getRow(position);
            int col = getCol(position);

            if (cells[row][col] == null)
                cells[row][col] = new Cell();

            cells[row][col].addParticle(particle);
        }
    }

    public void calculateNeighbours() {
        positionParticles();
        this.particleList.forEach(Particle::removeAllNeighbours);
        this.particleList.forEach(particle -> {
            DoublePair position = particle.getNext(R.POS);
            int row = getRow(position);
            int col = getCol(position);

            checkWallCollision(particle, row, col);

            for (int[] dir : DIRECTIONS) {
                int currRow = row + dir[0];
                int currCol = col + dir[1];

                if (currRow < 0 || currRow >= gridM || currCol < 0
                        || currCol >= gridN || cells[currRow][currCol] == null ||
                        cells[currRow][currCol].getParticles().isEmpty())
                    continue;

                cells[currRow][currCol].getParticles().stream()
                        .filter(particle::isColliding)
                        .forEach(p -> {
                            particle.addNeighbour(p);
                            p.addNeighbour(particle);
                        });
            }
        });
    }

    public int reenterParticles() {
        int count = 0;

        for (Particle p : particleList) {
            if (p.getCurrent(R.POS).getSecond() <= -Constants.RE_ENTRANCE_THRESHOLD) {
                DoubleTriad newPos = ParticleGenerator.generateParticlePosition(particleList, p.getId(),
                        p.getRadius(), p.getLength(), true);

                p.setCurr(R.POS, newPos);
                count++;
                p.initRs();
            }
        }
        return count;
    }

    public static void calculateWallDimensions() {
        Space.BOTTOM_WALL_LENGTH = (Constants.WIDTH - Space.SLIT_SIZE) / 2;
        Space.LEFT_BOTTOM_WALL_X = Space.BOTTOM_WALL_LENGTH / 2;
        Space.RIGHT_BOTTOM_WALL_X = Constants.WIDTH - Space.LEFT_BOTTOM_WALL_X;
        Space.TOP_WALL_X = Constants.WIDTH / 2;
        Space.SIDE_WALL_Y = Constants.LENGTH / 2;
    }

    private void checkWallCollision(Particle particle, int row, int col) {
        double x = particle.getNext(R.POS).getFirst();
        double y = particle.getNext(R.POS).getSecond();
        double w = particle.getNext(R.POS).getThird();
        double r = particle.getNextRadius();
        double l = particle.getNextLength();

        // BOTTOM
        if (row == 0) {
            DoublePair[] closestPoints = MathUtils.closestPointsBetweenSegments(
                    particle.getNext(R.POS), l,
                    new DoublePair(0, 0), new DoublePair(Constants.WIDTH, 0));

            double dy = Math.abs(closestPoints[0].getSecond() - closestPoints[1].getSecond());

            if (Double.compare(r, dy) >= 0) {
                double dx = (l/2) * Math.abs(Math.cos(w)) + r;
                if (((x <= BOTTOM_WALL_LENGTH ||
                        (x >= (Constants.WIDTH + Space.SLIT_SIZE) / 2)))) {
                    double wallX = LEFT_BOTTOM_WALL_X - r/2;
                    if (x >= TOP_WALL_X)
                        wallX = RIGHT_BOTTOM_WALL_X + r/2;
                    // Choque vertical con la pared
                    DoubleTriad position = new DoubleTriad(wallX, -r, 0);
                    particle.addNeighbour(getWallParticle(position, BOTTOM_WALL_LENGTH - r, r));
                } else if ((x - dx <= BOTTOM_WALL_LENGTH)) {
                    // Borde izquierdo slit
                    DoubleTriad position = new DoubleTriad(LEFT_BOTTOM_WALL_X - r/2, 0, 0);
                    particle.addNeighbour(getWallParticle(position, BOTTOM_WALL_LENGTH - r, r));
                } else if (x + dx >= (Constants.WIDTH + Space.SLIT_SIZE) / 2) {
                    // Borde derecho slit
                    DoubleTriad position = new DoubleTriad(RIGHT_BOTTOM_WALL_X + r/2, 0, 0);
                    particle.addNeighbour(getWallParticle(position, BOTTOM_WALL_LENGTH - r, r));
                }
            }
        }

        // TOP
        if (row == gridM - 1) {
            DoublePair[] closestPoints = MathUtils.closestPointsBetweenSegments(
                    particle.getNext(R.POS), l,
                    new DoublePair(0, Constants.LENGTH), new DoublePair(Constants.WIDTH, Constants.LENGTH));

            double dY = Math.abs(closestPoints[0].getSecond() - closestPoints[1].getSecond());

            if (Double.compare(r, dY) >= 0) {
                DoubleTriad position = new DoubleTriad(TOP_WALL_X, Constants.LENGTH + r, 0);
                particle.addNeighbour(getWallParticle(position, Constants.WIDTH, r));
            }
        }

        if (y >= 0) {
            // LEFT
            if (col == 0) {
                DoublePair[] closestPoints = MathUtils.closestPointsBetweenSegments(
                        particle.getNext(R.POS), l,
                        new DoublePair(0, 0), new DoublePair(0, Constants.LENGTH));
                double dX = Math.abs(closestPoints[0].getFirst() - closestPoints[1].getFirst());

                if (Double.compare(r, dX) >= 0) {
                    DoubleTriad position = new DoubleTriad(-r, SIDE_WALL_Y, Math.PI / 2);
                    particle.addNeighbour(getWallParticle(position, Constants.LENGTH, r));
                }
            }

            // RIGHT
            if (col == gridN - 1) {
                DoublePair[] closestPoints = MathUtils.closestPointsBetweenSegments(
                        particle.getNext(R.POS), l,
                        new DoublePair(Constants.WIDTH, 0), new DoublePair(Constants.WIDTH, Constants.LENGTH));
                double dX = Math.abs(closestPoints[0].getFirst() - closestPoints[1].getFirst());

                if (Double.compare(r, dX) >= 0) {
                    DoubleTriad position = new DoubleTriad(Constants.WIDTH + r, SIDE_WALL_Y, Math.PI / 2);
                    particle.addNeighbour(getWallParticle(position, Constants.LENGTH, r));
                }
            }
        }
    }

    private Particle getWallParticle(DoubleTriad position, double length, double radius) {
        Particle wall = new Particle(radius, length,0, position);
        wall.setNextR(R.POS, position);
        wall.setNextR(R.VEL, new DoubleTriad(0, 0, 0));
        wall.setNextLength(length);
        wall.setNextRadius(radius);
        return wall;
    }

    private int getRow(DoublePair position) {
        int toRet = (int) ((position.getSecond()) / rowSize);
        if (toRet < 0)
            toRet = 0;
        else if (toRet > gridM - 1)
            toRet = gridM - 1;

        return toRet;
    }

    private int getCol(DoublePair position) {
        int toRet = (int) (position.getFirst() / colSize);
        if (toRet < 0)
            toRet = 0;
        else if (toRet > gridN - 1)
            toRet = gridN - 1;

        return toRet;
    }

    public List<Particle> getParticleList() {
        return particleList;
    }
}
