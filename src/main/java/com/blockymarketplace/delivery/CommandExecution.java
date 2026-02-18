package com.blockymarketplace.delivery;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CommandExecution {
    private static final Logger LOGGER = Logger.getLogger(CommandExecution.class.getName());

    private CommandExecution() {}

    public static boolean executeAsConsole(String command) {
        String sanitized = command == null ? null : command.trim();
        if (sanitized == null || sanitized.isEmpty()) {
            return false;
        }

        if (sanitized.startsWith("/")) {
            sanitized = sanitized.substring(1);
        }

        try {
            Class<?> managerClass = resolveClass(new String[] {
                    "com.hypixel.hytale.server.core.command.system.CommandManager",
                    "com.hypixel.hytale.server.core.command.CommandManager"
            });
            if (managerClass == null) {
                LOGGER.log(Level.WARNING, "CommandManager class not found");
                return false;
            }

            Method getMethod = managerClass.getMethod("get");
            Object manager = getMethod.invoke(null);

            Class<?> consoleClass = resolveClass(new String[] {
                    "com.hypixel.hytale.server.core.command.system.sender.ConsoleSender",
                    "com.hypixel.hytale.server.core.command.system.ConsoleSender"
            });
            if (consoleClass == null) {
                LOGGER.log(Level.WARNING, "ConsoleSender class not found");
                return false;
            }

            Object console = consoleClass.getField("INSTANCE").get(null);

            Method handler = null;
            for (Method method : managerClass.getMethods()) {
                if (!method.getName().equals("handleCommand")) {
                    continue;
                }
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 2 && params[1] == String.class) {
                    handler = method;
                    break;
                }
            }

            if (handler == null) {
                LOGGER.log(Level.WARNING, "CommandManager handleCommand method not found");
                return false;
            }

            handler.invoke(manager, console, sanitized);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to execute command: " + sanitized, e);
            return false;
        }
    }

    private static Class<?> resolveClass(String[] candidates) {
        for (String candidate : candidates) {
            try {
                return Class.forName(candidate);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }
}
