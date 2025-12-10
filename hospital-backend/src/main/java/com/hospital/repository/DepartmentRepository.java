package com.hospital.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Department;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentRepository extends BaseMapper<Department> {
}

