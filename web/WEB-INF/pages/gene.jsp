<%@ include file="include.jsp" %>

<html>
<head><title>gene</title></head>
<body>
<h1><fmt:message key="heading"/></h1>
<p><fmt:message key="greeting"/> <c:out value="${model.now}"/>
</p> 
<h3>Genes Loaded</h3>
<%--
<c:forEach items="${model.genes}" var="gen">
  <c:out value="${gen.id}"/> <c:out value="${gen.name}"/> <i><c:out value="${gen.description}"/></i><br><br>
</c:forEach>
--%>
<br>
<a href="<c:url value="welcome.jsp"/>">Home</a>
<br>
</body>
</html>