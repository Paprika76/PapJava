package com.papjava.utils;


import java.util.Locale;

/**
 * @ClassName StingUtils
 * @Description
 * @Author Paprika
 **/

public class StringUtils {
    public static String upperCaseFirstLetter(String field){
        if(org.apache.commons.lang3.StringUtils.isEmpty(field)){
            return field;
        }
        return field.substring(0,1).toUpperCase()+field.substring(1);
    }
    public static String lowerCaseFirstLetter(String field){
        if(org.apache.commons.lang3.StringUtils.isEmpty(field)){
            return field;
        }
        return field.substring(0,1).toLowerCase()+field.substring(1);
    }

}
