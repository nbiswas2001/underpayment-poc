import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { OverviewComponent } from './overview/overview.component'
import { AccountsComponent } from './accounts/accounts.component'
import { AccountDetailComponent } from './account-detail/account-detail.component';

const routes: Routes = [
    { path: 'overview', component: OverviewComponent },
    { path: 'accounts', component: AccountsComponent },
    { path: 'account-detail', component: AccountDetailComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
