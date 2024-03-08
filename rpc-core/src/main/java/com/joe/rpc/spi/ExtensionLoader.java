package com.joe.rpc.spi;

import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ExtensionLoader {
    private static String EXTENSION_DIR_PREFIX = "META-INF/rpc/";

    // 实例化的bean
    private static Map<String, Object> singletonsObject = new ConcurrentHashMap<>();
    private static Set<Class> loadedClazz = new ConcurrentHashSet<>();

    private static ExtensionLoader extensionLoader = new ExtensionLoader();

    public static ExtensionLoader getInstance() {
        return extensionLoader;
    }

    public <V> V get(String name) {
        return (V) singletonsObject.get(name);
    }

    public void loadExtension(Class clazz) throws Exception {
        if (clazz == null) {
            throw new IllegalArgumentException("class 没找到");
        }
        if (loadedClazz.contains(clazz)) {
            return;
        } else {
            loadedClazz.add(clazz);
        }
        ClassLoader classLoader = this.getClass().getClassLoader();
        String clazzName = clazz.getName();
        // 从系统SPI以及用户SPI中找bean
        String spiFilePath = EXTENSION_DIR_PREFIX + clazzName;
        Enumeration<URL> enumeration = classLoader.getResources(spiFilePath);
        while (enumeration.hasMoreElements()) {
            URL url = enumeration.nextElement();
            InputStreamReader inputStreamReader = null;
            inputStreamReader = new InputStreamReader(url.openStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineArr = line.split("=");
                String key = lineArr[0];
                String name = lineArr[1];
                final Class<?> aClass = Class.forName(name);
                log.info("加载bean key: {} , value: {}", key, name);
                singletonsObject.put(clazzName + ":" + key, aClass.newInstance());
            }
        }
    }
}
