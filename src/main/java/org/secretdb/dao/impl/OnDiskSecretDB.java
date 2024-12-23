package org.secretdb.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.secretdb.cryptology.CryptoUtil;
import org.secretdb.dao.SecretDB;
import org.secretdb.dao.model.Secret;
import org.secretdb.servlet.http.model.Payload;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * An implementation of SecretDB interface with on-disk IO
 */
public class OnDiskSecretDB implements SecretDB {
    private static final Logger logger = LogManager.getLogger(CryptoUtil.class);

    private final ObjectMapper om;

    public OnDiskSecretDB(final ObjectMapper om) {
        this.om = om;
    }

    @Override
    public List<Secret> list(final String tenantId) {
        final String homeDirectory = System.getProperty("user.home");
        final String filePath = homeDirectory + "/secret_db/" + tenantId;
        final File dbFile = new File(filePath);

        if (!dbFile.exists()) return Collections.emptyList();

        return logLatencyAndExecute(arg -> listSecrets(arg), dbFile, "List");
    }

    public void getFromSpecifiedFile() {

    }

    @Override
    public Optional<Secret> get(final String tenantId, final String name) {
        final String homeDirectory = System.getProperty("user.home");
        final String filePath = homeDirectory + "/secret_db/" + tenantId;
        final File dbFile = new File(filePath);

        if (!dbFile.exists()) return Optional.empty();

        return logLatencyAndExecute(arg -> getSecret(arg, name), dbFile, "Get");
    }

    @Override
    public void write(final String tenantId, final Payload payload) {
        final String homeDirectory = System.getProperty("user.home");
        final String filePath = homeDirectory + "/secret_db/" + tenantId;
        final File dbFile = getOrCreate(filePath);

        logLatencyAndExecute(arg -> updateSecrets(arg, payload), dbFile, "Write");
        updateSecrets(dbFile, payload);
    }

    private <T,R> R logLatencyAndExecute(Function<T, R> func, T input, final String operationName) {
        final long startTime = System.currentTimeMillis();
        final R res = func.apply(input);
        final long endTime = System.currentTimeMillis();
        logger.info("{} Operation succeeded in {} ms",  operationName, (endTime - startTime));
        return res;
    }

    private List<Secret> listSecrets(final File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            List<Secret> secrets = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                final Secret secret = om.readValue(line, Secret.class);
                secrets.add(secret);
            }
            return secrets;
        } catch (final IOException e) {
            logger.error("Exception when reading file {}", e.toString());
            throw new RuntimeException("Exception when reading file", e);
        }
    }

    private Optional<Secret> getSecret(final File file, final String name) {
        Secret secret;
        try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            String line;
            while ((line = br.readLine()) != null) {
                secret = om.readValue(line, Secret.class);
                if (secret.getName().equals(name)) {
                    return Optional.of(secret);
                }
            }

            // no-found
            return Optional.empty();
        } catch (final IOException e) {
            logger.error("Exception when reading file {}", e.toString());
            throw new RuntimeException("Exception when reading file", e);
        }
    }

    // TODO: place write lock when write to prevent dirty writes, read doesn't require lock
    // This should be mitigated as we partition tenant db into separate files already, but in case rare concurrent modification, this is needed
    private Void updateSecrets(final File file, final Payload payload) {

        final List<String> updatedLines = new ArrayList<>();
        Boolean isExisting = false;

        Secret secret;
        try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            String line;
            while ((line = br.readLine()) != null) {
                secret = om.readValue(line, Secret.class);
                if (secret.getName().equals(payload.getName())) {
                    isExisting = true;
                    secret.setValue(payload.getValue());
                    updatedLines.add(om.writeValueAsString(secret));
                } else {
                    updatedLines.add(line);
                }
            }

            if (!isExisting) { // if this is a new secret
                updatedLines.add(om.writeValueAsString(
                        Secret.builder()
                                .name(payload.getName())
                                .value(payload.getValue())
                                .build()));
            }
        } catch (final IOException e) {
            logger.error("Exception when reading file {}", e.toString());
            throw new RuntimeException("Exception when reading file", e);
        }

        // Write updated lines back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsolutePath()))) {
            for (String line : updatedLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (final IOException e) {
            logger.error("Exception when writing file {}", e.toString());
            throw new RuntimeException("Exception when writing file", e);
        }
        return null;
    }

    private File getOrCreate(final String filePath) {
        final File file = new File(filePath);

        try {
            // Ensure the parent directories exist
            final File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    logger.info("Parent directories created successfully. " + parentDir.getAbsolutePath());
                } else {
                    logger.info("Failed to create parent directories. " + parentDir.getAbsolutePath());
                }
            }

            // Create the file
            if (file.createNewFile()) {
                logger.info("File created successfully at: " + file.getAbsolutePath());
            } else {
                logger.info("File already exists at: " + file.getAbsolutePath());
            }

            return file;
        } catch (IOException e) {
            logger.error("Exception when creating new file {}", e.toString());
            throw new RuntimeException("Error creating db file", e);
        }
    }
}
