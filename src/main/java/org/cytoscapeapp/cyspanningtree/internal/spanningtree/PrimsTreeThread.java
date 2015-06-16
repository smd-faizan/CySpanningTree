package org.cytoscapeapp.cyspanningtree.internal.spanningtree;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;

import org.cytoscapeapp.cyspanningtree.internal.SpanningTreeStartMenu;
import org.cytoscapeapp.cyspanningtree.internal.CyActivator;
import org.cytoscapeapp.cyspanningtree.internal.visuals.SpanningTreeUpdateView;

public class PrimsTreeThread extends Thread{
    public boolean stop;
    public CyNetwork currentnetwork;
    public CyNetworkView currentnetworkview;
    boolean isMinimum;
    CyNode rootNode;
    String edgeWeightAttribute;
    SpanningTreeStartMenu menu;
    
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
                                    edgeValue = Double.parseDouble(""+ row.get(edgeWeightAttribute, SpanningTreeStartMenu.edgeWeightAttributeColumn.getType()));
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
                                    edgeValue = Double.parseDouble(""+ row.get(edgeWeightAttribute, SpanningTreeStartMenu.edgeWeightAttributeColumn.getType()));
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
        CyRootNetwork root = ((CySubNetwork)currentnetwork).getRootNetwork();
        CyNetwork stNetwork = root.addSubNetwork(stnodeList, stedgeList);
        stNetwork.getRow(stNetwork).set(CyNetwork.NAME, "Prim's Spanning Tree");
        CyNetworkManager networkManager = CyActivator.networkManager;
        networkManager.addNetwork(stNetwork);
        CyNetworkView stView = CyActivator.networkViewFactory.createNetworkView(stNetwork);
        CyActivator.networkViewManager.addNetworkView(stView);
        SpanningTreeUpdateView.updateView(stView, "grid");// 2nd argument name of layout
    }
    
    
    public void end(){
        stop = true;
    }
}