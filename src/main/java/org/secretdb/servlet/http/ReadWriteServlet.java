package org.secretdb.servlet.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.secretdb.cryptology.Crypto;
import org.secretdb.dao.SecretDB;
import org.secretdb.dao.impl.OnDiskSecretDB;
import org.secretdb.dao.model.Secret;
import org.secretdb.servlet.http.model.Payload;
import org.secretdb.util.RequestUtil;
import org.secretdb.util.model.ValidationException;

import javax.crypto.BadPaddingException;
import java.io.IOException;
import java.util.Optional;

public class ReadWriteServlet extends HttpServlet {
    // TODO: could be singleton
    private static final Crypto crypto = new Crypto();

    // TODO: move below to Factory so that we could have one instance per tenant for better servlet isolation and thread safety
    // Alternatively, have read or write DB instances
    private static final ObjectMapper om = new ObjectMapper();
    private static final SecretDB secretDB = new OnDiskSecretDB();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String tenantId;
        try {
            tenantId = RequestUtil.getTenantId(req);
        } catch (ValidationException e) {
            resp.setStatus(400);
            resp.getWriter().write("Bad tenant Id");
            return;
        }

        final String name = req.getParameter("name");

        if (name == null || name.isBlank()) {
            resp.setStatus(400);
            resp.getWriter().write("bad input, name needs to present as query parameter");
        }

        final String privateKey = req.getParameter("key");

        final Optional<Secret> secretOpt = secretDB.get(tenantId, name);
        if (secretOpt.isPresent()) {
            final String secret = secretOpt.get().getValue();
            try {
                final String decrypted = crypto.decrypt(privateKey, secret);
                resp.setStatus(200);
                resp.getWriter().write(decrypted);
            } catch (final BadPaddingException e) {
                resp.setStatus(400);
                resp.getWriter().write("Bad input");
            }
        } else {
            resp.setStatus(404);
            resp.getWriter().write("Get not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // TODO, encrypt value with the key in the payload
        final String tenantId;
        try {
            tenantId = RequestUtil.getTenantId(req);
        } catch (ValidationException e) {
            resp.setStatus(400);
            resp.getWriter().write("Bad tenant Id");
            return;
        }

        final Payload payload = getPayload(req);
        // TODO, validate payload so that key and name cannot have white space nor special characters

        payload.setValue(crypto.encrypt(payload.getKey(), payload.getValue()));

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
}