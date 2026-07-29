package com.example.schedule.domain;

import java.io.Serializable;
import java.util.Objects;

public class CodeItemId implements Serializable {

    private String groupCode;
    private String code;

    public CodeItemId() {
    }

    public CodeItemId(String groupCode, String code) {
        this.groupCode = groupCode;
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodeItemId that)) return false;
        return Objects.equals(groupCode, that.groupCode) && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupCode, code);
    }
}
