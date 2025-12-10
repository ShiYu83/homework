package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.entity.DoctorFollow;
import com.hospital.repository.DoctorFollowRepository;
import com.hospital.service.DoctorFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生关注服务实现类
 */
@Service
@RequiredArgsConstructor
public class DoctorFollowServiceImpl implements DoctorFollowService {
    
    private final DoctorFollowRepository doctorFollowRepository;
    
    @Override
    @Transactional
    public void followDoctor(Long patientId, Long doctorId) {
        // 检查是否已经关注
        if (isFollowing(patientId, doctorId)) {
            return;
        }
        
        DoctorFollow follow = new DoctorFollow();
        follow.setPatientId(patientId);
        follow.setDoctorId(doctorId);
        follow.setCreateTime(LocalDateTime.now());
        
        doctorFollowRepository.insert(follow);
    }
    
    @Override
    @Transactional
    public void unfollowDoctor(Long patientId, Long doctorId) {
        LambdaQueryWrapper<DoctorFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorFollow::getPatientId, patientId)
               .eq(DoctorFollow::getDoctorId, doctorId);
        
        doctorFollowRepository.delete(wrapper);
    }
    
    @Override
    public List<Long> getFollowedDoctorIds(Long patientId) {
        LambdaQueryWrapper<DoctorFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorFollow::getPatientId, patientId);
        
        List<DoctorFollow> follows = doctorFollowRepository.selectList(wrapper);
        return follows.stream()
                .map(DoctorFollow::getDoctorId)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean isFollowing(Long patientId, Long doctorId) {
        LambdaQueryWrapper<DoctorFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorFollow::getPatientId, patientId)
               .eq(DoctorFollow::getDoctorId, doctorId);
        
        return doctorFollowRepository.selectCount(wrapper) > 0;
    }
}