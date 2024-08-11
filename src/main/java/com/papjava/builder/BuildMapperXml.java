package com.papjava.builder;

import com.papjava.bean.Constants;
import com.papjava.bean.FieldInfo;
import com.papjava.bean.TableInfo;
import com.papjava.utils.FileUtils;
import com.papjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName BuildMapper
 * @Description
 * @Author Paprika
 **/

public class BuildMapperXml {
    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);

    private static final String BASE_RESULT_MAP = "base_result_map";
    private static final String BASE_COLUMN_LIST = "base_column_list";
    private static final String BASE_QUERY_CONDITION = "base_query_condition";
//    private static final String BASE_CONDITION = "base_condition";
    private static final String BASE_QUERY_CONDITION_EXTEND = "base_query_condition_extend";
    private static final String QUERY_CONDITION = "query_condition";

    private static String poClass = null;
    static List<FieldInfo> idxFieldList = new ArrayList<>();
    static List<FieldInfo> primaryKeyFieldList = new ArrayList<>();
    static FieldInfo autoIncrementFieldInfo = null;
    static List<FieldInfo> fieldListTotal = null;

    static String tableName = null;

    public static void initValues(TableInfo tableInfo){

        tableName = tableInfo.getTableName();
        fieldListTotal = tableInfo.getFieldList();
        poClass = Constants.PACKAGE_PO + "." + tableInfo.getBeanName();
        //获取 1.索引idx列表（索引包括主键） 和 2.主键列表 3.唯一的自增主键String
        initIdxAndPrimaryKeysAndAutoIncrementKey(tableInfo);
    }

    //创建文件!!! 创建PO对象class文件
    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_MAPPER_XML);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        initValues(tableInfo);
        String className = tableInfo.getBeanName() + Constants.SUFFIX_MAPPER;

        File poFile = new File(folder, className + ".xml");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            FileUtils.interceptExist(poFile);

            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out,"utf8");
            bw = new BufferedWriter(outw);

            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
            bw.write("<mapper namespace=\""+Constants.PACKAGE_MAPPER +"."+className+"\">\n\n");

            //构建 实体映射
            buildResultMap(bw,tableInfo);
            //构建 通用查询结果列
            buildBaseSelectSql(bw,tableInfo);

            //构建 基础的查询条件(所有字段的 where 条件)
            buildBaseSelectConditionSql(bw,tableInfo);
            //构建 扩展的查询条件(Fuzzy timeStart timeEnd)
            buildExtendSelectConditionSQL(bw,tableInfo);

            //where 引用上面两个sql 作为通用条件限定
            bw.write("\t<!--通用条件限定语句-->\n");
            bw.write("\t<sql id=\""+QUERY_CONDITION+"\">\n");
            bw.write("\t\t<where>\n");
            bw.write("\t\t\t<include refid=\""+ BASE_QUERY_CONDITION +"\"/>\n");
            bw.write("\t\t\t<include refid=\""+ BASE_QUERY_CONDITION_EXTEND +"\"/>\n");
            bw.write("\t\t</where>\n");
            bw.write("\t</sql>\n\n");

            // select语句  1.返回List
            buildSelectListStatement(bw,tableInfo);

            // select语句   2.返回数量
            bw.write("\t<!--select数量count-->\n");
            bw.write("\t<select id=\"selectCount\" resultType=\"java.lang.Long\">\n");
            bw.write("\t\tSELECT count(1) FROM `"+tableName+"`\n");
            bw.write("\t\t<include refid=\""+QUERY_CONDITION+"\"/>\n");
            bw.write("\t</select>\n\n");

            // insert 匹配有值的字段插入
            buildInsertStatement(bw,tableInfo);

            // insertOrUpdate 插入或更新  （插入的话那么就是 匹配有值的字段插入）
            buildInsertOrUpdateStatement(bw,tableInfo);

            // 批量插入
            buildInsertBatchStatement(bw,tableInfo);

            // 批量插入或修改 insertOrUpdateBatch
            buildInsertOrUpdateBatchStatement(bw,tableInfo);


            /* 根据主键进行查询 更新 删除 */
            //根据主键进行查询
            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                List<FieldInfo> keyFieldValueList = entry.getValue();
                int i = 0;
                StringBuilder jointpropertyName = new StringBuilder();
                StringBuilder jointFieldName = new StringBuilder();
                for (FieldInfo fieldInfo : keyFieldValueList) {
                    i++;
                    String upper1stLetterPropertyName =StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName());
                    jointpropertyName.append(upper1stLetterPropertyName);
                    jointFieldName.append(fieldInfo.getFieldName()).append("=#{").append(upper1stLetterPropertyName).append("}");
                    if (i < keyFieldValueList.size()){
                        jointpropertyName.append("And");
                        jointFieldName.append(" AND ");
                    }
                }

                bw.write("\t<!--根据"+jointpropertyName+"查询-->\n");
                bw.write("\t<select id=\"selectBy"+jointpropertyName+"\" resultMap=\""+BASE_RESULT_MAP+"\">\n");
                bw.write("\t\tselect\n"+"\t\t<include refid=\""+BASE_COLUMN_LIST+"\"/>\n"+
                        "\t\tfrom `"+tableName+"` where "+jointFieldName+"\n");
                bw.write("\t</select>\n\n");

                bw.write("\t<!--根据"+jointpropertyName+"删除-->\n");
                bw.write("\t<delete id=\"deleteBy"+jointpropertyName+"\">\n");
                bw.write("\t\tdelete from `"+tableName+"` where "+jointFieldName+"\n");
                bw.write("\t</delete>\n\n");

                bw.write("\t<!--根据"+jointpropertyName+"更新-->\n");
                bw.write("\t<update id=\"updateBy"+jointpropertyName+"\" parameterType=\""+poClass+"\">\n");
                bw.write("\t\tUPDATE `"+tableName+"`\n");
                bw.write("\t\t<set>\n");
                for (FieldInfo fieldInfo : fieldListTotal) {
                    if (autoIncrementFieldInfo.getPropertyName()!=null&&!autoIncrementFieldInfo.getPropertyName().equals(fieldInfo.getPropertyName())){
                        bw.write("\t\t\t<if test=\""+"bean."+fieldInfo.getPropertyName() + " !=null"+"\">\n");
                        bw.write("\t\t\t\t"+fieldInfo.getFieldName()+" = #{bean."+fieldInfo.getPropertyName()+"},\n");
                        bw.write("\t\t\t</if>\n");
                    }
                }
                bw.write("\t\t</set>\n");
                bw.write("\t\t where "+jointFieldName+"\n");
                bw.write("\t</update>\n\n");

            }




            bw.write("\n</mapper>\n");
            bw.flush();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
        catch (Exception e){
            logger.info("创建mapper XML失败：",e);
        }
    }

    public static void initIdxAndPrimaryKeysAndAutoIncrementKey(TableInfo tableInfo){
        //获取 1.主键列表 和 2.索引列表（索引包括主键） 3.唯一的自增主键String
        Set<Map.Entry<String, List<FieldInfo>>> keyIndexEntries = tableInfo.getKeyIndexMap().entrySet();
        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexEntries) {
            idxFieldList.addAll(entry.getValue());
            if ("PRIMARY".equals(entry.getKey())){
                primaryKeyFieldList.addAll(entry.getValue());
                List<FieldInfo> fieldInfoList = entry.getValue();
                for (FieldInfo fieldInfo : fieldInfoList) {
                    if (fieldInfo.getAutoIncrement()) {
//                            autoIncrementPropertyName = autoIncrementPropertyName==null?fieldInfo.getPropertyName():autoIncrementPropertyName;
//                            autoIncrementPropertyName = fieldInfo.getPropertyName();
                        autoIncrementFieldInfo = fieldInfo;
                    }
                }
            }
        }
    }

    public static void buildResultMap(BufferedWriter bw,TableInfo tableInfo) throws IOException {
        bw.write("\t<!--实体映射-->\n");
//        String poClass = Constants.PACKAGE_PO + "." + tableInfo.getBeanName();
        bw.write("\t<resultMap id=\""+BASE_RESULT_MAP+"\" type=\"" + poClass + "\">\n");

//        List<FieldInfo> idFieldList = new ArrayList<>();
//        Set<Map.Entry<String, List<FieldInfo>>> keyIndexEntries = tableInfo.getKeyIndexMap().entrySet();
//        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexEntries) {
//            if ("PRIMARY".equals(entry.getKey())){
//                //这是List<FieldInfo>的！！这个一般是1个元素的！！说明是单个索引
//                //如果是多个元素  说明是联合索引
//                List<FieldInfo> fieldInfoList = entry.getValue();
//                /* 特别注意这里  本项目设计的是：如果是联合主键 多个主键 那么xml中会将所有主键都打上id！！！ */
//                idFieldList.addAll(fieldInfoList);
//                break;
////                    if (fieldInfoList.size()==1){
////                        idField = fieldInfoList.get(0);//说明只有一个元素 是唯一主键或者 唯一索引
////                        break;
////                    }
//            }
//        }



        for (FieldInfo fieldInfo : fieldListTotal) {
            bw.write("\t\t<!--"+fieldInfo.getComment()+"-->\n");
            String key = "";
            if (ArrayUtils.contains(primaryKeyFieldList.toArray(), fieldInfo)){
                key = "id";
            }else{
                key = "result";
            }
            bw.write("\t\t<"+key+" column=\""+fieldInfo.getFieldName()+"\" property=\""+fieldInfo.getPropertyName()+"\"/>\n");
        }

        bw.write("\t</resultMap>\n\n");

    }


    public static void buildBaseSelectSql(BufferedWriter bw,TableInfo tableInfo) throws IOException {
        bw.write("\t<!--通用查询结果列-->\n");
        bw.write("\t<sql id=\""+BASE_COLUMN_LIST+"\">\n");

        StringBuilder columnBuilder = new StringBuilder();
        for (FieldInfo fieldInfo : fieldListTotal) {
            columnBuilder.append(fieldInfo.getFieldName()).append(",");
        }
        columnBuilder = new StringBuilder(columnBuilder.substring(0, columnBuilder.length() - 1));
        bw.write("\t\t"+columnBuilder+"\n");

        bw.write("\t</sql>\n\n");
    }

    public static void buildBaseSelectConditionSql(BufferedWriter bw,TableInfo tableInfo) throws IOException {
        bw.write("\t<!--基础查询条件-->\n");
        bw.write("\t<sql id=\""+ BASE_QUERY_CONDITION +"\">\n");

        //StringBuilder testCondition = new StringBuilder();
        String testCondition = "";
        for (FieldInfo fieldInfo : fieldListTotal) {
            if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())){
                testCondition = "query."+fieldInfo.getPropertyName() + " !=null and query."+
                        fieldInfo.getPropertyName()+" != ''";
            }else{
                testCondition = "query."+fieldInfo.getPropertyName() + " !=null";
            }
            bw.write("\t\t<if test=\""+testCondition+"\">\n");
            bw.write("\t\t\tand "+fieldInfo.getFieldName()+" = #{query."
                    +fieldInfo.getPropertyName()+"}\n");
            bw.write("\t\t</if>\n");
        }

        bw.write("\t</sql>\n\n");
    }

    public static void buildExtendSelectConditionSQL(BufferedWriter bw,TableInfo tableInfo) throws IOException{
        bw.write("\t<!--扩展查询条件-->\n");
        bw.write("\t<sql id=\""+BASE_QUERY_CONDITION_EXTEND+"\">\n");
//        bw.write("\t\t<where>\n");
//        bw.write("\t\t\t<include refid=\""+BASE_QUERY_CONDITION_FILED+"\"/>\n");

        //StringBuilder testCondition = new StringBuilder();
        String testCondition = "";
        for (FieldInfo fieldInfo : tableInfo.getFieldExtendList()) {
            testCondition = "query."+fieldInfo.getPropertyName() + " !=null and query."+
                    fieldInfo.getPropertyName()+" != ''";
            bw.write("\t\t<if test=\""+testCondition+"\">\n");

            if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())){
                bw.write("\t\t\tand "+fieldInfo.getFieldName()+" like concat('%', #{query."
                        +fieldInfo.getPropertyName()+"}, '%')\n");
            }
            else if(ArrayUtils.contains(Constants.SQL_DATE_TYPES, fieldInfo.getSqlType())
                    ||ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType())){
                if(fieldInfo.getPropertyName().endsWith("Start")){
                    bw.write("\t\t\t<![CDATA[ and "+fieldInfo.getFieldName()+" >= str_to_date(#{query."
                            +fieldInfo.getPropertyName()+"}, '%Y-%m-%d') ]]>\n");
                }else{
                    bw.write("\t\t\t<![CDATA[ and "+fieldInfo.getFieldName()+" <  date_sub(str_to_date(#{query."
                            +fieldInfo.getPropertyName()+"}, '%Y-%m-%d'), interval -1 day) ]]>\n");
                }

            }

            bw.write("\t\t</if>\n");
        }
