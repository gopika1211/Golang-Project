package main

import (
    "encoding/json"
    "fmt"
    "io/ioutil"
    "net/http"
    "os"
)

// API details
const (
    apiKey   = "eab8cccbb67dd35227b32b9b" // Your API key
    apiURL   = "https://v6.exchangerate-api.com/v6/"
)

// ConversionResponse represents the structure of the API response
type ConversionResponse struct {
    ConversionRates map[string]float64 `json:"conversion_rates"`
}

func main() {
    var amount float64
    var baseCurrency, targetCurrency string

    // User input
    fmt.Print("Enter amount: ")
    fmt.Scan(&amount)

    fmt.Print("Enter base currency (e.g., USD): ")
    fmt.Scan(&baseCurrency)

    fmt.Print("Enter target currency (e.g., EUR): ")
    fmt.Scan(&targetCurrency)

    // Convert currency
    convertedAmount, err := convertCurrency(amount, baseCurrency, targetCurrency)
    if err != nil {
        fmt.Println("Error:", err)
        return
    }

    fmt.Printf("Converted Amount: %.2f %s\n", convertedAmount, targetCurrency)
}

// convertCurrency fetches conversion rates and calculates the converted amount
func convertCurrency(amount float64, baseCurrency, targetCurrency string) (float64, error) {
    // API request for conversion rates
    response, err := http.Get(apiURL + apiKey + "/latest/" + baseCurrency)
    if err != nil {
        return 0, fmt.Errorf("failed to fetch conversion rates: %w", err)
    }
    defer response.Body.Close()

    // Read response body
    body, err := ioutil.ReadAll(response.Body)
    if err != nil {
        return 0, fmt.Errorf("failed to read response body: %w", err)
    }

    // Parse JSON response
    var conversionResponse ConversionResponse
    if err := json.Unmarshal(body, &conversionResponse); err != nil {
        return 0, fmt.Errorf("failed to parse JSON response: %w", err)
    }

    // Get the conversion rate
    rate, exists := conversionResponse.ConversionRates[targetCurrency]
    if !exists {
        return 0, fmt.Errorf("invalid target currency: %s", targetCurrency)
    }

    return amount * rate, nil // Return the converted amount
}
