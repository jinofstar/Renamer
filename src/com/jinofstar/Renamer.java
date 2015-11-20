package com.jinofstar;

import java.io.*;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.FileHandler;

public class Renamer {
    final int blockingQueueSize = 100;

    final ThreadPoolExecutor executor;
    ImageRenamer imageRenamer;
    DeleteFilenameFilter deleteFilenameFilter;

    public Renamer() {
        executor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(blockingQueueSize));
        imageRenamer = new ImageRenamer();

        deleteFilenameFilter = new DeleteFilenameFilter();
        deleteFilenameFilter.addExtension("txt");
        deleteFilenameFilter.addExtension("db");
        deleteFilenameFilter.hiddenFile(true);
    }

    public void run(String path) {
        String fullPath = Utils.getFileFullPath(path);
        if (fullPath == null) {
            return;
        }

        System.out.println("Full path : " + fullPath);
        executor.execute(new TraverseTask(path));
    }

    class TraverseTask implements Runnable {
        final String path;

        public TraverseTask(String path) {
            this.path = path;

            System.out.println("new TraverseTask");
        }

        @Override
        public void run() {
            System.out.println(">>>> start <<<<");
            System.out.println("path : " + path);

            File rootFile = new File(path);
            if (!rootFile.exists()) {
                System.out.println(path + " is not exist!");
                return;
            }

            if (!rootFile.isDirectory()) {
                System.out.println(path + " is not directory!");
                return;
            }

            new Item(rootFile).run();
        }

        class Item {
            int count = 0;
            File rootFile;

            public Item(File file) {
                this.rootFile = file;
            }

            public void run() {
                removeFilesIfNeeded();

                File[] files = rootFile.listFiles(fileFilter);
                if (files == null) {
                    System.out.println(rootFile.getName() + " is empty folder!");
                    return;
                }

                for (int i = 0; i < files.length; i++) {
                    File file = files[i];

                    if (file.isDirectory()) {
                        new Item(file).run();
                    } else {
                        System.out.println("Rename File : " + file.getName());

                        String newFileName = String.format(format, ++count, Utils.getFileExtension(file.getName()).toLowerCase());
                        imageRenamer.offer(new RenameItem(file, newFileName));
                    }
                }
            }

            private void removeFilesIfNeeded() {
                File[] files = rootFile.listFiles(deleteFilenameFilter);
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

        private void traverse(File rootFile) {
            removeFilesIfNeeded(rootFile);

            File[] files = rootFile.listFiles(fileFilter);
            if (files == null) {
                System.out.println(rootFile.getName() + " is empty folder!");
                return;
            }

            int count = 0;

            String suffix = getSuffix(files.length);
            String format = getFileFormat(rootFile.getName(), suffix);

            for (int i = 0; i < files.length; i++) {
                File file = files[i];

                if (file.isDirectory()) {
                    traverse(file);
                } else {
                    System.out.println("Rename File : " + file.getName());

                    String newFileName = String.format(format, ++count, Utils.getFileExtension(file.getName()).toLowerCase());
                    imageRenamer.offer(new RenameItem(file, newFileName));
                }
            }

            System.out.println(">>>> end <<<<");
        }

        public String getSuffix(int count) {
            int digits = Utils.numberOfDigits(count);
            if (digits < 2) {
                digits = 2;
            }

            StringBuilder format = new StringBuilder();
            format.append("%0").append(digits).append("d");
            return format.toString();
        }

        public String getFileFormat(String prefix, String suffix) {
            StringBuilder format = new StringBuilder();
            format.append(prefix).append(" ").append(suffix).append(".%s");

            return format.toString();
        }

        private void removeFilesIfNeeded(File rootFile) {
            File[] files = rootFile.listFiles(deleteFilenameFilter);
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

    //
    FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                return true;
            }

            try {
                if (Utils.isZipFile(pathname)) {
                    return true;
                }
            } catch (Exception e) {
            }

            return false;
        }
    };

    // internal
    public class DeleteFilenameFilter implements FilenameFilter {
        List<String> extList = new ArrayList<>();
        boolean hiddenFile = false;

        public DeleteFilenameFilter() {
        }

        public void addExtension(String ext) {
            extList.add(ext);
        }

        public void hiddenFile(boolean enable) {
            hiddenFile = enable;
        }

        @Override
        public boolean accept(File dir, String name) {
            if (name.lastIndexOf('.') > 0) {
                for (String ext : extList) {
                    return (name.endsWith(ext));
                }
            }

            if (hiddenFile) {
                return name.startsWith(".");
            }

            return false;
        }
    }
}
