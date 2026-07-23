package com.sunbeam.votingapp.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbUtil {

    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    // Local development defaults
    private static final String LOCAL_DB_URL =
            "jdbc:mysql://localhost:3306/classwork_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String LOCAL_DB_USER = "root";
    private static final String LOCAL_DB_PASSWORD = "Vaishnavibade@om_1624";

    static {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to load MySQL JDBC Driver.", e);
        }
    }

    public static Connection getConnection() throws SQLException {

        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = LOCAL_DB_URL;
            dbUser = LOCAL_DB_USER;
            dbPassword = LOCAL_DB_PASSWORD;
        }

        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}