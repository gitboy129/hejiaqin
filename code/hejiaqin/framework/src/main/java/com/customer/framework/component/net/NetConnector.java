package com.customer.framework.component.net;

import com.customer.framework.utils.StringUtil;

/***/
public class NetConnector {
    /**
     * 创建http连接，根据BaseRequest的设置，可能进行HttpURLConnection和HttpClient两种类型的连接
     * @param request 请求
     * @return 响应
     */
    public static NetResponse connect(NetRequest request) {
        if (StringUtil.isNullOrEmpty(request.getUrl())) {
            NetResponse response = new NetResponse();
            response.setResponseCode(NetResponse.ResponseCode.ParamError);
            return response;
        }
        return NetUrlConnection.connect(request);
    }

    /***/
    public static NetResponse uploadDirect(NetRequest request) {
        if (StringUtil.isNullOrEmpty(request.getUrl())) {
            NetResponse response = new NetResponse();
            response.setResponseCode(NetResponse.ResponseCode.ParamError);
            return response;
        }
        return NetUrlConnection.upload(request);
    }
}
