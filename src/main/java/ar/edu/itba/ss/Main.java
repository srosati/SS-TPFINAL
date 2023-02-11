package main.java.ar.edu.itba.ss;

import main.java.ar.edu.itba.ss.models.Particle;
import main.java.ar.edu.itba.ss.models.R;
import main.java.ar.edu.itba.ss.models.Space;
import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.ParticleGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    public static boolean hasToGenerate = true;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        if (args.length != 1) {
            System.out.println("Usage: java -jar SS-TPFINAL.jar <config_file>");
            System.exit(1);
        }

        configFileReader(args[0]);
        Space.calculateWallDimensions();

        List<Particle> particles;
        if (hasToGenerate) {
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
            iter++;

            outFile.write(String.format("%f %f %f\n", Space.SLIT_SIZE, Constants.WIDTH, Constants.LENGTH));
            System.out.println("Starting simulation...");

            while (Double.compare(elapsed, Constants.SIMULATION_TIME) < 0) {
                particles = space.getParticleList();

                space.getNextRs(elapsed);


                if (iter % 20 == 0) {
                    outFile.write(Constants.PARTICLE_AMOUNT + "\n");
                    outFile.write("iter " + iter + "\n");
                    for (Particle p : particles) {
                        outFile.write(String.format(Locale.ROOT, "%d %f %f %f %f %f\n",
                                p.getId(),
                                p.getCurrent(R.POS).getFirst(),
                                p.getCurrent(R.POS).getSecond(),
                                p.getCurrent(R.POS).getThird(),
                                p.getRadius(),
                                p.getLength()));
                    }
                }

//                for (Particle p : particles) {
//                    p.calculateNewSize(elapsed);
////                    System.out.printf("A:%f; l:%f; r:%f\n", p.getArea(), p.getLength(), p.getRadius());
//                }

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

    public static void configFileReader(String file) {

        File configFile = new File(file);
        try (Scanner configReader = new Scanner(configFile)) {
            while (configReader.hasNext()) {
                String[] line = configReader.nextLine().split(" ");
                switch (line[0].toLowerCase()) {
                    case "generate" -> hasToGenerate = Boolean.parseBoolean(line[1]);
                    case "slit_size" -> Space.SLIT_SIZE = Double.parseDouble(line[1]);
                    case "particle_amount" -> Constants.PARTICLE_AMOUNT = Integer.parseInt(line[1]);
                    case "tao" -> Constants.PROP_FACTOR = Double.parseDouble(line[1]);
                    case "oscillation_w" -> Constants.ANGULAR_W = Double.parseDouble(line[1]);
                }
            }
        } catch (NoSuchElementException | IllegalArgumentException | FileNotFoundException e) {
            System.out.println("Error parsing config file. Will use default values");
//            System.out.println(e.getMessage());
//            System.exit(1);
        }

    }
}