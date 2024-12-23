package org.secretdb.servlet.http;

import dagger.Component;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.secretdb.dao.SecretDB;
import org.secretdb.dao.SecretDBFactory;
import org.secretdb.dao.model.Secret;
import org.secretdb.util.RequestUtil;
import org.secretdb.util.model.ValidationException;

import java.io.IOException;
import java.util.List;


public class ListServlet extends HttpServlet {

    @Inject
    SecretDBFactory secretDBFactory;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // list doesn't require an encryption key to list all names of secrets
        // Above should be done thru API AuthN/AuthZ instead of encryption
        final String tenantId;
        try {
            tenantId = RequestUtil.getTenantId(req);
        } catch (ValidationException e) {
            resp.setStatus(400);
            resp.getWriter().write("Bad tenant Id");
            return;
        }

        final SecretDB secretDB = secretDBFactory.getSecretDB(tenantId, SecretDBFactory.DB_MODE.READ);
        System.out.println("Using DB instance " + secretDB);

        final List<Secret> secretList = secretDB.list(tenantId);
        resp.setStatus(200);
        for (final Secret secret: secretList) {
            resp.getWriter().println(secret.getName());
        }
    }
}
