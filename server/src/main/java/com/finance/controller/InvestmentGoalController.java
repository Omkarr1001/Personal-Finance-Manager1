package com.finance.controller;

import com.finance.model.InvestmentGoal;
import com.finance.model.User;
import com.finance.repository.InvestmentGoalRepository;
import com.finance.repository.UserRepository;
import com.finance.security.CurrentUser;
import com.finance.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/goals")
@CrossOrigin(origins = "*")
public class InvestmentGoalController {

    @Autowired
    private InvestmentGoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<InvestmentGoal>> getAllGoals(@CurrentUser UserPrincipal currentUser) {
        List<InvestmentGoal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentGoal> getGoalById(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        Optional<InvestmentGoal> goal = goalRepository.findById(id);
        
        if (goal.isPresent() && goal.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.ok(goal.get());
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<InvestmentGoal> createGoal(@Valid @RequestBody GoalRequest goalRequest, 
                                                   @CurrentUser UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        InvestmentGoal goal = new InvestmentGoal();
        goal.setUser(user);
        goal.setName(goalRequest.getName());
        goal.setDescription(goalRequest.getDescription());
        goal.setTargetAmount(goalRequest.getTargetAmount());
        goal.setCurrentAmount(goalRequest.getCurrentAmount() != null ? goalRequest.getCurrentAmount() : BigDecimal.ZERO);
        goal.setTargetDate(goalRequest.getTargetDate());
        goal.setStatus(InvestmentGoal.GoalStatus.ACTIVE);

        InvestmentGoal savedGoal = goalRepository.save(goal);
        return ResponseEntity.ok(savedGoal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestmentGoal> updateGoal(@PathVariable Long id, 
                                                   @Valid @RequestBody GoalRequest goalRequest,
                                                   @CurrentUser UserPrincipal currentUser) {
        Optional<InvestmentGoal> existingGoal = goalRepository.findById(id);
        
        if (existingGoal.isPresent() && existingGoal.get().getUser().getId().equals(currentUser.getId())) {
            InvestmentGoal goal = existingGoal.get();
            goal.setName(goalRequest.getName());
            goal.setDescription(goalRequest.getDescription());
            goal.setTargetAmount(goalRequest.getTargetAmount());
            goal.setCurrentAmount(goalRequest.getCurrentAmount() != null ? goalRequest.getCurrentAmount() : BigDecimal.ZERO);
            goal.setTargetDate(goalRequest.getTargetDate());
            goal.setStatus(goalRequest.getStatus() != null ? goalRequest.getStatus() : InvestmentGoal.GoalStatus.ACTIVE);

            InvestmentGoal updatedGoal = goalRepository.save(goal);
            return ResponseEntity.ok(updatedGoal);
        }
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        Optional<InvestmentGoal> goal = goalRepository.findById(id);
        
        if (goal.isPresent() && goal.get().getUser().getId().equals(currentUser.getId())) {
            goalRepository.delete(goal.get());
            return ResponseEntity.ok().build();
        }
        
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<InvestmentGoal>> getActiveGoals(@CurrentUser UserPrincipal currentUser) {
        List<InvestmentGoal> activeGoals = goalRepository.findByUserIdAndStatus(currentUser.getId(), InvestmentGoal.GoalStatus.ACTIVE);
        return ResponseEntity.ok(activeGoals);
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<InvestmentGoal> updateProgress(@PathVariable Long id, 
                                                       @RequestBody ProgressRequest progressRequest,
                                                       @CurrentUser UserPrincipal currentUser) {
        Optional<InvestmentGoal> goal = goalRepository.findById(id);
        
        if (goal.isPresent() && goal.get().getUser().getId().equals(currentUser.getId())) {
            InvestmentGoal investmentGoal = goal.get();
            investmentGoal.setCurrentAmount(progressRequest.getCurrentAmount());
            
            // Auto-complete if target is reached
            if (investmentGoal.getCurrentAmount().compareTo(investmentGoal.getTargetAmount()) >= 0) {
                investmentGoal.setStatus(InvestmentGoal.GoalStatus.COMPLETED);
            }
            
            InvestmentGoal updatedGoal = goalRepository.save(investmentGoal);
            return ResponseEntity.ok(updatedGoal);
        }
        
        return ResponseEntity.notFound().build();
    }

    // Request classes
    public static class GoalRequest {
        private String name;
        private String description;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private LocalDateTime targetDate;
        private InvestmentGoal.GoalStatus status;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getTargetAmount() {
            return targetAmount;
        }

        public void setTargetAmount(BigDecimal targetAmount) {
            this.targetAmount = targetAmount;
        }

        public BigDecimal getCurrentAmount() {
            return currentAmount;
        }

        public void setCurrentAmount(BigDecimal currentAmount) {
            this.currentAmount = currentAmount;
        }

        public LocalDateTime getTargetDate() {
            return targetDate;
        }

        public void setTargetDate(LocalDateTime targetDate) {
            this.targetDate = targetDate;
        }

        public InvestmentGoal.GoalStatus getStatus() {
            return status;
        }

        public void setStatus(InvestmentGoal.GoalStatus status) {
            this.status = status;
        }
    }

    public static class ProgressRequest {
        private BigDecimal currentAmount;

        public BigDecimal getCurrentAmount() {
            return currentAmount;
        }

        public void setCurrentAmount(BigDecimal currentAmount) {
            this.currentAmount = currentAmount;
        }
    }
} 