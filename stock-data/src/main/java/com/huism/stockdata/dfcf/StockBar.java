package com.huism.stockdata.dfcf;

import lombok.Data;

@Data
public class StockBar {

//    date,open,high,low,close,volume

    private String symbol;

    private Double open;

    private Double high;

    private Double low;

    private Double close;

    private int volume;

    private String date;
}
