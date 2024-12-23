package org.secretdb.util;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.secretdb.cryptology.CryptoUtil;
import org.secretdb.util.model.ValidationException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class RequestUtil {
    private static final Logger logger = LogManager.getLogger(CryptoUtil.class);

    private static final Pattern TENANT_ID_REGEX_PATTERN = Pattern.compile("^/[a-zA-Z0-9_-]+$");

    public static String getTenantId(final HttpServletRequest req) throws ValidationException {
        if (!TENANT_ID_REGEX_PATTERN.matcher(req.getPathInfo()).matches()) {
            throw new ValidationException("Tenant id malformed, it needs to match ^[a-zA-Z0-9_-]+$");
        }

        final String tenantId = req.getPathInfo().replaceFirst("^/", ""); // remove prefixing "/"
        logger.info("Tenant " + tenantId);

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
