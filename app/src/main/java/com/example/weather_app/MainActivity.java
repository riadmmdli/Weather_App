package com.example.weather_app;

import android.os.Bundle;
import android.os.*;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    TextView cityName;
    Button search;
    TextView show;
    String url;
    boolean isSearchInProgress = false; // Flag to track search state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.searchMaterialButton);
        show = findViewById(R.id.weather);

        // Set onClickListener for the search button
        search.setOnClickListener(view -> performSearch());

        // Add onEditorActionListener to handle Enter key
        cityName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(); // Trigger search when Enter is pressed
                return true; // Indicate the action was handled
            }
            return false; // Let the system handle other actions
        });
    }

    /**
     * Method to perform search logic
     */
    private void performSearch() {
        if (isSearchInProgress) {
            Toast.makeText(MainActivity.this, "Search is already in progress. Please wait.", Toast.LENGTH_SHORT).show();
            return; // Prevent multiple searches at the same time
        }

        // Clear previous error messages or data
        show.setText("");

        // Get the city name from the user input
        String city = cityName.getText().toString();

        // Ensure city is not empty and show a "Searching..." Toast
        if (city == null || city.isEmpty()) {
            Toast.makeText(MainActivity.this, "Enter City", Toast.LENGTH_SHORT).show();
            return; // Stop further processing if city is empty
        }



        // Construct the URL with the city name
        url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=d8140b53d0daf1f7de2456435cd301f6";

        // Set the search flag to true
        isSearchInProgress = true;

        try {
            // Fetch weather data asynchronously
            getWeather task = new getWeather();
            task.execute(url);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Error: Unable to initiate search.", Toast.LENGTH_LONG).show();
            isSearchInProgress = false; // Reset the flag in case of failure
        }
    }

    // AsyncTask to fetch weather data
    class getWeather extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
                urlCon.connect();

                InputStream input = urlCon.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String sb) {
            isSearchInProgress = false; // Reset the search flag
            super.onPostExecute(sb);
            if (sb == null) {
                Toast.makeText(MainActivity.this, "Error: Unable to fetch weather data.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                // Parse the JSON response
                JSONObject jso = new JSONObject(sb);
                String cityName = jso.getString("name");
                JSONObject main = jso.getJSONObject("main");

                // Extract values
                double tempKelvin = main.getDouble("temp");
                double feelsLikeKelvin = main.getDouble("feels_like");
                double tempMinKelvin = main.getDouble("temp_min");
                double tempMaxKelvin = main.getDouble("temp_max");
                int humidity = main.getInt("humidity");
                int pressure = main.getInt("pressure");

                // Convert Kelvin to Celsius
                double tempCelsius = tempKelvin - 273.15;
                double feelsLikeCelsius = feelsLikeKelvin - 273.15;
                double tempMinCelsius = tempMinKelvin - 273.15;
                double tempMaxCelsius = tempMaxKelvin - 273.15;

                // Display City Name and Temperature
                TextView cityNameDisplay = findViewById(R.id.cityNameDisplay);
                TextView temperatureText = findViewById(R.id.temperatureText);
                cityNameDisplay.setText(cityName);
                temperatureText.setText(String.format("%.2f°C", tempCelsius));

                // Update additional attributes
                TextView feelsLikeText = findViewById(R.id.feelsLikeText);
                TextView minTempText = findViewById(R.id.minTempText);
                TextView maxTempText = findViewById(R.id.maxTempText);
                TextView humidityText = findViewById(R.id.humidityText);
                TextView pressureText = findViewById(R.id.pressureText);

                feelsLikeText.setText(String.format("Feels Like: %.2f°C", feelsLikeCelsius));
                minTempText.setText(String.format("Min Temp: %.2f°C", tempMinCelsius));
                maxTempText.setText(String.format("Max Temp: %.2f°C", tempMaxCelsius));
                humidityText.setText(String.format("Humidity: %d%%", humidity));
                pressureText.setText(String.format("Pressure: %d hPa", pressure));
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, "Error: Unable to parse weather data.", Toast.LENGTH_LONG).show();
            }
        }
    }
}


