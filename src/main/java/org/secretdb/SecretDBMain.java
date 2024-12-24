package org.secretdb;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.secretdb.scheduled.BackupWorker;
import org.secretdb.module.DaggerSecretDBComponent;
import org.secretdb.module.SecretDBComponent;
import org.secretdb.scheduled.CleanupWorker;
import org.secretdb.servlet.container.ServletContainer;
import org.secretdb.servlet.container.impl.TomcatServletContainer;
import org.secretdb.servlet.http.ListServlet;
import org.secretdb.servlet.http.ReadWriteServlet;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

        // Ideally, scheduled-threads could be a separate daemon service itself. But for simplicity, having it within this project for now
        scheduleWorkerForBackupAndStartServer(sc);
    }

    private static void scheduleWorkerForBackupAndStartServer(final ServletContainer sc) {
        final Logger logger = LogManager.getLogger(SecretDBMain.class);

        // TODO: have a clean up work that cleans up application_log more than 7 days. Backup cleanup can be dangerous as no monitoring on the in-use file.
        // Override default thread factory to create daemon thread for back-up worker as it's non-critical and shouldn't block jvm shutting up
        try (final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        })) {
            logger.info("Scheduling backup and cleanup daemon worker threads...");
            executorService.scheduleWithFixedDelay(new BackupWorker(), 1, 30, TimeUnit.MINUTES);
            executorService.scheduleWithFixedDelay(new CleanupWorker(), 2, 30, TimeUnit.MINUTES);
            logger.info("Daemon workers scheduled, spinning up server...");
            sc.startAndAwait();
        } finally {
            logger.fatal("For unknown reasons, main thread exits. This is unexpected. Sleeping for 5000 ms before exiting.");
            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);
        }
    }
}