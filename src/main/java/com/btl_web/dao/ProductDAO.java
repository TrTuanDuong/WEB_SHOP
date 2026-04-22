/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.btl_web.dao;

import com.btl_web.model.DbSupport;
import com.btl_web.model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletContext;

/**
 *
 * @author ADMINN
 */
public class ProductDAO {
    private volatile boolean schemaInitialized = false;
    public List<Product> all(ServletContext context) throws SQLException {
        initSchemaIfNeeded();
        String sql = "SELECT id, name, branch_id, group_name, segment, size, color, price "
                + "FROM shop_product ORDER BY id";
        List<Product> products = new ArrayList<>();
        try (Connection con = DbSupport.getConnection();
             PreparedStatement statement = con.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                products.add(new Product(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                    resultSet.getString("branch_id"),
                        resultSet.getString("group_name"),
                        resultSet.getString("segment"),
                        resultSet.getString("size"),
                        resultSet.getString("color"),
                        resultSet.getBigDecimal("price"),
                        null));
            }
            return Collections.unmodifiableList(products);
        }
    }

    public Product findById(ServletContext context, String id) throws SQLException {
        initSchemaIfNeeded();
        String sql = "SELECT id, name, branch_id, group_name, segment, size, color, price "
                + "FROM shop_product WHERE id = ?";
        try (Connection connection = DbSupport.getConnection(); 
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new Product(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                    resultSet.getString("branch_id"),
                        resultSet.getString("group_name"),
                        resultSet.getString("segment"),
                        resultSet.getString("size"),
                        resultSet.getString("color"),
                        resultSet.getBigDecimal("price"),
                        null);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm sản phẩm theo mã từ CSDL.", e);
        }
    }

    private synchronized void initSchemaIfNeeded() throws SQLException {
        if (schemaInitialized) {
            return;
        }
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Không tìm thấy PostgreSQL JDBC driver.", e);
        }
        String createTableSql = "CREATE TABLE IF NOT EXISTS shop_product ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL,"
            + "branch_id TEXT,"
                + "group_name TEXT NOT NULL,"
                + "segment TEXT NOT NULL,"
                + "size TEXT NOT NULL,"
                + "color TEXT NOT NULL,"
                + "price NUMERIC(14, 2) NOT NULL)";

        try (Connection connection = DbSupport.getConnection();
             PreparedStatement statement = connection.prepareStatement(createTableSql)) {
            statement.executeUpdate();
            schemaInitialized = true;
        }
    }
}
