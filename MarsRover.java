//Java program used by the Mars Rover to navigate from the landing site to the target site
//CSCI-561 Fall 2019, Assignment 1
//Created By: Karthik Anand Bhat
//Date Created: September 12th 2019
//Date Modified: September 23rd 2019

import java.io.*;
import java.util.*;

public class MarsRover {

    private static InputBean readFile() throws IOException{
        BufferedReader br = null;
        try {
            InputBean inputBean = new InputBean();

            String line = null;
            List<String> contents = new ArrayList<String>();

            //Code to delete the file if exists!
            File out = new File("./output.txt");
            if(out.exists()){
                out.delete();
            }

            //Create file object
            File input = new File("./src/input100vijay.txt");
            //Read all lines and insert into a array-list
            br = new BufferedReader(new FileReader(input));

            while ((line = br.readLine()) != null) {
                contents.add(line);
            }

            //Variables
            inputBean.algoName = contents.get(0);
            inputBean.width = Integer.parseInt(contents.get(1).split(" ")[0]); //Number of columns
            inputBean.height = Integer.parseInt(contents.get(1).split(" ")[1]); //Number of rows

            //ERROR. Height & Width inverted!!!
            inputBean.landingHeight = Integer.parseInt(contents.get(2).split(" ")[1]);
            inputBean.landingWidth = Integer.parseInt(contents.get(2).split(" ")[0]);
            inputBean.maxZ = Integer.parseInt(contents.get(3));
            inputBean.numberOfTargetSites = Integer.parseInt(contents.get(4));

            //Locations of the target sites taken as col, row
            inputBean.targetSites = new int[inputBean.numberOfTargetSites][2];
            for (int i = 0; i < inputBean.numberOfTargetSites; i++) {
                inputBean.targetSites[i][0] = Integer.parseInt(contents.get(5 + i).split(" ")[0]);
                inputBean.targetSites[i][1] = Integer.parseInt(contents.get(5 + i).split(" ")[1]);
            }


            //Matrix having all the cells
            inputBean.matrix = new int[inputBean.height][inputBean.width];
            for (int i = 0; i < inputBean.height; i++) {
                String[] arrayRow = contents.get(5 + inputBean.numberOfTargetSites + i).trim().split("\\s+");

                for (int j = 0; j < inputBean.width; j++) {
                    inputBean.matrix[i][j] = Integer.parseInt(arrayRow[j]);
                }
            }

            return inputBean;
        }
        catch (IOException ex){
            ex.printStackTrace();
            throw new IOException();
        }
        finally {
                try{
                    if(br!=null) br.close();
                }
                catch (IOException ex){
                    ex.printStackTrace();
                }
            }
    }


    public static void main(String[] args){

        try{
            //To keep track of running time
            long startTime=System.currentTimeMillis();

            InputBean inputBean= readFile();


           //BFS
           if (inputBean.algoName.equalsIgnoreCase("BFS")){
               breadthFirstSearch(inputBean);
           }
           //UCS
           if (inputBean.algoName.equalsIgnoreCase("UCS")){
               uniformCostSearch(inputBean);
           }
           //A*
           if (inputBean.algoName.equalsIgnoreCase("A*")){
               aStarSearch(inputBean);
           }
           System.out.println("Time taken for execution is:"+(System.currentTimeMillis()-startTime));
        }
        catch (Throwable t){
            t.printStackTrace();
        }
    }

