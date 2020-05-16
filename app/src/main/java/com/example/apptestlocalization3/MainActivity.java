package com.example.apptestlocalization3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/*
caixa
57 até a parte superior da saida
62 até a parte inferior da saida
8 até o sensor
 */

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private TextView tvGeral;
    private TextView tvClima;
    private Button btUpdate;

    private boolean requestingLocationUpdates = false;
    private int cont = 0;

    private Localizacao localizacao = new Localizacao();

    final int REQUEST_CHECK_SETTINGS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvGeral = (TextView) findViewById(R.id.tvGeral);
        tvClima = (TextView) findViewById(R.id.tvClima);

        btUpdate = (Button) findViewById(R.id.btUpdate);

        pedirPermissoes();
        createLocationRequest();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestingLocationUpdates) {
                    onPause();
                    btUpdate.setText("Pause");
                    tvGeral.setText("");
                    tvClima.setText("");
                }
                else {
                    startLocationUpdates();
                    //onResume();
                    btUpdate.setText("Start");
                }
            }

        });

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

/*
        //chamado quando há falha no pedido de localização
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        //solicita autorização para obter a localização
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        //Toast.makeText(getApplicationContext(), "Autorização negada", Toast.LENGTH_LONG).show();
                    } catch (IntentSender.SendIntentException sendEx) {
                        Toast.makeText(getApplicationContext(), "Erro Autorização", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
*/

        //remover caso não queira uma verificação inicial
        //chamado quando se verifica o resultado da última localização indepedentimente da conexão está ativada ou não
        /*
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                updateData(location);
            }
        });
         */

        locationCallback = new LocationCallback() {
            @Override
            //chamado quando a conexão está ativada e se tem um resultado
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    tvGeral.setText("Erro: location null");
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    updateData(location);
                }
            }

            //Chamado quando há uma alteração na disponibilidade dos dados do local.
            @Override
            public void onLocationAvailability(LocationAvailability avail) {
                super.onLocationAvailability(avail);

                //testa se a localização esta habilitada
                if (!avail.isLocationAvailable()){
                    Toast.makeText(getApplicationContext(), "Habilite a localização para utilizar todas as funcionalidades", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    //chamado quando se tem uma resposta da solicitação de autorização da activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        //Toast.makeText(getApplicationContext(), "teste5", Toast.LENGTH_LONG).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getApplicationContext(), "Não será possível conhecer sua localização", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        //Toast.makeText(getApplicationContext(), "teste7", Toast.LENGTH_LONG).show();
                        break;
                }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        //Toast.makeText(getApplicationContext(), "teste5", Toast.LENGTH_LONG).show();
        super.onResume();
        /*
        if (!requestingLocationUpdates) {
            startLocationUpdates();
        }

         */
    }

    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        requestingLocationUpdates = true;
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        requestingLocationUpdates = false;
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        //locationRequest.setFastestInterval(15000);
        //locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //verificar a possibilidade de conexão por outras redes
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void updateData(Location location){
        if (location == null) {
            tvGeral.setText("Erro: location null");
            return;
        }

        localizacao.setLatitude(location.getLatitude());
        localizacao.setLongitude(location.getLongitude());
        localizacao.setProvider(location.getProvider());
        localizacao.setTime(location.getTime());
        localizacao.setAccuracy(location.getAccuracy());
        localizacao.setAltitude(location.getAltitude());
        localizacao.setSpeed(location.getSpeed());

        tvGeral.setText("Latitude: " + localizacao.getLatitude() + "\nLogitude: " + localizacao.getLongitude() + "\nProvider: " + localizacao.getProvider()
                + "\nTime: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(localizacao.getTime()) + "\nAccuracy: " + localizacao.getAccuracy()
                + "\nAltitude: " + localizacao.getAltitude() + "\nSpeed: " + localizacao.getSpeed());

        updateClima(localizacao);
    }

    private void updateClima(Localizacao localizacao){
        //realiza a busca das informações do clima na cidade de Jaboatão dos Guararapes - PE
        DownloadJsonAsyncTask downloadJson = new DownloadJsonAsyncTask(new DownloadJsonAsyncTask.AsyncResponseJson() {
            ProgressDialog dialog;
            @Override
            public void processFinish(Clima result) {
                if (result != null ) {
                    String temp = "";

                    temp += "Cidade: " + result.getCityName() + "\n";
                    temp += "Data: " + result.getDate() + "\n";
                    temp += "Temperatura: " + result.getTemp() + "\n";
                    temp += "Humidade: " + result.getHumidity() + "\n\n";

                    for (Clima.ClimaDia tempClima : result.getClimaDia()) {
                        temp += "Data: " + tempClima.getDate() + "\n";
                        temp += " Weekday: " + tempClima.getWeekday() + "\n";
                        temp += " Max: " + tempClima.getMax() + "\n";
                        temp += " Min: " + tempClima.getMin() + "\n";
                        temp += " Condition: " + tempClima.getCondition() + "\n\n\n";
                    }
                    cont++;
                    temp = "Contagem: " + cont + "\n\n" + temp;
                    tvClima.setText(temp);
                }else{
                    tvClima.setText("Contagem: " + cont + "\n\n" + "Erro ao obter os dados");
                }
                dialog.dismiss();
                //interrompe as atualizações
                stopLocationUpdates();
            }

            @Override
            public void processStart() {
                dialog = ProgressDialog.show(MainActivity.this, "Aguarde", "Fazendo download do JSON");
            }

        });
        downloadJson.execute("https://api.hgbrasil.com/weather?array_limit=3&fields=only_results,humidity,temp,city_name,forecast,condition,weekday,max,min,date&key=bdda2060&lat=" +
                localizacao.getLatitude() + "&log=" + localizacao.getLongitude() + "&user_ip=remote");
    }

    private void pedirPermissoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CHECK_SETTINGS);
            }
        }
    }
}