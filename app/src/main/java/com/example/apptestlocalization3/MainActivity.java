package com.example.apptestlocalization3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

/*
caixa
57 até a parte superior da saida
62 até a parte inferior da saida
8 até o sensor
 */

public class MainActivity extends AppCompatActivity {

    final int REQUEST_CHECK_SETTINGS = 10;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private TextView tvGeral;
    private Button btUpdate;

    private boolean requestingLocationUpdates = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvGeral = (TextView) findViewById(R.id.tvGeral);
        btUpdate = (Button) findViewById(R.id.btUpdate);

        pedirPermissoes();
        createLocationRequest();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        /*
        //chamado quando a activity é inicializada e a localização está ativa
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                Toast.makeText(getApplicationContext(), "Autorização ok", Toast.LENGTH_LONG).show();
            }
        });
         */

        //chamado quando há falha no pedido de localização
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "teste0", Toast.LENGTH_LONG).show();
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    Toast.makeText(getApplicationContext(), "teste1", Toast.LENGTH_LONG).show();
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        Toast.makeText(getApplicationContext(), "Autorização negada", Toast.LENGTH_LONG).show();
                    } catch (IntentSender.SendIntentException sendEx) {
                        Toast.makeText(getApplicationContext(), "Erro Autorização", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        //chamado quando se verifica o resultado da última localização indepedentimente da conexão está ativada ou não
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                Toast.makeText(getApplicationContext(), "teste-10", Toast.LENGTH_LONG).show();
                if (location != null) {
                    tvGeral.setText("Latitude: " + location.getLatitude() + "\nLogitude: " + location.getLongitude() + "\nProvider: " + location.getProvider()
                            + "\nTime: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(location.getTime()) + "\nAccuracy: " + location.getAccuracy()
                            + "\nAltitude: " + location.getAltitude() + "\nSpeed: " + location.getSpeed());
                } else {
                        tvGeral.setText("Erro: location null");
                }
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            //chamado quando a conexão está ativada e se tem um resultado
            public void onLocationResult(LocationResult locationResult) {
                Toast.makeText(getApplicationContext(), "teste-11", Toast.LENGTH_LONG).show();
                if (locationResult == null) {
                    tvGeral.setText("Erro: location null");
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    tvGeral.setText("Latitude: " + location.getLatitude() + "\nLogitude: " + location.getLongitude() + "\nProvider: " + location.getProvider()
                            + "\nTime: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(location.getTime()) + "\nAccuracy: " + location.getAccuracy()
                            + "\nAltitude: " + location.getAltitude() + "\nSpeed: " + location.getSpeed());
                }
            }

            //Chamado quando há uma alteração na disponibilidade dos dados do local.
            @Override
            public void onLocationAvailability(LocationAvailability avail) {
                super.onLocationAvailability(avail);

                //testa se a localização esta habilitada
                if (avail.isLocationAvailable())
                    Toast.makeText(getApplicationContext(), "teste-2", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "teste-3", Toast.LENGTH_LONG).show();
            }
        };
    }

    //chamado quando se tem um resposta da solicitação de autorização da activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getApplicationContext(), "teste-12", Toast.LENGTH_LONG).show();
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getApplicationContext(), "teste5", Toast.LENGTH_LONG).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getApplicationContext(), "teste6", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "teste7", Toast.LENGTH_LONG).show();
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
        super.onResume();
        if (!requestingLocationUpdates) {
            startLocationUpdates();
        }
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

    private void pedirPermissoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CHECK_SETTINGS);
            }
        }
    }

    /*
    private void pedirPermissoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CHECK_SETTINGS);
            } else {
                //configurarServico();
                createLocationRequest();
            }
        }
    }
     */

    /*
    public void configurarServico(){
        try {
            Toast.makeText(this, "configurarServico", Toast.LENGTH_LONG).show();
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 5, locationListener, null);
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener, null);
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener, null);
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (android.location.LocationListener) locationListener, null);
        }catch(SecurityException ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
     */
}