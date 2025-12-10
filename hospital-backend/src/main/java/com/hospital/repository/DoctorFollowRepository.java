package com.hospital.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.DoctorFollow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生关注Repository
 */
@Mapper
public interface DoctorFollowRepository extends BaseMapper<DoctorFollow> {
}