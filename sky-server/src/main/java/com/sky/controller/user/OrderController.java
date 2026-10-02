package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.exception.OrderBusinessException;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

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
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 用户下单（生成待支付订单，支付动作由 /order/payment 发起）
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
     * 订单支付
     * 返回调起微信收银台的签名四件套；未配置商户凭证时走模拟支付（mock=true），
     * 订单直接标记支付成功，前端跳过requestPayment直接轮询订单状态
     * @param ordersPaymentDTO
     * @return
     */
    @PostMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 支付成功回调（微信服务器调用，已在登录拦截器放行）
     * 处理流程：验签 -> APIv3密钥解密 -> 校验交易状态 -> 更新订单为已支付
     * 应答规范：成功返回200+{"code":"SUCCESS"}；失败返回500+{"code":"FAIL"}，微信会按退避策略重试
     */
    @PostMapping("/payNotify")
    @ApiOperation("支付成功回调（微信服务器调用）")
    public String payNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String serial = request.getHeader("Wechatpay-Serial");
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        String nonce = request.getHeader("Wechatpay-Nonce");
        String signature = request.getHeader("Wechatpay-Signature");
        String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        log.info("收到支付回调，订单验签中... serial={}", serial);

        if (!weChatPayUtil.verifyNotifySign(serial, timestamp, nonce, body, signature)) {
            response.setStatus(500);
            return "{\"code\":\"FAIL\",\"message\":\"验签失败\"}";
        }

        //验签通过，解密resource取出商户订单号和交易状态
        JSONObject resource = JSON.parseObject(body).getJSONObject("resource");
        String plain = weChatPayUtil.decryptNotifyResource(
                resource.getString("associated_data"),
                resource.getString("nonce"),
                resource.getString("ciphertext"));
        JSONObject orderInfo = JSON.parseObject(plain);
        log.info("支付回调解密结果：{}", orderInfo);

        if (!"SUCCESS".equals(orderInfo.getString("trade_state"))) {
            response.setStatus(500);
            return "{\"code\":\"FAIL\",\"message\":\"交易未成功\"}";
        }

        orderService.paySuccess(orderInfo.getString("out_trade_no"));
        return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
    }

    /**
     * 查询订单详情（支付结果轮询使用，只能查自己的订单）
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> orderDetail(@PathVariable Long id) {
        OrderVO orderVO = orderService.details(id);
        if (orderVO == null || !orderVO.getUserId().equals(BaseContext.getCurrentId())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return Result.success(orderVO);
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

    /**
     * 用户催单
     * @param id
     * @return
     */
    @PutMapping("/reminder/{id}")
    @ApiOperation("用户催单")
    public Result<String> reminder(@PathVariable Long id) {
        log.info("用户催单：{}", id);
        orderService.reminder(id);
        return Result.success();
    }
}
