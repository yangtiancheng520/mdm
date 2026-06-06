package com.vueadmin.dto;

import lombok.Data;
import java.util.List;

/**
 * 规则脚本DTO
 */
@Data
public class RuleScriptDto {

    private Long id;

    private String scriptCode;

    private String scriptName;

    private String scriptType;

    private String scriptContent;

    private List<ScriptParam> inputParams;

    private List<ScriptParam> outputParams;

    private String status;

    private String createdBy;

    private String createdAt;

    private String updatedBy;

    private String updatedAt;

    private String description;

    /**
     * 测试输入参数（用于执行测试）
     */
    private String testInput;

    /**
     * 测试输出结果（用于执行测试）
     */
    private String testOutput;

    /**
     * 脚本参数定义
     */
    @Data
    public static class ScriptParam {
        private String name;
        private String type;
        private Boolean required;
        private String description;
    }
}
