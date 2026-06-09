package com.vueadmin.service.distribution.connector;

import com.vueadmin.dto.distribution.DistributionResultDTO;
import com.vueadmin.entity.distribution.DisSystemConfig;

import java.util.Map;

/**
 * 系统连接器接口
 */
public interface SystemConnector {

    /**
     * 测试连接
     */
    boolean testConnection();

    /**
     * 执行分发 - 创建
     */
    DistributionResultDTO create(Map<String, Object> data);

    /**
     * 执行分发 - 更新
     */
    DistributionResultDTO update(String targetKey, Map<String, Object> data);

    /**
     * 执行分发 - 删除
     */
    DistributionResultDTO delete(String targetKey);

    /**
     * 获取系统配置
     */
    DisSystemConfig getConfig();

    /**
     * 获取系统类型
     */
    String getSystemType();
}
