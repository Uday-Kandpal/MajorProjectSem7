package majorprojectsem7.core;

import java.util.ArrayList;

/**
 * A graph that sorts topologically whenever applied the default comparators.
 *
 * @author Uday Kandpal
 */
public class Graph implements Comparable<Graph> {

    private double executionTime = 0;
    private String name = "T";

    private int topologicalIndex = 0;
    private long referenceCounter = 0;
    private Processor alloted;
    private ArrayList<Graph> children = new ArrayList<>();

    private ArrayList<Graph> parent = new ArrayList<>();

    private double startTime = 0;

    public Graph(int name, double exec) {
        executionTime = exec;
        this.name += (1 + name);
    }

    public ArrayList<Graph> getChildren() {
        return children;
    }

    public ArrayList<Graph> getParent() {
        return parent;
    }

    /**
     * @return the executionTime
     */
    public double getExecutionTime() {
        return executionTime;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public void wait(double endTime) {
        if (endTime >= getStartTime()) {
            startTime = endTime;
        }
    }

    /**
     *
     * @param g
     */
    public void addChildren(Graph g) {
        children.add(g);
        g.addParent(this);
        g.topologicalIndex = topologicalIndex + 1;
    }

    private void addParent(Graph p) {
        parent.add(p);
    }

    public void setReferenceCounter(long update) {
        referenceCounter = update;
    }

    public long getReferenceCounter() {
        return referenceCounter;
    }

    public int getTopologicalIndex() {
        return topologicalIndex;
    }

    public void allotTo(Processor p) {
        this.alloted = p;
        double maxTime = 0;
        if (!getParent().isEmpty()) {
            for (Graph parent : getParent()) {
                maxTime = Math.max(parent.getEndTime(), maxTime);
            }
            startTime = maxTime;
            p.updateTimer(startTime);
            p.startTime.add(startTime);
        } else {
            if (p.getTasksAllocated().isEmpty()) {
                startTime = 0;
            } else {
                startTime = p.getTasksAllocated().get(p.getTasksAllocated().size() - 1).executionTime;
            }
            p.updateTimer(startTime);
            p.startTime.add(startTime);
        }
       // System.out.println(this.getName() + ":" + p.startTime);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Graph o) {
        Integer n1 = topologicalIndex;
        Integer n2 = o.topologicalIndex;
        if (n1 > n2) {
            return 1;
        } else if (n1 < n2) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * @return the alloted
     */
    public Processor getAlloted() {
        return alloted;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getEndTime() {
        return startTime + executionTime;
    }

}
