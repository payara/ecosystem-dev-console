package fish.payara.console.dev.cdi.dto;

import jakarta.enterprise.inject.spi.ObserverMethod;

public class ObserverDTO {
    private String eventTypeName;
    private String beanClass;
    private String reception;
    private String transactionPhase;

    public ObserverDTO(String eventTypeName, String beanClass, String reception, String transactionPhase) {
        this.eventTypeName = eventTypeName;
        this.beanClass = beanClass;
        this.reception = reception;
        this.transactionPhase = transactionPhase;
    }

    // New constructor using ObserverMethod
    public ObserverDTO(ObserverMethod<?> observerMethod) {
        this.eventTypeName = observerMethod.getObservedType().getTypeName();
        this.beanClass = observerMethod.getBeanClass().getName();
        this.reception = observerMethod.getReception().name();
        this.transactionPhase = observerMethod.getTransactionPhase().name();
    }

    public String getEventTypeName() {
        return eventTypeName;
    }

    public void setEventTypeName(String eventTypeName) {
        this.eventTypeName = eventTypeName;
    }

    public String getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(String beanClass) {
        this.beanClass = beanClass;
    }

    public String getReception() {
        return reception;
    }

    public void setReception(String reception) {
        this.reception = reception;
    }

    public String getTransactionPhase() {
        return transactionPhase;
    }

    public void setTransactionPhase(String transactionPhase) {
        this.transactionPhase = transactionPhase;
    }
}
