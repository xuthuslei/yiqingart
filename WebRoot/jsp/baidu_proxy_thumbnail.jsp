<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@page contentType="image/jpeg" %><%
	String url = "https://pcs.baidu.com/rest/2.0/pcs/thumbnail?access_token=3.15d99b9ecbc2ed2172585f77f699dba3.2592000.1386288853.1732080263-1514280&" + request.getQueryString();
    
    OutputStream o = response.getOutputStream();
	URL connect = new URL(url.toString());
    URLConnection connection = connect.openConnection();
    InputStream is = connection.getInputStream();
    byte[] buf = new byte[32 * 1024]; // 32k buffer
    int nRead = 0;
    while( (nRead=is.read(buf)) != -1 ) {
        o.write(buf, 0, nRead);
    }
    o.flush();
    o.close();// *important* to ensure no more jsp output
    //return; 
%>
