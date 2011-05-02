
<!--  Default header for only the main gemma home page -->
<%@ include file="/common/taglibs.jsp"%>
<jwr:script src='/scripts/ajax/ext/data/DwrProxy.js' />
<c:if test="${pageContext.request.locale.language != 'en'}">
	<div id="switchLocale">
		<a href="<c:url value='/mainMenu.html?locale=en'/>"><fmt:message key="webapp.name" /> in English</a>
	</div>
</c:if>

<c:if test="${appConfig['maintenanceMode']}">
	<div style="font-weight: bold; color: #AA4444; font-size: 1.3em">
		Gemma is undergoing maintenance! Some functions may not be available.
	</div>
</c:if>

<div id="branding">

	<div id="headerLeft">
		<a href="<c:url value='/home2.html'/>"><img src="<c:url value='/images/logo/gemma-lg153x350.gif'/>" alt="gemma" />
		</a>
	</div>
</div>



	<script type="text/javascript">
	/*var menu = new Ext.Toolbar({
	renderTo: document.body,
	width:500,
	floating:false,
	items:[
	{
		xtype: 'button',
		text:'Home'
	},{
		    xtype: 'button',
            text: 'Browse',
			menu: [{text: 'Genes'},{text: 'Gene Groups'},{text: 'Experiments'},{text: 'Experiment Groups'},{text: 'Arrays'}]
	},{
		    xtype: 'button',
            text: 'Tools',
			menu: [{text: 'Load data'},{text: 'Edit Profile'}]
	},{
		    xtype: 'button',
            text: 'Manage Groups',
			menu: [{text: 'Gene Groups'},{text: 'Experiment Groups'},{text: 'User Groups'}]
	}]
	});
	*/

	</script>



<%-- Put constants into request scope --%>
<Gemma:constants scope="request" />
