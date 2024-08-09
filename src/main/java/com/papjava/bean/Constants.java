package com.papjava.bean;

import com.papjava.utils.PropertiesUtils;

/**
 * @ClassName Constants
 * @Description
 * @Author Paprika
 **/

public class Constants {
    public static String AUTHOR_COMMENT;
    public static Boolean IGNORE_TABLE_PREFIX;
    public static String SUFFIX_BEAN_QUERY;
    public static String SUFFIX_BEAN_QUERY_FUZZY;
    public static String SUFFIX_BEAN_QUERY_TIME_START;
    public static String SUFFIX_BEAN_QUERY_TIME_END;

    public static String SUFFIX_MAPPER;
    public static String SUFFIX_SERVICE;
    public static String SUFFIX_SERVICE_IMPL;
    public static String SUFFIX_CONTROLLER;



    // 需要忽略的属性！！！
    public static String IGNORE_BEAN_TOJSON_FIELD;
    public static String IGNORE_BEAN_TOJSON_EXPRESSION;
    public static String IGNORE_BEAN_TOJSON_CLASS;

    //日期序列化 反序列化

    public static String BEAN_DATE_FORMAT_EXPRESSION;
    public static String BEAN_DATE_FORMAT_CLASS;

    public static String BEAN_DATE_UNFORMAT_EXPRESSION;
    public static String BEAN_DATE_UNFORMAT_CLASS;


    private static String PATH_JAVA = "java";

    private static String PATH_RESOURCES = "resources";

    public static String PACKAGE_BASE;
    public static String PACKAGE_PO;
    public static String PACKAGE_VO;
    public static String PACKAGE_UTILS;
    public static String PACKAGE_ENUMS;
    public static String PACKAGE_QUERY;
    public static String PACKAGE_MAPPER;
    public static String PACKAGE_SERVICE;
    public static String PACKAGE_SERVICE_IMPL;
    public static String PACKAGE_EXCEPTION;
    public static String PACKAGE_CONTROLLER;




    public static String PATH_BASE;
    public static String PATH_PO;
    public static String PATH_VO;
    public static String PATH_UTILS;
    public static String PATH_ENUMS;
    public static String PATH_QUERY;
    public static String PATH_MAPPER;
    public static String PATH_MAPPER_XML;
    public static String PATH_SERVICE;
    public static String PATH_SERVICE_IMPL;
    public static String PATH_EXCEPTION;
    public static String PATH_CONTROLLER;

