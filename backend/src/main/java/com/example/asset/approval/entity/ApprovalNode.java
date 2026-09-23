package com.example.asset.approval.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;



@Data
@TableName("approval_node")
public class ApprovalNode {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long flowId;
    private String nodeCode;
    private String nodeName;
    private String approverRole;
    private Integer sortOrder;
    private Integer required;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
