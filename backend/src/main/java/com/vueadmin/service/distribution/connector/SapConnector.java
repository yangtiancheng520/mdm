package com.vueadmin.service.distribution.connector;

import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.vueadmin.dto.distribution.DistributionResultDTO;
import com.vueadmin.entity.distribution.DisSystemConfig;
import com.vueadmin.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * SAP连接器 - 基于JCo 3.0实现
 * 支持供应商、客户、物料的创建/修改/删除
 */
public class SapConnector implements SystemConnector {

    private static final Logger logger = LoggerFactory.getLogger(SapConnector.class);

    private final DisSystemConfig config;
    private final Map<String, Object> connectionConfig;
    private JCoDestination destination;

    // 数据类型对应的BAPI配置
    private static final Map<String, BapiConfig> BAPI_CONFIGS = new HashMap<>();

    static {
        // 客户BAPI配置
        BAPI_CONFIGS.put("CUSTOMER", new BapiConfig(
            "BAPI_CUSTOMER_CREATE",
            "BAPI_CUSTOMER_CHANGE",
            null,
            "CUSTOMERNO",
            new String[]{"CUSTOMERNO", "CUSTOMER_NO"}
        ));

        // 物料BAPI配置
        BAPI_CONFIGS.put("MATERIAL", new BapiConfig(
            "BAPI_MATERIAL_SAVEDATA",
            "BAPI_MATERIAL_SAVEDATA",
            null,
            "MATERIAL",
            new String[]{"MATERIAL", "MATNR"}
        ));
    }

    // 目标数据提供器缓存
    private static final Map<String, SimpleDestinationDataProvider> providers = new ConcurrentHashMap<>();

    public SapConnector(DisSystemConfig config, Map<String, Object> connectionConfig) {
        this.config = config;
        this.connectionConfig = connectionConfig;
        initDestination();
    }

    /**
     * 初始化SAP连接
     */
    private void initDestination() {
        try {
            String destName = "SAP_" + config.getConfigCode();

            Properties props = new Properties();
            props.setProperty(DestinationDataProvider.JCO_ASHOST, getStringAny("ashost", "host"));
            props.setProperty(DestinationDataProvider.JCO_SYSNR, getStringAny("sysnr", "systemNumber"));
            props.setProperty(DestinationDataProvider.JCO_CLIENT, getString("client"));
            props.setProperty(DestinationDataProvider.JCO_USER, getString("user"));
            props.setProperty(DestinationDataProvider.JCO_PASSWD, getStringAny("passwd", "password"));
            props.setProperty(DestinationDataProvider.JCO_LANG, getStringAny("lang", "language", "ZH"));
            props.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, String.valueOf(config.getPoolSize()));
            props.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, String.valueOf(config.getPoolSize() * 2));

            SimpleDestinationDataProvider provider = providers.get(destName);
            if (provider == null) {
                provider = new SimpleDestinationDataProvider();
                providers.put(destName, provider);
            }
            provider.addDestination(destName, props);

            try {
                com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(provider);
            } catch (IllegalStateException e) {
                logger.debug("DestinationDataProvider已注册: {}", destName);
            }

