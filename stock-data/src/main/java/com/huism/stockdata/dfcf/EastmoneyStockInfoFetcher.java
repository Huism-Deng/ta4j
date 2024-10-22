package com.huism.stockdata.dfcf;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/*
http://push2.eastmoney.com/api/qt/clist/get
东方财富 常见的 f 字段及其含义：
f1：是否停牌（0 代表交易中，1 代表停牌）
f2：最新价（当前交易价格）
f3：涨跌幅（百分比，和前一天的收盘价相比的涨跌幅）
f4：涨跌额（最新价和前一交易日收盘价的差值）
f5：成交量（交易的股数，单位为手，一手=100股）
f6：成交额（交易的总金额，单位为元）
f7：振幅（当天最高价与最低价的百分比差异）
f8：换手率（换手率 = 成交量 / 流通股数）
f9：静态市盈率（PE，过去一年的市盈率）
f10：量比（成交量与过去5天平均成交量的比率）
f11：每股收益（EPS，Earnings Per Share）
f12：股票代码
f13：交易所代码（1 表示深圳，0 表示上海）
f14：股票名称
f15：最高价（当天最高价格）
f16：最低价（当天最低价格）
f17：开盘价（当天开盘价格）
f18：昨收价（前一天的收盘价格）
f20：总市值（以元为单位）
f21：流通市值（以元为单位）
f22：涨速（当前上涨速度）
f23：市净率（PB，Price-to-Book Ratio）
f24：现手（最新一笔交易的成交量）
f25：最近逐笔成交（展示最后一笔成交的详细信息）
f26：涨停价（当日涨停的价格）
f27：跌停价（当日跌停的价格）
f28：上市天数（股票已经上市的天数）
f33：外盘（主动买入成交量）
f34：内盘（主动卖出成交量）
f37：委比（委买手数与委卖手数的差额比率）
f38：委差（委买手数与委卖手数的差额）
f43：最新成交手数（最近一笔成交的手数）
f45：涨跌停标记（1 表示涨停，-1 表示跌停，0 表示正常交易）
f62：资金流入量（大单资金流入总量）
f63：资金流出量（大单资金流出总量）
f64：资金净流入（资金流入与流出的差额）
f65：大单成交量（大单买入的成交量）
f66：大单卖出量（大单卖出的成交量）
f67：主力净流入（主力资金的净流入量）
f68：主力净流入占比（主力资金净流入在总成交量中的比例）
f69：中单净流入（中单资金净流入量）
f70：小单净流入（小单资金净流入量）
f71：中单资金净流入占比
f72：小单资金净流入占比
f107：公司类型（如A股、B股等）
f115：是否为港股通标的（1 表示是港股通，0 表示否）
f128：上市状态（1 表示上市，2 表示暂停上市，3 表示退市）
f140：股票全称
f141：市场板块（如沪市主板、深市主板、创业板等）
f136：最近一次公告日期
 */

/*
http://push2his.eastmoney.com/api/qt/stock/kline/get
常见的请求参数说明：

secid: 股票代码，格式为市场代码.股票代码，如 1.600519 表示贵州茅台（1 表示上海市场，0 表示深圳市场）。
ut: 固定值，通常为 fa5fd1943c7b386f172d6893dbfba10b。
fields1: 选择基本字段，通常为 f1,f2,f3,f4,f5,f6。
fields2: 选择扩展字段，如 f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61，这些字段对应K线数据的开盘价、收盘价、最高价、最低价、成交量、成交额等。
klt: K线周期（1 表示分钟，101 表示日K线，102 表示周K线，103 表示月K线）。
                klt=1：1分钟K线
                klt=5：5分钟K线
                klt=15：15分钟K线
                klt=30：30分钟K线
                klt=60：60分钟K线
                klt=101：日K线
                klt=102：周K线
                klt=103：月K线
fqt: 前复权方式（0 表示不复权，1 表示前复权，2 表示后复权）。
beg: 起始时间，格式为 YYYYMMDD。
end: 结束时间，格式为 YYYYMMDD。
smplmt: 数据限制，默认为460，可以指定返回的数据条数。
lmt: 每次返回的数据条数。

 */
public class EastmoneyStockInfoFetcher {

    private static final String API_URL = "http://push2.eastmoney.com/api/qt/clist/get";
    private static final String STOCK_URL = "http://push2his.eastmoney.com/api/qt/stock/kline/get";

    private static final OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) {
        // 构建URL参数
        HttpUrl.Builder urlBuilder = HttpUrl.parse(API_URL).newBuilder();
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

        // 发送请求并处理响应
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();
                    JsonArray stockList = jsonResponse.getAsJsonObject("data").getAsJsonArray("diff");

                    // 解析股票数据
                    for (int i = 0; i < stockList.size(); i++) {
                        JsonObject stock = stockList.get(i).getAsJsonObject();
                        String code = stock.get("f12").getAsString(); // 股票代码

                        // 过滤掉创业板和科创板
                        if (code.startsWith("300") || code.startsWith("688")) {
                            continue; // 跳过创业板和科创板股票
                        }

                        String name = stock.get("f14").getAsString(); // 股票名称
                        String fullName = stock.get("f140").getAsString(); // 股票全称
                        String exchangeCode = stock.get("f13").getAsString(); // 交易所代码
                        String marketStatus = stock.get("f128").getAsString(); // 上市状态
                        String isHongKongStock = stock.get("f115").getAsString(); // 是否港股通
                        String peRatio = stock.get("f3").getAsString(); // 动态市盈率
                        String staticPeRatio = stock.has("f9") ? stock.get("f9").getAsString() : ""; // 静态市盈率
                        String pbRatio = stock.get("f23").getAsString(); // 市净率
                        String totalMarketValue = stock.get("f20").getAsString(); // 总市值
                        String circulatingMarketValue = stock.get("f21").getAsString(); // 流通市值

                        // 输出股票数据
//                        System.out.println("股票代码: " + code);
//                        System.out.println("股票名称: " + name);
//                        System.out.println("股票全称: " + fullName);
//                        System.out.println("交易所代码: " + exchangeCode);
//                        System.out.println("上市状态: " + marketStatus);
//                        System.out.println("是否港股通: " + isHongKongStock);
//                        System.out.println("动态市盈率: " + peRatio);
//                        System.out.println("静态市盈率: " + staticPeRatio);
//                        System.out.println("市净率: " + pbRatio);
//                        System.out.println("总市值: " + totalMarketValue);
//                        System.out.println("流通市值: " + circulatingMarketValue);
//                        System.out.println("----------------------------");
                    }
                } else {
                    System.out.println("请求失败: " + response.code());
                }
            }
        });
    }
}

