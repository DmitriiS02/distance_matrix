package src.main.java.ru.distance_matrix;



import com.google.gson.JsonArray;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.xmlbeans.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;
import com.sun.tools.javac.Main;

import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.awt.*;
import java.util.Scanner;
import java.util.Timer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class ApplicationMintrance {
    private static double prevResult = 0;
    private static final String CONFIG_FILE = "C:/my_java/distance_matrix/src/main/java/ru/distance_matrix/config.properties";
    private static boolean inWork = true;
    private static String apiKey = "abc3-dds3-ssa2";
    private static File executibleFile;
    private static int rowToStart =4;

    /* main method is the entry point of the program.
    It prompts the user to enter an expression and uses the evaluate method to evaluate it.
    If the user enters "q", the program will exit.
    The program also has a prevResult variable that keeps track of the previous result,
    which can be used as an operand in the next expression.
    If the user enters an expression that starts with an operator,
    it is assumed that the user wants to use the previous result as the first operand and
    the input string is modified accordingly before being passed to the evaluate method
    */
    public static void main(String[] args) throws IOException {

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
        System.out.println("1. Выбрать Excel файл");
        System.out.println("2. Просмотреть текущий ключ доступа");
        System.out.println("3. Обновить ключ доступа");
        System.out.println("4. Выход");
        Scanner scanner = new Scanner(System.in);
        while (inWork == true) {
            loadSettings();
            System.out.print("\nНомер опции >");
            String input = scanner.nextLine();

            int choice = 0;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введенное значение не является целым числом.");
            }

            ArrayList<JsonObject> resultArray = new ArrayList<>();


            switch (choice) {

                case 1:

                    ArrayList<String> requestArray = new ArrayList<String>();

                    System.out.println("Убедитесь, что в выбранном файле структура соответствует следующим параметрам:\n" +
                            "В столбце \"V\" начилая с 5-й строки перечислен координаты северной широты,\n" +
                            "а в столбце \"W\" c 5-й строки перечислены координаты западной долготы.\n");
                    try {
                        requestArray = fileExecuting();
                    } catch (NullPointerException e) {
                        System.out.println("\nФайл Пуст. Проверьте на содержание\n\n" + e);
                    } catch (NotOfficeXmlFileException e) {
                        System.out.println("\nНеверный формат входного файла, выберите .xlsx" + e);
                    } catch (Exception e) {
                        System.out.println("\nВозникла непредвиденная ошибка\n" + e);
                    }
                    for (int i = 0; i < 2; i++) { //i<requestArray.size() TODO:23.03.2025  Здесь должно быть либо статическое значение, либо меняем ширину в динамике
                        //System.out.println(requestArray.get(i));
                        try {
                            JsonObject result = requestData(requestArray.get(i));//Идем по сформированному request одной строки
                            resultArray.add(result);
                        } catch (Exception e) {
                            System.out.println("\nНе удалось установить соединение с Яндекс сервисом" + e);
                        }


                    }


                    WriteInExcel(resultArray);

                    //System.out.println(resultArray);
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
    public static JsonObject requestData(String requestBody) {
        HttpURLConnection connection = null;
        StringBuilder response;
        JsonObject json= new JsonObject();
        try {
            URL url = new URL(requestBody);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);

            connection.connect();

            StringBuilder responseBuilder = new StringBuilder();

            if(HttpURLConnection.HTTP_UNAUTHORIZED == connection.getResponseCode()){
                System.out.println("Ошибка валидации ключа доступа. Проверьте текущий статус ключа, при необходимоти - смените");
            }
            else if(HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = in.readLine())!= null){
                    responseBuilder.append(line);
                }
                in.close();
                String jsonResponse = responseBuilder.toString();

                Gson gson = new Gson();
                json = gson.fromJson(jsonResponse, JsonObject.class);

            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(connection!=null){
                connection.disconnect();
            }
        }
        return json;
    }

    public static ArrayList<String> fileExecuting() throws IOException {
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

        ArrayList<String> requestBodyList = new ArrayList<String>();

        if (filename != null) {
            executibleFile = new File(directory, filename);
            DetectionOfLastRow();
            try {
                // Создаем экземпляр FileInputStream для чтения файла
                FileInputStream inputStream = new FileInputStream(executibleFile);
                // Создаем экземпляр XSSFWorkbook с выбранным файлом
                Workbook book = new XSSFWorkbook(inputStream);
                Sheet sheet = book.getSheetAt(0);
                int countNotEmpty =0;

                for (int rowOrig = rowToStart;rowOrig<=sheet.getLastRowNum();rowOrig++){

                    StringBuilder requestBody = new StringBuilder();

                    Row curRowOrigin = sheet.getRow(rowOrig);
                    Cell curCelLatOrigin = curRowOrigin.getCell(21);
                    String latOrigin = curCelLatOrigin.getStringCellValue();
                    Cell curCelLonOrigin = curRowOrigin.getCell(22);
                    String lonOrigin = curCelLonOrigin.getStringCellValue();
//                    if ((latOrigin == null || lonOrigin ==null)||(latOrigin==""||lonOrigin=="")){
//                        continue;
//                    }
                    requestBody.append("https://api.routing.yandex.net/v2/distancematrix?origins="+latOrigin+","+lonOrigin+"&destinations=");
                    //System.out.println(requestBody); //Смотриим на тело запросаа
                    for(int j = 26;j<29;j++){ //TODO: 23.03.2024
                        Row curRowDestinationLat = sheet.getRow(1);
                        Row curRowDestinationLon = sheet.getRow(2);
                        String latDest = curRowDestinationLat.getCell(j).getStringCellValue();
                        String lonDest = curRowDestinationLon.getCell(j).getStringCellValue();
//                        if ((latDest.equals(null) || lonDest.equals(null))||(latDest.isEmpty()||lonDest.isEmpty())) {
//                            continue;
//                        }

                        requestBody.append(latDest+","+lonDest+"|");
                        //System.out.println(latDest+" "+lonDest);



                    }
                    //countNotEmpty++;

                    requestBody.deleteCharAt(requestBody.length() - 1);
                    requestBody.append("&mode=driving&apikey="+apiKey);

                    //System.out.println(requestBody);
                    requestBodyList.add(requestBody.toString());
                }

                System.out.println(countNotEmpty);

                inputStream.close();
            } catch (IOException e) {
               ;
            }
        }
        return requestBodyList;
    }
    public static void DetectionOfLastRow() throws IOException {
        File execFile = executibleFile;
        FileInputStream inputStream = new FileInputStream(execFile);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        int startRow = rowToStart; // Начальная строка диапазона
        int endRow = sheet.getLastRowNum(); // Конечная строка диапазона
        int columnToCheck = 26;

        for (int i = startRow; i <= endRow; i++) {
            Row row = sheet.getRow(i);
            Cell cell = row.getCell(columnToCheck);
            if (cell==null||cell.getStringCellValue()=="") {
                rowToStart = i;
                break;
            }
        }
        inputStream.close();

    }
    public static void WriteInExcel(ArrayList<JsonObject> arrayResult) throws IOException {
        File execFile = executibleFile;
        FileInputStream inputStream = new FileInputStream(execFile);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        for (int rowIndex = rowToStart, i = 0; i<2; i++, rowIndex++) { //TODO:23.03.2024  Здесь должно быть либо статическое значение, либо меняем ширину в динамике
            JsonObject singleResponse = arrayResult.get(i);
            JsonObject rows = singleResponse.getAsJsonArray("rows").get(0).getAsJsonObject();
            JsonArray elements = rows.getAsJsonArray("elements");

            Row row = sheet.getRow(rowIndex);

            for(int col = 26, j=0; col < 29;j++, col++){ //TODO:23.03.2024 Здесь должно быть либо статическое значение, либо меняем ширину в динамике
                JsonObject currentElement = elements.get(j).getAsJsonObject();
                String status = currentElement.get("status").getAsString();
                if ("FAIL".equals(status)){
                    Cell cell = row.createCell(col);
                    cell.setCellValue(status);
                }
                else{
                    JsonObject durationObject = currentElement.getAsJsonObject("duration");
                    String durationValue = durationObject.get("value").getAsString();
                    Cell cell = row.createCell(col);
                    cell.setCellValue(durationValue);
                    System.out.println("Duration value: " + durationValue);
                }

            }
        }



        FileOutputStream fileOut = new FileOutputStream(execFile);
        workbook.write(fileOut);
        workbook.close();
        fileOut.close();
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
