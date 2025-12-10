package com.hospital.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Patient;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PatientRepository extends BaseMapper<Patient> {
}

