package com.vueadmin.dto.distribution;

import lombok.Data;
import java.util.Map;

/**
 * 分发结果DTO
 */
@Data
public class DistributionResultDTO {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 目标系统返回的主键
     */
    private String targetKey;

    /**
     * 响应数据
     */
    private Map<String, Object> responseData;

    /**
     * SAP返回码
     */
    private String sapReturnCode;

    /**
     * SAP消息类型
     */
    private String sapMessageType;

    public static DistributionResultDTO success(String message) {
        DistributionResultDTO result = new DistributionResultDTO();
        result.setSuccess(true);
        result.setMessage(message);
        return result;
    }

    public static DistributionResultDTO success(String message, String targetKey) {
        DistributionResultDTO result = success(message);
        result.setTargetKey(targetKey);
        return result;
    }

    public static DistributionResultDTO fail(String errorMsg) {
        DistributionResultDTO result = new DistributionResultDTO();
        result.setSuccess(false);
        result.setErrorMsg(errorMsg);
        return result;
    }

    public static DistributionResultDTO fail(String errorCode, String errorMsg) {
        DistributionResultDTO result = fail(errorMsg);
        result.setErrorCode(errorCode);
        return result;
    }
}
