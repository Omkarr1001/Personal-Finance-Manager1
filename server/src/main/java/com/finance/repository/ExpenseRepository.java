package com.finance.repository;

import com.finance.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);
    
    List<Expense> findByUserIdAndCategoryOrderByExpenseDateDesc(Long userId, String category);
    
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate ORDER BY e.expenseDate DESC")
    List<Expense> findByUserIdAndExpenseDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT e.category, SUM(e.amount) as totalAmount FROM Expense e WHERE e.user.id = :userId GROUP BY e.category")
    List<Object[]> getExpensesByCategory(Long userId);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal getTotalExpenses(Long userId);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpensesByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
} 