    //UCS
    private static void uniformCostSearch(InputBean inputBean){
        //String containing the result
        StringBuilder resultString = new StringBuilder();
        //Run the algorithm for the number of target sites
        int landingRow= inputBean.landingHeight;
        int landingCol=inputBean.landingWidth;
        //Landing site in 1D
        int landingSite=rowcolumnTo1D(inputBean,landingRow,landingCol);


        for (int i=0;i<inputBean.numberOfTargetSites;i++){

            boolean isGoalReached=false; //Flag to check if goal is reached
            //Current target site:
            int currentTargetRow=inputBean.targetSites[i][1]; //Input has the row,col inverted
            int currentTargetCol=inputBean.targetSites[i][0];

            //Goal state in 1D
            int goalState= rowcolumnTo1D(inputBean,currentTargetRow,currentTargetCol);

            //List of visited nodes
            List<Integer> visitedList=new ArrayList<Integer>();

            boolean[] visited=new boolean[inputBean.height*inputBean.width];

            //Priority Queue for BFS based on comparator
            PriorityQueue<Node> queue = new PriorityQueue<Node>(new NodeComparator());

            //Map to store current node's previous node
            Map<Integer,Integer> parentNode = new HashMap<Integer, Integer>();  //Key is the node, and value is the parent

            //Add landing site to the queue and keep track of the parent node
            //Path-cost of landing site is 0

            Node landingSiteNode=new Node(landingSite,0);

            queue.add(landingSiteNode); //Add at the end

            parentNode.put(landingSite,null); //Parent of landing site is null

            while (queue.size()!=0){
                //Dequeue
                Node elementNode=queue.poll();
                int element=elementNode.getIndex(); //Removes head of the linked list. FIFO

                if(visited[element]){
                    continue;
                }

                visited[element]=true;

                if(element==goalState) {
                    System.out.println(elementNode.getPathCost());
                    isGoalReached=true;
                    break;
                }

                //Find adjacent neighbours of the given popped node
                List<Node> neighbours=getAccessibleNeighboursUCS(inputBean,OneDTorowcolumn(inputBean,element).get(0),OneDTorowcolumn(inputBean,element).get(1));


                //List<Node> neighbours=allNodes.get(element);


                if (!(neighbours.isEmpty())) {
                    for (Node k : neighbours) {
                        //Increase the path cost
                        k.setPathCost(k.getPathCost()+elementNode.getPathCost());

                        //If not visited, add to queue


                        if (!(visited[k.getIndex()])) {
                            queue.add(k);
                            //Put if key not already present
                            parentNode.putIfAbsent(k.getIndex(), element);
                        }
                    }
                }
            }

            if (isGoalReached){
                Integer k=goalState;
                Integer value;
                List<Integer> results = new ArrayList<Integer>();

                results.add(k);

                while(parentNode.get(k)!=null){
                    value=parentNode.get(k);
                    results.add(value);
                    k=value;
                }

                //Invert results and send it to the file to write
                Collections.reverse(results);

               // System.out.println(results);

                for (Integer nodes: results){

                    resultString.append(OneDTorowcolumn(inputBean,nodes).get(1));
                    resultString.append(",");
                    resultString.append(OneDTorowcolumn(inputBean,nodes).get(0));
                    resultString.append(" ");
                }
                resultString.append("\n");

            }
            else {
                resultString.append("FAIL");
                resultString.append("\n");
            }
        }
        writeFile(resultString.toString());

    }

