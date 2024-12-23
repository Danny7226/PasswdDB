package org.passwddb.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.passwddb.dao.SecretDB;
import org.passwddb.dao.model.Secret;
import org.passwddb.servlet.http.model.Payload;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * An implementation of SecretDB interface with on-disk IO
 */
public class OnDiskSecretDB implements SecretDB {
    private static final ObjectMapper om = new ObjectMapper();
    @Override
    public void list() {

    }

    public void getFromSpecifiedFile() {

    }

    @Override
    public void get() {

    }

    @Override
    public void write(final String tenantId, final Payload payload) {
        final String homeDirectory = System.getProperty("user.home");
        final String filePath = homeDirectory + "/secret_db/" + tenantId;

        final File dbFile = getOrCreate(filePath);

        logLatencyAndExecute(arg -> updateSecrets(arg, payload), dbFile);
        updateSecrets(dbFile, payload);
    }

    private <T,R> R logLatencyAndExecute(Function<T, R> func, T input) {
        final long startTime = System.currentTimeMillis();
        final R res = func.apply(input);
        final long endTime = System.currentTimeMillis();
        System.out.println("File updated successfully in " + (endTime - startTime) + " ms");
        return res;
    }

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
            throw new RuntimeException("Exception when reading file", e);
        }

        // Write updated lines back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsolutePath()))) {
            for (String line : updatedLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (final IOException e) {
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
                    System.out.println("Parent directories created successfully. " + parentDir.getAbsolutePath());
                } else {
                    System.out.println("Failed to create parent directories. " + parentDir.getAbsolutePath());
                }
            }

            // Create the file
            if (file.createNewFile()) {
                System.out.println("File created successfully at: " + file.getAbsolutePath());
            } else {
                System.out.println("File already exists at: " + file.getAbsolutePath());
            }

            return file;
        } catch (IOException e) {
            throw new RuntimeException("Error creating db file", e);
        }
    }
}
