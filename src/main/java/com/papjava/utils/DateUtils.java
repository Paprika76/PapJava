package com.papjava.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName DateUtils
 * @Description
 * @Author Paprika
 * @date 2024-07-27
 **/

public class DateUtils {

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDD = "yyyy/MM/dd";
//    public static final String  = "yyyy-MM-dd";


    public static String format(Date date, String pattern){
        return new SimpleDateFormat(pattern).format(date);
    }
    public static Date parse(String date, String pattern){
        try {
            return new SimpleDateFormat(pattern).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String format = format(new Date(), YYYY_MM_DD_HH_MM_SS);
        System.out.println(format);
        Date parsedDate = parse(format, YYYY_MM_DD_HH_MM_SS);
        System.out.println(parsedDate);


        String format2 = format(parsedDate, YYYY_MM_DD_HH_MM_SS);
        System.out.println(format2);
    }
}
