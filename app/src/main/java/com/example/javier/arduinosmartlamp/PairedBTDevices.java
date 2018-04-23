package com.example.javier.arduinosmartlamp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PairedBTDevices extends AppCompatActivity {
/* Esta clase inicia la aplicación y la primera actividad, con la que se pretende activar el sistema Bluetooth
   del dispositivo y mostrar los demás aparatos con los que previamente se ha emparejado. El usuario debe eliger
   el módulo de Bluetooth HC-06 y la aplicación tratará de establecer la conexión con él.*/

    // Componentes de interfaz gráfica

    private Button btnConnectBT;
    private Button btnBTSearch;
    private ListView devicesListView;

    // Bluetooth

    private BluetoothAdapter BTAdapter = null;
    private Set pairedDevices;

    /* OnItemClickListener de BTButton. Extrae dirección MAC del dispositivo BT seleccionado de entre los mostrados en la lista
       (últimos 17 caracteres), y se la envía a LampControl. */

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            Intent i = new Intent(PairedBTDevices.this, LampControl.class);
            i.putExtra(BLUETOOTH_SERVICE, address); //this will be received at ledControl (class) Activity
            startActivity(i);

        }
    };

    private void initBT() {

        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        if (BTAdapter == null) {

            messageToUser("Función Bluetooth no existente.");
            finish();

        } else {

            if (!BTAdapter.isEnabled()) {

                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon, 1);

            }

        }

    }

    private void displayDevicesList() {

        if (BTAdapter == null || !BTAdapter.isEnabled()) {

            messageToUser("Primero debe conectarse el sistema Bluetooth de este dispositivo.");

        } else {

            pairedDevices = BTAdapter.getBondedDevices();
            List<String> devicesList = new ArrayList<>();

            if (pairedDevices.size() > 0) {

                for (Object obt : pairedDevices) {

                    BluetoothDevice bt = (BluetoothDevice) obt;
                    devicesList.add(bt.getName() + "\n" + bt.getAddress());
                }

            } else {

                messageToUser("No se ha encontrado ningún dispositivo.");

            }


            final ArrayAdapter adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, devicesList);
            devicesListView.setAdapter(adapter);
            devicesListView.setOnItemClickListener(myListClickListener);

        }

    }

    private void messageToUser (String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paired_devices);
        Toolbar toolbar = findViewById(R.id.toolbar_paired_devices);
        setSupportActionBar(toolbar);

        btnConnectBT = findViewById(R.id.btnConnectBT);
        btnBTSearch = findViewById(R.id.btnBTSearch);
        devicesListView = findViewById(R.id.listView);

        btnConnectBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initBT();
            }
        });

        btnBTSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDevicesList();
            }
        });

        toolbar.bringToFront();

    }

}
