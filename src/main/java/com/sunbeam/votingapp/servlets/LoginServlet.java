/*
 * File: LoginServlet.java
 * Fixed/Why: Added input validation for email and password. Protected against session fixation
 * by invalidating the old session and generating a new one on login. Initialized a secure,
 * random CSRF token stored in the HTTP session. Secured the username cookie with HttpOnly
 * to prevent XSS-based reading. Handled SQLException gracefully.
 */
package com.sunbeam.votingapp.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import com.sunbeam.votingapp.daos.UserDao;
import com.sunbeam.votingapp.entities.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		
		// Input validation
		if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
			resp.sendRedirect("InvalidLogin.html");
			return;
		}

		User user = null;
		try (UserDao userDao = new UserDao()) {
			user = userDao.loginUser(email, password);
			if (user == null) {
				resp.sendRedirect("InvalidLogin.html");
			} else {
				// Prevent Session Fixation: recreate session
				HttpSession oldSession = req.getSession(false);
				if (oldSession != null) {
					oldSession.invalidate();
				}
				HttpSession httpSession = req.getSession(true);
				httpSession.setAttribute("user", user);

				// Generate CSRF Token for form submissions
				String csrfToken = UUID.randomUUID().toString();
				httpSession.setAttribute("csrfToken", csrfToken);

				// Secure cookie configuration
				Cookie cookie = new Cookie("username", user.getFirst_name());
				cookie.setMaxAge(3600);
				cookie.setHttpOnly(true);
				cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
				resp.addCookie(cookie);

				resp.sendRedirect("candidate");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database connection error occurred. Please try again later.");
		}
	}
}
