export class FormatEnums {

    step(e: string): string {
        switch (e) {
            case 'LOADING_DATA': return 'Loading Data ...';
            case 'CALC_AC_ELIGIBILITY': return 'Calc Elig (Account)';
            case 'GENERATE_CIRCS': return 'Generate circs';
            case 'CALC_CIRCS_ELIGIBILITY': return 'Calc Elig (Circs)';
            case 'CALC_ENTITLEMENT': return 'Calc Entitlement';
            default: return 'Unknown ('+e+')';
        }        
    }

    result(e: string): string {
        switch (e) {
            case 'NEW': return 'New';
            case 'DATA_ERROR': return 'Data Error';
            case 'MAYBE_ELIGIBLE': return 'Maybe eligible';
            case 'ELIGIBLE': return 'Eligible';
            case 'INELIGIBLE': return 'Ineligible';
            case 'ENTITLED': return 'Entitled';
            case 'TOO_COMPLEX': return 'Too complex';
            default: return 'Unknown ('+e+')';
        }        
    }

    reason(e: string): string {
        switch(e){
            case 'NONE': return 'None';
            case 'NOT_REACHED_SPA': return 'Not SPA yet';
            case 'MALE_PRE_2010_SPA_DATE': return 'M, pre 2010';
            case 'NEVER_MARRIED': return 'Never married';
            case 'ON_MAX_AWARD': return 'On max award';
            case 'NO_ELIGIBLE_CIRCS': return 'No elig circs';
            case 'NOT_MARRIED': return 'Not married';
            case 'INVALID_SPOUSE_SEX': return 'Invalid Sps sex';
            case 'NO_SPOUSE_CAT_A': return '0 Sps CatA';
            case 'SPOUSE_CAT_A_BELOW_MIN': return 'Low Sps CatA';
            case 'MULTIPLE_MARRIAGES': return 'Multiple marriages';
            case 'ON_NEW_SP': return 'on new SP'
            case 'CAT_D': return 'Cat D';
            case 'CAT_BL': return 'Cat BL';
            case 'CAT_ABL': return 'Cat ABL';
            default: return 'Unknown ('+e+')';
        }
    }

    startEvent(e: string): string {
        switch(e){
            case 'CLAIM': return 'Claim';
            case 'MARRIAGE': return 'Marriage';
            case 'SPOUSE_SP': return 'Spouse on SP';
            case 'REACHED_80': return 'Reached 80';
            default: return 'Unknown ('+e+')';
        } 
    }

    ageCat(e: string): string {
        switch(e) {
            case 'UNDER_SPA': return '< SPA';
            case 'SPA': return 'SPA';
            case 'OVER_80': return '80+';
            case 'DECEASED': return 'Dead';
            default: return 'Unknown ('+e+')';
        }
    }

}