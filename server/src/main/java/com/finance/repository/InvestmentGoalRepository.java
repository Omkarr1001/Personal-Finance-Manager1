package com.finance.repository;

import com.finance.model.InvestmentGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentGoalRepository extends JpaRepository<InvestmentGoal, Long> {
    List<InvestmentGoal> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<InvestmentGoal> findByUserIdAndStatus(Long userId, InvestmentGoal.GoalStatus status);
} 