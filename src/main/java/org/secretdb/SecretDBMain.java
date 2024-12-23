package org.secretdb;

import com.google.common.base.Preconditions;
import jakarta.servlet.http.HttpServlet;
import org.secretdb.servlet.container.ServletContainer;
import org.secretdb.servlet.container.impl.TomcatServletContainer;
import org.secretdb.servlet.http.ListServlet;
import org.secretdb.servlet.http.ReadWriteServlet;

public class SecretDBMain {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        Preconditions.checkArgument(port > 1024, "Port number must be higher than 1024");

        final ServletContainer sc =
                new TomcatServletContainer(port);

        final HttpServlet listServlet = new ListServlet();
        sc.registerServlet("/api/list/*", listServlet);

        final HttpServlet readWriteServlet = new ReadWriteServlet();
        sc.registerServlet("/api/*", readWriteServlet);

        // TODO: add scheduled thread to backup secrets file every day/week.
        // Ideally, it could be a separate daemon service itself. But for simplicity, having it within this project for now

        sc.startAndAwait();
    }
}