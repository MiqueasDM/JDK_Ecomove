package com.example.jdk_ecomove;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.github.lucadruda.iotc.device.ICentralStorage;
import com.github.lucadruda.iotc.device.models.Storage;

import java.nio.charset.StandardCharsets;

public class MemStorage implements ICentralStorage {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void persist(Storage storage) {
        System.out.println("New credentials available:");
        System.out.println(storage.getHubName());
        System.out.println(storage.getDeviceId());
        System.out.println(new String(storage.getDeviceKey(), StandardCharsets.UTF_8));
        return;
    }

    @Override
    public Storage retrieve() {
        // return new
        // Storage("iotc-1f9e162c-eacc-408d-9fb2-c9926a071037.azure-devices.net",
        // "javasdkcomponents",
        // "+yz0YcYq/SEwvaF0UjLNKyrKuL8oyFknTtoEJOfOgTo=".getBytes());
        return new Storage();
    }

}
