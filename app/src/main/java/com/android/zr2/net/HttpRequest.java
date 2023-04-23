package com.android.zr2.net;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.https.HttpsUtils;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class HttpRequest {

    private static final int TIMEOUT = 30;

    private static volatile HttpRequest httpRequest;

    private OkHttpClient httpClient;


    private HttpRequest() {
        initOkHttp();
    }


    public static HttpRequest getInstance() {
        if (httpRequest == null) {
            synchronized (HttpRequest.class) {
                if (httpRequest == null) {
                    httpRequest = new HttpRequest();
                }
            }
        }
        return httpRequest;
    }

    /**
     * 配置okhttp
     */
    private void initOkHttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new HeaderInterceptor())
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS);

        sslSupport(builder);
        httpClient = builder.build();
        OkHttpUtils.initClient(httpClient);

    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    private void sslSupport(OkHttpClient.Builder builder) {
        try {
            HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
            builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
            builder.hostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void get(String url, Object tag, ResponseCallBack callBack) {
        getRequest(url, 1, null, tag, callBack);
    }

    public void getWithID(String url, int id, Object tag, ResponseCallBack callBack) {
        getRequest(url, id, null, tag, callBack);
    }

    public void getWithParams(String url, Map<String, String> map, Object tag, ResponseCallBack callBack) {
        getRequest(url, 1, map, tag, callBack);
    }

    private void getRequest(String url, int id, Map<String, String> map, Object tag, ResponseCallBack callBack) {
        GetBuilder builder = OkHttpUtils
                .get()
                .url(url)
                .id(id)
                .params(map)
                .tag(tag);
        RequestCall requestCall = builder.build();
        requestCall.execute(callBack);
    }

    public void post(String url, Map<String, String> map, Object tag, ResponseCallBack callBack) {
        OkHttpUtils
                .post()
                .url(url)
                .params(map)
                .tag(tag)
                .build()
                .execute(callBack);
    }

    public void upload(String url, String name, Map<String, String> map, File file, Object tag, ResponseCallBack callBack) {
        uploadFile(url, 1, name, map, file, tag, callBack);
    }


    public void uploadWithID(String url, int id, String name, Map<String, String> map, File file, Object tag, ResponseCallBack callBack) {
        uploadFile(url, id, name, map, file, tag, callBack);
    }

    private void uploadFile(String url, int id, String name, Map<String, String> map, File file, Object tag, ResponseCallBack callBack) {
        OkHttpUtils
                .post()
                .url(url)
                .id(id)
                .addFile(name, file.getName(), file)
                .params(map)
                .tag(tag)
                .build()
                .execute(callBack);
    }


    public void postJson(String url, String json, Object tag, ResponseCallBack callBack) {
        OkHttpUtils
                .postString()
                .url(url)
                .content(json)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .tag(tag)
                .build()
                .execute(callBack);
    }

}
