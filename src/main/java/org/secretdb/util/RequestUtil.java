package org.secretdb.util;

import jakarta.servlet.http.HttpServletRequest;
import org.secretdb.util.model.ValidationException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class RequestUtil {
    public static String getTenantId(final HttpServletRequest req) throws ValidationException {
        // TODO: validate path info, cannot have special character and cannot have non-prefixing "/"
        final String tenantId = req.getPathInfo().replaceFirst("^/", ""); // remove prefixing "/"
        System.out.println("Tenant " + tenantId);

        if (tenantId.isBlank()) throw new ValidationException("Tenant id malformed " + tenantId);
        return tenantId;
    }

    public static String getName(final HttpServletRequest req) throws ValidationException {
        final String name = req.getParameter("name");
        final String decoded = URLDecoder.decode(name, StandardCharsets.UTF_8);

        if (decoded == null || name.isBlank()) {
            throw new ValidationException("bad input, name needs to present as query parameter");
        }

        return decoded;
    }
}
