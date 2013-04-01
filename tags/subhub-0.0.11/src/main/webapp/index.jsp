<%@page import="org.dictat.subhub.utils.VersionUtil"%>
<%@page isThreadSafe="true" %>
<html>
<head>
<title>SubHub</title>
<link rel="stylesheet" href="default.css">
</head>
<body>
	<div id="container">
		<h2>SubHub</h2>
		<span class="version"><%= VersionUtil.getVersion() %></span>
	</div>
</body>
</html>
