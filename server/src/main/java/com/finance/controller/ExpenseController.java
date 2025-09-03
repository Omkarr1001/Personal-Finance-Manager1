package com.finance.controller;

import com.finance.model.Expense;
import com.finance.model.User;
import com.finance.repository.ExpenseRepository;
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
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses(@CurrentUser UserPrincipal currentUser) {
        List<Expense> expenses = expenseRepository.findByUserIdOrderByExpenseDateDesc(currentUser.getId());
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        Optional<Expense> expense = expenseRepository.findById(id);
        
        if (expense.isPresent() && expense.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.ok(expense.get());
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(@Valid @RequestBody ExpenseRequest expenseRequest, 
                                               @CurrentUser UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setDescription(expenseRequest.getDescription());
        expense.setAmount(expenseRequest.getAmount());
        expense.setCategory(expenseRequest.getCategory());
        expense.setNotes(expenseRequest.getNotes());
        expense.setExpenseDate(expenseRequest.getExpenseDate() != null ? expenseRequest.getExpenseDate() : LocalDateTime.now());

        Expense savedExpense = expenseRepository.save(expense);
        return ResponseEntity.ok(savedExpense);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable Long id, 
                                               @Valid @RequestBody ExpenseRequest expenseRequest,
                                               @CurrentUser UserPrincipal currentUser) {
        Optional<Expense> existingExpense = expenseRepository.findById(id);
        
        if (existingExpense.isPresent() && existingExpense.get().getUser().getId().equals(currentUser.getId())) {
            Expense expense = existingExpense.get();
            expense.setDescription(expenseRequest.getDescription());
            expense.setAmount(expenseRequest.getAmount());
            expense.setCategory(expenseRequest.getCategory());
            expense.setNotes(expenseRequest.getNotes());
            expense.setExpenseDate(expenseRequest.getExpenseDate() != null ? expenseRequest.getExpenseDate() : LocalDateTime.now());

            Expense updatedExpense = expenseRepository.save(expense);
            return ResponseEntity.ok(updatedExpense);
        }
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        Optional<Expense> expense = expenseRepository.findById(id);
        
        if (expense.isPresent() && expense.get().getUser().getId().equals(currentUser.getId())) {
            expenseRepository.delete(expense.get());
            return ResponseEntity.ok().build();
        }
        
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getExpenseSummary(@CurrentUser UserPrincipal currentUser) {
        BigDecimal totalExpenses = expenseRepository.getTotalExpenses(currentUser.getId());
        List<Object[]> expensesByCategory = expenseRepository.getExpensesByCategory(currentUser.getId());
        
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;
        
        Map<String, Object> summary = Map.of(
            "totalExpenses", totalExpenses,
            "expensesByCategory", expensesByCategory
        );
        
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Expense>> getExpensesByCategory(@PathVariable String category, 
                                                             @CurrentUser UserPrincipal currentUser) {
        List<Expense> expenses = expenseRepository.findByUserIdAndCategoryOrderByExpenseDateDesc(currentUser.getId(), category);
        return ResponseEntity.ok(expenses);
    }

    // Request class
    public static class ExpenseRequest {
        private String description;
        private BigDecimal amount;
        private String category;
        private String notes;
        private LocalDateTime expenseDate;

        // Getters and Setters
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public LocalDateTime getExpenseDate() {
            return expenseDate;
        }

        public void setExpenseDate(LocalDateTime expenseDate) {
            this.expenseDate = expenseDate;
        }
    }
} 