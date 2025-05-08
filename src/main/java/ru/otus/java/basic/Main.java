package ru.otus.java.basic;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final String PARENT_DIRECTORY = "..";

    private static String currentDirectory;
    private static Boolean isActiveProgramm = true;

    public static void main(String[] args) {
        currentDirectory = System.getProperty("user.dir");
        Scanner scanner = new Scanner(System.in);

        while (isActiveProgramm) {
            System.out.println("Текущая директория " + currentDirectory + "> ");
            String command = scanner.nextLine().trim();

            if (command.startsWith("ls")) {
                boolean longFormat = command.equals("ls -i");
                listFiles(longFormat);
            } else if (command.startsWith("cd")) {
                String path = command.substring("cd".length()).trim();
                changeDirectory(path);
            } else if (command.startsWith("mkdir")) {
                String name = command.substring("mkdir".length()).trim();
                createDirectory(name);
            } else if (command.startsWith("rm")) {
                String filename = command.substring("rm".length()).trim();
                deleteFile(filename);
            } else if (command.startsWith("mv")) {
                String[] parts = command.substring("mv".length()).trim().split("\\s+", 2);
                String source = parts[0];
                String destination = parts[1];
                moveFile(source, destination);
            } else if (command.startsWith("cp")) {
                String[] parts = command.substring("cp".length()).trim().split("\\s+", 2);
                String source = parts[0];
                String destination = parts[1];
                copyFile(source, destination);
            } else if (command.startsWith("finfo")) {
                String filename = command.substring("finfo".length()).trim();
                printFileInfo(filename);
            } else if (command.equals("help")) {
                printHelp();
            } else if (command.startsWith("find")) {
                String filename = command.substring("find".length()).trim();
                findFile(filename);
            } else if (command.equals("exit")) {
                isActiveProgramm = false;
                System.out.println("Завершение работы программы");
            } else {
                System.out.println("Неподдерживаемая команда");
            }
        }

        scanner.close();
    }

    private static void listFiles(boolean longFormat) {
        File directory = new File(currentDirectory);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (longFormat) {
                    printFileDetails(file);
                } else {
                    System.out.println(file.getName());
                }
            }
        }
    }

    private static void changeDirectory(String path) {
        if (path.equals(PARENT_DIRECTORY)) {
            currentDirectory = new File(currentDirectory).getParent();
        } else {
            File newDirectory = new File(currentDirectory, path);
            if (newDirectory.isDirectory()) {
                currentDirectory = newDirectory.getAbsolutePath();
            } else {
                System.out.println("Папка не существует: " + newDirectory.getAbsolutePath());
            }
        }
    }

    private static void createDirectory(String name) {
        File newDirectory = new File(currentDirectory, name);
        if (newDirectory.mkdir()) {
            System.out.println("Папка создана: " + newDirectory.getAbsolutePath());
        } else {
            System.out.println("Не получается создать папку: " + newDirectory.getAbsolutePath());
        }
    }

    private static void deleteFile(String filename) {
        File file = new File(currentDirectory, filename);
        if (file.exists()) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                if (file.delete()) {
                    System.out.println("Файл удален: " + file.getAbsolutePath());
                } else {
                    System.out.println("Не получается удалить файл: " + file.getAbsolutePath());
                }
            }
        } else {
            System.out.println("Файл не найден: " + file.getAbsolutePath());
        }
    }

    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (file.delete()) {
                        System.out.println("Файл удален: " + file.getAbsolutePath());
                    } else {
                        System.out.println("Не получается удалить файл: " + file.getAbsolutePath());
                    }
                }
            }
        }

        if (directory.delete()) {
            System.out.println("Папка удалена: " + directory.getAbsolutePath());
        } else {
            System.out.println("Невозможно удалить папку: " + directory.getAbsolutePath());
        }
    }

    public static void moveFile(String filePath, String folderPath) {
        try {
            File file = new File(filePath);
            File folder = new File(folderPath);
            if (file.exists() && folder.exists()) {
                Path destinationPath = folder.toPath().resolve(file.getName());
                Files.move(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Файл перемещен");
            } else {
                System.out.println("Невозможно переместить файл");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyFile(String sourceFile, String destinationFolder) {
        File fileToCopy = new File(sourceFile);
        File destinationDir = new File(destinationFolder);

        if (!fileToCopy.exists()) {
            System.out.println("Исходный файл не найден");
        }

        if (!destinationDir.isDirectory()) {
            System.out.println("Путь назначения не является папкой.");
        }

        Path destinationPath = Path.of(destinationDir.getAbsolutePath(), fileToCopy.getName());

        try {
            Files.copy(fileToCopy.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Файл успешно скопирован в " + destinationPath);
        } catch (IOException e) {
            System.out.println("Ошибка копирования");
        }
    }

    private static void printFileInfo(String filename) {
        File file = new File(currentDirectory, filename);
        if (file.exists()) {
            printFileDetails(file);
        } else {
            System.out.println("Файл не найден: " + file.getAbsolutePath());
        }
    }

    private static void printFileDetails(File file) {
        BasicFileAttributes attributes;
        try {
            attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            System.out.println("Невозможно прочитать файл: " + file.getAbsolutePath());
            return;
        }

        String name = file.getName();
        long size = attributes.size();
        Date lastModified = new Date(attributes.lastModifiedTime().toMillis());

        System.out.println(name + " - " + size + " bytes - " + DATE_FORMAT.format(lastModified));
    }

    private static void findFile(String filename) {
        File directory = new File(currentDirectory);
        File[] files = directory.listFiles();

        if (files != null) {
            boolean found = false;
            for (File file : files) {
                if (file.getName().equals(filename)) {
                    System.out.println("Файл найден в папке: " + file.getAbsolutePath());
                    found = true;
                }

                if (file.isDirectory()) {
                    findFileInDirectory(file, filename);
                }
            }

            if (!found) {
                System.out.println("Файл в папке не найден");
            }
        } else {
            System.out.println("Ошибка: папка пуста или не существует");
        }
    }

    private static void findFileInDirectory(File directory, String filename) {
        File[] files = directory.listFiles();
        if (files != null) {
            boolean found = false;
            for (File file : files) {
                if (file.getName().equals(filename)) {
                    System.out.println("Файл найден в подкаталоге: " + file.getAbsolutePath());
                    found = true;
                }

                if (file.isDirectory()) {
                    findFileInDirectory(file, filename);
                }
            }

            if (!found) {
                System.out.println("Файл в подкаталоге не найден ");
            }
        } else {
            System.out.println("Ошибка: папка пуста или не существует");
        }
    }

    private static void printHelp() {
        System.out.println("Справка:");
        System.out.println("ls - список файлов в текущем каталоге");
        System.out.println("ls -l - список файлов в текущем каталоге с подробной информацией");
        System.out.println("cd - изменить каталог");
        System.out.println("mkdir - создать новый каталог");
        System.out.println("rm - удалить файл или каталог");
        System.out.println("mv - переместить файл или каталог");
        System.out.println("cp - скопировать файл");
        System.out.println("finfo - получить подробную информацию о файле");
        System.out.println("help - отобразить это справочное сообщение");
        System.out.println("find - найти файл в текущем каталоге или любом из его подкаталогов");
        System.out.println("exit - завершение работы");
    }
}