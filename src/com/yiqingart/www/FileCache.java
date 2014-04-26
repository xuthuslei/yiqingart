package com.yiqingart.www;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;


public class FileCache {
	private Logger logger = Logger.getLogger("Acra");

	public static FileCache fileCache = null;
	private HashMap<String , Node> picList = null; 
	private HashMap<String , Node> thumbailList = null; 
	private HashMap<String , Node> videoList = null; 
	private HashMap<String , Node> m3u8List = null; 
	private HashMap<String , Node> roomNewPicList = null; 
	public FileCache() {
		picList = new HashMap<String , Node>(); 
		thumbailList = new HashMap<String , Node>(); 
		videoList = new HashMap<String , Node>(); 
		m3u8List = new HashMap<String , Node>(); 
		roomNewPicList = new HashMap<String , Node>(); 
	}
	
	public static FileCache getInstance(){
		if(fileCache != null ){
			return fileCache;
		}
		else{
			fileCache = new FileCache();
			return fileCache;
		}
	}
	
	private Object getValue(HashMap<String , Node> List, String key ){
		Node node = List.get(key);
		if( node == null ){
			logger.log(Level.INFO, "not found  "+key);
			return null;
		}
		if(node.isExpire()){
			List.remove(key);
			logger.log(Level.INFO, "remove "+key);
			return null;
		}
		logger.log(Level.INFO, "found  "+key);
		return node.getValue();
	}
	
	private void setValue(HashMap<String, Node> List, String key, Object value,
			Long ttl, int maxLen) {
		if (List.size() >= maxLen) {
			logger.log(Level.INFO, "list full  "+List.size());
			Iterator<Entry<String, Node>> iter = List.entrySet().iterator();
			Long longTime = 0l;
			String longKey = null;
			while (iter.hasNext()) {
				Entry<String, Node> entry = (Entry<String, Node>) iter.next();
				String nodekey = entry.getKey();
				Node val = entry.getValue();
				if(val.isExpire()){
					logger.log(Level.INFO, nodekey + " Expire remove");
					iter.remove();
				}
				else{
					if( longTime < val.usedTime()){
						longTime = val.usedTime();
						longKey = nodekey;
					}
				}
			}
			if(List.size() >= maxLen){
				if(longKey!=null){
					logger.log(Level.INFO, longKey + " used remove");
					List.remove(longKey);
				}
				else
				{
					logger.log(Level.SEVERE, "full");
					return;
				}
			}
		}
		Node node = new Node(ttl, value);
		List.put(key, node);
		logger.log(Level.INFO, "put  "+key);
	}
	private String[] getList(HashMap<String, Node> List){
	    if (List.size() >0 ) {
	        Set<String> reslut = List.keySet();
	        for( String key : reslut){
	            if(getValue(List, key) == null){
	                reslut.remove(key);
	            }
	        }
            return reslut.toArray(new String[0]);
        }
	    else{
	        return null;
	    }
	}

	@SuppressWarnings("unchecked")
	public List<byte[]> getPic(String key) {
		return (List<byte[]>)getValue(picList, key);
	}

	public void setPic(String key, List<byte[]> value, Long ttl) {
		setValue(picList, key, value,  ttl, 24);
	}

	@SuppressWarnings("unchecked")
	public List<byte[]> getThumbail(String key) {
		return (List<byte[]>)getValue(thumbailList, key);
	}

	public void setThumbail(String key, List<byte[]> value, Long ttl) {
		setValue(thumbailList, key, value,  ttl, 12);
	}
	
	@SuppressWarnings("unchecked")
	public List<byte[]> getVideo(String key) {
		return (List<byte[]>)getValue(videoList, key);
	}

	public void setVideo(String key, List<byte[]> value, Long ttl) {
		setValue(videoList, key, value,  ttl, 48);
	}
	
	@SuppressWarnings("unchecked")
    public List<byte[]> getM3U8(String key) {
        return (List<byte[]>)getValue(m3u8List, key);
    }
    
    public String[] getM3U8List() {
        return getList(m3u8List);
    }

    public void setM3U8(String key, List<byte[]> value, Long ttl) {
        setValue(m3u8List, key, value,  ttl, 6);
    }
	
	public JSONObject getRoomNewPic(String key) {
		return (JSONObject)getValue(roomNewPicList, key);
	}

	public void setRoomNewPic(String key, JSONObject value, Long ttl) {
		setValue(roomNewPicList, key, value,  ttl, 20);
	}
	
	private class Node {
		private Long time;
		private Long visit;
		private Long ttl;
		private Object value;
		public Node(Long ttl, Object value) {
			this.time = this.visit = System.currentTimeMillis();
			this.ttl = ttl;
			this.value = value;
		}
		public Boolean isExpire(){
			return  System.currentTimeMillis() - time > ttl ;
		}
		
		public Object getValue(){
			this.visit = System.currentTimeMillis();
			return value;
		}
		public Long usedTime(){
			return System.currentTimeMillis() - visit;
		}
	}

}
