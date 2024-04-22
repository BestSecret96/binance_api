package com.example.binance.api;

import com.example.binance.api.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a trade pair with a list of bids and asks.
 *
 * @author: Shubham Nikam
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TradePair {
    private String symbol;
    private List<Order> bids = new ArrayList<>();
    private List<Order> asks = new ArrayList<>();
    private BigDecimal previousVolume = BigDecimal.ZERO;

    public TradePair(String symbol) {
        this.symbol = symbol;
    }

    public void updateOrderBook(List<Order> bids, List<Order> asks) {
        this.bids = bids;
        this.asks = asks;
    }

    /**
     * Calculates the change in volume of the trade pair.
     *
     * @return the change in volume
     */
    public BigDecimal calculateVolumeChange() {
        BigDecimal currentVolume = bids.stream()
                .map(order -> BigDecimal.valueOf(order.getQuantity()).multiply(BigDecimal.valueOf(order.getPrice())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        currentVolume = currentVolume.add(asks.stream()
                .map(order -> BigDecimal.valueOf(order.getQuantity()).multiply(BigDecimal.valueOf(order.getPrice())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal volumeChange = currentVolume.subtract(previousVolume);
        previousVolume = currentVolume;

        return volumeChange;
    }
}