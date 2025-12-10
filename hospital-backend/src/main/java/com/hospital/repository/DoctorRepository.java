package com.hospital.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DoctorRepository extends BaseMapper<Doctor> {
}

