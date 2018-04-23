package com.example.javier.arduinosmartlamp;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ListApps extends AppCompatActivity {
/* ListApps muestra una lista de aplicaciones, mostrando de cada una su nombre y su icono, y dependiendo del caso,
   también indica si han sido escogidas por el usuario, o si tienen un color asignado. */

    private AppList installedApps = new AppList(new ArrayList<AppInfo>()) ;
    private AppInfo selectedApp = new AppInfo();
    // Variable a la que se asigna la información de la aplicación pulsada por el usuario.

    private int showColors;
    /* Esta variable que se recibe como extra de un Intent determina si se muestra la lista de aplicaciones para
    que el usuario selecciona las que desee, o para que escoja un color para ellas.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_apps);

        Toolbar toolbar = findViewById(R.id.toolbar_list_apps);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListApps.this.onBackPressed();
            }
        });

        final ColorPicker cp = new ColorPicker(ListApps.this, 0, 0, 0);
        final ListView userInstalledApps = findViewById(R.id.installed_app_list);

        showColors = getIntent().getIntExtra("int_value", 0);
        getSupportActionBar().setTitle((showColors ==0)? "Añadir aplicaciones" : "Escoger colores");

        Bundle data = getIntent().getExtras();
        installedApps = data.getParcelable("applist");
        this.getIcons(installedApps);

        final AppAdapter installedAppAdapter;

        if (showColors == 0) {

            installedAppAdapter = new AppAdapter(ListApps.this, installedApps.getAppList(), false);

        } else {

            installedAppAdapter = new AppAdapter(ListApps.this, installedApps.getAppList(), true);

        }

        userInstalledApps.setAdapter(installedAppAdapter);
        userInstalledApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedApp = installedApps.getAppList().get(i);

                if (showColors == 0) { // Se marca la aplicación pulsada como añadida/no añadida.

                    selectedApp.setAdded(!selectedApp.isAdded());
                    installedAppAdapter.notifyDataSetChanged();

                } else {
                // Se despliega el objeto de ColorPicker para que el usuario elija el color que quiere para la aplicación.

                    cp.show();
                    Button okColor = cp.findViewById(R.id.okColorButton);
                    okColor.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            selectedApp.setColor("" + String.format("%03d", cp.getRed()) + String.format("%03d", cp.getGreen())
                                    + String.format("%03d", cp.getBlue()));
                            installedAppAdapter.notifyDataSetChanged();
                            cp.dismiss();
                        }
                    });

                }
            }

        });


    }

    @Override
    public void onBackPressed(){

        for (AppInfo appInfo : installedApps.getAppList()) {

            appInfo.setIcon(null);

        }

        Intent i = getIntent();
        i.putExtra("applist", installedApps);
        i.putExtra("int_value", showColors);
        setResult(RESULT_OK, i);
        finish();
    }

    /* Por lo que se ha observado, la clase Drawable es incompatible con la interfaz Parcelable, por lo que se ha
       optado por recorrer también en ListApps la lista de aplicaciones instaladas en el dispositivo para poder
       recuperar los iconos de las aplicaciones que resultan interesantes para el usuario. Esto supone un consumo
       de recursos absurdo y seguramente evitable para el que debería buscarse una alternativa.
     */

    private void getIcons(AppList appList) {
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        Map<String,AppInfo> appMap = new HashMap<>();
        for (AppInfo appInfo : appList.getAppList()) {
            appMap.put(appInfo.getName(),appInfo);
        }
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            if ((!isSystemPackage(p)) && appMap.containsKey(p.applicationInfo.loadLabel(getPackageManager()).toString())) {
                Drawable icon = p.applicationInfo.loadIcon(getPackageManager());
                appMap.get(p.applicationInfo.loadLabel(getPackageManager()).toString()).setIcon(icon);
            }
        }

    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

}
