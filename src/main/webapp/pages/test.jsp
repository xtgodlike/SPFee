11
<%
	String url = request.getAttribute("url").toString();
	response.sendRedirect(url);
%>