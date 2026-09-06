package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    /**
     * 统计指定时间区间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDateTime begin, LocalDateTime end) {
        //存放begin到end范围内的每天日期
        List<LocalDate> dateList = getDateList(begin.toLocalDate(), end.toLocalDate());

        //存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("status", 5);
            map.put("begin", beginTime);
            map.put("end", endTime);

            Double turnover = reportMapper.sumTurnover(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(join(dateList))
                .turnoverList(join(turnoverList))
                .build();
    }

    /**
     * 统计指定时间区间内的用户数据
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDateTime begin, LocalDateTime end) {
        //存放begin到end范围内的每天日期
        List<LocalDate> dateList = getDateList(begin.toLocalDate(), end.toLocalDate());

        //存放每天的新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        //存放每天的总用户数量
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("end", endTime);

            //查询当天之前（含当天）的总用户数量
            Integer totalUserCount = reportMapper.countUsers(map);
            totalUserList.add(totalUserCount);

            //查询当天的新增用户数量
            map.put("begin", beginTime);
            Integer newUserCount = reportMapper.countUsers(map);
            newUserList.add(newUserCount);
        }

        return UserReportVO.builder()
                .dateList(join(dateList))
                .totalUserList(join(totalUserList))
                .newUserList(join(newUserList))
                .build();
    }

    /**
     * 统计指定时间区间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrdersStatistics(LocalDateTime begin, LocalDateTime end) {
        //存放begin到end范围内的每天日期
        List<LocalDate> dateList = getDateList(begin.toLocalDate(), end.toLocalDate());

        //存放每天的订单总数
        List<Integer> orderCountList = new ArrayList<>();
        //存放每天的有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);

            //查询每天的订单总数
            Integer orderCount = reportMapper.countOrders(map);
            orderCountList.add(orderCount);

            //查询每天的有效订单数（订单状态为已完成）
            map.put("status", 5);
            Integer validOrderCount = reportMapper.countOrders(map);
            validOrderCountList.add(validOrderCount);
        }

        //计算时间区间内的订单总数量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //计算时间区间内的有效订单数量
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        //计算订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(join(dateList))
                .orderCountList(join(orderCountList))
                .validOrderCountList(join(validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 统计指定时间区间内的销量排名top10
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getTop10(LocalDateTime begin, LocalDateTime end) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);

        List<GoodsSalesDTO> goodsSalesDTOList = reportMapper.getTop10(map);

        String nameList = join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()));
        String numberList = join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()));

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出指定时间区间内的运营数据报表
     * @param response
     * @param begin
     * @param end
     */
    public void exportBusinessData(HttpServletResponse response, LocalDate begin, LocalDate end) {
        //查询概览运营数据，供Excel报表展示
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        BusinessDataVO businessDataVO = getBusinessData(beginTime, endTime);

        //通过POI将数据写入Excel文件并返回
        XSSFWorkbook excel = new XSSFWorkbook();
        XSSFSheet sheet = excel.createSheet("sheet1");

        //创建标题行
        XSSFRow row1 = sheet.createRow(0);
        row1.createCell(0).setCellValue("时间区间：" + begin + " 至 " + end);

        //创建概览表头
        XSSFRow row2 = sheet.createRow(1);
        row2.createCell(0).setCellValue("营业额");
        row2.createCell(1).setCellValue("有效订单");
        row2.createCell(2).setCellValue("订单完成率");
        row2.createCell(3).setCellValue("平均客单价");
        row2.createCell(4).setCellValue("新增用户");

        //创建概览数据行
        XSSFRow row3 = sheet.createRow(2);
        row3.createCell(0).setCellValue(businessDataVO.getTurnover());
        row3.createCell(1).setCellValue(businessDataVO.getValidOrderCount());
        row3.createCell(2).setCellValue(businessDataVO.getOrderCompletionRate());
        row3.createCell(3).setCellValue(businessDataVO.getUnitPrice());
        row3.createCell(4).setCellValue(businessDataVO.getNewUsers());

        //创建明细表头
        XSSFRow row4 = sheet.createRow(3);
        row4.createCell(0).setCellValue("日期");
        row4.createCell(1).setCellValue("营业额");
        row4.createCell(2).setCellValue("有效订单");
        row4.createCell(3).setCellValue("订单完成率");

        //遍历日期逐行写入明细数据
        int rowIndex = 4;
        for (LocalDate date : getDateList(begin, end)) {
            LocalDateTime dayBegin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            BusinessDataVO dayData = getBusinessData(dayBegin, dayEnd);

            XSSFRow row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(date.toString());
            row.createCell(1).setCellValue(dayData.getTurnover());
            row.createCell(2).setCellValue(dayData.getValidOrderCount());
            row.createCell(3).setCellValue(dayData.getOrderCompletionRate());
        }

        //通过输出流将Excel文件写至浏览器下载
        try (ServletOutputStream out = response.getOutputStream()) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=report.xlsx");
            excel.write(out);
        } catch (IOException e) {
            log.error("导出Excel报表失败：{}", e.getMessage());
        } finally {
            try {
                excel.close();
            } catch (IOException e) {
                log.error("关闭Excel工作簿失败：{}", e.getMessage());
            }
        }
    }

    /**
     * 获取begin到end范围内的每天日期列表
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        while (!begin.isAfter(end)) {
            dates.add(begin);
            begin = begin.plusDays(1);
        }
        return dates;
    }

    /**
     * 将集合元素以逗号分隔拼接为字符串
     */
    private String join(List<?> list) {
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /**
     * 查询指定时间区间内的概览运营数据
     */
    private BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);

        //营业额（已完成订单的金额合计）
        Double turnover = reportMapper.sumTurnover(map);
        turnover = turnover == null ? 0.0 : turnover;

        //有效订单数（订单状态为已完成）
        map.put("status", 5);
        Integer validOrderCount = reportMapper.countOrders(map);

        //订单总数
        Map totalMap = new HashMap();
        totalMap.put("begin", begin);
        totalMap.put("end", end);
        Integer totalOrderCount = reportMapper.countOrders(totalMap);

        //订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        //平均客单价 = 营业额 / 有效订单数
        Double unitPrice = 0.0;
        if (validOrderCount != 0) {
            unitPrice = turnover / validOrderCount;
        }

        //新增用户数
        Integer newUsers = reportMapper.countUsers(map);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }
}
