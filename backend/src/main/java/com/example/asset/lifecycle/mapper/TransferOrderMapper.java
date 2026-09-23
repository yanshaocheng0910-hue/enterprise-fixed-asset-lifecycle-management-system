package com.example.asset.lifecycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.asset.lifecycle.entity.TransferOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TransferOrderMapper extends BaseMapper<TransferOrder> {

    @Select("SELECT MAX(order_code) FROM asset_transfer_order WHERE order_code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxOrderCodeByPrefix(@Param("prefix") String prefix);
}
