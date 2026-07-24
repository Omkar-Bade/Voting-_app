/*
 * File: UserDao.java
 * Purpose: Provides data access operations for the 'users' table in the database.
 * Fully refactored to use Try-with-resources, logging, input validation, proper exception
 * propagation, and support shared connections for transaction boundaries.
 */
package com.sunbeam.votingapp.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sunbeam.votingapp.entities.User;
import com.sunbeam.votingapp.utils.DbUtil;
import com.sunbeam.votingapp.utils.PasswordUtil;

/**
 * Data Access Object for handling database operations on the 'users' table.
 * 
 * Provides robust resource management, logging, parameter validation,
 * and support for transactional connection sharing.
 */
public class UserDao implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(UserDao.class.getName());

    private final Connection connection;
    private final boolean closeConnection;

    /**
     * Default constructor creating a fresh connection managed by this DAO instance.
     * The connection will be automatically closed when close() is called.
     * 
     * @throws SQLException if a database access error occurs
     */
    public UserDao() throws SQLException {
        try {
            this.connection = DbUtil.getConnection();
            this.closeConnection = true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to establish a database connection in UserDao", e);
            throw e;
        }
    }

    /**
     * Constructor using a shared connection. Typically used for transaction management.
     * The connection lifecycle is managed externally and will NOT be closed by this DAO.
     * 
     * @param connection the shared connection to use
     */
    public UserDao(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Shared database connection cannot be null");
        }
        this.connection = connection;
        this.closeConnection = false;
    }

    /**
     * Authenticates a user by checking email and verifying their hashed password.
     * 
     * @param email the user's email address
     * @param password the plaintext password to verify
     * @return the authenticated User object, or null if credentials are invalid or if input is empty
     * @throws SQLException if a database access error occurs
     */
    public User loginUser(String email, String password) throws SQLException {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            LOGGER.log(Level.WARNING, "Login attempt rejected due to empty/null credentials");
            return null;
        }

        final String sql = "SELECT id, first_name, last_name, email, password, dob, status, role FROM users WHERE email = ?";
        try (PreparedStatement select = connection.prepareStatement(sql)) {
            select.setString(1, email);
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (PasswordUtil.verify(password, storedPassword)) {
                        return mapUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during loginUser query for email: " + email, e);
            throw e;
        }
        return null;
    }

    /**
     * Updates the voting status of a user to true (status = 1) in the database.
     * 
     * @param id the user ID
     * @return the number of rows affected (should be 1 on success)
     * @throws SQLException if a database access error occurs
     */
    public int updateStatus(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid user ID provided for status update: " + id);
        }

        final String sql = "UPDATE users SET status = 1 WHERE id = ?";
        try (PreparedStatement update = connection.prepareStatement(sql)) {
            update.setInt(1, id);
            return update.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during updateStatus query for ID: " + id, e);
            throw e;
        }
    }

    /**
     * Registers a new user in the database after validating that the email is unique.
     * 
     * @param user the User details to register
     * @return the number of rows affected (should be 1 on success)
     * @throws SQLException if the email is already registered or a database access error occurs
     */
    public int registerUser(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User details to register cannot be null");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("User email cannot be null or empty during registration");
        }

        // Validate duplicate email before attempting insertion
        if (emailExists(user.getEmail())) {
            LOGGER.log(Level.WARNING, "Registration failed because email already exists: {0}", user.getEmail());
            throw new SQLException("Email address is already registered: " + user.getEmail(), "23000", 1062);
        }

        final String sql = "INSERT INTO users (first_name, last_name, email, password, dob, status, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement insert = connection.prepareStatement(sql)) {
            insert.setString(1, user.getFirst_name());
            insert.setString(2, user.getLast_name());
            insert.setString(3, user.getEmail());
            insert.setString(4, user.getPassword());
            insert.setDate(5, user.getDate());
            insert.setBoolean(6, user.getStatus());
            insert.setString(7, user.getRole());
            return insert.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during registerUser query for email: " + user.getEmail(), e);
            throw e;
        }
    }

    /**
     * Checks if a given email address already exists in the database.
     * 
     * @param email the email address to check
     * @return true if the email exists, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean emailExists(String email) throws SQLException {
        if (email == null || email.isBlank()) {
            return false;
        }

        final String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (PreparedStatement select = connection.prepareStatement(sql)) {
            select.setString(1, email);
            try (ResultSet rs = select.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during emailExists check for email: " + email, e);
            throw e;
        }
    }

    /**
     * Retrieves the total count of registered voters in the system.
     * 
     * @return the total voters count
     * @throws SQLException if a database access error occurs
     */
    public int getTotalVotersCount() throws SQLException {
        final String sql = "SELECT COUNT(*) FROM users";
        try (PreparedStatement select = connection.prepareStatement(sql)) {
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Failed to retrieve user count from result set");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during getTotalVotersCount query", e);
            throw e;
        }
    }

    /**
     * Helper method to map a single row in the ResultSet to a User object.
     * 
     * @param rs the active ResultSet from which to extract data
     * @return a fully populated User object
     * @throws SQLException if a database access error occurs
     */
    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFirst_name(rs.getString("first_name"));
        user.setLast_name(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setDate(rs.getDate("dob"));
        user.setStatus(rs.getBoolean("status"));
        user.setRole(rs.getString("role"));
        return user;
    }

    /**
     * AutoCloseable interface implementation. Closes the connection only if it was
     * created internally by this instance.
     * 
     * @throws SQLException if a database access error occurs during close
     */
    @Override
    public void close() throws SQLException {
        if (closeConnection && connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to close database connection in UserDao", e);
                throw e;
            }
        }
    }
}
