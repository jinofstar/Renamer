package com.jinofstar;

public class Main {

    public static void main(String[] args) {
        if (args.length <= 0) {
            System.out.println("Usage : Renamer.jar [dirs]");
        }

        Renamer renamer = new Renamer();

        for(String dir : args) {
            System.out.println("Starting with : " + dir);

            renamer.run(dir);
        }
    }
}

