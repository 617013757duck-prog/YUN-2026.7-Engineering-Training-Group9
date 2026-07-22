package com.medical.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.platform.entity.Symptom;
import com.medical.platform.exception.BusinessException;
import com.medical.platform.repository.SymptomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SymptomService {

    private final SymptomRepository symptomRepository;

    public Symptom getById(Long id) {
        Symptom symptom = symptomRepository.selectById(id);
        if (symptom == null) {
            throw new BusinessException(404, "症状记录不存在");
        }
        return symptom;
    }

    public List<Symptom> getByVisitId(Long visitId) {
        return symptomRepository.findByVisitId(visitId);
    }

    public List<Symptom> getRedFlagSymptoms(Long visitId) {
        return symptomRepository.findByVisitIdAndRedFlag(visitId, 1);
    }

    public Page<Symptom> listSymptoms(String symptomName, Page<Symptom> page) {
        return symptomRepository.selectPage(page, 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Symptom>()
                .like(symptomName != null, Symptom::getSymptomName, symptomName)
                .orderByDesc(Symptom::getCreateTime));
    }

    public Symptom create(Symptom symptom) {
        symptom.setIsRedFlag(symptom.getIsRedFlag() != null ? symptom.getIsRedFlag() : 0);
        symptomRepository.insert(symptom);
        return symptom;
    }

    public Symptom update(Long id, Symptom symptom) {
        Symptom existing = symptomRepository.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "症状记录不存在");
        }
        symptom.setId(id);
        symptomRepository.updateById(symptom);
        return symptom;
    }

    public void delete(Long id) {
        symptomRepository.deleteById(id);
    }
}