package com.example.javier.arduinosmartlamp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class LampControl extends AppCompatActivity {
/* Clase responsable de la actividad principal de la aplicación, que muestra la ventana desde la que se accede
   a las demás y que almacena toda la información relevante para el correcto funcionamiento del conjunto completo.

   La clase privada ConnectBT y los métodos relacionados con la conexión Bluetooth que aquí se encuentran,
   al igual que los de PairedBTDevices, se han extraído del paso 6 de este tutorial:
   http://www.instructables.com/id/Android-Bluetooth-Control-LED-Part-2/.
   La clase ColorPicker pertenece a la librería
   disponible en https://github.com/Pes8/android-material-color-picker-dialog.
   Las funciones que trabajan con la lista de aplicaciones instaladas en el dispositivo y las clases AppAdapter,
   AppInfo y ListApps se han adaptado del proyecto visitable en
   https://inducesmile.com/android/android-list-installed-apps-in-device-programmatically/.

*/

    // Elementos de interfaz gráfica

    private FloatingActionButton btnTurnOnOff, btnChangeColor;
    private ColorPicker cp = null;
    private Button btnAddApps, btnPickColors, btnAddCommands, btnInitSpeech;
    private ProgressDialog progress;

    // Bluetooth

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(LampControl.this, "Conectando.", "Espera, por favor.");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (BTSocket == null || !isBtConnected) {
                    BTAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = BTAdapter.getRemoteDevice(MAC_Adress);//connects to the device's address and checks if it's available
                    BTSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    BTSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                messageToUser("Conexión fallida.", true);
                finish();
            } else {
                messageToUser("Conexión establecida.", true);
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    private boolean isBtConnected = false;
    private String MAC_Adress;
    BluetoothAdapter BTAdapter = null;
    BluetoothSocket BTSocket = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Control de lampara, ajustes de aplicación

    int REDValue = 255, GREENValue = 255, BLUEValue = 255;
    private boolean lampTurnedOn = false;
    private boolean soundActivated = true;
    private boolean notificationMode = true;

    // Registro de aplicaciones y color asignado

    private AppList installedApps = new AppList(new ArrayList<AppInfo>());
    private Map<String, AppInfo> selectedApps = new HashMap<>();
    private Map<String, CommandInfo> voiceCommands = new HashMap<>();

    // Manejo de notificaciones recibidas

    BroadcastReceiver notificationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (notificationMode)
            checkPackage(intent.getStringExtra("key"));

        }

    };

    // Métodos y funciones

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lamp_control);
        Toolbar toolbar = findViewById(R.id.toolbar_lamp_control);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Control de lámpara");

        // Se recibe la dirección MAC

        Intent newInt = getIntent();
        MAC_Adress = newInt.getStringExtra(PairedBTDevices.BLUETOOTH_SERVICE);

        new ConnectBT().execute();

        // Interfaz

        btnTurnOnOff = findViewById(R.id.btnTurnOnOff);
        btnTurnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnOffLamp();
            }
        });

        btnChangeColor = findViewById(R.id.btnChangeColor);
        btnChangeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLampColor();
            }
        });

        cp = new ColorPicker(this, REDValue, GREENValue, BLUEValue);

        btnAddApps = findViewById(R.id.addApps);
        btnAddApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAppList();
            }
        });

        btnPickColors = findViewById(R.id.pickColors);
        btnPickColors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectColors();
            }
        });

        btnAddCommands = findViewById(R.id.addCommands);
        btnAddCommands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addVoiceCommands();
            }
        });

        btnInitSpeech = findViewById(R.id.mic);
        btnInitSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechToText();
            }
        });

        // Receiver

        startService(new Intent(this, NotificationListener.class));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NotificationListener.MY_ACTION);
        registerReceiver(notificationReceiver, intentFilter);

        // Obtener lista de aplicaciones

        installedApps = getInstalledApps();
        Collections.sort(installedApps.getAppList(), new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo app1, AppInfo app2) {
                return app1.getName().compareTo(app2.getName()); // Se ordenan alfabéticamente
            }
        });

    }

    /* Los dos métodos siguientes tienen como función procesar el uso del pequeño menú presente en la esquina
       superior derecha de la ventana de LampControl.
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        menu.add("Sonido activado").setActionView(R.layout.action_layout_checkbox).setCheckable(true).setChecked(this.soundActivated);
        menu.add("Informar de notificaciones").setActionView(R.layout.action_layout_checkbox).setCheckable(true).setChecked(this.notificationMode);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        CharSequence opcionSeleccionada = item.getTitle();
        item.setChecked(!item.isChecked());
        //noinspection SimplifiableIfStatement
        if (opcionSeleccionada.equals("Sonido activado")) {
            changeBuzzerMode();
        } else if (opcionSeleccionada.equals("Informar de notificaciones")) {
            notificationMode = !notificationMode;
        }

        return true;
    }

    private void changeBuzzerMode() {

        soundActivated = !soundActivated;

        if (BTSocket != null) {

            try {

                byte[] changeModeMessage;

                if (soundActivated)
                    changeModeMessage = "buzzerOn".getBytes();
                else
                    changeModeMessage = "buzzerOff".getBytes();

                BTSocket.getOutputStream().write(changeModeMessage);


            } catch (IOException e) {
                messageToUser("Error en el proceso de envío de datos a la placa Arduino.", true);
            }
        }

    }

    private void turnOnOffLamp() {

        if (BTSocket != null) {

            try {

                byte[] colorBytes = (lampTurnedOn) ? "turnOff".getBytes() : "turnOn".getBytes();
                BTSocket.getOutputStream().write(colorBytes);

            } catch (IOException e) {
                messageToUser("Error en el proceso de envío de datos a la placa Arduino.", true);
            }
        }

        lampTurnedOn = !lampTurnedOn;
        if (lampTurnedOn) btnTurnOnOff.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorTurnedOn)));
        else btnTurnOnOff.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.materialcolorpicker__white)));

    }

    private void changeLampColor() {
        cp.setColor(Color.argb(255, REDValue, GREENValue, BLUEValue));
        cp.show();
        Button okColor = cp.findViewById(R.id.okColorButton);
        okColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                REDValue = cp.getRed();
                GREENValue = cp.getGreen();
                BLUEValue = cp.getBlue();
                cp.dismiss();

                if (BTSocket != null) {

                    try {

                        byte[] changeColor = ("" + String.format("%03d", REDValue) +
                                String.format("%03d", GREENValue) + String.format("%03d", BLUEValue)).getBytes();

                        BTSocket.getOutputStream().write(changeColor);


                    } catch (IOException e) {
                        messageToUser("Error en el proceso de envío de datos a la placa Arduino.", true);
                    }
                }

            }
        });

    }

    private void showAppList() {

        // Comprobar acceso permitido a información de notificaciones
        if  (checkNotificationAccess()) {

            Intent i = new Intent(LampControl.this, ListApps.class);
            i.putExtra("applist", installedApps);
            i.putExtra("int_value", 0);
            startActivityForResult(i, 1);

        }

    }

    private void selectColors() {

        Intent i = new Intent(LampControl.this, ListApps.class);
        i.putExtra("applist", new AppList(new ArrayList<>(selectedApps.values())));
        i.putExtra("int_value", 1);
        startActivityForResult(i, 1);

    }

    private void addVoiceCommands() {

        Intent i = new Intent(LampControl.this, ListCommands.class);
        i.putExtra("commands", new CommandList(new ArrayList<>(voiceCommands.values())));
        startActivityForResult(i, 2);


    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Pronuncia el comando que quieres ejecutar.");
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, "Procesando comando.");
        try {
            startActivityForResult(intent, 3);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "El reconocimiento de voz no está disponible en este dispositivo.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {

            Bundle bundleData = data.getExtras();
            int mostrarColores = bundleData.getInt("int_value");

            switch (requestCode) {

                case 1: { // ListApps

                    if (mostrarColores == 0) {

                        installedApps = bundleData.getParcelable("applist");
                        updateAppList(installedApps);

                    } else {

                        AppList selectedApps = bundleData.getParcelable("applist");
                        for (AppInfo appInfo : selectedApps.getAppList()) {
                            this.selectedApps.get(appInfo.getPackageName()).setColor(appInfo.getColor());
                        }
                        System.out.println();


                    }

                    break;

                }

                case 2: { // ListCommands

                    CommandList voiceCommands = bundleData.getParcelable("commands");
                    this.voiceCommands.clear();
                    for (CommandInfo commandInfo : voiceCommands.getCommandList()) {
                        this.voiceCommands.put(commandInfo.getCommand(), commandInfo);
                    }

                    break;
                }

                case 3: { // Reconocimiento de voz

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String command = result.get(0);
                    checkCommand(command);

                    break;
                }

                case 4: { // Retorno de ajustes de acceso a notificaciones

                    break;
                }

            }

        }

    }

    private void updateAppList(AppList allApps) { // Mejorar

        // Generar lista de añadidos

        AppList addedApps = new AppList(new ArrayList<AppInfo>());

        for (AppInfo appInfo : allApps.getAppList()) {
            if (appInfo.isAdded()) {
                addedApps.getAppList().add(appInfo);
            }
        }

        // Eliminar de selectedApps las aplicaciones que no se encuentren en addedApps.

        List<AppInfo> removeApps = new ArrayList<>();

        for (AppInfo appInfo : selectedApps.values()) {
            if (!addedApps.getAppList().contains(appInfo)) {
                removeApps.add(appInfo);
            }
        }

        for (AppInfo appInfo : removeApps) {
            selectedApps.remove(appInfo.getPackageName());
        }

        // Añadir a selectedApps las nuevas aplicaciones de addedApps, en caso de haberlas.

        for (AppInfo appInfo : addedApps.getAppList()) {
            if (!selectedApps.keySet().contains(appInfo.getPackageName())) {
                appInfo.setColor("Indeterminado");
                selectedApps.put(appInfo.getPackageName(), appInfo);
            }
        }

    }

    private boolean checkNotificationAccess() {

        String enabledListeners = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                "enabled_notification_listeners");

        if (!enabledListeners.contains("arduinosmartlamp")) {

            /* Para API superior o igual a nivel 22:
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)); */

            startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), 4);
            String meessageToUser = "Si deseas que Arduino SmartLamp te informe de ciertas notificaciones, debes dar acceso a " +
                    "la aplicación a la información sobre ellas.";
            messageToUser(meessageToUser, true);
            return false;

        } else

            return true;

    }

    private void turnOnLampColor(String key, boolean notificacion) {

        if (BTSocket != null) {

            try {

                if (notificacion) {

                    byte[] colorBytes = (this.selectedApps.get(key).getColor() + "NT").getBytes();
                    BTSocket.getOutputStream().write(colorBytes);

                } else {

                    byte[] commandBytes = (this.voiceCommands.get(key).getColor() + "VC").getBytes();
                    BTSocket.getOutputStream().write(commandBytes);

                }

            } catch (IOException e) {
                messageToUser("Error en el proceso de envío de datos a la placa Arduino.", true);
            }
        }

    }

    private void checkPackage(String paquete) {

        for (String packageName : this.selectedApps.keySet()) {
            if (paquete.equalsIgnoreCase(packageName)) {
                messageToUser("Notificación de " + selectedApps.get(packageName).getName() + " recibida.", false);
                turnOnLampColor(packageName, true);
                break;
            }
        }

    }

    private void checkCommand(String command) {

        if (this.voiceCommands.containsKey(command)) {

            String color = this.voiceCommands.get(command).getColor();

            if (!color.equals("Indeterminado")) {

                messageToUser("Comando reconocido: " + command, true);
                REDValue = Integer.valueOf(color.substring(0, 3));
                GREENValue = Integer.valueOf(color.substring(3, 6));
                BLUEValue = Integer.valueOf(color.substring(6, 9));
                turnOnLampColor(command, false);

                // La lámpara queda encendida con el color que está asociado al comando reconocido.

                lampTurnedOn = true;
                btnTurnOnOff.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorTurnedOn)));

            } else {

                messageToUser("Se ha reconocido el comando -" + command +
                        "-, pero no se ha escogido un color para él.", true);

            }

        }

        else {
            messageToUser("Comando no reconocido: " + command, false);

        }

    }

    // Método empleado para mostrar mensajes por pantalla durante un tiempo corto o largo (booleano slow).
    private void messageToUser(String msg, boolean slow) {
        if (slow)
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

    }

    private AppList getInstalledApps() {
        AppList res = new AppList(new ArrayList<AppInfo>());
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            if (!isSystemPackage(p)) {
                String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
                //Drawable icon = p.applicationInfo.loadIcon(getPackageManager());
                String packageName = p.packageName;
                res.getAppList().add(new AppInfo(packageName,appName));
            }
        }
        return res;
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

}
