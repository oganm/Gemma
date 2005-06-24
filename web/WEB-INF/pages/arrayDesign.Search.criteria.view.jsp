<%@ include file="/common/taglibs.jsp"%>

<%--<jsp:useBean id="query" scope="request" class="edu.columbia.gemma.controller.domain.BibliographicReferenceQuery"/>--%>
<jsp:useBean id="query" scope="request" class="edu.columbia.gemma.expression.arrayDesign.ArrayDesignImpl"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<SCRIPT LANGUAGE = "JavaScript">
	function selectButton(target){
		if(target == 0){
			document.searchForm._eventId.value="submitPubMed"
			//document.searchForm._flowId.value="pubMed.Search" set if using generic app controller
			if (document.searchForm.pubMedId.value==""){
				alert("Enter a valid PubMed ID.");
				return false;
			}
			else{
 				document.searchForm.submit();
			}
		}
		if(target == 1){
			document.searchForm._eventId.value="getArrayDesigns"
			//document.searchForm._flowId.value="bibRef.GetAll" set if using generic app controller
 			document.searchForm.submit();
		}
	}
	</SCRIPT> 

<HEAD>
</HEAD>
	<BODY>
		<DIV align="left">
			<FORM name="searchForm" action="arrayDesignSearch.htm">
				<INPUT type="hidden" name="_flowExecutionId" value="<%=request.getAttribute("flowExecutionId") %>">
				<INPUT type="hidden" name="_currentStateId" value="criteria.view">
				<INPUT type="hidden" name="_eventId" value="">
				<INPUT type="hidden" name="_flowId" value="">
<%--				
				<TABLE width="100%">
					<TR>
						<TD>
							<DIV align="left"><b>Search ArrayDesigns</b></DIV>
						</TD>
					</TR>
					<TR>
						<TD COLSPAN="2"><HR></TD>
					</TR>
					<spring:hasBindErrors name="query">
						<TR>
							<TD COLSPAN="2"><FONT color="red">Please provide valid query criteria!</FONT></TD>
						</TR>
					</spring:hasBindErrors>
					<TR>
						<TD>PubMed ID</TD>
						<TD><INPUT type="text" name="pubMedId" value=""></TD>
					</TR>
					<TR>
						<TD COLSPAN="2"><HR></TD>
					</TR>
					<TR>
  					    <TD COLSPAN="2">
						    <DIV align="right">
							    <INPUT type="button" onclick="javascript:selectButton(0)" value="Search">
						    </DIV>
						</TD>
					</TR>
--%>

					<TR>
						<TD>
							<DIV align="left"><b>View All Gemma ArrayDesigns</b></DIV>
						</TD>
					</TR>
					<TR>
						<TD COLSPAN="2"><HR></TD>
					</TR>
					<TR>
  					    <TD COLSPAN="2">
						    <DIV align="left">
							    <INPUT type="button" onclick="javascript:selectButton(1)" value="Select">
						    </DIV>
						</TD>
					</TR>
					<TR>
						<TD COLSPAN="2"><HR></TD>
					</TR>


				</TABLE>
		</DIV>	
	</BODY>
</HTML>