    //BFS
    private static void breadthFirstSearch(InputBean inputBean){

        //String containing the result
        StringBuilder resultString = new StringBuilder();
        //Run the algorithm for the number of target sites
        int landingRow= inputBean.landingHeight;
        int landingCol=inputBean.landingWidth;
        //Landing site in 1D
        int landingSite=rowcolumnTo1D(inputBean,landingRow,landingCol);


        for (int i=0;i<inputBean.numberOfTargetSites;i++){

            boolean isGoalReached=false; //Flag to check if goal is reached
            //Current target site:
            int currentTargetRow=inputBean.targetSites[i][1]; //Input has the row,col inverted
            int currentTargetCol=inputBean.targetSites[i][0];

            //Goal state in 1D
            int goalState= rowcolumnTo1D(inputBean,currentTargetRow,currentTargetCol);

            //List of visited nodes

            boolean[] visited = new boolean[inputBean.height*inputBean.width];

            //Queue for BFS
            LinkedList<Integer> queue = new LinkedList<Integer>();

            //Map to store current node's previous node
            Map<Integer,Integer> parentNode = new HashMap<Integer, Integer>();  //Key is the node, and value is the parent

            //Add landing site to the queue and keep track of the parent node
            queue.add(landingSite); //Add at the end
            parentNode.put(landingSite,null); //Parent of landing site is null
            int c=0;
            while (queue.size()!=0){
                c+=1;
                //Dequeue
                int element=queue.poll(); //Removes head of the linked list. FIFO
                if(visited[element]){
                    continue;
                }
                visited[element]=true;
                //visitedList.add(element);
                if(element==goalState) {
                    System.out.println(c);
                    isGoalReached=true;
                    break;
                }

                //Find adjacent neighbours of the given popped node
                List<Integer> neighbours=getAccessibleNeighbours(inputBean,OneDTorowcolumn(inputBean,element).get(0),OneDTorowcolumn(inputBean,element).get(1));

                if (!(neighbours.isEmpty())) {
                    for (int node : neighbours) {
                        //If not visited, add to queue

                        if (!(visited[node])) {
                            queue.add(node);
                            //Put if key not already present
                            parentNode.putIfAbsent(node, element);
                        }
                    }
                }
            }

            if (isGoalReached){
                Integer k=goalState;
                Integer value;
                List<Integer> results = new ArrayList<Integer>();

                results.add(k);
                int d=0;
                 while(parentNode.get(k)!=null){
                     d+=1;
                     value=parentNode.get(k);
                     results.add(value);
                     k=value;
                 }
                System.out.println(d);
                 //Invert results and send it to the file to write
                Collections.reverse(results);

              //  System.out.println(results);

                for (Integer nodes: results){

                    resultString.append(OneDTorowcolumn(inputBean,nodes).get(1));
                    resultString.append(",");
                    resultString.append(OneDTorowcolumn(inputBean,nodes).get(0));
                    resultString.append(" ");
                }
                resultString.append("\n");

            }
            else {
                resultString.append("FAIL");
                resultString.append("\n");
            }
        }
        writeFile(resultString.toString());

    }

    //A*
    private static void aStarSearch(InputBean inputBean){
        System.out.println("In A* algo!!");
        //String containing the result
        StringBuilder resultString = new StringBuilder();
        //Run the algorithm for the number of target sites
        int landingRow= inputBean.landingHeight;
        int landingCol=inputBean.landingWidth;
        //Landing site in 1D
        int landingSite=rowcolumnTo1D(inputBean,landingRow,landingCol);


        for (int i=0;i<inputBean.numberOfTargetSites;i++){

            boolean isGoalReached=false; //Flag to check if goal is reached
            //Current target site:
            int currentTargetRow=inputBean.targetSites[i][1]; //Input has the row,col inverted
            int currentTargetCol=inputBean.targetSites[i][0];

            //Goal state in 1D
            int goalState= rowcolumnTo1D(inputBean,currentTargetRow,currentTargetCol);

            //List of visited nodes

            boolean[] visited=new boolean[inputBean.width*inputBean.height];


            //Priority Queue for BFS based on comparator
            PriorityQueue<Node> queue = new PriorityQueue<Node>(new NodeComparatorHeuristic());

            //Map to store current node's previous node
            Map<Integer,Integer> parentNode = new HashMap<Integer, Integer>();  //Key is the node, and value is the parent

            //Add landing site to the queue and keep track of the parent node
            //Path-cost of landing site is 0

            Node landingSiteNode=new Node(landingSite,0);

            queue.add(landingSiteNode); //Add at the end

            parentNode.put(landingSite,null); //Parent of landing site is null


            while (queue.size()!=0){
                //System.out.println("Queue:"+ queue);
                //Dequeue
                Node elementNode=queue.poll();
                int element=elementNode.getIndex(); //Removes head of the linked list. FIFO
                if(visited[element]){
                    continue;
                }
                //System.out.println(element);
                visited[element]=true;

                if(element==goalState) {
                    System.out.println(elementNode.getPathCost());
                    isGoalReached=true;
                    break;
                }

                //Find adjacent neighbours of the given popped node
               List<Node> neighbours=getAccessibleNeighboursUCS(inputBean,OneDTorowcolumn(inputBean,element).get(0),OneDTorowcolumn(inputBean,element).get(1));

                if (!(neighbours.isEmpty())) {
                    for (Node k : neighbours) {
                        //Increase the path cost
                        k.setPathCost(k.getPathCost()+elementNode.getPathCost()+ (int)(Math.abs(inputBean.matrix[OneDTorowcolumn(inputBean, k.getIndex()).get(0)][OneDTorowcolumn(inputBean, k.getIndex()).get(1)] - (inputBean.matrix[OneDTorowcolumn(inputBean, element).get(0)][OneDTorowcolumn(inputBean, element).get(1)]))));

                        k.setHeuristicCost(calculateHeuristicCost(inputBean,currentTargetRow,currentTargetCol,k));

                        //If not visited, add to queue

                        if (!(visited[k.getIndex()])) {
                            queue.add(k);
                            //Put if key not already present
                            parentNode.putIfAbsent(k.getIndex(), element);
                        }
                    }
                }
            }


            if (isGoalReached){
                Integer k=goalState;
                Integer value;
                List<Integer> results = new ArrayList<Integer>();

                results.add(k);

                while(parentNode.get(k)!=null){
                    value=parentNode.get(k);
                    results.add(value);
                    k=value;
                }

                //Invert results and send it to the file to write
                Collections.reverse(results);


                for (Integer nodes: results){

                    resultString.append(OneDTorowcolumn(inputBean,nodes).get(1));
                    resultString.append(",");
                    resultString.append(OneDTorowcolumn(inputBean,nodes).get(0));
                    resultString.append(" ");
                }
                resultString.append("\n");

            }
            else {
                resultString.append("FAIL");
                resultString.append("\n");
            }
        }
        writeFile(resultString.toString());

    }



