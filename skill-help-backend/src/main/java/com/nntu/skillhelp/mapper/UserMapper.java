package com.nntu.skillhelp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nntu.skillhelp.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
