package com.papjava.builder;

import com.papjava.bean.Constants;
import com.papjava.bean.FieldInfo;
import com.papjava.bean.TableInfo;
import com.papjava.utils.FileUtils;
import com.papjava.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BuildMapper
 * @Description
 * @Author Paprika
 **/

public class BuildMapper {
    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);

    //创建文件!!! 创建PO对象class文件
    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_MAPPER);
        if (!folder.exists()){
            folder.mkdirs();
        }

        String className = tableInfo.getBeanName() + Constants.SUFFIX_MAPPER;

        File poFile = new File(folder, className + ".java");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            FileUtils.interceptExist(poFile);
            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out,"utf8");
            bw = new BufferedWriter(outw);
            bw.write("package " + Constants.PACKAGE_MAPPER + ";");
            bw.newLine();
            bw.newLine();
            bw.write("import org.apache.ibatis.annotations.Mapper;\n");
            bw.write("import org.apache.ibatis.annotations.Param;\n");

//            if (tableInfo.getHaveDate()||tableInfo.getHaveDateTime()){
//                bw.write("import java.util.Date;");
//                bw.newLine();
//                bw.newLine();
//            }
//
//            if (tableInfo.getHaveBigDecimals()){
//                bw.write("import java.math.BigDecimal;");
//                bw.newLine();
//            }


            bw.newLine();
            //构建类的注释
            BuildComment.createClassComment(bw, tableInfo.getComment() + "Mapper" );
            bw.newLine();
            bw.write("@Mapper\n");
            bw.write("public interface "+className+"<T, P> extends BaseMapper {");
            bw.newLine();

            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                List<FieldInfo> keyFieldValueList = entry.getValue();

                int i = 0;
                StringBuilder methodName = new StringBuilder();
                StringBuilder paramsCodeStr = new StringBuilder();
                for (FieldInfo fieldInfo : keyFieldValueList) {
                    i++;
                    methodName.append(StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName()));
                    paramsCodeStr.append("@Param(\""+StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName())+
                            "\") "+fieldInfo.getJavaType()+" " + fieldInfo.getPropertyName());
                    if (i < keyFieldValueList.size()){
                        methodName.append("And");
                        paramsCodeStr.append(", ");
                    }
                }
                BuildComment.createFieldComment(bw,"根据"+methodName+"查询");
                bw.write("\tT selectBy" + methodName + "("+paramsCodeStr+");\n\n");

                BuildComment.createFieldComment(bw,"根据"+methodName+"更新");
                bw.write("\tLong updateBy" + methodName + "(" + "@Param(\"bean\") T t, " +paramsCodeStr+");\n\n");

                BuildComment.createFieldComment(bw,"根据"+methodName+"删除");
                bw.write("\tLong deleteBy" + methodName + "("+paramsCodeStr+");\n\n");

            }


            bw.write("}");
            bw.flush();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
        catch (Exception e){
            logger.info("创建mapper失败：",e);
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
