package org.iotf.wrapper.permissionHandle;

public enum PermissionType {

    DEFAULT("DEFAULT", "默认权限"),

    ADMIN("ADMIN", "系统管理员");

    private final String code;
    private final String description;

    PermissionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }
}