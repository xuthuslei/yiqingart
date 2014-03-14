package com.yiqingart.www;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
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

public class Insert extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger("Insert");

	public enum Method {
		WORK_GROUP, PHONE_CFG, ROOM_RIGHT, USER_GROUP, WORK_PLACE, NOVALUE;
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
			jsonString = insertWorkGroup(req);
			break;
		case PHONE_CFG:
			jsonString = insertPhoneCfg(req);
			break;	
		case ROOM_RIGHT:
			jsonString = insertRoomRight(req);
			break;	
		case USER_GROUP:
			jsonString = insertUserGroup(req);
			break;	
		case WORK_PLACE:
			jsonString = insertWorkPlaceCfg(req);
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
	private String insertWorkGroup(HttpServletRequest req) {
		Connection connection = null;
		try {
			connection = Common.getConnection();
			
			String sql = "insert into work_group_config( group_name, shotFreq, beginHour, beginMinute, endHour, endMinute, week1, week2, week3, week4, week5,  week6, week7, imgsize, target, config_version ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
	
	private String insertPhoneCfg(HttpServletRequest req) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
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
			
			sql = "insert into phone_cfg( phone_id, workgroupid, workplaceid, config_version ) values (?,?,?,?)";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setInt(1, Integer.valueOf(req.getParameter("phone_id")));
		    ps.setLong(2, Long.valueOf(req.getParameter("workgroupid")));
		    ps.setLong(3, Long.valueOf(req.getParameter("workplaceid")));
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
	
	private String insertRoomRight(HttpServletRequest req) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = Common.getConnection();
			
			String sql = "select * from  user_group where usergroupid=" + req.getParameter("usergroupid");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(!rs.next()){
				return "{\"result\":false,\"msg\":\"用户组不存在\"}";
			}
			
			sql = "select * from  work_place_cfg where workplaceid=" + req.getParameter("workplaceid");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(!rs.next()){
				return "{\"result\":false,\"msg\":\"房间不存在\"}";
			}
			
			sql = "insert into room_right( usergroupid, workpalceid ) values (?,?)";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setLong(1, Long.valueOf(req.getParameter("usergroupid")));
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
	
	private String insertUserGroup(HttpServletRequest req) {
		Connection connection = null;
		try {
			connection = Common.getConnection();
			
			String sql = "insert into user_group( name, admin_right ) values (?, ?)";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setNString(1, req.getParameter("name"));
		    ps.setBoolean(2, Boolean.valueOf(req.getParameter("admin_right")));
		    
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
	
	private String insertWorkPlaceCfg(HttpServletRequest req) {
		Connection connection = null;
		try {
			connection = Common.getConnection();
			
			String sql = "insert into work_place_cfg( place_name ) values (?)";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setNString(1, req.getParameter("place_name"));
		    
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
