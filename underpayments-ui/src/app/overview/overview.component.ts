import { Component, OnInit, AfterViewInit } from '@angular/core';
import { DataService } from '../data.service';
import { Observable } from 'rxjs';
import { OverviewRec } from '../model';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { FormatEnums } from '../format-enums';

@Component({
    selector: 'app-overview',
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit {


    constructor(private dataService: DataService, private http: HttpClient) { }

    ngOnInit(): void {}

    overview : OverviewRec | undefined
    fetchingOverview = false

        //----------------------
    fetchOverview() {
        this.fetchingOverview = true;
        this.overview = undefined;
        this.dataService.getOverview().subscribe( result => {
            this.overview = result    
            this.fetchingOverview = false;    
        })
    }

    //-----------------------------
    runJob(jobName: string) {
        this.dataService.runJobRecurse([jobName], 0)
    }
    runJobs(jobNames: string[]) {
        this.dataService.runJobRecurse(jobNames, 0);
    }

    runLoadAcJobs() {
        this.dataService.runJobRecurse(['createAccount', 'calcAccountEligibility', 
        'populateAccount'], 0)
    }

    //----------------------
    fmtE = new FormatEnums()
}
