package org.cytoscape.spanningtree.internal;

import java.util.Properties;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

    public CyApplicationManager cyApplicationManager;
    public CySwingApplication cyDesktopService;
    public CyServiceRegistrar cyServiceRegistrar;
    public SpanningTreeMenuAction menuaction;
    public static CyNetworkFactory networkFactory;
    public static CyNetworkManager networkManager;
    public static CyNetworkViewFactory networkViewFactory;
    public static CyNetworkViewManager networkViewManager;
    public static VisualStyleFactory visualStyleFactoryServiceRef;
    public static VisualMappingFunctionFactory vmfFactoryP;
    public static VisualMappingManager vmmServiceRef;
    public static VisualMappingFunctionFactory vmfFactoryC;
    public static VisualMappingFunctionFactory vmfFactoryD;
    
    @Override
    public void start(BundleContext context) throws Exception {
        String version = new String("1.0");
        this.networkViewManager = getService(context, CyNetworkViewManager.class);
        this.networkViewFactory = getService(context, CyNetworkViewFactory.class);
        this.networkFactory = getService(context, CyNetworkFactory.class);
        this.networkManager = getService(context, CyNetworkManager.class);
        this.cyApplicationManager = getService(context, CyApplicationManager.class);
        this.cyDesktopService = getService(context, CySwingApplication.class);
        this.cyServiceRegistrar = getService(context, CyServiceRegistrar.class);
        this.visualStyleFactoryServiceRef = getService(context,VisualStyleFactory.class);
        this.vmfFactoryP = getService(context,VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        this.vmmServiceRef = getService(context,VisualMappingManager.class);
        this.vmfFactoryC = getService(context,VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
        this.vmfFactoryD = getService(context,VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
  
        menuaction = new SpanningTreeMenuAction(cyApplicationManager, "CySpanningTree " + version, this);
        Properties properties = new Properties();
        registerAllServices(context, menuaction, properties);
    }

    public CyServiceRegistrar getcyServiceRegistrar() {
        return cyServiceRegistrar;
    }

    public CyApplicationManager getcyApplicationManager() {
        return cyApplicationManager;
    }

    public CySwingApplication getcytoscapeDesktopService() {
        return cyDesktopService;
    }

    public SpanningTreeMenuAction getmenuaction() {
        return menuaction;
    }
}
