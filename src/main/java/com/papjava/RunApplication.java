package com.papjava;

import com.papjava.bean.TableInfo;
import com.papjava.builder.*;

import java.util.List;

/**
 * @ClassName RunApplication
 * @Description
 * @Author Paprika
 * @date 2024-07-24
 **/

public class RunApplication {
    public static void main(String[] args) {
        //获取所有表的信息  表:表信息  +  所有字段信息  +  所有索引信息
        List<TableInfo> tableInfoList = BuildTable.getTables();

        BuildBase.execute();

        // 创建文件
        for (TableInfo tableInfo : tableInfoList) {
            BuildPo.execute(tableInfo);
            BuildQuery.execute(tableInfo);
            BuildMapper.execute(tableInfo);
            BuildMapperXml.execute(tableInfo);
            BuildService.execute(tableInfo);
            BuildServiceImpl.execute(tableInfo);
            BuildController.execute(tableInfo);
        }
    }
}
