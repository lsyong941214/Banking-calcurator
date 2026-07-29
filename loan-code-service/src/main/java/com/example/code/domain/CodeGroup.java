package com.example.code.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "code_group")
public class CodeGroup {

    @Id
    private String groupCode;

    private String groupName;

    protected CodeGroup() {
    }

    public String getGroupCode() {
        return groupCode;
    }

    public String getGroupName() {
        return groupName;
    }
}
