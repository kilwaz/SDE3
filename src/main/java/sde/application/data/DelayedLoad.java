package sde.application.data;

import java.lang.reflect.Method;

public class DelayedLoad {
    private String uuid;
    private Class clazz;
    private Method method;

    public DelayedLoad(String uuid, Class clazz, Method method) {
        this.uuid = uuid;
        this.clazz = clazz;
        this.method = method;
    }

    public String getUuid() {
        return uuid;
    }

    public Class getClazz() {
        return clazz;
    }

    public Method getMethod() {
        return method;
    }
}
