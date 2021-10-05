package uk.gov.dwp.rbc.sp.underpayments.domain.etl.awcm;

import lombok.val;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.awcm.R2508AWCM;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.*;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.ErrorFlags;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Problems;
import uk.gov.dwp.rbc.sp.underpayments.utils.PscsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.WarningFlags.Warning.AWCM_SAC_BYTE_COUNT_MISMATCH;

public class SubAWCM_Processor {

    public List<SubAwardComponent> process(R2508AWCM awcm, Problems awProbs) {


        List<SubAwardComponent> result = new ArrayList<>();

        val calculatedBytes = awcm.D2508_SUB_AW_CM_CNT * 22 + awcm.D2508_GMP_SCM_CNT * 37 + awcm.D2508_AWCMTOAWCM_CNT * 17;

        if(!awcm.D2508_AWCM_D01_17.isBlank() && calculatedBytes> 0) {

            if(calculatedBytes != awcm.D2508_AWCM_BYTES_CNT) {
                awProbs.warnings().set(AWCM_SAC_BYTE_COUNT_MISMATCH);
            }

            var ptr = 0;
            //PEs
            for (int i = 0; i < awcm.D2508_SUB_AW_CM_CNT; i++) {
                val data = awcm.D2508_AWCM_D01_17.substring(ptr, ptr + 22 * 2);
                loadSubAwardComponent(new SpSubAwardComponent(), data, result);
                ptr += 22 * 2;

            }
            //GMPs
            for (int i = 0; i < awcm.D2508_GMP_SCM_CNT; i++) {
                val data = awcm.D2508_AWCM_D01_17.substring(ptr, ptr + 37 * 2);
                loadSubAwardComponent(new GmpSubAwcm(), data, result);
                ptr += 37 * 2;
            }
            // Links
            for (int i = 0; i < awcm.D2508_AWCMTOAWCM_CNT; i++) {
                val data = awcm.D2508_AWCM_D01_17.substring(ptr, ptr + 17 * 2);
                loadSubAwardComponent(new Awcm2AwcmLink(), data, result);
                ptr += 17 * 2;
            }
        }

        if(result.isEmpty()){
            awProbs.errors().set(ErrorFlags.Error.AWCM_NO_SACS);
        }

        return result;
    }

    //------------------------------------------------------------------
    private void loadSubAwardComponent(SpSubAwardComponent sac,
                                       String data,
                                       List<SubAwardComponent> result){
        sac.setRawData(data);

        //PE Subtype
        val peSacCode = PscsUtils.intData(data,0,2);
        val peSacTypeOpt = Arrays.stream(SacType.values()).filter(st->st.getCode()==peSacCode).findFirst();

        //Load only SP subtypes
        if(!peSacTypeOpt.isPresent()) return;

        //Flag 1
        sac.setFlag1(PscsUtils.intData(data, 2, 1));

        //Fraction indicator
        val fractionIndicator = PscsUtils.intData(data, 3, 1);
        val spRate = new Rate();
        spRate.setSacType(peSacTypeOpt.get());
        //Rate
        if (fractionIndicator == 0) {
            spRate.setPercentRate(PscsUtils.intData(data, 4, 2));
        } else if(fractionIndicator == 9) {
            val f = new Fraction();
            f.setNumerator(PscsUtils.intData(data, 4, 1));
            f.setDenominator(PscsUtils.intData(data, 5, 1));
            spRate.setFraction(f);
        }
        sac.setRate(spRate);

        result.add(sac);

    }
    //-----------------------------------------------------------------------------
    private void loadSubAwardComponent(GmpSubAwcm subComp,
                                       String data,
                                       List<SubAwardComponent> result){
        subComp.setRawData(data);
        result.add(subComp);
    }
    //-----------------------------------------------------------------------------
    private void loadSubAwardComponent(Awcm2AwcmLink subComp,
                                       String data,
                                       List<SubAwardComponent> result){

        subComp.setRawData(data);
        result.add(subComp);
    }

}
