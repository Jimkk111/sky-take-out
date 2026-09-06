package com.sky.controller.user;

import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.mapper.SetmealMapper;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 套餐浏览接口（用户端）
 */
@RestController("userSetmealController")
@RequestMapping("/setmeal")
@Api(tags = "套餐浏览接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 根据分类id查询套餐
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> list(Setmeal setmeal) {
        log.info("用户端查询套餐：{}", setmeal);
        List<Setmeal> list = setmealMapper.list(setmeal);
        return Result.success(list);
    }

    /**
     * 根据套餐id查询套餐包含的菜品
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询套餐包含的菜品")
    public Result<List<DishItemVO>> dishList(@PathVariable Long id) {
        log.info("用户端查询套餐包含的菜品：{}", id);
        List<DishItemVO> list = setmealMapper.getDishItemBySetmealId(id);
        return Result.success(list);
    }
}
