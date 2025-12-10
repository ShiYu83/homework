package com.hospital.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.PrescriptionItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrescriptionItemRepository extends BaseMapper<PrescriptionItem> {
}

