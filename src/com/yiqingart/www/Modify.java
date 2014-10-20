package com.yiqingart.www;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Modify extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger("Insert");

	public enum Method {
		WORK_GROUP, PHONE_CFG, USER, USER_GROUP, WORK_PLACE, PHONE_RESET, NOVALUE;
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
		String requestURL = req.getRequestURI().substring(req.getContextPath().length());
		String jsonString = null;
		String[] inputParams = requestURL.toString().split("/")[2]
				.split("\\x2E");
		String method = inputParams[0];
		String format = inputParams[1];
		HttpSession session = req.getSession(true);// 如果没有该session，则自动创建一个新的

		Integer cacheSecond = 0;
		resp.setContentType("application/json");
		// resp.setHeader("Cache-Control", "nocache");

		resp.setCharacterEncoding("utf-8");
		Method m = Method.toMethod(method.toUpperCase());
		switch (m) {
		case WORK_GROUP:
			jsonString = modifyWorkGroup( req);
			break;
		case PHONE_CFG:
			jsonString = modifyPhoneCfg(req);
			break;
		case USER:
			jsonString = modifyUser(req);
			break;
		case USER_GROUP:
			jsonString = modifyUserGroup(req);
			break;
		case WORK_PLACE:
			jsonString = modifyWorkPlaceCfg(req);
			break;
		case PHONE_RESET:
			jsonString = modifyPhoneReset(req);
			break;
		default:
			break;
		}
		
		if( jsonString == null )
		{
			logger.log(Level.INFO, "wrong method:" + req.getRequestURL());
			jsonString = "{\"error\":\"wrong "+req.getRequestURL()+"\"}";
		}
		else
		{
			logger.log(Level.INFO, "cacheSecond out " + cacheSecond);
			resp.setDateHeader("Expires",
					System.currentTimeMillis() + cacheSecond * 1000);
		}
		
		PrintWriter pw = resp.getWriter();
		if (format.equalsIgnoreCase("jsonp")) {
			pw.write("jsonpCallback(" + jsonString + ")");
		} else {
			pw.write(jsonString);
		}

		pw.flush();
		pw.close();
	}
	private String modifyWorkGroup(HttpServletRequest req) {
		
		Connection connection = null;
		try {
			// 具体的数据库操作逻辑
			connection = Common.getConnection();
			
			String sql = "update work_group_config set group_name=? , shotFreq=? , beginHour=? , beginMinute=? , endHour=? , endMinute=? , week1=? , week2=? , week3=? , week4=? , week5=? ,  week6=? , week7=? , imgsize=?, target=?, config_version=?  where workgroupid=?";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setNString(1, req.getParameter("group_name"));
		    ps.setInt(2, Integer.valueOf(req.getParameter("shotFreq")));
		    ps.setInt(3, Integer.valueOf(req.getParameter("beginHour")));
		    ps.setInt(4, Integer.valueOf(req.getParameter("beginMinute")));
		    ps.setInt(5, Integer.valueOf(req.getParameter("endHour")));
		    ps.setInt(6, Integer.valueOf(req.getParameter("endMinute")));
		    ps.setInt(7, Integer.valueOf(req.getParameter("week1")));
		    ps.setInt(8, Integer.valueOf(req.getParameter("week2")));
		    ps.setInt(9, Integer.valueOf(req.getParameter("week3")));
		    ps.setInt(10, Integer.valueOf(req.getParameter("week4")));
		    ps.setInt(11, Integer.valueOf(req.getParameter("week5")));
		    ps.setInt(12, Integer.valueOf(req.getParameter("week6")));
		    ps.setInt(13, Integer.valueOf(req.getParameter("week7")));
		    ps.setInt(14, Integer.valueOf(req.getParameter("imgsize")));
		    ps.setInt(15, Integer.valueOf(req.getParameter("target")));
		    ps.setLong(16, System.currentTimeMillis());
		    ps.setLong(17, Long.valueOf(req.getParameter("workgroupid")));
		    
		    ps.executeUpdate();
		    
		    sql = "update phone_cfg set config_version=? where workgroupid=?";
		    ps = connection.prepareStatement(sql);
		    ps.setLong(1, System.currentTimeMillis());
		    ps.setLong(2, Long.valueOf(req.getParameter("workgroupid")));
		    
		    ps.executeUpdate();
		    
			return "{\"result\":true}";
		} catch (Exception e) {
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
	
	private String modifyPhoneCfg(HttpServletRequest req) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// 具体的数据库操作逻辑
			connection = Common.getConnection();
			String sql = "select * from  work_group_config where workgroupid=" + req.getParameter("workgroupid");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(!rs.next()){
				return "{\"result\":false,\"msg\":\"工作组不存在\"}";
			}
			
			sql = "select * from  work_place_cfg where workplaceid=" + req.getParameter("workplaceid");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(!rs.next()){
				return "{\"result\":false,\"msg\":\"房间不存在\"}";
			}
			
			//sql = "update phone_cfg set workgroupid=? , workplaceid = ? where phone_id=?";
			sql = "replace into phone_cfg( workgroupid, workplaceid, phone_id, config_version ) values (?,?,?,?)";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setLong(1, Long.valueOf(req.getParameter("workgroupid")));
		    ps.setLong(2, Long.valueOf(req.getParameter("workplaceid")));
		    ps.setInt(3, Integer.valueOf(req.getParameter("phone_id")));
		    ps.setLong(4, System.currentTimeMillis());
		    
		    ps.executeUpdate();
		    
			return "{\"result\":true}";
		} catch (Exception e) {
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
	
	private String modifyUser(HttpServletRequest req) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql;
		try {
			// 具体的数据库操作逻辑
			connection = Common.getConnection();
			
			if(req.getParameter("usergroupid") != null){
				sql = "select * from  user_group where usergroupid=" + req.getParameter("usergroupid");
				stmt = connection.createStatement();
				rs = stmt.executeQuery(sql);
				
				if(!rs.next()){
					return "{\"result\":false,\"msg\":\"用户组不存在\"}";
				}
				
				sql = "update user set usergroupid=? where useridx=?";
				PreparedStatement ps = connection.prepareStatement(sql);
				ps.setLong(1, Long.valueOf(req.getParameter("usergroupid")));
				ps.setLong(2, Long.valueOf(req.getParameter("useridx")));
				ps.executeUpdate();
			}
			
			if(req.getParameter("name") != null){
				sql = "update user set name=? where useridx=?";
				PreparedStatement ps = connection.prepareStatement(sql);
				ps.setNString(1, req.getParameter("name"));
				ps.setLong(2, Long.valueOf(req.getParameter("useridx")));
				ps.executeUpdate();
			}
			
			if(req.getParameter("lastaccess") != null){
				sql = "update user set lastaccess=? where useridx=?";
				PreparedStatement ps = connection.prepareStatement(sql);
				ps.setLong(1, Long.valueOf(req.getParameter("lastaccess")));
				ps.setLong(2, Long.valueOf(req.getParameter("useridx")));
				ps.executeUpdate();
			}
		    
			return "{\"result\":true}";
		} catch (Exception e) {
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
	
	private String modifyUserGroup(HttpServletRequest req) {
		
		Connection connection = null;
		try {
			// 具体的数据库操作逻辑
			connection = Common.getConnection();
			String sql = "update user_group set name=? , admin_right = ?  where usergroupid=?";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setNString(1, req.getParameter("name"));
		    ps.setBoolean(2, Boolean.valueOf(req.getParameter("admin_right")));
		    ps.setLong(3, Long.valueOf(req.getParameter("usergroupid")));
		    
		    ps.executeUpdate();
		    
			return "{\"result\":true}";
		} catch (Exception e) {
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
	
	private String modifyWorkPlaceCfg(HttpServletRequest req) {
		
		Connection connection = null;
		try {
			// 具体的数据库操作逻辑
			connection = Common.getConnection();
			String sql = "update work_place_cfg set place_name=?  where workplaceid=?";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setNString(1, req.getParameter("place_name"));
		    ps.setLong(2, Long.valueOf(req.getParameter("workplaceid")));
		    
		    ps.executeUpdate();
		    
		    sql = "update phone_cfg set config_version=? where workplaceid=?";
		    ps = connection.prepareStatement(sql);
		    ps.setLong(1, System.currentTimeMillis());
		    ps.setLong(2, Long.valueOf(req.getParameter("workplaceid")));
		    ps.executeUpdate();
		    
			return "{\"result\":true}";
		} catch (Exception e) {
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
	private String modifyPhoneReset(HttpServletRequest req) {
		Connection connection = null;
		try {
			connection = Common.getConnection();
			
			String sql = "update access_log set startcount = 0";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    ps.executeUpdate();
		    
			return "{\"result\":true}";
		} catch (Exception e) {
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
	
}
