package org.secretdb.servlet.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.secretdb.cryptology.Crypto;
import org.secretdb.cryptology.SHA256;
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
    private static final Logger logger = LogManager.getLogger(ReadWriteServlet.class);

    @Inject
    Crypto crypto;

    @Inject
    ObjectMapper om;

    @Inject
    SecretDBFactory secretDBFactory;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String tenantId;
        try {
            tenantId = RequestUtil.getTenantId(req);
        } catch (ValidationException e) {
            resp.setStatus(400);
            resp.getWriter().write("Bad tenant Id");
            logger.warn("{} is a bad input, tenant related, I am returning 4xx", req.toString());
            return;
        }

        final Payload payload = getPayload(req);

        final SecretDB secretDB = secretDBFactory.getSecretDB(tenantId, SecretDBFactory.DB_MODE.READ);
        logger.info("Using DB instance " + secretDB);

        final Optional<Secret> secretOpt = secretDB.get(tenantId, payload.getName());
        if (secretOpt.isPresent()) {
            final String secret = secretOpt.get().getValue();
            try {
                final String decrypted = crypto.decrypt(payload.getKey(), secret);
                resp.setStatus(200);
                resp.getWriter().write(decrypted);
            } catch (final BadPaddingException e) {
                resp.setStatus(400);
                resp.getWriter().write("Bad input");
                logger.warn("Request on secret {} has a bad input, private key not correct, I am returning 4xx", payload.getName());
            }
        } else {
            resp.setStatus(404);
            resp.getWriter().write("Get not found");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String tenantId;
        try {
            tenantId = RequestUtil.getTenantId(req);
        } catch (ValidationException e) {
            resp.setStatus(400);
            resp.getWriter().write("Bad tenant Id");
            logger.warn("{} is a bad input, tenant related, I am returning 4xx", req.toString());
            return;
        }

        final Payload payload = getPayload(req);

        final SecretDB secretDB = secretDBFactory.getSecretDB(tenantId, SecretDBFactory.DB_MODE.WRITE);
        logger.info("Using DB instance " + secretDB);

        secretDB.write(tenantId,
                Secret.builder()
                        .name(payload.getName())
                        .value(crypto.encrypt(payload.getKey(), payload.getValue()))
                        .private_key_hash(SHA256.hash(payload.getKey()))
                        .build());

        resp.getWriter().write("Write succeeded!");
    }

    private Payload getPayload(final HttpServletRequest req) {
        try {
            return om.readValue(req.getReader(), Payload.class);
        } catch (final IOException e) {
            throw new RuntimeException("Exception deserializing payload", e); // TODO change to 4xx client exception
        }
    }
}