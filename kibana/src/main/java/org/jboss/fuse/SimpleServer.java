package org.jboss.fuse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.slf4j.Logger;

import java.util.Enumeration;
import java.util.Properties;

public class SimpleServer {

    protected final static Logger LOG = org.slf4j.LoggerFactory.getLogger(SimpleServer.class);

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(System.getProperty("webPort"));
        LOG.info("[Port : " + port + "]");

        Server server = new Server(port);

        ResourceHandler handler = new ResourceHandler();
        handler.setDirectoriesListed(true);
        handler.setWelcomeFiles(new String[]{ "index.html" });
        LOG.info("[index.html page registered as welcome page]");

        handler.setResourceBase("src/main/resources");
        LOG.info("[Resource Base point to the resources directory]");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { handler, new DefaultHandler() });
        server.setHandler(handlers);

        LOG.info(("[HTTP Simple Server started at the address : http://localhost:9090]"));

        server.start();
        server.join();
    }
}