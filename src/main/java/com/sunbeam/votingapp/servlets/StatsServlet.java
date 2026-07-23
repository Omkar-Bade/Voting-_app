/*
 * File: StatsServlet.java
 * Purpose: Exposes total candidates count and total registered voters count
 * from the database in JSON format. Used by the login page stats panel.
 */
package com.sunbeam.votingapp.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import com.sunbeam.votingapp.daos.CandidateDao;
import com.sunbeam.votingapp.daos.UserDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/stats")
public class StatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int totalCandidates = 0;
        int totalVoters = 0;

        try (CandidateDao candidateDao = new CandidateDao()) {
            totalCandidates = candidateDao.getTotalCandidatesCount();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (UserDao userDao = new UserDao()) {
            totalVoters = userDao.getTotalVotersCount();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            out.print(String.format("{\"totalCandidates\":%d,\"totalVoters\":%d,\"status\":\"Active\"}", 
                    totalCandidates, totalVoters));
            out.flush();
        }
    }
}
