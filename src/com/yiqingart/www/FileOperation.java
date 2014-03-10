package com.yiqingart.www;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import com.baidu.bae.api.memcache.BaeCache;
import com.yiqingart.www.Acra.Method;

public class FileOperation extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8826861146379330737L;
	private Logger logger = Logger.getLogger("FileOperation");

	public enum Method {
		THUMBNAIL, PIC, VIDEO, NOVALUE;
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
		case PIC:
			file_pic( req, resp);
			break;
		case THUMBNAIL:
			file_thumbail( req, resp);
			break;	
		case VIDEO:
			file_video( req, resp);
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
 
	@SuppressWarnings("unchecked")
	private void file_pic(HttpServletRequest req, HttpServletResponse resp) {
		try {
			HttpSession session = req.getSession(true);// 如果没有该session，则自动创建一个新的
			String access_token = Common.getAccessToken(session);
			BaeCache baeCache = Common.getBaeCache();
			List<byte[]> value =new ArrayList<byte[]>();
			
			logger.log(Level.SEVERE, "path="+ req.getRequestURI());
			String filename = req.getRequestURI().substring("/file/pic/".length());
			logger.log(Level.SEVERE, "filename:"+filename);
				
			resp.setContentType("image/jpeg");
			resp.setDateHeader("Expires",System.currentTimeMillis() + 24*3600 * 1000);
			
			OutputStream o = resp.getOutputStream();
			
			if (filename.length() < 180) {
				value = (List<byte[]>) baeCache.get(filename);
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
			}
			
			String url = "https://pcs.baidu.com/rest/2.0/pcs/thumbnail?access_token="+access_token + "&method=generate&path="+filename+"&quality=100&width=1600&height=1200";
			
			
			URL connect = new URL(url.toString());
			URLConnection connection = connect.openConnection();
			InputStream is = connection.getInputStream();
			byte[] buf = new byte[1024]; // 32k buffer
			value =new ArrayList<byte[]>();
			
			int nRead = 0;
			while( (nRead=is.read(buf)) != -1 ) {
				if(filename.length()<180){
				    if( nRead == 1024 ){
				    	value.add(buf.clone());
				    }
				    else {
				    	value.add(subBytes(buf, 0 , nRead));
				    }
				}
			    o.write(buf, 0, nRead);
			}
			if(filename.length()<180){
				baeCache.add(filename, value, 600000);
			}
			o.flush();
			o.close();
		} catch (Exception e) {
			// 异常处理逻辑
			logger.log(Level.SEVERE, "error:", e);
			return ;
		}
	}
	@SuppressWarnings("unchecked")
	private void file_thumbail(HttpServletRequest req, HttpServletResponse resp) {
		try {
			HttpSession session = req.getSession(true);// 如果没有该session，则自动创建一个新的
			String access_token = Common.getAccessToken(session);
			BaeCache baeCache = Common.getBaeCache();
			List<byte[]> value =new ArrayList<byte[]>();
			
			String filename = req.getRequestURI().substring("/file/THUMBNAIL/".length());
			logger.log(Level.INFO, "filename:"+filename);
			
			resp.setContentType("image/jpeg");
			resp.setDateHeader("Expires",System.currentTimeMillis() + 24*3600 * 1000);
			
			OutputStream o = resp.getOutputStream();
			
			if (filename.length() < 180) {
				value = (List<byte[]>) baeCache.get("thu_"+ filename);
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
			}
			
			String url = "https://pcs.baidu.com/rest/2.0/pcs/thumbnail?access_token="+access_token + "&method=generate&path="+filename+"&quality=100&width=320&height=240";
			
			
			URL connect = new URL(url.toString());
			URLConnection connection = connect.openConnection();
			InputStream is = connection.getInputStream();
			byte[] buf = new byte[1024]; // 32k buffer
			value =new ArrayList<byte[]>();
			
			int nRead = 0;
			while( (nRead=is.read(buf)) != -1 ) {
				if(filename.length()<180){
				    if( nRead == 1024 ){
				    	value.add(buf.clone());
				    }
				    else {
				    	value.add(subBytes(buf, 0 , nRead));
				    }
				}
			    o.write(buf, 0, nRead);
			}
			if(filename.length()<180){
				baeCache.add("thu_"+filename, value, 600000);
			}
			o.flush();
			o.close();
		} catch (Exception e) {
			// 异常处理逻辑
			logger.log(Level.SEVERE, "error:", e);
			return ;
		}
	}
	private void file_video(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// 在解析请求之前先判断请求类型是否为文件上传类型
		String method = req.getMethod();
        String filename = req.getRequestURI().substring("/file/video/".length());
		logger.log(Level.INFO, "filename:"+filename+" method:"+method);

		if( method.equalsIgnoreCase("POST")){
	        // 文件上传处理工厂
	        FileItemFactory factory = new DiskFileItemFactory();
	
	        // 创建文件上传处理器
	        ServletFileUpload upload = new ServletFileUpload(factory);
	
	        // 开始解析请求信息
	        List items = null;
	        try {
	            items = upload.parseRequest(req);
	        }
	        catch (FileUploadException e) {
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
	            	BaeCache baeCache = Common.getBaeCache();
	            	baeCache.add(filename, item, 600000);
	            }
	        }
		}
		else {
			BaeCache baeCache = Common.getBaeCache();
			FileItem item = (FileItem) baeCache.get(filename);
			
			if( item == null){
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			else {
				OutputStream o = resp.getOutputStream();
				o.write(item.get());
				o.flush();
				o.close();
			}
		}
	}
	
}
