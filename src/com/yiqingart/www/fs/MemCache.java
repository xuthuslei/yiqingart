package com.yiqingart.www.fs;

import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.yiqingart.www.FileCache;

public class MemCache extends CacheOperation {
    static private Logger logger = Logger.getLogger("fs");
    
    public enum TYPE {PIC, VIDEO};
    private TYPE type;
    public MemCache(TYPE type, CacheOperation next, Boolean save) {
        super(next, save);
        this.type = type;
    }
    public MemCache(TYPE type, CacheOperation next) {
        super(next, true);
        this.type = type;
    }
      @Override
    public Boolean do_save(String filename, List<byte[]> value) {
        logger.info("MemCache save "+filename); 
        if(!IsSave())return true;
        FileCache filecache = FileCache.getInstance();
        switch(type){
            case PIC:                
                filecache.setPic(filename, value, 600000l);
                break;
            case VIDEO:
                filecache.setVideo(filename, value, 60000l);
                break;
             default:
                return false; 
        }
        return true;
    }

    @Override
    public List<byte[]> do_get(String filename, OutputStream os ) {
        logger.info("MemCache get "+filename); 
        FileCache filecache = FileCache.getInstance();
        List<byte[]> value = null;
        switch(type){
            case PIC:                
                value =  filecache.getPic(filename);
                break;
            case VIDEO:
                value =  filecache.getVideo(filename);
                break;
             default:   
                 return null; 
        }
        if(value == null){
            logger.info("MemCache get"+filename+" fail");
            return null;
        }
        try {
            for (int i = 0; i < value.size(); i++) {
                byte data[] = value.get(i);
                os.write(data, 0, data.length);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.log(Level.FATAL, "MemCache get "+filename+"error:", e);
            return null;
        }
        return value;
    }
}
