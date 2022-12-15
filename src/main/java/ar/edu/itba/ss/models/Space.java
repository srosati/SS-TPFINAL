package main.java.ar.edu.itba.ss.models;

import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.Integration;
import main.java.ar.edu.itba.ss.utils.MathUtils;
import main.java.ar.edu.itba.ss.utils.ParticleGenerator;

import java.util.List;

public class Space {
    private final static int[][] DIRECTIONS = new int[][]{new int[]{-1, 0}, new int[]{-1, 1},
            new int[]{0, 0}, new int[]{0, 1}, new int[]{1, 1}};

    public static double SLIT_SIZE = 3;
    private final Cell[][] cells;
    private final List<Particle> particleList;

    private final double colSize;
    private final double rowSize;

    private final int gridM;
    private final int gridN;

    public Space(List<Particle> particles) {
        this.particleList = particles;

        double maxRadius = particles.stream().mapToDouble(Particle::getRadius).max()
                .orElseThrow(RuntimeException::new);

        double l = Constants.LENGTH;
        double w = Constants.WIDTH;
        this.gridM = (int) Math.floor(l / (2 * maxRadius));
        this.gridN = (int) Math.floor(w / (2 * maxRadius));

        this.rowSize = l / gridM;
        this.colSize = w / gridN;
        this.cells = new Cell[gridM][gridN];
    }

    public void getNextRs() {
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
            particle.setNextR(0, new DoubleTriad(r0X, r0Y, r0w));

            // Predict Speed for each particle
            double r1X = Integration.beemanPredV(currVel.getFirst(), Constants.STEP, currAcc.getFirst(),
                    prevAcc.getFirst());
            double r1Y = Integration.beemanPredV(currVel.getSecond(), Constants.STEP, currAcc.getSecond(),
                    prevAcc.getSecond());
            double r1w = Integration.beemanPredV(currVel.getThird(), Constants.STEP, currAcc.getThird(),
                    prevAcc.getThird());

            particle.setPredV(new DoubleTriad(r1X, r1Y, r1w));

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
                    prevAcc.getThird(), 0.0);

            p.setNextR(R.VEL, new DoubleTriad(r1X, r1Y, r1w));
        });

        particleList.forEach(p -> p.setPredV(p.getNext(R.VEL)));

        particleList.forEach(p -> {
            DoubleTriad force = p.calculateForces();
            p.setNextR(R.ACC, new DoubleTriad(force.getFirst() / p.getMass(),
                    force.getSecond() / p.getMass(), force.getThird() / p.getMass()));
        });

        particleList.forEach(p -> {
            p.setPrev(R.POS, p.getCurrent(R.POS));
            p.setPrev(R.VEL, p.getCurrent(R.VEL));
            p.setPrev(R.ACC, p.getCurrent(R.ACC));

            p.setCurr(R.POS, p.getNext(R.POS));
            p.setCurr(R.VEL, p.getNext(R.VEL));
            p.setCurr(R.ACC, p.getNext(R.ACC));
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

    private void checkWallCollision(Particle particle, int row, int col) {
        double x = particle.getNext(R.POS).getFirst();
        double y = particle.getNext(R.POS).getSecond();
        double r = particle.getRadius();
        double l = particle.getLength();

        // BOTTOM
        if (row == 0) {
            double dy = MathUtils.minDistanceBetweenSegments(particle.getNext(R.POS), l,
                    new DoublePair(0, 0), new DoublePair(Constants.WIDTH, 0));

            if (Double.compare(r, dy) >= 0) {
                if (((x <= Constants.WIDTH / 2 - Space.SLIT_SIZE / 2) ||
                        (x >= Constants.WIDTH / 2 + Space.SLIT_SIZE / 2))) {
                    // Choque vertical con la pared
                    DoubleTriad position = new DoubleTriad(x, -r, 0);
                    particle.addNeighbour(getWallParticle(position, r));
                } else if ((x - r <= (Constants.WIDTH - Space.SLIT_SIZE) / 2)) {
                    // Borde izquierdo slit
                    DoubleTriad position = new DoubleTriad((Constants.WIDTH - Space.SLIT_SIZE) / 2, 0, 0);
                    particle.addNeighbour(getWallParticle(position, 0));
                } else if (x + r >= (Constants.WIDTH + Space.SLIT_SIZE) / 2) {
                    // Borde derecho slit
                    DoubleTriad position = new DoubleTriad((Constants.WIDTH + Space.SLIT_SIZE) / 2, 0, 0);
                    particle.addNeighbour(getWallParticle(position, 0));
                }
            }
        }

        // TOP
        if (row == gridM - 1) {
            double dY = MathUtils.minDistanceBetweenSegments(particle.getNext(R.POS), l,
                    new DoublePair(0, Constants.LENGTH), new DoublePair(Constants.WIDTH, Constants.LENGTH));

            if (Double.compare(r, dY) >= 0) {
                DoubleTriad position = new DoubleTriad(x, Constants.LENGTH + r, 0);
                particle.addNeighbour(getWallParticle(position, r));
            }
        }

        if (y >= 0) {
            // LEFT
            if (col == 0) {
                double dX = MathUtils.minDistanceBetweenSegments(particle.getNext(R.POS), l,
                        new DoublePair(0, 0), new DoublePair(0, Constants.LENGTH));
                if (Double.compare(r, dX) >= 0) {
                    DoubleTriad position = new DoubleTriad(-r, y, 0);
                    particle.addNeighbour(getWallParticle(position, r));
                }
            }

            // RIGHT
            if (col == gridN - 1) {
                double dX = MathUtils.minDistanceBetweenSegments(particle.getNext(R.POS), l,
                        new DoublePair(Constants.WIDTH, 0), new DoublePair(Constants.WIDTH, Constants.LENGTH));

                if (Double.compare(r, dX) >= 0) {
                    DoubleTriad position = new DoubleTriad(Constants.WIDTH + r, y, 0);
                    particle.addNeighbour(getWallParticle(position, r));
                }
            }
        }
    }

    private Particle getWallParticle(DoubleTriad position, double radius) {
        Particle wall = new Particle(radius, 0, position);
        wall.setNextR(R.POS, position);
        wall.setNextR(R.VEL, new DoubleTriad(0, 0, 0));
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
