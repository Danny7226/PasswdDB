package org.secretdb;

import com.google.common.base.Preconditions;
import org.secretdb.module.DaggerSecretDBComponent;
import org.secretdb.module.SecretDBComponent;
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

        final SecretDBComponent daggerComponent = DaggerSecretDBComponent.create();

        final ListServlet listServlet = new ListServlet();
        daggerComponent.inject(listServlet);
        sc.registerServlet("/api/list/*", listServlet);

        final ReadWriteServlet readWriteServlet = new ReadWriteServlet();
        daggerComponent.inject(readWriteServlet);
        sc.registerServlet("/api/*", readWriteServlet);

        // TODO: add scheduled thread to backup secrets file every day/week.
        // Ideally, it could be a separate daemon service itself. But for simplicity, having it within this project for now

        sc.startAndAwait();
    }
}