import React from "react";
import "./Dashboard.css";
import Trades from "./Trades";
import Expenses from "./Expenses";
import Goals from "./Goals";

function Dashboard() {
  return (
    <div className="dashboard-container">
      {/* Header */}
      <header className="dashboard-header">
        <h1>📊 Finance Manager Dashboard</h1>
        <p>Track your portfolio, expenses, and goals in one place</p>
      </header>

      {/* Grid Layout */}
      <div className="dashboard-grid">
        {/* Portfolio Section */}
        <div className="dashboard-card large">
          <h2>Portfolio Overview</h2>
          <div className="chart-placeholder">
            <p>[Chart will be here]</p>
          </div>
          <Trades />
        </div>

        {/* Expenses Section */}
        <div className="dashboard-card">
          <h2>Recent Expenses</h2>
          <Expenses />
        </div>

        {/* Goals Section */}
        <div className="dashboard-card">
          <h2>Financial Goals</h2>
          <Goals />
        </div>

        {/* Reports Section */}
        <div className="dashboard-card">
          <h2>Reports & Insights</h2>
          <p>
            Coming soon: Add spending trends, income vs expenses, and more
            insights here.
          </p>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
