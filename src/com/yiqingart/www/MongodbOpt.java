package com.yiqingart.www;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

public class MongodbOpt {
    private static Logger logger = Logger.getLogger(MongodbOpt.class);
    
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
    
    public static Boolean save_file(String filename, InputStream is, Map<String, String> parameters)
    {
        MongoClient mc = null;
        
        try {
            mc = getMongoClient();
            DB mongoDB = mc.getDB(Common.BAE_MONGODB_DB);
            mongoDB.authenticate(Common.BAE_API_KEY, Common.BAE_SECRET_KEY.toCharArray());

            GridFS myFS = new GridFS(mongoDB);
            
            myFS.remove(filename);
            
            GridFSInputFile inputFile = myFS.createFile(is, filename);
            
            if(inputFile != null)
            {
                if (parameters != null) {
                    for (Entry<String, String> entry : parameters.entrySet()) {
                        String key = entry.getKey();
                        String paramValue = entry.getValue();
                        inputFile.put(key, paramValue);
                    }
                }            
                
                inputFile.save();
                return true;
            }
            else
            {   
                logger.log(Level.FATAL, "save_file " +  filename + " createFile error");
                return false;
            }
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
    
    public static Boolean get_file(String filename, OutputStream os)
    {
        MongoClient mc = null;
       
        try {
            mc = getMongoClient();
            DB mongoDB = mc.getDB(Common.BAE_MONGODB_DB);
            mongoDB.authenticate(Common.BAE_API_KEY, Common.BAE_SECRET_KEY.toCharArray());

            GridFS myFS = new GridFS(mongoDB);
            
            GridFSDBFile outputFile = myFS.findOne(filename);
            
            if(outputFile!=null)
            {
                outputFile.writeTo(os);
                return true;
            }
            else
            {
                logger.log(Level.FATAL, "get_file " +  filename + " findOne error");
                return false;
            }
            
         }
        catch (Exception e) {
            logger.log(Level.FATAL, "get_file error:", e);
            return false;
        }
        finally{
            if(mc != null){
                mc.close();
            }
        }
    }      
   
}
