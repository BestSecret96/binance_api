package com.example.binance;

import com.example.binance.api.BinanceAPI;
import com.example.binance.api.TradePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The main class of the application.
 *
 * @author: Shubham Nikam
 */
@SpringBootApplication
public class BinanceAPIApplication {

    private static final Logger logger = LoggerFactory.getLogger(BinanceAPIApplication.class);
    public static final String BTC_USDT = "BTCUSDT";
    public static final String ETH_USDT = "SETHUS";
    public static final String PRICE_QUANTITY = "Price: {}, Quantity: {}";

    /**
     * The main method of the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BinanceAPIApplication.class, args);

        BinanceAPI binanceAPI = new BinanceAPI();
        TradePair btcUsdt = new TradePair(BTC_USDT);
        TradePair ethUsdt = new TradePair(ETH_USDT);

        btcUsdt.updateOrderBook(binanceAPI.getOrderBook(BTC_USDT), binanceAPI.getOrderBook(BTC_USDT));
        ethUsdt.updateOrderBook(binanceAPI.getOrderBook(ETH_USDT), binanceAPI.getOrderBook(ETH_USDT));

        binanceAPI.subscribeUpdates(BTC_USDT, btcUsdt);
        binanceAPI.subscribeUpdates(ETH_USDT, ethUsdt);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("{} order book:", BTC_USDT);
                btcUsdt.getBids().forEach(bid -> logger.info(PRICE_QUANTITY, bid.getPrice(), bid.getQuantity()));
                btcUsdt.getAsks().forEach(ask -> logger.info(PRICE_QUANTITY, ask.getPrice(), ask.getQuantity()));
                logger.info("{} volume change: {}", BTC_USDT, btcUsdt.calculateVolumeChange());

                logger.info("{} order book:", ETH_USDT);
                ethUsdt.getBids().forEach(bid -> logger.info(PRICE_QUANTITY, bid.getPrice(), bid.getQuantity()));
                ethUsdt.getAsks().forEach(ask -> logger.info(PRICE_QUANTITY, ask.getPrice(), ask.getQuantity()));
                logger.info("{} volume change: {}", ETH_USDT, ethUsdt.calculateVolumeChange());
            }
        }, 0, 10000);
    }
}