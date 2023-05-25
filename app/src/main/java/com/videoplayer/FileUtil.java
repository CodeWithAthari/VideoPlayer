package com.videoplayer;

import java.io.File;

public class FileUtil {
    public static boolean isExistFile(String path) {
        File file = new File(path);
        return file.exists();
    }
    public static long getFileLength(String path) {
        if (!isExistFile(path)) return 0;
        return new File(path).length();
    }
}
