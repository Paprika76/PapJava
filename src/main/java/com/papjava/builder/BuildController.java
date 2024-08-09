package com.papjava.builder;

import com.papjava.bean.Constants;
import com.papjava.bean.FieldInfo;
import com.papjava.bean.TableInfo;
import com.papjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BuildController
 * @Description
 * @Author Paprika
 **/

public class BuildController {

    private static final Logger logger = LoggerFactory.getLogger(BuildServiceImpl.class);
    //创建文件!!! 创建PO对象class文件
    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_CONTROLLER);
        if (!folder.exists()){
            folder.mkdirs();
        }
        String poClassName = tableInfo.getBeanName();
        String poInstance = StringUtils.lowerCaseFirstLetter(poClassName);
        String mapperClassName = poClassName + Constants.SUFFIX_MAPPER;
        String mapperInstance = StringUtils.lowerCaseFirstLetter(mapperClassName);
        String queryClassName = poClassName + Constants.SUFFIX_BEAN_QUERY;
        String serviceInterfaceBeanName = poClassName + Constants.SUFFIX_SERVICE;
        String serviceInstance = StringUtils.lowerCaseFirstLetter(serviceInterfaceBeanName);
        String beanName = poClassName + Constants.SUFFIX_CONTROLLER;
        File poFile = new File(folder,  beanName +".java");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out,"utf8");
            bw = new BufferedWriter(outw);
            bw.write("package " + Constants.PACKAGE_CONTROLLER + ";");
            bw.newLine();
            bw.newLine();

            bw.write("import "+ Constants.PACKAGE_QUERY+".SimplePage;\n");
            bw.write("import "+ Constants.PACKAGE_ENUMS+".PageSize;\n");
            bw.write("import " + Constants.PACKAGE_VO +".ResponseVO;\n");

            bw.write("import " + Constants.PACKAGE_MAPPER +"."+mapperClassName+";\n");
            bw.write("import " + Constants.PACKAGE_SERVICE+"."+serviceInterfaceBeanName+";\n");
            bw.write("import " + Constants.PACKAGE_PO+"."+poClassName+";\n");
            bw.write("import " + Constants.PACKAGE_QUERY+"."+poClassName+Constants.SUFFIX_BEAN_QUERY+";\n");
            bw.write("import " + Constants.PACKAGE_VO+"."+"PaginationResultVO"+";\n");
            bw.write("import org.springframework.web.bind.annotation.RequestBody;\n");
            bw.write("import org.springframework.web.bind.annotation.RequestMapping;\n");
            bw.write("import org.springframework.web.bind.annotation.RestController;\n");
            bw.write("import javax.annotation.Resource;\n");
            bw.write("\nimport java.util.List;\n");

            bw.newLine();
            //构建类的注释
            BuildComment.createClassComment(bw, tableInfo.getComment()+Constants.SUFFIX_CONTROLLER);
            bw.newLine();
