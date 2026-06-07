package com.vueadmin.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误 1xxxx
    SUCCESS(0, "操作成功"),
    UNKNOWN_ERROR(10000, "未知错误"),
    PARAM_ERROR(10001, "参数错误"),
    PARAM_MISSING(10002, "参数缺失"),
    PARAM_INVALID(10003, "参数格式不正确"),

    // 认证授权错误 2xxxx
    UNAUTHORIZED(20001, "未登录或登录已过期"),
    TOKEN_INVALID(20002, "Token无效"),
    TOKEN_EXPIRED(20003, "Token已过期"),
    FORBIDDEN(20004, "没有权限访问"),
    LOGIN_ERROR(20005, "用户名或密码错误"),
    ACCOUNT_DISABLED(20006, "账号已被禁用"),
    ACCOUNT_LOCKED(20007, "账号已被锁定"),

    // 数据错误 3xxxx
    DATA_NOT_FOUND(30001, "数据不存在"),
    DATA_ALREADY_EXISTS(30002, "数据已存在"),
    DATA_SAVE_ERROR(30003, "数据保存失败"),
    DATA_UPDATE_ERROR(30004, "数据更新失败"),
    DATA_DELETE_ERROR(30005, "数据删除失败"),
    DATA_STATUS_ERROR(30006, "数据状态不正确"),

    // 业务错误 4xxxx
    // 用户相关 401xx
    USER_NOT_FOUND(40100, "用户不存在"),
    USER_ACCOUNT_EXISTS(40101, "账号已存在"),
    USER_PASSWORD_ERROR(40102, "密码错误"),

    // 角色相关 402xx
    ROLE_NOT_FOUND(40200, "角色不存在"),
    ROLE_CODE_EXISTS(40201, "角色编码已存在"),
    ROLE_IN_USE(40202, "角色正在使用中，无法删除"),

    // 权限相关 403xx
    PERMISSION_NOT_FOUND(40300, "权限不存在"),
    PERMISSION_CODE_EXISTS(40301, "权限编码已存在"),

    // 主数据相关 404xx
    MASTER_DATA_NOT_FOUND(40400, "主数据不存在"),
    MASTER_DATA_TYPE_NOT_FOUND(40401, "主数据类型不存在"),
    MASTER_DATA_CODE_EXISTS(40402, "主数据编码已存在"),
    MASTER_DATA_STATUS_ERROR(40403, "主数据状态不允许此操作"),

    // 流程相关 405xx
    WORKFLOW_NOT_FOUND(40500, "流程不存在"),
    WORKFLOW_INSTANCE_NOT_FOUND(40501, "流程实例不存在"),
    TASK_NOT_FOUND(40502, "任务不存在"),
    TASK_ALREADY_COMPLETED(40503, "任务已完成"),

    // 文件相关 406xx
    FILE_UPLOAD_ERROR(40600, "文件上传失败"),
    FILE_NOT_FOUND(40601, "文件不存在"),
    FILE_TYPE_ERROR(40602, "文件类型不正确"),
    FILE_SIZE_ERROR(40603, "文件大小超出限制"),

    // 系统错误 5xxxx
    BUSINESS_ERROR(40000, "业务处理失败"),
    SYSTEM_ERROR(50000, "系统错误"),
    DATABASE_ERROR(50001, "数据库错误"),
    CACHE_ERROR(50002, "缓存错误"),
    RPC_ERROR(50003, "远程调用错误"),
    SCRIPT_ERROR(50004, "脚本执行错误");

    private final Integer code;
    private final String message;
}
