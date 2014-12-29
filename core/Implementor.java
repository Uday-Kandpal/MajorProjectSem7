package majorprojectsem7.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import static majorprojectsem7.core.FitnessFunction.getProcessors;
import static majorprojectsem7.core.FitnessFunction.getTasks;
import static majorprojectsem7.core.FitnessFunction.numberOfProcessor;
import static majorprojectsem7.core.FitnessFunction.numberOfTasks;
import static majorprojectsem7.core.FitnessFunction.p;
import static majorprojectsem7.core.FitnessFunction.tasks;
import static majorprojectsem7.core.FitnessFunction.cost;

/**
 *
 * @author Uday Kandpal
 */
public class Implementor {

    public static void processInput() throws IOException, FileNotFoundException, NumberFormatException {
        BufferedReader br = new BufferedReader(new FileReader("in1.txt"));
        numberOfProcessor = Integer.parseInt(br.readLine());
        numberOfTasks = Integer.parseInt(br.readLine());
        p = new Processor[numberOfProcessor];
        for (int i = 0; i < getProcessors().length; i++) {
            p[i] = new Processor(i);
        }
        tasks = new Graph[numberOfTasks];
        for (int i = 0; i < numberOfTasks; i++) {
            double exec = Double.parseDouble(br.readLine());
            tasks[i] = new Graph(i, exec);
        }
        for (int i = 0; i < numberOfTasks; i++) {
            int numOfChildren = Integer.parseInt(br.readLine());
            String x[] = br.readLine().trim().split(" ");
            for (int j = 0; j < numOfChildren; j++) {
                getTasks()[i].addChildren(getTasks()[Integer.parseInt(x[j]) - 1]);
            }
            //System.out.println();
        }
        cost = new double[numberOfTasks][numberOfTasks];
        for (int i = 0; i < numberOfTasks; i++) {
            String x[] = br.readLine().trim().split(" ");
            for (int j = 0; j < x.length; j++) {
                cost[i][j] = Double.parseDouble(x[j]);
            }
        }

        System.out.println("Topological order : ");
        Arrays.sort(getTasks());
        for (int i = 0; i < numberOfTasks; i++) {
            System.out.println(getTasks()[i].getName() + " : " + getTasks()[i].getTopologicalIndex());
        }
        for (int i = 0; i < numberOfTasks; i++) {
            getTasks()[i].allotTo(getProcessors()[(i % getProcessors().length)]);
            getProcessors()[(i % getProcessors().length)].allocateTask(getTasks()[i]);
        }
        for(Graph task : getTasks()){
            System.out.println(task.getName()+" : "+task.getAlloted().toString() + ", startTime = "+task.getStartTime()+", endTime = "+task.getEndTime());
        }
        for (Processor p1 : getProcessors()) {
            System.out.println(p1 + " : " + p1.getTasksAllocated());
        }
        br.close();
    }

    public static void allocate(ArrayList<Chromosome> list) {
        //System.out.println("chromosome : " + list);
        for (Processor p1 : p) {
            p1.reset();
        }
        for (int i = 0; i < list.size(); i++) {
            int maxi = 0;
            for (int j = 1; j < list.get(i).data.size(); j++) {
                if (list.get(i).toData().get(j) > list.get(i).toData().get(maxi)) {
                    maxi = j;
                } else if (Objects.equals(list.get(i).toData().get(j), list.get(i).toData().get(maxi))) {
                    if (p[j].getTimer() <= p[maxi].getTimer()) {
                        maxi = j;
                    }
                }
            }

            tasks[i].allotTo(p[maxi]);
            p[maxi].allocateTask(tasks[i]);
            p[maxi].updateTimer(p[maxi].getTimer() + tasks[i].getExecutionTime());
        }
    }

    public static void main(String args[]) throws Exception {
        processInput();

        MeanFieldAnnealing an = new MeanFieldAnnealing();

        System.out.println("Number of tasks : " + numberOfTasks);
        System.out.println("Number of processors : " + numberOfProcessor);
        System.out.println("Pareto fronts : ");
        an.getAllocationByMFA(numberOfTasks, numberOfProcessor); // *check static 
        System.out.println();
        Graph[] ans = FitnessFunction.getTasks();
        Processor[] p = FitnessFunction.getProcessors();

        System.out.println("final allocation\n");
        System.out.println("Task\tProcessor\t");
        for (int i = 0; i < ans.length; i++) {
            System.out.println("" + ans[i].getName() + " : " + ans[i].getAlloted().toString()+ ", startTime = "+ans[i].getStartTime()+", endTime = "+ans[i].getEndTime());
        }
    }
}
