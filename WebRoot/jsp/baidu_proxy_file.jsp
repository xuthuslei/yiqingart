<%@ page import="java.net.*,java.util.*,java.lang.*,java.io.*"%>
<%@ page contentType="text/xml;charset=gbk"%>
<%  
    String url = "https://pcs.baidu.com/rest/2.0/pcs/file?access_token=3.15d99b9ecbc2ed2172585f77f699dba3.2592000.1386288853.1732080263-1514280&" + request.getQueryString();
    //out.println(url);
    System.out.println("url:"+url);

    if(url != null){
		// 使用GET方式向目的服务器发送请求
		URL connect = new URL(url.toString());
		URLConnection connection = connect.openConnection();
		connection.connect();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;
		while((line = reader.readLine()) != null){
		   out.println(line);
		}
		reader.close();
    }
%>
