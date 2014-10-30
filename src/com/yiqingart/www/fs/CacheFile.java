package com.yiqingart.www.fs;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;

import com.yiqingart.www.Common;

public class CacheFile {
    static private Logger logger = Logger.getLogger("fs");
    private CacheOperation firstopt;

    public CacheFile(CacheOperation opter) {
        this.firstopt = opter;
    }
    
//    public static byte[] subBytes(byte[] src, int begin, int count) {
//        byte[] bs = new byte[count];
//        for (int i=begin; i<begin+count; i++) bs[i-begin] = src[i];
//        return bs;
//    }   
    

    public Boolean save( String filename, InputStream is) {
        try {
            logger.info("save "+filename);
            List<byte[]> value = Common.readIs(is);
            
            if(value == null){
                logger.info("readIs "+filename+" fail");
                return false;
            }
            
            CacheOperation cur = firstopt;
            
            while(cur != null)
            {
                if(!cur.do_save(filename, value))
                {
                    return false;
                }
                cur = cur.getNext();
            }
            
            return true;
        }catch  (Exception e) {
            logger.fatal("error:", e);
            return false;
        }
    }
    
    public Boolean get(String filename,  OutputStream os  ) {
        try{
            logger.info("get "+filename);
            CacheOperation read = firstopt;
            List<byte[]> value = null;
            while(read != null)
            {
                value = read.do_get(filename, os);
                if(value != null)
                {
                    break;
                }
                read = read.getNext();
            }
            
            if(value == null){
                logger.info("get  "+filename+" fail");
                return false;
            }
            
    //        for (int i = 0; i < value.size(); i++) {
    //            byte data[] = value.get(i);
    //            os.write(data, 0, data.length);
    //        }
            
            CacheOperation write = firstopt;
            
            while(write != null && write != read )
            {
                write.do_save(filename, value);
                write = write.getNext();
            }
            return true;
        }catch  (Exception e) {
            logger.fatal("error:", e);
            return false;
        }
    }    
}
