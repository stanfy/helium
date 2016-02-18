package com.stanfy.helium.swagger;

import java.util.List;
import java.util.Map;

/** Root of Swagger spec. */
final class Root {

  final String swagger = "2.0";

  Info info;

  String host;

  List<String> schemes;

  String basePath;

  Map<String, Path> paths;

  Map<String, Definition> definitions;

  static class Info {
    final String title, description, version;

    Info(String title, String description, String version) {
      this.title = title;
      this.description = description;
      this.version = version;
    }
  }

}
