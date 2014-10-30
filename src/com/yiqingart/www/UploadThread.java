package com.yiqingart.www;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UploadThread implements Runnable {

    private String localFilename;
    private String remoteFilename;
    private Boolean deleteLater = false;
    private List<byte[]> value = null;
    private Logger logger = Logger.getLogger("UploadThread");
    
    public UploadThread(String localFilename, String remoteFilename) {
        this.localFilename = localFilename;
        this.remoteFilename = remoteFilename;
        this.deleteLater = false;
    }
    
    public UploadThread(List<byte[]> value, String remoteFilename) {
        this.localFilename = null;
        this.value  = value;
        this.remoteFilename = remoteFilename;
        this.deleteLater = false;        
    }
    public UploadThread(String localFilename, String remoteFilename,Boolean deleteLater) {
        this.localFilename = localFilename;
        this.remoteFilename = remoteFilename;
        this.deleteLater = deleteLater;
    }
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i=begin; i<begin+count; i++) bs[i-begin] = src[i];
        return bs;
    }
    @Override
    public void run() {
        // TODO Auto-generated method stub
        File file = null;
        logger.log(Level.WARNING, "UploadThread file:"+ localFilename +" is runing");
        if(value == null)
        {
            file = new File("/home/bae" + localFilename);
            FileInputStream is;
            value = new ArrayList<byte[]>();
                  
            try {
                
                is = new FileInputStream(file);
                
                value = Common.readIs(is);
                
                is.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.log(Level.SEVERE, "error:", e);
                return;
            }
        }
        
        int count = 0;
        while(count < 3) 
        {
            count++;               
        
            String accessToken = Common.getAccessToken(null);
             
            Map<String, String> urlparams = new HashMap<String, String>();
            Map<String, Object> params = new HashMap<String, Object>();
            
            urlparams.put("method", "upload");
            urlparams.put("access_token", accessToken);
            urlparams.put("path", "/apps/yiqingart"+remoteFilename);    
            //String url = "https://c.pcs.baidu.com/rest/2.0/pcs/file?"+HttpUtil.buildQuery(urlparams, "UTF-8");
            String url = "https://pcs.baidu.com/rest/2.0/pcs/file?"+HttpUtil.buildQuery(urlparams, "UTF-8");
            logger.log(Level.INFO, "url:"+url);
            urlparams.clear();
            
            params.put("file", value);
            
            try {
                String response = HttpUtil.uploadFile(url, params);
                logger.log(Level.INFO, "response:"+response);
                if(deleteLater&&file!=null)
                {
                    file.delete();
                }
                return;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.log(Level.SEVERE, "error:", e);
            }
        }
    }

}
