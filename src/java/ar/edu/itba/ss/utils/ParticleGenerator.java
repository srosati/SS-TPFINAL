package ar.edu.itba.ss.utils;

import ar.edu.itba.ss.models.DoubleTriad;
import ar.edu.itba.ss.models.Particle;
import ar.edu.itba.ss.models.R;

import java.io.*;
import java.util.*;

public class ParticleGenerator {
    public static List<Particle> generate(String staticFile) {
        List<Particle> particles = new ArrayList<>();
        System.out.println("Begin particle generation");
        for (int i = 0; i < Constants.PARTICLE_AMOUNT; i++) {
            double minLength, radius, length, area;
            do {
                radius = randomNum(Constants.MIN_RADIUS, Constants.MAX_RADIUS);
                length = randomNum(Constants.MIN_LENGTH, Constants.MAX_LENGTH);

                area = (length * 2 * radius) + (Math.PI * radius * radius);

                double maxRadius = radius + Constants.RADIUS_AMPLITUDE;
                minLength = (area - Math.PI * Math.pow(maxRadius, 2)) / (2 * maxRadius);
            } while (minLength <= 0);


            double lag = Math.random() * 2 * Math.PI;
            double newRadius = radius + Constants.RADIUS_AMPLITUDE * Math.sin(lag);
            double newLength = (area - Math.PI * Math.pow(newRadius, 2)) / (2 * newRadius);

            DoubleTriad position = generateParticlePosition(particles, -1, newRadius, newLength, false);
            Particle newParticle = new Particle(newRadius, newLength, lag, position, radius);

            particles.add(newParticle);
        }

        try (FileWriter writer = new FileWriter(staticFile)) {
            writer.write(Constants.PARTICLE_AMOUNT + "\n");
            for (Particle p : particles) {
                writer.write(String.format(Locale.ROOT, "%d %f %f %f %f %f %f\n", p.getId(),
                        p.getCurrent(R.POS).getFirst(), p.getCurrent(R.POS).getSecond(),
                        p.getRadius(), p.getLength(), p.getCurrent(R.POS).getThird(), p.getLag()));
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
                double lag = Double.parseDouble(line[6]);
                // TODO: Chequear el initial_radius
                particleList.add(new Particle(id, radius, length, lag, new DoubleTriad(x, y, w)));
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
            position = randomPosition(radius, length, reentrant);

            colliding = false;
            for (Particle p : particles) {
                if (p.isColliding(position, radius, length)) {
                    colliding = true;
                    break;
                }
            }
        } while (colliding);
        return position;
    }

    private static DoubleTriad randomPosition(double radius, double length, boolean reentrant) {
        double w = randomNum(0, 2 * Math.PI);

        double dx = (length/2) + radius + Constants.WALL_RADIUS; // TODO: ver angulo
        double x = randomNum(dx, Constants.WIDTH - dx);

        double dy = (length/2) + radius + Constants.WALL_RADIUS;
        double y = randomNum(dy + (reentrant ? Constants.RE_ENTRANCE_MIN_Y : 0), Constants.LENGTH - dy);

        return new DoubleTriad(x, y, w);
    }

    private static double randomNum(double min, double max) {
        return min + Math.random() * (max - min);
    }
}