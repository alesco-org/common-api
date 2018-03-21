package org.alesco.api;

public class LocalCache {

    private Object handle;

    public LocalCache() {
    }

    public LocalCache(Object handle) {
        this.handle = handle;
    }

    public void save(Object o) {
        handle = o;
    }

    public Object get() {
        return handle;
    }

}
