package com.papjava.builder;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.papjava.bean.Constants;
import com.papjava.bean.FieldInfo;
import com.papjava.bean.TableInfo;
import com.papjava.utils.JsonUtils;
import com.papjava.utils.PropertiesUtils;
import com.papjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName BuilderTable
 * @Description
 * @Author Paprika
 * @date 2024-07-24
 **/

public class BuildTable {
    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);
    private static Connection conn= null;

    /**
     * show tables能获取到所有表名      show table status能获取到所有表名还有很多状态信息！！！！
     */
    private static String SQL_SHOW_TABLE_STATUS = "show table status";


    private static String SQL_SHOW_TABLE_FIELDS = "show full FIELDS from %s";


    private static String SQL_SHOW_TABLE_INDEXES = "show index from %s";




    static {
        String driverName = PropertiesUtils.getString("db.driver.name");
        String url = PropertiesUtils.getString("db.url");
        String user = PropertiesUtils.getString("db.username");
        String password = PropertiesUtils.getString("db.password");
        try {
            Class.forName(driverName) ;
            conn = DriverManager.getConnection(url,user, password);
        } catch(Exception e){
                logger.error("数据库连接失败",e);

        }
    }


    /**
     * show tables能获取到所有表名      show table status能获取到所有表名还有很多状态信息！！！！
     */
    public static List<TableInfo> getTables(){
        PreparedStatement ps = null;
        ResultSet tableResult = null;

        // 设置多个表的信息 每个表都是一个TableInfo
        List<TableInfo> tableInfoList = new ArrayList<>();

        try {
            ps =conn.prepareStatement(SQL_SHOW_TABLE_STATUS);
            tableResult =ps.executeQuery();
            while (tableResult.next()) {
                String tableName = tableResult.getString("Name");
                String comment = tableResult.getString("Comment");
                logger.info("tableName:{} , comment :{}", tableName, comment);
                String beanName = tableName;
                if (Constants.IGNORE_TABLE_PREFIX){
                    beanName = tableName.substring(beanName.indexOf("_")+1);
                }
                beanName = processField(beanName,true);//这个bean是表名  也就是类名  所以第一个字母要大写
//                logger.info("beanName:{}",beanName);
                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableName(tableName);
                tableInfo.setBeanName(beanName);
                tableInfo.setComment(comment);
                tableInfo.setBeanParamName(beanName+Constants.SUFFIX_BEAN_QUERY);

                readFieldInfo(tableInfo);

                getKeyIndexInfo(tableInfo);

//                System.out.println("tableInfo:");
//                System.out.println(tableInfo);
                logger.info(JsonUtils.convertObj2Json(tableInfo));
                tableInfoList.add(tableInfo);

            }
        }catch (Exception e){
            logger.error("读取表失败",e);
        }finally {
            if (tableResult!=null){
                try {
                    tableResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps!=null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn!=null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return tableInfoList;
    }

//    FieldInfo 字段的信息  即字段的
    public static void readFieldInfo(TableInfo tableInfo){
        PreparedStatement ps = null;
        ResultSet fieldResult = null;

        // 设置多个表的信息 每个表都是一个TableInfo
        List<FieldInfo> fieldInfoList = new ArrayList<>();

        List<FieldInfo> fieldExtendList = new ArrayList<>();

        try {
            ps =conn.prepareStatement(String.format(SQL_SHOW_TABLE_FIELDS,tableInfo.getTableName()));
            fieldResult =ps.executeQuery();
            while (fieldResult.next()) {
                String field = fieldResult.getString("field");
                String type = fieldResult.getString("type");//字段的数据类型
                String extra = fieldResult.getString("extra");
                String comment = fieldResult.getString("comment");
                if(type.indexOf("(" )>0){
                    type = type.substring(0,type.indexOf("("));
                }

                FieldInfo fieldInfo = new FieldInfo();
                fieldInfoList.add(fieldInfo);

                fieldInfo.setFieldName(field);
                String propertyName = processField(field, false);
                fieldInfo.setPropertyName(propertyName);
                fieldInfo.setAutoIncrement("auto_increment".equalsIgnoreCase(extra)?true:false);//忽略大小写比较字符串！！！！
                //字段的数据类型
                fieldInfo.setSqlType(type);
                fieldInfo.setJavaType(processJavaType(type));
                fieldInfo.setComment(comment);

                /**
                 * 注意这里设置tableInfo的特殊属性！！！！！
                 */
                if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE,type)){
                    tableInfo.setHaveBigDecimals(true);
                }
                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES,type)){
                    tableInfo.setHaveDate(true);
                }
                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)){
                    tableInfo.setHaveDateTime(true);
                }


//                    //添加query到一个list里去  后面我们添加getter和setter时就会加上用这个list的list
                //添加到extend中去 （为了保持友好的顺序 ） String name String nameFuzzy
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())) {
                    //添加query到一个list里去  后面我们添加getter和setter时就会加上用这个list的list
                    FieldInfo fuzzyFieldInfo = new FieldInfo();
                    fuzzyFieldInfo.setPropertyName(propertyName+Constants.SUFFIX_BEAN_QUERY_FUZZY);
                    fuzzyFieldInfo.setSqlType(fieldInfo.getSqlType());
                    fuzzyFieldInfo.setJavaType(fieldInfo.getJavaType());
                    fuzzyFieldInfo.setFieldName(fieldInfo.getFieldName());
//                    fieldInfoList.add(fuzzyFieldInfo);
                    fieldExtendList.add(fuzzyFieldInfo);
                }
