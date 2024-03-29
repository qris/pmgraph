<!-- Jsp to show the legend corresponding to graph. This reads the database to get the data   -->

<%@ page import="java.awt.Color"%>
<%@ page import="java.security.MessageDigest"%>
<%@ page import="java.sql.Connection"%>
<%@ page import="java.sql.PreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.Timestamp"%>
<%@ page import="org.aptivate.bmotools.pmgraph.*"%>
<%@ page import="java.util.List"%>
<%@ page import="java.net.InetAddress"%>
<%@ page import="java.util.Hashtable"%>
<%@ page import="java.util.Enumeration"%>
<%@ page import="java.net.UnknownHostException"%>
<%@ page import="org.aptivate.bmotools.pmgraph.View"%>
<%@ page import="org.aptivate.bmotools.pmgraph.ConfigurationException"%>
<%@ page pageEncoding="utf-8" language="java"
	contentType="text/html; charset=utf-8"%>
<%
	Hashtable<Integer,List<DataPoint>> resultsHash = new Hashtable<Integer,List<DataPoint>>();
	
	ArrayList<ArrayList<LegendElement>> headers = new ArrayList<ArrayList<LegendElement>>();
	ArrayList<ArrayList<LegendElement>> rows = new ArrayList<ArrayList<LegendElement>>();
    
    //sortBy = downloaded | uploaded |total_byties
	String sortBy = request.getParameter("sortBy");
	//order: DESC | ASC
	String order = request.getParameter("order");
	long now = new java.util.Date().getTime();
	long start = now - 180 * 60000;
	long end = now;
    
	 //methods to get new URL
    UrlBuilder pageUrl = new UrlBuilder();

    // Input Validation
    String errorMsg = null, configError= null;   

    // Validate Parameters
	try
	{ 
		pageUrl.setParameters(request);
		start = pageUrl.getParams().getStartTime();
		end = pageUrl.getParams().getEndTime();
	}	
	catch (PageUrlException e)
	{
		errorMsg = e.getLocalizedMessage();
	}	
    //populate the results from the database
	try {
		LegendData legendData = new LegendData();
		resultsHash = legendData.getLegendData(sortBy, order, pageUrl.getParams(), Configuration.needsLongGraph(start, end));
		
	} catch (	ConfigurationException e)
	{
		configError = e.getLocalizedMessage();
		if (e.getCause() != null)
			configError += "<p>" +e.getCause().getLocalizedMessage() + "</p>";
	}	
%>
<%@page import="java.util.ArrayList"%>
<table id="legend_tbl">
	<%
	// We pass the results from the database to View to format and populate the table 
	//LegendTable table = View.getLegendTable(pageUrl, results);
	boolean buildHeaders = true;
	for (Enumeration e = resultsHash.keys(); e.hasMoreElements();) 
	{
		int key = (Integer) e.nextElement();
		List<DataPoint> results = resultsHash.get(key);
		LegendTable table = View.getLegendTable(pageUrl, results, buildHeaders);
		headers = table.getHeaders(); 
		rows = table.getRows();
		if (buildHeaders)
		{
			for (ArrayList<LegendElement> row: headers)
			{
			%> 
				<tr class="legend_th">
				<%
				for (LegendElement column: row)
				{
					%><th<%			
					if (column.isDoubleColSpan())
					{%> 
						colspan="2"
					<%}
					if (column.isDoubleRowSpan()) 
					{%> 
						rowspan="2" 
					<%}	%>>
					<%
					//The only elements with links in the legend are the ones you can click to sort by desc/asc order 
					if (column.getLink()!=null)
					{%> 	
						<a href="<%=column.getLink()%>" title="Click to sort by descending/ascending order" name="<%=column.getName()%>"><%=column.getValue()%></a>
					<%} else
					{
						%> <%=column.getValue()%> <%
		 			} %>	
		 		</th>
				<%}	%>
			</tr>
			<%}
		} else {%>
		   <tr> 
		   <th colspan="7" rowspan="1"> <p style="margin-top : 1em;"> </p></th>
		   </tr>
		<% }
		buildHeaders = false;
		int i = 0;
		for (ArrayList<LegendElement> row: rows)
		{%>
			<tr class="row<%=i % 2%>">
			<td style="background-color: <%=row.get(0).getValue()%>; width: 5px;"></td>
			<%
			int j = 0; 
			for (LegendElement column: row)
			{
				if(column != row.get(0))
				{ %>
					<td <%if (j>2)
					{%> 
						class="numval"
					<%}
					if (column.isDoubleColSpan())
					{%> 
						colspan="2"
					<%}
					if (column.isDoubleRowSpan()) 
					{%> 
						rowspan="2" 
					<%} %>>
					<%if (column.getLink()!=null)
					{%> 
						<a href="<%=column.getLink()%>" name="<%=column.getName() %>"><%=column.getValue()%></a>
					<% } else
					{
					%> <%=column.getValue()%> <%
					} %>
					</td>
				<%
				}
				j++;
			}
			%>
			</tr>
			<%
			i++;
		}
	}
	%>
</table>

<%
//Only see link to port list if in remote port view
if (pageUrl.getParams().getView() == View.REMOTE_PORT) {
%>
<a href="javascript:window.open('port_assignment.html');void(0);">Well
known ports list</a>
<%
}
%>
<%
if (configError != null) {
%>
<div class="error_panel"><%=configError%></div>
<%
}
%>