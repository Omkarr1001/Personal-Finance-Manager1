package com.finance.repository;

import com.finance.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserIdOrderByTradeDateDesc(Long userId);
    
    List<Trade> findByUserIdAndSymbolOrderByTradeDateDesc(Long userId, String symbol);
    
    List<Trade> findByUserIdAndAssetTypeOrderByTradeDateDesc(Long userId, String assetType);
    
    @Query("SELECT t FROM Trade t WHERE t.user.id = :userId AND t.tradeDate BETWEEN :startDate AND :endDate ORDER BY t.tradeDate DESC")
    List<Trade> findByUserIdAndTradeDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT SUM(t.totalAmount) FROM Trade t WHERE t.user.id = :userId AND t.tradeType = 'BUY'")
    BigDecimal getTotalInvestedAmount(Long userId);
    
    @Query("SELECT SUM(t.totalAmount) FROM Trade t WHERE t.user.id = :userId AND t.tradeType = 'SELL'")
    BigDecimal getTotalSoldAmount(Long userId);
    
    @Query("SELECT t.symbol, SUM(t.quantity) as totalQuantity FROM Trade t WHERE t.user.id = :userId GROUP BY t.symbol")
    List<Object[]> getHoldingsBySymbol(Long userId);
} 