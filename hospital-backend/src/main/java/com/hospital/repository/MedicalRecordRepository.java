package com.hospital.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.MedicalRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MedicalRecordRepository extends BaseMapper<MedicalRecord> {
}

