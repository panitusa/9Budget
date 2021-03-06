import {BudgetTiming} from './budget-timing.model';
import {ITransaction} from './transaction';
import {ICategory} from './category';
import {v4 as uuid} from 'uuid';

export interface IBudget {
  id?: number;
  name?: string;
  amount?: number;
  amountSpent?: number;
  budgetTiming?: BudgetTiming;
  useLeftOver?: boolean;
  active?: boolean;
  accountId?: uuid;
  transactions?: ITransaction[];
  category?: ICategory;
}

export class Budget implements IBudget {
  constructor(
    public id?: number,
    public name?: string,
    public amount?: number,
    public amountSpent?: number,
    public budgetTiming?: BudgetTiming,
    public useLeftOver?: boolean,
    public active?: boolean,
    public accountId?: uuid,
    public transactions?: ITransaction[],
    public category?: ICategory
  ) {
    this.useLeftOver = this.useLeftOver || false;
    this.active = this.active || false;
  }
}
