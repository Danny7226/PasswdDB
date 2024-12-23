package org.secretdb.util;

import jakarta.servlet.http.HttpServletRequest;
import org.secretdb.util.model.ValidationException;

public class RequestUtil {
    public static String getTenantId(final HttpServletRequest req) throws ValidationException {
        // TODO: validate path info, cannot have special character and cannot have non-prefixing "/"
        final String tenantId = req.getPathInfo().replaceFirst("^/", ""); // remove prefixing "/"
        System.out.println("Tenant " + tenantId);

        if (tenantId.isBlank()) throw new ValidationException("Tenant id malformed " + tenantId);
        return tenantId;
    }
}
