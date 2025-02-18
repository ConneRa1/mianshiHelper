// 通用返回类型
export interface BaseResponse<T> {
  code: number;
  data: T;
  message: string;
}

// 分页返回类型
export interface PageResponse<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
} 