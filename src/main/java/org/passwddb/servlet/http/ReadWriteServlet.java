package org.passwddb.servlet.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.passwddb.servlet.http.model.Payload;

import java.io.BufferedReader;
import java.io.IOException;

public class ReadWriteServlet extends HttpServlet {
    private static final ObjectMapper om = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String tenantId = getTenantId(req);

        System.out.println("id " + req.getParameter("id"));
        System.out.println("key " + req.getParameter("key"));
        resp.getWriter().write("Get!");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // TODO, have a random Id generated for the name and encrypt value with the key in the payload
        final String tenantId = getTenantId(req);

        final Payload payload = getPayload(req);
        System.out.println(payload);
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
        final String tenantId = req.getPathInfo().substring(1); // remove prefixing "/"
        System.out.println("Tenant " + tenantId);

        return tenantId;
    }
}