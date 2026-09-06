package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 分页条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 根据条件统计订单数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);
}
