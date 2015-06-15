/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cytoscape.spanningtree.internal.spanningtree;

import com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import java.util.ArrayList;
import java.util.List;
import org.cytoscape.model.*;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.spanningtree.internal.CyActivator;
import org.cytoscape.spanningtree.internal.SpanningTreeStartMenu;
import org.cytoscape.spanningtree.internal.visuals.Saloon;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class SpanningTreeThread extends Thread {
    public boolean stop;
    public CyNetwork currentnetwork;
    public CyNetworkView currentnetworkview;
    boolean isMinimum;
    String edgeWeightAttribute;
    SpanningTreeStartMenu menu;

    public SpanningTreeThread(CyNetwork currentnetwork, CyNetworkView currentnetworkview, boolean isMinimum, String edgeWeightAttribute, SpanningTreeStartMenu menu) {
        this.currentnetwork = currentnetwork;
        this.currentnetworkview = currentnetworkview;
        this.isMinimum = isMinimum;
        this.edgeWeightAttribute = edgeWeightAttribute;
        this.menu = menu;
    }

    // kruskals algo
    @Override
    public void run() {
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
        menu.endOfComputation("Spanning tree network created in Network panel");
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
                        adjacencyMatrixOfNetwork[k][nodeList.indexOf(neighbor)] = Double.parseDouble(""+ row.get(edgeWeightAttribute, SpanningTreeStartMenu.edgeWeightAttributeColumn.getType()));
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
        CyRootNetwork root = ((CySubNetwork)currentnetwork).getRootNetwork();
        CyNetwork stNetwork = root.addSubNetwork(nodeList, stEdgeList);
        stNetwork.getRow(stNetwork).set(CyNetwork.NAME, "Kruskal's Spanning Tree");
        CyNetworkManager networkManager = CyActivator.networkManager;
        networkManager.addNetwork(stNetwork);
        CyNetworkView stView = CyActivator.networkViewFactory.createNetworkView(stNetwork);
        CyActivator.networkViewManager.addNetworkView(stView);
                    
                    
        
        
        /* Let it RIP for sometime
        CyNetwork SpanningTree;
        // To get a reference of CyNetworkFactory at CyActivator class of the App
        CyNetworkFactory networkFactory = CyActivator.networkFactory;
        // Create a new network
        SpanningTree = networkFactory.createNetwork();

        // Set name for network
        SpanningTree.getRow(SpanningTree).set(CyNetwork.NAME, "Kruskal's Spanning Tree");

        // Add nodes to the network
        List<CyNode> nodesInNewNetwork = new ArrayList<CyNode>(totalnodecount);
        for (int i = 0; i < nodeList.size(); i++) {
            nodesInNewNetwork.add(SpanningTree.addNode());
        }
        // Set name for new nodes
        for (int i = 0; i < nodeList.size(); i++) {
            SpanningTree.getRow(nodesInNewNetwork.get(i)).set(CyNetwork.NAME, nodeTable.getRow(nodeList.get(i).getSUID()).get(CyNetwork.NAME, String.class));
        }
        // add edge atribute with name edgeWeightAttribute
        CyTable edgeTable = SpanningTree.getDefaultEdgeTable();
        if(edgeTable.getColumn(edgeWeightAttribute) == null){
            edgeTable.createColumn(edgeWeightAttribute, SpanningTreeStartMenu.edgeWeightAttributeColumn.getType(), 
                    SpanningTreeStartMenu.edgeWeightAttributeColumn.isImmutable());
        }
        //add edges
        for (int i = 0; i < totalnodecount; i++) {
            for (int j = 0; j < totalnodecount; j++) {
                double maxi = adjacencyMatrixOfNewNetwork[i][j];
                if (maxi > Integer.MIN_VALUE && maxi < Integer.MAX_VALUE) {
                    CyEdge root = SpanningTree.addEdge(nodesInNewNetwork.get(i), nodesInNewNetwork.get(j), true);
                    CyRow row = SpanningTree.getDefaultEdgeTable().getRow(root.getSUID());
                    if(SpanningTreeStartMenu.edgeWeightAttributeColumn.getType() == String.class)
                        row.set(edgeWeightAttribute, "" + maxi);
                    else if(SpanningTreeStartMenu.edgeWeightAttributeColumn.getType() == Double.class)
                        row.set(edgeWeightAttribute, maxi);
                    else if(SpanningTreeStartMenu.edgeWeightAttributeColumn.getType() == Long.class)
                        row.set(edgeWeightAttribute, (long)maxi);
                    else if(SpanningTreeStartMenu.edgeWeightAttributeColumn.getType() == Integer.class)
                        row.set(edgeWeightAttribute, (int)maxi);
                }
            }
        }

        // Add the network to Cytoscape
        CyNetworkManager networkManager = CyActivator.networkManager;
        networkManager.addNetwork(SpanningTree);
        
        //Add view to cyto
//        CyNetworkView myView = CyActivator.networkViewFactory.createNetworkView(SpanningTree);
//        CyActivator.networkViewManager.addNetworkView(myView);
        
        // Apply Style
//        Saloon.applyStyle(myView);
                */
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
