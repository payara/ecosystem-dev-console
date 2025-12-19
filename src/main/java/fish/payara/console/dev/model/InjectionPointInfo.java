/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.model;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Captures CDI injection point metadata together with
 * its resolution outcome for Dev Console diagnostics.
 *
 * @author Gaurav Gupta
 */
public class InjectionPointInfo implements Serializable {

    private final String declaringBeanClass;
    private final String memberSignature;
    private final Type requiredType;
    private final Set<String> qualifiers;

    private ResolutionStatus resolutionStatus = ResolutionStatus.NOT_PROCESSED;
    private List<String> candidateBeans;
    private String failureReason;

    private InjectionPointInfo(
            String declaringBeanClass,
            String memberSignature,
            Type requiredType,
            Set<String> qualifiers) {

        this.declaringBeanClass = declaringBeanClass;
        this.memberSignature = memberSignature;
        this.requiredType = requiredType;
        this.qualifiers = qualifiers;
    }

    /* ---------- Factory ---------- */

    public static InjectionPointInfo from(Bean<?> bean, InjectionPoint ip) {

        String memberSig = memberSignature(ip.getMember());

        Set<String> qualifierNames = ip.getQualifiers().stream()
                .map(a -> "@" + a.annotationType().getSimpleName())
                .collect(Collectors.toSet());

        return new InjectionPointInfo(
                bean.getBeanClass().getName(),
                memberSig,
                ip.getType(),
                qualifierNames
        );
    }

    private static String memberSignature(Member member) {
        if (member == null) {
            return "<unknown>";
        }
        return member.getDeclaringClass().getName() + "#" + member.getName();
    }

    /* ---------- Setters (requested) ---------- */

    public void setResolutionStatus(ResolutionStatus resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    public void setCandidateBeans(List<String> candidateBeans) {
        this.candidateBeans = candidateBeans;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    /* ---------- Optional helper methods ---------- */

    public void markResolved(String beanClassName) {
        this.resolutionStatus = ResolutionStatus.RESOLVED;
        this.candidateBeans = List.of(beanClassName);
    }

    public void markAmbiguous(List<String> candidates) {
        this.resolutionStatus = ResolutionStatus.AMBIGUOUS;
        this.candidateBeans = candidates;
    }

    public void markUnsatisfied(String reason) {
        this.resolutionStatus = ResolutionStatus.UNSATISFIED;
        this.failureReason = reason;
    }

    /* ---------- Getters ---------- */

    public String getDeclaringBeanClass() {
        return declaringBeanClass;
    }

    public String getMemberSignature() {
        return memberSignature;
    }

    public Type getRequiredType() {
        return requiredType;
    }

    public Set<String> getQualifiers() {
        return qualifiers;
    }

    public ResolutionStatus getResolutionStatus() {
        return resolutionStatus;
    }

    public List<String> getCandidateBeans() {
        return candidateBeans;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