    static {
        AUTHOR_COMMENT = PropertiesUtils.getString("author.comment");

        SUFFIX_BEAN_QUERY_FUZZY = PropertiesUtils.getString("suffix.bean.query.fuzzy");
        SUFFIX_BEAN_QUERY_TIME_START = PropertiesUtils.getString("suffix.bean.query.time.start");
        SUFFIX_BEAN_QUERY_TIME_END = PropertiesUtils.getString("suffix.bean.query.time.end");

        SUFFIX_MAPPER = PropertiesUtils.getString("suffix.mapper");

        //日期序列化 反序列化
        IGNORE_BEAN_TOJSON_FIELD = PropertiesUtils.getString("ignore.bean.tojson.field");
        IGNORE_BEAN_TOJSON_EXPRESSION = PropertiesUtils.getString("ignore.bean.tojson.expression");
        IGNORE_BEAN_TOJSON_CLASS = PropertiesUtils.getString("ignore.bean.tojson.class");
        BEAN_DATE_FORMAT_EXPRESSION = PropertiesUtils.getString("bean.date.format.expression");
        BEAN_DATE_FORMAT_CLASS = PropertiesUtils.getString("bean.date.format.class");
        BEAN_DATE_UNFORMAT_EXPRESSION = PropertiesUtils.getString("bean.date.unformat.expression");
        BEAN_DATE_UNFORMAT_CLASS = PropertiesUtils.getString("bean.date.unformat.class");


        IGNORE_TABLE_PREFIX = Boolean.valueOf(PropertiesUtils.getString("ignore.table.prefix"));
        SUFFIX_BEAN_QUERY = PropertiesUtils.getString("suffix.bean.query");

        SUFFIX_SERVICE = PropertiesUtils.getString("suffix.service");
        SUFFIX_SERVICE_IMPL = PropertiesUtils.getString("suffix.service.impl");;
        SUFFIX_CONTROLLER = PropertiesUtils.getString("suffix.controller");;

        PACKAGE_BASE = PropertiesUtils.getString("package.base");
        //PO
        PACKAGE_PO = PACKAGE_BASE + "." + PropertiesUtils.getString("package.po");
        PACKAGE_VO = PACKAGE_BASE + "." + PropertiesUtils.getString("package.vo");
        PACKAGE_UTILS = PACKAGE_BASE + "." + PropertiesUtils.getString( "package.utils");
        PACKAGE_ENUMS = PACKAGE_BASE + "." + PropertiesUtils.getString( "package.enums");
        PACKAGE_QUERY = PACKAGE_BASE + "." + PropertiesUtils.getString( "package.query");
        PACKAGE_MAPPER = PACKAGE_BASE + "." + PropertiesUtils.getString( "package.mapper");
        PACKAGE_SERVICE = PACKAGE_BASE + "." + PropertiesUtils.getString( "package.service");
        PACKAGE_SERVICE_IMPL = PACKAGE_SERVICE + "." + PropertiesUtils.getString( "package.service.impl");
        PACKAGE_EXCEPTION = PACKAGE_BASE + "." + PropertiesUtils.getString( "package.exception");
        PACKAGE_CONTROLLER = PACKAGE_BASE + "." + PropertiesUtils.getString( "package.controller");


        PATH_BASE = PropertiesUtils.getString( "path.base");
        PATH_BASE = PATH_BASE + PATH_JAVA;
        PATH_PO = PATH_BASE + "/" + PACKAGE_PO.replace(".","/");
        PATH_VO = PATH_BASE + "/" + PACKAGE_VO.replace(".","/");
        PATH_UTILS = PATH_BASE + "/" + PACKAGE_UTILS.replace(".","/");
        PATH_ENUMS = PATH_BASE + "/" + PACKAGE_ENUMS.replace(".","/");
        PATH_QUERY = PATH_BASE + "/" + PACKAGE_QUERY.replace(".","/");
        PATH_MAPPER = PATH_BASE + "/" + PACKAGE_MAPPER.replace(".","/");
        PATH_MAPPER_XML = PropertiesUtils.getString("path.base") + PATH_RESOURCES + "/" + PACKAGE_MAPPER.replace(".","/");
        PATH_SERVICE = PATH_BASE + "/" + PACKAGE_SERVICE.replace(".","/");
        PATH_SERVICE_IMPL = PATH_BASE + "/" + PACKAGE_SERVICE_IMPL.replace(".","/");
        PATH_EXCEPTION = PATH_BASE + "/" + PACKAGE_EXCEPTION.replace(".","/");
        PATH_CONTROLLER = PATH_BASE + "/" + PACKAGE_CONTROLLER.replace(".","/");


    }
    public final static String[] SQL_DATE_TIME_TYPES = new String[] {"datetime" ,"timestamp" };
    public final static String[] SQL_DATE_TYPES = new String[] {"date"};
    public final static String[] SQL_DECIMAL_TYPE = new String [] {"decimal","double", "float" };
    public final static String[] SQL_STRING_TYPE = new String[] {"char", "varchar", "text","mediumtext","longtext"};
    //Integer
    public final static String[] SQL_INTEGER_TYPE = new String[] {"int","tinyint" };
    //Long
    public final static String[] SQL_LONG_TYPE = new String[] {"bigint"};


    public static void main(String[] args) {

        System.out.println(PATH_BASE);
        System.out.println(PACKAGE_BASE);
        System.out.println(PACKAGE_PO);

        System.out.println(PATH_PO);
        System.out.println(PATH_MAPPER_XML);

        System.out.println(PATH_SERVICE);
        System.out.println(PATH_SERVICE_IMPL);
        System.out.println(PATH_VO);
        System.out.println(PATH_EXCEPTION);
        System.out.println(PATH_CONTROLLER);
    }
}
