package com.yz.minded.pojo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserTagsDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2338099764892094539L;

    private String tags;
}
