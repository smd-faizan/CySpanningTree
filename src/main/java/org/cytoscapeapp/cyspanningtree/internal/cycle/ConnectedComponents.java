package org.cytoscapeapp.cyspanningtree.internal.cycle;

import java.util.List;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 *
 * @author smd.faizan@gmail.com
 */
// class that will contain graph basic utilities 
public class ConnectedComponents extends Thread{
    private CyNetwork network;
    private List<CyNode> nodeList;
    private boolean visited[];
    
    // check whether network is connected using DFS
    public boolean isConnectedNetwork(CyNetwork network){
        this.network = network;
        nodeList = network.getNodeList();
        visited = new boolean[network.getNodeCount()];
        for(int i=0;i<visited.length;i++)
            visited[i] = false;
        
        DFS(nodeList.get(0));
        
        return allNodesVisited();
    }
    
    private void DFS(CyNode node){
        visited[nodeList.indexOf(node)] = true;
        List<CyNode> neighbors = network.getNeighborList(node, CyEdge.Type.ANY);
        for(CyNode neighbor: neighbors){
            if(!visited[nodeList.indexOf(neighbor)])
                DFS(neighbor);
        }
    }
    
    private boolean allNodesVisited(){
        for(int i=0 ; i<visited.length ; i++){
            if(visited[i] == false){
                return false;
            }
        }
        return true;
    }
    
    
}
