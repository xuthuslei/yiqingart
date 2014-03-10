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

public class Delete extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger("Delete");

	public enum Method {
		WORK_GROUP, ACCESS_LOG, PHONE_CFG, ROOM_RIGHT, USER, USER_GROUP, WORK_PLACE, NOVALUE;
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
			jsonString =  deleteWorkGroup( req);
			break;
		case ACCESS_LOG:
			jsonString =  deleteAccessLog(req);
			break;
		case PHONE_CFG:
			jsonString =  deletePhoneCfg(req);
			break;
		case ROOM_RIGHT:
			jsonString =  deleteRoomRight(req);
			break;
		case USER:
			jsonString =  deleteUser(req);
			break; 
		case USER_GROUP:
			jsonString =  deleteUserGroup(req);
			break;
		case WORK_PLACE:
			jsonString =  deleteWorkPlaceCfg(req);
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
	private String deleteWorkGroup(HttpServletRequest req) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = Common.getConnection();
			
			String sql = "select * from  phone_cfg where workgroupid=" + req.getParameter("workgroupid");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(rs.next()){
				return "{\"result\":false,\"msg\":\"有相机在该房间\"}";
			}
			
			sql = "delete from work_group_config  where workgroupid=?";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setLong(1, Long.valueOf(req.getParameter("workgroupid")));
		    
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
	
	private String deleteAccessLog(HttpServletRequest req) {
		Connection connection = null;
		try {
			connection = Common.getConnection();
			
			String sql = "delete from access_log  where phone_id=?";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setInt(1, Integer.valueOf(req.getParameter("phone_id")));
		    
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
	
	private String deletePhoneCfg(HttpServletRequest req) {
		Connection connection = null;
		try {
			connection = Common.getConnection();
			
			String sql = "delete from phone_cfg  where phone_id=?";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setInt(1, Integer.valueOf(req.getParameter("phone_id")));
		    
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
	
	private String deleteRoomRight(HttpServletRequest req) {
		Connection connection = null;
		try {
			connection = Common.getConnection();
			
			String sql = "delete from room_right  where usergroupid=? and workpalceid=?";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setLong(1, Long.valueOf(req.getParameter("usergroupid")));
		    ps.setLong(2, Long.valueOf(req.getParameter("workpalceid")));
		    
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
	
	private String deleteUser(HttpServletRequest req) {
		Connection connection = null;
		try {
			connection = Common.getConnection();
			
			String sql = "delete from user  where useridx=? ";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setLong(1, Long.valueOf(req.getParameter("useridx")));
		    
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
	
	private String deleteUserGroup(HttpServletRequest req) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = Common.getConnection();
			String sql = "select * from  user where usergroupid=" + req.getParameter("usergroupid");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(rs.next()){
				return "{\"result\":false,\"msg\":\"有用户在该组\"}";
			}
			
			sql = "delete from user_group  where usergroupid=? ";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setLong(1, Long.valueOf(req.getParameter("usergroupid")));
		     
		    ps.executeUpdate();
		    
		    sql = "delete from room_right  where usergroupid=? ";
		    ps = connection.prepareStatement(sql);
		    ps.setLong(1, Long.valueOf(req.getParameter("usergroupid")));
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
	
	private String deleteWorkPlaceCfg(HttpServletRequest req) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = Common.getConnection();
			String sql = "select * from  phone_cfg where workplaceid=" + req.getParameter("workplaceid");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(rs.next()){
				return "{\"result\":false,\"msg\":\"有相机在该房间\"}";
			}
			
			sql = "delete from work_place_cfg  where workplaceid=? ";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setLong(1, Long.valueOf(req.getParameter("workplaceid")));
		     
		    ps.executeUpdate();

		    sql = "delete from room_right  where workplaceid=? ";
		    ps = connection.prepareStatement(sql);
		    ps.setLong(1, Long.valueOf(req.getParameter("workplaceid")));
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
