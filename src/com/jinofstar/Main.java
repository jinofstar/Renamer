package com.jinofstar;

public class Main {

    public static void main(String[] args) {
        Renamer renamer = new Renamer();

        for(String dir : args) {
            System.out.println("start with : " + dir);

            renamer.run(dir);
        }
    }
}

