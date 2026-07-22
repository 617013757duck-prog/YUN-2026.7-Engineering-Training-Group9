package com.medical.platform.controller;

import com.medical.platform.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author 成员B
 */
@Tag(name = "健康检查", description = "系统健康检查接口")
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "medical-platform-backend");
        data.put("version", "1.0.0");
        data.put("timestamp", LocalDateTime.now());
        return Result.success("服务正常运行", data);
    }
}