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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

public class LocalFS {
    static private Logger logger = Logger.getLogger("LocalFS");
    static private ExecutorService executor = Executors.newFixedThreadPool(15);
    //static private MongoClient  mongoClient = null;
    
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
            logger.log(Level.SEVERE, "save_file error:", e);
            return null;
        }
    }
    public static Boolean save_file(String collectionName, String filename, InputStream is, Map<String, String> parameters, Boolean toNetDisk )
    {
        MongoClient mc = null;
        try {
            File file = new File("/home/bae" + filename); 
            File parent = file.getParentFile(); 
            if(parent!=null&&!parent.exists()){ 
                parent.mkdirs(); 
            } 
            FileOutputStream os = new FileOutputStream(file); 
            
            byte[] buf = new byte[32*1024]; // 32k buffer
                
            int nRead = 0;
            while ((nRead = is.read(buf)) != -1) {
                os.write(buf, 0, nRead);            
            }
            
            os.close();
            is.close();
            
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
            if(toNetDisk)
            {
                Runnable worker = new UploadThread(filename, filename);
                executor.execute(worker);
            }
            return true;
         }
        catch (Exception e) {
            logger.log(Level.SEVERE, "save_file error:", e);
            return false;
        }
        finally{
            if(mc != null){
                mc.close();
            }
        }
        
    }
    public static Boolean get_file_from_netdisk(String filename, OutputStream os) throws IOException
    {
        InputStream is = null;
        try {
            String access_token = Common.getAccessToken(null);
            Map<String, String> urlparams = new HashMap<String, String>();
            urlparams.put("method", "download");
            urlparams.put("access_token", access_token);
            urlparams.put("path", "/apps/yiqingart"+filename);    
                
            String url = "https://d.pcs.baidu.com/rest/2.0/pcs/file?" + HttpUtil.buildQuery(urlparams, "UTF-8");
              
            URL connect = new URL(url.toString());
            URLConnection connection = connect.openConnection();
    
            connection = Common.reload(connection);
                    
            is = connection.getInputStream();
            byte[] buf = new byte[32*1024]; // 32k buffer
            
            int nRead = 0;
            while( (nRead=is.read(buf)) != -1 ) {
                           
                os.write(buf, 0, nRead);
            }
            
            is.close();
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.log(Level.SEVERE, "error:", e);
            return false;
        }finally{
            if(is != null) {
                is.close();
                is = null;
            }                
        }
    }
    public static Boolean get_file(String filename, OutputStream os, Boolean fromNetDisk, Boolean cache ) throws IOException
    {
        FileOutputStream localos = null;
        FileInputStream  is = null;
        try {
            File file = new File("/home/bae" + filename); 
            if(file!=null&&!file.exists()){ 
                //本地没有,从网盘下载
                if(!cache)//不缓存
                {
                    // TODO 如果缓存时间未到也应该缓存
                    return get_file_from_netdisk(filename, os);
                }
                
                //需要缓存
                File parent = file.getParentFile(); 
                if(parent!=null&&!parent.exists()){ 
                    parent.mkdirs(); 
                } 
                localos = new FileOutputStream(file); 
                
                if(!get_file_from_netdisk(filename, localos ))
                {
                    return false;
                } 
            }
            
            is = new FileInputStream(file); 
            byte[] buf = new byte[32*1024]; // 32k buffer
            
            int nRead = 0;
            while( (nRead=is.read(buf)) != -1 ) {                           
                os.write(buf, 0, nRead);
            }
            return true;
        }
        catch(Exception e) {
            logger.log(Level.SEVERE, "get_file error:", e);
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
