package com.laydown.srouter.api.model;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class RouterMeta implements Serializable {

    private String clazzName;

    private String path;

    private Integer id;

    private String destType;

    public String getClazzName() {
        return this.clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDestType() {
        return this.destType;
    }

    public void setDestType(String destType) {
        this.destType = destType;
    }
}
