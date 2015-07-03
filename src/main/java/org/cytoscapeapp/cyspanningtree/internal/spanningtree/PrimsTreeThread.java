package org.cytoscapeapp.cyspanningtree.internal.spanningtree;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

import org.cytoscapeapp.cyspanningtree.internal.SpanningTreeStartMenu;
import org.cytoscapeapp.cyspanningtree.internal.CyActivator;
import org.cytoscapeapp.cyspanningtree.internal.cycle.ConnectedComponents;
import org.cytoscapeapp.cyspanningtree.internal.visuals.SpanningTreeUpdateView;

public class PrimsTreeThread extends Thread{
    public boolean stop;
    public CyNetwork currentnetwork;
    public CyNetworkView currentnetworkview;
    boolean isMinimum;
    CyNode rootNode;
    String edgeWeightAttribute;
    SpanningTreeStartMenu menu;
    public CyNetwork STNetwork = null;
    
    public PrimsTreeThread(CyNetwork currentnetwork, CyNetworkView currentnetworkview, boolean isMinimum, String edgeWeightAttribute, CyNode rootNode, SpanningTreeStartMenu menu) {
        this.currentnetwork = currentnetwork;
        this.currentnetworkview = currentnetworkview;
        this.isMinimum = isMinimum;
        this.rootNode = rootNode;
        this.edgeWeightAttribute = edgeWeightAttribute;
        this.menu = menu;
    }
    // prims algo
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
        System.out.println("Start time for PRIMS Spanning Tree algo: " + lStartTime + " milli seconds");
        
        List<CyNode> nodeList = currentnetwork.getNodeList();
        CyTable edgeTable = currentnetwork.getDefaultEdgeTable();
        int totalnodecount = nodeList.size();
        List<CyNode> nodes = new ArrayList<CyNode>();// spanning tree nodes, all nodes ideally
        List<CyEdge> edges = new ArrayList<CyEdge>();// spanning tree edges
        boolean[] visited = new boolean[totalnodecount];
        for(int i=0;i<visited.length;i++){
            visited[i] = false;
        }
        if(rootNode==null){
            rootNode = nodeList.get(0);
        }
        visited[nodeList.indexOf(rootNode)] = true;
        nodes.add(rootNode);
        double cmp;
        double edgeValue;
        
