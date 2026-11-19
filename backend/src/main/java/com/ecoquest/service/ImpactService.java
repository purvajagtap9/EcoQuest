package com.ecoquest.service;

import com.ecoquest.dto.admin.ImpactFormulaRequest;
import com.ecoquest.dto.admin.ImpactFormulaResponse;
import com.ecoquest.dto.impact.ImpactLineItem;
import com.ecoquest.dto.impact.ImpactSummaryResponse;
import com.ecoquest.entity.ImpactFormulaConfig;
import com.ecoquest.entity.ImpactMetric;
import com.ecoquest.entity.Quest;
import com.ecoquest.entity.QuestSubmission;
import com.ecoquest.entity.User;
import com.ecoquest.entity.enums.ImpactType;
import com.ecoquest.entity.enums.QuestCategory;
import com.ecoquest.exception.ApiException;
import com.ecoquest.repository.ImpactFormulaConfigRepository;
import com.ecoquest.repository.ImpactMetricRepository;
import com.ecoquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Formulas are configurable (spec #22): an admin override in ImpactFormulaConfig wins; any
 * category without one falls back to the defaults below, so the system produces meaningful
 * impact numbers with zero setup. Every value is explicitly labeled an ESTIMATE.
 */
@Service
@RequiredArgsConstructor
public class ImpactService {

    private static final String DISCLAIMER =
            "These figures are estimates derived from configurable per-quest formulas, not precise environmental measurements.";

    private static final Map<QuestCategory, ImpactFormulaConfig> DEFAULT_FORMULAS = new EnumMap<>(QuestCategory.class);
    static {
        DEFAULT_FORMULAS.put(QuestCategory.TREE_PLANTATION,
                ImpactFormulaConfig.builder().impactType(ImpactType.TREES_PLANTED).valuePerCompletion(1.0).unit("trees").build());
        DEFAULT_FORMULAS.put(QuestCategory.WASTE_MANAGEMENT,
                ImpactFormulaConfig.builder().impactType(ImpactType.WASTE_SEGREGATED_KG).valuePerCompletion(2.0).unit("kg").build());
        DEFAULT_FORMULAS.put(QuestCategory.PLASTIC_FREE,
                ImpactFormulaConfig.builder().impactType(ImpactType.PLASTIC_AVOIDED_KG).valuePerCompletion(0.5).unit("kg").build());
        DEFAULT_FORMULAS.put(QuestCategory.WATER_CONSERVATION,
                ImpactFormulaConfig.builder().impactType(ImpactType.WATER_SAVED_LITERS).valuePerCompletion(50.0).unit("L").build());
        DEFAULT_FORMULAS.put(QuestCategory.ENERGY_CONSERVATION,
                ImpactFormulaConfig.builder().impactType(ImpactType.ENERGY_SAVED_KWH).valuePerCompletion(5.0).unit("kWh").build());
        DEFAULT_FORMULAS.put(QuestCategory.SUSTAINABLE_TRANSPORT,
                ImpactFormulaConfig.builder().impactType(ImpactType.SUSTAINABLE_TRIPS).valuePerCompletion(1.0).unit("trips").build());
        DEFAULT_FORMULAS.put(QuestCategory.COMMUNITY_CLEANUP,
                ImpactFormulaConfig.builder().impactType(ImpactType.COMMUNITY_CLEANUPS).valuePerCompletion(1.0).unit("cleanups").build());
        // BIODIVERSITY and OTHER have no default metric - not every quest maps to a countable impact figure
    }

    private final ImpactFormulaConfigRepository formulaConfigRepository;
    private final ImpactMetricRepository impactMetricRepository;
    private final UserRepository userRepository;

    /** Called from SubmissionReviewService.approve - creates the ImpactMetric row, if this category has a formula. */
    public void recordImpactForApproval(QuestSubmission submission, Quest quest) {
        ImpactFormulaConfig formula = formulaConfigRepository.findByCategory(quest.getCategory())
                .orElse(DEFAULT_FORMULAS.get(quest.getCategory()));
        if (formula == null) {
            return; // this category has no configured impact metric (e.g. BIODIVERSITY, OTHER)
        }

        String schoolId = userRepository.findById(submission.getStudentId())
                .map(User::getStudentProfile)
                .map(sp -> sp != null ? sp.getSchoolId() : null)
                .orElse(null);

        impactMetricRepository.save(ImpactMetric.builder()
                .type(formula.getImpactType())
                .value(formula.getValuePerCompletion())
                .unit(formula.getUnit())
                .studentId(submission.getStudentId())
                .schoolId(schoolId)
                .submissionId(submission.getId())
                .createdAt(Instant.now())
                .build());
    }

    // ---------- Summaries ----------

    public ImpactSummaryResponse getSummaryForStudent(String studentId) {
        return summarize(impactMetricRepository.findByStudentId(studentId));
    }

    public ImpactSummaryResponse getSummaryForSchool(String schoolId) {
        return summarize(impactMetricRepository.findBySchoolId(schoolId));
    }

    public ImpactSummaryResponse getGlobalSummary() {
        return summarize(impactMetricRepository.findAll());
    }

    private ImpactSummaryResponse summarize(List<ImpactMetric> metrics) {
        Map<ImpactType, ImpactLineItem> byType = metrics.stream()
                .collect(Collectors.groupingBy(
                        ImpactMetric::getType,
                        Collectors.collectingAndThen(Collectors.toList(), list -> ImpactLineItem.builder()
                                .type(list.get(0).getType())
                                .totalValue(list.stream().mapToDouble(m -> m.getValue() == null ? 0 : m.getValue()).sum())
                                .unit(list.get(0).getUnit())
                                .build())));

        return ImpactSummaryResponse.builder()
                .items(byType.values().stream()
                        .sorted((a, b) -> a.getType().name().compareTo(b.getType().name()))
                        .toList())
                .disclaimer(DISCLAIMER)
                .build();
    }

    // ---------- Admin: formula configuration ----------

    public List<ImpactFormulaResponse> listFormulas() {
        Map<QuestCategory, ImpactFormulaConfig> overrides = formulaConfigRepository.findAll().stream()
                .collect(Collectors.toMap(ImpactFormulaConfig::getCategory, c -> c));

        return List.of(QuestCategory.values()).stream()
                .map(category -> {
                    ImpactFormulaConfig override = overrides.get(category);
                    ImpactFormulaConfig effective = override != null ? override : DEFAULT_FORMULAS.get(category);
                    if (effective == null) {
                        return ImpactFormulaResponse.builder().category(category).isDefault(true).build();
                    }
                    return ImpactFormulaResponse.builder()
                            .id(override != null ? override.getId() : null)
                            .category(category)
                            .impactType(effective.getImpactType())
                            .valuePerCompletion(effective.getValuePerCompletion())
                            .unit(effective.getUnit())
                            .isDefault(override == null)
                            .build();
                })
                .toList();
    }

    public ImpactFormulaResponse upsertFormula(ImpactFormulaRequest request) {
        ImpactFormulaConfig config = formulaConfigRepository.findByCategory(request.getCategory())
                .orElse(ImpactFormulaConfig.builder().category(request.getCategory()).build());
        config.setImpactType(request.getImpactType());
        config.setValuePerCompletion(request.getValuePerCompletion());
        config.setUnit(request.getUnit());
        ImpactFormulaConfig saved = formulaConfigRepository.save(config);

        return ImpactFormulaResponse.builder()
                .id(saved.getId())
                .category(saved.getCategory())
                .impactType(saved.getImpactType())
                .valuePerCompletion(saved.getValuePerCompletion())
                .unit(saved.getUnit())
                .isDefault(false)
                .build();
    }

    public void resetFormulaToDefault(QuestCategory category) {
        formulaConfigRepository.findByCategory(category)
                .ifPresentOrElse(
                        config -> formulaConfigRepository.deleteById(config.getId()),
                        () -> { throw ApiException.notFound("No override exists for this category"); }
                );
    }
}
