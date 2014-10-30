package com.yiqingart.www;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class LocalFS {
    static private Logger logger = Logger.getLogger(LocalFS.class);
    static private ExecutorService executor = Executors.newFixedThreadPool(15);
    
    public enum TYPE_CACHE {PIC, VIDEO, NOCAHCE};
    public enum TYPE_GET {MEM_LOCAL, MEM_REMOTE, MEM_LOCAL_REMOTE, LOCAL_REMOTE};
    public enum TYPE_SAVE {MEM_LOCAL, MEM_REMOTE, MEM_LOCAL_REMOTE};
    private static MongoClient getMongoClient()
    {
        try {
            
            String serverName = Common.BAE_MONGODB_HOST + ":" + Common.BAE_MONGODB_PORT;
            
            MongoClient mongoClient = new MongoClient(new ServerAddress(serverName), 
                    Arrays.asList(MongoCredential.createMongoCRCredential(Common.BAE_API_KEY, Common.BAE_MONGODB_DB,Common.BAE_SECRET_KEY.toCharArray())), 
                    new MongoClientOptions.Builder().cursorFinalizerEnabled(false).build());
        
            
            return mongoClient;
        }
        catch(Exception e) {
            logger.log(Level.FATAL, "save_file error:", e);
            return null;
        }
    }
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i=begin; i<begin+count; i++) bs[i-begin] = src[i];
        return bs;
    }
    private static Boolean cache_set(TYPE_CACHE cacheType, String key, List<byte[]> value)
    {
        FileCache filecache = FileCache.getInstance();
        switch(cacheType){
            case PIC:                
                filecache.setPic(key, value, 600000l);
                break;
            case VIDEO:
                filecache.setVideo(key, value, 60000l);
                break;
            case NOCAHCE:
                break;
             default:
                logger.log(Level.FATAL, "cache_set type "+cacheType+ " error");
                return false; 
        }
        return true;
    }
    private static List<byte[]> cache_get(TYPE_CACHE cacheType,String key) {
        FileCache filecache = FileCache.getInstance();
        switch(cacheType){
            case PIC:                
                return filecache.getPic(key);
            case VIDEO:
                return filecache.getVideo(key);
            case NOCAHCE:
                break;
             default:
                logger.log(Level.FATAL, "cache_set type "+cacheType+ " error");                
        }
        return null; 
    }
    public static Boolean save_file(String collectionName, String filename, TYPE_CACHE cacheType, InputStream is, Map<String, String> parameters, TYPE_SAVE save_type )
    {
        MongoClient mc = null;
        try {
            File file = new File("/home/bae" + filename); 
            File parent = file.getParentFile(); 
            if(parent!=null&&!parent.exists()){ 
                parent.mkdirs(); 
            } 
            FileOutputStream os = new FileOutputStream(file); 
            
            byte[] buf = new byte[1024]; // 32k buffer
            List<byte[]> value = new ArrayList<byte[]>();
            
            int nRead = 0, count = 0;
            while ((nRead = is.read(buf)) != -1) {
                count += nRead;
                os.write(buf, 0, nRead);   
                if (nRead == 1024) {
                    value.add(buf.clone());
                } else {
                    value.add(subBytes(buf, 0, nRead));
                }
            }
            
            os.close();
            is.close();
            
            if(count == 0)
            {
                logger.log(Level.WARN, "save file "+filename+" for netdisk null");
                file.delete();
                return false;
            }
            
            cache_set(cacheType, filename, value);
            
            mc = getMongoClient();
        
            DB mongoDB = mc.getDB(Common.BAE_MONGODB_DB);
            mongoDB.authenticate(Common.BAE_API_KEY, Common.BAE_SECRET_KEY.toCharArray());
            
            DBCollection collection = mongoDB.getCollection(collectionName);
            
            BasicDBObject document = new BasicDBObject();
            document.put("filename", filename);
            
            collection.remove(document);
            
            if (parameters != null) {
                for (Entry<String, String> entry : parameters.entrySet()) {
                    String key = entry.getKey();
                    String paramValue = entry.getValue();
                    document.put(key, paramValue);
                }
            }   
            //将新建立的document保存到collection中去
            collection.insert(document);
            if(save_type != TYPE_SAVE.MEM_LOCAL)
            {
                Runnable worker = new UploadThread(filename, filename, save_type == TYPE_SAVE.MEM_REMOTE?true:false);
                executor.execute(worker);
            }
            return true;
         }
        catch (Exception e) {
            logger.log(Level.FATAL, "save_file error:", e);
            return false;
        }
        finally{
            if(mc != null){
                mc.close();
            }
        }
        
    }
    public static Boolean save_file(String collectionName, String filename, TYPE_CACHE cacheType, List<byte[]> value, Map<String, String> parameters, TYPE_SAVE save_type)
    {
        MongoClient mc = null;
        try {
            File file = new File("/home/bae" + filename); 
            File parent = file.getParentFile(); 
            if(parent!=null&&!parent.exists()){ 
                parent.mkdirs(); 
            } 
            FileOutputStream os = new FileOutputStream(file); 
            
            int count = 0;
            for (int i = 0; i < value.size(); i++) {
                byte data[] = value.get(i);
                count += data.length;
                if(data.length > 0) os.write(data, 0, data.length);
            }
            
            os.close();
            
            if(count == 0)
            {
                logger.log(Level.WARN, "save file "+filename+" for netdisk null");
                file.delete();
                return false;
            }
            
            cache_set(cacheType, filename, value);
            
            mc = getMongoClient();
        
            DB mongoDB = mc.getDB(Common.BAE_MONGODB_DB);
            mongoDB.authenticate(Common.BAE_API_KEY, Common.BAE_SECRET_KEY.toCharArray());
            
            DBCollection collection = mongoDB.getCollection(collectionName);
            
            BasicDBObject document = new BasicDBObject();
            document.put("filename", filename);
            
            collection.remove(document);
            
            if (parameters != null) {
                for (Entry<String, String> entry : parameters.entrySet()) {
                    String key = entry.getKey();
                    String paramValue = entry.getValue();
                    document.put(key, paramValue);
                }
            }   
            //将新建立的document保存到collection中去
            collection.insert(document);
            if(save_type != TYPE_SAVE.MEM_LOCAL)
            {
                Runnable worker = new UploadThread(filename, filename, save_type == TYPE_SAVE.MEM_REMOTE?true:false);
                executor.execute(worker);
            }
            return true;
         }
        catch (Exception e) {
            logger.log(Level.FATAL, "save_file error:", e);
            return false;
        }
        finally{
            if(mc != null){
                mc.close();
            }
        }
        
    }
    public static Boolean get_file_from_netdisk(String filename, OutputStream os, TYPE_CACHE cacheType) throws IOException
    {
        InputStream is = null;
        try {
            String access_token = Common.getAccessToken(null);
            Map<String, String> urlparams = new HashMap<String, String>();
            urlparams.put("method", "download");
            urlparams.put("access_token", access_token);
            urlparams.put("path", "/apps/yiqingart"+filename);    
                
            String url = "https://pcs.baidu.com/rest/2.0/pcs/file?" + HttpUtil.buildQuery(urlparams, "UTF-8");
              
            URL connect = new URL(url.toString());
            URLConnection connection = connect.openConnection();
    
            connection = Common.reload(connection);
                    
            is = connection.getInputStream();
            byte[] buf = new byte[1024]; // 32k buffer
            List<byte[]> value = new ArrayList<byte[]>();
            
            int nRead = 0, count = 0;
            while ((nRead = is.read(buf)) != -1) {
                count += nRead;
                os.write(buf, 0, nRead);   
                if (nRead == 1024) {
                    value.add(buf.clone());
                } else {
                    value.add(subBytes(buf, 0, nRead));
                }
            }
            
            if(0==count)
            {
                logger.log(Level.WARN, "get file "+filename+" from netdisk null");
                is.close();
                return false;
            }
            
            cache_set(cacheType, filename, value);
            is.close();
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.log(Level.FATAL, "error:", e);
            return false;
        }finally{
            if(is != null) {
                is.close();
                is = null;
            }                
        }
    }
    public static Boolean get_file(String filename, TYPE_CACHE cacheType, OutputStream os, TYPE_GET get_type ) throws IOException
    {
        FileOutputStream localos = null;
        FileInputStream  is = null;
        try {
            List<byte[]> value = null;
            
            value = (List<byte[]>) cache_get(cacheType, filename);
            
            if(value != null)
            {
                for (int i = 0; i < value.size(); i++) {
                    byte data[] = value.get(i);
                    os.write(data, 0, data.length);
                }
                return true;
            }
            File file = new File("/home/bae" + filename); 
            if(file!=null&&!file.exists()){ 
                //本地没有,从网盘下载
                if(get_type == TYPE_GET.MEM_LOCAL )//仅本地
                {
                    return false;
                }
                if(get_type == TYPE_GET.MEM_REMOTE)//内存和本地
                {
                    // TODO 如果缓存时间未到也应该缓存
                    return get_file_from_netdisk(filename, os, cacheType);
                }
                
                //需要缓存
                File parent = file.getParentFile(); 
                if(parent!=null&&!parent.exists()){ 
                    parent.mkdirs(); 
                } 
                localos = new FileOutputStream(file); 
                
                if(!get_file_from_netdisk(filename, localos, (get_type == TYPE_GET.LOCAL_REMOTE)?TYPE_CACHE.NOCAHCE:cacheType ))
                {
                    return false;
                } 
            }
            
            is = new FileInputStream(file); 
            byte[] buf = new byte[1024]; // 32k buffer
            
            int nRead = 0, count = 0;
            while( (nRead=is.read(buf)) != -1 ) {  
                count += nRead;
                os.write(buf, 0, nRead);
            }
            
            if(0==count)
            {
                logger.log(Level.WARN, "get file "+filename+" from local null");
                
                file.delete();
                return false;
            }
            return true;
        }
        catch(Exception e) {
            logger.log(Level.FATAL, "get_file error:", e);
            return false;
        }
        finally
        {
            if(localos != null)
            {
                localos.close();
                localos = null;
            }
            if(is != null)
            {
                is.close();
                is = null;
            }
        }
    }
}