    //Get neighbouring accessible neighbours
    //Check the Z value.
    private static List<Integer> getAccessibleNeighbours(InputBean inputBean, int row, int col){

        //Check if row and column is correct
        checkIndex(inputBean,row,col);

        int maxZ=inputBean.maxZ; //Since object instance variables aren't private
        int numberOfRows=inputBean.height;
        int numberOfColumns=inputBean.width;

        //At most 8 neighbours are possible
        List<Integer> neighbours = new ArrayList<Integer>();

        //East
        if (col+1<=numberOfColumns-1 && Math.abs(inputBean.matrix[row][col+1]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add(rowcolumnTo1D(inputBean,row,col+1));
        }

        //West
        if (col-1>=0 && Math.abs(inputBean.matrix[row][col-1]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add(rowcolumnTo1D(inputBean,row,col-1));
        }

        //North
        if (row-1>=0 && Math.abs(inputBean.matrix[row-1][col]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add(rowcolumnTo1D(inputBean,row-1,col));
        }

        //South
        if (row+1<=numberOfRows-1 && Math.abs(inputBean.matrix[row+1][col]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add(rowcolumnTo1D(inputBean,row+1,col));
        }

        //NorthEast
        if ((row-1>=0 && col+1<=numberOfColumns-1) && Math.abs(inputBean.matrix[row-1][col+1]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add(rowcolumnTo1D(inputBean,row-1,col+1));
        }
        //NorthWest
        if ((row-1>=0 && col-1>=0) && Math.abs(inputBean.matrix[row-1][col-1]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add(rowcolumnTo1D(inputBean,row-1,col-1));
        }

        //SouthEast
        if ((row+1<=numberOfRows-1 && col+1<=numberOfColumns-1) && Math.abs(inputBean.matrix[row+1][col+1]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add(rowcolumnTo1D(inputBean,row+1,col+1));
        }

        //SouthWest
        if ((row+1<=numberOfRows-1 && col-1>=0) && Math.abs(inputBean.matrix[row+1][col-1]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add(rowcolumnTo1D(inputBean,row+1,col-1));
        }

        return neighbours;
    }

    //Check if index passed is valid or not
    private static void checkIndex(InputBean inputBean,int row, int col)
    {
        if(row<0 || row>inputBean.height-1) throw new IllegalArgumentException("Row index out of bounds!");
        if(col<0 || col>inputBean.width-1) throw new IllegalArgumentException("Column index out of bounds!");
    }

    //Method to convert from row, column to 1D array representation
    private static int rowcolumnTo1D(InputBean inputBean, int row, int col) {

        int rowSize=inputBean.width;
        return (row*rowSize)+col;
    }

    //Convert 1 Dimensional array representation value to a row/column
    private static List<Integer> OneDTorowcolumn(InputBean inputBean,int val1D){
        int sizeOfOneRow=inputBean.width;
        List<Integer> rcVal= new ArrayList<Integer>();
        rcVal.add(val1D/sizeOfOneRow);  //Row value
        rcVal.add(val1D%sizeOfOneRow);  //Column value
        return rcVal;
    }


    //For UCS, method returns Node which contains the index of neighbours and the path cost initially (10 or 14).
    private static List<Node> getAccessibleNeighboursUCS(InputBean inputBean, int row, int col){
        //Check if row and column is correct
        checkIndex(inputBean,row,col);

        int maxZ=inputBean.maxZ; //Since object instance variables aren't private
        int numberOfRows=inputBean.height;
        int numberOfColumns=inputBean.width;

        //At most 8 neighbours are possible
        List<Node> neighbours = new ArrayList<Node>();

        //East
        if (col+1<=numberOfColumns-1 && Math.abs(inputBean.matrix[row][col+1]-inputBean.matrix[row][col])<=maxZ){

            neighbours.add(new Node(rowcolumnTo1D(inputBean,row,col+1),10));
        }

        //West
        if (col-1>=0 && Math.abs(inputBean.matrix[row][col-1]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add( new Node(rowcolumnTo1D(inputBean,row,col-1),10));
        }

        //North
        if (row-1>=0 && Math.abs(inputBean.matrix[row-1][col]-inputBean.matrix[row][col])<=maxZ){

            neighbours.add(new Node(rowcolumnTo1D(inputBean,row-1,col),10));
        }

        //South
        if (row+1<=numberOfRows-1 && Math.abs(inputBean.matrix[row+1][col]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add(new Node(rowcolumnTo1D(inputBean,row+1,col),10));
        }

        //NorthEast
        if ((row-1>=0 && col+1<=numberOfColumns-1) && Math.abs(inputBean.matrix[row-1][col+1]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add(new Node(rowcolumnTo1D(inputBean,row-1,col+1),14));
        }
        //NorthWest
        if ((row-1>=0 && col-1>=0) && Math.abs(inputBean.matrix[row-1][col-1]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add(new Node(rowcolumnTo1D(inputBean,row-1,col-1),14));
        }

        //SouthEast
        if ((row+1<=numberOfRows-1 && col+1<=numberOfColumns-1) && Math.abs(inputBean.matrix[row+1][col+1]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add(new Node(rowcolumnTo1D(inputBean,row+1,col+1),14));
        }

        //SouthWest
        if ((row+1<=numberOfRows-1 && col-1>=0) && Math.abs(inputBean.matrix[row+1][col-1]-inputBean.matrix[row][col])<=maxZ){
            neighbours.add(new Node(rowcolumnTo1D(inputBean,row+1,col-1),14));
        }

        return neighbours;
    }


    //Admissible heuristics calculated using the Euclidean distance
    private static int calculateHeuristicCost(InputBean inputBean,int currentTargetRow, int currentTargetColumn, Node k){
        int nodeRow = OneDTorowcolumn(inputBean,k.getIndex()).get(0);
        int nodeCol=OneDTorowcolumn(inputBean,k.getIndex()).get(1);

        return (int)((Math.sqrt(Math.pow((nodeRow-currentTargetRow),2)+ Math.pow(nodeCol-currentTargetColumn,2)))*10);
    }

    //Function to write to a file
    private static void writeFile(String resultString){
        BufferedWriter bfwr=null;
        try {
            bfwr=new BufferedWriter(new FileWriter(new File("output.txt"), true));
            bfwr.write(resultString);
            bfwr.flush();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
        finally {
            if(bfwr!=null){
                try {
                    bfwr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


}
