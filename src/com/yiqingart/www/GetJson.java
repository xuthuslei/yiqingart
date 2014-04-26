package com.yiqingart.www;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.bae.api.memcache.BaeCache;


public class GetJson extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger("GetJson");

	public enum Method {
		HELLO,ACCESS_TOKEN, NEW_PIC_LIST, ROOM_LIST, ROOM_NEWEST_PIC, ROOM_DAY_LIST, ROOM_DAY_PIC_LIST, WORK_GROUP_LIST, PHONE_HEARTBEAT, ADMIN_DATA , NETDISK, LIVEVIDEO, NOVALUE;
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
		case HELLO:
			cacheSecond = 0;
			jsonString = "{\"result\":\"hello haha\"}";
			break;
		case ACCESS_TOKEN:
			cacheSecond = 30;
			jsonString = getAccessTokenJson(session);
			break;
		case NEW_PIC_LIST:
			cacheSecond = 30;
			jsonString = getNewPic(session);
			break;
		case ROOM_LIST:
			cacheSecond = 120;
			jsonString = getRoomListJson(session);
			break;
		case ROOM_NEWEST_PIC:
			cacheSecond = 25;
			jsonString = getRoomNewestPicJson(session, req.getParameter("room"), req.getParameter("path_id"));
			break;
		case ROOM_DAY_LIST:
			cacheSecond = 30;
			jsonString = getRoomDayListJson(session, req.getParameter("room"));
			break;
		case ROOM_DAY_PIC_LIST:
			cacheSecond = 120;
			jsonString = getRoomDayPicListJson(session, req.getParameter("room_day"), req.getParameter("limit"));
			final Calendar c = Calendar.getInstance();
			c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
			int mYear = c.get(Calendar.YEAR); // 获取当前年份
			int mMonth = c.get(Calendar.MONTH) + 1;// 获取当前月份
			int mDay = c.get(Calendar.DAY_OF_MONTH);// 获取当前月份的日期号码
			String today = String.format("%04d-%02d-%02d", mYear,
					mMonth, mDay);
			
			if(!today.equalsIgnoreCase(req.getParameter("room_day").split("/")[4]))
			{
				cacheSecond = 24*3600;
			}
			break;
		case WORK_GROUP_LIST:
			jsonString = getWorkGroupList();
			break;
		case ADMIN_DATA:
			jsonString = getAdminData( req);
			break;
		case PHONE_HEARTBEAT:
			jsonString = getPhoneHeartBeat(req);
			break;
		case NETDISK:
			jsonString = getNetDiskJson(req);
			break;
		case LIVEVIDEO:
		    jsonString = getLiveVideo();
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

	private String  getLiveVideo() {
        FileCache filecache = FileCache.getInstance();
        String[] list = filecache.getM3U8List();
        if(list == null){
            return "[]";
        }
        JSONArray jsonResult = new JSONArray();
        for(String key: list){
            jsonResult.put(key);
        }
        
        return jsonResult.toString();
    }
	private String getAccessTokenJson(HttpSession session) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("access_token", Common.getAccessToken(session));

		JSONObject json = new JSONObject(map);
		return json.toString();
	}

	private String getNetDiskJson(HttpServletRequest req) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = Common.getConnection();
			String sql = "select * from  netdisk ";
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			if(!rs.next()){
				return null;
			}
			
			JSONObject jsonResult = new JSONObject();
			jsonResult.put("result", 0);
			jsonResult.put("expires_in",rs.getLong("expires_in"));
			jsonResult.put("access_token",rs.getNString("access_token"));
			jsonResult.put("refresh_token",rs.getNString("refresh_token"));
			
			Map<String, String> params = new HashMap<String, String>();
			
			params.put("method", "info");
			params.put("access_token", rs.getNString("access_token"));
			
			String url = "https://pcs.baidu.com/rest/2.0/pcs/quota";
			String response = HttpUtil.doGet(url, params);
			
			if (null == response) {
				jsonResult.put("state", 1);
			}
			else{
				jsonResult.put("state", 0);
				JSONObject json;
				json = new JSONObject(response);
				jsonResult.put("quota", json.getLong("quota"));
				jsonResult.put("used", json.getLong("used"));
			}
			
			return jsonResult.toString();
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
	private String getRoomListJson(HttpSession session) {
		String accessToken = Common.getAccessToken(session);
		BaeCache baeCache = Common.getBaeCache();
		JSONObject json;
		
		String value = (String)baeCache.get("room_list");
		
		if (value != null) {
			json = new JSONObject(value);
			json.put("memcached", "true");
			return json.toString();
		}
		
		Map<Long, String> room_list = getRoomList(session);
		JSONArray roomLists = new JSONArray();
		try {
			Iterator<Entry<Long, String>> iter = room_list.entrySet()
					.iterator();
			while (iter.hasNext()) {
				Map.Entry<Long, String> entry = (Map.Entry<Long, String>) iter
						.next();
				Long path_id = entry.getKey();
				String room = entry.getValue();

				JSONObject roomJson = new JSONObject();
				roomJson.put("path_id", path_id);
				roomJson.put("room", room);

				roomLists.put(roomJson);
			}
		} catch (Exception e) {
			// 异常处理逻辑
			logger.log(Level.SEVERE, "error:", e);
			return "";
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("list", roomLists);
		map.put("access_token", accessToken);

		json = new JSONObject(map);
		if(baeCache.add("room_list", json.toString(), 30000)){
			json.put("memcached", "put_true");
		}
		else{
			json.put("memcached", "put_false:"+baeCache.getErrMsg());
		}
		
		return json.toString();
	}

	

	private String getNewPic(HttpSession session) {

		Map<Long, String> room_list = getRoomList(session);
		String accessToken = Common.getAccessToken(session);
		Map<String, String> params = new HashMap<String, String>();
		String response;

		JSONArray fileLists = new JSONArray();

		if (room_list == null) {
			logger.log(Level.INFO, "null room_list");
			return null;
		}

		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		int mYear = c.get(Calendar.YEAR); // 获取当前年份
		int mMonth = c.get(Calendar.MONTH) + 1;// 获取当前月份
		int mDay = c.get(Calendar.DAY_OF_MONTH);// 获取当前月份的日期号码

		try {
			Iterator<Entry<Long, String>> iter = room_list.entrySet()
					.iterator();
			while (iter.hasNext()) {
				Map.Entry<Long, String> entry = (Map.Entry<Long, String>) iter
						.next();
				Long path_id = entry.getKey();
				String room = entry.getValue();

				String target = String.format("%s/%04d-%02d-%02d", room, mYear,
						mMonth, mDay);
				params.clear();

				BaiduApiClient baidu = new BaiduApiClient(accessToken);

				params.put("method", "list");
				params.put("path", target);
				params.put("by", "time");
				params.put("order", "desc");
				params.put("limit", "0-1");

				response = baidu.request(
						"https://pcs.baidu.com/rest/2.0/pcs/file", params,
						BaiduApiClient.METHOD_GET);
				if (null == response) {
					logger.log(Level.INFO, target + " found no pic");
					continue;
				}

				JSONObject json;
				json = new JSONObject(response);

				JSONArray lists = json.getJSONArray("list");

				if (null == lists) {
					logger.log(Level.INFO, target + " found no pic");
					continue;
				}

				if (lists.length() < 1) {
					logger.log(Level.INFO, target + " found no pic");
					continue;
				}

				JSONObject file = lists.getJSONObject(0);

				if (file.getInt("isdir") == 0) {
					file.put("path_id", path_id);
					fileLists.put(file);
//					logger.log(Level.INFO,
//							target + " found  pic " + file.getString("path"));
				}
			}

			JSONObject jsonResult = new JSONObject();
			jsonResult.put("list", fileLists);
			jsonResult.put("tag", System.currentTimeMillis());
			jsonResult.put("access_token", accessToken);
			return jsonResult.toString();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error:", e);
		}
		return null;
	}

	private String getRoomNewestPicJson(HttpSession session, String room, String path_id) {

		String accessToken = Common.getAccessToken(session);
		Map<String, String> params = new HashMap<String, String>();
		String response;
		//BaeCache baeCache = Common.getBaeCache();
		FileCache filecache = FileCache.getInstance();
		
		if (room == null) {
			logger.log(Level.INFO, "null room ");
			return null;
		}
		JSONObject file = null;
		
		file = filecache.getRoomNewPic(room);
		if (file != null) {
			file.put("memcached", "true");
			if( path_id != null )
			{
				file.put("path_id", path_id);
			}
			return file.toString();
		}

		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		int mYear = c.get(Calendar.YEAR); // 获取当前年份
		int mMonth = c.get(Calendar.MONTH) + 1;// 获取当前月份
		int mDay = c.get(Calendar.DAY_OF_MONTH);// 获取当前月份的日期号码

		try {

			String target = String.format("%s/%04d-%02d-%02d", room, mYear,
					mMonth, mDay);
			params.clear();

			BaiduApiClient baidu = new BaiduApiClient(accessToken);

			params.put("method", "list");
			params.put("path", target);
			params.put("by", "time");
			params.put("order", "desc");
			params.put("limit", "0-1");

			response = baidu.request("https://pcs.baidu.com/rest/2.0/pcs/file",
					params, BaiduApiClient.METHOD_GET);
			if (null == response) {
				logger.log(Level.INFO, target + " found no pic");
				return null;
			}

			JSONObject json;
			json = new JSONObject(response);

			JSONArray lists = json.getJSONArray("list");

			if (null == lists) {
				logger.log(Level.INFO, target + " found no pic");
				return null;
			}

			if (lists.length() < 1) {
				logger.log(Level.INFO, target + " found no pic");
				return null;
			}

			file = lists.getJSONObject(0);

			if (file.getInt("isdir") != 0) {
				logger.log(Level.INFO, target + " found no pic");
				return null;
			}
			if( path_id != null )
			{
				file.put("path_id", path_id);
			}
			
			file.put("filecache", "from pcs");
			filecache.setRoomNewPic(room, file, 25000l);
			
			logger.log(Level.INFO,
					target + " found  pic " + file.getString("path"));
			
			return file.toString();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error:", e);
		}
		return null;
	}

	private Map<Long, String> getRoomList(HttpSession session) {

		// if( null != session )
		// {
		// @SuppressWarnings("unchecked")
		// ArrayList<String>
		// room_list=(ArrayList<String>)session.getAttribute("room_list");
		// if( room_list!=null)
		// {
		// return room_list;
		// }
		// }

		String accessToken = Common.getAccessToken(session);
		Map<String, String> params = new HashMap<String, String>();
		String response;

		if (accessToken.length() < 2) {
			logger.log(Level.INFO, "null access_token");
			return null;
		}

		try {
			Map<Long, String> room_list = new HashMap<Long, String>();
			
			BaiduApiClient baidu = new BaiduApiClient(accessToken);

			params.put("method", "list");
			params.put("path", "/apps/yqart");

			response = baidu.request("https://pcs.baidu.com/rest/2.0/pcs/file",
					params, BaiduApiClient.METHOD_GET);

			JSONObject json;
			json = new JSONObject(response);

			JSONArray lists = json.getJSONArray("list");

			for (int i = 0; i < lists.length(); i++) {
				JSONObject room = lists.getJSONObject(i);

				if (room.getInt("isdir") == 1) {
					room_list
							.put(room.getLong("fs_id"), room.getString("path"));
//					logger.log(Level.INFO,
//							"room add id:" + room.getLong("fs_id") + " path:"
//									+ room.getString("path"));
				}
			}

			// if( null != session )
			// {
			// session.setAttribute("room_list", room_list);
			// }
			return room_list;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.log(Level.SEVERE, "error:", e);
		}
		return null;
	}
	private String getRoomDayListJson(HttpSession session, String room) {
		String accessToken = Common.getAccessToken(session);
		Map<String, String> params = new HashMap<String, String>();
		String response;

		if (accessToken.length() < 2) {
			logger.log(Level.INFO, "null access_token");
			return null;
		}
		
		if (room == null) {
			logger.log(Level.INFO, "null room");
			return null;
		}

		try {
			JSONArray dayLists = new JSONArray();
			
			BaiduApiClient baidu = new BaiduApiClient(accessToken);

			params.put("method", "list");
			params.put("path", room);
			params.put("by", "time");
			params.put("order", "desc");

			response = baidu.request("https://pcs.baidu.com/rest/2.0/pcs/file",
					params, BaiduApiClient.METHOD_GET);

			JSONObject json;
			json = new JSONObject(response);

			JSONArray lists = json.getJSONArray("list");

			for (int i = 0; i < lists.length(); i++) {
				JSONObject day = lists.getJSONObject(i);

				if (day.getInt("isdir") == 1) {
					
					dayLists.put(day.getString("path"));
//					logger.log(Level.INFO,
//							"room add  path:"+ day.getString("path"));
				}
			}
			
			JSONObject jsonResult = new JSONObject();
			jsonResult.put("list", dayLists);
			jsonResult.put("tag", System.currentTimeMillis());
			return jsonResult.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.log(Level.SEVERE, "error:", e);
		}
		return null;
	}
	private String getRoomDayPicListJson(HttpSession session, String room_day, String limit) {
		String accessToken = Common.getAccessToken(session);
		Map<String, String> params = new HashMap<String, String>();
		String response;

		if (accessToken.length() < 2) {
			logger.log(Level.INFO, "null access_token");
			return null;
		}
		
		if (room_day == null) {
			logger.log(Level.INFO, "null room_day");
			return null;
		}
		
		try {
			JSONArray picLists = new JSONArray();
			
			BaiduApiClient baidu = new BaiduApiClient(accessToken);

			params.put("method", "list");
			params.put("path", room_day);
			params.put("by", "time");
			params.put("order", "desc");
			if( limit != null )
			{
				params.put("limit", limit);
			}

			response = baidu.request("https://pcs.baidu.com/rest/2.0/pcs/file",
					params, BaiduApiClient.METHOD_GET);

			JSONObject json;
			json = new JSONObject(response);

			JSONArray lists = json.getJSONArray("list");

			for (int i = 0; i < lists.length(); i++) {
				JSONObject pic = lists.getJSONObject(i);

				if (pic.getInt("isdir") == 0) {
					picLists.put(pic.getString("path"));
					//logger.log(Level.INFO, "room add path:" + pic.getString("path"));
				}
			}
			
			JSONObject jsonResult = new JSONObject();
			jsonResult.put("list", picLists);
			jsonResult.put("access_token", accessToken);
			jsonResult.put("tag", System.currentTimeMillis());
			return jsonResult.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.log(Level.SEVERE, "error:", e);
		}
		return null;
	}
	private String getWorkGroupList() {
		
		String sql = "select * from  work_group_config";

		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			JSONArray workGroupLists = new JSONArray();
			
			
			// 具体的数据库操作逻辑
			connection = Common.getConnection();
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				JSONObject workGroup = new JSONObject();
				workGroup.put("idx", rs.getLong("idx"));
				workGroup.put("name", rs.getString("name"));
				workGroup.put("shotFreq", rs.getInt("shotFreq"));
				workGroup.put("beginHour", rs.getInt("beginHour"));
				workGroup.put("beginMinute", rs.getInt("beginMinute"));
				workGroup.put("endHour", rs.getInt("endHour"));
				workGroup.put("endMinute", rs.getInt("endMinute"));
				workGroup.put("config_version", rs.getLong("config_version"));
				workGroup.put("week1", rs.getInt("week1"));
				workGroup.put("week2", rs.getInt("week2"));
				workGroup.put("week3", rs.getInt("week3"));
				workGroup.put("week4", rs.getInt("week4"));
				workGroup.put("week5", rs.getInt("week5"));
				workGroup.put("week6", rs.getInt("week6"));
				workGroup.put("week7", rs.getInt("week7"));
				
				workGroupLists.put(workGroup);
			}
			
			JSONObject jsonResult = new JSONObject();
			jsonResult.put("list", workGroupLists);
			jsonResult.put("tag", System.currentTimeMillis());
			return jsonResult.toString();
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
	
private String getAdminData(HttpServletRequest req) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// 具体的数据库操作逻辑
			connection = Common.getConnection();
			stmt = connection.createStatement();
			JSONObject jsonResult = new JSONObject();
			
			String sql = "select * from  work_group_config";
			rs = stmt.executeQuery(sql);
			jsonResult.put("work_group_config", Common.resultSetToJson(rs));
			
			sql = "select * from  user_group";
			rs = stmt.executeQuery(sql);
			jsonResult.put("user_group", Common.resultSetToJson(rs));
			
			sql = "select * from  access_log";
			rs = stmt.executeQuery(sql);
			jsonResult.put("access_log", Common.resultSetToJson(rs));
			
			sql = "select * from  netdisk";
			rs = stmt.executeQuery(sql);
			jsonResult.put("netdisk", Common.resultSetToJson(rs));
				
			sql = "select * from  phone_cfg";
			rs = stmt.executeQuery(sql);
			jsonResult.put("phone_cfg", Common.resultSetToJson(rs));
			
			sql = "select * from  room_right";
			rs = stmt.executeQuery(sql);
			jsonResult.put("room_right", Common.resultSetToJson(rs));
			
			sql = "select * from  user";
			rs = stmt.executeQuery(sql);
			jsonResult.put("user", Common.resultSetToJson(rs));
			
			sql = "select * from  user_group";
			rs = stmt.executeQuery(sql);
			jsonResult.put("user_group", Common.resultSetToJson(rs));
			
			sql = "select * from  work_place_cfg";
			rs = stmt.executeQuery(sql);
			jsonResult.put("work_place_cfg", Common.resultSetToJson(rs));
			
			sql = "select * from  globel";
			rs = stmt.executeQuery(sql);
			jsonResult.put("globel", Common.resultSetToJson(rs));
			
			jsonResult.put("tag", System.currentTimeMillis());
			return jsonResult.toString();
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
	
	private String getPhoneHeartBeat(HttpServletRequest req) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		Long startcount = 0l;
		try {
			connection = Common.getConnection();
			String sql = "select * from  access_log where phone_id = " + req.getParameter("phone_id");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			if(rs.next()){
				String net_config_version = req.getParameter("net_config_version");
				if((net_config_version!=null)&&(net_config_version.length()>0)){
					startcount = rs.getLong("startcount");
					if(Long.valueOf(req.getParameter("net_config_version")) == 0 )
					{
						startcount++;
					}
				}
			}
			sql = "replace into access_log( android_id, phone_id, time, config_version, version, sendbytes, startcount, msg ) values (?,?,?,?,?,?,?,?)";
		    PreparedStatement ps = connection.prepareStatement(sql);
		    
		    ps.setNString(1, req.getParameter("android_id"));
		    ps.setInt(2, Integer.valueOf(req.getParameter("phone_id")));
		    ps.setLong(3, System.currentTimeMillis()); // 日期和时间
		    ps.setLong(4, Long.valueOf(req.getParameter("net_config_version")));
		    ps.setNString(5, req.getParameter("version"));
		    ps.setLong(6, Long.valueOf(req.getParameter("sendbytes")));
		    ps.setLong(7, startcount);
		    ps.setNString(8, req.getParameter("msg")==null?"":req.getParameter("msg"));
			ps.executeUpdate();
			sql = "SELECT work_place_cfg.place_name, "
			            +"work_group_config.beginHour, "
					    +"work_group_config.beginMinute, "
			            +"work_group_config.endHour, "
					    +"work_group_config.endMinute, "
			            +"work_group_config.shotFreq, "
			            +"work_group_config.week1, "
			            +"work_group_config.week2, "
			            +"work_group_config.week3, "
			            +"work_group_config.week4, "
			            +"work_group_config.week5, "
			            +"work_group_config.week6, "
			            +"work_group_config.week7, "
			            +"work_group_config.imgsize, "
			            +"work_group_config.target, "
			            +"phone_cfg.config_version, "
			            +"netdisk.appsPath, "
					    +"netdisk.access_token "
			        +"FROM work_group_config, work_place_cfg, netdisk, phone_cfg "
					+"WHERE phone_cfg.phone_id = "
					+ req.getParameter("phone_id")
					+ " AND work_place_cfg.workplaceid = phone_cfg.workplaceid AND work_group_config.workgroupid = phone_cfg.workgroupid ";

			logger.log(Level.WARNING, "sql:"+ sql);
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			logger.log(Level.WARNING, "rs:");	
			if(rs.next()){
				//{"net_config_version":1387215337,"apps_path_key":"/apps/yqart","access_token_key":"3.675009f414c2601568ed6f9e3522e6a1.2592000.1388673605.1732080263-1514280",
				//"shoot_freq_key":30,"checkbox_week_key":"true,true,true,true,true,true,true","time_range_begin_hour_key":7,
				//"time_range_begin_minute_key":30,"time_range_end_hour_key":19,"time_range_end_minute_key":30}
				JSONObject jsonResult = new JSONObject();
				jsonResult.put("result", 0);
				jsonResult.put("net_config_version", rs.getLong("config_version"));
				jsonResult.put("apps_path_key", rs.getNString("appsPath"));
				jsonResult.put("access_token_key", rs.getNString("access_token"));
				jsonResult.put("shoot_freq_key", rs.getInt("shotFreq"));
				jsonResult.put("time_range_begin_hour_key", rs.getInt("beginHour"));
				jsonResult.put("time_range_begin_minute_key", rs.getInt("beginMinute"));
				jsonResult.put("time_range_end_hour_key", rs.getInt("endHour"));
				jsonResult.put("time_range_end_minute_key", rs.getInt("endMinute"));
				jsonResult.put("local_name_key", rs.getNString("place_name"));
				jsonResult.put("pic_width", rs.getInt("imgsize"));
				jsonResult.put("target", rs.getInt("target"));
				if((System.getProperty("baejavasdk.local")!=null)&&(System.getProperty("baejavasdk.local").equalsIgnoreCase("true"))){
					jsonResult.put("domain", "http://192.168.1.10:8080");
				}
				else
				{
					jsonResult.put("domain", "http://yiqingart.duapp.com");
				}
				String weekSet = "";
				if(rs.getInt("week1")==1){
					weekSet += "true,";
				}
				else{
					weekSet += "false,";
				}
				if(rs.getInt("week2")==1){
					weekSet += "true,";
				}
				else{
					weekSet += "false,";
				}
				if(rs.getInt("week3")==1){
					weekSet += "true,";
				}
				else{
					weekSet += "false,";
				}
				if(rs.getInt("week4")==1){
					weekSet += "true,";
				}
				else{
					weekSet += "false,";
				}
				if(rs.getInt("week5")==1){
					weekSet += "true,";
				}
				else{
					weekSet += "false,";
				}
				if(rs.getInt("week6")==1){
					weekSet += "true,";
				}
				else{
					weekSet += "false,";
				}
				if(rs.getInt("week7")==1){
					weekSet += "true";
				}
				else{
					weekSet += "false";
				}
				jsonResult.put("checkbox_week_key", weekSet);
				return jsonResult.toString();
			}
			else{
				return "{\"result\":1}";
			}
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
