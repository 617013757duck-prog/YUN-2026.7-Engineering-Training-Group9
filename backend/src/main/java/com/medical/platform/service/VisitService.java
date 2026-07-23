package com.medical.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.platform.client.AIServiceClient;
import com.medical.platform.dto.AIAnalyzeResponse;
import com.medical.platform.dto.VisitCreateRequest;
import com.medical.platform.dto.VisitVO;
import com.medical.platform.entity.Symptom;
import com.medical.platform.entity.TriageResult;
import com.medical.platform.entity.Visit;
import com.medical.platform.entity.VisitStatus;
import com.medical.platform.exception.BusinessException;
import com.medical.platform.repository.SymptomRepository;
import com.medical.platform.repository.TriageResultRepository;
import com.medical.platform.repository.VisitRepository;
import com.medical.platform.state.VisitStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final SymptomRepository symptomRepository;
    private final TriageResultRepository triageResultRepository;
    private final AIServiceClient aiServiceClient;

    @Transactional
    public Visit createVisit(VisitCreateRequest request) {
        Visit visit = new Visit();
        visit.setPatientName(request.getPatientName());
        visit.setPatientPhone(request.getPatientPhone());
        visit.setPatientIdCard(request.getPatientIdCard());
        visit.setAge(request.getAge());
        visit.setGender(request.getGender());
        visit.setChiefComplaint(request.getChiefComplaint());
        visit.setHistoryOfPresentIllness(request.getHistoryOfPresentIllness());
        visit.setPastMedicalHistory(request.getPastMedicalHistory());
        visit.setMedicationHistory(request.getMedicationHistory());
        visit.setAllergies(request.getAllergies());
        visit.setStatus(VisitStatus.PENDING.name());
        
        visitRepository.insert(visit);

        if (request.getSymptoms() != null) {
            for (var symptomReq : request.getSymptoms()) {
                Symptom symptom = new Symptom();
                symptom.setVisitId(visit.getId());
                symptom.setSymptomName(symptomReq.getSymptomName());
                symptom.setSymptomCode(symptomReq.getSymptomCode());
                symptom.setSeverity(symptomReq.getSeverity());
                symptom.setOnsetTime(symptomReq.getOnsetTime());
                symptom.setDuration(symptomReq.getDuration());
                symptom.setLocation(symptomReq.getLocation());
                symptom.setDescription(symptomReq.getDescription());
                symptom.setAssociatedSymptoms(symptomReq.getAssociatedSymptoms());
                symptom.setIsRedFlag(0);
                symptomRepository.insert(symptom);
            }
        }

        return visit;
    }

    public VisitVO getVisitById(Long id) {
        Visit visit = visitRepository.selectById(id);
        if (visit == null) {
            throw new BusinessException(404, "就诊记录不存在");
        }
        
        VisitVO vo = convertToVO(visit);
        vo.setSymptoms(symptomRepository.findByVisitId(id));
        
        TriageResult triage = triageResultRepository.findByVisitId(id);
        vo.setTriage(triage);
        
        return vo;
    }

    public Page<VisitVO> listVisits(String status, String riskLevel, Long patientId, Page<Visit> page) {
        LambdaQueryWrapper<Visit> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Visit::getStatus, status);
        }
        if (riskLevel != null && !riskLevel.isEmpty()) {
            wrapper.eq(Visit::getRiskLevel, riskLevel);
        }
        if (patientId != null) {
            wrapper.eq(Visit::getPatientId, patientId);
        }
        wrapper.orderByDesc(Visit::getCreateTime);

        Page<Visit> visitPage = visitRepository.selectPage(page, wrapper);
        Page<VisitVO> resultPage = new Page<>();
        resultPage.setRecords(visitPage.getRecords().stream().map(this::convertToVO).toList());
        resultPage.setTotal(visitPage.getTotal());
        resultPage.setSize(visitPage.getSize());
        resultPage.setCurrent(visitPage.getCurrent());
        return resultPage;
    }

    @Transactional
    public Visit updateStatus(Long id, VisitStatus newStatus) {
        Visit visit = visitRepository.selectById(id);
        if (visit == null) {
            throw new BusinessException(404, "就诊记录不存在");
        }
        
        VisitStatus currentStatus = VisitStatus.valueOf(visit.getStatus());
        VisitStatus updatedStatus = VisitStateMachine.transition(currentStatus, newStatus);
        visit.setStatus(updatedStatus.name());
        visitRepository.updateById(visit);
        
        return visit;
    }

    @Transactional
    public Visit updateVisit(Long id, VisitCreateRequest request) {
        Visit visit = visitRepository.selectById(id);
        if (visit == null) {
            throw new BusinessException(404, "就诊记录不存在");
        }
        
        visit.setPatientName(request.getPatientName());
        visit.setPatientPhone(request.getPatientPhone());
        visit.setPatientIdCard(request.getPatientIdCard());
        visit.setAge(request.getAge());
        visit.setGender(request.getGender());
        visit.setChiefComplaint(request.getChiefComplaint());
        visit.setHistoryOfPresentIllness(request.getHistoryOfPresentIllness());
        visit.setPastMedicalHistory(request.getPastMedicalHistory());
        visit.setMedicationHistory(request.getMedicationHistory());
        visit.setAllergies(request.getAllergies());
        
        visitRepository.updateById(visit);
        
        if (request.getSymptoms() != null) {
            symptomRepository.delete(new LambdaQueryWrapper<Symptom>().eq(Symptom::getVisitId, id));
            for (var symptomReq : request.getSymptoms()) {
                Symptom symptom = new Symptom();
                symptom.setVisitId(id);
                symptom.setSymptomName(symptomReq.getSymptomName());
                symptom.setSymptomCode(symptomReq.getSymptomCode());
                symptom.setSeverity(symptomReq.getSeverity());
                symptom.setOnsetTime(symptomReq.getOnsetTime());
                symptom.setDuration(symptomReq.getDuration());
                symptom.setLocation(symptomReq.getLocation());
                symptom.setDescription(symptomReq.getDescription());
                symptom.setAssociatedSymptoms(symptomReq.getAssociatedSymptoms());
                symptom.setIsRedFlag(0);
                symptomRepository.insert(symptom);
            }
        }
        
        return visit;
    }

    @Transactional
    public void deleteVisit(Long id) {
        visitRepository.deleteById(id);
        symptomRepository.delete(new LambdaQueryWrapper<Symptom>().eq(Symptom::getVisitId, id));
        triageResultRepository.delete(new LambdaQueryWrapper<TriageResult>().eq(TriageResult::getVisitId, id));
    }

    private VisitVO convertToVO(Visit visit) {
        VisitVO vo = new VisitVO();
        vo.setId(visit.getId());
        vo.setPatientName(visit.getPatientName());
        vo.setPatientPhone(visit.getPatientPhone());
        vo.setPatientIdCard(visit.getPatientIdCard());
        vo.setAge(visit.getAge());
        vo.setGender(visit.getGender());
        vo.setChiefComplaint(visit.getChiefComplaint());
        vo.setHistoryOfPresentIllness(visit.getHistoryOfPresentIllness());
        vo.setPastMedicalHistory(visit.getPastMedicalHistory());
        vo.setMedicationHistory(visit.getMedicationHistory());
        vo.setAllergies(visit.getAllergies());
        vo.setStatus(visit.getStatus());
        try {
            vo.setStatusName(VisitStatus.valueOf(visit.getStatus()).getDisplayName());
        } catch (Exception e) {
            vo.setStatusName(visit.getStatus());
        }
        vo.setRiskLevel(visit.getRiskLevel());
        vo.setRiskBasis(visit.getRiskBasis());
        vo.setAiAnalysis(visit.getAiAnalysis());
        vo.setMedicalStaffComment(visit.getMedicalStaffComment());
        vo.setTriageResult(visit.getTriageResult());
        vo.setCreateTime(visit.getCreateTime());
        vo.setUpdateTime(visit.getUpdateTime());
        return vo;
    }

    /**
     * 执行AI分析
     * 
     * @param id 就诊ID
     * @return 更新后的就诊记录
     */
    @Transactional
    public Visit performAIAnalysis(Long id) {
        Visit visit = visitRepository.selectById(id);
        if (visit == null) {
            throw new BusinessException(404, "就诊记录不存在");
        }
        
        // 检查状态是否允许AI分析
        VisitStatus currentStatus = VisitStatus.valueOf(visit.getStatus());
        if (!VisitStateMachine.canTransition(currentStatus, VisitStatus.AI_ANALYZED)) {
            throw new BusinessException(400, "当前状态不允许执行AI分析");
        }
        
        // 获取症状列表
        List<Symptom> symptoms = symptomRepository.findByVisitId(id);
        if (symptoms.isEmpty()) {
            throw new BusinessException(400, "就诊记录没有症状信息");
        }
        
        // 提取症状名称
        List<String> symptomNames = symptoms.stream()
            .map(Symptom::getSymptomName)
            .collect(Collectors.toList());
        
        // 计算症状持续天数（简化处理）
        int durationDays = 1;
        if (symptoms.get(0).getDuration() != null) {
            try {
                durationDays = Integer.parseInt(symptoms.get(0).getDuration().replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                durationDays = 1;
            }
        }
        
        // 调用AI服务
        log.info("开始AI分析: visitId={}", id);
        AIAnalyzeResponse aiResponse = aiServiceClient.analyze(
            id,
            symptomNames,
            visit.getAge(),
            durationDays,
            visit.getChiefComplaint()
        );
        
        // 更新就诊记录
        visit.setRiskLevel(aiResponse.getRiskLevel());
        visit.setAiAnalysis(aiResponse.getAnalysis());
        visit.setStatus(VisitStatus.AI_ANALYZED.name());
        visitRepository.updateById(visit);
        
        // 保存分诊结果
        TriageResult triage = new TriageResult();
        triage.setVisitId(id);
        triage.setRiskLevel(aiResponse.getRiskLevel());
        triage.setRecommendations(String.join(";", aiResponse.getSuggestions()));
        triage.setAiModelVersion(aiResponse.getModelVersion());
        triage.setPromptVersion(aiResponse.getPromptVersion());
        triage.setKnowledgeBaseVersion(aiResponse.getKbVersion());
        triageResultRepository.insert(triage);
        
        log.info("AI分析完成: visitId={}, riskLevel={}", id, aiResponse.getRiskLevel());
        return visit;
    }

    /**
     * 提交就诊记录进行AI分析（自动触发）
     * 
     * @param id 就诊ID
     * @return 就诊记录
     */
    @Transactional
    public Visit submitForAnalysis(Long id) {
        Visit visit = visitRepository.selectById(id);
        if (visit == null) {
            throw new BusinessException(404, "就诊记录不存在");
        }
        
        // 更新状态为预筛查完成
        VisitStatus currentStatus = VisitStatus.valueOf(visit.getStatus());
        if (currentStatus == VisitStatus.PENDING) {
            visit.setStatus(VisitStatus.PRE_SCREENED.name());
            visitRepository.updateById(visit);
        }
        
        return visit;
    }
}