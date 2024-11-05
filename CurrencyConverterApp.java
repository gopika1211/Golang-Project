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
        createAndShowGUI();
    }

    // GUI creation and event handling
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Currency Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 250);
        frame.setLayout(new FlowLayout());

        JTextField amountField = new JTextField(10);
        JComboBox<String> baseCurrencyDropdown = new JComboBox<>();
        JComboBox<String> targetCurrencyDropdown = new JComboBox<>();
        JButton convertButton = new JButton("Convert");
        JLabel resultLabel = new JLabel("Converted Amount: ");
        JLabel errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);

        frame.add(new JLabel("Amount: "));
        frame.add(amountField);
        frame.add(new JLabel("From Currency: "));
        frame.add(baseCurrencyDropdown);
        frame.add(new JLabel("To Currency: "));
        frame.add(targetCurrencyDropdown);
        frame.add(convertButton);
        frame.add(resultLabel);
        frame.add(errorLabel);

        loadCurrencyCodes(baseCurrencyDropdown, targetCurrencyDropdown);

        convertButton.addActionListener((ActionEvent e) -> {
            String amountText = amountField.getText();
            String baseCurrency = (String) baseCurrencyDropdown.getSelectedItem();
            String targetCurrency = (String) targetCurrencyDropdown.getSelectedItem();
            try {
                double amount = Double.parseDouble(amountText);
                double convertedAmount = convertCurrency(amount, baseCurrency, targetCurrency);
                resultLabel.setText(String.format("Converted Amount: %.2f %s", convertedAmount, targetCurrency));
                errorLabel.setText("");
            } catch (NumberFormatException ex) {
                errorLabel.setText("Please enter a valid number.");
            } catch (Exception ex) {
                errorLabel.setText("Error retrieving conversion rate.");
                ex.printStackTrace();
            }
        });

        frame.setVisible(true);
    }

    // Method to load available currency codes from the API
    private static void loadCurrencyCodes(JComboBox<String> baseCurrencyDropdown, JComboBox<String> targetCurrencyDropdown) {
        try {
            String response = makeApiRequest(API_URL + API_KEY + "/latest/USD");
            Map<String, Double> conversionRates = parseJsonResponse(response);
            conversionRates.keySet().forEach(currency -> {
                baseCurrencyDropdown.addItem(currency);
                targetCurrencyDropdown.addItem(currency);
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading currency codes.");
        }
    }

    // Method to convert currency based on API data
    private static double convertCurrency(double amount, String baseCurrency, String targetCurrency) throws Exception {
        String response = makeApiRequest(API_URL + API_KEY + "/latest/" + baseCurrency);
        Map<String, Double> conversionRates = parseJsonResponse(response);

        if (!conversionRates.containsKey(targetCurrency)) {
            throw new IllegalArgumentException("Invalid target currency: " + targetCurrency);
        }

        return amount * conversionRates.get(targetCurrency);
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
                content.append(line);
            }
            return content.toString();
        } finally {
            conn.disconnect();
        }
    }

    // Method to parse JSON response and extract conversion rates
    private static Map<String, Double> parseJsonResponse(String jsonResponse) {
        Map<String, Double> rates = new HashMap<>();
        String[] parts = jsonResponse.split("\"conversion_rates\":\\{")[1].split("\\}")[0].split(",");

        for (String part : parts) {
            String[] keyValue = part.trim().replaceAll("\"", "").split(":");
            rates.put(keyValue[0], Double.parseDouble(keyValue[1]));
        }

        System.out.println("Current Exchange Rates: " + rates); // Debugging log
        return rates;
    }
}
