package com.vueadmin.controller.distribution;

import com.sap.conn.jco.*;
import com.vueadmin.dto.ApiResponse;
import com.vueadmin.entity.distribution.DisSystemConfig;
import com.vueadmin.repository.DisSystemConfigRepository;
import com.vueadmin.service.distribution.connector.ConnectorFactory;
import com.vueadmin.service.distribution.connector.SapConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * SAP BAPI信息查询Controller
 */
@RestController
@RequestMapping("/api/sap")
public class SapBapiController {

    @Autowired
    private DisSystemConfigRepository systemConfigRepository;

    @Autowired
    private ConnectorFactory connectorFactory;

    /**
     * 获取BAPI函数的参数信息
     */
    @GetMapping("/bapi-info")
    public ApiResponse<Map<String, Object>> getBapiInfo(
            @RequestParam String bapiName,
            @RequestParam(defaultValue = "7") Long configId) {

        Map<String, Object> result = new HashMap<>();

        try {
            DisSystemConfig config = systemConfigRepository.findById(configId)
                    .orElseThrow(() -> new RuntimeException("配置不存在"));

            // 获取SAP连接
            JCoDestination destination = getDestination(config);
            JCoRepository repository = destination.getRepository();

            // 获取函数
            JCoFunction function = repository.getFunction(bapiName);
            if (function == null) {
                return ApiResponse.error(404, "BAPI函数不存在: " + bapiName);
            }

            result.put("bapiName", bapiName);

            // 导入参数
            List<Map<String, Object>> importParams = new ArrayList<>();
            JCoParameterList importList = function.getImportParameterList();
            if (importList != null) {
                for (JCoFieldIterator it = importList.getFieldIterator(); it.hasNextField(); ) {
                    JCoField field = it.nextField();
                    Map<String, Object> param = new HashMap<>();
                    param.put("name", field.getName());
                    param.put("type", field.getTypeAsString());
                    param.put("description", field.getDescription());
                    importParams.add(param);
                }
            }
            result.put("importParameters", importParams);

            // 导出参数
            List<Map<String, Object>> exportParams = new ArrayList<>();
            JCoParameterList exportList = function.getExportParameterList();
            if (exportList != null) {
                for (JCoFieldIterator it = exportList.getFieldIterator(); it.hasNextField(); ) {
                    JCoField field = it.nextField();
                    Map<String, Object> param = new HashMap<>();
                    param.put("name", field.getName());
                    param.put("type", field.getTypeAsString());
                    param.put("description", field.getDescription());
                    exportParams.add(param);
                }
            }
            result.put("exportParameters", exportParams);

            // 表参数
            List<Map<String, Object>> tableParams = new ArrayList<>();
            JCoParameterList tableList = function.getTableParameterList();
            if (tableList != null) {
                for (JCoFieldIterator it = tableList.getFieldIterator(); it.hasNextField(); ) {
                    JCoField field = it.nextField();
                    Map<String, Object> param = new HashMap<>();
                    param.put("name", field.getName());
                    param.put("type", field.getTypeAsString());
                    param.put("description", field.getDescription());
                    tableParams.add(param);
                }
            }
            result.put("tableParameters", tableParams);

            return ApiResponse.success(result);

        } catch (Exception e) {
            return ApiResponse.error(500, "获取BAPI信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取BAPI结构的详细字段
     */
    @GetMapping("/bapi-structure")
    public ApiResponse<Map<String, Object>> getBapiStructure(
            @RequestParam String bapiName,
            @RequestParam String paramName,
            @RequestParam(defaultValue = "7") Long configId) {

        Map<String, Object> result = new HashMap<>();

        try {
            DisSystemConfig config = systemConfigRepository.findById(configId)
                    .orElseThrow(() -> new RuntimeException("配置不存在"));

            JCoDestination destination = getDestination(config);
            JCoRepository repository = destination.getRepository();
            JCoFunction function = repository.getFunction(bapiName);

            if (function == null) {
                return ApiResponse.error(404, "BAPI函数不存在: " + bapiName);
            }

            // 尝试从导入参数获取结构
            JCoParameterList importList = function.getImportParameterList();
            if (importList != null) {
                try {
                    JCoStructure structure = importList.getStructure(paramName);
                    List<Map<String, Object>> fields = new ArrayList<>();
                    for (JCoFieldIterator it = structure.getFieldIterator(); it.hasNextField(); ) {
                        JCoField field = it.nextField();
                        Map<String, Object> fieldInfo = new HashMap<>();
                        fieldInfo.put("name", field.getName());
                        fieldInfo.put("type", field.getTypeAsString());
                        fieldInfo.put("description", field.getDescription());
                        fields.add(fieldInfo);
                    }
                    result.put("structureName", paramName);
                    result.put("fields", fields);
                    return ApiResponse.success(result);
                } catch (Exception ignored) {
                }
            }

            // 尝试从表参数获取
            JCoParameterList tableList = function.getTableParameterList();
            if (tableList != null) {
                try {
                    JCoTable table = tableList.getTable(paramName);
                    List<Map<String, Object>> fields = new ArrayList<>();
                    for (JCoFieldIterator it = table.getFieldIterator(); it.hasNextField(); ) {
                        JCoField field = it.nextField();
                        Map<String, Object> fieldInfo = new HashMap<>();
                        fieldInfo.put("name", field.getName());
                        fieldInfo.put("type", field.getTypeAsString());
                        fieldInfo.put("description", field.getDescription());
                        fields.add(fieldInfo);
                    }
                    result.put("tableName", paramName);
                    result.put("fields", fields);
                    return ApiResponse.success(result);
                } catch (Exception ignored) {
                }
            }

            return ApiResponse.error(404, "参数结构不存在: " + paramName);

        } catch (Exception e) {
            return ApiResponse.error(500, "获取结构信息失败: " + e.getMessage());
        }
    }

    /**
     * 搜索BAPI函数
     */
    @GetMapping("/search-bapi")
    public ApiResponse<List<Map<String, Object>>> searchBapi(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "7") Long configId) {

        try {
            DisSystemConfig config = systemConfigRepository.findById(configId)
                    .orElseThrow(() -> new RuntimeException("配置不存在"));

            JCoDestination destination = getDestination(config);

            // 使用RFC_FUNCTION_SEARCH搜索函数
            JCoFunction function = destination.getRepository().getFunction("RFC_FUNCTION_SEARCH");
            if (function == null) {
                return ApiResponse.error(404, "RFC_FUNCTION_SEARCH不可用");
            }

            function.getImportParameterList().setValue("FUNCNAME", keyword.toUpperCase() + "*");
            function.execute(destination);

            JCoTable results = function.getTableParameterList().getTable("FUNCTIONS");
            List<Map<String, Object>> functions = new ArrayList<>();

            for (int i = 0; i < results.getNumRows() && i < 50; i++) {
                results.setRow(i);
                Map<String, Object> func = new HashMap<>();
                func.put("name", results.getString("FUNCNAME"));
                func.put("group", results.getString("GROUPNAME"));
                functions.add(func);
            }

            return ApiResponse.success(functions);

        } catch (Exception e) {
            return ApiResponse.error(500, "搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取SAP目标
     */
    private JCoDestination getDestination(DisSystemConfig config) throws JCoException {
        // 使用ConnectorFactory获取已初始化的SapConnector
        SapConnector sapConnector = (SapConnector) connectorFactory.getConnector(config);
        return sapConnector.getJCoDestination();
    }
}
