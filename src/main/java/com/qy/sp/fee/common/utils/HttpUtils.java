package com.qy.sp.fee.common.utils;


import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * HTTP通讯工具�?.
 *
 * @author <a href="http://www.jiangzezhou.com">jiangzezhou</a>
 * @version 1.0.0.0, 6/15/15 19::42
 */
public final class HttpUtils {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger
            (HttpUtils.class.getName());
    //连接超时时间3�?
    public static final int HTTP_CONNECTION_TIME_OUT = 5000;
    //读取超时时间5�?
    public static final int HTTP_READ_TIME_OUT = 7000;


    private HttpUtils() {

    }

    public static HttpResponseInfo postRobj(final String adress, final byte[] bytes,
                                            final Map<String, String> headers, final int connectTimeOut,
                                            final int readTimeOut) throws Exception {
        final HttpResponseInfo httpResponseInfo =
                postRObjWithStream(adress, bytes, headers, connectTimeOut, readTimeOut);
        final String content =
                StringUtil.getStrFromInputStream(httpResponseInfo.getInputStream());
        httpResponseInfo.setBody(content);
        return httpResponseInfo;

    }

    public static String postRstr(final String adress, final byte[] bytes,
                                  final Map<String, String> headers, final int connectTimeOut,
                                  final int readTimeOut) throws Exception {
        final HttpResponseInfo httpResponseInfo =
                postRobj(adress, bytes, headers, connectTimeOut, readTimeOut);
        return httpResponseInfo.getBody();
    }

    public static String postRstr(String adress, byte[] bytes,
                                  Map<String, String> headers) throws Exception {
        return postRstr(adress, bytes, headers,
                HTTP_CONNECTION_TIME_OUT, HTTP_READ_TIME_OUT);
    }

    private static HttpResponseInfo postRObjWithStream(final String adress, final byte[] bytes,
                                                       final Map<String, String> headers,
                                                       final int connectTimeOut,
                                                       final int readTimeOut) throws Exception {
        LOGGER.info("address: " + adress);
        LOGGER.info("connectTimeOut: " + connectTimeOut);
        LOGGER.info("readTimeOut: " + readTimeOut);
        final HttpResponseInfo httpResponseInfo = new HttpResponseInfo();
        URL url = new URL(adress);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(connectTimeOut);
        conn.setReadTimeout(readTimeOut);
        conn.setRequestMethod("POST");
        if (headers != null) {
            Set<Map.Entry<String, String>> entrySet = headers.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.getOutputStream().write(bytes, 0, bytes.length);
        conn.getOutputStream().flush();
        conn.getOutputStream().close();
        int code = conn.getResponseCode();
        LOGGER.info("code: " + code);
        httpResponseInfo.setHttpCode(code);
        httpResponseInfo.setInputStream(getByCode(code, conn));
        return httpResponseInfo;
    }

    /**
     * 通知httpCode返回码获取inputStream.
     *
     * @param code       http状�?�码.
     * @param connection httpUrlConnecton
     * @return inputstream
     * @throws Exception
     */
    private static InputStream getByCode(final int code,
                                         final HttpURLConnection connection)
            throws Exception {
        if (code == 200) {
            return connection.getInputStream();
        } else {
            return connection.getErrorStream();
        }
    }


    public static class HttpResponseInfo implements Serializable {

        private InputStream inputStream;

        private int httpCode;

        private String body;

        private String errorMsg;

        private List<String> headers;

        public int getHttpCode() {
            return httpCode;
        }

        public void setHttpCode(final int httpCode) {
            this.httpCode = httpCode;
        }

        public String getBody() {
            return body;
        }

        public void setBody(final String body) {
            this.body = body;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(final String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public void setHeaders(final List<String> headers) {
            this.headers = headers;
        }

        public void setInputStream(final InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public String toString() {
            return "HttpResponseInfo{" +
                    "inputStream=" + inputStream +
                    ", httpCode=" + httpCode +
                    ", body='" + body + '\'' +
                    ", errorMsg='" + errorMsg + '\'' +
                    ", headers=" + headers +
                    '}';
        }
    }

}
