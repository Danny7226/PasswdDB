package org.secretdb.servlet.container.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.secretdb.SecretDBMain;
import org.secretdb.servlet.container.ServletContainer;

import org.secretdb.servlet.container.model.ServletContainerException;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

public class TomcatServletContainer implements ServletContainer {
    private final Logger logger = LogManager.getLogger(TomcatServletContainer.class);

    private final Tomcat tomcat;
    private final Context context;

    public TomcatServletContainer(int port) {
        this.tomcat = new Tomcat();
        this.tomcat.setBaseDir(".");

        final Connector connector = new Connector();
        connector.setPort(port);
        connector.setProperty("address", "127.0.0.1"); // Restrict http traffic to localhost only
        this.tomcat.getService().addConnector(connector);

        this.context = this.tomcat.addContext("", new java.io.File(".").getAbsolutePath());
    }

    /**
     * Registering a servlet to container
     *
     * @param path  path of the registering servlet
     * @param servlet      defined servlet
     * @param interceptors interceptors to run for pre post request
     */
    @Override
    public void registerServlet(String path, Servlet servlet, Filter... interceptors) {
        final String servletName = String.format("%s_servlet", path);
        Tomcat.addServlet(this.context, servletName, servlet);
        this.context.addServletMappingDecoded(path, servletName);
    }

    @Override
    public void startAndAwait() throws ServletContainerException {
        try {
            this.tomcat.start();
            logger.info("Tomcat server started, blocking the main thread and listening...");
            this.tomcat.getServer().await();
        } catch (final LifecycleException e) {
            throw new ServletContainerException("Error starting servlet container", e);
        }
    }

    @Override
    public void stop() throws ServletContainerException {
        try {
            this.tomcat.stop();
        } catch (final LifecycleException e) {
            throw new ServletContainerException("Error stopping servlet container", e);
        }
    }
}
