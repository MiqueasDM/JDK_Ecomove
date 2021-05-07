package com.example.jdk_ecomove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;
import android.os.Looper;

import com.github.lucadruda.iotc.device.IoTCClient;
import com.github.lucadruda.iotc.device.callbacks.CommandCallback;
import com.github.lucadruda.iotc.device.callbacks.PropertiesCallback;
import com.github.lucadruda.iotc.device.enums.IOTC_COMMAND_RESPONSE;
import com.github.lucadruda.iotc.device.enums.IOTC_CONNECT;
import com.github.lucadruda.iotc.device.enums.IOTC_EVENTS;
import com.github.lucadruda.iotc.device.exceptions.IoTCentralException;
import com.github.lucadruda.iotc.device.models.IoTCProperty;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
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

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    final static String TELEMETRY_COMPONENT = "firstComponent";
    final static String TELEMETRY_FIELD = "temperature";
    ToggleButton apagado;
    private String deviceId="22uk5z8higm";
    private String scopeId="0ne002AC476";
    private String deviceKey="oEYwmtVlhKEycnBPY6fIJVFwecOz0eV8lKWyWTWkoNw=";
    private String matriculaActual="";

    private IoTCClient ioTCClient;


    int LOCATION_REQUEST_CODE = 10001;
    private static final String TAG = "MainActivity";


    private LocationManager locManager;
    private Location loc;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for(Location location: locationResult.getLocations()) {
                Log.d(TAG, "onLocationResult: " + location.toString());

                if (location != null)
                    sendTelemetry(location);

            }
        }
    };

    private void sendTelemetry(Location location) {

        JSONObject jsonTelemetry = new JSONObject();
        JSONObject jsonLocation = new JSONObject();
        try {
            if (ioTCClient != null) {
                jsonLocation.put("lon", (double)location.getLongitude());
                jsonLocation.put("lat", (double)location.getLatitude());
                jsonTelemetry.put("ubicacion", jsonLocation);
                System.out.println(jsonTelemetry);
                if (ioTCClient != null)
                    ioTCClient.SendTelemetry(jsonTelemetry.toString());
                else
                    System.out.println("cliente no conectado");
            }
        } catch (JSONException | IoTCentralException e) {
            e.printStackTrace();
        }


    }

    private void checkSettingsAndStartLocationUpdates()
    {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //Settings of device are satisfied and we can start location updates
                startLocationUpdates();
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(MainActivity.this, 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates()
    {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates()
    {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
            checkSettingsAndStartLocationUpdates();
        }
        else
        {
            askLocationPermission();
        }
    }

    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "askLocationPermission: you should show an alert dialog...");
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }


    private void getLastLocation() {
        @SuppressLint("MissingPermission") Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //We have a location
                    Log.d(TAG, "onSuccess: " + location.toString());
                    Log.d(TAG, "onSuccess: " + location.getLatitude());
                    Log.d(TAG, "onSuccess: " + location.getLongitude());
                } else {
                    Log.d(TAG, "onSuccess: Location was null...");
                }
            }
        });
        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: " + e.getLocalizedMessage());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
//                getLastLocation();
                checkSettingsAndStartLocationUpdates();
            } else {
                //Permission not granted
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //apagado=(ToggleButton)findViewById(R.id.TBApagar);






        JSONObject jsonTelemetry = new JSONObject();
        JSONObject location = new JSONObject();
        try {
            location.put("lon", 28.4622209);
            location.put("lat", -16.2769572);
            jsonTelemetry.put("ubicacion", location);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonProperty= new JSONObject();
        try{
            jsonProperty.put("matricula", matriculaActual);
        } catch(JSONException exe){
            exe.printStackTrace();
        }
        PropertiesCallback onProps = (IoTCProperty property) -> {
            System.out.println(String.format("Received property '%s' with value: %s", property.getName(),
                    property.getValue().toString()));
            if(property.getName().equals("matricula")){
                matriculaActual=property.getValue().toString();
            }
            property.ack("Property applied");
        };

        CommandCallback onCommand = (command) -> {
            System.out.println(String.format("Received command '%s' with value: %s", command.getName(),
                    command.getRequestPayload().toString()));
            return command.reply(IOTC_COMMAND_RESPONSE.SUCCESS, "Command executed");
        };
        ioTCClient = new IoTCClient(deviceId, scopeId, IOTC_CONNECT.DEVICE_KEY, deviceKey, new MemStorage());
        ioTCClient.on(IOTC_EVENTS.Properties, onProps);
        ioTCClient.on(IOTC_EVENTS.Commands, onCommand);


        try {
            ioTCClient.Connect();
            JSONObject jsonMatricula = new JSONObject();
            jsonMatricula.put("matricula", "6666 ATO");
            ioTCClient.SendProperty(jsonMatricula.toString());
            ioTCClient.SendProperty(String.format("{\"propertyComponent\":{\"__t\":\"c\",\"prop1\":%d}}", 20));
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            locationRequest = LocationRequest.create();
            locationRequest.setInterval(4000);
            locationRequest.setFastestInterval(2000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } catch (IoTCentralException | JSONException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }


    }

    public void onClick(View view){

    }
}