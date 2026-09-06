package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.WorkspaceMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private WorkspaceMapper workspaceMapper;

    /**
     * 查询今日运营数据
     * @return
     */
    public BusinessDataVO getBusinessData() {
        //查询今天的开始时间和结束时间
        LocalDateTime begin = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);

        //查询今日新增用户数
        Integer newUser = workspaceMapper.countUsers(map);

        //查询今日总订单数
        Integer totalOrderCount = workspaceMapper.countOrders(map);

        //查询今日有效订单数（订单状态为已完成）
        map.put("status", Orders.COMPLETED);
        Integer validOrderCount = workspaceMapper.countOrders(map);

        //查询今日营业额
        Double turnover = workspaceMapper.sumTurnover(map);
        turnover = turnover == null ? 0.0 : turnover;

        //订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0 && validOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        //平均客单价 = 营业额 / 有效订单数
        Double unitPrice = 0.0;
        if (validOrderCount != 0) {
            unitPrice = turnover / validOrderCount;
        }

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUser)
                .build();
    }

    /**
     * 查询套餐总览
     * @return
     */
    public SetmealOverViewVO getOverviewSetmeals() {
        Integer sold = workspaceMapper.countSetmealByStatus(StatusConstant.ENABLE);
        Integer discontinued = workspaceMapper.countSetmealByStatus(StatusConstant.DISABLE);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询菜品总览
     * @return
     */
    public DishOverViewVO getOverviewDishes() {
        Integer sold = workspaceMapper.countDishByStatus(StatusConstant.ENABLE);
        Integer discontinued = workspaceMapper.countDishByStatus(StatusConstant.DISABLE);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询订单管理数据
     * @return
     */
    public OrderOverViewVO getOverviewOrders() {
        //查询今天的开始时间和结束时间
        LocalDateTime begin = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);

        //全部订单
        Integer allOrders = workspaceMapper.countOrders(map);

        //已取消订单
        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = workspaceMapper.countOrders(map);

        //已完成订单
        map.put("status", Orders.COMPLETED);
        Integer completedOrders = workspaceMapper.countOrders(map);

        //待接单订单
        map.put("status", Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = workspaceMapper.countOrders(map);

        //待派送订单（已接单）
        map.put("status", Orders.CONFIRMED);
        Integer deliveredOrders = workspaceMapper.countOrders(map);

        return OrderOverViewVO.builder()
                .allOrders(allOrders)
                .cancelledOrders(cancelledOrders)
                .completedOrders(completedOrders)
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .build();
    }
}
