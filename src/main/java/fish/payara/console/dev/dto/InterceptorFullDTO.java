/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.dto;

import fish.payara.console.dev.model.InterceptorInfo;
import java.util.List;
import fish.payara.console.dev.model.Record;

/**
 *
 * @author Gaurav Gupta
 */
public class InterceptorFullDTO extends InterceptorDTO {

    private List<Record> invocationRecords;

    public InterceptorFullDTO(InterceptorInfo interceptorInfo) {
        super(interceptorInfo);
    }

    public List<Record> getInvocationRecords() {
        return invocationRecords;
    }

    public void setInvocationRecords(List<Record> invocationRecords) {
        this.invocationRecords = invocationRecords;
    }


}
