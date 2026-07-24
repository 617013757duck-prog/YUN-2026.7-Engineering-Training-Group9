package com.medical.platform.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.medical.platform.serializer.SensitiveFieldSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 敏感字段脱敏注解
 * 用于标记需要脱敏的敏感字段
 *
 * @author 贺孟缘
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveFieldSerializer.class)
public @interface SensitiveField {

    /**
     * 脱敏类型
     */
    SensitiveType type() default SensitiveType.GENERAL;

    /**
     * 脱敏类型枚举
     */
    enum SensitiveType {
        /**
         * 身份证号：370***********1234
         */
        ID_CARD,

        /**
         * 手机号：138****5678
         */
        PHONE,

        /**
         * 通用脱敏：保留前3后4
         */
        GENERAL
    }
}