package com.yiqingart.www;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.bae.api.factory.BaeFactory;
import com.baidu.bae.api.memcache.BaeCache;

public class Common {
	final public static String QQ_CLENT_ID = "100572767"; 
	final public static String QQ_CLENT_SECRET = "71e30056cf39ee51c7ec8fd5b44d1602";
	final public static String BD_API_KEY = "UhvHvCrsb1OIoClSdQ61OQGZ";
	final public static String BD_SECRET_KEY = "KgRUZ7M7aaGR9bpwRarGNpX9ITxVwlP2";
	final public static String BAE_API_KEY = "UhvHvCrsb1OIoClSdQ61OQGZ";
	final public static String BAE_SECRET_KEY = "KgRUZ7M7aaGR9bpwRarGNpX9ITxVwlP2";
	final private static Logger logger = Logger.getLogger("Insert");
	final public static String BAE_MONGODB_DB = "MvtEDmWnDjCqieNCBwze";
	final public static String BAE_MONGODB_HOST = "mongo.duapp.com";
	final public static String BAE_MONGODB_PORT = "8908";
	public static Connection getConnection() throws ClassNotFoundException, SQLException{
		if((System.getProperty("baejavasdk.local")!=null)&&(System.getProperty("baejavasdk.local").equalsIgnoreCase("true"))){
    		logger.log(Level.INFO, "is local");
    		return getLocalConnection();
    	}
		String host = "sqld.duapp.com";
		String port = "4050";
		String driverName = "com.mysql.jdbc.Driver";
		String dbUrl = "jdbc:mysql://";
		String serverName = host + ":" + port + "/";

		// 从平台查询应用要使用的数据库名
		String databaseName = "LEzDiKPBFwEDIcHHlfdz";
		String connName = dbUrl + serverName + databaseName;
		Connection connection = null;
		
		Class.forName(driverName);
		// 具体的数据库操作逻辑
		connection = DriverManager.getConnection(connName, BAE_API_KEY,
				BAE_SECRET_KEY);
		
		
		return connection;
	}
	private static Connection getLocalConnection() throws ClassNotFoundException, SQLException{
		
		String host = "192.168.1.2";
		String port = "3306";
		String driverName = "com.mysql.jdbc.Driver";
		String dbUrl = "jdbc:mysql://";
		String serverName = host + ":" + port + "/";

		// 从平台查询应用要使用的数据库名
		String databaseName = "OxBssklEmKZUvxanzEVi";
		String connName = dbUrl + serverName + databaseName;
		Connection connection = null;
		
		Class.forName(driverName);
		// 具体的数据库操作逻辑
		connection = DriverManager.getConnection(connName, "admin",
				"password");
		
		
		return connection;
	}
	
	public static BaeCache getBaeCache(){
		//cacheId为资源id，memcacheAddr为cache的服务地址和端口（例如，cache.duapp.com:10240）, user为ak, password为sk
		String cacheId ="vuADzfnWuiBWnrlGzSFD";
		String memcacheAddr = "cache.duapp.com:20243";
		 
		BaeCache baeCache = BaeFactory.getBaeCache(cacheId, memcacheAddr, BAE_API_KEY, BAE_SECRET_KEY);		
		return baeCache;
	}
    public static JSONArray resultSetToJson(ResultSet rs) throws SQLException,JSONException
    {
       // json数组
       JSONArray array = new JSONArray();
      
       // 获取列数
       ResultSetMetaData metaData = rs.getMetaData();
       int columnCount = metaData.getColumnCount();
      
       // 遍历ResultSet中的每条数据
        while (rs.next()) {
            JSONObject jsonObj = new JSONObject();
           
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName =metaData.getColumnLabel(i);
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            } 
            array.put(jsonObj); 
        }
      
       return array;
    }
    public static String getDay(){
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        int mYear = c.get(Calendar.YEAR); // 获取当前年份
        int mMonth = c.get(Calendar.MONTH) + 1;// 获取当前月份
        int mDay = c.get(Calendar.DAY_OF_MONTH);// 获取当前月份的日期号码
        String target = String.format("%04d-%02d-%02d", mYear, mMonth, mDay);
        return target;
    }
    public static URLConnection reload(URLConnection uc) throws Exception {

        HttpURLConnection huc = (HttpURLConnection) uc;
        
        if (huc.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP 
                || huc.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM)// 302, 301
            return reload(new URL(huc.getHeaderField("location")).openConnection());
        
        return uc;
    }
    public static String getAccessToken(HttpSession session) {

    	if((System.getProperty("baejavasdk.local")!=null)&&(System.getProperty("baejavasdk.local").equalsIgnoreCase("true"))){
    		logger.log(Level.INFO, "is local");
    		//return "21.e96287eb48c8961a19cb0c985fb1c551.2592000.1396597390.1732080263-1514280";
    	}
		if (null != session) {
			String access_token = (String) session.getAttribute("access_token");
			if (access_token != null) {
				logger.log(Level.INFO, "access_token:" + access_token);
				return access_token;
			}
		}
		String sql = "select * from netdisk";

		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// 具体的数据库操作逻辑
			connection = Common.getConnection();
			
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			rs.next();
			String access_token = rs.getString("access_token");
			if (null != session) {
				session.setAttribute("access_token", access_token);
			}
			logger.log(Level.INFO, "access_token:" + access_token);
			return access_token;
		} catch (Exception e) {
			// 异常处理逻辑
			logger.log(Level.SEVERE, "error:", e);
			return "";
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "error:", e);
				return "";
			}
		}
	}
}
