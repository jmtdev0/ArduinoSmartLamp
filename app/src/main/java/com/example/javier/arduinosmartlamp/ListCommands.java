package com.example.javier.arduinosmartlamp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.util.ArrayList;

public class ListCommands extends AppCompatActivity {
/* Clase utilizada para enumerar los comandos de voz registrados, eligir un color para ellos, eliminarlos
   y añadir nuevos. */

    private ListView commandList;
    private Button btnNewCommand;

    private CommandList voiceCommands = new CommandList(new ArrayList<CommandInfo>());
    private ArrayList<String> commands = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private String newCommand = "";
    private CommandInfo selectedCommand; // Información del último comando pulsado.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_commands);

        Toolbar toolbar = findViewById(R.id.toolbar_list_commands);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Añadir comandos de voz");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListCommands.this.onBackPressed();
            }
        });

        Bundle data = getIntent().getExtras();
        voiceCommands = data.getParcelable("commands");

        commandList = findViewById(R.id.command_list);
        btnNewCommand = findViewById(R.id.newCommand);
        final ColorPicker cp = new ColorPicker(ListCommands.this, 0, 0, 0);

        btnNewCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCommand();
            }
        });

        for (CommandInfo voiceCommand : voiceCommands.getCommandList()) {
            String value = voiceCommand.getCommand() + " [Pulsa para editar]";
            commands.add(value);
        }

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, commands);
        commandList.setAdapter(adapter);
        commandList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         /* Al pulsar un comando, se abre un diálogo que pide al usuario que especifique qué acción
            quiere llevar a cabo. */

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                selectedCommand = voiceCommands.getCommandList().get(i);
                CharSequence opcionesEdicion[] = new CharSequence[]{"Cambiar nombre", "Escoger color", "Eliminar comando"};

                AlertDialog.Builder builder = new AlertDialog.Builder(ListCommands.this);
                builder.setTitle("¿Qué quieres hacer?");
                builder.setItems(opcionesEdicion, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {

                            case 0: { // Editar nombre

                                AlertDialog.Builder builder = new AlertDialog.Builder(ListCommands.this);
                                builder.setTitle("Introduce el nuevo nombre");

                                final EditText input = new EditText(ListCommands.this);
                                input.setInputType(InputType.TYPE_CLASS_TEXT);
                                builder.setView(input);
                                input.setText(selectedCommand.getCommand());

                                builder.setPositiveButton("Guardar cambios", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        selectedCommand.setCommand(input.getText().toString());
                                        commands.set(voiceCommands.getCommandList().indexOf(selectedCommand),
                                                selectedCommand.getCommand() + " [Pulsa para editar]");
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();

                                break;
                            }

                            case 1: { // Cambiar color asignado

                                if (!selectedCommand.getColor().equals("Indeterminado")) {
                                    String oldColor = selectedCommand.getColor();
                                   cp.setColor(Color.argb(255, Integer.valueOf(oldColor.substring(0,3)),
                                           Integer.valueOf(oldColor.substring(3,6)), Integer.valueOf(oldColor.substring(6,9))));
                                }
                                cp.show();
                                Button okColor = cp.findViewById(R.id.okColorButton);
                                okColor.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        selectedCommand.setColor("" + String.format("%03d", cp.getRed()) + String.format("%03d", cp.getGreen())
                                                + String.format("%03d", cp.getBlue()));
                                        cp.dismiss();


                                    }
                                });

                                break;

                            }

                            case 2: { // Eliminar comando

                                commands.remove(voiceCommands.getCommandList().indexOf(selectedCommand));
                                voiceCommands.getCommandList().remove(selectedCommand);
                                adapter.notifyDataSetChanged();
                                break;

                            }

                        }
                    }


                });

                builder.show();

            }

        });

    }

    private void addCommand() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Introduce el nuevo comando");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        input.setText("");

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newCommand = input.getText().toString();
                voiceCommands.getCommandList().add(new CommandInfo(newCommand));
                commands.add(newCommand + " [Pulsa para editar]");
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    @Override
    public void onBackPressed(){

        Intent i = getIntent();
        i.putExtra("commands", voiceCommands);
        setResult(RESULT_OK, i);
        finish();
    }

}
