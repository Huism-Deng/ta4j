package com.huism.stockdata.dfcf;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class SpideStockData {

    private static final OkHttpClient client = new OkHttpClient();

    // 股票列表
    private static final String LIST_URL1 = "http://push2.eastmoney.com/api/qt/clist/get?pn=1&pz=5000&po=1&np=1&fltt=2&invt=2&fid=f3&fs=b:MK0010&fields=f1,f2,f3,f4,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f115,f128,f140,f141,f136,f152&_=timestamp";
    private static final String LIST_URL2 = "http://push2.eastmoney.com/api/qt/clist/get";

    // 个股历史数据
    private static final String STOCK_URL = "http://push2his.eastmoney.com/api/qt/stock/kline/get";
    // https://push2his.eastmoney.com/api/qt/stock/kline/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61&beg=0&end=20500101&ut=fa5fd1943c7b386f172d6893dbfba10b&rtntype=6&secid=0.002635&klt=101&fqt=1&cb=jsonp1729582964213

    public static void main(String[] args) {
//        String stockCode = "1.603993"; // 洛阳钼业，1表示沪市  (安洁科技 0.002635)
//        getSingleStockData("1");

    }

    private void getStockListData() {
        // 构建URL参数
        HttpUrl.Builder urlBuilder = HttpUrl.parse(LIST_URL2).newBuilder();
        urlBuilder.addQueryParameter("pn", "1");   // 页码
        urlBuilder.addQueryParameter("pz", "5000"); // 每页最大数据量
        urlBuilder.addQueryParameter("po", "1");
        urlBuilder.addQueryParameter("np", "1");
        urlBuilder.addQueryParameter("fltt", "2");
        urlBuilder.addQueryParameter("invt", "2");
        urlBuilder.addQueryParameter("fid", "f3");
        urlBuilder.addQueryParameter("fs", "b:MK0010"); // 沪深A股  (b:MK0021 港股市场)
        urlBuilder.addQueryParameter("fields", "f12,f13,f14,f20,f21,f3,f9,f23,f115,f128,f140");

        // 构建完整的URL
        String url = urlBuilder.build().toString();

        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
    }

    private static void getSingleStockData(String stockCode) {
        var s = querySingleStock(stockCode);
//        CsvUtil.saveToCSV();
    }

    private static String querySingleStock(String stockCode) {
        // 构建URL参数
        HttpUrl.Builder urlBuilder = HttpUrl.parse(STOCK_URL).newBuilder();
        urlBuilder.addQueryParameter("secid", stockCode);
        urlBuilder.addQueryParameter("ut", "fa5fd1943c7b386f172d6893dbfba10b");
        urlBuilder.addQueryParameter("fields1", "f1,f2,f3,f4,f5,f6");
        urlBuilder.addQueryParameter("fields2", "f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61");
        urlBuilder.addQueryParameter("klt", "101");     // 日K线
        urlBuilder.addQueryParameter("fqt", "1");       // 前复权
        urlBuilder.addQueryParameter("beg", "20150101"); // 起始时间
        urlBuilder.addQueryParameter("end", "20250101"); // 终止时间
        urlBuilder.addQueryParameter("lmt", "10000");   // 最大条数

        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                return response.body().string();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
