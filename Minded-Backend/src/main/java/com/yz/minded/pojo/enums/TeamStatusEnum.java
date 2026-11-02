package com.yz.minded.pojo.enums;

/**
 * 队伍状态枚举类
 */
public enum TeamStatusEnum {
    
    PUBLIC("公开", 0),
    SECRET("加密", 1),
    PRIVATE("私有",2);

    /**
     * 枚举值
     */
    private int value;
    
    /**
     * 枚举信息
     */
    private String text;
    
    public static TeamStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (TeamStatusEnum teamStatusEnum : TeamStatusEnum.values()) {
            if (teamStatusEnum.getValue() == value) {
                return teamStatusEnum;
            }
        }

        return null;
    }

    TeamStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }
    

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
