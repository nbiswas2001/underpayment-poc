import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

export class Gql {


    constructor(private http: HttpClient){}

    // --------------------------------------------------------------------
    executeGqlQuery<T>(qryUrl: string, qry: GqlQuery, result: GqlDataHolder<T>) {

        console.log(qry.get())
        this.http
        .post<GqlResponse<T>>(qryUrl, { query: qry.get() })
        .subscribe(resp => {
            if(qry instanceof PageGqlQuery){
                let r = resp.data[qry.qryName] as PgResponse<T>
                result.items = r.items;
                result.totalItems = r.totalItems;
                result.totalPages = r.totalPages;   
                result.isLoaded = true;
            }
            else if(qry instanceof ListGqlQuery){
                let r = resp.data[qry.qryName] as ListResponse<T>
                result.items = r.items;
                result.isLoaded = true;
            }
            else if(qry instanceof ItemGqlQuery) {
                let r = resp.data[qry.qryName] as ItemResponse<T>
                result.item = r.item;
                result.isLoaded = true;
            }
            console.log(result)
        },
        (err) => {
            alert('Query failed')
        });
    }
}

// ===============================
export abstract class GqlQuery {
    private argTxt = '';
    constructor (public qryName: string, private qryTemplate: string, private itemTemplate: string) {}

    private sep(){
        return this.argTxt === '' ? '' : ', ';
    }

    //-------------------------------
    numArg(name: string, value: number) {
        this._arg(name, value)
    }
    boolArg(name: string, value: boolean) {
        this._arg(name, value? 'true':'false')
    }

    private _arg(name: string, value: any){
        this.argTxt += this.sep() +name +': '+value;
        return this;      
    }
    //-------------------------------
    strArg(name: string, value: any) {
        this.argTxt += this.sep() +name +': \"'+value+'\"';
        return this;
    }
    //-------------------------------
    get() {
        const qryTxt = this.qryTemplate
                            .replace('$qry', this.qryName)
                            .replace('$item', this.itemTemplate)
                            .replace('$args', this.argTxt);                    
        return qryTxt;
    }
}


// ---------------------------------------------
export class PageGqlQuery extends GqlQuery {
    constructor (qryName: string, itemTemplate: string, pageNum: number, pageSize: number, filter: string) {
        super(qryName, QRY_PAGED, itemTemplate);
        this.numArg('pageNum', pageNum);
        this.numArg('pageSize', pageSize);
        this.strArg('filter', filter);
    }

}

// ---------------------------------------------
export class ListGqlQuery extends GqlQuery {
    constructor (qryName: string, itemTemplate: string) {
        super(qryName, QRY_LIST, itemTemplate);
    }
}

// ---------------------------------------------
export class ItemGqlQuery extends GqlQuery {
    constructor (qryName: string, itemTemplate: string) {
        super(qryName, QRY_ITEM, itemTemplate);
    }
}

const QRY_PAGED = `
{
    $qry($args) {
        items {
            $item
        }
        totalItems
        totalPages
    }
}`;

const QRY_LIST = `
{
    $qry($args) {
        items {
            $item
        }
    }
}`;

const QRY_ITEM = `
{
    $qry($args) {
        item {
            $item
        }
    }
}`;

// =======================================
export class GqlDataHolder<T> {
    isLoaded = false;
    items: T[] | undefined;
    item: T | undefined;
    totalItems = 0;
    totalPages = 0;
}

export interface GqlResponse<T> {
    data: ResponseData<T>;
    errors: QryError[];
}

export interface ResponseData<T> {
    [index: string]: PgResponse<T> | ListResponse<T> | ItemResponse<T>;
}

//-------------------------
export interface QryError {
    message: string;
}

export interface ListResponse<T> {
    items: T[];
    errors: QryError[];
}

export interface PgResponse<T> extends ListResponse<T> {
    totalItems: number;
    totalPages: number;
}

export interface ItemResponse<T> {
    item: T;
    errors: QryError[];
}
