package com.sky.controller.user;

import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜品浏览接口（用户端）
 */
@RestController("userDishController")
@RequestMapping("/dish")
@Api(tags = "菜品浏览接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 根据分类id查询菜品（含口味，用户端选择规格使用）
     * @param dish
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Dish dish) {
        log.info("用户端查询菜品：{}", dish);
        List<DishVO> list = dishService.listWithFlavor(dish);
        return Result.success(list);
    }
}
