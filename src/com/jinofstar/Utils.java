package com.jinofstar;

public class Utils {
    public static int numberOfDigits(int number) {
        return (number == 0) ? 1 : (1 + (int)Math.floor(Math.log10(Math.abs(number))));
    }

    public static String getFileExtension (String path) {
        String separator = System.getProperty("file.separator");

        int indexOfLastSeparator = path.lastIndexOf(separator);
        String filename = path.substring(indexOfLastSeparator + 1);

        int extensionIndex = filename.lastIndexOf(".");
        try {
            return filename.substring(extensionIndex + 1);
        } catch (Exception e) {
            return "";
        }
    }
}
