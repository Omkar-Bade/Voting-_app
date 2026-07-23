/*
 * File: CandidateServlet.java
 * Fixed/Why: Enforced server-side session check (security validation rather than trusting
 * a raw cookie username). Retrieved and injected the CSRF token into the form. Overhauled
 * the HTML output to render a modern, responsive interface styled by a shared stylesheet.
 */
package com.sunbeam.votingapp.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import com.sunbeam.votingapp.daos.CandidateDao;
import com.sunbeam.votingapp.entities.Candidate;
import com.sunbeam.votingapp.entities.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/candidate")
public class CandidateServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Secure session validation
		HttpSession session = req.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			resp.sendRedirect("index.html");
			return;
		}

		User user = (User) session.getAttribute("user");
		String username = user.getFirst_name() + " " + user.getLast_name();
		String csrfToken = (String) session.getAttribute("csrfToken");

		List<Candidate> candidates = null;
		try (CandidateDao candidateDao = new CandidateDao()) {
			candidates = candidateDao.findAll();
		} catch (SQLException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load candidate list.");
			return;
		}

		resp.setContentType("text/html; charset=UTF-8");
		PrintWriter wr = resp.getWriter();
		
		wr.println("<!DOCTYPE html>");
		wr.println("<html lang='en'>");
		wr.println("<head>");
		wr.println("    <meta charset='UTF-8'>");
		wr.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
		wr.println("    <title>E-Voting Portal</title>");
		wr.println("    <link rel='stylesheet' href='styles.css'>");
		wr.println("</head>");
		wr.println("<body>");
		wr.println("    <div class='container'>");
		wr.println("        <header class='app-header'>");
		wr.println("            <div class='user-welcome'>");
		wr.println("                <span class='avatar'>" + user.getFirst_name().substring(0, 1) + "</span>");
		wr.println("                <div>");
		wr.println("                    <p class='welcome-text'>Logged in as</p>");
		wr.println("                    <h3 class='user-name'>" + username + "</h3>");
		wr.println("                </div>");
		wr.println("            </div>");
		wr.println("            <a href='logout' class='btn btn-outline'>Logout</a>");
		wr.println("        </header>");
		wr.println("        <main class='voting-card'>");
		wr.println("            <div class='card-info'>");
		wr.println("                <h2>Cast Your Vote</h2>");
		wr.println("                <p>Select one candidate from the list below and submit. You can only vote once.</p>");
		wr.println("            </div>");
		wr.println("            <form action='vote' method='post'>");
		wr.println("                <input type='hidden' name='csrfToken' value='" + csrfToken + "'>");
		wr.println("                <div class='candidate-group'>");
		
		if (candidates != null && !candidates.isEmpty()) {
			for (Candidate c : candidates) {
				wr.println("                    <label class='candidate-item'>");
				wr.println("                        <input type='radio' name='cid' value='" + c.getId() + "' required>");
				wr.println("                        <div class='item-details'>");
				wr.println("                            <div class='candidate-meta'>");
				wr.println("                                <span class='candidate-full-name'>" + c.getName() + "</span>");
				wr.println("                                <span class='candidate-party-name'>" + c.getParty().toUpperCase() + "</span>");
				wr.println("                            </div>");
				wr.println("                            <div class='check-indicator'></div>");
				wr.println("                        </div>");
				wr.println("                    </label>");
			}
		} else {
			wr.println("                    <p class='no-candidates'>No candidates registered in the system.</p>");
		}
		
		wr.println("                </div>");
		wr.println("                <div class='card-footer'>");
		wr.println("                    <button type='submit' class='btn btn-primary btn-block'>Submit Secure Vote</button>");
		wr.println("                </div>");
		wr.println("            </form>");
		wr.println("        </main>");
		wr.println("    </div>");
		wr.println("</body>");
		wr.println("</html>");
	}
}
