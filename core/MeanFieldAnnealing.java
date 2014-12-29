package majorprojectsem7.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import static majorprojectsem7.core.FitnessFunction.cost;

/**
 *
 * @author Uday Kandpal
 */
public class MeanFieldAnnealing {

    public ArrayList<Chromosome2D> population = new ArrayList<>();

    private static Processor getMinByTimer(ArrayList<Processor> arrayList) {
        int mini = 0;
        for (int i = 0; i < arrayList.size(); i++) {
            if (((Processor) arrayList.get(i)).getTimer() < ((Processor) arrayList.get(mini)).getTimer()) {
                mini = i;
            }
        }
        //System.out.println(arrayList.get(mini));
        return arrayList.get(mini);
    }

    long range = 273;

    double constants[][] = new double[10000][1000];

    double randomTemperature(long range) {
        return (new Random().nextDouble() * range);
    }

    ArrayList<ArrayList<Double>> generateRandomSpins(int numberOfTasks, int numberOfProcessors, long range) {
        ArrayList<ArrayList<Double>> list = new ArrayList<>(numberOfTasks);
        int max = numberOfTasks * numberOfProcessors;
        for (int i = 0; i < numberOfTasks; i++) {
            ArrayList<Double> v = new ArrayList<>(numberOfProcessors);
            for (int j = 0; j < numberOfProcessors; j++) {
                v.add(0.5);
            }
            list.add(v);
        }
        return list;
    }

    double calculateEnergyDifferentiatedFuction(double T, ArrayList<Chromosome> spin, int numberOfTasks, int numberOfProcessors) {
        double ujk = 0;
        for (int i = 0; i < numberOfTasks; i++) {
            for (int j = 0; j < numberOfTasks; j++) {
                if (i != j) {
                    for (int k = 0; k < numberOfProcessors; k++) {
                        ujk += ((cost[i][j] - FitnessFunction.multiplierFittness3) * (1 - 2 * spin.get(i).toData().get(k) * spin.get(j).toData().get(k))) / T;
                    }
                }
            }
        }
        ujk += FitnessFunction.computedFitnessValues.getFitness2() + FitnessFunction.computedFitnessValues.getFitness3();
        return ujk;
    }

    boolean inCoolingRange(double T) {
        return T > 100;
    }

    private boolean isSignificantValue(double delta) {
        return (Math.log(delta) > 1);
    }

    private boolean isDecreasing(ArrayList<Double> delE) {
        if (delE.isEmpty()) {
            return true;
        }
        return delE.get(delE.size() - 1) < 0 || delE.size() > 100000;
    }

    public void getAllocationByMFA(int numberOfTasks, int numberOfProcessors) throws Exception {
        //step1 globalPopulation 
        population = new ArrayList<>();

        FitnessFunction.computedFitnessValues = FitnessFunction.computeFitnessValues();
        System.out.println("Initially computed : " + FitnessFunction.computedFitnessValues.toString());

        while (population.size() != numberOfTasks) {
            //Step 2
            double T0 = randomTemperature(range);
            double T = T0;
            double tf = 0.00005;

            //Step 3
            ArrayList<Chromosome> spins = new ArrayList<>(numberOfTasks);

            for (int i = 0; i < numberOfTasks; i++) {
                ArrayList<Double> temp = new ArrayList<>();
                for (int j = 0; j < numberOfProcessors; j++) {
                    temp.add(0.5); // between 0 and 1
                }
                spins.add(new Chromosome(temp, FitnessFunction.computedFitnessValues.getFitness1()));
                //System.out.println("testing values spin : " + spins.toString());
                //System.out.println("NEXT : \n");
            }
            TreeSet<Integer> dominantPopulationIndex = new TreeSet<>();
            // Step 4
            while (inCoolingRange(T)) {
                //System.out.println("Stage 1 : temp = " + T);
                ArrayList<Double> delE = new ArrayList<>();
                int iterateLength = 10000, x = 0;
                while (isDecreasing(delE) && x++ < iterateLength) {
                    //System.out.println("Stage 2 : ");
                    for (int i = 0; i < numberOfTasks; i++) {
                        //System.out.println("Stage 3 : ");
                        Double R = new Random().nextDouble();
                        if (R < 0.5) {
                            // fitness detection code
                            Random r = new Random();
                            int j = (int) (r.nextDouble() * numberOfTasks);
                            double delta = 0.0;

                            ArrayList<Double> newSpinVector = new ArrayList<>();
                            for (int k = 0; k < numberOfProcessors; k++) {
                                double ujk = calculateEnergyDifferentiatedFuction(T, spins, numberOfTasks, numberOfProcessors) + FitnessFunction.computedFitnessValues.getFitness2() + FitnessFunction.computedFitnessValues.getFitness3();
                                double vjk = 0.5 * (1 + Math.tanh(ujk));
                                newSpinVector.add(vjk);
                                delta += ujk * (vjk - spins.get(j).toData().get(k));
                            }
                            delE.add(delta);
                            if (isSignificantValue(delta)) {
                                break;
                            }
                            spins.set(j, new Chromosome(newSpinVector, spins.get(j).fitness));
                            ArrayList<ArrayList<Double>> copyOf = new ArrayList<>();
                        } else if (population.size() > 2) {
                            // cross over code
                            Random binaryParentSelector = new Random();
                            Random parentContributionDecider = new Random();
                            Random parentPrioritizer = new Random();
                            double parentContributionPercent = parentContributionDecider.nextDouble();
                            boolean primaryParent = parentPrioritizer.nextDouble() > 0.5;
                            int parent2 = binaryParentSelector.nextInt(population.size() - 1);
                            //System.out.println("Population size : " + population.size());
                            spins.set(i, spins.get(i).crossOver(population.get(parent2).toData().get(i), parentContributionPercent, primaryParent));
                            dominantPopulationIndex.add(parent2);
                            binaryParentSelector = null;
                            parentPrioritizer = null;
                            parentContributionDecider = null;
                            //       dominantPopulationIndex.add(i);
                        }
                    }
                }
                // Step 5
                T *= tf;
            }
            //Step 6 : Evaluate new Schedule
            Implementor.allocate(spins);
            // Step 7 : Add to population
            population.add(new Chromosome2D(spins));
            if (!FitnessFunction.computedFitnessValues.equals(FitnessFunction.computeFitnessValues())) {
                FitnessFunction.computedFitnessValues = FitnessFunction.computeFitnessValues();
                System.out.println(FitnessFunction.computedFitnessValues.toString());
            }
            //Step 8 : evaluted in this while loop
            //Step 9 : eliminate dominated solutions
            Iterator<Integer> it = dominantPopulationIndex.iterator();
            while (it.hasNext()) {
                int x = (int) it.next();
                if (population.size() > x) {
                    population.remove(x);
                }
            }
        }
        //System.out.println(spins);
    }

    static Comparator<Graph> byName = (Graph o1, Graph o2) -> {
        int n1 = Integer.parseInt(o1.getName().replace("T", ""));
        int n2 = Integer.parseInt(o2.getName().replace("T", ""));
        if (n1 > n2) {
            return 1;
        } else if (n1 == n2) {
            return 0;
        }
        return -1;
    };

    public static ArrayList<Processor> deepCopy(Processor p[], ArrayList<Integer> list) {
        ArrayList<Processor> filter = new ArrayList<>();
        list.stream().forEach((list1) -> {
            try {
                filter.add((Processor) p[list1].clone());
            } catch (CloneNotSupportedException ex) {
                //Logger.getLogger(MeanFieldAnnealing.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return filter;
    }
}
