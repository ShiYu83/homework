package com.hospital.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Medicine;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MedicineRepository extends BaseMapper<Medicine> {
}

