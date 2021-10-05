import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { DataService } from './data.service';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent {
    title = 'underpayments-ui';

    constructor(private router: Router, public dataService: DataService) { }

    //Navigate to tab
    gotoTab(path: String) {
        this.router.navigate([path])
    }

    ngOnInit(): void {
        this.gotoTab('/overview')
    }

}
