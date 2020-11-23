package com.geekbrains.krilov;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

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

    public static void deletePath(String pathName) throws IOException {
        Files.walk(Paths.get(pathName))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static void deletePath(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

}
