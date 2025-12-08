/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.dto;

import fish.payara.console.dev.model.DecoratorInfo;
import java.time.Instant;

/**
 *
 * @author Gaurav Gupta
 */
public class DecoratorDTO extends DecoratorInfo {
    
    private int createdCount;
    private Instant lastCreated;
    private int invokedCount;
    private Instant lastInvoked;
    
    public DecoratorDTO(DecoratorInfo decoratorInfo) {
        super(
                decoratorInfo.getClassName(),
                decoratorInfo.getDecoratedTypes(),
                decoratorInfo.getDelegateType(),
                decoratorInfo.getDelegateQualifiers(),
                decoratorInfo.getClassQualifiers(),
                decoratorInfo.getScope()
        );
    }

    public int getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(int createdCount) {
        this.createdCount = createdCount;
    }

    public Instant getLastCreated() {
        return lastCreated;
    }

    public void setLastCreated(Instant lastCreated) {
        this.lastCreated = lastCreated;
    }

    public int getInvokedCount() {
        return invokedCount;
    }

    public void setInvokedCount(int invokedCount) {
        this.invokedCount = invokedCount;
    }

    public Instant getLastInvoked() {
        return lastInvoked;
    }

    public void setLastInvoked(Instant lastInvoked) {
        this.lastInvoked = lastInvoked;
    }


}
