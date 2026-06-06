package com.vueadmin.service.system;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.vueadmin.dto.PageResult;
import com.vueadmin.dto.RuleScriptDto;
import com.vueadmin.entity.system.RuleScript;
import com.vueadmin.repository.RuleScriptRepository;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 规则脚本服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleScriptService {

    private final RuleScriptRepository ruleScriptRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 搜索规则脚本
     */
    public PageResult<RuleScriptDto> search(String scriptCode, String scriptName, String scriptType, String status) {
        List<RuleScript> scripts = ruleScriptRepository.searchScripts(scriptCode, scriptName, scriptType, status);
        List<RuleScriptDto> list = scripts.stream().map(this::toDto).collect(Collectors.toList());
        return new PageResult<>(list, (long) list.size());
    }

    /**
     * 获取所有启用的脚本
     */
    public List<RuleScriptDto> getActiveScripts() {
        return ruleScriptRepository.findByStatus("active").stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取脚本详情
     */
    public RuleScriptDto getById(Long id) {
        return ruleScriptRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * 根据编码获取脚本
     */
    public RuleScriptDto getByCode(String scriptCode) {
        return ruleScriptRepository.findByScriptCode(scriptCode)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * 创建规则脚本
     */
    @Transactional
    public RuleScript create(RuleScriptDto dto) {
        if (ruleScriptRepository.existsByScriptCode(dto.getScriptCode())) {
            throw new RuntimeException("脚本编码已存在: " + dto.getScriptCode());
        }

        RuleScript script = new RuleScript();
        script.setScriptCode(dto.getScriptCode());
        script.setScriptName(dto.getScriptName());
        script.setScriptType(dto.getScriptType());
        script.setScriptContent(dto.getScriptContent());
        script.setInputParams(dto.getInputParams() != null ? JSON.toJSONString(dto.getInputParams()) : null);
        script.setOutputParams(dto.getOutputParams() != null ? JSON.toJSONString(dto.getOutputParams()) : null);
        script.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        script.setCreatedBy(dto.getCreatedBy());
        script.setDescription(dto.getDescription());

        return ruleScriptRepository.save(script);
    }

    /**
     * 更新规则脚本
     */
    @Transactional
    public void update(Long id, RuleScriptDto dto) {
        RuleScript script = ruleScriptRepository.findById(id).orElse(null);
        if (script == null) return;

        script.setScriptName(dto.getScriptName());
        script.setScriptType(dto.getScriptType());
        script.setScriptContent(dto.getScriptContent());
        script.setInputParams(dto.getInputParams() != null ? JSON.toJSONString(dto.getInputParams()) : null);
        script.setOutputParams(dto.getOutputParams() != null ? JSON.toJSONString(dto.getOutputParams()) : null);
        script.setStatus(dto.getStatus());
        script.setUpdatedBy(dto.getUpdatedBy());
        script.setDescription(dto.getDescription());

        ruleScriptRepository.save(script);
    }

    /**
     * 删除规则脚本
     */
    @Transactional
    public void delete(Long id) {
        ruleScriptRepository.deleteById(id);
    }

    /**
     * 执行Groovy脚本
     */
    public Object executeGroovy(String scriptContent, Map<String, Object> params) {
        try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader()) {
            Class<?> groovyClass = groovyClassLoader.parseClass(scriptContent);
            GroovyObject groovyObject = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance();

            // 设置参数
            if (params != null) {
                params.forEach(groovyObject::setProperty);
            }

            // 执行run方法
            return groovyObject.invokeMethod("run", new Object[]{});
        } catch (Exception e) {
            log.error("执行Groovy脚本失败", e);
            throw new RuntimeException("执行脚本失败: " + e.getMessage());
        }
    }

    /**
     * 执行JavaScript脚本
     */
    public Object executeJavaScript(String scriptContent, Map<String, Object> params) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("javascript");

            // 绑定参数
            if (params != null) {
                params.forEach((key, value) -> engine.put(key, value));
            }

            return engine.eval(scriptContent);
        } catch (Exception e) {
            log.error("执行JavaScript脚本失败", e);
            throw new RuntimeException("执行脚本失败: " + e.getMessage());
        }
    }

    /**
     * 测试执行脚本
     */
    public Map<String, Object> testScript(Long id, String testInput) {
        RuleScript script = ruleScriptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("脚本不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("scriptCode", script.getScriptCode());
        result.put("scriptType", script.getScriptType());

        try {
            // 解析测试输入
            Map<String, Object> params = null;
            if (testInput != null && !testInput.isEmpty()) {
                params = JSON.parseObject(testInput, Map.class);
            }

            // 执行脚本
            long startTime = System.currentTimeMillis();
            Object output;

            if ("groovy".equals(script.getScriptType())) {
                output = executeGroovy(script.getScriptContent(), params);
            } else if ("javascript".equals(script.getScriptType())) {
                output = executeJavaScript(script.getScriptContent(), params);
            } else {
                throw new RuntimeException("不支持的脚本类型: " + script.getScriptType());
            }

            long endTime = System.currentTimeMillis();

            result.put("success", true);
            result.put("output", output);
            result.put("executeTime", (endTime - startTime) + "ms");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 根据编码执行脚本
     */
    public Object executeByCode(String scriptCode, Map<String, Object> params) {
        RuleScript script = ruleScriptRepository.findByScriptCode(scriptCode)
                .orElseThrow(() -> new RuntimeException("脚本不存在: " + scriptCode));

        if (!"active".equals(script.getStatus())) {
            throw new RuntimeException("脚本未启用: " + scriptCode);
        }

        if ("groovy".equals(script.getScriptType())) {
            return executeGroovy(script.getScriptContent(), params);
        } else if ("javascript".equals(script.getScriptType())) {
            return executeJavaScript(script.getScriptContent(), params);
        } else {
            throw new RuntimeException("不支持的脚本类型: " + script.getScriptType());
        }
    }

    /**
     * 转换为DTO
     */
    private RuleScriptDto toDto(RuleScript script) {
        RuleScriptDto dto = new RuleScriptDto();
        dto.setId(script.getId());
        dto.setScriptCode(script.getScriptCode());
        dto.setScriptName(script.getScriptName());
        dto.setScriptType(script.getScriptType());
        dto.setScriptContent(script.getScriptContent());
        dto.setStatus(script.getStatus());
        dto.setCreatedBy(script.getCreatedBy());
        dto.setUpdatedBy(script.getUpdatedBy());
        dto.setDescription(script.getDescription());

        // 解析参数定义
        if (script.getInputParams() != null && !script.getInputParams().isEmpty()) {
            dto.setInputParams(JSON.parseArray(script.getInputParams(), RuleScriptDto.ScriptParam.class));
        }
        if (script.getOutputParams() != null && !script.getOutputParams().isEmpty()) {
            dto.setOutputParams(JSON.parseArray(script.getOutputParams(), RuleScriptDto.ScriptParam.class));
        }

        // 格式化时间
        if (script.getCreatedAt() != null) {
            dto.setCreatedAt(script.getCreatedAt().format(formatter));
        }
        if (script.getUpdatedAt() != null) {
            dto.setUpdatedAt(script.getUpdatedAt().format(formatter));
        }

        return dto;
    }
}
