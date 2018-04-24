package com.example.javier.arduinosmartlamp;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class AppInfo implements Parcelable {
/* Clase cuyos objetos reúnen los atributos necesarios para manejar la información de distintas aplicaciones.
   Implementa la interfaz Parceable para que sea posible transmitir objetos de su clase entre diferentes
   actividades de la aplicación. Este no es el único método disponible para este cometido. Los métodos propios
   de esta interfaz se han generado tanto para esta clase como para las otras que también la implementan con
   la ayuda de este sitio: http://www.parcelabler.com/.
*/

    private String packageName;
    private String name;
    private Drawable icon;
    private String color; // Color asociado a la aplicación.
    private boolean added; // Se le asigna el valor verdadero si el usuario ha seleccionado la aplicación.

    public AppInfo() {
    }

    public AppInfo(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
        this.color = "Indeterminado";
        this.added = false;
    }

    public AppInfo(String packageName, String name, Drawable icon, String color, boolean added) {
        this.packageName = packageName;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.added = added;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }

    /* Se considera que dos objetos de AppInfo representan a la misma aplicación cuando comparten el
       mismo nombre de paquete. */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppInfo appInfo = (AppInfo) o;

        return packageName != null ? packageName.equals(appInfo.packageName) : appInfo.packageName == null;
    }

    @Override
    public int hashCode() {
        return packageName != null ? packageName.hashCode() : 0;
    }

    protected AppInfo(Parcel in) {
        packageName = in.readString();
        name = in.readString();
        icon = (Drawable) in.readValue(Drawable.class.getClassLoader());
        color = in.readString();
        added = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeString(name);
        dest.writeValue(icon);
        dest.writeString(color);
        dest.writeByte((byte) (added ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
}


