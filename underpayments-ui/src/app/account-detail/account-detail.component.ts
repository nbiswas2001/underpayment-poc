import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { environment } from 'src/environments/environment';
import { DataService } from '../data.service';
import { GqlDataHolder } from '../gql';
import { Account, SubAwardComponent } from '../model';

@Component({
  selector: 'app-account-detail',
  templateUrl: './account-detail.component.html',
  styleUrls: ['./account-detail.component.scss']
})
export class AccountDetailComponent implements OnInit {

  constructor(public dataService: DataService, private http: HttpClient) { }

  d = new GqlDataHolder<Account>();
  
  citizenKey?: string = "BN763159"
  schema?: string = "PSL1"

  ngOnInit(): void {
  }

  formatSac(sac: SubAwardComponent): string {
    if(sac.rate && 
      (sac.rate.sacType == 'CAT_A_BASIC'
      || sac.rate.sacType == 'CAT_D_BASIC'
      || sac.rate.sacType == 'CAT_BL_BASIC'
      )){
      return sac.rate.sacType+"("+sac.rate.value+")"
    }
    else return " "
  }

  getNarration(){
    this.dataService.getNarration(this.schema, this.citizenKey);
  }  


  getAccount() {
    if(this.citizenKey) {
     this.dataService.getAccountByCitizenKey(this.citizenKey!, this.d);
    }
    else alert("Provide a value")
  }

  formatAmt(amt?: number): string {
    return amt? '£ '+amt/100 : ''
  }  

  formatErrors(data?: number): string {
    let errs = ""
    if(data) {
      for(let i =0; i < this.errorFlags.length; i++){
        let new_data = data >> (i - 1);
        if(new_data & 1) errs = errs + this.errorFlags[i] + ", " 
      }
    }
    return errs
  }

  errorFlags = [
    'PRSN_INVALID_SEX',
    'PRSNTOPRSN_SPOUSE_NINO_MISSING',
    'PRSNTOPRSN_MISSING_RELN_START_DATE',
    'PRSNTOPRSN_INVALID_RELN_START_DATE',
    'PRSNTOPRSN_INVALID_RELN_END_DATE',
    'PRSNTOPRSN_DUPLICATE_RELN',
    'RELN_SPOUSE_AC_MISSING',
    'AWCM_NO_SACS',
    'AC_RELN_HAS_ERRORS',
    'AC_AWARD_HAS_ERRORS',
    'DATA_LOAD_EXCEPTION',
    'EXCEPTION_CALC_AC_ELIGIBILITY',
    'EXCEPTION_GENERATE_CIRCS',
    'EXCEPTION_CALC_CIRCS_ELIGIBILITY',
    'EXCEPTION_CALC_ENTITLEMENT'
  ];

  formatWarnings(data?: number): string {
    let warns = ""
    if(data) {
      for(let i =0; i < this.warningFlags.length; i++){
        let new_data = data >> (i - 1);
        if(new_data & 1) warns = warns + this.warningFlags[i] + ", " 
      }
    }
    return warns
  }

  warningFlags = [
    'PRSN_COUNTRY_CODE_MISSING',
    'PRSN_ADDRESS_MISSING',
    'PRSN_ADDRESS_MALFORMED',
    'PRSN_NAME_MALFORMED',
    'PRSN_FIRST_NAME_MISSING',
    'PRSN_SURNAME_MISSING',
    'PRSN_UNVERIFIED_DOB',
    'PRSN_UNVERIFIED_DOD',
    'AWCM_SAC_BYTE_COUNT_MISMATCH',
    'AWCM_INVALID_START_DATE'
  ];

}
