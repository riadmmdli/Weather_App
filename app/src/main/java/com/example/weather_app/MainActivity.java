package com.example.weather_app;

import android.os.Bundle;
import android.os.*;
import android.view.View;
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


    class getWeather extends AsyncTask<String , Void , String>{
        @Override
        protected String doInBackground(String... urls){
            StringBuilder sb = new StringBuilder();
            try{
                URL url = new URL(urls[0]);
                HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
                urlCon.connect();

                InputStream input = urlCon.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(input));
                String line ="";
                while((line = br.readLine()) != null){
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
            super.onPostExecute(sb);
            if (sb == null) {
                show.setText("Error: Unable to fetch weather data.");
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
                e.printStackTrace();
                show.setText("Error: Unable to parse weather data.");
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.searchMaterialButton);
        show = findViewById(R.id.weather);
        final String[] temp = {""};
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Toast.makeText(MainActivity.this, "Button Clicked" , Toast.LENGTH_SHORT).show(); //shows notification below the app
                String city = cityName.getText().toString();
                try{
                    if(city!=null) {
                        url = "https://api.openweathermap.org/data/2.5/weather?q="+city+"&appid=d8140b53d0daf1f7de2456435cd301f6";
                    }else{
                        Toast.makeText(MainActivity.this , "Enter City" , Toast.LENGTH_SHORT).show();
                    }
                    getWeather task = new getWeather();
                    temp[0] = task.execute(url).get();

                }catch(ExecutionException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                if(temp[0] == null){
                    show.setText("Could not be able to find the details");
                }

            }
        });
    }
}