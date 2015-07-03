package org.cytoscapeapp.cyspanningtree.internal.cycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.cytoscapeapp.cyspanningtree.internal.CyActivator;
import org.cytoscapeapp.cyspanningtree.internal.SpanningTreeStartMenu;
import static org.cytoscapeapp.cyspanningtree.internal.SpanningTreeStartMenu.pTreeThread;
import org.cytoscapeapp.cyspanningtree.internal.spanningtree.PrimsTreeThread;
import org.cytoscapeapp.cyspanningtree.internal.visuals.SpanningTreeUpdateView;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class HAMCycle extends Thread{
    private boolean stop = false;
    
    // Data Structures used for HAM
    private CyNode tempNode = null;
    private boolean[] visited;
    private List<CyEdge> resultEdgeList;
            
    private CyNode startNode;
    private CyNode lastNode;
    private CyNetwork wholeNetwork;
    private List<CyNode> nodeList;
    private int nodeCount;
    private CyNetworkView currentnetworkview;
    private String edgeWeightAttribute;
    private SpanningTreeStartMenu menu;
    private PrimsTreeThread pTreeThread;
    private boolean isMinimum;
    private CyNetwork STNetwork;
    
    public HAMCycle(PrimsTreeThread pTreeThread, CyNode startNode, CyNetwork wholeNetwork, 
            CyNetworkView currentnetworkview, String edgeWeightAttribute,
            boolean isMinimum, SpanningTreeStartMenu menu) {
        this.startNode = startNode;
        this.wholeNetwork = wholeNetwork;
        nodeList = wholeNetwork.getNodeList();
        nodeCount = wholeNetwork.getNodeCount();
        this.currentnetworkview = currentnetworkview;
        this.edgeWeightAttribute = edgeWeightAttribute;
        this.isMinimum = isMinimum;
        this.menu = menu;
        // Data Structures used for HAM
        visited = new boolean[nodeCount];
        resultEdgeList = new ArrayList<CyEdge>();
    }
    // prims algo
    @Override
    public void run() {
        pTreeThread = new PrimsTreeThread(wholeNetwork, currentnetworkview, isMinimum, edgeWeightAttribute, startNode, menu);
        pTreeThread.run();
//        if (pTreeThread != null && pTreeThread.isAlive()) {
//            try {
//                pTreeThread.join();
//            }catch(InterruptedException ex){
//            }
//        }
        this.STNetwork = pTreeThread.STNetwork;
        nodeList = STNetwork.getNodeList();
        nodeCount = STNetwork.getNodeCount();
//        menu.calculatingresult(null);
        stop = false;
        long lStartTime = System.currentTimeMillis();
        System.out.println("Start time for HAM cycle : " + lStartTime + " milli seconds");
        
        for(int i=0;i<visited.length;i++){
            visited[i] = false;
        }
        createHAMpath(startNode);
        // add the edge between lastNode and startNode to make HAM cycle
        List<CyEdge> edgesTo = wholeNetwork.getConnectingEdgeList(lastNode, startNode, CyEdge.Type.DIRECTED);
        if (edgesTo.size() > 0)
            resultEdgeList.add(edgesTo.get(0));
        else{
            // HAM cycle does not exist 
            System.out.println("HAM cycle cannot be created for the Prim's spanning tree!");
            menu.endOfComputation("HAM cycle cannot be created for the Prim's spanning tree!");
            return;
        }
        if(!allNodesVisited()){
            System.out.println("HAM cycle cannot be created!");
            menu.endOfComputation("HAM cycle cannot be created!");
        }
        else{
            createNetwork(wholeNetwork, nodeList, resultEdgeList);
            menu.endOfComputation("HAM cycle network created as a separate network in Network panel");
        }
        
        long lEndTime = System.currentTimeMillis();
	long difference = lEndTime - lStartTime;
        System.out.println("End time for HAM cycle : " + lEndTime + " milli seconds");
	System.out.println("Execution time for HAM cycle : " + difference +" milli seconds");
         
    }
    
    private void createHAMpath(CyNode node){
        visited[nodeList.indexOf(node)] = true;
        
        if(allNodesVisited()){
            lastNode = node;
            return;
        }
        
        if(allNeighborsVisited(node, STNetwork)){
            tempNode = node;
            return;
        }
        
        List<CyNode> neighbors = STNetwork.getNeighborList(node, CyEdge.Type.ANY);
        for(CyNode neighbor: neighbors){
            if(stop)
                return;
            if(!visited[nodeList.indexOf(neighbor)]){
                if(tempNode == null){
                    // add edge between node and neighbor
                    List<CyEdge> edgesTo = wholeNetwork.getConnectingEdgeList(node, neighbor, CyEdge.Type.DIRECTED);
                    if (edgesTo.size() > 0)
                        resultEdgeList.add(edgesTo.get(0));
                    else{
                        // HAM path does not exist 
                        return;
                    }
                }
                else{
                    // add edge between neighbor and tempnode
                    List<CyEdge> edgesTo = wholeNetwork.getConnectingEdgeList(tempNode, neighbor, CyEdge.Type.DIRECTED);
                    if (edgesTo.size() > 0)
                        resultEdgeList.add(edgesTo.get(0));
                    else{
                        // HAM path does not exist 
                        return;
                    }
                    tempNode = null;
                }
                createHAMpath(neighbor);
            }
        }

    }
    
    private boolean allNodesVisited(){
        for(int i=0 ; i<nodeCount ; i++){
            if(visited[i] == false){
                return false;
            }
        }
        return true;
    }
    
    private boolean allNeighborsVisited(CyNode node, CyNetwork network){
        List<CyNode> neighbors = network.getNeighborList(node, CyEdge.Type.ANY);
        for (CyNode neighbor : neighbors) {
            if(visited[nodeList.indexOf(neighbor)] == false){
                return false;
            }
        }
        return true;
    }
    
    public void createNetwork(CyNetwork network, List<CyNode> stnodeList, List<CyEdge> stedgeList){
        // select the nodes and edges
        CyTable nTable = network.getDefaultNodeTable();
        CyTable eTable = network.getDefaultEdgeTable();
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
        TaskIterator itr = f.createTaskIterator(network);
        CyActivator.adapter.getTaskManager().execute(itr);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(PrimsTreeThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        // set the name of the network
        this.menu.calculatingresult("Created! Renaming the network...");
        String currentNetworkName = network.getRow(network).get(CyNetwork.NAME, String.class);
        Set<CyNetwork> allnetworks = CyActivator.networkManager.getNetworkSet();
        long maxSUID = Integer.MIN_VALUE;
        for(CyNetwork net : allnetworks){
            if(net.getSUID() > maxSUID)
                maxSUID = net.getSUID();
        }
        this.STNetwork = CyActivator.networkManager.getNetwork(maxSUID);
        STNetwork.getRow(STNetwork).set(CyNetwork.NAME, currentNetworkName + " - Hamiltonian cycle");
        
    }
    
    public void end(){
        stop = true;
    }
    
}
