/*
 * File: LogoutServlet.java
 * Fixed/Why: Checked for existing session before invalidating (avoiding creating a new session
 * just to destroy it). Aligned cookie path configuration with the path set during login
 * to ensure that the browser correctly removes the cookie.
 */
package com.sunbeam.votingapp.servlets;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Clear session safely
		HttpSession httpSession = req.getSession(false);
		if (httpSession != null) {
			httpSession.invalidate();
		}

		// Clear cookie matching path configuration
		Cookie cookie = new Cookie("username", "");
		cookie.setMaxAge(0);
		cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
		resp.addCookie(cookie);

		resp.sendRedirect("Logout.html");
	}
}
