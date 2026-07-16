package com.sunbeam.votingapp.servlets;

import java.io.IOException;
import java.sql.SQLException;

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
		User user = null;
		try (UserDao userDao = new UserDao()) {
			user = userDao.loginUser(email, password);
			if (user == null)
				resp.sendRedirect("InvalidLogin.html"); // to naviagte from servlet to html page or other servlet
			else {
				Cookie cookie = new Cookie("username", user.getFirst_name());
				cookie.setMaxAge(3600);
				resp.addCookie(cookie);

				HttpSession httpSession = req.getSession();
				httpSession.setAttribute("user", user);

				resp.sendRedirect("candidate");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
