package com.papjava.builder;

import com.papjava.bean.Constants;
import com.papjava.bean.FieldInfo;
import com.papjava.bean.TableInfo;
import com.papjava.utils.DateUtils;
import com.papjava.utils.PropertiesUtils;
import com.papjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BuildService
 * @Description
 * @Author Paprika
 * @date 2024-07-31
 **/

public class BuildService {
    private static final Logger logger = LoggerFactory.getLogger(BuildService.class);
    //创建文件!!! 创建PO对象class文件
    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_SERVICE);
        if (!folder.exists()){
            folder.mkdirs();
        }
        String poName = tableInfo.getBeanName();
        String beanName = poName + Constants.SUFFIX_SERVICE;
        File poFile = new File(folder,  beanName +".java");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out,"utf8");
            bw = new BufferedWriter(outw);
            bw.write("package " + Constants.PACKAGE_SERVICE + ";");
            bw.newLine();
            bw.newLine();

            bw.write("import " + Constants.PACKAGE_PO+"."+poName+";\n");
            bw.write("import " + Constants.PACKAGE_QUERY+"."+poName+Constants.SUFFIX_BEAN_QUERY+";\n");
            bw.write("import " + Constants.PACKAGE_VO+"."+"PaginationResultVO"+";\n");
            bw.write("\nimport java.util.List;\n");

            bw.newLine();
            //构建类的注释
            BuildComment.createClassComment(bw, tableInfo.getComment()+Constants.SUFFIX_SERVICE+"接口类");
            bw.newLine();

            bw.write("public interface "+beanName+" {");
            bw.newLine();


            BuildComment.createFieldComment(bw,"根据条件查询列表");
            bw.write("\tList<"+tableInfo.getBeanName()+"> findListByParam("+tableInfo.getBeanParamName() +" query);\n\n");

            BuildComment.createFieldComment(bw,"根据条件查询数量");
            bw.write("\tLong findCountByParam("+tableInfo.getBeanParamName() +" query);\n\n");

            BuildComment.createFieldComment(bw,"分页查询");
            bw.write("\tPaginationResultVO<"+tableInfo.getBeanName()+"> findListByPage("+tableInfo.getBeanParamName() +" query);\n\n");

            BuildComment.createFieldComment(bw,"新增一个");
            bw.write("\tLong add("+tableInfo.getBeanName() +" bean);\n\n");

            BuildComment.createFieldComment(bw,"批量新增");
            bw.write("\tLong addBatch(List<"+tableInfo.getBeanName() +"> listBean);\n\n");

            BuildComment.createFieldComment(bw,"批量新增或修改");
            bw.write("\tLong addOrUpdateBatch(List<"+tableInfo.getBeanName() +"> listBean);\n\n");


            /* 根据主键进行查询 更新 删除 */
            //根据主键进行查询
            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                List<FieldInfo> keyFieldValueList = entry.getValue();
                int i = 0;
                StringBuilder jointPropertyName = new StringBuilder();
                StringBuilder jointArgumentPropertyNameStr = new StringBuilder();
                for (FieldInfo fieldInfo : keyFieldValueList) {
                    i++;
                    String upper1stLetterPropertyName =StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName());
                    jointPropertyName.append(upper1stLetterPropertyName);
                    jointArgumentPropertyNameStr.append(fieldInfo.getJavaType()).append(" ").append(fieldInfo.getPropertyName());
                    if (i < keyFieldValueList.size()){
                        jointArgumentPropertyNameStr.append(", ");
                        jointPropertyName.append("And");
                    }
                }

                BuildComment.createFieldComment(bw,"根据"+jointPropertyName+"查询对象");
                bw.write("\t"+poName+" get"+poName+"By"+jointPropertyName+"("+jointArgumentPropertyNameStr+");\n\n");

                BuildComment.createFieldComment(bw,"根据"+jointPropertyName+"更新对象");
                bw.write("\tLong update"+poName+"By"+jointPropertyName+"("+tableInfo.getBeanName() +" bean, "+jointArgumentPropertyNameStr+");\n\n");

                BuildComment.createFieldComment(bw,"根据"+jointPropertyName+"更新删除");
                bw.write("\tLong delete"+poName+"By"+jointPropertyName+"("+jointArgumentPropertyNameStr+");\n\n");

            }


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
