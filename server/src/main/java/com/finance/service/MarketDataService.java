package com.finance.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class MarketDataService {

    @Value("${api.alpha-vantage.api-key}")
    private String alphaVantageApiKey;

    @Value("${api.alpha-vantage.base-url}")
    private String alphaVantageBaseUrl;

    @Value("${api.coingecko.base-url}")
    private String coinGeckoBaseUrl;

    private final WebClient webClient = WebClient.builder().build();

    public BigDecimal getCurrentPrice(String symbol) {
        // Try to get stock price first
        try {
            return getStockPrice(symbol);
        } catch (Exception e) {
            // If stock price fails, try crypto
            try {
                return getCryptoPrice(symbol);
            } catch (Exception ex) {
                // Return mock data for demo purposes
                return getMockPrice(symbol);
            }
        }
    }

    private BigDecimal getStockPrice(String symbol) {
        String url = alphaVantageBaseUrl + "?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + alphaVantageApiKey;
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Map<String, Object> globalQuote = (Map<String, Object>) response.get("Global Quote");
                    if (globalQuote != null) {
                        String priceStr = (String) globalQuote.get("05. price");
                        return new BigDecimal(priceStr);
                    }
                    throw new RuntimeException("Price not found");
                })
                .block();
    }

    private BigDecimal getCryptoPrice(String symbol) {
        String url = coinGeckoBaseUrl + "/simple/price?ids=" + symbol.toLowerCase() + "&vs_currencies=usd";
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Map<String, Object> cryptoData = (Map<String, Object>) response.get(symbol.toLowerCase());
                    if (cryptoData != null) {
                        Double price = (Double) cryptoData.get("usd");
                        return BigDecimal.valueOf(price);
                    }
                    throw new RuntimeException("Price not found");
                })
                .block();
    }

    private BigDecimal getMockPrice(String symbol) {
        // Mock prices for demo purposes
        Map<String, BigDecimal> mockPrices = Map.of(
            "AAPL", new BigDecimal("150.00"),
            "GOOGL", new BigDecimal("2800.00"),
            "MSFT", new BigDecimal("300.00"),
            "TSLA", new BigDecimal("250.00"),
            "AMZN", new BigDecimal("3300.00"),
            "BTC", new BigDecimal("45000.00"),
            "ETH", new BigDecimal("3000.00"),
            "ADA", new BigDecimal("1.50"),
            "DOT", new BigDecimal("25.00")
        );
        
        return mockPrices.getOrDefault(symbol.toUpperCase(), new BigDecimal("100.00"));
    }

    public Mono<Map<String, Object>> getStockQuote(String symbol) {
        String url = alphaVantageBaseUrl + "?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + alphaVantageApiKey;
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (Map<String, Object>) response);
    }

    public Mono<Map<String, Object>> getCryptoQuote(String symbol) {
        String url = coinGeckoBaseUrl + "/coins/" + symbol.toLowerCase() + "/market_chart?vs_currency=usd&days=1&interval=hourly";
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (Map<String, Object>) response);
    }
} 