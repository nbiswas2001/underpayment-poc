import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { DataService } from '../data.service';
import { GqlDataHolder } from '../gql';
import { Account, Circumstance, SpAward } from '../model'
import { FormatEnums } from '../format-enums';




@Component({
    selector: 'app-accounts',
    templateUrl: './accounts.component.html',
    styleUrls: ['./accounts.component.scss']
})
export class AccountsComponent implements OnInit {

    private datepipe: DatePipe = new DatePipe('en-GB')
    
    constructor(private dataService: DataService) { }

    d = new GqlDataHolder<Account>();
    accounts: Account[] | undefined;

    filter = "entitled"

    fetching = false;

    ITEMS_PER_PAGE = 100;
    currentPage = 1;

    //------------------
    ngOnInit(): void {
    }

    //----------------------------------
    ok() {
        this.d.isLoaded = false;
        this.fetching = true;
        this.dataService.getAllAccounts(0, this.ITEMS_PER_PAGE, this.filter, this.d);
        this.currentPage = 1;
    }

    //------------------------------
    pageChanged(event: any){
        let pg = event.page - 1;
        this.dataService.getAllAccounts(pg, this.ITEMS_PER_PAGE, this.filter, this.d);
    }

    private dt(dt: Date): string {
        let s = this.datepipe.transform(dt, 'd-M-yyyy')
        return !s? '': s;
    }

    fmtE = new FormatEnums();


}
 
const s = (s:string): string =>  { return s? s: '' ;}
