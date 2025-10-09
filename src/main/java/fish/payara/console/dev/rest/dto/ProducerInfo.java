package fish.payara.console.dev.rest.dto;

import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.BeanManager;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Collectors;

public class ProducerInfo {

    public enum Kind {
        FIELD,
        METHOD
    }

    private final String producerClass;   // class where @Produces is declared
    private final String memberSignature; // field or method signature without class name
    private final String producedType;    // type as String
    private final Kind kind;              // FIELD or METHOD

    public ProducerInfo(AnnotatedMember<?> member, Type producedType, Kind kind, BeanManager bm) {
        Member javaMember = member.getJavaMember();
        this.producerClass = javaMember.getDeclaringClass().getName();
        this.producedType = producedType.getTypeName(); // store as String for JSON

        // Remove class name prefix from member signature
        String fullSignature = javaMember.toString();
        String classNamePrefix = javaMember.getDeclaringClass().getName() + ".";
        if (fullSignature.contains(classNamePrefix)) {
            this.memberSignature = simplifySignature(fullSignature.replace(classNamePrefix, ""));
        } else {
            this.memberSignature = simplifySignature(fullSignature);
        }
        this.kind = kind;
    }

    static String simplifySignature(String signature) {
        int parenIndex = signature.indexOf("(");

        String beforeParams;
        String params = "";

        if (parenIndex != -1) {
            beforeParams = signature.substring(0, parenIndex).trim();
            params = signature.substring(parenIndex + 1, signature.length() - 1).trim();
        } else {
            // no params case (e.g. field or no-arg method without parentheses)
            beforeParams = signature.trim();
        }

        // split into parts
        String[] parts = beforeParams.split("\\s+");
        StringBuilder simplified = new StringBuilder();

        // modifiers + return type
        for (int i = 0; i < parts.length - 1; i++) {
            simplified.append(simpleName(parts[i])).append(" ");
        }

        // method name or field name
        simplified.append(parts[parts.length - 1]);

        // only add () if it’s a method
        if (parenIndex != -1) {
            simplified.append("(");
            if (!params.isEmpty()) {
                String[] paramList = params.split(",");
                for (int i = 0; i < paramList.length; i++) {
                    simplified.append(simpleName(paramList[i].trim()));
                    if (i < paramList.length - 1) {
                        simplified.append(", ");
                    }
                }
            }
            simplified.append(")");
        }

        return simplified.toString().trim();
    }

    private static String simpleName(String fqcn) {
        fqcn = fqcn.trim();
        int idx = fqcn.lastIndexOf('.');
        return (idx == -1) ? fqcn : fqcn.substring(idx + 1);
    }

    public String getProducerClass() {
        return producerClass;
    }

    public String getMemberSignature() {
        return memberSignature;
    }

    public String getProducedType() {
        return producedType;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public String toString() {
        return "ProducerInfo{"
                + "producerClass='" + producerClass + '\''
                + ", memberSignature='" + memberSignature + '\''
                + ", producedType='" + producedType + '\''
                + ", kind=" + kind
                + '}';
    }
}
