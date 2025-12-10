package com.hospital.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.ConsultingRoom;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConsultingRoomRepository extends BaseMapper<ConsultingRoom> {
}

