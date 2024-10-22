package com.huism.stockdata.dfcf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class EastmoneyStockDataDownloader {

    public static String everyday_data = "http://push2his.eastmoney.com/api/qt/stock/kline/get?secid=symbol&ut=fa5fd1943c7b386f172d6893dbfba10b&fields1=f1%2Cf2%2Cf3%2Cf4%2Cf5%2Cf6&fields2=f51%2Cf52%2Cf53%2Cf54%2Cf55%2Cf56%2Cf57%2Cf58%2Cf59%2Cf60%2Cf61&klt=101&fqt=1&end=20500101&lmt=210&cb=quote_jp1";

    private static final String URL_TEMPLATE = "http://push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s&ut=fa5fd1943c7b386f172d6893dbfba10b&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61&klt=%d&fqt=%d&beg=%s&end=%s&lmt=10000";
    private static final String STOCK_CODE = "1.603993"; // 洛阳钼业，1表示沪市
    private static final int KLT_DAY = 101;  // 日K线
    private static final int FQT = 1;  // 前复权
    private static final String START_DATE = "20150101";
    private static final String END_DATE = "20250101";

    public static void main(String[] args) {
        try {
            // 1. 获取数据
            List<String[]> stockData = getStockData(STOCK_CODE, KLT_DAY, FQT, START_DATE, END_DATE);

            // 2. 将数据保存为CSV文件
            saveToCSV(stockData, "luoyang_molybdenum_stock_data.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取股票交易数据
     */
    public static List<String[]> getStockData(String stockCode, int klt, int fqt, String beg, String end) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // 构建请求URL
        String url = String.format(URL_TEMPLATE, stockCode, klt, fqt, beg, end);

        // 创建请求对象
        Request request = new Request.Builder()
                .url(url)
                .build();

        // 执行请求并获取响应
        Response response = client.newCall(request).execute();
        String jsonData = response.body().string();

        // 解析响应数据
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonData);

        // 提取K线数据
        List<String[]> stockData = new ArrayList<>();
        JsonNode klines = rootNode.path("data").path("klines");
        for (JsonNode kline : klines) {
            String[] data = kline.asText().split(",");
            stockData.add(data);
        }

        return stockData;
    }

    /**
     * 将股票数据保存为CSV文件
     */
    public static void saveToCSV(List<String[]> data, String filePath) throws IOException {
        var file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter out = new FileWriter(filePath);// date,open,high,low,close,volume
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader("日期", "开盘价", "收盘价", "最高价", "最低价", "成交量", "成交额", "换手率"))) {
            for (String[] record : data) {
                printer.printRecord((Object[]) record);
            }
        }
    }
}

