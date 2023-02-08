package main.java.ar.edu.itba.ss;

import main.java.ar.edu.itba.ss.models.Particle;
import main.java.ar.edu.itba.ss.models.R;
import main.java.ar.edu.itba.ss.models.Space;
import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.ParticleGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        if (args.length != 2) {
            System.out.println("Usage: java -jar SS-TPFINAL.jar <generate> <slit_size>");
            System.exit(1);
        }

        boolean generate = Boolean.parseBoolean(args[0]);
        Space.SLIT_SIZE = Double.parseDouble(args[1]);

        List<Particle> particles;
        if (generate) {
            particles = ParticleGenerator.generate("./outFiles/input.txt");
        } else {
            particles = ParticleGenerator.read("./outFiles/input.txt");
        }

        double elapsed = Constants.STEP;
        Space space = new Space(particles);
        int iter = 0;

        try (FileWriter outFile = new FileWriter("./outFiles/out.txt");
             FileWriter flowFile = new FileWriter("./outFiles/flow.txt")) {
            particles.forEach(Particle::initRs);

//            outFile.write(Constants.PARTICLE_AMOUNT + "\n");
//            outFile.write("iter " + iter + "\n");
            iter++;

//            for (Particle p : particles) {
//                double w = Math.cos(Math.PI / 4);
//                double x = Math.sin(Math.PI / 4);
//                outFile.write(String.format(Locale.ROOT, "%d %f %f %f %f %f %f\n", p.getId(),
//                        p.getCurrent(R.POS).getFirst(),
//                        p.getCurrent(R.POS).getSecond(),
//                        p.getRadius(), p.getLength(), w, x));
//            }

            while (Double.compare(elapsed, Constants.SIMULATION_TIME) < 0) {
                particles = space.getParticleList();

                space.getNextRs();


                if (iter % 20 == 0) {
                    outFile.write(Constants.PARTICLE_AMOUNT + "\n");
                    outFile.write("iter " + iter + "\n");
                    for (Particle p : particles) {
//                        // Get orientation Quaternion from rotation
//                        double w1 = Math.cos(Math.PI / 4);
//                        double x1 = Math.sin(Math.PI / 4);
//                        double y1 = 0;
//                        double z1 = 0;
//
//                        double w2 = Math.cos(p.getCurrent(R.POS).getThird() / 2);
//                        double x2 = 0;
//                        double y2 = -Math.sin(p.getCurrent(R.POS).getThird() / 2);
//                        double z2 = 0;
//
//                        // Multiply quaternions
//                        double w = w1 * w2 - x1 * x2 - y1 * y2 - z1 * z2;
//                        double x = w1 * x2 + x1 * w2 + y1 * z2 - z1 * y2;
//                        double y = w1 * y2 - x1 * z2 + y1 * w2 + z1 * x2;
//                        double z = w1 * z2 + x1 * y2 - y1 * x2 + z1 * w2;
//
//                        // TODO: Pasar computo de quaternions a python
//                        // Solo deberia escribir rotacion (p.getRotation())
//
//                        outFile.write(String.format(Locale.ROOT, "%d %f %f %f %f %f %f %f %f\n", p.getId(),
//                                p.getCurrent(R.POS).getFirst(),
//                                p.getCurrent(R.POS).getSecond(),
//                                p.getRadius(), p.getLength(), w, x, y, z));

                        outFile.write(String.format(Locale.ROOT, "%d %f %f %f %f %f\n",
                                p.getId(),
                                p.getCurrent(R.POS).getFirst(),
                                p.getCurrent(R.POS).getSecond(),
                                p.getCurrent(R.POS).getThird(),
                                p.getRadius(),
                                p.getLength()));
                    }
                }


                int flow = space.reenterParticles();
                flowFile.write(String.format(Locale.ROOT, "%f %d\n", elapsed, flow));

                iter++;
                elapsed += Constants.STEP;
            }

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            Date resultDate = new Date(totalTime);
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            System.out.println(sdf.format(resultDate));

        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}