        ListIterator<CyNode> itr; 
        if(isMinimum){
            while(nodes.size() != totalnodecount){
                cmp = Double.MAX_VALUE; 
                CyNode nextNode = null;
                CyEdge nextEdge = null;
                CyNode curr;
                itr = nodes.listIterator();
                while(itr.hasNext()){
                    if(stop)
                        return;
                    curr = itr.next();
                    List<CyNode> neighbors = currentnetwork.getNeighborList(curr, CyEdge.Type.ANY);
                    for (CyNode neighbor : neighbors) {
                        if(!visited[nodeList.indexOf(neighbor)]){
                            List<CyEdge> edgesTo = currentnetwork.getConnectingEdgeList(curr, neighbor, CyEdge.Type.DIRECTED);
                            if (edgesTo.size() > 0) {
                                CyRow row = edgeTable.getRow(edgesTo.get(0).getSUID());
                                try {
                                    if(edgeWeightAttribute == null)
                                        edgeValue = 1.0;
                                    else{
                                        try{
                                            edgeValue = Double.parseDouble(""+ row.get(edgeWeightAttribute, SpanningTreeStartMenu.edgeWeightAttributeColumn.getType()));
                                        } catch(NumberFormatException ex){
                                            String output = edgeWeightAttribute+" for the Edge ["+ row.get("name", String.class)
                                                    +"] is not a number! it is rather a String ["
                                                    + row.get(edgeWeightAttribute, SpanningTreeStartMenu.edgeWeightAttributeColumn.getType())+"]";
                                            System.out.println(output);
                                            JOptionPane.showMessageDialog(null, output, "Data inconsistency!", JOptionPane.ERROR_MESSAGE);
                                            stop = true;
                                            menu.endOfComputation("Aborted by user!");
                                            return;
                                        }
                                    }
                                    if (edgeValue < cmp) {
                                        cmp = edgeValue;
                                        nextNode = neighbor;
                                        nextEdge = edgesTo.get(0);
                                    } 
                                } catch (NumberFormatException ex) {
                                }
                            }

                        }

                    }

                }
                if(nextNode!=null && nextEdge!=null){
                    nodes.add(nextNode);
                    edges.add(nextEdge);
                    visited[nodeList.indexOf(nextNode)] = true;
                }
            }
              
        } else{
            while(nodes.size() != totalnodecount){
                cmp = (double)Integer.MIN_VALUE; 
                CyNode nextNode = null;
                CyEdge nextEdge = null;
                CyNode curr;
                itr = nodes.listIterator();
                while(itr.hasNext()){
                    if(stop)
                        return;
                    curr = itr.next();
                    List<CyNode> neighbors = currentnetwork.getNeighborList(curr, CyEdge.Type.ANY);
                    for (CyNode neighbor : neighbors) {
                        if(!visited[nodeList.indexOf(neighbor)]){
                            List<CyEdge> edgesTo = currentnetwork.getConnectingEdgeList(curr, neighbor, CyEdge.Type.DIRECTED);
                            if (edgesTo.size() > 0) {
                                CyRow row = edgeTable.getRow(edgesTo.get(0).getSUID());
                                try {
                                    if(edgeWeightAttribute == null)
                                        edgeValue = 1.0;
                                    else{
                                        try{
                                            edgeValue = Double.parseDouble(""+ row.get(edgeWeightAttribute, SpanningTreeStartMenu.edgeWeightAttributeColumn.getType()));
                                        } catch(NumberFormatException ex){
                                            String output = edgeWeightAttribute+" for the Edge ["+ row.get("name", String.class)
                                                    +"] is not a number! it is rather a String ["
                                                    + row.get(edgeWeightAttribute, SpanningTreeStartMenu.edgeWeightAttributeColumn.getType())+"]";
                                            System.out.println(output);
                                            JOptionPane.showMessageDialog(null, output, "Data inconsistency!", JOptionPane.ERROR_MESSAGE);
                                            stop = true;
                                            menu.endOfComputation("Aborted by user!");
                                            return;
                                        }
                                    }
                                    if (edgeValue > cmp) {
                                        cmp = edgeValue;
                                        nextNode = neighbor;
                                        nextEdge = edgesTo.get(0);
                                    } 
                                } catch (NumberFormatException ex) {
                                }
                            }

                        }

                    }

                }
                if(nextNode!=null && nextEdge!=null){
                    nodes.add(nextNode);
                    edges.add(nextEdge);
                    visited[nodeList.indexOf(nextNode)] = true;
                }
            }                    
        }

        createNetwork(nodes, edges);
        
        long lEndTime = System.currentTimeMillis();
	long difference = lEndTime - lStartTime;
        System.out.println("End time for PRIMS Spanning Tree algo: " + lEndTime + " milli seconds");
	System.out.println("Execution time for PRIMS Spanning tree algo: " + difference +" milli seconds");
        menu.endOfComputation("Prim's Spanning tree network created in Network panel"); 
    }
    
    public void createNetwork(List<CyNode> stnodeList, List<CyEdge> stedgeList){
        // select the nodes and edges
        CyTable nTable = currentnetwork.getDefaultNodeTable();
        CyTable eTable = currentnetwork.getDefaultEdgeTable();
        for(CyEdge e : stedgeList){
            CyRow row = eTable.getRow(e.getSUID());
            row.set("selected", true);
        }
        for(CyNode n : stnodeList){
            CyRow row = eTable.getRow(n.getSUID());
            row.set("selected", true);
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
        this.STNetwork = CyActivator.networkManager.getNetwork(maxSUID);
        STNetwork.getRow(STNetwork).set(CyNetwork.NAME, currentNetworkName + " - Prim's Spanning Tree");
        
    }
    
    
    public void end(){
        stop = true;
    }
}