package fish.payara.console.dev.dto;

import fish.payara.console.dev.model.InjectionPointInfo;
import fish.payara.console.dev.model.ResolutionStatus;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * JSON-friendly DTO representing a CDI injection point
 * together with its resolution outcome.
 *
 * Used by Dev Console REST endpoints.
 *
 * @author Gaurav Gupta
 */
public class InjectionPointDTO {

    private final String declaringBean;
    private final String member;
    private final String requiredType;
    private final Set<String> qualifiers;

    private final ResolutionStatus status;
    private final List<String> candidateBeans;
    private final String failureReason;

    public InjectionPointDTO(InjectionPointInfo info) {

        this.declaringBean = info.getDeclaringBeanClass();
        this.member = info.getMemberSignature();

        Type type = info.getRequiredType();
        this.requiredType = type != null ? type.getTypeName() : null;

        this.qualifiers = info.getQualifiers();
        this.status = info.getResolutionStatus();
        this.candidateBeans = info.getCandidateBeans();
        this.failureReason = info.getFailureReason();
    }

    /* ---------- Getters (JSON serialization) ---------- */

    public String getDeclaringBean() {
        return declaringBean;
    }

    public String getMember() {
        return member;
    }

    public String getRequiredType() {
        return requiredType;
    }

    public Set<String> getQualifiers() {
        return qualifiers;
    }

    public ResolutionStatus getStatus() {
        return status;
    }

    public List<String> getCandidateBeans() {
        return candidateBeans;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
