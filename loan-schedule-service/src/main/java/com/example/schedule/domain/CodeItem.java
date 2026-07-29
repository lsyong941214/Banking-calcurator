package com.example.schedule.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "code_item")
@IdClass(CodeItemId.class)
public class CodeItem {

    @Id
    private String groupCode;

    @Id
    private String code;

    private String codeName;

    private int sortOrder;

    protected CodeItem() {
    }

    public String getGroupCode() {
        return groupCode;
    }

    public String getCode() {
        return code;
    }

    public String getCodeName() {
        return codeName;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
