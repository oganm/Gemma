<%@ include file="/common/taglibs.jsp"%>
<head>
	<title>Details for ${expressionExperiment.shortName}</title>
	<jwr:script src='/scripts/ajax/ext/data/DwrProxy.js' />
	<jwr:script src='/scripts/app/eeDataFetch.js' />
	<jwr:script src='/scripts/app/ExpressionExperimentDetails.js' />
</head>

<h2>
	Details for ${expressionExperiment.shortName}
</h2>

<div id="loading-mask" style=""></div>
<div id="loading">
	<div class="loading-indicator">
		&nbsp;Loading...
	</div>
</div>

<input id="eeId" type="hidden" value="${eeId}" />

<div id="eedetails">
	<div id="messages"></div>
	<div id="basics" style="padding: 5px;"></div>
	<div id="annotator" style="padding: 5px;"></div>

	<div id="downloads" style="padding: 5px;">

		Download data Files:

		<a href="#" onClick="fetchData(true, ${expressionExperiment.id }, 'text', null, null)">Filtered</a> &nbsp;&nbsp;
		<a href="#" onClick="fetchData(false, ${expressionExperiment.id }, 'text', null, null)">Unfiltered</a>
		<a class="helpLink" href="?"
			onclick="showHelpTip(event, 'Tab-delimited data file for this experiment. The filtered version corresponds to what is used in most Gemma analyses, removing some probes. Unfiltered includes all probes'); return false"><img
				src="/Gemma/images/help.png" /> </a>

	</div>

	<div id="design" style="padding: 5px;">
		<h3>
			<fmt:message key="experimentalDesign.title" />
			&nbsp;
			<a title="Open the design details window" target="_blank"
				href="/Gemma/experimentalDesign/showExperimentalDesign.html?eeid=${expressionExperiment.id}"> <img
					src="/Gemma/images/magnifier.png" /> </a>
		</h3>
		<div id="eeDesignMatrix"></div>
	</div>


	<div id="bioMaterialMapping" style="padding: 5px;"></div>

	<div id="qc" style="padding: 5px;">
		<h3>
			Quality Control information
		</h3>
		<Gemma:expressionQC ee="${expressionExperiment.id}" />
	</div>

	<authz:authorize ifAnyGranted="admin">
		<div id="history" style="padding: 5px;">
		</div>
		<c:if test="${ lastArrayDesignUpdate != null}">
			<p>
				The last time an array design associated with this experiment was updated: ${lastArrayDesignUpdate.date}
			</p>
		</c:if>
	</authz:authorize>


</div>

<authz:authorize ifAnyGranted="admin">
	<%-- fixme: let 'users' edit their own datasets --%>
	<input type="hidden" name="hasAdmin" id="hasAdmin" value="true" />
</authz:authorize>
<authz:authorize ifNotGranted="admin">
	<input type="hidden" name="hasAdmin" id="hasAdmin" value="" />
</authz:authorize>


