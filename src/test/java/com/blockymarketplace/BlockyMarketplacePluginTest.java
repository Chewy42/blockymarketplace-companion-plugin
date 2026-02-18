package com.blockymarketplace;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class BlockyMarketplacePluginTest {

    @Test
    void pluginClassHasRequiredMethods() throws ClassNotFoundException {
        Class<?> pluginClass = Class.forName("com.blockymarketplace.BlockyMarketplacePlugin", false, getClass().getClassLoader());

        assertNotNull(pluginClass);

        boolean hasSetup = false;
        boolean hasStart = false;
        boolean hasShutdown = false;

        for (Method method : pluginClass.getDeclaredMethods()) {
            if ("setup".equals(method.getName())) hasSetup = true;
            if ("start".equals(method.getName())) hasStart = true;
            if ("shutdown".equals(method.getName())) hasShutdown = true;
        }

        assertTrue(hasSetup, "Plugin should have setup method");
        assertTrue(hasStart, "Plugin should have start method");
        assertTrue(hasShutdown, "Plugin should have shutdown method");
    }

    @Test
    void pluginClassHasGetConvexClientMethod() throws ClassNotFoundException {
        Class<?> pluginClass = Class.forName("com.blockymarketplace.BlockyMarketplacePlugin", false, getClass().getClassLoader());

        boolean hasMethod = false;
        for (Method method : pluginClass.getDeclaredMethods()) {
            if ("getConvexClient".equals(method.getName())) {
                hasMethod = true;
                break;
            }
        }

        assertTrue(hasMethod, "Plugin should have getConvexClient method");
    }

    @Test
    void pluginClassHasGetConfigMethod() throws ClassNotFoundException {
        Class<?> pluginClass = Class.forName("com.blockymarketplace.BlockyMarketplacePlugin", false, getClass().getClassLoader());

        boolean hasMethod = false;
        for (Method method : pluginClass.getDeclaredMethods()) {
            if ("getConfig".equals(method.getName())) {
                hasMethod = true;
                break;
            }
        }

        assertTrue(hasMethod, "Plugin should have getConfig method");
    }
}
