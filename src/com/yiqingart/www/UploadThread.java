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
    private Logger logger = Logger.getLogger("UploadThread");
    
    public UploadThread(String localFilename, String remoteFilename) {
        this.localFilename = localFilename;
        this.remoteFilename = remoteFilename;
    }
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i=begin; i<begin+count; i++) bs[i-begin] = src[i];
        return bs;
    }
    @Override
    public void run() {
        // TODO Auto-generated method stub
        logger.log(Level.WARNING, "UploadThread file:"+ localFilename +" is runing");
        File file = new File("/home/bae" + localFilename);
        FileInputStream is;
        byte[] buf = new byte[1024]; // 32k buffer
        List<byte[]> value = new ArrayList<byte[]>();
              
        try {
            if(file==null||!file.exists())
            {           
                logger.log(Level.SEVERE, "file:"+ localFilename +" not found!!");
                return;
            }
            is = new FileInputStream(file);
            
            int nRead = 0;
            while ((nRead = is.read(buf)) != -1) {
                if (nRead == 1024) {
                    value.add(buf.clone());
                } else {
                    value.add(subBytes(buf, 0, nRead));
                }
            }
            
            is.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.log(Level.SEVERE, "error:", e);
            return;
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
                return;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.log(Level.SEVERE, "error:", e);
            }
        }
    }

}
