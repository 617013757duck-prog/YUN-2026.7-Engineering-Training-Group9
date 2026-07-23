package com.medical.platform.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应类
 * 
 * @author 成员B
 */
@Data
public class PageResult<T> implements Serializable {
    
    private Long total;
    private Long pages;
    private Long current;
    private Long size;
    private List<T> records;
    
    public static <T> PageResult<T> of(Long total, Long pages, Long current, Long size, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setPages(pages);
        result.setCurrent(current);
        result.setSize(size);
        result.setRecords(records);
        return result;
    }
}