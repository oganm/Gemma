<%@ include file="/common/taglibs.jsp"%>
<p>
<content tag="heading">
<fmt:message key="menu.compassIndexer"/>
</content>
<p>
Use the Index button to index the database using Compass::Gps. This will
delete the current index and reindex the database based on the mappings and devices
defined in the Compass::Gps configuration context.
<p>
<form method="post" action="<c:url value="/indexer.html"/>">
	<spring:bind path="command.doIndex">
		<input type="hidden" name="doIndex" value="true" />
	</spring:bind>
    <input type="submit" value="Index"/>
</form>

<c:if test="${! empty indexResults}">
	<p>Indexing took: <c:out value="${indexResults.indexTime}" />ms.
</c:if>
<p>
<br>
