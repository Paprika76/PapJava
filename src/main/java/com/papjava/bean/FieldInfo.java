package com.papjava.bean;

import java.util.List;

/**
 * @ClassName TableInfo
 * @Description  主要从show full FIELDS from xx_table中获取
 * @Author Paprika
 * @date 2024-07-24
 **/

public class FieldInfo {
    /**
     * 字段名称
     */
    private String FieldName;

    /**
     * bean属性名称
     */
    private String propertyName;//比如user_id  写为userId

    private String sqlType;

    /**
     * 字段类型
     */
    private String javaType;

    /**
     * 字段注释
     */
    private String comment;

    /**
     * 字段是否自增长
     */
    private Boolean isAutoIncrement;

    public String getFieldName() {
        return FieldName;
    }

    public void setFieldName(String fieldName) {
        FieldName = fieldName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getAutoIncrement() {
        return isAutoIncrement;
    }

    public void setAutoIncrement(Boolean autoIncrement) {
        isAutoIncrement = autoIncrement;
    }

    @Override
    public String toString() {
        return "FieldInfo{" +
                "FieldName='" + FieldName + '\'' +
                ", propertyName='" + propertyName + '\'' +
                ", sqlType='" + sqlType + '\'' +
                ", javaType='" + javaType + '\'' +
                ", comment='" + comment + '\'' +
                ", isAutoIncrement=" + isAutoIncrement +
                '}';
    }
}
