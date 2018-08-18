package com.example.root.forhelp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by root on 04.03.18.
 */

public class User implements Parcelable {
    String _id;
    String email;
    String jwt;

    public User(String _id, String email, String jwt) {
        this._id = _id;
        this.email = email;
        this.jwt = jwt;
    }

    private User(Parcel in) {
        this._id = in.readString();
        this.email = in.readString();
        this.jwt = in.readString();

    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(_id);
        parcel.writeString(email);
        parcel.writeString(jwt);
    }
}
