package com.yiqingart.www;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

public class MongodbOpt {
    private Logger logger = Logger.getLogger("FileOperation");
    public static MongodbOpt mongodbOpt = null;
    
    private MongoClient mongoClient = null;
     
    public MongodbOpt() {
        try {
            String serverName = Common.BAE_MONGODB_HOST + ":" + Common.BAE_MONGODB_PORT;
            
            mongoClient = new MongoClient(new ServerAddress(serverName), 
                    Arrays.asList(MongoCredential.createMongoCRCredential(Common.BAE_API_KEY, Common.BAE_MONGODB_DB,Common.BAE_SECRET_KEY.toCharArray())), 
                    new MongoClientOptions.Builder().cursorFinalizerEnabled(false).build());
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "MongodbOpt error:", e);
        }
    }
    
    public Boolean save_file(String filename, InputStream is, Map<String, String> parameters)
    {
        int count = 0;
        while(true)
        {
            try {
                DB mongoDB = mongoClient.getDB(Common.BAE_MONGODB_DB);
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
                    count++;
                    if(count<3)continue;
                    return false;
                }
             }
            catch (Exception e) {
                count++;
                if(count<3)continue;
                logger.log(Level.SEVERE, "save_file error:", e);
                return false;
            }
        }
    }
    
    public Boolean get_file(String filename, OutputStream os)
    {
        int count = 0;
        while(true)
        {
            try {
                DB mongoDB = mongoClient.getDB(Common.BAE_MONGODB_DB);
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
                    count++;
                    if(count<3)continue;
                    return false;
                }
                
             }
            catch (Exception e) {
                count++;
                if(count<3)continue;
                logger.log(Level.SEVERE, "get_file error:", e);
                return false;
            }
        }
    }
    
    public static MongodbOpt getInstance(){
        if(mongodbOpt != null ){
            return mongodbOpt;
        }
        else{
            mongodbOpt = new MongodbOpt();
            return mongodbOpt;
            }
    }    
   
}
