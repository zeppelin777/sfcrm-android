package com.android.zr.net;

import android.content.Context;
import android.text.TextUtils;

import com.android.zr.R;
import com.android.zr.activity.BaseActivity;
import com.android.zr.bean.BaseBean;
import com.android.zr.utils.ToastUtils;
import com.android.zr.utils.UiUtils;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;

import okhttp3.Call;

public abstract class NetResponseCallBack<T> extends ResponseCallBack<T> {


    private final WeakReference<Context> ref;

    public NetResponseCallBack(Context context) {
        ref = new WeakReference<>(context);
    }

    @Override
    public void onError(Call call, Exception e, int id) {
        if (e != null) {
            e.printStackTrace();
        }
        if (call == null || !call.isCanceled()) {
            ToastUtils.showToast(UiUtils.getString(R.string.connection_failed));
        }

        if (call != null && call.isCanceled()) {
            if (onCanceled(id)) {
                return;
            }
        }
        onError(id);
    }

    @Override
    public void onResponse(String response, int id) {
        try {
            Gson gson = new Gson();
            BaseBean<T> baseBean = gson.fromJson(response, getType());
            if (baseBean.getCode() == 0) {
                onSuccessObject(baseBean.getData(), id);
            } else {
                onFail(baseBean.getMsg(), id);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            ToastUtils.showToast(UiUtils.getString(R.string.connection_failed));
            onError(id);
        }
    }

    public abstract Type getType();


    @Override
    public void onSuccessObject(T data, int id) {
        hideLoading();
    }

    @Override
    public void onError(int id) {
        hideLoading();
    }

    public boolean onCanceled(int id) {
        return false;
    }

    @Override
    public void onFail(String msg, int id) {
        hideLoading();
        if (!TextUtils.isEmpty(msg)) {
            showTips(msg);
        }
    }


    private void hideLoading() {
        if (ref.get() != null && ref.get() instanceof BaseActivity)
            ((BaseActivity) ref.get()).hideLoading();
    }

    /**
     * 显示提示框
     *
     * @param tips 提示的内容
     */
    private void showTips(String tips) {
        if (ref.get() != null && ref.get() instanceof BaseActivity)
            ((BaseActivity) ref.get()).showTips(tips);
    }

}

