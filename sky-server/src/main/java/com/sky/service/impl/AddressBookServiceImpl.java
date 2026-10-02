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
     * 修改地址（默认地址不允许在此变更，只能通过setDefault；更新范围限定为当前用户的地址）
     * @param addressBook
     */
    public void update(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.update(addressBook);
    }

    /**
     * 批量删除当前用户的地址
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {
        addressBookMapper.deleteByIds(ids, BaseContext.getCurrentId());
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
        Long userId = BaseContext.getCurrentId();
        addressBookMapper.updateToNotDefault(userId);
        //再将指定地址设置为默认（带归属校验，目标地址不属于当前用户时不会命中任何行）
        addressBookMapper.updateToDefault(addressBook.getId(), userId);
    }
}
