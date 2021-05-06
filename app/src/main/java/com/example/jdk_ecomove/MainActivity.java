package com.example.jdk_ecomove;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.lucadruda.iotc.device.IoTCClient;
import com.github.lucadruda.iotc.device.callbacks.CommandCallback;
import com.github.lucadruda.iotc.device.callbacks.PropertiesCallback;
import com.github.lucadruda.iotc.device.enums.IOTC_COMMAND_RESPONSE;
import com.github.lucadruda.iotc.device.enums.IOTC_CONNECT;
import com.github.lucadruda.iotc.device.enums.IOTC_EVENTS;
import com.github.lucadruda.iotc.device.exceptions.IoTCentralException;
import com.github.lucadruda.iotc.device.models.IoTCProperty;

public class MainActivity extends AppCompatActivity {

    final static String TELEMETRY_COMPONENT = "firstComponent";
    final static String TELEMETRY_FIELD = "temperature";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String deviceId="12y5c71v2oh";
        String scopeId="0ne00279F3B";
        String deviceKey="+CVPzyuhn7DvBfRKeCICUTE5cbirZ7t0qzfOnEQmdqI=";

        IoTCClient ioTCClient = new IoTCClient(deviceId, scopeId, IOTC_CONNECT.DEVICE_KEY, deviceKey, new MemStorage());

        PropertiesCallback onProps = (IoTCProperty property) -> {
            System.out.println(String.format("Received property '%s' with value: %s", property.getName(),
                    property.getValue().toString()));
            property.ack("Property applied");
        };

        CommandCallback onCommand = (command) -> {
            System.out.println(String.format("Received command '%s' with value: %s", command.getName(),
                    command.getRequestPayload().toString()));
            return command.reply(IOTC_COMMAND_RESPONSE.SUCCESS, "Command executed");
        };

        ioTCClient.on(IOTC_EVENTS.Properties, onProps);
        ioTCClient.on(IOTC_EVENTS.Commands, onCommand);

        try {
            ioTCClient.Connect();
            ioTCClient.SendProperty(String.format("{\"propertyComponent\":{\"__t\":\"c\",\"prop1\":%d}}", 20));

            while (true) {
                System.out.println("Sending telemetry");
                ioTCClient.SendTelemetry(String.format("{\"%s\":%,.0f}", TELEMETRY_FIELD, Math.random() * 30),
                        String.format("{\"$.sub\":\"%s\"}", TELEMETRY_COMPONENT));
                Thread.sleep(4000);
            }

        } catch (InterruptedException | IoTCentralException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }
}