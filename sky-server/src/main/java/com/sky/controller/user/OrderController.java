package com.sky.controller.user;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单接口（用户端）
 */
@RestController("userOrderController")
@RequestMapping("/order")
@Api(tags = "用户端订单接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单（本项目下单即支付成功，订单进入待接单状态）
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/userPage")
    @ApiOperation("查询历史订单")
    public Result<PageResult> userPage(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("查询历史订单：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.getUserPage(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 再来一单
     * @param ordersDTO
     * @return
     */
    @PostMapping("/again")
    @ApiOperation("再来一单")
    public Result<String> again(@RequestBody OrdersDTO ordersDTO) {
        log.info("再来一单：{}", ordersDTO);
        orderService.again(ordersDTO);
        return Result.success();
    }
}
