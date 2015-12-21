package org.cytoscapeapp.cyspanningtree.internal.spanningtree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.cytoscape.model.*;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscapeapp.cyspanningtree.internal.CyActivator;
import org.cytoscapeapp.cyspanningtree.internal.SpanningTreeStartMenu;
import org.cytoscapeapp.cyspanningtree.internal.visuals.SpanningTreeUpdateView;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscapeapp.cyspanningtree.internal.cycle.ConnectedComponents;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class KruskalsTreeThread extends Thread {
    public boolean stop;
    public CyNetwork currentnetwork;
    public CyNetworkView currentnetworkview;
    boolean isMinimum;
    String edgeWeightAttribute;
    SpanningTreeStartMenu menu;

    public KruskalsTreeThread(CyNetwork currentnetwork, CyNetworkView currentnetworkview, boolean isMinimum, String edgeWeightAttribute, SpanningTreeStartMenu menu) {
        this.currentnetwork = currentnetwork;
        this.currentnetworkview = currentnetworkview;
        this.isMinimum = isMinimum;
        this.edgeWeightAttribute = edgeWeightAttribute;
        this.menu = menu;
    }

    // kruskals algo
    @Override
    public void run() {
        ConnectedComponents c = new ConnectedComponents();
        if(!c.isConnectedNetwork(currentnetwork)){
            System.out.println("Network is not connected. Multiple components exists! Please input a connected network");
            JOptionPane.showMessageDialog(null, "Network is not connected. Multiple components exists! Please input a connected network", "Unconnected network!", JOptionPane.ERROR_MESSAGE);
            return; 
        }
        menu.calculatingresult(null);
        stop = false;
        long lStartTime = System.currentTimeMillis();
        System.out.println("Strat time for Spanning Tree algo: " + lStartTime + " milli seconds");

        List<CyNode> nodeList = currentnetwork.getNodeList();
        int totalnodecount = nodeList.size();
        CyTable edgeTable = currentnetwork.getDefaultEdgeTable();
        CyTable nodeTable = currentnetwork.getDefaultNodeTable();

            // spanning tree
            double[][] adjacencyMatrixOfNetwork = createAdjMatrix(currentnetwork, nodeList, edgeTable, totalnodecount, edgeWeightAttribute);
            //printMatrix(adjacencyMatrixOfNetwork, "initail matrix");
            if(adjacencyMatrixOfNetwork == null)
                return;
            double[][] SpTreeAdjMatrix = createSpTreeAdjMatrix(adjacencyMatrixOfNetwork, nodeList, edgeTable, totalnodecount);
            //printMatrix(SpTreeAdjMatrix, "first spanning tree");
            if(SpTreeAdjMatrix == null)
                return;
            createNetwork(SpTreeAdjMatrix, nodeList, nodeTable, totalnodecount);

	long lEndTime = System.currentTimeMillis();
	long difference = lEndTime - lStartTime;
        System.out.println("End time for Spanning Tree algo: " + lEndTime + " milli seconds");
	System.out.println("Execution time for Spanning tree algo: " + difference +" milli seconds");
        menu.endOfComputation("Kruskal's spanning tree network created in Network panel");
    }

    public double[][] createAdjMatrix(CyNetwork currentnetwork, List<CyNode> nodeList, CyTable edgeTable, int totalnodecount, String edgeWeightAttribute) {
        //make an adjacencymatrix for the current network
        double[][] adjacencyMatrixOfNetwork = new double[totalnodecount][totalnodecount];
        for (int i = 0; i < totalnodecount; i++) {
            for (int j = 0; j < totalnodecount; j++) {
                if(stop)
                    return null;
                adjacencyMatrixOfNetwork[i][j] = Integer.MAX_VALUE;
            }
        }
        int k = 0;
        for (CyNode root : nodeList) {
            List<CyNode> neighbors = currentnetwork.getNeighborList(root, CyEdge.Type.OUTGOING);
            for (CyNode neighbor : neighbors) {
                if(stop)
                    return null;
                List<CyEdge> edges = currentnetwork.getConnectingEdgeList(root, neighbor, CyEdge.Type.DIRECTED);
                if (edges.size() > 0) {
                    CyRow row = edgeTable.getRow(edges.get(0).getSUID());
                    try {
                        if(edgeWeightAttribute == null)
                            adjacencyMatrixOfNetwork[k][nodeList.indexOf(neighbor)] = 1.0;
                        else{
                            try{
                                adjacencyMatrixOfNetwork[k][nodeList.indexOf(neighbor)] = Double.parseDouble(""+ row.get(edgeWeightAttribute, SpanningTreeStartMenu.edgeWeightAttributeColumn.getType()));
                            } catch(NumberFormatException ex){
                                String output = edgeWeightAttribute+" for the Edge ["+ row.get("name", String.class)
                                        +"] is not a number! it is rather a String ["
                                        + row.get(edgeWeightAttribute, SpanningTreeStartMenu.edgeWeightAttributeColumn.getType())
                                        +"]. Aborting! Please rectify and run the algorithm again.";
                                System.out.println(output);
                                JOptionPane.showMessageDialog(null, output, "Data inconsistency!", JOptionPane.ERROR_MESSAGE);
                                stop = true;
                                menu.endOfComputation("Aborted by user!");
                                return null;
                            }
                        }
                    } catch (NumberFormatException ex) {
                    }
                }
            }
            k++;
        }
        //printMatrix(adjacencyMatrixOfNetwork, "given matrix");
        return adjacencyMatrixOfNetwork;
    }

    public double[][] createSpTreeAdjMatrix(double[][] adjacencyMatrixOfNetwork, List<CyNode> nodeList, CyTable edgeTable, int totalnodecount) {
        // run kruskals algo
        double[][] adjacencyMatrixOfNetworkRelpica = new double[totalnodecount][totalnodecount];
        for (int i = 0; i < totalnodecount; i++) {
            for (int j = 0; j < totalnodecount; j++) {
                adjacencyMatrixOfNetworkRelpica[i][j] = adjacencyMatrixOfNetwork[i][j];
            }
        }
        Graph myGraph = new Graph(totalnodecount);
        int noOfEdges = 0;
        if (isMinimum) {
            while (noOfEdges < totalnodecount - 1) {
                Graph myGraphReplica = new Graph(myGraph);

                double minWeight = Integer.MAX_VALUE;
                int vertexi = 0;
                int vertexj = 0;
                for (int i = 0; i < totalnodecount; i++) {
                    for (int j = 0; j < totalnodecount; j++) {
                        if(stop)
                            return null;
                        if (adjacencyMatrixOfNetwork[i][j] < minWeight) {
                            minWeight = adjacencyMatrixOfNetwork[i][j];
                            vertexi = i;
                            vertexj = j;
                        }
                    }
                }
                //System.out.println("minimum value :"+minWeight+", point"+vertexi+","+vertexj);
                myGraph.addEdge(vertexi, vertexj);
                adjacencyMatrixOfNetwork[vertexi][vertexj] = Integer.MAX_VALUE;
                if (minWeight == Integer.MAX_VALUE) {
                    break;
                }
                Cycle myGraphCycle = new Cycle(myGraph);
                if (myGraphCycle.hasCycle()) {
                    myGraph = myGraphReplica;
                } else {
                    noOfEdges++;
                }
            }
        } else {
            while (noOfEdges < totalnodecount - 1) {
                Graph myGraphReplica = new Graph(myGraph);

                double maxWeight = Integer.MIN_VALUE;
                int vertexi = 0;
                int vertexj = 0;
                for (int i = 0; i < totalnodecount; i++) {
                    for (int j = 0; j < totalnodecount; j++) {
                        if(stop)
                            return null;
                        if (adjacencyMatrixOfNetwork[i][j] != Integer.MAX_VALUE) {
                        if (adjacencyMatrixOfNetwork[i][j] > maxWeight) {
                            maxWeight = adjacencyMatrixOfNetwork[i][j];
                            vertexi = i;
                            vertexj = j;
                        }
                        }
                    }
                }
                myGraph.addEdge(vertexi, vertexj);
                adjacencyMatrixOfNetwork[vertexi][vertexj] = Integer.MAX_VALUE;
                //System.out.println("Max weight is "+maxWeight+" at i="+vertexi+", j="+vertexj);
                if (maxWeight == Integer.MIN_VALUE) {
                    break;
                }
                Cycle myGraphCycle = new Cycle(myGraph);
                if (myGraphCycle.hasCycle()) {
                    myGraph = myGraphReplica;
                } else {
                    noOfEdges++;
                }
            }
        }
        //make a new adjacency matrix for the output tree
        adjacencyMatrixOfNetwork = adjacencyMatrixOfNetworkRelpica;
        double[][] adjacencyMatrixOfNewNetwork = new double[totalnodecount][totalnodecount];
        for (int i = 0; i < totalnodecount; i++) {
            for (int j = 0; j < totalnodecount; j++) {
                adjacencyMatrixOfNewNetwork[i][j] = Integer.MAX_VALUE;
            }
        }
        for (int i = 0; i < totalnodecount; i++) {
            for (Integer j : myGraph.adj(i)) {
                adjacencyMatrixOfNewNetwork[i][j] = adjacencyMatrixOfNetwork[i][j];
            }
        }
        return adjacencyMatrixOfNewNetwork;
    }

    public void createNetwork(double[][] adjacencyMatrixOfNewNetwork, List<CyNode> nodeList, CyTable nodeTable, int totalnodecount) {
        // get a edges in a List
        List<CyEdge> stEdgeList = new ArrayList<CyEdge>();
        for (int i = 0; i < totalnodecount; i++) {
            for (int j = 0; j < totalnodecount; j++) {
                double maxi = adjacencyMatrixOfNewNetwork[i][j];
                if (maxi > Integer.MIN_VALUE && maxi < Integer.MAX_VALUE) {
                    // TODO: Faizaan still have a doubt whether to use CyEdge.Type.DIRECTED / CyEdge.Type.ANY
                    List<CyEdge> edges = currentnetwork.getConnectingEdgeList(nodeList.get(i), nodeList.get(j), CyEdge.Type.DIRECTED);
                    if(edges.size() > 0)
                        stEdgeList.add(edges.get(0));
                }
            }
        }
        // select the nodes and edges
        List<CyEdge> edgeList = currentnetwork.getEdgeList();
        CyTable nTable = currentnetwork.getDefaultNodeTable();
        CyTable eTable = currentnetwork.getDefaultEdgeTable();
        
        for(CyNode n : nodeList){
            CyRow row = nTable.getRow(n.getSUID());
            row.set("selected", true);
        }
        
        for(CyEdge e: edgeList){
            CyRow row = eTable.getRow(e.getSUID());
            if(stEdgeList.contains(e)){
                row.set("selected", true);
            } else{
                row.set("selected", false);
            }
        }
        // create the network
        NewNetworkSelectedNodesAndEdgesTaskFactory f = CyActivator.adapter.
                get_NewNetworkSelectedNodesAndEdgesTaskFactory();
        TaskIterator itr = f.createTaskIterator(currentnetwork);
        CyActivator.adapter.getTaskManager().execute(itr);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(PrimsTreeThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        // set the name of the network
        this.menu.calculatingresult("Created! Renaming the network...");
        String currentNetworkName = currentnetwork.getRow(currentnetwork).get(CyNetwork.NAME, String.class);
        Set<CyNetwork> allnetworks = CyActivator.networkManager.getNetworkSet();
        long maxSUID = Integer.MIN_VALUE;
        for(CyNetwork net : allnetworks){
            if(net.getSUID() > maxSUID)
                maxSUID = net.getSUID();
        }
        CyNetwork STNetwork = CyActivator.networkManager.getNetwork(maxSUID);
        STNetwork.getRow(STNetwork).set(CyNetwork.NAME, currentNetworkName + " - Kruskal's Spanning Tree");
        
    }

    // Used when testing
    public static void printMatrix(double[][] matrix, String name) {
        System.out.println("The matrix " + name + " is :");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
    // Used when testing
    public static void printMatrixInt(int[][] matrix, String name) {
        System.out.println("The matrix " + name + " is :");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
    // Used when testing
    public void printGraph(Graph g, int n, String name) {
        System.out.println("The Graph " + name + " is :");
        for (int i = 0; i < n; i++) {
            System.out.print(i);
            for (Integer j : g.adj(i)) {
                System.out.print("--" + j);
            }
            System.out.println();
        }
    }
    
    public void end(){
        stop = true;
    }
}
