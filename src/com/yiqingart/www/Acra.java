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

public class Acra extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8826861146379330737L;
	private Logger logger = Logger.getLogger("Acra");

	public enum Method {
		SUBMIT, QUERY, PHONE_ID, NOVALUE;
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
		String[] inputParams = requestURL.toString().split("/");
		String method = inputParams[2];
		//HttpSession session = req.getSession(true);// 如果没有该session，则自动创建一个新的

		//Integer cacheSecond = 0;
		//resp.setContentType("application/json");
		// resp.setHeader("Cache-Control", "nocache");

		//resp.setCharacterEncoding("utf-8");
		Method m = Method.toMethod(method.toUpperCase());
		switch (m) {
		case SUBMIT:
			submit( req, resp);
			break;
		case QUERY:
			query( req, resp);
			break;
		case PHONE_ID:
			phone_id( req, resp);
			break;
		default:
			PrintWriter pw = resp.getWriter();
			pw.write("wrong");
			pw.flush();
			pw.close();
			break;
		}
	}
	private void submit(HttpServletRequest req, HttpServletResponse resp) {
		Connection connection = null;
		try {
			connection = Common.getConnection();
			int phone_id = 21;
			int start = req.getParameter("SHARED_PREFERENCES").indexOf("phone_id_key=")+"phone_id_key=".length();
			if (start > 0) {
				String subStr = req.getParameter("SHARED_PREFERENCES")
						.substring(start);
				int end = start + 1;
				if (Character.isDigit(subStr.charAt(1))) {
					end++;
				}
				phone_id = Integer.valueOf(req.getParameter(
						"SHARED_PREFERENCES").substring(start, end));
			}
			
			String sql = "insert into acra( APP_VERSION_CODE, SHARED_PREFERENCES, ANDROID_VERSION, APP_VERSION_NAME, STACK_TRACE, USER_APP_START_DATE, USER_CRASH_DATE, phone_id, time ) values (?,?,?,?,?,?,?,?,?)";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setNString(1, req.getParameter("APP_VERSION_CODE"));
		    ps.setNString(2, req.getParameter("SHARED_PREFERENCES"));
		    ps.setNString(3, req.getParameter("ANDROID_VERSION"));
		    ps.setNString(4, req.getParameter("APP_VERSION_NAME"));
		    ps.setNString(5, req.getParameter("STACK_TRACE"));
		    ps.setNString(6, req.getParameter("USER_APP_START_DATE"));
		    ps.setNString(7, req.getParameter("USER_CRASH_DATE"));
		    ps.setInt(8, phone_id);
		    ps.setLong(9, System.currentTimeMillis());
		    
		    ps.executeUpdate();
		    
		    PrintWriter pw = resp.getWriter();
			pw.write("hello");
			pw.flush();
			pw.close();
		    
			return ;
		} catch (Exception e) {
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
	}
	private void query(HttpServletRequest req, HttpServletResponse resp) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = Common.getConnection();
			String sql = "select * from  acra " ;
			if(null !=req.getParameter("phone_id")){
				sql +=" where phone_id ="+req.getParameter("phone_id");
			}
			sql +=" ORDER BY time DESC";
			if(null !=req.getParameter("start")){
				sql +=" LIMIT "+req.getParameter("start")+" , 30";
			}
			else{
				sql +=" LIMIT 0 , 30";
			}
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			
			PrintWriter pw = resp.getWriter();
			
			
			pw.write(Common.resultSetToJson(rs).toString());
			pw.flush();
			pw.close();
		} catch (Exception e) {
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
	}
	private void phone_id(HttpServletRequest req, HttpServletResponse resp) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = Common.getConnection();
			
			String sql = "select * from  acra where idx = 2" ;
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			if(!rs.next()){
				return;
			}
			PrintWriter pw = resp.getWriter();
			String shared = rs.getNString("SHARED_PREFERENCES");
			int start = shared.indexOf("phone_id_key=")+"phone_id_key=".length();
			String subStr = shared.substring(start);
			int end = start+1;
			if(Character.isDigit(subStr.charAt(1))){
				end++;
			}
			logger.log(Level.SEVERE, "start:"+start+"  end:"+end );
			int phone_id = Integer.valueOf(shared.substring(start,end));
			
			pw.write("phone_id"+phone_id);
			pw.flush();
			pw.close();
		} catch (Exception e) {
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
	}
}
