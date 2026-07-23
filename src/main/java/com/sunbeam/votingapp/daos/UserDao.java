/*
 * File: UserDao.java
 * Fixed/Why: Added constructor accepting an existing Connection to enable sharing
 * the connection for a transaction in VoteServlet. Modified loginUser to load
 * user by email first and then verify against hashed password using PasswordUtil.
 * Ensured proper closing of PreparedStatement/ResultSet using try-with-resources.
 */
package com.sunbeam.votingapp.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sunbeam.votingapp.entities.User;
import com.sunbeam.votingapp.utils.DbUtil;
import com.sunbeam.votingapp.utils.PasswordUtil;

public class UserDao implements AutoCloseable {
	private Connection connection;
	private boolean closeConnection = true;

	public UserDao() throws SQLException {
		connection = DbUtil.getConnection();
		closeConnection = true;
	}

	public UserDao(Connection connection) {
		this.connection = connection;
		this.closeConnection = false;
	}

	public User loginUser(String email, String password) throws SQLException {
		if (email == null || password == null) {
			return null;
		}
		String sql = "SELECT * FROM users WHERE email = ?";
		try (PreparedStatement select = connection.prepareStatement(sql)) {
			select.setString(1, email);
			try (ResultSet rs = select.executeQuery()) {
				if (rs.next()) {
					String storedPassword = rs.getString(5);
					if (PasswordUtil.verify(password, storedPassword)) {
						User user = new User();
						user.setId(rs.getInt(1));
						user.setFirst_name(rs.getString(2));
						user.setLast_name(rs.getString(3));
						user.setEmail(rs.getString(4));
						user.setPassword(storedPassword);
						user.setDate(rs.getDate(6));
						user.setStatus(rs.getBoolean(7));
						user.setRole(rs.getString(8));
						return user;
					}
				}
			}
		}
		return null;
	}

	public int updateStatus(int id) throws SQLException {
		String sql = "UPDATE users SET status = 1 WHERE id = ?";
		try (PreparedStatement update = connection.prepareStatement(sql)) {
			update.setInt(1, id);
			return update.executeUpdate();
		}
	}

	@Override
	public void close() throws SQLException {
		if (closeConnection && connection != null) {
			connection.close();
		}
	}
}

