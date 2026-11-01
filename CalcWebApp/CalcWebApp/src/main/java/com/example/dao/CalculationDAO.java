package com.example.dao;

import com.example.model.Calculation;
import com.example.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CalculationDAO {

    /** Saves one calculation (successful or with an error message). */
    public void save(Calculation c) throws SQLException {
        String sql = "INSERT INTO calculation (operand1, operand2, operation, result, error_msg) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, c.getOperand1());
            if (c.getOperand2() == null) ps.setNull(2, Types.DOUBLE); else ps.setDouble(2, c.getOperand2());
            ps.setString(3, c.getOperation());
            if (c.getResult() == null) ps.setNull(4, Types.DOUBLE); else ps.setDouble(4, c.getResult());
            ps.setString(5, c.getErrorMsg());
            ps.executeUpdate();
        }
    }

    /** Most recent calculations first. */
    public List<Calculation> findRecent(int limit) throws SQLException {
        List<Calculation> list = new ArrayList<>();
        String sql = "SELECT id, operand1, operand2, operation, result, error_msg, created_at " +
                     "FROM calculation ORDER BY id DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Calculation c = new Calculation();
                    c.setId(rs.getLong("id"));
                    c.setOperand1(rs.getDouble("operand1"));
                    double o2 = rs.getDouble("operand2");
                    c.setOperand2(rs.wasNull() ? null : o2);
                    c.setOperation(rs.getString("operation"));
                    double res = rs.getDouble("result");
                    c.setResult(rs.wasNull() ? null : res);
                    c.setErrorMsg(rs.getString("error_msg"));
                    c.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(c);
                }
            }
        }
        return list;
    }

    /** Deletes every saved calculation. */
    public void clearAll() throws SQLException {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("TRUNCATE TABLE calculation");
        }
    }
}
