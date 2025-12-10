package com.hospital.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentRepository extends BaseMapper<Payment> {
}

