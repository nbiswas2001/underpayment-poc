//--------- MI ----------------
export interface OverviewRec {
    name: string;
    count: number;
    components: OverviewRec[];
}

//--------------------------
export interface Account {
    id: string;
    citizenKey: string;
    dateOfBirth: Date
    dateOfDeath: Date
    isDobVerified: boolean
    isDodVerified: boolean
    isInternational: boolean
    sex: string;
    ageCategory: string;
    spaDate: Date;
    spStartDate: Date;
    benefitPayDay: number;
    isPmtInAdvance: boolean;
    awards: SpAward[];
    relationships: Relationship[];
    circumstances: Circumstance[];
    stage: string;
    stepCompleted: string;
    calcResult: CalcResult;
    problems: Problems
}

export interface SpAward {

    startDate: Date
    status: string
    pkAwcm: number
    subAwardComponents: SubAwardComponent[]
    problems: Problems
}

export interface SubAwardComponent {
    rate: Rate;
}

export interface CalcResult {
    code : string;
    reason: string;
    needsToClaim: boolean;
    underpaidAmount: number;
}

export interface Relationship {
    citizenKey: string;
    startDate: Date;
    endDate: Date;
    pkPrsnToPrsn: number;
    problems: Problems;
}

export interface Circumstance {
    number: number;
    startDate: Date;
    endDate: Date;
    startEvent: string;
    isMarried: boolean;
    spRates: Rate[];
    spouseCircumstance: SpouseCircumstance;
    calcResult: CalcResult
    entitlementCalcLog: EntitlementCalcLog
}

export interface SpouseCircumstance {
    citizenKey: string;
    sex: string;
    isOnSP: boolean;
    entitlementStartDate: Date;
    benefitStartDate: Date;
    relationshipEndDate: Date;
    relationshipEndReason: string;
    catARate: Rate;
}

export interface EntitlementCalcLog {
    sacType: string
    isPartWeek: boolean
    partWeekDays: number
    totalWeeks: number
    catDAddedAmount: number
    totalAmount: number
    isComposite: boolean
    compositePctRate: number
    entries: EntitlementCalcEntry[]
}

export interface EntitlementCalcEntry {
    startDate: Date
    endDate: Date
    rateAmount: number
    numWeeks: number
    totalAmount: number
}

export interface Rate {
    sacType: string
    value: string
}

export interface Problems {
    hasErrors: boolean
    hasWarnings: boolean
    exceptionData: string
    errorFlagsData: number
    warningFlagsData: number
}