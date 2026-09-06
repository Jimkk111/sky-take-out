package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    /**
     * 根据条件统计营业额
     * @param map
     * @return
     */
    Double sumTurnover(Map map);

    /**
     * 根据条件统计订单数量
     * @param map
     * @return
     */
    Integer countOrders(Map map);

    /**
     * 根据条件统计用户数量
     * @param map
     * @return
     */
    Integer countUsers(Map map);

    /**
     * 根据条件统计销量排名top10的菜品
     * @param map
     * @return
     */
    List<GoodsSalesDTO> getTop10(Map map);
}
