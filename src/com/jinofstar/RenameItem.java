package com.jinofstar;

import java.io.File;

public class RenameItem {
    public RenameItem(String filepath) {
        File[] files = new File(filepath).listFiles();

        if (files == null) {
            System.out.println(filepath + " is empty folder!");
            return;
        }
    }
}
