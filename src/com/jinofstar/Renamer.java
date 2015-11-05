package com.jinofstar;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Renamer {
    final int blockingQueueSize = 100;

    final ThreadPoolExecutor executor;
    final ThreadPoolExecutor renameExecutor;

    public Renamer() {
        this.executor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(blockingQueueSize));
        this.renameExecutor = new ThreadPoolExecutor(4, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(blockingQueueSize));
    }

    public void run(String path) {
        String fullPath = getFullPath(path);
        System.out.println("Full path : " + fullPath);

        recursive(path);
    }

    private void recursive(String path) {
        this.executor.execute(new RecursiveTask(path));
    }

    class RecursiveTask implements Runnable {
        final String path;
        String suffix;
        String prefix;

        public RecursiveTask(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            System.out.println(">>>> start recursive <<<<");
            System.out.println("path : " + path);

            File rootFile = new File(path);
            if (!rootFile.exists()) {
                System.out.println(path + " is not exist!");
                return;
            }

            removeFilesIfNeeded(rootFile);
            renameFiles(rootFile);

            System.out.println(">>>> end recursive <<<<");
        }

        private void renameFiles(File rootFile) {
            File[] files = rootFile.listFiles();
            if (files == null) {
                System.out.println(rootFile.getName() + " is empty folder!");
                return;
            }

            int count = 0;

            int digits = Utils.numberOfDigits(files.length);
            if (digits < 2) {
                digits = 2;
            }

            this.suffix = " %0" + digits + "d";
            this.prefix = rootFile.getName();

            for (int i = 0; i < files.length; i++) {
                File file = files[i];

                if (file.isHidden()) {
                    System.out.println("delete hidden file : " + file.getName());
                    file.delete();
                } if (file.isDirectory()) {
                    recursive(file.toString());
                } else {
                    System.out.println("file : " + file.getName());

                    renameFile(file, ++count);

                    renameExecutor.execute(new RenameTask(file));
                }
            }
        }

        private void renameFile(File file, int count) {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(prefix)
                    .append(String.format(suffix, count))
                    .append(".")
                    .append(Utils.getFileExtension(file.getName()).toLowerCase());

            String newFileName = stringBuilder.toString();

            if (!file.getName().equals(newFileName)) {
                File newFile = new File(file.getParentFile(), newFileName);
                System.out.println("new file : " + newFile.getName());
                file.renameTo(newFile);
            }
        }

        private void removeFilesIfNeeded(File rootFile) {
            DeleteFilenameFilter filenameFilter = new DeleteFilenameFilter();
            filenameFilter.addExtension("txt");
            filenameFilter.addExtension("db");

            File[] files = rootFile.listFiles(filenameFilter);
            if (files == null) {
                System.out.println(rootFile.getName() + " is empty folder!");
                return;
            }

            for (File file : files) {
                System.out.println("delete file : " + file.getAbsolutePath());
                file.delete();
            }
        }
    }

    public class DeleteFilenameFilter implements FilenameFilter {
        List<String> extList = new ArrayList<>();

        public DeleteFilenameFilter() {
            extList.add("txt");
            extList.add("db");
        }

        public void addExtension(String ext) {
            extList.add(ext);
        }

        @Override
        public boolean accept(File dir, String name) {
            if (name.lastIndexOf('.') > 0) {
                for (String ext : extList) {
                    return (name.endsWith(ext));
                }
            }

            return name.startsWith(".");
        }
    }

    class RenameTask implements Runnable {
        final File file;

        public RenameTask(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            System.out.println("start rename : " + file.toString());

            try {
                if (isZipFile(file)) {
                    // for image
                    System.out.println("rename : " + file.toString());
                    file.createNewFile();
                } else {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isZipFile(File file) throws IOException {
        int retry = 0;
        int MAX_RETRY = 5;

        while(!file.canRead()) {
            ++retry;

            try {
                file.wait(1000L);
            } catch (Exception e) {
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

    private String getFullPath(String path) {
        String fullPath = "";

        try {
            fullPath = new File(path).getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fullPath;
    }
}
