package com.hospital.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserRepository extends BaseMapper<SysUser> {
}

