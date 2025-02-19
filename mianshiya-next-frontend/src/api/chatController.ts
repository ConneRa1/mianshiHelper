import { request } from "@/utils/request";

export interface BaseResponse<T> {
  code: number;
  data: T;
  message: string;
}

export interface ChatRequest {
  chatId: number;
  content: string;
  category: string;
}

export interface ChatMessage {
  id: string;
  chatId: string;
  obj: string;
  content: string;
  createTime: string;
  updateTime: string;
}

export interface UserChat {
  chatId: number;
  userId: number;
  title: string;
  category: string;
  createTime: string;
}

export interface ChatHistoryResponse {
  records: ChatMessage[];
  total: number;
  size: number;
  current: number;
}

export interface ChatListResponse {
  records: UserChat[];
  total: number;
  size: number;
  current: number;
}

interface GetRecordsParams {
  category?: string;
  current?: number;
  pageSize?: number;
  
}

// 创建新对话
export function createNewChat(category: string) {
  return request.post<BaseResponse<ChatMessage>>('/api/chat/newChat', {
    category: category
  });
}

// 发送消息
export function sendMessage(data: ChatRequest) {
  return request.post<BaseResponse<string>>('/api/chat/normal-response', data);
}

// 获取历史记录
export function getChatHistory(chatId: number, current: number = 1, pageSize: number = 20) {
  return request.get<BaseResponse<ChatHistoryResponse>>('/api/chat/pagination-records', {
    params: { chatId, current, pageSize }
  });
}

// 获取历史会话列表
export const getRecords = (params: GetRecordsParams = {}) => {
  return request.get<BaseResponse<ChatListResponse>>('/api/chat/getRecords', {
    params
  });
}; 