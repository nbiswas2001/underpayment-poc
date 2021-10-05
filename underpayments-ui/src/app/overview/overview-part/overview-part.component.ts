import { Component, Input, OnInit } from '@angular/core';
import { OverviewRec } from 'src/app/model';

@Component({
  selector: 'app-overview-part',
  templateUrl: './overview-part.component.html'
})
export class OverviewPartComponent implements OnInit {

  constructor() { }

  @Input()
  overview: OverviewRec | undefined

  ngOnInit(): void {
  }

}
