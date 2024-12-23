package org.secretdb.servlet.http;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // list doesn't require an encryption key to list all names of secrets
        // Above should be done thru API Authentication instead of encryption
        resp.getWriter().write("List!");
    }
}
