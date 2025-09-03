package com.finance.controller;

import com.finance.model.Trade;
import com.finance.model.User;
import com.finance.repository.TradeRepository;
import com.finance.repository.UserRepository;
import com.finance.security.CurrentUser;
import com.finance.security.UserPrincipal;
import com.finance.service.MarketDataService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/trades")
@CrossOrigin(origins = "*")
public class TradeController {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MarketDataService marketDataService;

    @GetMapping
    public ResponseEntity<List<Trade>> getAllTrades(@CurrentUser UserPrincipal currentUser) {
        List<Trade> trades = tradeRepository.findByUserIdOrderByTradeDateDesc(currentUser.getId());
        
        // Update current prices and calculate P&L
        for (Trade trade : trades) {
            try {
                BigDecimal currentPrice = marketDataService.getCurrentPrice(trade.getSymbol());
                trade.setCurrentPrice(currentPrice);
                
                if (trade.getCurrentPrice() != null && trade.getPricePerUnit() != null) {
                    BigDecimal profitLoss = trade.getCurrentPrice()
                            .subtract(trade.getPricePerUnit())
                            .multiply(trade.getQuantity());
                    trade.setProfitLoss(profitLoss);
                    
                    BigDecimal profitLossPercentage = profitLoss
                            .divide(trade.getPricePerUnit().multiply(trade.getQuantity()), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    trade.setProfitLossPercentage(profitLossPercentage);
                }
            } catch (Exception e) {
                // Handle API errors gracefully
                trade.setCurrentPrice(BigDecimal.ZERO);
                trade.setProfitLoss(BigDecimal.ZERO);
                trade.setProfitLossPercentage(BigDecimal.ZERO);
            }
        }
        
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trade> getTradeById(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        Optional<Trade> trade = tradeRepository.findById(id);
        
        if (trade.isPresent() && trade.get().getUser().getId().equals(currentUser.getId())) {
            Trade tradeData = trade.get();
            
            // Update current price and P&L
            try {
                BigDecimal currentPrice = marketDataService.getCurrentPrice(tradeData.getSymbol());
                tradeData.setCurrentPrice(currentPrice);
                
                if (tradeData.getCurrentPrice() != null && tradeData.getPricePerUnit() != null) {
                    BigDecimal profitLoss = tradeData.getCurrentPrice()
                            .subtract(tradeData.getPricePerUnit())
                            .multiply(tradeData.getQuantity());
                    tradeData.setProfitLoss(profitLoss);
                    
                    BigDecimal profitLossPercentage = profitLoss
                            .divide(tradeData.getPricePerUnit().multiply(tradeData.getQuantity()), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    tradeData.setProfitLossPercentage(profitLossPercentage);
                }
            } catch (Exception e) {
                tradeData.setCurrentPrice(BigDecimal.ZERO);
                tradeData.setProfitLoss(BigDecimal.ZERO);
                tradeData.setProfitLossPercentage(BigDecimal.ZERO);
            }
            
            return ResponseEntity.ok(tradeData);
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Trade> createTrade(@Valid @RequestBody TradeRequest tradeRequest, 
                                           @CurrentUser UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        Trade trade = new Trade();
        trade.setUser(user);
        trade.setSymbol(tradeRequest.getSymbol().toUpperCase());
        trade.setAssetType(tradeRequest.getAssetType());
        trade.setTradeType(tradeRequest.getTradeType());
        trade.setQuantity(tradeRequest.getQuantity());
        trade.setPricePerUnit(tradeRequest.getPricePerUnit());
        trade.setTotalAmount(tradeRequest.getQuantity().multiply(tradeRequest.getPricePerUnit()));
        trade.setFees(tradeRequest.getFees() != null ? tradeRequest.getFees() : BigDecimal.ZERO);
        trade.setNotes(tradeRequest.getNotes());
        trade.setTradeDate(tradeRequest.getTradeDate() != null ? tradeRequest.getTradeDate() : LocalDateTime.now());

        Trade savedTrade = tradeRepository.save(trade);
        return ResponseEntity.ok(savedTrade);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trade> updateTrade(@PathVariable Long id, 
                                           @Valid @RequestBody TradeRequest tradeRequest,
                                           @CurrentUser UserPrincipal currentUser) {
        Optional<Trade> existingTrade = tradeRepository.findById(id);
        
        if (existingTrade.isPresent() && existingTrade.get().getUser().getId().equals(currentUser.getId())) {
            Trade trade = existingTrade.get();
            trade.setSymbol(tradeRequest.getSymbol().toUpperCase());
            trade.setAssetType(tradeRequest.getAssetType());
            trade.setTradeType(tradeRequest.getTradeType());
            trade.setQuantity(tradeRequest.getQuantity());
            trade.setPricePerUnit(tradeRequest.getPricePerUnit());
            trade.setTotalAmount(tradeRequest.getQuantity().multiply(tradeRequest.getPricePerUnit()));
            trade.setFees(tradeRequest.getFees() != null ? tradeRequest.getFees() : BigDecimal.ZERO);
            trade.setNotes(tradeRequest.getNotes());
            trade.setTradeDate(tradeRequest.getTradeDate() != null ? tradeRequest.getTradeDate() : LocalDateTime.now());

            Trade updatedTrade = tradeRepository.save(trade);
            return ResponseEntity.ok(updatedTrade);
        }
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrade(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        Optional<Trade> trade = tradeRepository.findById(id);
        
        if (trade.isPresent() && trade.get().getUser().getId().equals(currentUser.getId())) {
            tradeRepository.delete(trade.get());
            return ResponseEntity.ok().build();
        }
        
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/portfolio")
    public ResponseEntity<Map<String, Object>> getPortfolio(@CurrentUser UserPrincipal currentUser) {
        List<Trade> trades = tradeRepository.findByUserIdOrderByTradeDateDesc(currentUser.getId());
        
        BigDecimal totalInvested = tradeRepository.getTotalInvestedAmount(currentUser.getId());
        BigDecimal totalSold = tradeRepository.getTotalSoldAmount(currentUser.getId());
        
        if (totalInvested == null) totalInvested = BigDecimal.ZERO;
        if (totalSold == null) totalSold = BigDecimal.ZERO;
        
        BigDecimal currentValue = BigDecimal.ZERO;
        BigDecimal totalProfitLoss = BigDecimal.ZERO;
        
        for (Trade trade : trades) {
            try {
                BigDecimal currentPrice = marketDataService.getCurrentPrice(trade.getSymbol());
                if (currentPrice != null) {
                    BigDecimal tradeValue = currentPrice.multiply(trade.getQuantity());
                    currentValue = currentValue.add(tradeValue);
                    
                    BigDecimal tradeProfitLoss = currentPrice
                            .subtract(trade.getPricePerUnit())
                            .multiply(trade.getQuantity());
                    totalProfitLoss = totalProfitLoss.add(tradeProfitLoss);
                }
            } catch (Exception e) {
                // Handle API errors
            }
        }
        
        Map<String, Object> portfolio = Map.of(
            "totalInvested", totalInvested,
            "totalSold", totalSold,
            "currentValue", currentValue,
            "totalProfitLoss", totalProfitLoss,
            "totalTrades", trades.size()
        );
        
        return ResponseEntity.ok(portfolio);
    }

    // Request class
    public static class TradeRequest {
        private String symbol;
        private String assetType;
        private Trade.TradeType tradeType;
        private BigDecimal quantity;
        private BigDecimal pricePerUnit;
        private BigDecimal fees;
        private String notes;
        private LocalDateTime tradeDate;

        // Getters and Setters
        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getAssetType() {
            return assetType;
        }

        public void setAssetType(String assetType) {
            this.assetType = assetType;
        }

        public Trade.TradeType getTradeType() {
            return tradeType;
        }

        public void setTradeType(Trade.TradeType tradeType) {
            this.tradeType = tradeType;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPricePerUnit() {
            return pricePerUnit;
        }

        public void setPricePerUnit(BigDecimal pricePerUnit) {
            this.pricePerUnit = pricePerUnit;
        }

        public BigDecimal getFees() {
            return fees;
        }

        public void setFees(BigDecimal fees) {
            this.fees = fees;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public LocalDateTime getTradeDate() {
            return tradeDate;
        }

        public void setTradeDate(LocalDateTime tradeDate) {
            this.tradeDate = tradeDate;
        }
    }
} 