package org.secretdb.dao;

import org.secretdb.dao.model.Secret;

import java.util.List;
import java.util.Optional;

public interface SecretDB {
    List<Secret> list(final String tenantId);

    Optional<Secret> get(final String tenantId, final String name);

    void write(final String tenantId, final Secret secret);
}
