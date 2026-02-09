package com.oceanpay.servlet;

import com.oceanpay.model.Product;
import com.oceanpay.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/product/list")
public class ProductListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Product> productList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT id, name, price, stock FROM product";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setStock(rs.getInt("stock"));
                productList.add(product);
            }
            // 将数据存入请求域，转发给JSP显示
            request.setAttribute("productList", productList);
            request.getRequestDispatcher("/WEB-INF/views/product_list.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            // 实际项目中应跳转到错误页面
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }


}
