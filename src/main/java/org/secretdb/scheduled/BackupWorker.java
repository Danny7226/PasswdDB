package org.secretdb.scheduled;

import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class BackupWorker implements Runnable{
    private static final String BACKUP_LOG_PATH = System.getProperty("user.home") + "/secret_db/backup/last_modify_date";
    private static final String SOURCE_DATA_DIR =  System.getProperty("user.home") + "/secret_db/data";
    private static final String BACKUP_DIR =  System.getProperty("user.home") + "/secret_db/backup";

    private static final Logger logger = LogManager.getLogger(BackupWorker.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AtomicLong currentIteration = new AtomicLong(0L);

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        try {
            currentIteration.incrementAndGet();

            final File backupLogFile = new File(BACKUP_LOG_PATH);

            Files.createDirectories(Paths.get(backupLogFile.getParentFile().getParent()));

            final LocalDate today = LocalDate.now();

            if (!backupLogFile.exists()) {
                logger.info("Back up journal doesn't exist, backing up and write journal now");
                backupAndWriteToJournalFile(backupLogFile, today);
            } else {
                // Read the file and compare dates
                final String dateOnFileString = readDateFromFile(backupLogFile);
                final LocalDate dateOnFile = LocalDate.parse(dateOnFileString, DATE_TIME_FORMATTER);
                if (today.equals(dateOnFile)) {
                    logger.info("Skipping iteration as I've backed up today");
                } else {
                    backupAndWriteToJournalFile(backupLogFile, today);
                }
            }
        } catch (final Throwable e) {
            logger.fatal(
                    "Back-up worker got terminated somehow at iteration {}. " +
                            "Sleeping for 5s and will have to call the currently running JVM to exit. {}", currentIteration.get(), e);
            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);
            System.exit(-1);
        }
    }

    private static void copyFilesInFolder(final Path source, final Path target) throws IOException {
        // Ensure the target directory exists
        Files.createDirectories(target);

        // Walk through the source directory
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Copy each file to the target
                final Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // TODO: make it atomic?
    private static void backupAndWriteToJournalFile(final File file, final LocalDate date) throws IOException {
        final String currentDate = date.format(DATE_TIME_FORMATTER);

        try {
            copyFilesInFolder(Paths.get(SOURCE_DATA_DIR), Paths.get(BACKUP_DIR + "/" + currentDate));
            logger.info("Folder copied successfully!");
        } catch (IOException e) {
            logger.error("Failed to copy folder: " + e);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(currentDate);
            writer.newLine();
            logger.info("Wrote new date to journal file");
        }
    }

    private static String readDateFromFile(final File file) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine().trim();
        }
    }
}
