package com.yiqingart.www;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
	final public static String BD_API_KEY = "5TL8PlqCuFnliMfsIlEyDXlz";
	final public static String BD_SECRET_KEY = "wSOkEjPZDGKG1GyLjCd76soBKXqYgIji";
	final public static String BAE_API_KEY = "UhvHvCrsb1OIoClSdQ61OQGZ";
	final public static String BAE_SECRET_KEY = "KgRUZ7M7aaGR9bpwRarGNpX9ITxVwlP2";
	final private static Logger logger = Logger.getLogger("Insert");
	public static Connection getConnection() throws ClassNotFoundException, SQLException{
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
    
    public static String getAccessToken(HttpSession session) {

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
