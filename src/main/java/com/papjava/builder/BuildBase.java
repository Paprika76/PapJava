package com.papjava.builder;

import com.papjava.bean.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @ClassName BuildBase
 * @Description
 * @Author Paprika
 **/

public class BuildBase {
    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);


    public static void execute() {

        String importStr = "";

        //生成vo常用模块类 分页器
        build(Constants.PACKAGE_VO,"PaginationResultVO", Constants.PATH_VO);

        //生成vo常用模块类 数据响应体
        build(Constants.PACKAGE_VO,"ResponseVO", Constants.PATH_VO);

        //生成date枚举enum模块
        build(Constants.PACKAGE_ENUMS,"DateTimePatternEnum", Constants.PATH_ENUMS);

        //生成date枚举utils模块
        build(Constants.PACKAGE_UTILS,"DateUtils", Constants.PATH_UTILS);

        //生成BaseMapper mapper模块中!!
        build(Constants.PACKAGE_MAPPER,"BaseMapper", Constants.PATH_MAPPER);

        //生成pageSize enum模块
        String pageSizeEnum = "PageSize";
        build(Constants.PACKAGE_ENUMS,pageSizeEnum, Constants.PATH_ENUMS);
        //生成ResponseCodeEnum enum模块
        String responseCodeEnum = "ResponseCodeEnum";
        build(Constants.PACKAGE_ENUMS,responseCodeEnum, Constants.PATH_ENUMS);


        //生成BaseQuery query模块
        build(Constants.PACKAGE_QUERY,"BaseQuery", Constants.PATH_QUERY);

        //生成SimplePage query模块
        importStr = "\nimport " + Constants.PACKAGE_ENUMS + "." + pageSizeEnum+";\n";
        build(Constants.PACKAGE_QUERY,"SimplePage", Constants.PATH_QUERY, importStr);


        //异常处理exception模块
        importStr = "\nimport " + Constants.PACKAGE_ENUMS + "." + responseCodeEnum+";\n";
        build(Constants.PACKAGE_EXCEPTION,"BusinessException", Constants.PATH_EXCEPTION, importStr);

        //ABaseController controller模块
        importStr = "\nimport "+Constants.PACKAGE_ENUMS+".ResponseCodeEnum;\n" +
                "import "+Constants.PACKAGE_VO+".ResponseVO;\n" +
                "import "+Constants.PACKAGE_EXCEPTION+".BusinessException;\n";
        build(Constants.PACKAGE_CONTROLLER,"ABaseController", Constants.PATH_CONTROLLER, importStr);

        importStr = "\nimport "+Constants.PACKAGE_ENUMS+".ResponseCodeEnum;\n" +
                "import "+Constants.PACKAGE_VO+".ResponseVO;\n" +
                "import "+Constants.PACKAGE_EXCEPTION+".BusinessException;\n" +
                "import org.springframework.dao.DuplicateKeyException;\n" +
                "import org.springframework.web.bind.annotation.ExceptionHandler;\n" +
                "import org.springframework.web.bind.annotation.RestController;\n" +
                "import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;\n" +
                "import org.springframework.web.servlet.NoHandlerFoundException;\n" +
                "\n" +
                "import javax.servlet.http.HttpServletRequest;\n\n" +
                "import org.slf4j.Logger;\n" +
                "import org.slf4j.LoggerFactory;\n" +
                "\n" +
                "import java.net.BindException;\n";
        build(Constants.PACKAGE_CONTROLLER,"AGlobalExceptionHandlerController", Constants.PATH_CONTROLLER, importStr);



    }

    private static void build(String packageName, String fileName, String outputPath, String importStr) {
        File folder = new File(outputPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File javaFile = new File(outputPath, fileName + ".java");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;

        InputStream in = null;
        InputStreamReader inr = null;
        BufferedReader bf = null;
        try {
            out = new FileOutputStream(javaFile);
            outw = new OutputStreamWriter(out, "utf-8");
            bw = new BufferedWriter(outw);
            String templatePath = BuildBase.class.getClassLoader().getResource("template/" + fileName +
                    ".txt").getPath();
            templatePath = URLDecoder.decode(templatePath, StandardCharsets.UTF_8.name());
            in = new FileInputStream(templatePath);
            inr = new InputStreamReader(in, "utf-8");
            bf = new BufferedReader(inr);
            String lineInfo = null;
            bw.write("package " + packageName+ ";\n");
            bw.write(importStr);
            while ((lineInfo = bf.readLine()) != null) {
                bw.write(lineInfo);
                bw.newLine();
            }
            bw.flush();


        } catch (Exception e) {
            logger.error("生成基础模块类：{}，失败", fileName, e);

        }
    }

    private static void build(String packageName, String fileName, String outputPath){
        build(packageName, fileName, outputPath, "");
    }

}