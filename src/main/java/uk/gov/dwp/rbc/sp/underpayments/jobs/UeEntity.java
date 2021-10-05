package uk.gov.dwp.rbc.sp.underpayments.jobs;

import lombok.val;
import org.slf4j.Logger;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.ErrorFlags;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Problems;

import java.util.UUID;
import java.util.function.Consumer;


public interface UeEntity {

    Problems getProblems();
    Logger logger();

    //------------------------------------------------
    default void tryLoad(Consumer<Problems> block) {
        val p = getProblems();
        try {
            block.accept(p);
        }catch (Exception e){
            logException(e, getProblems(), logger(), null);
        }
        if(p.errors().isAtLeastOneSet()) {
            p.setHasErrors(true);
        }
        if(p.warnings().isAtLeastOneSet()){
            p.setHasWarnings(true);
        }
        p.setErrorFlagsData(p.errors().getData());
        p.setWarningFlagsData(p.warnings().getData());
    }

    //-----------------------------------------------------------------------
    static void logException(Exception e,
                             Problems probs,
                             Logger logger,
                             String citizenKey) {
        val exData =  probs.getExceptionData();
        val sb = new StringBuilder(exData == null? "" : exData);
        val st = e.getStackTrace()[0];
        val errId = UUID.randomUUID().toString();
        sb.append(e.getClass().getSimpleName())
                .append("<").append(errId).append(">")
                .append(": ").append(e.getMessage());

        probs.setExceptionData(sb.toString());
        probs.setHasErrors(true);
        probs.errors().set(ErrorFlags.Error.DATA_LOAD_EXCEPTION);

        val sb2 = new StringBuilder();
        sb2.append("Error <").append(errId).append(">");
        if(citizenKey!=null) sb2.append("(").append(citizenKey).append(")");
        logger.error(sb2.toString(), e);
    }
}