            destination = JCoDestinationManager.getDestination(destName);
            logger.info("SAP连接初始化成功: {} -> {}", config.getConfigName(), destName);

        } catch (JCoException e) {
            logger.error("初始化SAP连接失败: {}", e.getMessage());
            throw new BusinessException("初始化SAP连接失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("初始化SAP连接失败: {}", e.getMessage());
            throw new BusinessException("初始化SAP连接失败: " + e.getMessage());
        }
    }

    @Override
    public boolean testConnection() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> future = executor.submit(() -> {
                try {
                    destination.ping();
                    logger.info("SAP连接测试成功: {}", config.getConfigName());
                    return true;
                } catch (JCoException e) {
                    logger.error("SAP连接测试失败: {}", e.getMessage());
                    throw new RuntimeException(e.getMessage());
                }
            });
            return future.get(20, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.error("SAP连接测试超时: {}", config.getConfigName());
            throw new BusinessException("连接超时，请检查SAP服务器网络连通性");
        } catch (Exception e) {
            logger.error("SAP连接测试失败: {}", e.getMessage());
            throw new BusinessException("连接失败: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }
    }

    @Override
    public DistributionResultDTO create(Map<String, Object> data) {
        String dataType = (String) data.get("_DATA_TYPE");
        if (dataType == null) {
            dataType = "VENDOR";
        }

        BapiConfig bapiConfig = BAPI_CONFIGS.get(dataType);
        if (bapiConfig == null) {
            return DistributionResultDTO.fail("INVALID_DATA_TYPE", "不支持的数据类型: " + dataType);
        }

        return executeBAPI(bapiConfig.createBapi, data, dataType, "CREATE");
    }

    @Override
    public DistributionResultDTO update(String targetKey, Map<String, Object> data) {
        String dataType = (String) data.get("_DATA_TYPE");
        if (dataType == null) {
            dataType = "VENDOR";
        }

        BapiConfig bapiConfig = BAPI_CONFIGS.get(dataType);
        if (bapiConfig == null) {
            return DistributionResultDTO.fail("INVALID_DATA_TYPE", "不支持的数据类型: " + dataType);
        }

        Map<String, Object> newData = new HashMap<>(data);
        newData.put(bapiConfig.keyField, targetKey);

        return executeBAPI(bapiConfig.updateBapi, newData, dataType, "UPDATE");
    }

    @Override
    public DistributionResultDTO delete(String targetKey) {
        return DistributionResultDTO.fail("NOT_SUPPORTED", "SAP系统不支持物理删除操作，请使用状态标记");
    }

    /**
     * 执行BAPI函数
     */
    private DistributionResultDTO executeBAPI(String bapiName, Map<String, Object> data, String dataType, String operation) {
        try {
            JCoFunction function = destination.getRepository().getFunction(bapiName);
            if (function == null) {
                return DistributionResultDTO.fail("BAPI_NOT_FOUND", "BAPI函数不存在: " + bapiName);
            }

            // 设置导入参数
            setImportParameters(function, data, dataType);

            // 执行
            function.execute(destination);

            // 获取返回结果
            return parseResult(function, dataType);

        } catch (JCoException e) {
            logger.error("执行BAPI失败: {} - {}", bapiName, e.getMessage());
            return DistributionResultDTO.fail("JCO_ERROR", e.getMessage());
        }
    }

    /**
     * 设置导入参数
     */
    private void setImportParameters(JCoFunction function, Map<String, Object> data, String dataType) throws JCoException {
        JCoParameterList importParams = function.getImportParameterList();

        if (importParams == null) {
            logger.warn("BAPI {} 没有导入参数", function.getName());
            return;
        }

        switch (dataType) {
            case "CUSTOMER":
                setCustomerParameters(function, importParams, data);
                break;
            case "MATERIAL":
                setMaterialParameters(function, importParams, data);
                break;
            default:
                setParameters(importParams, data);
        }
    }

    
    /**
     * 设置客户参数
     */
    private void setCustomerParameters(JCoFunction function, JCoParameterList importParams, Map<String, Object> data) throws JCoException {
        String[] structureNames = {"GENERALDATA", "I_GENERALDATA", "PI_GENERALDATA"};
        JCoStructure generalData = null;

        for (String structName : structureNames) {
            try {
                generalData = importParams.getStructure(structName);
                break;
            } catch (Exception e) {
                // 继续尝试
            }
        }

        if (generalData != null) {
            setStructureValue(generalData, "CUSTOMER_NO", data.get("customer_code"));
            setStructureValue(generalData, "NAME", data.get("customer_name"));
            setStructureValue(generalData, "NAME_2", data.get("customer_name2"));
            setStructureValue(generalData, "CITY", data.get("city"));
            setStructureValue(generalData, "COUNTRY", data.get("country"));
        } else {
            setParameters(importParams, data);
        }
    }

    /**
     * 设置物料参数
     * BAPI: BAPI_MATERIAL_SAVEDATA
     * 所需结构:
     * - HEADDATA (BAPIMATHEAD): 物料头信息
     * - CLIENTDATA (BAPI_MARA): 客户端数据
     * - CLIENTDATAX: 标记需要更新的字段
     * - MATERIALDESCRIPTION: 物料描述表
     */
    private void setMaterialParameters(JCoFunction function, JCoParameterList importParams, Map<String, Object> data) throws JCoException {
        // 判断是创建还是更新（通过检查是否已有物料号）
        String materialNo = (String) data.get("MATERIAL");
        boolean isCreate = !materialExistsInSAP(materialNo);

        // 设置HEADDATA (BAPIMATHEAD)
        try {
            JCoStructure headData = importParams.getStructure("HEADDATA");
            // 字段映射: MATNR -> MATERIAL, MBRSH -> IND_SECTOR, MTART -> MATL_TYPE
            setStructureValue(headData, "MATERIAL", data.get("MATERIAL"));
            setStructureValue(headData, "IND_SECTOR", data.get("IND_SECTOR"));
            setStructureValue(headData, "MATL_TYPE", data.get("MATL_TYPE"));
            setStructureValue(headData, "BASIC_VIEW", "X");
        } catch (Exception e) {
            logger.error("设置HEADDATA失败: {}", e.getMessage());
        }

        // 设置CLIENTDATA (BAPI_MARA)
        try {
            JCoStructure clientData = importParams.getStructure("CLIENTDATA");
            // 字段映射: MEINS -> BASE_UOM, MATKL -> MATL_GROUP
            setStructureValue(clientData, "BASE_UOM", data.get("BASE_UOM"));
            setStructureValue(clientData, "MATL_GROUP", data.get("MATL_GROUP"));
        } catch (Exception e) {
            logger.error("设置CLIENTDATA失败: {}", e.getMessage());
        }

        // 设置CLIENTDATAX - 标记哪些字段需要更新
        try {
            JCoStructure clientDataX = importParams.getStructure("CLIENTDATAX");
            // 创建时标记BASE_UOM，更新时不标记（SAP不允许修改已存在物料的基本单位）
            if (isCreate) {
                setStructureValue(clientDataX, "BASE_UOM", "X");
                logger.info("创建物料，标记BASE_UOM");
            } else {
                logger.info("更新物料，不标记BASE_UOM");
            }
            setStructureValue(clientDataX, "MATL_GROUP", "X");
        } catch (Exception e) {
            logger.error("设置CLIENTDATAX失败: {}", e.getMessage());
        }

        // 设置MATERIALDESCRIPTION表
        try {
            JCoTable matDesc = function.getTableParameterList().getTable("MATERIALDESCRIPTION");
            matDesc.appendRow();
            matDesc.setValue("LANGU", "ZH");
            // 字段映射: MAKTX -> MATL_DESC
            matDesc.setValue("MATL_DESC", data.get("MATL_DESC"));
        } catch (Exception e) {
            logger.error("设置MATERIALDESCRIPTION失败: {}", e.getMessage());
        }
    }

    /**
     * 检查物料是否在SAP中存在
     * 简单实现：尝试读取物料，如果成功则存在
     */
    private boolean materialExistsInSAP(String materialNo) {
        try {
            JCoFunction function = destination.getRepository().getFunction("BAPI_MATERIAL_GET_DETAIL");

            JCoParameterList importParams = function.getImportParameterList();
            importParams.setValue("MATERIAL", materialNo);

            function.execute(destination);

            // 检查返回消息
            JCoStructure returnStruct = function.getExportParameterList().getStructure("RETURN");
            String messageType = returnStruct.getString("TYPE");

            // 如果没有错误，说明物料存在
            return !"E".equals(messageType) && !"A".equals(messageType);
        } catch (Exception e) {
            // 如果出错，通常表示物料不存在
            logger.debug("检查物料存在性失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 设置结构值
     */
    private void setStructureValue(JCoStructure structure, String key, Object value) {
        if (value != null && structure != null) {
            try {
                structure.setValue(key, value.toString());
            } catch (Exception e) {
                logger.debug("设置结构值失败: {} = {}", key, value);
            }
        }
    }

    /**
     * 设置表值
     */
    private void setTableValue(JCoTable table, String key, Object value) {
        if (value != null && table != null) {
            try {
                table.setValue(key, value.toString());
            } catch (Exception e) {
                logger.debug("设置表值失败: {} = {}", key, value);
            }
        }
    }

    /**
     * 通用参数设置
     */
    private void setParameters(JCoParameterList params, Map<String, Object> data) {
        if (params == null || data == null) return;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            try {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value != null && !key.startsWith("_")) {
                    params.setValue(key, value.toString());
                }
            } catch (Exception e) {
                logger.debug("设置参数失败: {} - {}", entry.getKey(), e.getMessage());
            }
        }
    }

    /**
     * 解析返回结果
     */
    private DistributionResultDTO parseResult(JCoFunction function, String dataType) {
        DistributionResultDTO result = new DistributionResultDTO();
        JCoParameterList exportParams = function.getExportParameterList();

        // 保存所有导出参数到responseData
        Map<String, Object> responseData = exportParamsToMap(exportParams);
        result.setResponseData(responseData);

        if (exportParams != null) {
            try {
                // 尝试获取RETURN结构
                JCoStructure returnStruct = null;
                String[] returnNames = {"RETURN", "RETURNINFO", "ET_RETURN"};
                for (String returnName : returnNames) {
                    try {
                        returnStruct = exportParams.getStructure(returnName);
                        break;
                    } catch (Exception e) {
                        // 继续尝试
                    }
                }

                if (returnStruct != null) {
                    // 获取RETURN结构中的字段
                    String type = "";
                    String message = "";
                    String code = "";
                    String msgV1 = "";
                    String msgV2 = "";
                    String msgV3 = "";
                    String msgV4 = "";

                    try { type = returnStruct.getString("TYPE"); } catch (Exception ignored) {}
                    try { message = returnStruct.getString("MESSAGE"); } catch (Exception ignored) {}
                    try { code = returnStruct.getString("ID"); } catch (Exception ignored) {}  // 使用ID字段作为消息类
                    try { msgV1 = returnStruct.getString("MESSAGE_V1"); } catch (Exception ignored) {}
                    try { msgV2 = returnStruct.getString("MESSAGE_V2"); } catch (Exception ignored) {}
                    try { msgV3 = returnStruct.getString("MESSAGE_V3"); } catch (Exception ignored) {}
                    try { msgV4 = returnStruct.getString("MESSAGE_V4"); } catch (Exception ignored) {}

                    result.setSapMessageType(type);
                    result.setSapReturnCode(code);

                    // 构建完整消息
                    StringBuilder fullMessage = new StringBuilder();
                    if (message != null && !message.isEmpty()) {
                        fullMessage.append(message);
                    }
                    if (msgV1 != null && !msgV1.isEmpty()) {
                        fullMessage.append(" [").append(msgV1);
                        if (msgV2 != null && !msgV2.isEmpty()) fullMessage.append(", ").append(msgV2);
                        if (msgV3 != null && !msgV3.isEmpty()) fullMessage.append(", ").append(msgV3);
                        if (msgV4 != null && !msgV4.isEmpty()) fullMessage.append(", ").append(msgV4);
                        fullMessage.append("]");
                    }

                    result.setMessage(fullMessage.toString());

                    // 记录关键日志
                    logger.info("SAP返回: TYPE={}, ID={}, MESSAGE={}", type, code, fullMessage);

                    // S=成功, E=错误, W=警告, I=信息, A=终止
                    if ("S".equals(type) || "W".equals(type) || "I".equals(type)) {
                        result.setSuccess(true);
                    } else if ("E".equals(type) || "A".equals(type)) {
                        result.setSuccess(false);
                        result.setErrorMsg(fullMessage.toString());
                    } else if (type == null || type.isEmpty()) {
                        // TYPE为空时，检查是否有错误消息
                        if (message != null && !message.isEmpty()) {
                            result.setSuccess(false);
                            result.setErrorMsg(message);
                        } else {
                            result.setSuccess(true);
                            result.setMessage("执行成功(无返回类型)");
                        }
                    } else {
                        // 其他类型，保守处理为失败
                        result.setSuccess(false);
                        result.setErrorMsg("未知返回类型: " + type + " - " + fullMessage);
                    }
                } else {
                    // 没有RETURN结构，检查是否有其他返回信息
                    logger.info("未找到RETURN结构，检查其他返回参数");

                    // 尝试从responseData中查找错误信息
                    boolean hasError = false;
                    for (Map.Entry<String, Object> entry : responseData.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        logger.info("导出参数: {} = {}", key, value);

                        // 检查是否包含错误标识
                        if (value != null) {
                            String strValue = value.toString();
                            if (strValue.contains("error") || strValue.contains("Error") || strValue.contains("ERROR")) {
                                hasError = true;
                                result.setErrorMsg(strValue);
                            }
                        }
                    }

                    if (!hasError) {
                        result.setSuccess(true);
                        result.setMessage("执行成功(无RETURN结构)");
                    } else {
                        result.setSuccess(false);
                    }
                }

                // 获取创建的主键
                BapiConfig bapiConfig = BAPI_CONFIGS.get(dataType);
                if (bapiConfig != null) {
                    for (String keyField : bapiConfig.returnKeyFields) {
                        try {
                            String keyValue = exportParams.getString(keyField);
                            if (keyValue != null && !keyValue.isEmpty()) {
                                result.setTargetKey(keyValue);
                                logger.info("获取到SAP主键: {} = {}", keyField, keyValue);
                                break;
                            }
                        } catch (Exception ignored) {}
                    }
                }

            } catch (Exception e) {
                logger.error("解析SAP返回结果异常: {}", e.getMessage(), e);
                result.setSuccess(false);
                result.setErrorMsg("解析返回结果异常: " + e.getMessage());
            }
        } else {
            // 没有导出参数
            logger.warn("SAP没有返回任何导出参数");
            result.setSuccess(false);
            result.setErrorMsg("SAP没有返回任何数据");
        }

        return result;
    }

    /**
     * 将导出参数转换为Map
     */
    private Map<String, Object> exportParamsToMap(JCoParameterList exportParams) {
        Map<String, Object> map = new HashMap<>();
        if (exportParams == null) return map;

        try {
            for (JCoFieldIterator it = exportParams.getFieldIterator(); it.hasNextField(); ) {
                JCoField field = it.nextField();
                map.put(field.getName(), convertJCoValue(field));
            }
        } catch (Exception e) {
            logger.debug("转换导出参数失败: {}", e.getMessage());
        }

        return map;
    }

    /**
     * 将JCo值转换为可序列化的Java对象
     */
    private Object convertJCoValue(JCoField field) {
        try {
            if (field.isStructure()) {
                return structureToMap(field.getStructure());
            } else if (field.isTable()) {
                return tableToList(field.getTable());
            } else {
                return field.getValue();
            }
        } catch (Exception e) {
            logger.debug("转换字段值失败: {} - {}", field.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * 将JCoStructure转换为Map
     */
    private Map<String, Object> structureToMap(JCoStructure structure) {
        Map<String, Object> map = new HashMap<>();
        try {
            for (JCoFieldIterator it = structure.getFieldIterator(); it.hasNextField(); ) {
                JCoField field = it.nextField();
                map.put(field.getName(), convertJCoValue(field));
            }
        } catch (Exception e) {
            logger.debug("转换结构失败: {}", e.getMessage());
        }
        return map;
    }

    /**
     * 将JCoTable转换为List
     */
    private List<Map<String, Object>> tableToList(JCoTable table) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            for (int i = 0; i < table.getNumRows(); i++) {
                table.setRow(i);
                Map<String, Object> row = new HashMap<>();
                for (JCoFieldIterator it = table.getFieldIterator(); it.hasNextField(); ) {
                    JCoField field = it.nextField();
                    row.put(field.getName(), convertJCoValue(field));
                }
                list.add(row);
            }
        } catch (Exception e) {
            logger.debug("转换表失败: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public DisSystemConfig getConfig() {
        return config;
    }

    @Override
    public String getSystemType() {
        return "SAP";
    }

    /**
     * 获取JCo目标连接（供外部调用）
     */
    public JCoDestination getJCoDestination() {
        return destination;
    }

    private String getString(String key) {
        Object value = connectionConfig.get(key);
        return value != null ? value.toString() : "";
    }

    private String getString(String key, String defaultValue) {
        Object value = connectionConfig.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private String getStringAny(String... keys) {
        for (String key : keys) {
            Object value = connectionConfig.get(key);
            if (value != null && !value.toString().isEmpty()) {
                return value.toString();
            }
        }
        return keys.length > 0 ? keys[keys.length - 1] : "";
    }

    /**
     * BAPI配置类
     */
    private static class BapiConfig {
        String createBapi;
        String updateBapi;
        String deleteBapi;
        String keyField;
        String[] returnKeyFields;

        BapiConfig(String createBapi, String updateBapi, String deleteBapi, String keyField, String[] returnKeyFields) {
            this.createBapi = createBapi;
            this.updateBapi = updateBapi;
            this.deleteBapi = deleteBapi;
            this.keyField = keyField;
            this.returnKeyFields = returnKeyFields;
        }
    }

    /**
     * 简单的目标数据提供器实现
     */
    private static class SimpleDestinationDataProvider implements DestinationDataProvider {
        private final Map<String, Properties> destinations = new ConcurrentHashMap<>();
        private DestinationDataEventListener listener;

        @Override
        public Properties getDestinationProperties(String destName) {
            return destinations.get(destName);
        }

        @Override
        public void setDestinationDataEventListener(DestinationDataEventListener listener) {
            this.listener = listener;
        }

        @Override
        public boolean supportsEvents() {
            return false;
        }

        public void addDestination(String destName, Properties props) {
            destinations.put(destName, props);
        }
    }
}
