package com.papjava.builder;

import com.papjava.bean.Constants;
import com.papjava.bean.FieldInfo;
import com.papjava.bean.TableInfo;
import com.papjava.utils.DateUtils;
import com.papjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @ClassName BuildPo
 * @Description
 * @Author Paprika
 **/

public class BuildPo {
    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);
    //创建文件!!! 创建PO对象class文件
    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_PO);
        if (!folder.exists()){
            folder.mkdirs();
        }
        File poFile = new File(folder, tableInfo.getBeanName() + ".java");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out,"utf8");
            bw = new BufferedWriter(outw);
            bw.write("package " + Constants.PACKAGE_PO + ";");
            bw.newLine();
            bw.newLine();
            bw.write("import java.io.Serializable;");
            bw.newLine();

            if (tableInfo.getHaveDate()||tableInfo.getHaveDateTime()){
                bw.write("import java.util.Date;");
                bw.newLine();
                bw.newLine();
                bw.write("import com.papjava.enums.DateTimePatternEnum;\n" +
                        "import com.papjava.utils.DateUtils;");
                bw.newLine();
                bw.newLine();
                bw.write(Constants.BEAN_DATE_UNFORMAT_CLASS);
                bw.newLine();
                bw.write(Constants.BEAN_DATE_FORMAT_CLASS);
                bw.newLine();
            }
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (ArrayUtils.contains(Constants.IGNORE_BEAN_TOJSON_FIELD.split(","),fieldInfo.getPropertyName())
                        ||ArrayUtils.contains(Constants.IGNORE_BEAN_TOJSON_FIELD.split(","),fieldInfo.getFieldName())){
                    bw.write(Constants.IGNORE_BEAN_TOJSON_CLASS);
                    bw.newLine();
                    bw.newLine();
                    break;
                }
            }

            if (tableInfo.getHaveBigDecimals()){
                bw.write("import java.math.BigDecimal;");
                bw.newLine();
            }


            bw.newLine();
            //构建类的注释
            BuildComment.createClassComment(bw, tableInfo.getComment());
            bw.newLine();

            bw.write("public class "+tableInfo.getBeanName()+" implements Serializable {");
            bw.newLine();



            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                BuildComment.createFieldComment(bw,fieldInfo.getComment());
                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,fieldInfo.getSqlType())){
                    bw.write("\t"+String.format(Constants.BEAN_DATE_FORMAT_EXPRESSION, DateUtils.YYYY_MM_DD_HH_MM_SS));
                    bw.newLine();

                    bw.write("\t"+String.format(Constants.BEAN_DATE_UNFORMAT_EXPRESSION, DateUtils.YYYY_MM_DD_HH_MM_SS));
                    bw.newLine();
                }
                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES,fieldInfo.getSqlType())){
                    bw.write("\t"+String.format(Constants.BEAN_DATE_FORMAT_EXPRESSION, DateUtils.YYYY_MM_DD));
                    bw.newLine();

                    bw.write("\t"+String.format(Constants.BEAN_DATE_UNFORMAT_EXPRESSION, DateUtils.YYYY_MM_DD));
                    bw.newLine();
                }
                //不管是驼峰还是原字段名都会进行判断！！！
                if (ArrayUtils.contains(Constants.IGNORE_BEAN_TOJSON_FIELD.split(","), fieldInfo.getPropertyName())
                    ||ArrayUtils.contains(Constants.IGNORE_BEAN_TOJSON_FIELD.split(","), fieldInfo.getFieldName())){
                    bw.write("\t"+String.format(Constants.IGNORE_BEAN_TOJSON_EXPRESSION, DateUtils.YYYY_MM_DD));
                    bw.newLine();
                }

                bw.write("\tprivate " + fieldInfo.getJavaType() + " " + fieldInfo.getPropertyName() + ";");
                bw.newLine();
                bw.newLine();
//                if (){
//                    bw/
//                }
            }


            //getter setter和toString()重写！！！！
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                String tempField = StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName());
                bw.write("\tpublic "+fieldInfo.getJavaType()+" get" + tempField + "() {\n");
                bw.write("\t\treturn this." + fieldInfo.getPropertyName() + ";");
                bw.write("\n\t}\n\n");

                bw.write("\tpublic void set" + tempField + "(" + fieldInfo.getJavaType() + " " + fieldInfo.getPropertyName() + ") {\n");
                bw.write("\t\tthis." + fieldInfo.getPropertyName() + " = " + fieldInfo.getPropertyName() + ";");
                bw.write("\n\t}\n\n");
            }

            StringBuilder toString = new StringBuilder();
            Integer index = 0;
            // 重写toString方法
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                index++;

                String properName = fieldInfo.getPropertyName();
                if (ArrayUtils.contains (Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType())){
                    properName = "DateUtils.format(" + properName +", DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())";
                }else if (ArrayUtils.contains(Constants.SQL_DATE_TYPES,fieldInfo.getSqlType())){
                    properName = "DateUtils.format(" + properName +",DateTimePatternEnum.YYYY_MM_DD.getPattern())";
                }


                toString.append(fieldInfo.getComment()+":\" + (" + fieldInfo.getPropertyName() + " == null ? \"空\" : "+properName+")");
                if(index<tableInfo.getFieldList().size()){
                    toString.append(" + ");
//                    if(index%2==0){
                        toString.append("\n\t\t");
//                    }
                    toString.append("\", ");
                }

            }

            bw.write("\t@Override\n");
            bw.write("\tpublic String toString() {");
            String toStringStr = toString.toString();
            toStringStr = "\"" + toStringStr;
            bw.newLine();
            bw.write("\t\treturn "+toStringStr +";\n");
            bw.write("\t}\n\n");

            bw.write("}");
            bw.flush();
        }catch (Exception e){
            logger.info("创建po失败：",e);
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
