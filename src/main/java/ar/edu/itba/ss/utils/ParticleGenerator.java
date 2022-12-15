package main.java.ar.edu.itba.ss.utils;

import main.java.ar.edu.itba.ss.models.DoubleTriad;
import main.java.ar.edu.itba.ss.models.Particle;
import main.java.ar.edu.itba.ss.models.R;

import java.io.*;
import java.util.*;

public class ParticleGenerator {
    public static List<Particle> generate(String staticFile) {
        List<Particle> particles = new ArrayList<>();
        System.out.println("Begin particle generation");
        for (int i = 0; i < Constants.PARTICLE_AMOUNT; i++) {
            double newRadius = randomNum(Constants.MIN_RADIUS, Constants.MAX_RADIUS);
            double newLength = randomNum(Constants.MIN_LENGTH, Constants.MAX_LENGTH);

            DoubleTriad position = generateParticlePosition(particles, -1, newRadius, newLength, false);
            Particle newParticle = new Particle(newRadius, newLength, position);

            particles.add(newParticle);
        }

        try (FileWriter writer = new FileWriter(staticFile)) {
            writer.write(Constants.PARTICLE_AMOUNT + "\n");
            for (Particle p : particles) {
                writer.write(String.format(Locale.ROOT, "%d %f %f %f %f\n", p.getId(),
                        p.getCurrent(R.POS).getFirst(), p.getCurrent(R.POS).getSecond(),
                        p.getRadius(), p.getLength()));
            }
        } catch (
                IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        System.out.println("End particle generator");

        return particles;
    }


    public static List<Particle> read(String staticPath) {
        List<Particle> particleList = new ArrayList<>();

        File staticFile = new File(staticPath);
        try (Scanner myReader = new Scanner(staticFile)) {
            int totalParticles = Integer.parseInt(myReader.nextLine());

            for (int i = 0; i < totalParticles; i++) {
                String[] line = myReader.nextLine().split(" ");
                int id = Integer.parseInt(line[0]);
                double x = Double.parseDouble(line[1]);
                double y = Double.parseDouble(line[2]);
                double radius = Double.parseDouble(line[3]);
                double length = Double.parseDouble(line[4]);
                double w = Double.parseDouble(line[5]);
                particleList.add(new Particle(id, radius, length, new DoubleTriad(x, y, w)));
            }
        } catch (NoSuchElementException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return particleList;
    }

    public static DoubleTriad generateParticlePosition(List<Particle> particles, int id, double radius, double length, boolean reentrant) {
        boolean colliding;
        DoubleTriad position;
        do {
            position = randomPosition(radius + length, reentrant); // TODO: hacer bien

            colliding = false;
            for (Particle p : particles) {
                double distance = MathUtils.minDistanceBetweenSegments(p.getCurrent(R.POS), p.getLength(), position, length);
                if (id != p.getId() && Math.pow(distance, 2) < Math.pow(p.getRadius() + radius, 2)) {
                    colliding = true;
                    break;
                }
            }
        } while (colliding);
        return position;
    }

    private static DoubleTriad randomPosition(double radius, boolean reentrant) {
        double x = randomNum(radius, Constants.WIDTH - radius);
        double y = randomNum(radius + (reentrant ? Constants.RE_ENTRANCE_MIN_Y : 0), Constants.LENGTH - radius);
        double w = randomNum(0, 2 * Math.PI);

        return new DoubleTriad(x, y, w);
    }

    private static double randomNum(double min, double max) {
        return min + Math.random() * (max - min);
    }
}