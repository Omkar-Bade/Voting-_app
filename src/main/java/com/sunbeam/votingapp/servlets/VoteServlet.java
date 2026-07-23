/*
 * File: VoteServlet.java
 * Fixed/Why: Added CSRF token verification. Wrapped the user status update and candidate
 * vote count increment in a single JDBC transaction (using a shared connection with autoCommit
 * set to false, followed by commit or rollback) to ensure atomic database consistency.
 * Updated the session User object status to true upon successful vote so that double-voting
 * is blocked immediately within the session.
 */
package com.sunbeam.votingapp.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.sunbeam.votingapp.daos.CandidateDao;
import com.sunbeam.votingapp.daos.UserDao;
import com.sunbeam.votingapp.entities.User;
import com.sunbeam.votingapp.utils.DbUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/vote")
public class VoteServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession httpSession = req.getSession(false);
		User user = (httpSession != null) ? (User) httpSession.getAttribute("user") : null;
		if (user == null) {
			resp.sendRedirect("index.html");
			return;
		}

		// CSRF token verification
		String sessionCsrfToken = (String) httpSession.getAttribute("csrfToken");
		String requestCsrfToken = req.getParameter("csrfToken");
		if (sessionCsrfToken == null || !sessionCsrfToken.equals(requestCsrfToken)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
			return;
		}

		if (user.getStatus()) {
			resp.sendRedirect("AlreadyVoted.html");
		} else {
			int uid = user.getId();
			String cidParam = req.getParameter("cid");
			if (cidParam == null || cidParam.isEmpty()) {
				resp.sendRedirect("candidate");
				return;
			}
			int cid = Integer.parseInt(cidParam);

			// Perform updates in a single connection transaction
			try (Connection connection = DbUtil.getConnection()) {
				connection.setAutoCommit(false);
				try (UserDao userDao = new UserDao(connection);
					 CandidateDao candidateDao = new CandidateDao(connection)) {
					
					int userUpdated = userDao.updateStatus(uid);
					int candidateUpdated = candidateDao.updateVotes(cid);
					
					if (userUpdated > 0 && candidateUpdated > 0) {
						connection.commit();
						// Synchronize the session user status
						user.setStatus(true);
						resp.sendRedirect("Voted.html");
					} else {
						connection.rollback();
						resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not complete the voting registration. Database error.");
					}
				} catch (SQLException ex) {
					connection.rollback();
					throw ex;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database transaction error occurred. Please try again.");
			}
		}
	}
}
