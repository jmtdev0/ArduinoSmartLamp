package com.example.javier.arduinosmartlamp;

import android.os.Parcel;
import android.os.Parcelable;

public class CommandInfo implements Parcelable {
/* Los objetos de CommandInfo contienen información relativa a comandos de voz: su enunciado y el color
   que se les asigna. Implementa Parceable por la misma razón que lo hace AppInfo. */

    private String command;
    private String color;

    public CommandInfo(String command) {
        this.command = command;
        this.color = "Indeterminado";
    }

    public CommandInfo(String command, String color) {
        this.command = command;
        this.color = color;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    protected CommandInfo(Parcel in) {
        command = in.readString();
        color = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(command);
        dest.writeString(color);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CommandInfo> CREATOR = new Parcelable.Creator<CommandInfo>() {
        @Override
        public CommandInfo createFromParcel(Parcel in) {
            return new CommandInfo(in);
        }

        @Override
        public CommandInfo[] newArray(int size) {
            return new CommandInfo[size];
        }
    };
}