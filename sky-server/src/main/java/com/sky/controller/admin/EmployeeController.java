package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO, HttpServletResponse response) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        // 以HttpOnly cookie下发令牌，浏览器自动携带且JS无法读取；本地http调试，secure保持关闭
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getAdminTokenName(), token)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtProperties.getAdminTtl() / 1000)
                .secure(false)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @Operation()
    public Result<String> logout(HttpServletResponse response) {
        // 覆盖同名cookie使其立即过期，清除浏览器中的令牌
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getAdminTokenName(), "")
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return Result.success();
    }

    /**
     *  新增员工
     * @param employeeDTO
     * @return Result
     */
    @PostMapping
    @Operation(summary = "新增员工", description = "新增员工接口")
    public Result<String> save(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.save(employeeDTO);
        return Result.success("新增员工成功");
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return Result<PageResult>
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询员工", description = "分页查询员工接口")
    public Result<PageResult> pageQuery(@RequestBody EmployeePageQueryDTO employeePageQueryDTO){
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     * @return Result<String>
     */
    @PostMapping("/status/{status}")
    @Operation(summary = "员工状态修改", description = "员工状态修改接口")
    public Result<String> setStatus(@PathVariable Integer status, Long id){
        log.info("启用/禁用员工账号：{}，{}", status, id);
        employeeService.setStatus(status, id);
        return Result.success("员工状态修改成功");
    }

    /**
     * 根据id查询员工
     * @param id
     * @return Result<Employee>
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "根据id查询员工", description = "根据id查询员工接口")
    public Result<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     * @return Result
     */
    @PutMapping
    @Operation(summary = "修改员工信息", description = "修改员工信息接口")
    public Result<String> update(@RequestBody EmployeeDTO employeeDTO){
        employeeService.update(employeeDTO);
        return Result.success("修改员工信息成功");
    }

    /**
     * 修改密码
     * @param passwordEditDTO
     * @return Result
     */
    @PutMapping("/editPassword")
    @Operation(summary = "修改密码", description = "修改密码接口")
    public Result<String> editPassword(@RequestBody PasswordEditDTO passwordEditDTO){
        log.info("修改密码：{}", passwordEditDTO);
        employeeService.editPassword(passwordEditDTO);
        return Result.success("修改密码成功");
    }
}
