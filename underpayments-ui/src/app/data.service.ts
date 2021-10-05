import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { environment } from '../environments/environment';
import { OverviewRec, Account } from './model';
import { Gql, PageGqlQuery, GqlDataHolder, ItemGqlQuery } from './gql';

@Injectable({
    providedIn: 'root'
})
export class DataService {

    port = '9007'

    private overviewUrl = environment.serviceUrl+'/mi/overview';
    private accountUrl = environment.serviceUrl+'/queries/account';
    private jobUrl = environment.serviceUrl+'/jobs/run/';
    private narrateUrl = environment.serviceUrl+'/narrate/';

    private gql: Gql;

    constructor(private http: HttpClient) { 
        this.gql = new Gql(http);
    }

    private u(url: string): string{
        return url.replace('<PORT>', this.port);
    }
    // ------------------------------------
    getOverview(): Observable<OverviewRec> {
        return this.http.get<OverviewRec>(this.u(this.overviewUrl))
    }

    // ----------------------------------------------------------------------------
    getAllAccounts(pageNum: number, pageSize: number, filter: string, result: GqlDataHolder<Account>) {

        const qry = new PageGqlQuery('all', ACCOUNT_FULL, pageNum, pageSize, filter);
        this.gql.executeGqlQuery<Account>(this.u(this.accountUrl), qry, result);
    }

    //-------------------------------------------------------------------------
    getAccountByCitizenKey(citizenKey: string, result: GqlDataHolder<Account>) {
        const qry = new ItemGqlQuery('withCitizenKey', ACCOUNT_FULL);
        qry.strArg('citizenKey', citizenKey)
        this.gql.executeGqlQuery<Account>(this.u(this.accountUrl), qry, result);
    }

    //---------------------------------------------------
    runJobRecurse(jobNames: string[], currIdx: number){
        if(currIdx <= jobNames.length -1) {
            let jobName = jobNames[currIdx];
            this.http.get<any>(this.u(this.jobUrl)+jobName)
            .subscribe(
                (resp) => {
                    if(resp.message === 'OK'){
                        this.runJobRecurse(jobNames, currIdx + 1);
                    }
                },
                (err) => {
                    alert("Job failed - "+jobName)
                }
            );
        }
        else {
            alert("All jobs complete")
        }
    }

    //-------------------------------------------------
    getNarration(schema?: string, citizenKey?: string) {

        let headers = new HttpHeaders({
          Accept:'text/html'
        });
    
        if(schema && citizenKey) {
          this.http.get(this.u(this.narrateUrl)+schema+'/'+citizenKey, {headers: headers, responseType: 'blob'})
                .subscribe(
                    (resp) => {
                      console.log(resp)
                      const blob = new Blob([resp], { type: 'text/html' });
                      const url= window.URL.createObjectURL(blob);
                      window.open(url);
                    },
                    (err) => {
                      console.log(err)
                        alert("Failed")
                    }
                );
        }
        else alert("Provide a citizenKey value")
      }
}

const ACCOUNT_FULL =`
            id	
            citizenKey
            dateOfBirth
            dateOfDeath
            isDobVerified
            isDodVerified
            isInternational
            spStartDate
            benefitPayDay
            isPmtInAdvance
            sex
            ageCategory
            spaDate
            relationships {
                citizenKey
                type
                startDate
                endDate
                startVerified
                endVerified
                endReason  
                pkPrsnToPrsn 			
                problems {
                    hasErrors
                    hasWarnings
                    exceptionData
                    errorFlagsData
                    warningFlagsData
                }
            }
            awards {
                startDate
                status
                pkAwcm
                subAwardComponents {
                    ... on PESubAwcm {
                        rawData
                        rate {
                            sacType
                            value
                        }
                    }
                    ... on GmpSubAwcm {
                        rawData
                    }
                    ... on Awcm2AwcmLink {
                        rawData
                    }
                }
                problems {
                    hasErrors
                    hasWarnings
                    exceptionData
                    errorFlagsData
                    warningFlagsData
                }
            }
            circumstances {
                number
                startDate
                endDate
                startEvent
                isMarried
                spRates {
                    sacType
                    value
                }
                pkAwcm
                spouseCircumstance {
                    citizenKey
                    sex
                    isOnSP
                    blQualificationEvent
                    entitlementStartDate
                    benefitStartDate
                    relationshipEndDate
                    relationshipEndReason
                    pkAwcm
                    catARate {
                        value
                    }
                }
                calcResult {
                    code
                    reason
                    needsToClaim
                    underpaidAmount
                }
                entitlementCalcLog {
                    sacType
                    isPartWeek
                    partWeekDays
                    totalWeeks
                    catDAddedAmount
                    totalAmount
                    isComposite
                    compositePctRate
                    entries {
                        startDate
                        endDate
                        rateAmount
                        numWeeks
                        totalAmount
                    }
                }
            }
            stepCompleted
            calcResult {
                code
                reason
                needsToClaim
                underpaidAmount
                analyticsFlags
            }
            problems {
                hasErrors
                hasWarnings
                exceptionData
                errorFlagsData
                warningFlagsData
            }
`