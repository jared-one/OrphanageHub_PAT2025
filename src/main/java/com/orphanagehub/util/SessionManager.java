package com.orphanagehub.util;

import io.vavr.control.Option;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static SessionManager instance;
    private Map<String, Object> attributes = new HashMap<>();
    
    private SessionManager() {}
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    public Option<Object> getAttribute(String key) {
        return Option.of(attributes.get(key));
    }
    
    public void clear() {
        attributes.clear();
    }
}
