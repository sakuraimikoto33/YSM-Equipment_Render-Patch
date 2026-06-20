package net.okitsu.ysmequipmentrenderpatch.runtime;

import org.objectweb.asm.Type;

import java.lang.reflect.Method;

public final class Reflector {
    private Reflector() {
    }

    public static Method findMethod(YsmRuntimeSymbols.MethodRef methodRef, ClassLoader classLoader) {
        if (methodRef == null) {
            return null;
        }

        try {
            Class<?> owner = Class.forName(methodRef.ownerClassName(), false, classLoader);
            Method method = findMethod(owner.getDeclaredMethods(), methodRef);
            if (method == null) {
                method = findMethod(owner.getMethods(), methodRef);
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

    private static Method findMethod(Method[] methods, YsmRuntimeSymbols.MethodRef methodRef) {
        for (Method method : methods) {
            if (method.getName().equals(methodRef.name)
                    && Type.getMethodDescriptor(method).equals(methodRef.descriptor)) {
                return method;
            }
        }
        return null;
    }
}
