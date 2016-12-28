package com.stanfy.helium.sample.android;

import android.os.Parcel;
import android.os.Parcelable;

// A custom type to check how it is used in parcelable-related generated code.
public class CustomType<T> implements Parcelable {

  public static final Creator<CustomType<?>> CREATOR = new Creator<CustomType<?>>() {
    public CustomType<?> createFromParcel(Parcel source) {
      return new CustomType();
    }
    public CustomType<?>[] newArray(int size) {
      return new CustomType[size];
    }
  };

  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel dst, int flags) {

  }

}
