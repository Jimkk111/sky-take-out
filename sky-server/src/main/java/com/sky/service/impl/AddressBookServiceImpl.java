package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.exception.AddressBookBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 新增地址
     * @param addressBook
     */
    public void save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        //新增地址默认为非默认地址
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }

    /**
     * 查询当前用户的全部地址
     * @return
     */
    public List<AddressBook> list() {
        return addressBookMapper.list(BaseContext.getCurrentId());
    }

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    public AddressBook getById(Long id) {
        AddressBook addressBook = addressBookMapper.getById(id);
        if (addressBook == null) {
            throw new AddressBookBusinessException("地址不存在");
        }
        return addressBook;
    }

    /**
     * 修改地址
     * @param addressBook
     */
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    /**
     * 批量删除地址
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {
        addressBookMapper.deleteByIds(ids);
    }

    /**
     * 查询默认地址
     * @return
     */
    public AddressBook getDefault() {
        return addressBookMapper.getDefault(BaseContext.getCurrentId());
    }

    /**
     * 设置默认地址
     * @param addressBook
     */
    @Transactional
    public void setDefault(AddressBook addressBook) {
        //先将当前用户的所有地址设置为非默认
        addressBookMapper.updateToNotDefault(BaseContext.getCurrentId());
        //再将指定地址设置为默认
        addressBookMapper.update(AddressBook.builder().id(addressBook.getId()).isDefault(1).build());
    }
}
