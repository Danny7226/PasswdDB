package org.secretdb;

import jakarta.servlet.http.HttpServlet;
import org.secretdb.servlet.container.ServletContainer;
import org.secretdb.servlet.container.impl.TomcatServletContainer;
import org.secretdb.servlet.http.ListServlet;
import org.secretdb.servlet.http.ReadWriteServlet;

public class SecretDBMain {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        final ServletContainer tomcat =
                new TomcatServletContainer(port);
        final HttpServlet listServlet = new ListServlet();
        tomcat.registerServlet("/api/list/*", listServlet);

        final HttpServlet readWriteServlet = new ReadWriteServlet();
        tomcat.registerServlet("/api/*", readWriteServlet);

        tomcat.startAndAwait();
    }
}