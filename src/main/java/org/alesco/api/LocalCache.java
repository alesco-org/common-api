package org.alesco.api;

public class LocalCache {

    private Object handle;

    public void save(Object o) {
        handle = o;
    }

    public Object get() {
        return handle;
    }

}
