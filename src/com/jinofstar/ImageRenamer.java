package com.jinofstar;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ImageRenamer {
    final int QUEUE_SIZE = 100;
    final ThreadPoolExecutor executor;

    public ImageRenamer() {
        this.executor = new ThreadPoolExecutor(4, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(QUEUE_SIZE));
    }

    public void offer(RenameItem item) {
        try {
            if (Utils.isZipFile(item.file) == false) {
                return;
            }

            executor.execute(new RenameTask(item));
        } catch (Exception ignore) {

        }
    }

    class RenameTask implements Runnable {
        final File file;
        final RenameItem item;

        public RenameTask(RenameItem item) {
            this.item = item;
            this.file = item.file;
        }

        @Override
        public void run() {
            try {
                Utils.renameFile(item.file, item.fileName);

                System.out.println("=====================================================");
                System.out.println("rename with " + file.toString());

                System.out.println("=====================================================");
            } catch (Exception e) {
            }
        }
    }
}