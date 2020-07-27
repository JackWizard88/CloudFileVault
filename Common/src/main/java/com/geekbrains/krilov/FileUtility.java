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

    public static void move(File dir, File file) throws IOException {
        String path = dir.getAbsolutePath() + "/" + file.getName();
        createFile(path);
        InputStream is = new FileInputStream(file);
        try(OutputStream os = new FileOutputStream(new File(path))) {
            byte [] buffer = new byte[8192];
            while (is.available() > 0) {
                int readBytes = is.read(buffer);
                System.out.println(readBytes);
                os.write(buffer, 0, readBytes);
            }
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

    public static String sendFileList(List<Path> fileList) {

        System.out.println("sending file list...");
        String list = "";
        if (fileList.size() == 0) return "list is empty//end.";

        for (Path f : fileList) {
            String path = f.getFileName().toString();
            list += path + "//";
        }

        return list + "end.";

    }
}
