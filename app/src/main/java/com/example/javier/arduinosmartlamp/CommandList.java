package com.example.javier.arduinosmartlamp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class CommandList implements Parcelable {
// Clase que contiene una lista de AppInfo. Implementa Parceable por la misma razón que lo hace AppInfo.

    private ArrayList<CommandInfo> commandList;

    public CommandList(ArrayList<CommandInfo> commandList) {
        this.commandList = commandList;
    }

    public ArrayList<CommandInfo> getCommandList() {
        return commandList;
    }

    public void setCommandList(ArrayList<CommandInfo> commandList) {
        this.commandList = commandList;
    }

    protected CommandList(Parcel in) {
        if (in.readByte() == 0x01) {
            commandList = new ArrayList<>();
            in.readList(commandList, CommandInfo.class.getClassLoader());
        } else {
            commandList = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (commandList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(commandList);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CommandList> CREATOR = new Parcelable.Creator<CommandList>() {
        @Override
        public CommandList createFromParcel(Parcel in) {
            return new CommandList(in);
        }

        @Override
        public CommandList[] newArray(int size) {
            return new CommandList[size];
        }
    };
}
