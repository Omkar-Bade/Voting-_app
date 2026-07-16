package com.sunbeam.votingapp.servlets;

import java.io.IOException;
import java.sql.SQLException;

import com.sunbeam.votingapp.daos.CandidateDao;
import com.sunbeam.votingapp.daos.UserDao;
import com.sunbeam.votingapp.entities.User;

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
		HttpSession httpSession = req.getSession();
		User user = (User) httpSession.getAttribute("user");
		if (user.getStatus())
			resp.sendRedirect("AlreadyVoted.html");
		else {
			int uid = user.getId();
			int cid = Integer.parseInt(req.getParameter("cid"));

			try (CandidateDao candidateDao = new CandidateDao()) {
				try (UserDao userDao = new UserDao()) {
					userDao.updateStatus(uid);
					candidateDao.updateVotes(cid);
					resp.sendRedirect("Voted.html");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
