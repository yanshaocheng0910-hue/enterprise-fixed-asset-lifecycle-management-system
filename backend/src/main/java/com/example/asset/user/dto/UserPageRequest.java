package com.example.asset.user.dto;

import lombok.Data;

@Data
public class UserPageRequest {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String username;
    private String realName;
    private Integer status;
}
