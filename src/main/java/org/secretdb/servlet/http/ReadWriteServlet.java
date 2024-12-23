package org.secretdb.servlet.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.secretdb.cryptology.Crypto;
import org.secretdb.dao.SecretDB;
import org.secretdb.dao.SecretDBFactory;
import org.secretdb.dao.model.Secret;
import org.secretdb.servlet.http.model.Payload;
import org.secretdb.util.RequestUtil;
import org.secretdb.util.model.ValidationException;

import javax.crypto.BadPaddingException;
import java.io.IOException;
import java.util.Optional;

public class ReadWriteServlet extends HttpServlet {
    @Inject
    Crypto crypto;

    @Inject
    ObjectMapper om;

    @Inject
    SecretDBFactory secretDBFactory;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String tenantId;
        final String name;
        try {
            tenantId = RequestUtil.getTenantId(req);
            name = RequestUtil.getName(req);
        } catch (ValidationException e) {
            resp.setStatus(400);
            resp.getWriter().write("Bad input, please double check tenant id and make sure name is included as a query parameter");
            return;
        }

        final String privateKey = req.getParameter("key");

        final SecretDB secretDB = secretDBFactory.getSecretDB(tenantId, SecretDBFactory.DB_MODE.READ);
        System.out.println("Using DB instance " + secretDB);

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

        final SecretDB secretDB = secretDBFactory.getSecretDB(tenantId, SecretDBFactory.DB_MODE.WRITE);
        System.out.println("Using DB instance " + secretDB);

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