package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface WorkspaceMapper {

    /**
     * 根据条件统计营业额
     * @param map
     * @return
     */
    Double sumTurnover(Map map);

    /**
     * 根据条件统计订单数量
     * @param map
     * @return
     */
    Integer countOrders(Map map);

    /**
     * 根据条件统计用户数量
     * @param map
     * @return
     */
    Integer countUsers(Map map);

    /**
     * 根据状态统计菜品数量
     * @param status
     * @return
     */
    @Select("select count(id) from dish where status = #{status}")
    Integer countDishByStatus(Integer status);

    /**
     * 根据状态统计套餐数量
     * @param status
     * @return
     */
    @Select("select count(id) from setmeal where status = #{status}")
    Integer countSetmealByStatus(Integer status);
}
