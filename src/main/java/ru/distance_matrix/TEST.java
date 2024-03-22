package src.main.java.ru.distance_matrix;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class TEST {
    private static File executibleFile;
    private static String apiKey = "abc3-dds3-ssa2";
    private static int rowToStart =4;
    public static void main(String[] args) {
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
            //DetectionOfLastRow();

            try {
                // Создаем экземпляр FileInputStream для чтения файла
                FileInputStream inputStream = new FileInputStream(executibleFile);
                // Создаем экземпляр XSSFWorkbook с выбранным файлом
                Workbook book = new XSSFWorkbook(inputStream);
                Sheet sheet = book.getSheetAt(0);
                CountingGeneralCols(sheet);
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
        System.out.println(requestBodyList);

    }
    public static void CountingGeneralCols(Sheet sheet){
        Row row = sheet.getRow(0);
        boolean rowEnded =false;
        int cellIndex = 26;
        int countCols = 0;

        while(!rowEnded){
            Cell cell = row.getCell(cellIndex);
            if(cell == null|| cell.getStringCellValue().isEmpty()){
                break;
            }

            String strCell = cell.getStringCellValue();
            System.out.println(cell+" ");
            cellIndex++;
            countCols++;
        }
        CountingRows(countCols);
        System.out.println(cellIndex+" " +countCols);

    }
    public static void CountingRows(int countCols){
        int limitOfDayRequest = 1000;
        int res;
        res = limitOfDayRequest/countCols;
        System.out.println(res);
    }

}