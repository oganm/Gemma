<%@ include file="include.jsp" %>
<%@ taglib prefix="Gemma" uri="/Gemma" %>
<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head><title><fmt:message key="title"/></title><meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"></head>
<body bgcolor="#CCCC99">
<%--<img src="bigicon.gif" width="88" height="97"> <br>--%>
<h1><fmt:message key="geneLoader.heading"/></h1>
<form method="post">
<form name="form1" method="post" action="">
<table width="604" height="271" border="0" cellpadding="0" cellspacing="5" bgcolor="#CCCC99">
<input type="submit" alignment="center" value="Execute">       
</table>  
</form>
<a href="<c:url value="welcome.jsp"/>">Home</a>
</body>
</html>


