package com.example.asset.lifecycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.asset.lifecycle.entity.ReceiveOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReceiveOrderMapper extends BaseMapper<ReceiveOrder> {

    @Select("SELECT MAX(order_code) FROM asset_receive_order WHERE order_code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxOrderCodeByPrefix(@Param("prefix") String prefix);
}
