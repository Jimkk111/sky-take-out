package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.conditionSearch(ordersPageQueryDTO);

        //查询订单明细，封装订单的商品名称和数量
        List<OrderVO> orderVOList = getOrderVOList(page.getResult());

        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    public OrderStatisticsVO statistics() {
        Map map = new HashMap();

        //根据状态，分别查询出待接单、待派送、派送中的订单数量
        map.put("status", Orders.TO_BE_CONFIRMED);
        Integer toBeConfirmed = orderMapper.countByMap(map);

        map.put("status", Orders.CONFIRMED);
        Integer confirmed = orderMapper.countByMap(map);

        map.put("status", Orders.DELIVERY_IN_PROGRESS);
        Integer deliveryInProgress = orderMapper.countByMap(map);

        //将查询出的数据封装到orderStatisticsVO对象中并返回
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    public OrderVO details(Long id) {
        //根据订单id查询订单信息
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //根据订单id查询订单明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        //将订单及明细封装至OrderVO并返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                //订单状态变为已接单（待派送）
                .status(Orders.CONFIRMED)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Transactional
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单只有存在且状态为待接单才可以拒单
        if (!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //拒单需要退款（已支付订单），将支付状态修改为已退款
        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.REFUND)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Transactional
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        //管理端取消订单需要退款，将支付状态修改为已退款
        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.REFUND)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    public void delivery(Long id) {
        //根据id查询订单
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单只有存在且状态为已接单（待派送）才可以派送
        if (!ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(id)
                //订单状态更新为派送中
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    public void complete(Long id) {
        //根据id查询订单
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单只有存在且状态为派送中才可以完成
        if (!ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(id)
                //订单状态更新为已完成
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 封装订单的商品名称和数量字符串
     */
    private List<OrderVO> getOrderVOList(List<Orders> page) {
        List<OrderVO> orderVOList = page.stream().map(orders -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);

            //查询订单明细，将商品名称和数量拼接成字符串：商品名*数量
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
            String orderDishes = orderDetails.stream()
                    .map(detail -> detail.getName() + "*" + detail.getNumber())
                    .collect(Collectors.joining("，"));
            orderVO.setOrderDishes(orderDishes);
            orderVO.setOrderDetailList(orderDetails);
            orderVO.setOrderDetails(orderDetails);
            return orderVO;
        }).collect(Collectors.toList());
        return orderVOList;
    }

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //查询收货地址
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //本项目的下单为模拟支付，下单即支付成功，订单直接进入待接单状态
        Orders order = new Orders();
        order.setAddressBookId(ordersSubmitDTO.getAddressBookId());
        order.setPayMethod(ordersSubmitDTO.getPayMethod());
        order.setRemark(ordersSubmitDTO.getRemark());
        order.setEstimatedDeliveryTime(ordersSubmitDTO.getEstimatedDeliveryTime());
        order.setDeliveryStatus(ordersSubmitDTO.getDeliveryStatus() == null ? 1 : ordersSubmitDTO.getDeliveryStatus());
        order.setTablewareStatus(ordersSubmitDTO.getTablewareStatus() == null ? 1 : ordersSubmitDTO.getTablewareStatus());
        order.setPackAmount(ordersSubmitDTO.getPackAmount() == null ? 0 : ordersSubmitDTO.getPackAmount());
        order.setTablewareNumber(ordersSubmitDTO.getTablewareNumber() == null ? 0 : ordersSubmitDTO.getTablewareNumber());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(userId);
        order.setStatus(Orders.TO_BE_CONFIRMED);
        order.setPayStatus(Orders.PAID);
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());
        order.setPhone(addressBook.getPhone());
        order.setConsignee(addressBook.getConsignee());
        order.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //计算订单总金额
        BigDecimal amount = shoppingCartList.stream()
                .map(cart -> cart.getAmount().multiply(new BigDecimal(cart.getNumber())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setAmount(amount);

        //插入订单数据
        orderMapper.insert(order);

        //插入订单明细数据
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(cart -> OrderDetail.builder()
                .orderId(order.getId())
                .name(cart.getName())
                .image(cart.getImage())
                .dishId(cart.getDishId())
                .setmealId(cart.getSetmealId())
                .dishFlavor(cart.getDishFlavor())
                .number(cart.getNumber())
                .amount(cart.getAmount())
                .build()).collect(Collectors.toList());
        orderDetailMapper.insertBatch(orderDetailList);

        //清空购物车
        shoppingCartMapper.delete(shoppingCart);

        return OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
    }

    /**
     * 查询用户历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult getUserPage(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.conditionSearch(ordersPageQueryDTO);

        List<OrderVO> orderVOList = getOrderVOList(page.getResult());
        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 再来一单
     * @param ordersDTO
     */
    @Transactional
    public void again(OrdersDTO ordersDTO) {
        Long userId = BaseContext.getCurrentId();

        //根据id查询订单，只能对自己的订单再来一单
        Orders ordersDB = orderMapper.getById(ordersDTO.getId());
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!ordersDB.getUserId().equals(userId)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //查询订单明细，加入购物车
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(ordersDTO.getId());
        if (orderDetailList == null || orderDetailList.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(detail -> ShoppingCart.builder()
                .userId(userId)
                .name(detail.getName())
                .image(detail.getImage())
                .dishId(detail.getDishId())
                .setmealId(detail.getSetmealId())
                .dishFlavor(detail.getDishFlavor())
                .number(detail.getNumber())
                .amount(detail.getAmount())
                .createTime(LocalDateTime.now())
                .build()).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(shoppingCartList);
    }
}
