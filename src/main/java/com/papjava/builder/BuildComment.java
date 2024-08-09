package com.papjava.builder;

import com.papjava.bean.Constants;
import com.papjava.utils.DateUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName BuildComment
 * @Description
 * @Author Paprika
 **/

public class BuildComment {


    public static void createClassComment(BufferedWriter bw,String classComment) throws IOException {
        /**
         * @Description
         *
         * @Author Paprika
         * @date 2024/7/24
         **/
        bw.write("/**");
        bw.newLine();
        bw.write(" * @Description "+classComment+"\n");
        bw.write(" * @Author " + Constants.AUTHOR_COMMENT + "\n");
        bw.write(" * @date " + DateUtils.format(new Date(),DateUtils.YYYYMMDD) +"\n");
        bw.write(" */\n");
    }
    public static void createFieldComment(BufferedWriter bw,String fieldComment) throws IOException {
        /**
         *
         **/
        bw.write("\t/**\n");
        bw.write("\t * " + (fieldComment==null?"":fieldComment)+"\n");
        bw.write("\t */\n");
    }
    public static void createMethodComment(){

    }
}
