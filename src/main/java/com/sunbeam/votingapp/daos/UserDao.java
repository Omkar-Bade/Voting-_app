package com.sunbeam.votingapp.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sunbeam.votingapp.entities.User;
import com.sunbeam.votingapp.utils.DbUtil;

public class UserDao implements AutoCloseable {
	Connection connection;

	public UserDao() throws SQLException {
		connection = DbUtil.getConnection();
	}

	public User loginUser(String email, String password) throws SQLException {
		String sql = "SELECT * FROM users WHERE email =  ? AND PASSWORD = ?";
		try (PreparedStatement select = connection.prepareStatement(sql)) {
			select.setString(1, email);
			select.setString(2, password);
			try (ResultSet rs = select.executeQuery()) {
				if (rs.next()) {
					User user = new User();
					user.setId(rs.getInt(1));
					user.setFirst_name(rs.getString(2));
					user.setLast_name(rs.getString(3));
					user.setEmail(rs.getString(4));
					user.setPassword(rs.getString(5));
					user.setDate(rs.getDate(6));
					user.setStatus(rs.getBoolean(7));
					user.setRole(rs.getString(8));
					return user;
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
		if (connection != null)
			connection.close();
	}
}
