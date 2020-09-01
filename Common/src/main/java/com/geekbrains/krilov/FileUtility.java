package com.geekbrains.krilov;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtility {

    public static void createFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createDirectory(String dirName) throws IOException {
        Path path = Paths.get(dirName);
        path.normalize();
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

}
