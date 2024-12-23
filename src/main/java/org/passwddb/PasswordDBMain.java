package org.passwddb;

import jakarta.servlet.http.HttpServlet;
import org.passwddb.servlet.container.ServletContainer;
import org.passwddb.servlet.container.impl.TomcatServletContainer;
import org.passwddb.servlet.http.ListServlet;
import org.passwddb.servlet.http.ReadWriteServlet;

public class PasswordDBMain {
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