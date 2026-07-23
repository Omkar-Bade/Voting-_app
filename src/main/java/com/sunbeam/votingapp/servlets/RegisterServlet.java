/*
 * File: RegisterServlet.java
 * Purpose: Handles user registration (GET for rendering Register.html template,
 * POST for validating input, verifying unique email constraints, hashing passwords,
 * and saving new voter records in a race-condition-safe manner).
 */
package com.sunbeam.votingapp.servlets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Pattern;

import com.sunbeam.votingapp.daos.UserDao;
import com.sunbeam.votingapp.entities.User;
import com.sunbeam.votingapp.utils.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        renderForm(req, resp, "", "", "", "", "");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String dob = req.getParameter("dob");

        // CSRF Token validation
        HttpSession session = req.getSession(false);
        String sessionCsrf = (session != null) ? (String) session.getAttribute("csrfToken") : null;
        String requestCsrf = req.getParameter("csrfToken");

        if (sessionCsrf == null || !sessionCsrf.equals(requestCsrf)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        // Server-side validation
        if (firstName == null || firstName.trim().isEmpty() ||
            lastName == null || lastName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty() ||
            dob == null || dob.trim().isEmpty()) {
            
            renderForm(req, resp, "All fields are required.", firstName, lastName, email, dob);
            return;
        }

        // Email validation
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            renderForm(req, resp, "Please enter a valid email address.", firstName, lastName, email, dob);
            return;
        }

        // Password complexity check (minimum 6 characters)
        if (password.length() < 6) {
            renderForm(req, resp, "Password must be at least 6 characters long.", firstName, lastName, email, dob);
            return;
        }

        // Password matching check
        if (!password.equals(confirmPassword)) {
            renderForm(req, resp, "Passwords do not match.", firstName, lastName, email, dob);
            return;
        }

        // Parse Date of Birth
        Date sqlDate;
        try {
            sqlDate = Date.valueOf(dob);
        } catch (IllegalArgumentException e) {
            renderForm(req, resp, "Invalid date of birth format.", firstName, lastName, email, dob);
            return;
        }

        try (UserDao userDao = new UserDao()) {
            // Check for pre-existing email
            if (userDao.emailExists(email)) {
                renderForm(req, resp, "Email address is already registered.", firstName, lastName, email, dob);
                return;
            }

            // Create Voter Entity
            User user = new User();
            user.setFirst_name(firstName);
            user.setLast_name(lastName);
            user.setEmail(email);
            // Hash password using existing PasswordUtil
            user.setPassword(PasswordUtil.hash(password));
            user.setDate(sqlDate);
            user.setStatus(false); // status = 0 (has not voted yet)
            user.setRole("voter");  // force role to voter, never let input set role admin

            userDao.registerUser(user);
            
            // Redirect to signin with a success toast parameter
            resp.sendRedirect("index.html?registered=true");

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle duplicate entry constraint in race condition gracefully
            if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState())) {
                renderForm(req, resp, "Email address is already registered.", firstName, lastName, email, dob);
            } else {
                renderForm(req, resp, "Database error: " + e.getMessage(), firstName, lastName, email, dob);
            }
        }
    }

    private void renderForm(HttpServletRequest req, HttpServletResponse resp, String error, 
                            String firstName, String lastName, String email, String dob) throws IOException {
        HttpSession session = req.getSession(true);
        String csrfToken = (String) session.getAttribute("csrfToken");
        if (csrfToken == null) {
            csrfToken = UUID.randomUUID().toString();
            session.setAttribute("csrfToken", csrfToken);
        }

        String template = readTemplate(req, "/Register.html");
        
        // Escape placeholder targets literals safely using replace
        template = template.replace("${csrfToken}", csrfToken != null ? csrfToken : "");
        template = template.replace("${error}", error != null ? error : "");
        template = template.replace("${firstName}", firstName != null ? firstName : "");
        template = template.replace("${lastName}", lastName != null ? lastName : "");
        template = template.replace("${email}", email != null ? email : "");
        template = template.replace("${dob}", dob != null ? dob : "");

        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(template);
            out.flush();
        }
    }

    private String readTemplate(HttpServletRequest req, String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (java.io.InputStream is = req.getServletContext().getResourceAsStream(path)) {
            if (is == null) {
                throw new FileNotFoundException("Template file not found at webapp path: " + path);
            }
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
