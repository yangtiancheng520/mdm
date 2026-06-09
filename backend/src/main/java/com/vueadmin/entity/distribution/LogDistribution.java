package com.vueadmin.entity.distribution;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分发日志实体
 */
@Data
@Entity
@Table(name = "dis_log_distribution")
public class LogDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_code", nullable = false, length = 100, unique = true)
    private String logCode;

    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;

    @Column(name = "data_type_name", length = 100)
    private String dataTypeName;

    @Column(name = "data_id", nullable = false)
    private Long dataId;

    @Column(name = "data_code", length = 100)
    private String dataCode;

    @Column(name = "data_name", length = 200)
    private String dataName;

    @Column(name = "system_config_id")
    private Long systemConfigId;

    @Column(name = "system_config_name", length = 100)
    private String systemConfigName;

    @Column(name = "system_type", length = 20)
    private String systemType;

    @Column(name = "interface_name", nullable = false, length = 100)
    private String interfaceName;

    @Column(name = "operation", length = 20)
    private String operation;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    @Column(name = "mapped_data", columnDefinition = "TEXT")
    private String mappedData;

    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    @Column(name = "sap_return_code", length = 20)
    private String sapReturnCode;

    @Column(name = "sap_message", length = 500)
    private String sapMessage;

    @Column(name = "sap_message_type", length = 10)
    private String sapMessageType;

    @Column(name = "sap_key", length = 100)
    private String sapKey;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "receive_time")
    private LocalDateTime receiveTime;

    @Column(name = "field_count")
    private Integer fieldCount = 0;

    @Column(name = "success_field_count")
    private Integer successFieldCount = 0;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "max_retry")
    private Integer maxRetry = 3;

    @Column(name = "parent_log_id")
    private Long parentLogId;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
