package org.secretdb.scheduled;

import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CleanupWorker implements Runnable{
    private static final Logger logger = LogManager.getLogger(CleanupWorker.class);
    private static final String APP_LOG_REGEX = "application_log.\\d{4}_\\d{2}_\\d{2}.gz";
    private static final String APP_LOG_PATH = System.getProperty("user.home") + "/secret_db/logs";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd");
    private static final int CLEANUP_THRESHOLD_IN_DAYS = 2; // this is effectively deleting logs 3 days or before

    private final AtomicLong currentIteration = new AtomicLong(0L);
    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        try {
            currentIteration.incrementAndGet();

            final File appLogDir = new File(APP_LOG_PATH);

            if (!appLogDir.exists() || !appLogDir.isDirectory()) {
                logger.error("Instructed application log folder doesn't exist, this is abnormal as I am starting late already. Please check!!");
                return;
            }

            deleteFilesInFolder(Paths.get(appLogDir.getPath()));
        } catch (final Throwable e) {
            logger.fatal(
                    "Cleanup worker got terminated somehow at iteration {}. " +
                            "Sleeping for 5s and will have to call the currently running JVM to exit. {}", currentIteration.get(), e);
            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);
            System.exit(-1);
        }
    }

    private static void deleteFilesInFolder(final Path target) throws IOException {
        final LocalDate xDaysAgo = LocalDate.now().minusDays(CLEANUP_THRESHOLD_IN_DAYS);

        // Walk through the targeting directory
        Files.walkFileTree(target, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                final String fileName = file.getFileName().toString();
                // Only process files that match the log naming pattern
                if (fileName.matches(APP_LOG_REGEX)) {
                    logger.info("visiting {}", file.toAbsolutePath());
                    try {
                        final String dateStr = fileName.substring(16,26);  // Extract "yyyy_mm_dd" part
                        final LocalDate fileDate = LocalDate.parse(dateStr, DATE_TIME_FORMATTER);

                        if (fileDate.isBefore(xDaysAgo)) {
                            Files.delete(file);
                           logger.info("Deleted {}", fileName);
                        } else {
                            logger.info("Keeping file {} as it's within {} days", fileName, CLEANUP_THRESHOLD_IN_DAYS);
                        }
                    } catch (Exception e) {
                        logger.error("Error processing file {}", fileName, e);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