//            bw.write("@RestController(\""+StringUtils.lowerCaseFirstLetter(beanName)+"\")\n");
            bw.write("@RestController\n");
            bw.write("@RequestMapping(\"/"+poInstance+"\")\n");
            bw.write("public class "+beanName+" extends "+"ABaseController"+"{\n\n");

            bw.write("\t@Resource\n");
            bw.write("\tprivate "+serviceInterfaceBeanName + " " + serviceInstance+";\n\n");


            /* 下面的都是函数方法体了 */
            BuildComment.createFieldComment(bw,"根据条件分页列表");
            bw.write("\t@RequestMapping(\"loadDataList\")\n");
            bw.write("\tpublic ResponseVO loadDataList("+queryClassName + " query) {\n");
            bw.write("\t\treturn getSuccessResponseVO("+serviceInstance+".findListByPage(query));\n");
            bw.write("\t}\n\n");

            BuildComment.createFieldComment(bw,"新增");
            bw.write("\t@RequestMapping(\"add\")\n");
            bw.write("\tpublic ResponseVO add("+poClassName +" bean) {\n");
            bw.write("\t\t"+serviceInstance+".add(bean);\n");
            bw.write("\t\treturn getSuccessResponseVO(null);\n");
            bw.write("\t}\n\n");

            BuildComment.createFieldComment(bw,"批量新增");
            bw.write("\t@RequestMapping(\"addBatch\")\n");
            bw.write("\tpublic ResponseVO addBatch(@RequestBody List<"+poClassName +"> listBean){\n");
            bw.write("\t\t"+serviceInstance+".addBatch(listBean);\n");
            bw.write("\t\treturn getSuccessResponseVO(null);\n");
            bw.write("\t}\n\n");

            BuildComment.createFieldComment(bw,"批量新增或修改");
            bw.write("\t@RequestMapping(\"addOrUpdateBatch\")\n");
            bw.write("\tpublic ResponseVO addOrUpdateBatch(@RequestBody List<"+poClassName +"> listBean){\n");
            bw.write("\t\t"+serviceInstance+".addOrUpdateBatch(listBean);\n");
            bw.write("\t\treturn getSuccessResponseVO(null);\n");
            bw.write("\t}\n\n");


            /* 根据主键进行查询 更新 删除 */
            //根据主键进行查询
            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                List<FieldInfo> keyFieldValueList = entry.getValue();
                int i = 0;
                StringBuilder jointPropertyName = new StringBuilder();
                StringBuilder jointArgumentPropertyNameStr = new StringBuilder();
                StringBuilder jointArgumentCallStr = new StringBuilder();
                for (FieldInfo fieldInfo : keyFieldValueList) {
                    i++;
                    String upper1stLetterPropertyName =StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName());
                    jointPropertyName.append(upper1stLetterPropertyName);
                    jointArgumentPropertyNameStr.append(fieldInfo.getJavaType()).append(" ").append(fieldInfo.getPropertyName());
                    jointArgumentCallStr.append(fieldInfo.getPropertyName());
                    if (i < keyFieldValueList.size()){
                        jointArgumentPropertyNameStr.append(", ");
                        jointArgumentCallStr.append(", ");
                        jointPropertyName.append("And");
                    }
                }

                BuildComment.createFieldComment(bw,"根据"+jointPropertyName+"查询对象");
                bw.write("\t@RequestMapping(\""+"get"+poClassName+"By"+jointPropertyName+"\")\n");
                bw.write("\tpublic ResponseVO "+"get"+poClassName+"By"+jointPropertyName+"("+jointArgumentPropertyNameStr+"){\n");
                bw.write("\t\treturn getSuccessResponseVO("+serviceInstance+".get"+poClassName+"By"+jointPropertyName+"("+jointArgumentCallStr+"));\n");
                bw.write("\t}\n\n");

                BuildComment.createFieldComment(bw,"根据"+jointPropertyName+"更新对象");
                bw.write("\t@RequestMapping(\""+"update"+poClassName+"By"+jointPropertyName+"\")\n");
                bw.write("\tpublic ResponseVO "+"update"+poClassName+"By"+jointPropertyName+"("+tableInfo.getBeanName() +" bean, "+jointArgumentPropertyNameStr+") {\n");
                bw.write("\t\t"+serviceInstance+".update"+poClassName+"By"+jointPropertyName+"(bean, "+jointArgumentCallStr+");\n");
                bw.write("\t\treturn getSuccessResponseVO(null);\n");
                bw.write("\t}\n\n");

                BuildComment.createFieldComment(bw,"根据"+jointPropertyName+"删除对象");
                bw.write("\t@RequestMapping(\""+"delete"+poClassName+"By"+jointPropertyName+"\")\n");
                bw.write("\tpublic ResponseVO "+"delete"+poClassName+"By"+jointPropertyName+"("+jointArgumentPropertyNameStr+"){\n");
                bw.write("\t\t"+serviceInstance+".delete"+poClassName+"By"+jointPropertyName+"("+jointArgumentCallStr+");\n");
                bw.write("\t\treturn getSuccessResponseVO(null);\n");
                bw.write("\t}\n\n");

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
