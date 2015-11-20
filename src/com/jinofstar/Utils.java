package com.jinofstar;

import java.io.*;

public class Utils {
    public static int numberOfDigits(int number) {
        return (number == 0) ? 1 : (1 + (int)Math.floor(Math.log10(Math.abs(number))));
    }

    public static String getFileExtension(String path) {
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

    public static String getFileFullPath(String path) {
        try {
            return new File(path).getCanonicalPath();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static boolean isZipFile(File file) throws IOException {
        int retry = 0;
        int MAX_RETRY = 5;

        while(!file.canRead()) {
            ++retry;

            try {
                file.wait(1000L);
            } catch (Exception ignore) {
            }

            if (retry > MAX_RETRY) {
                throw new IOException("Cannot read file " + file.getAbsolutePath());
            }
        }

        if (file.length() < 4) {
            return false;
        }

        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        int test = in.readInt();
        in.close();
        return test == 0x504b0304;
    }

    public static void renameFile(File file, String newName) {
        if (file.getName().equals(newName)) {
            return;
        }

        File newFile = new File(file.getParentFile(), newName);

        System.out.println("new file : " + newFile.getName());
        file.renameTo(newFile);
    }
}
