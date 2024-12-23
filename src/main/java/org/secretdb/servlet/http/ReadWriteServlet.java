package org.secretdb.servlet.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.secretdb.dao.SecretDB;
import org.secretdb.dao.impl.OnDiskSecretDB;
import org.secretdb.dao.model.Secret;
import org.secretdb.servlet.http.model.Payload;

import java.io.IOException;
import java.util.Optional;

public class ReadWriteServlet extends HttpServlet {
    private static final ObjectMapper om = new ObjectMapper();
    private static final SecretDB secretDB = new OnDiskSecretDB();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String tenantId = getTenantId(req);

        final String name = req.getParameter("name");

        if (name == null || name.isBlank()) {
            resp.setStatus(400);
            resp.getWriter().write("bad input, name needs to present as query parameter");
        }

        System.out.println("key " + req.getParameter("key"));

        final Optional<Secret> secretOpt = secretDB.get(tenantId, name);
        if (secretOpt.isPresent()) {
            resp.setStatus(200);
            resp.getWriter().write(om.writeValueAsString(secretOpt.get()));
        } else {
            resp.setStatus(404);
            resp.getWriter().write("Get not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // TODO, encrypt value with the key in the payload
        final String tenantId = getTenantId(req);
        // write to a dedicated file for each tenant

        final Payload payload = getPayload(req);
        // TODO, validate payload so that key and name cannot have white space and special characters

        secretDB.write(tenantId, payload);
        resp.getWriter().write("POST succeeded!");
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