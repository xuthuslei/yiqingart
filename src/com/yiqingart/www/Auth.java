package com.yiqingart.www;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

public class Auth extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8826861146379330737L;
	private Logger logger = Logger.getLogger("Auth");

	public enum Method {
		BAIDU_LOGIN, BAIDU_OAUTH, BAIDU_REFRESH,QQ_LOGIN, QQ_REDIRECT, USER_INFO, QQ_LOGOUT, BAIDU_SOCIAL_CALLBACK, NOVALUE;
		public static Method toMethod(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return NOVALUE;
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String requestURL = req.getRequestURI();
		String[] inputParams = requestURL.toString().split("/");
		String method = inputParams[2];
		//HttpSession session = req.getSession(true);// 如果没有该session，则自动创建一个新的

		//Integer cacheSecond = 0;
		//resp.setContentType("application/json");
		// resp.setHeader("Cache-Control", "nocache");

		//resp.setCharacterEncoding("utf-8");
		Method m = Method.toMethod(method.toUpperCase());
		switch (m) {
		case BAIDU_LOGIN:
			baiduLogin( req, resp);
			break;
		case BAIDU_OAUTH:
			baiduOauth( req, resp);
			break;
		case BAIDU_REFRESH:
			baiduRefresh( req, resp);
			break;
		case QQ_LOGIN:
			qqLogin( req, resp);
			break;
		case QQ_REDIRECT:
			qqRedirect( req, resp);
			break; 
		case USER_INFO:
			userInfo( req, resp);
			break;
		case BAIDU_SOCIAL_CALLBACK:
			baiduSocialCallback( req, resp);
			break;
		default:
			PrintWriter pw = resp.getWriter();
			pw.write("wrong method:"+m);
			pw.flush();
			pw.close();
			break;
		}
		
		
	}

	private void userInfo(HttpServletRequest req, HttpServletResponse resp) {
		try {
			HttpSession session = req.getSession(true);
			JSONObject jsonUserInfo = null;
			if (null != session) {
				String openid = (String) session.getAttribute("openid");
				if ((openid == null)||openid.equals("")) {
					Cookie[] allCookies = req.getCookies(); // 获取Cookie
					if (allCookies != null) {
						
						for (int i = 0; i < allCookies.length; i++) {
							Cookie temp = allCookies[i];
							if (temp.getName().equals("openid")) {
								openid = temp.getValue(); 
								break;
							}
						}
					}
				}
				
				if ((openid != null)&&!openid.equals(""))
				{
					jsonUserInfo = getUserInfo(openid);
				}
					
				PrintWriter pw = resp.getWriter();
				if( jsonUserInfo == null ){
					pw.write("{\"ret\":1}");
				}
				else
				{
					jsonUserInfo.put("ret", 0);
					pw.write(jsonUserInfo.toString());
				}
				pw.flush();
				pw.close();
			} 
		} catch (Exception e) {
        	logger.log(Level.SEVERE, "error:", e);
        }
	}
	private void baiduLogin(HttpServletRequest req, HttpServletResponse resp){
		resp.setContentType("text/html;charset=utf-8");
		
        try {
           	String url = "https://openapi.baidu.com/oauth/2.0/authorize?response_type=code&scope=netdisk&client_id="+Common.BD_API_KEY+"&confirm_login=1&redirect_uri=http%3A%2F%2Flivepic2.yiqingart.com%2Fauth%2fbaidu_oauth";
        	resp.sendRedirect(url);
        } catch (Exception e) {
        	logger.log(Level.SEVERE, "error:", e);
        }
	}
	private void baiduOauth(HttpServletRequest req, HttpServletResponse resp){
		
		Connection connection = null;
		try {
			String code = req.getParameter("code");
			if(code == null){
				logger.log(Level.WARNING, "no code");
				return;
			}
			
			
			Map<String, String> params = new HashMap<String, String>();
			
			
			params.put("grant_type", "authorization_code");
			params.put("code", code);
			params.put("client_id", Common.BD_API_KEY);
			params.put("client_secret", Common.BD_SECRET_KEY);
			params.put("redirect_uri", "http://livepic2.yiqingart.com/auth/baidu_oauth");
			
			String url = "https://openapi.baidu.com/oauth/2.0/token";
			String response = HttpUtil.doPost(url, params);
			
			if (null == response) {
				logger.log(Level.WARNING, "no response");
				return;
			}

			JSONObject json;
			json = new JSONObject(response);
			
			logger.log(Level.INFO, "access_token:" + json.getString("access_token"));
			logger.log(Level.INFO, "refresh_token:" + json.getString("refresh_token"));
			
			// 具体的数据库操作逻辑
			connection = Common.getConnection();
			String sql = "update netdisk set access_token=? , refresh_token=?, expires_in=?";
			PreparedStatement ps = connection.prepareStatement(sql);

			ps.setNString(1, json.getString("access_token"));
			ps.setNString(2, json.getString("refresh_token"));
			ps.setLong(3, json.getLong("expires_in")*1000 + System.currentTimeMillis());
			ps.executeUpdate();
			
			sql = "update phone_cfg set config_version=? ";
		    ps = connection.prepareStatement(sql);
		    ps.setLong(1, System.currentTimeMillis());
		    
		    ps.executeUpdate();
			
//			PrintWriter pw = resp.getWriter();
//			
//			
//			pw.write(response);
//		
//
//			pw.flush();
//			pw.close();
			resp.sendRedirect("/test.html");

		}
		catch (Exception e) {
			// 异常处理逻辑
			logger.log(Level.SEVERE, "error:", e);
			return ;
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "error:", e);
				return ;
			}
		}
		
		return;
	}
	
	private void baiduRefresh(HttpServletRequest req, HttpServletResponse resp){
		Connection connection = null;
		String sql = "select * from netdisk";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// 具体的数据库操作逻辑
			connection = Common.getConnection();
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			rs.next();
			String refresh_token = rs.getNString("refresh_token");
						
			Map<String, String> params = new HashMap<String, String>();
			
			params.put("grant_type", "refresh_token");
			params.put("client_id", Common.BD_API_KEY);
			params.put("client_secret", Common.BD_SECRET_KEY);
			params.put("refresh_token", refresh_token);
			params.put("scope", "netdisk");
			
			String url = "https://openapi.baidu.com/oauth/2.0/token";
			String response = HttpUtil.doGet(url, params);
			
			if (null == response) {
				logger.log(Level.WARNING, "no response");
				return;
			}

			JSONObject json;
			json = new JSONObject(response);
			
			
			// 具体的数据库操作逻辑
			sql = "update netdisk set access_token=? , refresh_token=?, expires_in=?";
			PreparedStatement ps = connection.prepareStatement(sql);

			ps.setNString(1, json.getString("access_token"));
			ps.setNString(2, json.getString("refresh_token"));
			ps.setLong(3, json.getLong("expires_in")*1000 + System.currentTimeMillis());
			ps.executeUpdate();
			
			sql = "update phone_cfg set config_version=? ";
		    ps = connection.prepareStatement(sql);
		    ps.setLong(1, System.currentTimeMillis());
		    
		    ps.executeUpdate();
			
			PrintWriter pw = resp.getWriter();
			
			
			pw.write("refresh ok");
		

			pw.flush();
			pw.close();

		}
		catch (Exception e) {
			// 异常处理逻辑
			logger.log(Level.SEVERE, "error:", e);
			return ;
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "error:", e);
				return ;
			}
		}
		
		return;
	}
	private void qqLogin(HttpServletRequest req, HttpServletResponse resp){
		resp.setContentType("text/html;charset=utf-8");
        try {
        	//resp.sendRedirect(new Oauth().getAuthorizeURL(req));
        	String url = "https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id="+Common.QQ_CLENT_ID+"&redirect_uri=http%3A%2F%2Flivepic2.yiqingart.com%2Fauth%2fqq_redirect&state=12345";
        	resp.sendRedirect(url);
        } catch (Exception e) {
        	logger.log(Level.SEVERE, "error:", e);
        }
	}
	private void qqRedirect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		

        try {
        	resp.setCharacterEncoding("utf-8");
        	String access_token = null;
        	String code = req.getParameter("code");
			if(code == null){
				logger.log(Level.WARNING, "no code");
				return;
			}
			
			Map<String, String> params = new HashMap<String, String>();
			
			
			params.put("grant_type", "authorization_code");
			params.put("code", code);
			params.put("client_id", Common.QQ_CLENT_ID);
			params.put("client_secret", Common.QQ_CLENT_SECRET);
			params.put("redirect_uri", "http://livepic2.yiqingart.com/auth/qq_redirect");
			
			String url = "https://graph.qq.com/oauth2.0/token";
			String response = HttpUtil.doPost(url, params);
			
			if (null == response) {
				logger.log(Level.WARNING, "no response");
				return;
			}
			
			
			String[] keyValueList = response.split("&");
			for( String keyValue : keyValueList){
				String[] keyPair = keyValue.split("=");
				if(keyPair[0].equalsIgnoreCase("access_token"))
				{
					access_token = keyPair[1];
				}
			}
			params.clear();
			params.put("access_token", access_token);
			
			url = "https://graph.qq.com/oauth2.0/me";
			response = HttpUtil.doPost(url, params);
			
			if (null == response) {
				logger.log(Level.WARNING, "no response");
				return;
			}
			int startPos = response.indexOf("(");
			int endPos = response.indexOf(")");
			if(( startPos == -1 )||( endPos == -1 )){
				logger.log(Level.WARNING, "response error:" + response);
			}
			JSONObject jsonOpenid = new JSONObject(response.substring(startPos + 1, endPos-1));

			String openid = jsonOpenid.getString("openid");
			
			JSONObject jsonUserInfo = getQQUserInfo( openid, access_token );
			
			if( jsonUserInfo != null){
				Cookie openidCK=new Cookie("openid",openid); 
				openidCK.setMaxAge(14*24*3600); 
				resp.addCookie(openidCK);  
				HttpSession session = req.getSession(true);
				session.setAttribute("openid", openid );
				session.setAttribute("access_token", access_token );
				
				PrintWriter pw = resp.getWriter();
				//pw.write(response);
				pw.write(jsonUserInfo.getString("nickname")+"    ");
				pw.write(new String(jsonUserInfo.getString("nickname").getBytes("UTF-8"), "GBK"));
			
				
				pw.flush();
				pw.close();
	        }
			
        } catch (Exception e) {
        	logger.log(Level.SEVERE, "error:", e);
        }
	}
	private void baiduSocialCallback(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		

        try {
        	resp.setCharacterEncoding("utf-8");
        	String access_token = null;
        	String code = req.getParameter("code");
			if(code == null){
				logger.log(Level.WARNING, "no code");
				return;
			}
			
			Map<String, String> params = new HashMap<String, String>();
			
			
			params.put("grant_type", "authorization_code");
			params.put("code", code);
			params.put("client_id", Common.BD_API_KEY);
			params.put("client_secret", Common.BD_SECRET_KEY);
			params.put("redirect_uri", "http://yqartpic.duapp.com/auth/BAIDU_SOCIAL_CALLBACK");
			
			String url = "https://openapi.baidu.com/social/oauth/2.0/token";
			String response = HttpUtil.doGet(url, params);
			
			if (null == response) {
				logger.log(Level.WARNING, "no response");
				return;
			}
				
			PrintWriter pw = resp.getWriter();
			//pw.write(response);
			pw.write(response);
			
			
			pw.flush();
			pw.close();
			
        } catch (Exception e) {
        	logger.log(Level.SEVERE, "error:", e);
        }
	}
	private JSONObject getQQUserInfo(String openid){
		Connection connection = null;
		try {
			Statement stmt = null;
			ResultSet rs = null;
			
			// 具体的数据库操作逻辑
			connection = Common.getConnection();
			stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			
			String sql = "select * from  user where userid = '"+openid+"'";
			rs = stmt.executeQuery(sql);
			if(!rs.next()){
				logger.log(Level.WARNING, "no found user");
				return null;
			}
			
			return getQQUserInfo( openid, rs.getNString("access_token"));
		}
		catch (Exception e) {
			// 异常处理逻辑
			logger.log(Level.SEVERE, "error:", e);
			return null;
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "error:", e);
				return null;
			}
		}
	}
	private JSONObject getUserInfo(String openid){
		Connection connection = null;
		try {
			Statement stmt = null;
			ResultSet rs = null;
			
			// 具体的数据库操作逻辑
			connection = Common.getConnection();
			stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			
			String sql = "SELECT user.name, user.lastaccess, user_group.name as group_name,   user_group.admin_right FROM user, user_group WHERE user.userid = '"+openid+"'  AND user_group.usergroupid = user.usergroupid ";
		    
			//String sql = "select * from  user where userid = '"+openid+"'";
			rs = stmt.executeQuery(sql);
			if(!rs.next()){
				logger.log(Level.WARNING, "no found user");
				return null;
			}
			
			JSONArray jsonResult = Common.resultSetToJson(rs);
			if( jsonResult.length() == 0 ){
				return null;
			}
			else{
				return jsonResult.getJSONObject(0);
			}
		}
		catch (Exception e) {
			// 异常处理逻辑
			logger.log(Level.SEVERE, "error:", e);
			return null;
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "error:", e);
				return null;
			}
		}
	}
	private JSONObject getQQUserInfo(String openid, String access_token){
		Map<String, String> params = new HashMap<String, String>();
		try {
			params.put("access_token", access_token);
			params.put("openid", openid);
			params.put("oauth_consumer_key", Common.QQ_CLENT_ID);
			
			 
			
			String url = "https://graph.qq.com/user/get_user_info";
			String response = HttpUtil.doPost(url, params);
			
			JSONObject jsonUserInfo = new JSONObject(response);
			
			if( jsonUserInfo.getInt("ret") != 0 )
			{
				logger.log(Level.WARNING, "get_user_info error:" + jsonUserInfo.getInt("ret"));
				return null;
			}
			return jsonUserInfo;
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "error:", e);
			return null;
		}
	}
}
