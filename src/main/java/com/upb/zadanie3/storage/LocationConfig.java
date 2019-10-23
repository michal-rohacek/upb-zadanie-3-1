package com.upb.zadanie3.storage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LocationConfig {
    public static final Path filesLocation = Paths.get("upload-dir");
    public static final Path keysLocation = Paths.get("src/keys");
}
