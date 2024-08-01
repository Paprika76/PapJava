package com.papjava.builder;

import com.papjava.bean.Constants;
import com.papjava.bean.FieldInfo;
import com.papjava.bean.TableInfo;
import com.papjava.utils.DateUtils;
import com.papjava.utils.StringUtils;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName BuildQuery
 * @Description
 * @Author Paprika
 * @date 2024-07-28
 **/

public class BuildQuery {
    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);
    //创建文件!!! 创建PO对象class文件
    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_QUERY);
        if (!folder.exists()){
            folder.mkdirs();
        }
        String className = tableInfo.getBeanName() + Constants.SUFFIX_BEAN_QUERY;


        File poFile = new File(folder, className + ".java");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out,"utf8");
            bw = new BufferedWriter(outw);
            bw.write("package " + Constants.PACKAGE_QUERY + ";");
            bw.newLine();
            bw.newLine();

            if (tableInfo.getHaveDate()||tableInfo.getHaveDateTime()){
                bw.write("import java.util.Date;");
                bw.newLine();
                bw.newLine();
            }

            if (tableInfo.getHaveBigDecimals()){
                bw.write("import java.math.BigDecimal;");
                bw.newLine();
            }


            bw.newLine();
            //构建类的注释
            BuildComment.createClassComment(bw, tableInfo.getComment() + "查询对象");
            bw.newLine();

            bw.write("public class "+className +" extends BaseQuery {");
            bw.newLine();


//            List<FieldInfo> fieldInfoList = new ArrayList<>();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                BuildComment.createFieldComment(bw, fieldInfo.getComment());
                bw.write("\tprivate " + fieldInfo.getJavaType() + " " + fieldInfo.getPropertyName() + ";");
                bw.newLine();
                bw.newLine();

                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())) {
                    String propertyName = fieldInfo.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_FUZZY;
                    bw.write("\tprivate " + fieldInfo.getJavaType() + " " + propertyName + ";");
                    bw.newLine();
                    bw.newLine();

                }


                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, fieldInfo.getSqlType())
                        ||ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType())) {
                    String propertyName = fieldInfo.getPropertyName();
                    bw.write("\tprivate String " + propertyName + Constants.SUFFIX_BEAN_QUERY_TIME_START + ";");
                    bw.newLine();
                    bw.newLine();
                    bw.write("\tprivate String " + propertyName +
                            Constants.SUFFIX_BEAN_QUERY_TIME_END + ";");
                    bw.newLine();
                    bw.newLine();


                }
            }

            List<FieldInfo> copyFieldInfoList = new ArrayList<>(tableInfo.getFieldList());
//            List<FieldInfo> fieldList = tableInfo.getFieldList();

            copyFieldInfoList.addAll(tableInfo.getFieldExtendList());
            //getter setter和toString()重写！！！！
            for (FieldInfo fieldInfo : copyFieldInfoList) {
                String tempField = StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName());
                bw.write("\tpublic "+fieldInfo.getJavaType()+" get" + tempField + "() {\n");
                bw.write("\t\treturn this." + fieldInfo.getPropertyName() + ";");
                bw.write("\n\t}\n\n");

                bw.write("\tpublic void set" + tempField + "(" + fieldInfo.getJavaType() +
                        " " + fieldInfo.getPropertyName() + ") {\n");
                bw.write("\t\tthis." + fieldInfo.getPropertyName() + " = " + fieldInfo.getPropertyName() + ";");
                bw.write("\n\t}\n\n");
            }

//            StringBuilder toString = new StringBuilder();
//            Integer index = 0;
//            // 重写toString方法
//            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
//                index++;
//
//                String properName = fieldInfo.getPropertyName();
//                if (ArrayUtils.contains (Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType())){
//                    properName = "DateUtils.format(" + properName +", DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())";
//                }else if (ArrayUtils.contains(Constants.SQL_DATE_TYPES,fieldInfo.getSqlType())){
//                    properName = "DateUtils.format(" + properName +",DateTimePatternEnum.YYYY_MM_DD.getPattern())";
//                }
//
//
//                toString.append(fieldInfo.getComment()+":\" + (" + fieldInfo.getPropertyName() + " == null ? \"空\" : "+properName+")");
//                if(index<tableInfo.getFieldList().size()){
//                    toString.append(" + ");
////                    if(index%2==0){
//                    toString.append("\n\t\t");
////                    }
//                    toString.append("\", ");
//                }
//
//            }
//
//            bw.write("\t@Override\n");
//            bw.write("\tpublic String toString() {");
//            String toStringStr = toString.toString();
//            toStringStr = "\"" + toStringStr;
//            bw.newLine();
//            bw.write("\t\treturn "+toStringStr +";\n");
//            bw.write("\t}\n\n");




            bw.write("}");
            bw.flush();
        }catch (Exception e){
            logger.info("创建query失败：",e);
        }
//        File file = new File(folder, tableInfo.getBeanName() + ".java");
//        System.out.println(file.getPath());
//        try {
//            file.createNewFile();
//        }catch (Exception e){
//            e.printStackTrace();
//        }

    }

}
