import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class CurrencyConverterApp {

    private static final String API_KEY = "eab8cccbb67dd35227b32b9b"; // Your API key
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/";

    public static void main(String[] args) {
        createAndShowGUI(); // Initialize the GUI
    }

    // GUI creation and event handling
    private static void createAndShowGUI() {
        // Create a new JFrame for the application window
        JFrame frame = new JFrame("Currency Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 250);
        frame.setLayout(new FlowLayout());

        // Input field for amount
        JTextField amountField = new JTextField(10);
        // Dropdowns for base and target currencies
        JComboBox<String> baseCurrencyDropdown = new JComboBox<>();
        JComboBox<String> targetCurrencyDropdown = new JComboBox<>();
        // Button to trigger conversion
        JButton convertButton = new JButton("Convert");
        // Labels to display results and errors
        JLabel resultLabel = new JLabel("Converted Amount: ");
        JLabel errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED); // Set error label color to red

        // Add components to the frame
        frame.add(new JLabel("Amount: "));
        frame.add(amountField);
        frame.add(new JLabel("From Currency: "));
        frame.add(baseCurrencyDropdown);
        frame.add(new JLabel("To Currency: "));
        frame.add(targetCurrencyDropdown);
        frame.add(convertButton);
        frame.add(resultLabel);
        frame.add(errorLabel);

        // Load available currency codes into dropdowns
        loadCurrencyCodes(baseCurrencyDropdown, targetCurrencyDropdown);

        // Action listener for the convert button
        convertButton.addActionListener((ActionEvent e) -> {
            String amountText = amountField.getText();
            String baseCurrency = (String) baseCurrencyDropdown.getSelectedItem();
            String targetCurrency = (String) targetCurrencyDropdown.getSelectedItem();
            try {
                // Parse the amount entered by the user
                double amount = Double.parseDouble(amountText);
                // Convert the currency
                double convertedAmount = convertCurrency(amount, baseCurrency, targetCurrency);
                // Update result label with converted amount
                resultLabel.setText(String.format("Converted Amount: %.2f %s", convertedAmount, targetCurrency));
                errorLabel.setText(""); // Clear error label
            } catch (NumberFormatException ex) {
                errorLabel.setText("Please enter a valid number."); // Handle invalid number input
            } catch (Exception ex) {
                errorLabel.setText("Error retrieving conversion rate."); // Handle API errors
                ex.printStackTrace();
            }
        });

        frame.setVisible(true); // Make the frame visible
    }

    // Method to load available currency codes from the API
    private static void loadCurrencyCodes(JComboBox<String> baseCurrencyDropdown, JComboBox<String> targetCurrencyDropdown) {
        try {
            // Make an API request to get the latest currency rates
            String response = makeApiRequest(API_URL + API_KEY + "/latest/USD");
            Map<String, Double> conversionRates = parseJsonResponse(response);
            // Add currency codes to dropdowns
            conversionRates.keySet().forEach(currency -> {
                baseCurrencyDropdown.addItem(currency);
                targetCurrencyDropdown.addItem(currency);
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading currency codes."); // Show error dialog
        }
    }

    // Method to convert currency based on API data
    private static double convertCurrency(double amount, String baseCurrency, String targetCurrency) throws Exception {
        // Make an API request for the base currency
        String response = makeApiRequest(API_URL + API_KEY + "/latest/" + baseCurrency);
        Map<String, Double> conversionRates = parseJsonResponse(response);

        if (!conversionRates.containsKey(targetCurrency)) {
            throw new IllegalArgumentException("Invalid target currency: " + targetCurrency); // Handle invalid target currency
        }

        return amount * conversionRates.get(targetCurrency); // Convert the amount
    }

    // Centralized method to handle API requests
    private static String makeApiRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line); // Read the response
            }
            return content.toString(); // Return the response
        } finally {
            conn.disconnect(); // Ensure connection is closed
        }
    }

    // Method to parse JSON response and extract conversion rates
    private static Map<String, Double> parseJsonResponse(String jsonResponse) {
        Map<String, Double> rates = new HashMap<>();
        String[] parts = jsonResponse.split("\"conversion_rates\":\\{")[1].split("\\}")[0].split(",");

        for (String part : parts) {
            String[] keyValue = part.trim().replaceAll("\"", "").split(":");
            rates.put(keyValue[0], Double.parseDouble(keyValue[1])); // Add rate to the map
        }

        System.out.println("Current Exchange Rates: " + rates); // Debugging log
        return rates;
    }
}

