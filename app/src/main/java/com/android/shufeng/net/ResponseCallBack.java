package com.android.shufeng.net;

import com.zhy.http.okhttp.callback.Callback;

import java.io.IOException;

import okhttp3.Response;

public abstract class ResponseCallBack<T> extends Callback<String> {


    public abstract void onSuccessObject(T data, int id);

    public abstract void onFail(int code, String msg, int id);

    public abstract void onError(int id);

    @Override
    public String parseNetworkResponse(Response response, int id) throws IOException {
        return response.body().string();
    }

    @Override
    public boolean validateResponse(Response response, int id) {
        return true;
    }
}
