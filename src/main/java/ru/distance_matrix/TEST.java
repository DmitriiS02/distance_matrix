package src.main.java.ru.distance_matrix;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TEST {
    public static void main(String[] args) {
        Gson gson = new Gson();
        JsonArray input = null;

        try (BufferedReader br = new BufferedReader(new FileReader("C:/my_java/distance_matrix/src/main/resources/META-INF/response.json"))) {
            input  = gson.fromJson(br, JsonArray.class);
            JsonObject element = input.get(0).getAsJsonObject();
            JsonObject rows = element.getAsJsonArray("rows").get(0).getAsJsonObject();
            JsonArray elements = rows.getAsJsonArray("elements");

            for (int i = 0; i<elements.size();i++) {
                JsonObject currentElement = elements.get(i).getAsJsonObject();
                JsonObject durationObject = currentElement.getAsJsonObject("duration");
                int durationValue = durationObject.get("value").getAsInt();
                System.out.println("Duration value: " + durationValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Теперь у вас есть объект elementsArray, содержащий данные из вашего файла JSON
    }
}