package com.geekbrains.krilov;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

    public static void sendFile(DataOutputStream os, File file) throws IOException {

            os.writeUTF("/send " + file.getName());
            os.writeLong(file.length());

            try (InputStream is = new FileInputStream(file)) {
                byte [] buffer = new byte[8192];
                while (is.available() > 0) {
                    int readBytes = is.read(buffer);
                    os.write(buffer, 0, readBytes);
                }
            }
    }

    public static void getFile(DataInputStream is, File file) {
        try {
            System.out.println("incoming file: " + file.getName());
            file.createNewFile();

            long length = is.readLong();

            try (FileOutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                while (file.length() < length) {
                    int r = is.read(buffer);
                    if (r <= 0) break;
                    os.write(buffer, 0, r);
                }
            }
            System.out.println("File " + file.getName() + " loaded!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
