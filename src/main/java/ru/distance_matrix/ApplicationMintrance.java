package src.main.java.ru.distance_matrix;


import com.sun.tools.javac.Main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.awt.*;
import java.util.Scanner;
import java.util.Timer;

public class ApplicationMintrance {
    private static double prevResult = 0;
    private static final String CONFIG_FILE = "C:/my_java/distance_matrix/src/main/java/ru/distance_matrix/config.properties";
    private static boolean inWork = true;
    private static String apiKey = "abc3-dds3-ssa2";

    /* main method is the entry point of the program.
    It prompts the user to enter an expression and uses the evaluate method to evaluate it.
    If the user enters "q", the program will exit.
    The program also has a prevResult variable that keeps track of the previous result,
    which can be used as an operand in the next expression.
    If the user enters an expression that starts with an operator,
    it is assumed that the user wants to use the previous result as the first operand and
    the input string is modified accordingly before being passed to the evaluate method
    */
    public static void main(String[] args) {
        // Получаем объект Preferences для текущего пользователя

        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        System.out.flush();

        String username = System.getProperty("user.name");
        System.out.println("Имя текущего пользователя: " + username);

        System.out.println("Добро пожаловать в консольное приложение!\n" +
                "\n" +
                "Это приложение написано на Java и предназначено для обработки Excel файлов.\n" +
                "\n" +
                "В своей работе приложение использует API Сервиса \"Матрица расстояний\" от Яндекс.\n" +
                "\n" +
                "Убедитесь в наличии активного ключа доступа. \n" +
                "\n" +
                "Для продолжения работы выберите необходимую опцию из меню.\n");
        System.out.println("Меню:\n");
        System.out.println("1. Выбрать Excel файл"); // TODO: 18.03.2024
        System.out.println("2. Просмотреть текущий ключ доступа");
        System.out.println("3. Обновить ключ доступа");
        System.out.println("4. Выход");
        Scanner scanner = new Scanner(System.in);
        while (inWork == true) {
            loadSettings();
            System.out.print("\nНомер опции >");
            String input= scanner.nextLine();

            int choice = 0;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введенное значение не является целым числом.");
            }


            switch (choice) {

                case 1:
                    System.out.println("Убедитесь, что в выбранном файле структура соответствует следующим параметрам:\n" +
                            "В Ячейке А1");
                    FileExecuting();
                    break;
                case 2:
                    System.out.println("Текущий ключ доступа:  " + '"' + apiKey + '"');
                    //System.out.println("\nНажмите на любую клавишу - затем Enter, чтобы вернуться в Меню");
                    break;
                case 3:
                    System.out.print ("Для отмены введите "+"\u001B[31m"+"q\n\n" +"\u001B[0m"+"Введите новый ключ : ");
                    String newKey = scanner.nextLine();
                    if(!newKey.equals("q")){
                        SetNewApiKey(newKey);
                        saveSettings();
                        break;
                    }
                    else {
                        break;
                    }
                case 4:
                    System.out.print("Выход из программы ");
                    inWork = false;
                    break;
                default:
                    System.out.println("Неправильный выбор");
            }
        }
        scanner.close();
        ExitFromApp();
    }
    public static void SetNewApiKey(String newKey){
        apiKey = newKey;
        System.out.println("Новый ключ: " + '"' + apiKey + '"' + " - был успешно задан");
       //System.out.println("\nНажмите на любую клавишу - затем Enter, чтобы вернуться в Меню");

    }
    public static void FileExecuting(){
        Frame frame = new Frame();
        FileDialog fileDialog = new FileDialog(frame, "Выберите файл", FileDialog.LOAD);
        // Установка фильтра файлов
        //fileDialog.setFilenameFilter(new MyFileFilter());

        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();

        if (directory != null && filename != null) {
            String filePath = directory + filename;
            System.out.println("Файл выбран: " + filePath);
        } else {
            System.out.println("Выбор файла отменён.");
        }
    }
    public static void ExitFromApp(){
        // Создаем и запускаем поток, который будет выводить точки
        Thread dotsThread = new Thread(() -> {
            try {
                while (true) {
                    System.out.print(".");
                    Thread.sleep(300); // Пауза между точками (в миллисекундах)
                    System.out.print(".");
                    Thread.sleep(300); // Пауза между точками (в миллисекундах)
                    System.out.print(".");
                    Thread.sleep(300); // Пауза между точками (в миллисекундах)
                    System.out.print("\b\b\b"); // Удаляем точки, чтобы она заменялась следующей
                    Thread.sleep(500); // Пауза перед следующей точкой (в миллисекундах)
                }
            } catch (InterruptedException e) {
                // Обработка прерывания потока
            }
        });
        dotsThread.start(); // Запускаем поток с точками

        try {
            // Пауза перед остановкой потока с точками
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);

    }
    private static void loadSettings() {
        Properties prop = new Properties();
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
            apiKey = prop.getProperty("apiKey", apiKey);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void saveSettings() {
        Properties prop = new Properties();
        try (FileOutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.setProperty("apiKey", apiKey);
            prop.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
