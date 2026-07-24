/*
 * File: CandidateDao.java
 * Purpose: Provides data access operations for the 'candidates' table in the database.
 * Fully refactored to use Try-with-resources, logging, input validation, proper exception
 * propagation, and support shared connections for transaction boundaries.
 */
package com.sunbeam.votingapp.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sunbeam.votingapp.entities.Candidate;
import com.sunbeam.votingapp.utils.DbUtil;

/**
 * Data Access Object for handling database operations on the 'candidates' table.
 * 
 * Provides robust resource management, logging, parameter validation,
 * and support for transactional connection sharing.
 */
public class CandidateDao implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(CandidateDao.class.getName());

    private final Connection connection;
    private final boolean closeConnection;

    /**
     * Default constructor creating a fresh connection managed by this DAO instance.
     * The connection will be automatically closed when close() is called.
     * 
     * @throws SQLException if a database access error occurs
     */
    public CandidateDao() throws SQLException {
        try {
            this.connection = DbUtil.getConnection();
            this.closeConnection = true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to establish a database connection in CandidateDao", e);
            throw e;
        }
    }

    /**
     * Constructor using a shared connection. Typically used for transaction management.
     * The connection lifecycle is managed externally and will NOT be closed by this DAO.
     * 
     * @param connection the shared connection to use
     */
    public CandidateDao(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Shared database connection cannot be null");
        }
        this.connection = connection;
        this.closeConnection = false;
    }

    /**
     * Retrieves all candidates from the database.
     * 
     * @return a list of all candidates
     * @throws SQLException if a database access error occurs
     */
    public List<Candidate> findAll() throws SQLException {
        final String sql = "SELECT id, name, party, votes FROM candidates";
        try (PreparedStatement select = connection.prepareStatement(sql)) {
            try (ResultSet rs = select.executeQuery()) {
                List<Candidate> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapCandidate(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during findAll candidates query", e);
            throw e;
        }
    }

    /**
     * Increments the votes of a specific candidate by 1.
     * 
     * @param id the candidate ID
     * @return the number of rows affected (should be 1 on success)
     * @throws SQLException if a database access error occurs
     */
    public int updateVotes(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid candidate ID provided for vote update: " + id);
        }

        final String sql = "UPDATE candidates SET votes = votes + 1 WHERE id = ?";
        try (PreparedStatement update = connection.prepareStatement(sql)) {
            update.setInt(1, id);
            return update.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during updateVotes query for candidate ID: " + id, e);
            throw e;
        }
    }

    /**
     * Retrieves the total count of candidates in the database.
     * 
     * @return the total candidates count
     * @throws SQLException if a database access error occurs
     */
    public int getTotalCandidatesCount() throws SQLException {
        final String sql = "SELECT COUNT(*) FROM candidates";
        try (PreparedStatement select = connection.prepareStatement(sql)) {
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Failed to retrieve candidate count from result set");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during getTotalCandidatesCount query", e);
            throw e;
        }
    }

    /**
     * Helper method to map a single row in the ResultSet to a Candidate object.
     * 
     * @param rs the active ResultSet from which to extract data
     * @return a fully populated Candidate object
     * @throws SQLException if a database access error occurs
     */
    private Candidate mapCandidate(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String party = rs.getString("party");
        int votes = rs.getInt("votes");
        return new Candidate(id, name, party, votes);
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
                LOGGER.log(Level.SEVERE, "Failed to close database connection in CandidateDao", e);
                throw e;
            }
        }
    }
}