//        bw.write("\t\t</where>\n");

        bw.write("\t</sql>\n\n");
    }

    public static void buildSelectListStatement(BufferedWriter bw, TableInfo tableInfo) throws IOException {
        bw.write("\t<!--查询List 所有字段-->\n");
        bw.write("\t<select id=\"selectList\" resultMap=\""+BASE_RESULT_MAP+"\">\n");
        bw.write("\t\tSELECT\n");
        bw.write("\t\t<include refid=\""+BASE_COLUMN_LIST+"\"/>\n");
        bw.write("\t\tFROM `"+tableName+"`\n");
        bw.write("\t\t<include refid=\""+QUERY_CONDITION+"\"/>\n");

        bw.write("\t\t<if test=\"query.orderBy!=null\">\n");
        bw.write("\t\t\torder by ${query.orderBy}\n");  /* -----------------------注意这里写的是$ ----------------------------- */
        bw.write("\t\t</if>\n");

        bw.write("\t\t<if test=\"query.simplePage!=null\">\n");
        /* ---------------------注意这里写的是# --------------------------*/
        bw.write("\t\t\tlimit #{query.simplePage.start}, #{query.simplePage.end}\n");
        bw.write("\t\t</if>\n");

        bw.write("\t</select>\n\n");
    }

    public static void buildInsertStatement(BufferedWriter bw, TableInfo tableInfo) throws IOException {
        //匹配有值的字段插入
        bw.write("\t<!--插入 匹配有值的字段插入-->\n");
//        String autoIncrementPropertyName = null;
        bw.write("\t<insert id=\"insert\" parameterType=\""+poClass+"\">\n");
        if(null!=autoIncrementFieldInfo){
            bw.write("\t\t<selectKey keyProperty=\"bean."+autoIncrementFieldInfo.getPropertyName()+"\" " +
                    "resultType=\""+autoIncrementFieldInfo.getJavaType()+"\" order=\"AFTER\">\n");
            bw.write("\t\t\tSELECT LAST_INSERT_ID()\n");
            bw.write("\t\t</selectKey>\n");
//            autoIncrementPropertyName = fieldInfo.getPropertyName();
        }

//        Set<Map.Entry<String, List<FieldInfo>>> keyIndexEntries = tableInfo.getKeyIndexMap().entrySet();
//        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexEntries) {
//            if ("PRIMARY".equals(entry.getKey())){
//                List<FieldInfo> fieldInfoList = entry.getValue();
//                for (FieldInfo fieldInfo : fieldInfoList) {
//                    if (fieldInfo.getAutoIncrement()) {
//                        bw.write("\t\t<selectKey keyProperty=\"bean."+fieldInfo.getPropertyName()+"\" " +
//                                "resultType=\""+fieldInfo.getJavaType()+"\" order=\"AFTER\">\n");
//                        bw.write("\t\t\tSELECT LAST_INSERT_ID()\n");
//                        bw.write("\t\t</selectKey>\n");
//                        autoIncrementPropertyName = fieldInfo.getPropertyName();
//                        break;
//                    }
//                }
//
//            }
//        }
        // insert into `table`(a,b,c,d)      这个在后面values(a,b,c,d)
        bw.write("\t\tINSERT INTO `"+tableName+"`\n");
        bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        for (FieldInfo fieldInfo : fieldListTotal) {
            if (autoIncrementFieldInfo.getPropertyName()!=null&&!autoIncrementFieldInfo.getPropertyName().equals(fieldInfo.getPropertyName())){
                bw.write("\t\t\t<if test=\""+"bean."+fieldInfo.getPropertyName() + " !=null"+"\">\n");
                bw.write("\t\t\t\t"+fieldInfo.getFieldName()+",\n");
                bw.write("\t\t\t</if>\n");
            }
        }
        bw.write("\t\t</trim>\n");

        // values(a,b,c,d)
        bw.write("\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n");
        for (FieldInfo fieldInfo : fieldListTotal) {
            if (autoIncrementFieldInfo.getPropertyName()!=null&&!autoIncrementFieldInfo.getPropertyName().equals(fieldInfo.getPropertyName())){
                bw.write("\t\t\t<if test=\""+"bean."+fieldInfo.getPropertyName() + " !=null"+"\">\n");
                bw.write("\t\t\t\t#{bean."+fieldInfo.getPropertyName()+"},\n");
                bw.write("\t\t\t</if>\n");
            }
        }
        bw.write("\t\t</trim>\n");

        bw.write("\t</insert>\n\n");
    }

    public static void buildInsertOrUpdateStatement(BufferedWriter bw, TableInfo tableInfo) throws IOException {
        bw.write("\t<!--插入或更新 -->\n");


        bw.write("\t<insert id=\"insertOrUpdate\" parameterType=\""+poClass+"\">\n");
        // insert into `table`(a,b,c,d)      这个在后面values(a,b,c,d)
        bw.write("\t\tINSERT INTO `"+tableName+"`\n");
        bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        for (FieldInfo fieldInfo : fieldListTotal) {
            bw.write("\t\t\t<if test=\""+"bean."+fieldInfo.getPropertyName() + " !=null"+"\">\n");
            bw.write("\t\t\t\t"+fieldInfo.getFieldName()+",\n");
            bw.write("\t\t\t</if>\n");
        }
        bw.write("\t\t</trim>\n");


        // values(a,b,c,d)
        bw.write("\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n");
        for (FieldInfo fieldInfo : fieldListTotal) {
            bw.write("\t\t\t<if test=\""+"bean."+fieldInfo.getPropertyName() + " !=null"+"\">\n");
            bw.write("\t\t\t\t#{bean."+fieldInfo.getPropertyName()+"},\n");
            bw.write("\t\t\t</if>\n");
        }
        bw.write("\t\t</trim>\n");

        bw.write("\t\tON DUPLICATE key update\n");


        //下面的代码是   判断下面的字段是否已经都存在了
        bw.write("\t\t<trim prefix=\"\" suffix=\"\" suffixOverrides=\",\">\n");
        for (FieldInfo fieldInfo : fieldListTotal) {
            if (!ArrayUtils.contains(idxFieldList.toArray(),fieldInfo)){
                bw.write("\t\t\t<if test=\""+"bean."+fieldInfo.getPropertyName() + " !=null"+"\">\n");
                bw.write("\t\t\t\t"+fieldInfo.getFieldName()+" = VALUES("+fieldInfo.getFieldName()+"),\n");
                bw.write("\t\t\t</if>\n");
            }
        }

        bw.write("\t\t</trim>\n");

        bw.write("\t</insert>\n\n");
    }

    public static void buildInsertBatchStatement(BufferedWriter bw, TableInfo tableInfo) throws IOException {
        bw.write("\t<!-- 批量插入 -->\n");
//            bw.write("<\"insertBatch\" parameterType=\""+poClass+"\"" +
//                    " userGeneratedKeys=\"true\" keyProperty=\""++"\"");
        bw.write("");
        bw.write("\t<insert id=\"insertBatch\" parameterType=\""+poClass+"\">\n");
        bw.write("\t\tINSERT INTO `"+ tableName+"` \n");
        StringBuilder intoStr = new StringBuilder();
        StringBuilder valuesStr = new StringBuilder();
        int count01 = 0;
        for (FieldInfo fieldInfo : fieldListTotal) {
            if (!ArrayUtils.contains(primaryKeyFieldList.toArray(), fieldInfo)){
                count01++;
                intoStr.append(fieldInfo.getFieldName()).append(",");
                valuesStr.append("#{item.").append(fieldInfo.getPropertyName()).append("},");
//                    bw.write(fieldInfo.getFieldName()+",");
                if (count01%5==0&&count01<fieldListTotal.size()-1){
                    valuesStr.append("\n\t\t\t");
                }
            }
        }
        String substring = intoStr.substring(0, intoStr.length() - 1);
        bw.write("\t\t\t("+substring+") values \n");

//        bw.write("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\" open=\"(\" close=\")\">\n");
        bw.write("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\">\n");
        substring = valuesStr.substring(0, valuesStr.length() - 1);
        bw.write("\t\t\t("+substring+")\n");
        bw.write("\t\t</foreach>\n");

        bw.write("\t</insert>\n\n");
    }

    public static void buildInsertOrUpdateBatchStatement(BufferedWriter bw, TableInfo tableInfo) throws IOException {
        bw.write("\t<!-- 批量插入或修改 -->\n");
//            bw.write("<\"insertBatch\" parameterType=\""+poClass+"\"" +
//                    " userGeneratedKeys=\"true\" keyProperty=\""++"\"");
        bw.write("");
        bw.write("\t<insert id=\"insertOrUpdateBatch\" parameterType=\""+poClass+"\">\n");
        bw.write("\t\tINSERT INTO `"+ tableName+"` \n");
        StringBuilder intoStr = new StringBuilder();
        StringBuilder valuesStr = new StringBuilder();
        int count01 = 0;
        for (FieldInfo fieldInfo : fieldListTotal) {
            if (!ArrayUtils.contains(primaryKeyFieldList.toArray(), fieldInfo)){
                count01++;
                intoStr.append(fieldInfo.getFieldName()).append(",");
                valuesStr.append("#{item.").append(fieldInfo.getPropertyName()).append("},");
//                    bw.write(fieldInfo.getFieldName()+",");
                if (count01%5==0&&count01<fieldListTotal.size()-1){
                    valuesStr.append("\n\t\t\t");
                }
            }
        }
        String substring = intoStr.substring(0, intoStr.length() - 1);
        bw.write("\t\t\t("+substring+") values \n");

//        bw.write("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\" open=\"(\" close=\")\">\n");
        bw.write("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\">\n");
        substring = valuesStr.substring(0, valuesStr.length() - 1);
        bw.write("\t\t\t("+substring+")\n");
        bw.write("\t\t</foreach>\n");

        bw.write("\t\ton DUPLICATE key update\n");

        //下面的代码是   判断下面的字段是否已经都存在了
        StringBuilder updateStr = new StringBuilder();
        for (FieldInfo fieldInfo : fieldListTotal) {
            if (!ArrayUtils.contains(idxFieldList.toArray(),fieldInfo)){
//                    bw.write("\t\t\t<if test=\""+"bean."+fieldInfo.getPropertyName() + " !=null"+"\">\n");
//                    bw.write("\t\t\t</if>\n");
                updateStr = updateStr.append("\t\t"+fieldInfo.getFieldName()+" = VALUES("+fieldInfo.getFieldName()+"),\n");
            }
        }
        substring = updateStr.substring(0, updateStr.length() - 2);
        bw.write(substring+"\n");

        bw.write("\t</insert>\n\n");
    }

}
