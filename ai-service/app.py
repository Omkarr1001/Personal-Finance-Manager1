from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import StandardScaler
import joblib
import os
from typing import List, Dict, Any
import requests
from datetime import datetime, timedelta
import json

app = FastAPI(title="Finance AI Service", version="1.0.0")

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Pydantic models
class TradeData(BaseModel):
    symbol: str
    price: float
    volume: float
    date: str
    trade_type: str

class PortfolioData(BaseModel):
    trades: List[TradeData]
    risk_tolerance: str = "medium"  # low, medium, high

class RecommendationRequest(BaseModel):
    symbol: str
    current_price: float
    historical_data: List[Dict[str, Any]]
    user_risk_tolerance: str = "medium"

class MarketAnalysisRequest(BaseModel):
    symbols: List[str]
    analysis_type: str = "trend"  # trend, volatility, correlation

# Global variables for models
price_predictor = None
scaler = None
model_loaded = False

def load_or_train_model():
    """Load existing model or train a new one"""
    global price_predictor, scaler, model_loaded
    
    model_path = "model/price_predictor.pkl"
    scaler_path = "model/scaler.pkl"
    
    if os.path.exists(model_path) and os.path.exists(scaler_path):
        try:
            price_predictor = joblib.load(model_path)
            scaler = joblib.load(scaler_path)
            model_loaded = True
            print("Model loaded successfully")
        except Exception as e:
            print(f"Error loading model: {e}")
            train_model()
    else:
        train_model()

def train_model():
    """Train a simple price prediction model with mock data"""
    global price_predictor, scaler, model_loaded
    
    # Generate mock training data
    np.random.seed(42)
    n_samples = 1000
    
    # Mock features: price, volume, day_of_week, month, volatility
    prices = np.random.uniform(10, 500, n_samples)
    volumes = np.random.uniform(1000, 1000000, n_samples)
    days_of_week = np.random.randint(0, 7, n_samples)
    months = np.random.randint(1, 13, n_samples)
    volatility = np.random.uniform(0.01, 0.5, n_samples)
    
    # Create target: next day's price (with some randomness)
    next_prices = prices * (1 + np.random.normal(0, 0.02, n_samples))
    
    # Prepare features
    X = np.column_stack([prices, volumes, days_of_week, months, volatility])
    y = next_prices
    
    # Scale features
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)
    
    # Train model
    price_predictor = RandomForestRegressor(n_estimators=100, random_state=42)
    price_predictor.fit(X_scaled, y)
    
    # Save model
    os.makedirs("model", exist_ok=True)
    joblib.dump(price_predictor, "model/price_predictor.pkl")
    joblib.dump(scaler, "model/scaler.pkl")
    
    model_loaded = True
    print("Model trained and saved successfully")

@app.on_event("startup")
async def startup_event():
    load_or_train_model()

@app.get("/")
async def root():
    return {"message": "Finance AI Service is running", "model_loaded": model_loaded}

@app.post("/predict-price")
async def predict_price(request: RecommendationRequest):
    """Predict future price for a given symbol"""
    if not model_loaded:
        raise HTTPException(status_code=500, detail="Model not loaded")
    
    try:
        # Extract features from historical data
        if not request.historical_data:
            raise HTTPException(status_code=400, detail="Historical data required")
        
        # Use the most recent data point for prediction
        latest_data = request.historical_data[-1]
        
        # Extract features (simplified)
        current_price = request.current_price
        volume = latest_data.get('volume', 1000000)  # Default volume
        day_of_week = datetime.now().weekday()
        month = datetime.now().month
        volatility = latest_data.get('volatility', 0.1)  # Default volatility
        
        # Prepare features
        features = np.array([[current_price, volume, day_of_week, month, volatility]])
        features_scaled = scaler.transform(features)
        
        # Predict
        predicted_price = price_predictor.predict(features_scaled)[0]
        
        # Calculate confidence based on model's feature importance
        confidence = 0.7 + np.random.uniform(0, 0.2)  # Mock confidence
        
        return {
            "symbol": request.symbol,
            "current_price": request.current_price,
            "predicted_price": round(predicted_price, 2),
            "confidence": round(confidence, 2),
            "prediction_date": datetime.now().isoformat()
        }
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction error: {str(e)}")

@app.post("/get-recommendations")
async def get_recommendations(request: PortfolioData):
    """Generate trading recommendations based on portfolio data"""
    try:
        recommendations = []
        
        # Analyze each trade
        for trade in request.trades:
            # Simple recommendation logic
            if trade.trade_type == "BUY":
                # If bought recently, might want to hold or sell
                recommendation = "HOLD"
                confidence = 0.6
            else:
                # If sold recently, might want to buy back
                recommendation = "BUY"
                confidence = 0.7
            
            # Adjust based on risk tolerance
            if request.risk_tolerance == "low":
                confidence *= 0.8
            elif request.risk_tolerance == "high":
                confidence *= 1.2
            
            recommendations.append({
                "symbol": trade.symbol,
                "recommendation": recommendation,
                "confidence": round(min(confidence, 1.0), 2),
                "reason": f"Based on {trade.trade_type} trade on {trade.date}",
                "current_price": trade.price
            })
        
        # Add some general market recommendations
        general_recommendations = [
            {
                "symbol": "AAPL",
                "recommendation": "BUY",
                "confidence": 0.75,
                "reason": "Strong fundamentals and consistent growth",
                "current_price": 150.00
            },
            {
                "symbol": "TSLA",
                "recommendation": "HOLD",
                "confidence": 0.65,
                "reason": "High volatility, monitor closely",
                "current_price": 250.00
            }
        ]
        
        return {
            "portfolio_recommendations": recommendations,
            "general_recommendations": general_recommendations,
            "analysis_date": datetime.now().isoformat()
        }
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Recommendation error: {str(e)}")

@app.post("/analyze-market")
async def analyze_market(request: MarketAnalysisRequest):
    """Analyze market trends and patterns"""
    try:
        analysis_results = {}
        
        for symbol in request.symbols:
            # Mock market analysis
            volatility = np.random.uniform(0.1, 0.4)
            trend = np.random.choice(["bullish", "bearish", "neutral"])
            strength = np.random.uniform(0.3, 0.9)
            
            analysis_results[symbol] = {
                "volatility": round(volatility, 3),
                "trend": trend,
                "strength": round(strength, 2),
                "recommendation": "BUY" if trend == "bullish" and strength > 0.6 else "HOLD",
                "risk_level": "HIGH" if volatility > 0.3 else "MEDIUM" if volatility > 0.15 else "LOW"
            }
        
        return {
            "analysis_type": request.analysis_type,
            "symbols_analyzed": request.symbols,
            "results": analysis_results,
            "analysis_date": datetime.now().isoformat()
        }
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Market analysis error: {str(e)}")

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "model_loaded": model_loaded,
        "timestamp": datetime.now().isoformat()
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000) 