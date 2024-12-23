package org.secretdb.servlet.container;

import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;

public interface ServletContainer {
    /**
     * Registering a servlet to container
     * @param path path of the registering servlet
     * @param servlet defined servlet
     * @param interceptors interceptors to run for pre post request
     */
    public void registerServlet(final String path, final Servlet servlet, Filter... interceptors);

    public void startAndAwait();

    public void stop();
}
