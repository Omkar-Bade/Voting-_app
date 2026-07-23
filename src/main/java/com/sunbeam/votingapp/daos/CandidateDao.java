/*
 * File: CandidateDao.java
 * Fixed/Why: Added constructor accepting an existing Connection to enable sharing
 * the connection for a transaction in VoteServlet. Ensured proper closing of 
 * connection and try-with-resources for PreparedStatement and ResultSet.
 */
package com.sunbeam.votingapp.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sunbeam.votingapp.entities.Candidate;
import com.sunbeam.votingapp.utils.DbUtil;

public class CandidateDao implements AutoCloseable {
	private Connection connection;
	private boolean closeConnection = true;

	public CandidateDao() throws SQLException {
		connection = DbUtil.getConnection();
		closeConnection = true;
	}

	public CandidateDao(Connection connection) {
		this.connection = connection;
		this.closeConnection = false;
	}

	public List<Candidate> findAll() throws SQLException {
		String sql = "SELECT * FROM candidates";
		try (PreparedStatement select = connection.prepareStatement(sql)) {
			try (ResultSet rs = select.executeQuery()) {
				List<Candidate> list = new ArrayList<Candidate>();
				while (rs.next()) {
					int id = rs.getInt("id");
					String name = rs.getString("name");
					String party = rs.getString("party");
					int votes = rs.getInt("votes");
					Candidate c = new Candidate(id, name, party, votes);
					list.add(c);
				}
				return list;
			}
		}
	}

	public int updateVotes(int id) throws SQLException {
		String sql = "UPDATE candidates SET votes = votes+1 WHERE id = ?";
		try (PreparedStatement update = connection.prepareStatement(sql)) {
			update.setInt(1, id);
			return update.executeUpdate();
		}
	}

	public int getTotalCandidatesCount() throws SQLException {
		String sql = "SELECT COUNT(*) FROM candidates";
		try (PreparedStatement select = connection.prepareStatement(sql)) {
			try (ResultSet rs = select.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		return 0;
	}

	@Override
	public void close() throws SQLException {
		if (closeConnection && connection != null) {
			connection.close();
		}
	}

}
