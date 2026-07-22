package com.medical.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final SymptomRepository symptomRepository;
    private final TriageResultRepository triageResultRepository;

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
}