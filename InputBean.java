//Support classes for the Mars Rover java program
//CSCI-561 Fall 2019, Assignment 1
//Created By: Karthik Anand Bhat
//Date Created: September 12th 2019
//Date Modified: September 23rd 2019
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class InputBean {
    String algoName=null;
    int height=0;
    int width=0;
    int landingHeight=0;
    int landingWidth=0;
    int maxZ=0;
    int numberOfTargetSites=0;
    int[][] targetSites =null;
    int[][] matrix=null;

    @Override
    public String toString() {
        return "InputBean{" +
                "algoName='" + algoName + '\'' +
                ", height=" + height +
                ", width=" + width +
                ", landingHeight=" + landingHeight +
                ", landingWidth=" + landingWidth +
                ", maxZ=" + maxZ +
                ", numberOfTargetSites=" + numberOfTargetSites +
                ", targetSites=" + Arrays.deepToString(targetSites) +
                ", matrix=" + Arrays.deepToString(matrix) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputBean inputBean = (InputBean) o;
        return height == inputBean.height &&
                width == inputBean.width &&
                landingHeight == inputBean.landingHeight &&
                landingWidth == inputBean.landingWidth &&
                maxZ == inputBean.maxZ &&
                numberOfTargetSites == inputBean.numberOfTargetSites &&
                algoName.equals(inputBean.algoName) &&
                Arrays.equals(targetSites, inputBean.targetSites) &&
                Arrays.equals(matrix, inputBean.matrix);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(algoName, height, width, landingHeight, landingWidth, maxZ, numberOfTargetSites);
        result = 31 * result + Arrays.hashCode(targetSites);
        result = 31 * result + Arrays.hashCode(matrix);
        return result;
    }
}

//Class used to maintain the pathcost for UCS and heuristic for A*
class Node{
    private int index;
    private int pathCost;

    //New addition. Added setter, getter. Equals hashcode and toString changed
    private int heuristicCost;

    public Node(int index, int pathCost) {
        this.index = index;
        this.pathCost = pathCost;
    }


    public int getHeuristicCost() {
        return heuristicCost;
    }

    public void setHeuristicCost(int heuristicCost) {
        this.heuristicCost = heuristicCost;
    }

    public int getPathCost() {
        return pathCost;
    }

    public void setPathCost(int pathCost) {
        this.pathCost = pathCost;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return index == node.index &&
                pathCost == node.pathCost &&
                heuristicCost == node.heuristicCost;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, pathCost, heuristicCost);
    }

    @Override
    public String toString() {
        return "Node{" +
                "index=" + index +
                ", pathCost=" + pathCost +
                ", heuristicCost=" + heuristicCost +
                '}';
    }
}

//Comparator for priority queue for UCS search
class NodeComparator implements Comparator{

    @Override
    public int compare(Object o1, Object o2) {
        if((o1 instanceof Node) && (o2 instanceof Node)){

            return Integer.compare(((Node) o1).getPathCost(), ((Node) o2).getPathCost());
        }
        else throw new IllegalArgumentException("Objects passed are not nodes!");
    }
}

//Comparator for priority queue for A* search
class NodeComparatorHeuristic implements Comparator{
    @Override
    public int compare(Object o1, Object o2) {
        if((o1 instanceof Node) && (o2 instanceof Node)){

            return Integer.compare((((Node) o1).getPathCost()+((Node) o1).getHeuristicCost()),(((Node) o2).getPathCost()+((Node) o2).getHeuristicCost()));
        }
        else throw new IllegalArgumentException("Objects passed are not nodes!");
    }
}