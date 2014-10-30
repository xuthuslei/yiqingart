package com.yiqingart.www.fs;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.yiqingart.www.Common;
import com.yiqingart.www.HttpUtil;
import com.yiqingart.www.UploadThread;
public class NetDiskCache extends CacheOperation {

    static private ExecutorService executor = Executors.newFixedThreadPool(15);
    static private Logger logger = Logger.getLogger("fs");
    @Override
    public Boolean do_save(String filename, List<byte[]> value) {
        if(!IsSave())return true;
        logger.info("NetDiskCache save "+filename); 
        Runnable worker = new UploadThread( value, filename);
        executor.execute(worker);
        // TODO Auto-generated method stub
        return true;
    }
    public NetDiskCache(CacheOperation next) {
        super(next, true);
    }
    public NetDiskCache(CacheOperation next, Boolean save) {
        super(next, save);
    }
    public List<byte[]> do_get(String filename, OutputStream os ) {
        logger.info("NetDiskCache get "+filename); 
        InputStream is = null;
        try {
            String access_token = Common.getAccessToken(null);
            Map<String, String> urlparams = new HashMap<String, String>();
            urlparams.put("method", "download");
            urlparams.put("access_token", access_token);
            if(filename.indexOf("/apps/yiqingart") == 0)
            {
                urlparams.put("path", filename);    
            }
            else
            {
                urlparams.put("path", "/apps/yiqingart"+filename);   
            }
                
            String url = "https://pcs.baidu.com/rest/2.0/pcs/file?" + HttpUtil.buildQuery(urlparams, "UTF-8");
              
            URL connect = new URL(url.toString());
            URLConnection connection = connect.openConnection();
    
            connection = Common.reload(connection);
                    
            is = connection.getInputStream();
            
            List<byte[]> value = Common.readIs(is,os);
            is.close();
            return value;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.log(Level.FATAL, "NetDiskCache get "+filename+" error:", e);
            return null;
        }
    }
}
