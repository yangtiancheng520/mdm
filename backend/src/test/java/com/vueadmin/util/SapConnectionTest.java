package com.vueadmin.util;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SAP JCo连接测试工具
 */
public class SapConnectionTest {

    private static final String DEST_NAME = "SAP_TEST";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("SAP JCo 连接测试");
        System.out.println("========================================\n");

        // 测试配置 - MZ-QAS
        SapConfig config = new SapConfig();
        config.name = "MZ-QAS";
        config.ashost = "172.30.2.32";
        config.sysnr = "00";
        config.client = "500";
        config.user = "yangtc";
        config.passwd = "19830101";
        config.lang = "ZH";

        try {
            // 1. 检查JCo库是否加载
            System.out.println("1. 检查JCo库...");
            checkJCoLibrary();
            System.out.println("   ✓ JCo库加载成功\n");

            // 2. 注册Destination Provider
            System.out.println("2. 注册Destination Provider...");
            registerDestinationProvider(config);
            System.out.println("   ✓ 注册成功\n");

            // 3. 获取连接
            System.out.println("3. 获取SAP连接...");
            JCoDestination destination = JCoDestinationManager.getDestination(DEST_NAME);
            System.out.println("   ✓ 连接对象获取成功\n");

            // 4. 测试ping
            System.out.println("4. 测试连接 (ping)...");
            destination.ping();
            System.out.println("   ✓ SAP连接成功!\n");

            // 5. 显示系统信息
            System.out.println("5. 系统信息:");
            System.out.println("   系统ID: " + destination.getAttributes().getSystemID());
            System.out.println("   系统编号: " + destination.getAttributes().getSystemNumber());
            System.out.println("   客户端: " + destination.getAttributes().getClient());
            System.out.println("   用户: " + destination.getAttributes().getUser());
            System.out.println("   主机: " + destination.getAttributes().getPartnerHost());
            System.out.println("   系统类型: " + destination.getAttributes().getPartnerType());
            System.out.println();

            System.out.println("========================================");
            System.out.println("测试完成 - 所有检查通过!");
            System.out.println("========================================");

        } catch (JCoException e) {
            System.err.println("\n✗ JCo错误: " + e.getMessage());
            System.err.println("   错误码: " + e.getGroup());
            System.err.println("   详情: " + e.getKey());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n✗ 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 检查JCo库
     */
    private static void checkJCoLibrary() throws Exception {
        try {
            Class.forName("com.sap.conn.jco.JCo");
        } catch (ClassNotFoundException e) {
            throw new Exception("找不到JCo库，请确保sapjco3.jar在classpath中");
        }
    }

    /**
     * 注册Destination Provider
     */
    private static void registerDestinationProvider(SapConfig config) throws JCoException {
        SimpleDestinationDataProvider provider = new SimpleDestinationDataProvider();

        Properties props = new Properties();
        props.setProperty(DestinationDataProvider.JCO_ASHOST, config.ashost);
        props.setProperty(DestinationDataProvider.JCO_SYSNR, config.sysnr);
        props.setProperty(DestinationDataProvider.JCO_CLIENT, config.client);
        props.setProperty(DestinationDataProvider.JCO_USER, config.user);
        props.setProperty(DestinationDataProvider.JCO_PASSWD, config.passwd);
        props.setProperty(DestinationDataProvider.JCO_LANG, config.lang);
        // 连接池配置
        props.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, "3");
        props.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, "10");

        provider.addDestination(DEST_NAME, props);

        // 注意：在JCo 3.0中，不需要显式注册provider
        // 直接通过getDestination获取时会自动使用默认的文件provider
        // 我们需要使用Environment来注册自定义provider
        try {
            com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(provider);
        } catch (IllegalStateException e) {
            // 已经注册过了，忽略
            System.out.println("   (Provider已注册，跳过)");
        }
    }

    /**
     * SAP配置
     */
    static class SapConfig {
        String name;
        String ashost;
        String sysnr;
        String client;
        String user;
        String passwd;
        String lang;
    }

    /**
     * 简单的Destination Provider实现
     */
    static class SimpleDestinationDataProvider implements DestinationDataProvider {
        private final ConcurrentHashMap<String, Properties> destinations = new ConcurrentHashMap<>();
        private DestinationDataEventListener listener;

        @Override
        public Properties getDestinationProperties(String destName) {
            if (destinations.containsKey(destName)) {
                return destinations.get(destName);
            }
            return null;
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
