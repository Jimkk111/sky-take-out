package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    /**
     * 插入地址
     * @param addressBook
     */
    void insert(AddressBook addressBook);

    /**
     * 查询某用户的全部地址
     * @param userId
     * @return
     */
    @Select("select * from address_book where user_id = #{userId} order by is_default desc, id desc")
    List<AddressBook> list(Long userId);

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @Select("select * from address_book where id = #{id}")
    AddressBook getById(Long id);

    /**
     * 查询某用户的默认地址
     * @param userId
     * @return
     */
    @Select("select * from address_book where user_id = #{userId} and is_default = 1")
    AddressBook getDefault(Long userId);

    /**
     * 修改地址
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 将某用户的全部地址设置为非默认
     * @param userId
     */
    void updateToNotDefault(Long userId);

    /**
     * 将指定地址设置为默认（带归属校验）
     * @param id
     * @param userId
     */
    void updateToDefault(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 批量删除当前用户的地址
     * @param ids
     * @param userId
     */
    void deleteByIds(@Param("ids") List<Long> ids, @Param("userId") Long userId);
}
