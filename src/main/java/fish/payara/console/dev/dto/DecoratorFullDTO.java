/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.dto;

import fish.payara.console.dev.model.DecoratorInfo;
import java.util.List;
import fish.payara.console.dev.model.Record;

/**
 *
 * @author Gaurav Gupta
 */
public class DecoratorFullDTO extends DecoratorDTO {

    private List<Record> invocationRecords;

    public DecoratorFullDTO(DecoratorInfo decoratorInfo) {
        super(decoratorInfo);
    }

    public List<Record> getInvocationRecords() {
        return invocationRecords;
    }

    public void setInvocationRecords(List<Record> invocationRecords) {
        this.invocationRecords = invocationRecords;
    }


}
