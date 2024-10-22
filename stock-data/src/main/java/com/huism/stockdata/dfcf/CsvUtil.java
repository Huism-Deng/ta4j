package com.huism.stockdata.dfcf;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvUtil {

    public void saveIndividualStockData(List<String[]> data, String stockCode) {
        var csvFormat = CSVFormat.DEFAULT.withHeader("日期", "开盘价", "收盘价", "最高价", "最低价", "成交量", "成交额", "换手率");

        saveToCSV(data, stockCode, csvFormat);
    }

    public void saveStockList(List<String[]> data, String stockCode) {
        var csvFormat = CSVFormat.DEFAULT.withHeader("股票名称", "股票全称", "交易所代码", "上市状态", "是否港股通", "动态市盈率", "静态市盈率", "市净率", "总市值", "流通市值");

        saveToCSV(data, stockCode, csvFormat);
    }

    public static void saveToCSV(List<String[]> data, String filePath, CSVFormat csvFormat){
        try {
            var file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter out = new FileWriter(filePath);
            try (CSVPrinter printer = new CSVPrinter(out, csvFormat)) {
                for (String[] record : data) {
                    printer.printRecord((Object[]) record);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
