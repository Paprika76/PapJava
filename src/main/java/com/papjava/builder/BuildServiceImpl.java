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
 * @ClassName BuildService
 * @Description
 * @Author Paprika
 **/

public class BuildServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(BuildServiceImpl.class);
    //创建文件!!! 创建PO对象class文件
    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_SERVICE_IMPL);
        if (!folder.exists()){
            folder.mkdirs();
        }
        String poClassName = tableInfo.getBeanName();
        String mapperClassName = poClassName + Constants.SUFFIX_MAPPER;
        String mapperInstance = StringUtils.lowerCaseFirstLetter(mapperClassName);
        String queryClassName = poClassName + Constants.SUFFIX_BEAN_QUERY;
        String serviceInterfaceBeanName = poClassName + Constants.SUFFIX_SERVICE;
        String beanName = poClassName + Constants.SUFFIX_SERVICE_IMPL;
        File poFile = new File(folder,  beanName +".java");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out,"utf8");
            bw = new BufferedWriter(outw);
            bw.write("package " + Constants.PACKAGE_SERVICE_IMPL + ";");
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
            bw.write("import "+ Constants.PACKAGE_QUERY+".SimplePage;\n");
            bw.write("import "+ Constants.PACKAGE_ENUMS+".PageSize;\n");

            bw.write("import " + Constants.PACKAGE_MAPPER +"."+mapperClassName+";\n");
            bw.write("import " + Constants.PACKAGE_SERVICE+"."+serviceInterfaceBeanName+";\n");
            bw.write("import " + Constants.PACKAGE_PO+"."+poClassName+";\n");
            bw.write("import " + Constants.PACKAGE_QUERY+"."+poClassName+Constants.SUFFIX_BEAN_QUERY+";\n");
            bw.write("import " + Constants.PACKAGE_VO+"."+"PaginationResultVO"+";\n");
            bw.write("import org.springframework.stereotype.Service;\n");
            bw.write("import javax.annotation.Resource;\n");
            bw.write("\nimport java.util.List;\n");

            bw.newLine();
            //构建类的注释
            BuildComment.createClassComment(bw, tableInfo.getComment()+Constants.SUFFIX_SERVICE_IMPL+"实现类");
            bw.newLine();
            bw.write("@Service(\""+StringUtils.lowerCaseFirstLetter(beanName)+"\")\n");
            bw.write("public class "+beanName+" implements "+serviceInterfaceBeanName+"{\n\n");

            bw.write("\t@Resource\n");
            bw.write("\tprivate "+mapperClassName+"<"+poClassName+","+queryClassName+"> "+mapperInstance+";\n\n");


            /* 下面的都是函数方法体了 */
            BuildComment.createFieldComment(bw,"根据条件查询列表");
            bw.write("\t@Override\n\tpublic List<"+tableInfo.getBeanName()+"> findListByParam("+tableInfo.getBeanParamName() +" query){\n");
            bw.write("\t\treturn this."+mapperInstance+".selectList(query);");
            bw.write("\n\t}\n\n");

            BuildComment.createFieldComment(bw,"根据条件查询数量");
            bw.write("\t@Override\n\tpublic Long findCountByParam("+tableInfo.getBeanParamName() +" query){\n");
            bw.write("\t\treturn this."+mapperInstance+".selectCount(query);");
            bw.write("\n\t}\n\n");

            BuildComment.createFieldComment(bw,"分页查询");
            bw.write("\t@Override\n\tpublic PaginationResultVO<"+tableInfo.getBeanName()+"> findListByPage("+tableInfo.getBeanParamName() +" query){\n");
            bw.write("\t\tlong count = this.findCountByParam(query);\n");
            bw.write("\t\tint pageSize = query.getPageSize()==null?PageSize.SIZE15.getSize():query.getPageSize();\n");
            bw.write("\t\tSimplePage page = new SimplePage(query.getPageNo(), count, pageSize);\n");
            bw.write("\t\tquery.setSimplePage(page);\n");
            bw.write("\t\tList<"+poClassName+"> list = this.findListByParam(query);\n");
            bw.write("\t\tPaginationResultVO<"+poClassName+"> result = new PaginationResultVO(" +
                    "count, page.getPageSize(),page.getPageNo(),page.getPageTotal(),list);\n");
            bw.write("\t\treturn result;");
            bw.write("\n\t}\n\n");

            BuildComment.createFieldComment(bw,"新增一个");
            bw.write("\t@Override\n\tpublic Long add("+tableInfo.getBeanName() +" bean){\n");
            bw.write("\t\treturn this."+mapperInstance+".insert(bean);");
            bw.write("\n\t}\n\n");

            BuildComment.createFieldComment(bw,"批量新增");
            bw.write("\t@Override\n\tpublic Long addBatch(List<"+tableInfo.getBeanName() +"> listBean){\n");
            bw.write("\t\tif (listBean==null || listBean.isEmpty()) {\n");
            bw.write("\t\t\treturn 0L;\n\t\t}\n");
            bw.write("\t\treturn this."+mapperInstance+".insertBatch(listBean);");
            bw.write("\n\t}\n\n");

            BuildComment.createFieldComment(bw,"批量新增或修改");
            bw.write("\t@Override\n\tpublic Long addOrUpdateBatch(List<"+tableInfo.getBeanName() +"> listBean){\n");
            bw.write("\t\tif (listBean==null || listBean.isEmpty()) {\n");
            bw.write("\t\t\treturn 0L;\n\t\t}\n");
            bw.write("\t\treturn this."+mapperInstance+".insertOrUpdateBatch(listBean);");
            bw.write("\n\t}\n\n");

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
                bw.write("\t@Override\n\tpublic "+poClassName+" get"+poClassName+"By"+jointPropertyName+"("+jointArgumentPropertyNameStr+"){\n");
                bw.write("\t\treturn this."+mapperInstance+".selectBy"+jointPropertyName+"("+jointArgumentCallStr+");");
                bw.write("\n\t}\n\n");

                BuildComment.createFieldComment(bw,"根据"+jointPropertyName+"更新对象");
                bw.write("\t@Override\n\tpublic Long update"+poClassName+"By"+jointPropertyName+"("+tableInfo.getBeanName() +" bean, "+jointArgumentPropertyNameStr+") {\n");
                bw.write("\t\treturn this."+mapperInstance+".updateBy"+jointPropertyName+"(bean, "+jointArgumentCallStr+");");
                bw.write("\n\t}\n\n");

                BuildComment.createFieldComment(bw,"根据"+jointPropertyName+"删除对象");
                bw.write("\t@Override\n\tpublic Long delete"+poClassName+"By"+jointPropertyName+"("+jointArgumentPropertyNameStr+"){\n");
                bw.write("\t\treturn this."+mapperInstance+".deleteBy"+jointPropertyName+"("+jointArgumentCallStr+");");
                bw.write("\n\t}\n\n");

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
