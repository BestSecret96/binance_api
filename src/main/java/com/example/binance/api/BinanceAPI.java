package com.example.binance.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Binance API class.
 *
 * @author: Shubham Nikam
 */
@SuppressWarnings("unchecked")
public class BinanceAPI {
    private static final Logger logger = LoggerFactory.getLogger(BinanceAPI.class);
    private static final String ORDER_BOOK_URL = "https://api.binance.com/api/v3/depth?symbol=%s&limit=50";
    private static final String WEBSOCKET_URL = "wss://stream.binance.com:9443/ws/%s@depth";

    private final Gson gson = new Gson();
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    /**
     * Gets the order book for a given symbol.
     *
     * @author: Shuhbam Nikam
     *
     * @param symbol the symbol to get the order book for
     * @return the order book
     */
    public List<Order> getOrderBook(String symbol) {
        List<Order> orders = new ArrayList<>();
        HttpGet httpGet = new HttpGet(String.format(ORDER_BOOK_URL, symbol));
        logger.info("Sending request to: {}", httpGet.getURI());
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            logger.info("Response: {}", responseBody);
            orders.addAll(parseOrders(responseBody));
        } catch (Exception e) {
            logger.error("Error while getting order book", e);
        }
        return orders;
    }

    /**
     * Parses the order book from the response body.
     *
     * @param responseBody the response body
     * @return the order book
     */
    private List<Order> parseOrders(String responseBody) {
        List<Order> orders = new ArrayList<>();
        Map<String, Object> orderBook = gson.fromJson(responseBody, new TypeToken<Map<String, Object>>(){}.getType());
        List<List<String>> bids = (List<List<String>>) orderBook.get("bids");
        List<List<String>> asks = (List<List<String>>) orderBook.get("asks");
        if (bids != null) {
            for (List<String> bid : bids) {
                orders.add(new Order(Double.parseDouble(bid.get(0)), Double.parseDouble(bid.get(1))));
            }
        }
        if (asks != null) {
            for (List<String> ask : asks) {
                orders.add(new Order(Double.parseDouble(ask.get(0)), Double.parseDouble(ask.get(1))));
            }
        }
        return orders;
    }

    public void subscribeUpdates(String symbol, TradePair tradePair) {
        try {
            WebSocketClient client = new WebSocketClient(new URI(String.format(WEBSOCKET_URL, symbol.toLowerCase()))) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    logger.info("WebSocket connection opened");
                }

                @Override
                public void onMessage(String message) {
                    Map<String, Object> update = gson.fromJson(message, new TypeToken<Map<String, Object>>(){}.getType());
                    List<List<String>> bids = (List<List<String>>) update.get("b");
                    List<List<String>> asks = (List<List<String>>) update.get("a");
                    List<Order> bidOrders = new ArrayList<>();
                    List<Order> askOrders = new ArrayList<>();
                    for (List<String> bid : bids) {
                        bidOrders.add(new Order(Double.parseDouble(bid.get(0)), Double.parseDouble(bid.get(1))));
                    }
                    for (List<String> ask : asks) {
                        askOrders.add(new Order(Double.parseDouble(ask.get(0)), Double.parseDouble(ask.get(1))));
                    }
                    tradePair.updateOrderBook(bidOrders, askOrders);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.info("WebSocket connection closed: {}", reason);
                }

                @Override
                public void onError(Exception ex) {
                    logger.error("Error in WebSocket connection", ex);
                }
            };
            client.connect();
        } catch (URISyntaxException e) {
            logger.error("Error while subscribing updates", e);
        }
    }
}