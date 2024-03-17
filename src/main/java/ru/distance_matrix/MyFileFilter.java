package ru.distance_matrix;

import java.io.File;
import java.io.FilenameFilter;

class MyFileFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".xlsx") || new File(dir, name).isDirectory(); // Фильтрация по расширению .txt
    }
}