package com.hospital.service;

import com.hospital.entity.DoctorFollow;

import java.util.List;

/**
 * 医生关注服务接口
 */
public interface DoctorFollowService {
    /**
     * 关注医生
     * @param patientId 患者ID
     * @param doctorId 医生ID
     */
    void followDoctor(Long patientId, Long doctorId);
    
    /**
     * 取消关注医生
     * @param patientId 患者ID
     * @param doctorId 医生ID
     */
    void unfollowDoctor(Long patientId, Long doctorId);
    
    /**
     * 查询患者关注的医生列表
     * @param patientId 患者ID
     * @return 关注列表
     */
    List<Long> getFollowedDoctorIds(Long patientId);
    
    /**
     * 检查患者是否关注了医生
     * @param patientId 患者ID
     * @param doctorId 医生ID
     * @return 是否关注
     */
    boolean isFollowing(Long patientId, Long doctorId);
}