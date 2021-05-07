package com.example.jdk_ecomove;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.lucadruda.iotc.device.IoTCClient;
import com.github.lucadruda.iotc.device.callbacks.CommandCallback;
import com.github.lucadruda.iotc.device.callbacks.PropertiesCallback;
import com.github.lucadruda.iotc.device.enums.IOTC_COMMAND_RESPONSE;
import com.github.lucadruda.iotc.device.enums.IOTC_CONNECT;
import com.github.lucadruda.iotc.device.enums.IOTC_EVENTS;
import com.github.lucadruda.iotc.device.exceptions.IoTCentralException;
import com.github.lucadruda.iotc.device.models.IoTCProperty;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    final static String TELEMETRY_COMPONENT = "firstComponent";
    final static String TELEMETRY_FIELD = "temperature";
    ToggleButton apagado;
    private String deviceId="22uk5z8higm";
    private String scopeId="0ne002AC476";
    private String deviceKey="oEYwmtVlhKEycnBPY6fIJVFwecOz0eV8lKWyWTWkoNw=";
    private String matriculaActual="";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private TextView mLatitude,mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLatitude=findViewById(R.id.mLatitude);
        mLongitude=findViewById(R.id.mLongitude);
        // Establecer punto de entrada para la API de ubicación
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        setContentView(R.layout.activity_main);
        apagado=(ToggleButton)findViewById(R.id.TBApagar);

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
        IoTCClient ioTCClient = new IoTCClient(deviceId, scopeId, IOTC_CONNECT.DEVICE_KEY, deviceKey, new MemStorage());
        ioTCClient.on(IOTC_EVENTS.Properties, onProps);
        ioTCClient.on(IOTC_EVENTS.Commands, onCommand);

        try {
            ioTCClient.Connect();
            //ioTCClient.SendProperty(String.format("{\"propertyComponent\":{\"__t\":\"c\",\"prop1\":%d}}", 20));
            //while (true) {
                System.out.println("Sending telemetry");
                ioTCClient.SendTelemetry(jsonTelemetry.toString());
                System.out.println("mando matricual");
                ioTCClient.SendProperty(jsonProperty.toString());
                //ioTCClient.SendTelemetry(String.format("{\"%s\":%,.0f}", TELEMETRY_FIELD, Math.random() * 30),
                //        String.format("{\"$.sub\":\"%s\"}", TELEMETRY_COMPONENT));
                Thread.sleep(4000);
            //}

        } catch (InterruptedException | IoTCentralException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void onClick(View view){
        onConnected(null);
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            System.out.println(mLatitude);
            System.out.println(mLongitude);
            mLatitude.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitude.setText(String.valueOf(mLastLocation.getLongitude()));
        } else {
            Toast.makeText(this, "Ubicación no encontrada", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
}