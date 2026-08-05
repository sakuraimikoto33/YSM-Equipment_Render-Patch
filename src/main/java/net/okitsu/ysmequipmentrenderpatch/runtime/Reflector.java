package net.okitsu.ysmequipmentrenderpatch.runtime;

import net.okitsu.ysmmapping.api.YsmMethodSymbol;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

public final class Reflector {
    private Reflector() {
    }

    public static Method findMethod(YsmMethodSymbol symbol, ClassLoader classLoader) {
        if (symbol == null) {
            return null;
        }

        try {
            Class<?> owner = Class.forName(symbol.owner().replace('/', '.'), false, classLoader);
            Method method = findMethod(owner.getDeclaredMethods(), symbol);
            if (method == null) {
                method = findMethod(owner.getMethods(), symbol);
            }
            if (method != null) {
                method.setAccessible(true);
            }
            return method;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            return null;
        }
    }

    public static Object invoke(Method method, Object target, Object... arguments) {
        if (method == null) {
            return null;
        }

        try {
            return method.invoke(target, arguments);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            return null;
        }
    }

    private static Method findMethod(Method[] methods, YsmMethodSymbol symbol) {
        for (Method method : methods) {
            if (method.getName().equals(symbol.name())
                    && Type.getMethodDescriptor(method).equals(symbol.descriptor())) {
                return method;
            }
        }
        return null;
    }
}
