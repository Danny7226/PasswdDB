package org.passwddb.dao;

import org.passwddb.servlet.http.model.Payload;

public interface SecretDB {
    public void list();

    public void get();

    public void write(final String tenantId, final Payload payload);
}
