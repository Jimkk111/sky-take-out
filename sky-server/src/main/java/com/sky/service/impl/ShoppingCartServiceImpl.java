package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加商品到购物车
     * @param shoppingCartDTO
     */
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //判断当前商品是否已存在于购物车，存在则数量加1
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .dishId(shoppingCartDTO.getDishId())
                .setmealId(shoppingCartDTO.getSetmealId())
                .dishFlavor(shoppingCartDTO.getDishFlavor())
                .build();

        List<ShoppingCart> cartList = shoppingCartMapper.list(shoppingCart);

        if (cartList != null && !cartList.isEmpty()) {
            //已存在，数量加1
            ShoppingCart cart = cartList.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart.getId(), cart.getNumber());
            return;
        }

        //不存在，插入一条购物车数据，商品名称、图片、单价来自菜品或套餐
        shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .dishId(shoppingCartDTO.getDishId())
                .setmealId(shoppingCartDTO.getSetmealId())
                .dishFlavor(shoppingCartDTO.getDishFlavor())
                .number(1)
                .createTime(LocalDateTime.now())
                .build();

        Long dishId = shoppingCartDTO.getDishId();
        if (dishId != null) {
            Dish dish = dishMapper.getById(dishId);
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        } else {
            Long setmealId = shoppingCartDTO.getSetmealId();
            if (setmealId != null) {
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
        }
        shoppingCartMapper.insert(shoppingCart);
    }

    /**
     * 从购物车中减少一件商品
     * @param shoppingCartDTO
     */
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .dishId(shoppingCartDTO.getDishId())
                .setmealId(shoppingCartDTO.getSetmealId())
                .dishFlavor(shoppingCartDTO.getDishFlavor())
                .build();

        List<ShoppingCart> cartList = shoppingCartMapper.list(shoppingCart);
        if (cartList == null || cartList.isEmpty()) {
            return;
        }

        ShoppingCart cart = cartList.get(0);
        if (cart.getNumber() > 1) {
            //数量大于1，数量减1
            cart.setNumber(cart.getNumber() - 1);
            shoppingCartMapper.updateNumberById(cart.getId(), cart.getNumber());
        } else {
            //数量为1，直接删除该条购物车数据
            shoppingCartMapper.deleteById(cart.getId());
        }
    }

    /**
     * 查询当前用户的购物车
     * @return
     */
    public List<ShoppingCart> list() {
        return shoppingCartMapper.list(ShoppingCart.builder().userId(BaseContext.getCurrentId()).build());
    }

    /**
     * 清空当前用户的购物车
     */
    public void clean() {
        shoppingCartMapper.delete(ShoppingCart.builder().userId(BaseContext.getCurrentId()).build());
    }
}
