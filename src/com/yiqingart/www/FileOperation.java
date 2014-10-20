package com.yiqingart.www;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.baidu.bae.api.memcache.BaeCache;
import com.yiqingart.www.Acra.Method;

public class FileOperation extends HttpServlet {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 8826861146379330737L;
	private Logger logger = Logger.getLogger(FileOperation.class);

	static private ExecutorService executor = Executors.newFixedThreadPool(10);
	
	public enum Method {
		THUMBNAIL, PIC, VIDEO, RECORD, MONGODB, LOCAL, NOVALUE;
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
		String requestURL = req.getRequestURI().substring(req.getContextPath().length());
		String[] inputParams = requestURL.toString().split("/");
		String method = inputParams[2];
		
		Method m = Method.toMethod(method.toUpperCase());
		switch (m) {
		case PIC:
			file_get_pic( req, resp);
			break;
		case THUMBNAIL:
			file_get_thumbail( req, resp);
			break;	
		case VIDEO:
			file_get_video( req, resp);
			break;
		case RECORD:
		    file_get_record( req, resp);
		    break;
		case MONGODB:
		    file_get_mongodb( req, resp);
		    break;
		case LOCAL:
		    file_get_local( req, resp);
            break;
		default:
			PrintWriter pw = resp.getWriter();
			pw.write("wrong");
			pw.flush();
			pw.close();
			break;
		}
	}
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String requestURL = req.getRequestURI().substring(req.getContextPath().length());
		String[] inputParams = requestURL.toString().split("/");
		String method = inputParams[2];
		
		Method m = Method.toMethod(method.toUpperCase());
		switch (m) {
		case VIDEO:
			file_put_video( req, resp);
			break;
		case MONGODB:
            file_put_mongodb( req, resp);
            break;	
		case LOCAL:
            file_put_local( req, resp);
            break;    
		default:
			PrintWriter pw = resp.getWriter();
			pw.write("wrong");
			pw.flush();
			pw.close();
			break;
		}
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String requestURL = req.getRequestURI().substring(req.getContextPath().length());
		String[] inputParams = requestURL.toString().split("/");
		String method = inputParams[2];
		
