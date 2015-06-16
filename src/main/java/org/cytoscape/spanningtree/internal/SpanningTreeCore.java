package org.cytoscape.spanningtree.internal;

import java.util.Properties;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class SpanningTreeCore {

    public CyNetwork network;
    public CyNetworkView view;
    public CyApplicationManager cyApplicationManager;
    public CySwingApplication cyDesktopService;
    public CyServiceRegistrar cyServiceRegistrar;
    public CyActivator cyactivator;
    public SpanningTreeStartMenu spanningtreestartmenu;

    public SpanningTreeCore(CyActivator cyactivator) {
        this.cyactivator = cyactivator;
        this.cyApplicationManager = cyactivator.cyApplicationManager;
        this.cyDesktopService = cyactivator.cyDesktopService;
        this.cyServiceRegistrar = cyactivator.cyServiceRegistrar;
        spanningtreestartmenu = createSpanningTreeStartMenu();
        updatecurrentnetwork();
    }

    public void updatecurrentnetwork() {
        //get the network view object
        if (view == null) {
            view = null;
            network = null;
        } else {
            view = cyApplicationManager.getCurrentNetworkView();
            //get the network object; this contains the graph  
            network = view.getModel();
        }
    }

    public void closecore() {
        network = null;
        view = null;
    }

    public SpanningTreeStartMenu createSpanningTreeStartMenu() {
        SpanningTreeStartMenu startmenu = new SpanningTreeStartMenu(cyactivator, this);
        cyServiceRegistrar.registerService(startmenu, CytoPanelComponent.class, new Properties());
        CytoPanel cytopanelwest = cyDesktopService.getCytoPanel(CytoPanelName.WEST);
        int index = cytopanelwest.indexOfComponent(startmenu);
        cytopanelwest.setSelectedIndex(index);
        return startmenu;
    }

    public void closeSpanningTreeStartMenu() {
        cyServiceRegistrar.unregisterService(spanningtreestartmenu, CytoPanelComponent.class);
    }

    

    public CyApplicationManager getCyApplicationManager() {
        return this.cyApplicationManager;
    }

    public CySwingApplication getCyDesktopService() {
        return this.cyDesktopService;
    }
}
