package com.sky.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sky.dto.DishDTO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import com.sky.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.annotations.ApiOperation;


@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理")
@Slf4j 
public class DishController {
    
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品: {}", dishDTO);
        return Result.success();
    }
}
 