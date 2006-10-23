<%@ include file="/common/taglibs.jsp"%>
<title><fmt:message key="mainMenu.title" />
</title>
<%--<content tag="heading">
<fmt:message key="mainMenu.heading" />
</content>--%>


<p>
	<fmt:message key="mainMenu.message" />
</p>

<div class="separator"></div>

<ul class="glassList">


	<li>
		<a href="<c:url value="/searcher.html"/>"> <fmt:message key="menu.compassSearcher" /> </a>
	</li>
	<li>
		<a href="<c:url value="/arrays/showAllArrayDesigns.html"/>"> <fmt:message key="menu.ArrayDesignSearch" /> </a>
	</li>
	<li>
		<a href="<c:url value="/expressionExperiment/showAllExpressionExperiments.html"/>"> <fmt:message
				key="menu.ExpressionExperimentSearch" /> </a>
	</li>
	<li>
		<a href="<c:url value="/searchCoexpression.html"/>"> <fmt:message key="menu.Coexpression" /> </a>
	</li>
	<li>
		<a href="<c:url value="/geneFinder.html"/>"> <fmt:message key="menu.GeneFinder" /> </a>
	</li>
	<li>
		<a href="<c:url value="/about.html"/>">About this site</a>
	</li>
	<authz:authorize ifAnyGranted="user,admin">
		<li>
			<a href="<c:url value="/editProfile.html"/>"> <fmt:message key="menu.user" /> </a>
		</li>
	</authz:authorize>

</ul>
<authz:authorize ifAnyGranted="admin">
	<hr />
	<h2>
		Administrative functions
	</h2>
	<ul class="glassList">
		<li>
			<a href="<c:url value="/indexer.html"/>"> <fmt:message key="menu.compassIndexer" /> </a>
		</li>
		<li>
			<a href="<c:url value="/loadExpressionExperiment.html"/>"> <fmt:message key="menu.loadExpressionExperiment" /> </a>
		</li>
		<li>
			<a href="<c:url value="loadSimpleExpressionExperiment.html"/>"> Load expression data from a tabbed file</a>
		</li>
		<li>
			<a href="<c:url value="/addTestData.html"/>">Add test data </a>
		</li>
		<li>
			<a href="<c:url value="/uploadFile.html"/>"> <fmt:message key="menu.selectFile" /> </a>
		</li>
		<li>
			<a href="<c:url value="/arrayDesign/associateSequences.html"/>"> <fmt:message key="menu.arrayDesignSequenceAdd" />
			</a>
		</li>
		<li>
			<a href="<c:url value="/genome/goldenPathSequenceLoad.html"/>"> <fmt:message key="menu.goldenPathSequenceLoad" />
			</a>
		</li>
	</ul>

	<h2>
		Inactive, deprecated, or not ready for prime time
	</h2>
	<ul class="glassList">
		<li>
			<a href="<c:url value="/candidateGeneList.html"/>"> <fmt:message key="menu.CandidateGeneList" /> </a>
		</li>
		<li>
			<a href="<c:url value="/bibRefSearch.html"/>"> <fmt:message key="menu.flow.PubMedSearch" /> </a>
		</li>
	</ul>

</authz:authorize>

