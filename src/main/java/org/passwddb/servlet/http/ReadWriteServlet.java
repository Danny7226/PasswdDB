package org.passwddb.servlet.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.passwddb.dao.SecretDB;
import org.passwddb.dao.impl.OnDiskSecretDB;
import org.passwddb.servlet.http.model.Payload;

import java.io.IOException;

public class ReadWriteServlet extends HttpServlet {
    private static final ObjectMapper om = new ObjectMapper();
    private static final SecretDB secretDB = new OnDiskSecretDB();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String tenantId = getTenantId(req);
        // TODO: check if tenant exists

        System.out.println("id " + req.getParameter("id"));
        System.out.println("key " + req.getParameter("key"));
        resp.getWriter().write("Get!");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // TODO, have a random Id generated for the name and encrypt value with the key in the payload
        final String tenantId = getTenantId(req);
        // write to a dedicated file for each tenant

        final Payload payload = getPayload(req);
        System.out.println(payload);

        secretDB.write(tenantId, payload);
        resp.getWriter().write("POST!");
    }

    private Payload getPayload(final HttpServletRequest req) {
        try {
            return om.readValue(req.getReader(), Payload.class);
        } catch (final IOException e) {
            throw new RuntimeException("Exception deserializing payload", e); // TODO change to 4xx client exception
        }
    }

    private String getTenantId(final HttpServletRequest req) {
        // TODO: validate path info, cannot have special character and cannot have non-prefixing "/"
        final String tenantId = req.getPathInfo().replaceFirst("^/", ""); // remove prefixing "/"
        System.out.println("Tenant " + tenantId);

        return tenantId;
    }
}