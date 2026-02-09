<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>跨境商品列表 - v0.1</title>
    <style>
        table { border-collapse: collapse; width: 80%; margin: 20px auto; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: center; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
<h1 style="text-align: center;">跨境支付电商系统 v0.1</h1>
<h2 style="text-align: center;">商品列表</h2>

<table>
    <tr>
        <th>ID</th>
        <th>商品名称</th>
        <th>价格</th>
        <th>库存</th>
    </tr>
    <!-- 使用JSTL循环遍历productList -->
    <c:forEach var="product" items="${productList}">
        <tr>
            <td>${product.id}</td>
            <td>${product.name}</td>
            <td>¥${product.price}</td>
            <td>${product.stock}</td>
        </tr>
    </c:forEach>
</table>
<p style="text-align: center;">
    <a href="${pageContext.request.contextPath}/">返回首页</a>
</p>
</body>
</html>