//                    //添加query到一个list里去  后面我们添加getter和setter时就会加上用这个list的list
                //添加到extend中去 （为了保持友好的顺序 ） Date time   Date timeStart   Date timeEnd
                else if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, fieldInfo.getSqlType())
                        ||ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType())) {
                    //添加query到一个list里去  后面我们添加getter和setter时就会加上用这个list的list
                    FieldInfo timeStartFieldInfo = new FieldInfo();
                    timeStartFieldInfo.setPropertyName(propertyName+Constants.SUFFIX_BEAN_QUERY_TIME_START);
                    timeStartFieldInfo.setSqlType(fieldInfo.getSqlType());
                    timeStartFieldInfo.setJavaType("String");
                    timeStartFieldInfo.setFieldName(fieldInfo.getFieldName());
                    fieldExtendList.add(timeStartFieldInfo);

                    //添加query到一个list里去  后面我们添加getter和setter时就会加上用这个list的list
                    FieldInfo timeEndFieldInfo = new FieldInfo();
                    timeEndFieldInfo.setPropertyName(propertyName+Constants.SUFFIX_BEAN_QUERY_TIME_END);
                    timeEndFieldInfo.setSqlType(fieldInfo.getSqlType());
                    timeEndFieldInfo.setJavaType("String");
                    timeEndFieldInfo.setFieldName(fieldInfo.getFieldName());
                    fieldExtendList.add(timeEndFieldInfo);

                }


                //所有字段信息:
//                System.out.println(field+type+extra+comment+fieldInfo.getJavaType());
//
//                logger.info(field,type,extra,comment,fieldInfo.getJavaType());


            }
            tableInfo.setFieldList(fieldInfoList);
            tableInfo.setFieldExtendList(fieldExtendList);
//            getKeyIndexInfo(tableInfo);
//
//            System.out.println("tableInfo:");
//            System.out.println(tableInfo);
//                logger.info(tableInfo);/
        }catch (Exception e){
            logger.error("读取表失败",e);
        }finally {
            if(fieldResult!=null){
                try {
                    fieldResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps!=null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
//        return fieldInfoList;
    }

    //    FieldInfo 字段的信息  即字段的
    public static void getKeyIndexInfo(TableInfo tableInfo){
        PreparedStatement ps = null;
        ResultSet fieldResult = null;

        // 设置多个表的信息 每个表都是一个TableInfo
        List<FieldInfo> fieldInfoList = new ArrayList<>();

        Map<String, FieldInfo> tempMap = new HashMap<>();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            tempMap.put(fieldInfo.getFieldName(), fieldInfo);
        }

        try {
            ps =conn.prepareStatement(String.format(SQL_SHOW_TABLE_INDEXES,tableInfo.getTableName()));
            fieldResult =ps.executeQuery();
            while (fieldResult.next()) {
                //jdbc获取sql执行后的东西
                //keyName是index名
                String keyName = fieldResult.getString("key_name");
                Integer nonUnique = fieldResult.getInt("non_unique");//字段的数据类型
                String columnName = fieldResult.getString("column_name");
                if (nonUnique==1){//说明不是唯一索引
                    continue;
                }
                //注意KeyIndexMap字段的类型为： Map<String, List<FieldInfo>>

                List<FieldInfo> keyFieldList = tableInfo.getKeyIndexMap().get(keyName);
                if(null==keyFieldList){//把这个table的所有字段信息都添加过去
                    keyFieldList = new ArrayList();
                    tableInfo.getKeyIndexMap().put(keyName,keyFieldList);
                }
//                for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
//                    if(fieldInfo.getFieldName().equals(columnName)){
//                        keyFieldList.add(fieldInfo);
//                    }
//                }
                keyFieldList.add(tempMap.get(columnName));
            }

        }catch (Exception e){
            logger.error("读取索引失败",e);
        }finally {
            if(fieldResult!=null){
                try {
                    fieldResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps!=null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
//        return fieldInfoList;
    }


    public static String processField(String field, Boolean upperCaseFirstLetter){
        StringBuilder sb = new StringBuilder();
        String[] fields = field.split("_");//field是product_info  fields是product和info
        sb.append(upperCaseFirstLetter? StringUtils.upperCaseFirstLetter(fields[0]):fields[0]);
        for (int i = 1; i < fields.length; i++) {
            sb.append(StringUtils.upperCaseFirstLetter(fields[i]));
        }
        return sb.toString();
    }

    /**
     * sqlType是指tinyint、varchar这样的东西  要转换为javaType: Integer String
     */
    public static String processJavaType(String type){
        if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE,type)){
            return "Integer";
        }else if (ArrayUtils.contains (Constants.SQL_LONG_TYPE,type)){
            return "Long" ;
        }
        else if (ArrayUtils.contains(Constants.SQL_STRING_TYPE,type)) {
            return "String";
        }else if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)||ArrayUtils.contains(Constants.SQL_DATE_TYPES,type)){
            return "Date";//java一般只有Date类型
        }else if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE,type)){
            return "BigDecimal";
        }else {
            throw new RuntimeException ("无法识别的类型:" + type);
        }

    }

}

