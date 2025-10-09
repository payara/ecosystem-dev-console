package fish.payara.console.dev.cdi.dto;

/**
 * Bean Data Transfer Object to represent beans in a structured way for API responses.
 */
public class BeanDTO {
    private String id;
    private String beanType;
    private String description;
    private String scope;

    public BeanDTO(Object bean) {
        
        this.description = bean.toString();
        this.beanType = bean.getClass().getName();
//        // Convert bean to a string description similar to the current toString output
//        if(bean instanceof org.jboss.weld.bean.ManagedBean) {
//                description = ((org.jboss.weld.bean.ManagedBean)bean).getBeanClass().getTypeName();
//                scope = ((org.jboss.weld.bean.ManagedBean)bean).getScope().getTypeName();
//               for (Object qualifier : ((org.jboss.weld.bean.ManagedBean) bean).getQualifiers()) {
//                   if(qualifier instanceof jakarta.enterprise.inject.Any || qualifier instanceof jakarta.enterprise.inject.Default) {
//                       
//                   } else {
//                                          System.out.println("");
//
//                   }
//            }
//                if(!((org.jboss.weld.bean.ManagedBean) bean).getDecorators().isEmpty()) {
//                    System.out.println("");
//                }
//                    if(!((org.jboss.weld.bean.ManagedBean) bean).getInjectionPoints().isEmpty()) {
//                    System.out.println("");
//                } 
//                    if(!((org.jboss.weld.bean.ManagedBean) bean).getStereotypes().isEmpty()) {
//                    System.out.println("");
//                }
//                    id = ((org.jboss.weld.bean.ManagedBean) bean).getId();
//                    System.out.println("");
//        } else if(bean instanceof org.jboss.weld.bean.builtin.ContextBean) {
//            
////                description = ((org.jboss.weld.bean.ManagedBean)bean).getBeanClass().getTypeName();
////                description = ((org.jboss.weld.bean.builtin.ContextBean)bean).getTypes().getTypeName();
//            System.out.println("");
//        } else if(bean instanceof org.jboss.weld.bean.builtin.InterceptedBeanMetadataBean) {
//            System.out.println("");
//        } else if(bean instanceof org.jboss.weld.bean.ProducerMethod) {
//            System.out.println("");
//        } else if(bean instanceof org.jboss.weld.module.web.ServletContextBean) {
//            System.out.println("");
//        } else if(bean instanceof org.jboss.weld.module.web.HttpSessionBean) {
//            System.out.println("");
//        } else if(bean instanceof org.jboss.weld.bean.builtin.ConversationBean) {
//            System.out.println("");
//        } else if(bean instanceof org.jboss.weld.module.web.HttpServletRequestBean) {
//            System.out.println("");
//        } else if(bean instanceof org.jboss.weld.bean.builtin.RequestContextControllerBean) {
//            System.out.println("");
//        } else if(bean instanceof org.jboss.weld.bean.builtin.ExtensionBean) {
//            description = ((org.jboss.weld.bean.builtin.ExtensionBean)bean).getBeanClass().getTypeName();
//        } else if(bean instanceof org.jboss.weld.bean.builtin.DecoratorMetadataBean) {
//            System.out.println("");
//        } else if(bean instanceof org.jboss.weld.bean.builtin.EventBean) {
//            System.out.println("");
//        } else if(bean instanceof org.jboss.weld.bean.builtin.EventMetadataBean) {
//            System.out.println("");
//        } else {
//            System.out.println(bean.getClass());
//        }
    }

    public String getBeanType() {
        return beanType;
    }

    public void setBeanType(String beanType) {
        this.beanType = beanType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
