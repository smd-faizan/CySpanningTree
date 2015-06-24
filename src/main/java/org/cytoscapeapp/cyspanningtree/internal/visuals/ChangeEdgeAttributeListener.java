package org.cytoscapeapp.cyspanningtree.internal.visuals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscapeapp.cyspanningtree.internal.SpanningTreeCore;
import org.cytoscapeapp.cyspanningtree.internal.SpanningTreeStartMenu;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class ChangeEdgeAttributeListener implements SetCurrentNetworkListener{

    @Override
    public void handleEvent(SetCurrentNetworkEvent scne) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        CyNetwork network = scne.getNetwork();
        //JOptionPane.showMessageDialog(null, network.getEdgeCount()+" : nodes", "Spanning Tree", JOptionPane.ERROR_MESSAGE);
        
        SpanningTreeStartMenu menu = SpanningTreeCore.getSpanningTreeStartMenu();
        //menu.getEdgeAttributeComboBox().getModel().getSelectedItem().toString();
        menu.getEdgeAttributeComboBox().setModel(new javax.swing.DefaultComboBoxModel(getEdgeAttributes(network).toArray()));

        
    }

    public static List<String> getEdgeAttributes(CyNetwork network){
        Collection<CyColumn> edgeColumns = network.getDefaultEdgeTable().getColumns();
        List<String> columnsToAdd = new ArrayList<String>(1);
        
        int i = 0;
        for(CyColumn c:edgeColumns){
            if(!c.isPrimaryKey()){
                if(c.getType()==Double.class || c.getType()==Float.class 
                        || c.getType()==Integer.class || c.getType()==Long.class ){
                    columnsToAdd.add(c.getName());
                    i++;
                }
            }
        }
        columnsToAdd.add("None");
        
        return columnsToAdd;
    }
    
}
