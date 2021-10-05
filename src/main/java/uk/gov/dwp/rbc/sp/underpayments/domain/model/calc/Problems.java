package uk.gov.dwp.rbc.sp.underpayments.domain.model.calc;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

public class Problems {
    @Getter @Setter
    private Boolean hasErrors = false;

    @Getter @Setter
    private Boolean hasWarnings = false;

    @Getter @Setter
    private String exceptionData;

    @Getter @Setter
    private Integer errorFlagsData = 0;

    @Getter @Setter
    private Integer warningFlagsData = 0;

    @Transient
    private ErrorFlags errors;
    public ErrorFlags errors(){
        if(errors == null) errors = new ErrorFlags(errorFlagsData);
        return errors;
    }

    @Transient
    private WarningFlags warnings;
    public WarningFlags warnings(){
        if(warnings == null) warnings = new WarningFlags(warningFlagsData);
        return warnings;
    }

}
