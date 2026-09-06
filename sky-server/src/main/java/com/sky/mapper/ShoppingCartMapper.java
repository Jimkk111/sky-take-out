package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态条件查询购物车数据
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 批量插入购物车数据（再来一单）
     * @param shoppingCarts
     */
    void insertBatch(List<ShoppingCart> shoppingCarts);

    /**
     * 根据id修改商品数量
     * @param id
     * @param number
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(@Param("id") Long id, @Param("number") Integer number);

    /**
     * 删除购物车中的一条商品
     * @param id
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    /**
     * 动态条件删除购物车数据（清空购物车）
     * @param shoppingCart
     */
    void delete(ShoppingCart shoppingCart);
}