		Method m = Method.toMethod(method.toUpperCase());
		switch (m) {
		case PIC:
			file_post_pic( req, resp);
			break;
		case VIDEO:
			file_post_video( req, resp);
			break;
		default:
			PrintWriter pw = resp.getWriter();
			pw.write("wrong");
			pw.flush();
			pw.close();
			break;
		}
	}
	public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i=begin; i<begin+count; i++) bs[i-begin] = src[i];
        return bs;
    }
 
	private void file_get_pic(HttpServletRequest req, HttpServletResponse resp) {
		try {
			HttpSession session = req.getSession(true);// 如果没有该session，则自动创建一个新的
			String access_token = Common.getAccessToken(session);
			//BaeCache baeCache = Common.getBaeCache();
			FileCache filecache = FileCache.getInstance();
			List<byte[]> value =new ArrayList<byte[]>();
			
			logger.log(Level.FATAL, "path="+ req.getRequestURI().substring(req.getContextPath().length()));
			String filename = URLDecoder.decode(req.getRequestURI().substring(req.getContextPath().length()).substring("/file/pic".length()), "UTF-8");
			logger.log(Level.FATAL, "filename:"+filename);
				
			resp.setContentType("image/jpeg");
			resp.setDateHeader("Expires",System.currentTimeMillis() + 24*3600 * 1000);
			
			OutputStream o = resp.getOutputStream();
			
			
			value = (List<byte[]>) filecache.getPic(filename);
			if (value != null) {
				resp.addHeader("memchched", "true");
				for (int i = 0; i < value.size(); i++) {
					byte data[] = value.get(i);
					o.write(data, 0, data.length);
				}
				o.flush();
				o.close();
				return;
			}
			
			Map<String, String> urlparams = new HashMap<String, String>();
            
			urlparams.put("method", "download");
            urlparams.put("access_token", access_token);
            urlparams.put("path", filename);    
                
            String url = "https://d.pcs.baidu.com/rest/2.0/pcs/file?" + HttpUtil.buildQuery(urlparams, "UTF-8");
            
            URL connect = new URL(url.toString());
            URLConnection connection = connect.openConnection();
            
            try {
                connection = reload(connection);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.log(Level.FATAL, "error:", e);
                return;
            }
            
			InputStream is = connection.getInputStream();
			byte[] buf = new byte[1024]; // 32k buffer
			value =new ArrayList<byte[]>();
			
			int nRead = 0;
			while( (nRead=is.read(buf)) != -1 ) {
			    if( nRead == 1024 ){
			    	value.add(buf.clone());
			    }
			    else {
			    	value.add(subBytes(buf, 0 , nRead));
			    }
				
			    o.write(buf, 0, nRead);
			}
			if(filename.length()<180){
				filecache.setPic(filename, value, 600000l);
			}
			o.flush();
			o.close();
		} catch (Exception e) {
			// 异常处理逻辑
			logger.log(Level.FATAL, "error:", e);
			return;
		}
	}
	
	private void file_post_pic(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// 在解析请求之前先判断请求类型是否为文件上传类型
		String filename = URLDecoder.decode(req.getRequestURI().substring(req.getContextPath().length()).substring("/file/pic".length()), "UTF-8");
		
		String[] paramList = filename.split("/");
		String room = "/"+paramList[1]+"/"+paramList[2]+"/"+paramList[3];
		logger.log(Level.FATAL, "room:"+room);
	
		FileCache filecache = FileCache.getInstance();
		
		// 文件上传处理工厂
		FileItemFactory factory = new DiskFileItemFactory();

		// 创建文件上传处理器
		ServletFileUpload upload = new ServletFileUpload(factory);

		// 开始解析请求信息
		List items = null;
		try {
			items = upload.parseRequest(req);
		} catch (FileUploadException e) {
			e.printStackTrace();
		}

		// 对所有请求信息进行判断
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
			FileItem item = (FileItem) iter.next();
			// 信息为普通的格式
			if (item.isFormField()) {

			}
			// 信息为文件格式
			else {
				List<byte[]> value = new ArrayList<byte[]>();
				value.add(item.get());
				filecache.setPic(filename, value, 600000l);
				String accessToken = Common.getAccessToken(null);
				
				Map<String, String> urlparams = new HashMap<String, String>();
				Map<String, Object> params = new HashMap<String, Object>();
				
				urlparams.put("method", "upload");
				urlparams.put("access_token", accessToken);
				urlparams.put("path", filename);	
				String url = "https://pcs.baidu.com/rest/2.0/pcs/file?"+HttpUtil.buildQuery(urlparams, "UTF-8");
				urlparams.clear();
				params.put("file", item.get());
				
				try {
					String response = HttpUtil.uploadFile(url, params);
					logger.log(Level.INFO, "response:"+response);
					JSONObject file = new JSONObject(response);
					file.put("filecache", "from mem");
					filecache.setRoomNewPic(room, file, 300000l);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.log(Level.FATAL, "error:", e);
				}
			}
		}
	}
	private void file_get_thumbail(HttpServletRequest req, HttpServletResponse resp) {
		try {
			HttpSession session = req.getSession(true);// 如果没有该session，则自动创建一个新的
			String access_token = Common.getAccessToken(session);
			//BaeCache baeCache = Common.getBaeCache();
			FileCache filecache = FileCache.getInstance();
			List<byte[]> value =new ArrayList<byte[]>();
			
			String filename = URLDecoder.decode(req.getRequestURI().substring(req.getContextPath().length()).substring("/file/THUMBNAIL".length()), "UTF-8");
			logger.log(Level.INFO, "filename:"+filename);
			
			resp.setContentType("image/jpeg");
			resp.setDateHeader("Expires",System.currentTimeMillis() + 24*3600 * 1000);
			
			OutputStream o = resp.getOutputStream();
			
			value = (List<byte[]>) filecache.getThumbail(filename);
			if (value != null) {
				resp.addHeader("memchched", "true");
				for (int i = 0; i < value.size(); i++) {
					byte data[] = value.get(i);
					o.write(data, 0, data.length);
				}
				o.flush();
				o.close();
				return;
			}
			
			Map<String, String> urlparams = new HashMap<String, String>();
			urlparams.put("method", "generate");
			urlparams.put("access_token", access_token);
			urlparams.put("path", filename);	
			urlparams.put("quality", "100");	
			urlparams.put("width", "320");	
			urlparams.put("height", "240");	
				
			String url = "https://pcs.baidu.com/rest/2.0/pcs/thumbnail?" + HttpUtil.buildQuery(urlparams, "UTF-8");
			
			URL connect = new URL(url.toString());
			URLConnection connection = connect.openConnection();
			InputStream is = connection.getInputStream();
			byte[] buf = new byte[1024]; // 32k buffer
			value =new ArrayList<byte[]>();
			
			int nRead = 0;
			while( (nRead=is.read(buf)) != -1 ) {
			    if( nRead == 1024 ){
			    	value.add(buf.clone());
			    }
			    else {
			    	value.add(subBytes(buf, 0 , nRead));
			    }
				
			    o.write(buf, 0, nRead);
			}
			filecache.setThumbail(filename, value, 600000l);
			
			o.flush();
			o.close();
		} catch (Exception e) {
			// 异常处理逻辑
			logger.log(Level.FATAL, "error:", e);
			return ;
		}
	}
	private void file_get_video(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 在解析请求之前先判断请求类型是否为文件上传类型
        String method = req.getMethod();
        String filename = URLDecoder.decode(req.getRequestURI().substring(req.getContextPath().length()).substring("/file/video".length()), "UTF-8");
        String[] list = filename.toString().split("/");
        logger.log(Level.INFO, "filename:" + filename + " method:" + method);
        String formate = filename.substring(filename.lastIndexOf('.')+1);
        
        if(formate.equalsIgnoreCase("ts")){
            resp.setContentType("video/MP2T");
            resp.setDateHeader("Expires",System.currentTimeMillis() + 3600*1000);
        }
        else if(formate.equalsIgnoreCase("m3u8")){
            resp.setContentType("application/x-mpegURL");
            resp.setDateHeader("Expires",System.currentTimeMillis() + 5*1000);
        }
        else
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String[] list2 = list[1].split("-");
        if(list2.length<3)
        {
            filename = Common.getDay() + filename;
        }
        
        filename = req.getContextPath() + "/live/" + filename;
                
        OutputStream os = null;
        try{
            os = resp.getOutputStream();
            if(!LocalFS.get_file(filename, os, formate.equalsIgnoreCase("ts"), formate.equalsIgnoreCase("ts")))
            {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        catch  (Exception e) {
            logger.log(Level.FATAL, "error:", e);
            return ;
        }
        finally{
            if(os!=null)
            {
                os.close();
                os = null;
            }
        }       
    }
	private void file_get_video2(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// 在解析请求之前先判断请求类型是否为文件上传类型
		String method = req.getMethod();
		String filename = URLDecoder.decode(req.getRequestURI().substring(req.getContextPath().length()).substring("/file/video".length()), "UTF-8");
		logger.log(Level.INFO, "filename:" + filename + " method:" + method);
		FileCache filecache = FileCache.getInstance();
		String formate = filename.substring(filename.lastIndexOf('.')+1);
		List<byte[]> value = null;
		if(formate.equalsIgnoreCase("ts")){
		    resp.setContentType("video/MP2T");
		    value = (List<byte[]>) filecache.getVideo(filename);
		}
		else if(formate.equalsIgnoreCase("m3u8")){
		    resp.setContentType("application/x-mpegURL");
		    value = (List<byte[]>) filecache.getM3U8(filename);
		}
		else{
		    resp.setContentType("application/octet-stream");
		    value = (List<byte[]>) filecache.getVideo(filename);
		}
		    
		resp.setDateHeader("Expires",System.currentTimeMillis() + 5*1000);
		
		if (value == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else {
		    int len = 0;
		    for (int i = 0; i < value.size(); i++) {
                byte data[] = value.get(i);
                len +=data.length;
            }
		    resp.setContentLength(len);
			OutputStream o = resp.getOutputStream();
			for (int i = 0; i < value.size(); i++) {
				byte data[] = value.get(i);
				o.write(data, 0, data.length);
			}
			o.flush();
			o.close();
		}
	}
	private String get_video_record(String filename){
	    String[] list = filename.toString().split("/");
	    String result;
	    
	    String sql = "select filename, duration from  livevideo WHERE date='"+list[1]+"' and room='"+list[2]+"'";

	    logger.log(Level.INFO, "sql:"+sql);
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            // 具体的数据库操作逻辑
            connection = Common.getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            result  = "#EXTM3U\n";
            result += "#EXT-X-VERSION:3\n";
            result += "#EXT-X-TARGETDURATION:10\n";
            result += "#EXT-X-MEDIA-SEQUENCE:0\n";
             
            while(rs.next()){
                result += "#EXTINF:"+  rs.getInt("duration") +",\n";
                result += rs.getNString("filename") +"\n";
            }
            result += "#EXT-X-ENDLIST\n";
            return result;
        } catch (Exception e) {
            // 异常处理逻辑
            logger.log(Level.FATAL, "error:", e);
            return "";
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                logger.log(Level.FATAL, "error:", e);
                return "";
            }
        }
	}
	private void insert_video_record(String filename, int duration){
	    String[] list = filename.toString().split("/");
	    
	    if(duration < 1){
	        return;
	    }
	    
	    Connection connection = null;
        try {
            connection = Common.getConnection();
            
            String sql = "insert into livevideo( date, room, time, filename, duration ) values (CURDATE(),?,CURTIME(),?,?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            
            ps.setNString(1, list[1]);
            ps.setNString(2, list[2]);
            ps.setInt(3, duration);
            
            ps.executeUpdate();
            
            return ;
        } catch (Exception e) {
            // 异常处理逻辑
            logger.log(Level.FATAL, "error "+filename+":", e);
            return ;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                logger.log(Level.FATAL, "error:", e);
                return ;
            }
        }
	}
	private void file_put_video(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// 在解析请求之前先判断请求类型是否为文件上传类型
	    Map<String, String> params = new HashMap<String, String>();
		String method = req.getMethod();
		String filename = URLDecoder.decode(req.getRequestURI().substring(req.getContextPath().length()).substring("/file/video".length()), "UTF-8");
		String[] list = filename.toString().split("/");
		logger.log(Level.INFO, "filename:" + filename + " method:" + method);
		FileCache filecache = FileCache.getInstance();
		String formate = filename.substring(filename.lastIndexOf('.')+1);
		
		List<byte[]> value = new ArrayList<byte[]>();
		
		if(formate.equalsIgnoreCase("m3u8")){
            filecache.setM3U8(filename, value, 60000l);
        }
		
		int duration = req.getIntHeader("x-hls-duration");
        if( duration > 0)
        {
            params.put("duration", ""+duration);
            insert_video_record(filename, req.getIntHeader("x-hls-duration")); 
        }
        params.put("room", list[1]);
        params.put("day", Common.getDay());
        
        filename = req.getContextPath() + "/live/" + Common.getDay() + filename;
        
        InputStream is = null;
        try{
            is = req.getInputStream();
            if(!LocalFS.save_file("video", filename, is, params, formate.equalsIgnoreCase("ts"))){
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }                
        }
        catch  (Exception e) {
            logger.log(Level.FATAL, "error:", e);
            return ;
        }
        finally{
            if(is!=null)
            {
                is.close();
                is = null;
            }
        }
	}
	
	private void file_put_mongodb(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 在解析请求之前先判断请求类型是否为文件上传类型
	    Map<String, String> params = new HashMap<String, String>();
        String method = req.getMethod();
        String filename = URLDecoder.decode(req.getRequestURI().substring(req.getContextPath().length()).substring("/file/mongodb".length()), "UTF-8");
        String[] list = filename.toString().split("/");
        logger.log(Level.INFO, "filename:" + filename + " method:" + method);
        String formate = filename.substring(filename.lastIndexOf('.')+1);
        
        int duration = req.getIntHeader("x-hls-duration");
        if( duration > 0)
        {
            params.put("duration", ""+duration);
        }
        params.put("room", list[0]);
        params.put("day", Common.getDay());
        
        InputStream is = req.getInputStream();
        
        if(!MongodbOpt.save_file("/" + Common.getDay() + filename, is, params))
        {
            logger.log(Level.FATAL, "mongo.save_file "+filename+" error");
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }    
    }
	private void file_get_mongodb(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 在解析请求之前先判断请求类型是否为文件上传类型
        String method = req.getMethod();
        String filename = URLDecoder.decode(req.getRequestURI().substring(req.getContextPath().length()).substring("/file/mongodb".length()), "UTF-8");
        String[] list = filename.toString().split("/");
        logger.log(Level.INFO, "filename:" + filename + " method:" + method);
        String formate = filename.substring(filename.lastIndexOf('.')+1);
        
        if(formate.equalsIgnoreCase("ts")){
            resp.setContentType("video/MP2T");
            resp.setDateHeader("Expires",System.currentTimeMillis() + 3600*1000);
        }
        else if(formate.equalsIgnoreCase("m3u8")){
            resp.setContentType("application/x-mpegURL");
            resp.setDateHeader("Expires",System.currentTimeMillis() + 5*1000);
        }
        else
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String[] list2 = list[0].split("-");
        if(list2.length<3)
        {
            filename = "/" + Common.getDay() + filename;
        }
        
        OutputStream os = resp.getOutputStream();
        
        if(!MongodbOpt.get_file(filename, os))
        {
            logger.log(Level.FATAL, "mongo.get_file "+filename+" error");
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }    
    }
	private void insert_video_local(String filename, int duration){
        String[] list = filename.toString().split("/");
        
        if(duration < 1){
            return;
        }
        
        Connection connection = null;
        try {
            connection = Common.getConnection();
            
            String sql = "insert into livevideo( date, room, time, filename, duration ) values (CURDATE(),?,CURTIME(),?,?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            
            ps.setNString(1, list[1]);
            ps.setNString(2, list[2]);
            ps.setInt(3, duration);
            
            ps.executeUpdate();
            
            return ;
        } catch (Exception e) {
            // 异常处理逻辑
            logger.log(Level.FATAL, "error "+filename+":", e);
            return ;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                logger.log(Level.FATAL, "error:", e);
                return ;
            }
        }
    }
	private void file_put_local(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 在解析请求之前先判断请求类型是否为文件上传类型
        Map<String, String> params = new HashMap<String, String>();
        String method = req.getMethod();
        String filename = URLDecoder.decode(req.getRequestURI().substring(req.getContextPath().length()).substring("/file/local".length()), "UTF-8");
        String[] list = filename.toString().split("/");
        logger.log(Level.INFO, "filename:" + filename + " method:" + method);
        String formate = filename.substring(filename.lastIndexOf('.')+1);
        
        int duration = req.getIntHeader("x-hls-duration");
        if( duration > 0)
        {
            params.put("duration", ""+duration);
        }
        params.put("room", list[1]);
        params.put("day", Common.getDay());
        
        filename = req.getContextPath() + "/live/" + Common.getDay() + filename;
        
        InputStream is = null;
        try{
            is = req.getInputStream();
            if(!LocalFS.save_file("video", filename, is, params, formate.equalsIgnoreCase("ts"))){
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }                
        }
        catch  (Exception e) {
            logger.log(Level.FATAL, "error:", e);
            return ;
        }
        finally{
            if(is!=null)
            {
                is.close();
                is = null;
            }
        }
    }
    private void file_get_local(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 在解析请求之前先判断请求类型是否为文件上传类型
        String method = req.getMethod();
        String filename = URLDecoder.decode(req.getRequestURI().substring(req.getContextPath().length()).substring("/file/local".length()), "UTF-8");
        String[] list = filename.toString().split("/");
        logger.log(Level.INFO, "filename:" + filename + " method:" + method);
        String formate = filename.substring(filename.lastIndexOf('.')+1);
        
        if(formate.equalsIgnoreCase("ts")){
            resp.setContentType("video/MP2T");
            resp.setDateHeader("Expires",System.currentTimeMillis() + 3600*1000);
        }
        else if(formate.equalsIgnoreCase("m3u8")){
            resp.setContentType("application/x-mpegURL");
            resp.setDateHeader("Expires",System.currentTimeMillis() + 5*1000);
        }
        else
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String[] list2 = list[1].split("-");
        if(list2.length<3)
        {
            filename = Common.getDay() + filename;
        }
        
        filename = req.getContextPath() + "/live/" + filename;
                
        OutputStream os = null;
        try{
            os = resp.getOutputStream();
            if(!LocalFS.get_file(filename, os, formate.equalsIgnoreCase("ts"), formate.equalsIgnoreCase("ts")))
            {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        catch  (Exception e) {
            logger.log(Level.FATAL, "error:", e);
            return ;
        }
        finally{
            if(os!=null)
            {
                os.close();
                os = null;
            }
        }       
    }
	private void file_post_video(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// 在解析请求之前先判断请求类型是否为文件上传类型
		String method = req.getMethod();
		String filename = URLDecoder.decode(req.getRequestURI().substring(req.getContextPath().length()).substring("/file/video".length()), "UTF-8");
		logger.log(Level.INFO, "filename:" + filename + " method:" + method);
		FileCache filecache = FileCache.getInstance();
		String formate = filename.substring(filename.lastIndexOf('.')+1);
		
		// 文件上传处理工厂
		FileItemFactory factory = new DiskFileItemFactory();

		// 创建文件上传处理器
		ServletFileUpload upload = new ServletFileUpload(factory);

		// 开始解析请求信息
		List items = null;
		try {
			items = upload.parseRequest(req);
		} catch (FileUploadException e) {
			e.printStackTrace();
		}

		// 对所有请求信息进行判断
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
			FileItem item = (FileItem) iter.next();
			// 信息为普通的格式
			if (item.isFormField()) {

			}
			// 信息为文件格式
			else {
				List<byte[]> value = new ArrayList<byte[]>();
				value.add(item.get());
				if(formate.equalsIgnoreCase("m3u8")){
		            filecache.setM3U8(filename, value, 60000l);
		        }
		        else{
		            filecache.setVideo(filename, value, 120000l);
		        }   
			}
		}
	}
	
   private static URLConnection reload(URLConnection uc) throws Exception {

        HttpURLConnection huc = (HttpURLConnection) uc;
        
        if (huc.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP 
                || huc.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM)// 302, 301
            return reload(new URL(huc.getHeaderField("location")).openConnection());
        
        return uc;
    }
	private void file_get_record2(HttpServletRequest req, HttpServletResponse resp) throws IOException  {
	    String filename = URLDecoder.decode(req.getRequestURI().substring(req.getContextPath().length()).substring("/file/record".length()), "UTF-8");
	    logger.log(Level.INFO, "file_get_record filename:" + filename);
	    String formate = filename.substring(filename.lastIndexOf('.')+1);
        List<byte[]> value = null;
        if(formate.equalsIgnoreCase("ts")){
            resp.setContentType("video/MP2T");
            HttpSession session = req.getSession(true);// 如果没有该session，则自动创建一个新的
            String access_token = Common.getAccessToken(session);
            resp.setContentType("application/octet-stream");
            Map<String, String> urlparams = new HashMap<String, String>();
            urlparams.put("method", "download");
            urlparams.put("access_token", access_token);
            urlparams.put("path", "/apps/yiqingart/live"+filename);    
                
            String url = "https://d.pcs.baidu.com/rest/2.0/pcs/file?" + HttpUtil.buildQuery(urlparams, "UTF-8");
            OutputStream o = resp.getOutputStream();
            
            URL connect = new URL(url.toString());
            URLConnection connection = connect.openConnection();
            
            try {
                connection = reload(connection);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.log(Level.FATAL, "error:", e);
                return;
            }
                        
            InputStream is = connection.getInputStream();
            byte[] buf = new byte[1024]; // 32k buffer
            value =new ArrayList<byte[]>();
            
            int nRead = 0;
            while( (nRead=is.read(buf)) != -1 ) {
                if( nRead == 1024 ){
                    value.add(buf.clone());
                }
                else {
                    value.add(subBytes(buf, 0 , nRead));
                }
                
                o.write(buf, 0, nRead);
            }
            
            is.close();
            o.flush();
            o.close();
        }
        else if(formate.equalsIgnoreCase("m3u8")){
            resp.setContentType("application/x-mpegURL");
            PrintWriter pw = resp.getWriter();
            
            pw.write(get_video_record(filename));
            pw.flush();
            pw.close(); 
            get_video_record(filename);
        }
        else{
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
	}
	private void file_get_record(HttpServletRequest req, HttpServletResponse resp) throws IOException  {
        String filename = URLDecoder.decode(req.getRequestURI().substring(req.getContextPath().length()).substring("/file/record".length()), "UTF-8");
        logger.log(Level.INFO, "file_get_record filename:" + filename);
        String formate = filename.substring(filename.lastIndexOf('.')+1);
        if(formate.equalsIgnoreCase("ts")){
            filename = req.getContextPath() + "/live/" + filename;
            
            OutputStream os = null;
            try{
                os = resp.getOutputStream();
                if(!LocalFS.get_file(filename, os, formate.equalsIgnoreCase("ts"), formate.equalsIgnoreCase("ts")))
                {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
            catch  (Exception e) {
                logger.log(Level.FATAL, "error:", e);
                return ;
            }
            finally{
                if(os!=null)
                {
                    os.close();
                    os = null;
                }
            }    
        }
        else if(formate.equalsIgnoreCase("m3u8")){
            resp.setContentType("application/x-mpegURL");
            PrintWriter pw = resp.getWriter();
            
            pw.write(get_video_record(filename));
            pw.flush();
            pw.close(); 
            get_video_record(filename);
        }
        else{
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
