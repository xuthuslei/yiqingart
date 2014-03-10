/**
 * Copyright (c) 2011 Baidu.com, Inc. All Rights Reserved
 */
package com.yiqingart.www;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * openapi调用客户端类， 封装了进行调用的基本操作
 * 
 * @author chenhetong(chenhetong@baidu.com)
 * 
 */
public class BaiduApiClient {

    //get请求常量
    public static final String METHOD_GET = "GET";

    //post请求常量
    public static final String METHOD_POST = "POST";

    //BatchRun的请求将按照串行顺序执行
    public static final int BATCH_MODE_SERIAL_ONLY = 1;

    //BatchRun的请求将按照并行顺序执行
    public static final int BATCH_MODE_SERVER_PARALLEL = 0;

    private String accessToken;

    private String clientId;

    /**
     * 创建openapi的调用实例，使用Https的方法访问api
     * 
     * @param accessToken 基于https调用Open API时所需要的访问授权码
     */
    public BaiduApiClient(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * 通过accessToken和clientId来构建对象。
     * 如果希望访问public类型的api，请使用该方法初始化BaiduApiClient对象
     * 
     * @param accessToken
     * @param clientId appkey
     */
    public BaiduApiClient(String accessToken, String clientId) {

    }

    

    /**
     * 针对api请求的方法类
     * 
     * @param url api请求的url地址
     * @param parameters
     *        业务级参数，包括一本的文本参数（key-value都为string类型）、文件参数（key-fileName
     *        String类型；value byte[]）
     * @param method 请求的方法 "GET"/"POST"
     * @return 返回json格式信息
     * @throws IOException
     * @throws BaiduApiException
     */
    public String request(String url, Map<String, String> parameters, String method)
            throws Exception {
        //截取url中的访问的api的类型 eg https://openapi.baidu.com/public/2.0/mp3/  截取的类型为public类型
        String[] splits = url.split("/");
        String type = splits[3];
        if ("rest".equals(type)) {
            return restRequest(url, parameters, method);
        }
        if ("public".equals(type)) {
            return publicRequest(url, parameters, method);
        }
        return null;
    }

    /**
     * 访问rest类型的api
     * 
     * @param url rest类型的api url地址，使用全路径
     * @param parameters 业务级参数，key-value格式，key、value都必须是String类型
     * @param method 请求的方法 "GET"/"POST"
     * @return 返回 json格式的请求信息
     * @throws IOException 网络请求异常时发生IOException
     * @throws BaiduApiException
     */
    private String restRequest(String url, Map<String, String> parameters, String method)
            throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", this.accessToken);
        if (parameters != null) {
            params.putAll(parameters);
        }
        String response = null;
        try {
            if ("GET".equals(method)) {
                response = HttpUtil.doGet(url, params);
            } else {
                response = HttpUtil.doPost(url, params);
            }
        } catch (IOException e) {
            return null;
        }
        checkApiResponse(response);
        return response;

    }

    /**
     * 访问public类型的api请求
     * 
     * @param url public类型的api的全路径uri
     * @param parameters 业务级参数 （key、value均为String类型）
     * @param method 访问api的方法“GET”/“POST”
     * @return json格式数据
     * @throws IOException 当网络发生异常时发生IOException
     * @throws BaiduApiException 当返回的json信息中包含error信息时，抛出BaiduExceptioin
     */
    private String publicRequest(String url, Map<String, String> parameters, String method)
            throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("client_id", this.clientId);
        if (parameters != null) {
            params.putAll(parameters);
        }
        String response = null;
        try {
            if ("GET".equals(method)) {
                response = HttpUtil.doGet(url, params);
            } else {
                response = HttpUtil.doPost(url, params);
            }
        } catch (IOException e) {
            return null;
        }
        checkApiResponse(response);
        return response;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public static void checkApiResponse(String json) throws Exception{
        if ("error_code".equals(json)) {
            JSONObject obj = new JSONObject(json);
            if (obj != null) {
                Object objErrorCode = obj.get("error_code");
                Object objErrorMsg = obj.get("error_msg");
                if (objErrorCode != null) {
                	throw new Exception(objErrorCode.toString());
                }
                if (objErrorMsg != null) {
                	throw new Exception(objErrorMsg.toString());
                }
            }
        }
    }
    
    
}
