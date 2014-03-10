package com.yiqingart.www;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.baidu.bae.api.memcache.BaeCache;
import com.yiqingart.www.Acra.Method;

public class FileOperation extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8826861146379330737L;
	private Logger logger = Logger.getLogger("FileOperation");

	public enum Method {
		GET_PIC, PUT_PIC, THUMBNAIL, NOVALUE;
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
		case GET_PIC:
			get_pic( req, resp);
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
	private void get_pic(HttpServletRequest req, HttpServletResponse resp) {
		try {
			HttpSession session = req.getSession(true);// 如果没有该session，则自动创建一个新的
			String access_token = Common.getAccessToken(session);
			BaeCache baeCache = Common.getBaeCache();
			List<byte[]> value =new ArrayList<byte[]>();
			
			String key = req.getQueryString();
			
			resp.setContentType("image/jpeg");
			resp.setDateHeader("Expires",System.currentTimeMillis() + 24*3600 * 1000);
			
			OutputStream o = resp.getOutputStream();
			
			if (key.length() < 180) {
				value = (List<byte[]>) baeCache.get(key);
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
			
			String url = "https://pcs.baidu.com/rest/2.0/pcs/thumbnail?access_token="+access_token + "&" +  req.getQueryString();
			
			
			URL connect = new URL(url.toString());
			URLConnection connection = connect.openConnection();
			InputStream is = connection.getInputStream();
			byte[] buf = new byte[1024]; // 32k buffer
			value =new ArrayList<byte[]>();
			
			int nRead = 0;
			while( (nRead=is.read(buf)) != -1 ) {
				if(key.length()<180){
				    if( nRead == 1024 ){
				    	value.add(buf.clone());
				    }
				    else {
				    	value.add(subBytes(buf, 0 , nRead));
				    }
				}
			    o.write(buf, 0, nRead);
			}
			if(key.length()<180){
				baeCache.add(key, value, 600000);
			}
			o.flush();
			o.close();
		} catch (Exception e) {
			// 异常处理逻辑
			logger.log(Level.SEVERE, "error:", e);
			return ;
		}
	}
}
