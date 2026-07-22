package com.medical.platform.state;

import com.medical.platform.entity.VisitStatus;
import com.medical.platform.exception.BusinessException;

import java.util.Arrays;
import java.util.List;

public class VisitStateMachine {

    private static final List<List<VisitStatus>> TRANSITIONS = Arrays.asList(
        Arrays.asList(VisitStatus.PENDING, VisitStatus.PRE_SCREENED),
        Arrays.asList(VisitStatus.PRE_SCREENED, VisitStatus.AI_ANALYZED),
        Arrays.asList(VisitStatus.AI_ANALYZED, VisitStatus.SAFETY_REVIEWED),
        Arrays.asList(VisitStatus.SAFETY_REVIEWED, VisitStatus.DOCTOR_REVIEWING),
        Arrays.asList(VisitStatus.DOCTOR_REVIEWING, VisitStatus.DOCTOR_APPROVED),
        Arrays.asList(VisitStatus.DOCTOR_REVIEWING, VisitStatus.REJECTED),
        Arrays.asList(VisitStatus.DOCTOR_APPROVED, VisitStatus.FOLLOWUP_PLANNED),
        Arrays.asList(VisitStatus.FOLLOWUP_PLANNED, VisitStatus.COMPLETED),
        Arrays.asList(VisitStatus.PENDING, VisitStatus.REJECTED)
    );

    public static boolean canTransition(VisitStatus from, VisitStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return TRANSITIONS.stream()
            .anyMatch(t -> t.get(0) == from && t.get(1) == to);
    }

    public static VisitStatus transition(VisitStatus from, VisitStatus to) {
        if (!canTransition(from, to)) {
            throw new BusinessException(400, 
                String.format("状态转换不允许: %s -> %s", 
                    from != null ? from.getDisplayName() : "null", 
                    to != null ? to.getDisplayName() : "null"));
        }
        return to;
    }

    public static List<VisitStatus> getNextStates(VisitStatus current) {
        if (current == null) {
            return Arrays.asList(VisitStatus.PENDING);
        }
        return TRANSITIONS.stream()
            .filter(t -> t.get(0) == current)
            .map(t -> t.get(1))
            .distinct()
            .toList();
    }
}