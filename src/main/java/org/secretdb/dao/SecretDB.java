package org.secretdb.dao;

import org.secretdb.dao.model.Secret;
import org.secretdb.servlet.http.model.Payload;

import java.util.Optional;

public interface SecretDB {
    public void list(final String tenantId);

    public Optional<Secret> get(final String tenantId, final String name);

    public void write(final String tenantId, final Payload payload);
}
