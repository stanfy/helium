package com.stanfy.helium.swagger;

import com.google.gson.annotations.SerializedName;

final class Schema {
  @SerializedName("$ref")
  final String ref;

  Schema(String ref) {
    this.ref = ref;
  }
}
