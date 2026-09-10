package com.sky.service.impl;

import com.sky.service.DishService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.sky.dto.DishDTO;
import com.sky.mapper.DishMapper;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import java.util.List;

@Service
@Slf4j 
public class DishServiceImpl implements DishService {
    
    private final DishMapper dishMapper;

    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        if(dishDTO.getFlavors() != null && !dishDTO.getFlavors().isEmpty()) {
            List<DishFlavor> flavors = dishDTO.getFlavors();
            flavors.forEach(flavor -> flavor.setDishId(dish.getId()));

            flavorMapper.insert()
        }
    }
}
