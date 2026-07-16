package com.sunbeam.votingapp.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import com.sunbeam.votingapp.daos.CandidateDao;
import com.sunbeam.votingapp.entities.Candidate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/candidate")
public class CandidateServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String username = "";
		Cookie[] cookies = req.getCookies();
		for (Cookie c : cookies) {
			if (c.getName().equals("username")) {
				username = c.getValue();
				break;
			}
		}

		List<Candidate> candidates = null;
		try (CandidateDao candidateDao = new CandidateDao()) {
			candidates = candidateDao.findAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		PrintWriter wr = resp.getWriter();
		wr.println("<html>");

		wr.println("<head>");

		wr.println("<title>");
		wr.println("Candidates");
		wr.println("</title>");

		wr.println("</head>");

		wr.println("<body>");

		wr.println("<h1>");
		wr.println("Welcome - " + username);
		wr.println("</h1>");

		wr.println("<h1>");
		wr.println("Candidates - ");
		wr.println("</h1>");

		wr.println("<form action='vote' method='post'>");
		for (Candidate c : candidates) {
			wr.println("<div>");
			wr.println("<input type='radio' name='cid' value=" + c.getId() + ">" + c.getName() + " - " + c.getParty());
			wr.println("</div>");
		}

		wr.println("<div>");
		wr.println("<br>");
		wr.println("<input type='submit' value = 'vote'>");
		wr.println("</div>");

		wr.println("</form>");

		wr.println("</body>");

		wr.println("</html>");
	}
}
