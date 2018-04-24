package com.example.javier.arduinosmartlamp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class AppList implements Parcelable {
// Clase que contiene una lista de AppInfo. Implementa Parceable por la misma razón que lo hace AppInfo.

    private List<AppInfo> appList;

    public AppList(List<AppInfo> appList) {
        this.appList = appList;
    }

    public List<AppInfo> getAppList() {
        return appList;
    }

    public void setAppList(List<AppInfo> appList) {
        this.appList = appList;
    }


    protected AppList(Parcel in) {
        if (in.readByte() == 0x01) {
            appList = new ArrayList<>();
            in.readList(appList, AppInfo.class.getClassLoader());
        } else {
            appList = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (appList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(appList);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AppList> CREATOR = new Parcelable.Creator<AppList>() {
        @Override
        public AppList createFromParcel(Parcel in) {
            return new AppList(in);
        }

        @Override
        public AppList[] newArray(int size) {
            return new AppList[size];
        }
    };
}