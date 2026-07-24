package com.medical.platform.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.medical.platform.annotation.SensitiveField;

import java.io.IOException;

/**
 * 敏感字段脱敏序列化器
 * 根据脱敏类型对敏感数据进行脱敏处理
 *
 * @author 贺孟缘
 */
public class SensitiveFieldSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null || value.isEmpty()) {
            gen.writeString(value);
            return;
        }

        // 获取脱敏类型
        SensitiveField sensitiveField = null;
        try {
            sensitiveField = (SensitiveField) gen.getCurrentValue()
                    .getClass()
                    .getDeclaredField(gen.getOutputContext().getCurrentName())
                    .getAnnotation(SensitiveField.class);
        } catch (NoSuchFieldException e) {
            // 字段不存在，直接返回原值
            gen.writeString(value);
            return;
        }

        if (sensitiveField == null) {
            gen.writeString(value);
            return;
        }

        String maskedValue;
        switch (sensitiveField.type()) {
            case ID_CARD:
                maskedValue = maskIdCard(value);
                break;
            case PHONE:
                maskedValue = maskPhone(value);
                break;
            case GENERAL:
            default:
                maskedValue = maskGeneral(value);
                break;
        }

        gen.writeString(maskedValue);
    }

    /**
     * 身份证号脱敏：370***********1234
     */
    private String maskIdCard(String idCard) {
        if (idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 手机号脱敏：138****5678
     */
    private String maskPhone(String phone) {
        if (phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 通用脱敏：保留前3后4
     */
    private String maskGeneral(String value) {
        if (value.length() <= 7) {
            return "***";
        }
        return value.substring(0, 3) + "***" + value.substring(value.length() - 4);
    }
}