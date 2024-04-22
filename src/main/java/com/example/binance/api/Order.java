package com.example.binance.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents an order with a price and quantity.
 *
 * @author: Shubham Nikam
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Order {
    private double price;
    private double quantity;
}