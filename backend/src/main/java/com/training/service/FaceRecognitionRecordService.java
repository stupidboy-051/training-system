package com.training.service;

import com.training.entity.FaceRecognitionRecord;
import com.training.repository.FaceRecognitionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 人脸识别记录服务
 */
@Service
public class FaceRecognitionRecordService {
    
    @Autowired
    private FaceRecognitionRecordRepository faceRecognitionRecordRepository;
    
    /**
     * 保存人脸识别记录
     * @param courseId 课程ID
     * @param userId 用户ID
     * @param similarity 相似度
     * @param status 识别状态
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 保存的记录
     */
    @Transactional
    public FaceRecognitionRecord saveRecord(Long courseId, Long userId, BigDecimal similarity, 
                                          String status, String ipAddress, String userAgent) {
        FaceRecognitionRecord record = new FaceRecognitionRecord();
        record.setCourseId(courseId);
        record.setUserId(userId);
        record.setTimestamp(LocalDateTime.now());
        record.setSimilarity(similarity);
        record.setStatus(status);
        record.setIpAddress(ipAddress);
        record.setUserAgent(userAgent);
        
        return faceRecognitionRecordRepository.save(record);
    }
    
    /**
     * 获取课程的人脸识别记录
     * @param courseId 课程ID
     * @param pageable 分页信息
     * @return 人脸识别记录分页
     */
    public Page<FaceRecognitionRecord> getRecordsByCourse(Long courseId, Pageable pageable) {
        return faceRecognitionRecordRepository.findByCourseIdOrderByTimestampDesc(courseId, pageable);
    }
    
    /**
     * 获取课程在指定时间范围内的人脸识别记录
     * @param courseId 课程ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param pageable 分页信息
     * @return 人脸识别记录分页
     */
    public Page<FaceRecognitionRecord> getRecordsByCourseAndDateRange(Long courseId, 
                                                                     LocalDateTime startDate, 
                                                                     LocalDateTime endDate, 
                                                                     Pageable pageable) {
        return faceRecognitionRecordRepository.findByCourseIdAndTimestampBetweenOrderByTimestampDesc(
                courseId, startDate, endDate, pageable);
    }
    
    /**
     * 获取用户的人脸识别记录
     * @param userId 用户ID
     * @param pageable 分页信息
     * @return 人脸识别记录分页
     */
    public Page<FaceRecognitionRecord> getRecordsByUser(Long userId, Pageable pageable) {
        return faceRecognitionRecordRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }
    
    /**
     * 统计课程的人脸识别记录数量
     * @param courseId 课程ID
     * @return 记录数量
     */
    public long countRecordsByCourse(Long courseId) {
        return faceRecognitionRecordRepository.countByCourseId(courseId);
    }
    
    /**
     * 批量保存人脸识别记录
     * @param records 记录列表
     * @return 保存的记录列表
     */
    @Transactional
    public List<FaceRecognitionRecord> saveRecords(List<FaceRecognitionRecord> records) {
        return faceRecognitionRecordRepository.saveAll(records);
    }
}