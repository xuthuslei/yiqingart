package com.yiqingart.www.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;


import com.yiqingart.www.Common;


public class LocalCache extends CacheOperation {
    static private Logger logger = Logger.getLogger("fs");
    
    public LocalCache(CacheOperation next, Boolean save) {
        super(next, save);
    }
    public LocalCache(CacheOperation next) {
        super(next, true);
    }

     public Boolean do_save(String filename, List<byte[]> value) {
        logger.info("LocalCache save "+filename); 
        if(!IsSave())return true;
        try {
            File file = new File("/home/bae" + filename); 
            File parent = file.getParentFile(); 
            if(parent!=null&&!parent.exists()){ 
                parent.mkdirs(); 
            } 
            FileOutputStream os;
            os = new FileOutputStream(file);
            
            for (int i = 0; i < value.size(); i++) {
                byte data[] = value.get(i);
                if(data.length > 0) os.write(data, 0, data.length);
            }
            
            os.close();
        } catch (Exception e) {
            logger.info("LocalCache save "+filename+" fail"); 
            return false;
        }        
        
        return true;
    }

    

    public List<byte[]> do_get(String filename, OutputStream os ) {
        logger.info("LocalCache get "+filename); 
        try {
            File file = new File("/home/bae" + filename); 
            if(file!=null&&!file.exists()){ 
                logger.info("LocalCache get "+filename+" fail"); 
                return null;
            } 
            FileInputStream is = new FileInputStream(file); 
            
            List<byte[]> value = Common.readIs(is,os);
            
            is.close();
            return value;
        } catch (Exception e) {
            logger.info("LocalCache get "+filename+" error",e); 
            return null;
        }  
    }
}
