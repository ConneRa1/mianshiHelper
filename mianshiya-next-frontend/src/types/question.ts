export interface QuestionBankQueryRequest {
  current?: number;
  pageSize?: number;
  searchText?: string;
  sortField?: string;
  sortOrder?: string;
} 

export interface QuestionBank {
    id?: number;
    name?: string;
    description?: string;
    userId?: number;
    createTime?: Date;
    updateTime?: Date;
    editTime?: string;
  } 