package com.papjava.utils;

import java.io.File;
import java.io.IOException;

/**
 * @ClassName FileUtils
 * @Description
 * @Author Paprika
 * @date 2024-08-11
 **/

public class FileUtils {

    public static void interceptExist(File path) throws IOException {
        if (path.exists()) {
            throw new IOException("已存在该文件，请删除后再生成！\t" + path);
        }
    }
}
