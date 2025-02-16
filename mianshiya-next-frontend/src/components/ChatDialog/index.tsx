import React, { useState, useEffect } from 'react';
import { Modal, Input, Button, List, message, Layout, Menu } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';
import { createNewChat, sendMessage, getChatHistory, getRecords, ChatMessage, UserChat } from '@/api/chatController';
import { Components } from 'react-markdown';
import './index.css';

const { Sider, Content } = Layout;

interface ChatDialogProps {
  open: boolean;
  onClose: () => void;
  category: string;
}

// 添加类型定义
interface MarkdownComponents extends Components {
  p: React.FC<{ children?: React.ReactNode }>;
  pre: React.FC<{ children?: React.ReactNode }>;
  code: React.FC<{ inline?: boolean; children?: React.ReactNode }>;
  ul: React.FC<{ children?: React.ReactNode }>;
  ol: React.FC<{ children?: React.ReactNode }>;
  blockquote: React.FC<{ children?: React.ReactNode }>;
  strong: React.FC<{ children?: React.ReactNode }>;
}

// 修改消息角色类型常量
const USER_ROLE = 'Human';
const AI_ROLE = 'AI';

const ChatDialog: React.FC<ChatDialogProps> = ({ open, onClose, category }) => {
  const [chatId, setChatId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputValue, setInputValue] = useState('');
  const [loading, setLoading] = useState(false);
  const [sendingMessage, setSendingMessage] = useState('');
  const [chatList, setChatList] = useState<UserChat[]>([]);

  // 获取历史会话列表
  const loadChatList = async () => {
    try {
      const response = await getRecords({
        category: category  // 添加 category 参数
      });
      if (response?.code === 0 && response?.data) {
        setChatList(response.data.records);
      }
    } catch (error) {
      console.error('获取历史会话失败:', error);
    }
  };

  useEffect(() => {
    if (open) {
      loadChatList();
      if (!chatId) {
        initChat();
      }
    }
  }, [open]);

  const initChat = async () => {
    try {
      const response = await createNewChat(category);
      if (response?.code === 0 && response?.data) {
        // 从返回的 ChatMessage 中获取 chatId
        const chatId = Number(response.data.chatId);
        setChatId(chatId);
        // 直接使用返回的欢迎消息初始化对话
        setMessages([{
          id: response.data.id,
          chatId: response.data.chatId,
          obj: response.data.obj,
          content: response.data.content,
          createTime: response.data.createTime,
          updateTime: response.data.updateTime
        }]);
        // 重新加载聊天列表
        loadChatList();
      } else {
        throw new Error('创建对话失败: ' + response?.message || '未知错误');
      }
    } catch (error) {
      console.error('创建对话失败:', error);
      message.error('创建对话失败，请重试');
      onClose();
    }
  };

  const loadHistory = async (id: number) => {
    try {
      const response = await getChatHistory(id);
      if (response?.code === 0 && response?.data) {
        setMessages(response.data.records);
      } else {
        throw new Error('获取历史记录失败: ' + response?.message || '未知错误');
      }
    } catch (error) {
      console.error('获取历史记录失败:', error);
      message.error('获取历史记录失败');
    }
  };

  const handleSend = async () => {
    if (!inputValue.trim() || !chatId) return;
    
    setLoading(true);
    setSendingMessage(inputValue);
    
    try {
      const userMessage: ChatMessage = {
        id: Date.now().toString(),
        chatId: chatId.toString(),
        obj: USER_ROLE,
        content: inputValue,
        createTime: new Date().toISOString(),
        updateTime: new Date().toISOString()
      };
      
      setMessages(prev => [...prev, userMessage]);
      setInputValue('');
      
      const response = await sendMessage({
        chatId,
        content: inputValue,
        category: category
      });
      
      if (response?.code === 0 && response?.data) {
        const aiMessage: ChatMessage = {
          id: (Date.now() + 1).toString(),
          chatId: chatId.toString(),
          obj: AI_ROLE,
          content: response.data,
          createTime: new Date().toISOString(),
          updateTime: new Date().toISOString()
        };
        
        setMessages(prev => [...prev, aiMessage]);
      } else {
        throw new Error('发送消息失败: ' + response?.message || '未知错误');
      }
    } catch (error: any) {
      console.error('发送消息失败:', error);
      message.error(error.message || '发送消息失败，请重试');
      setInputValue(sendingMessage);
    } finally {
      setLoading(false);
      setSendingMessage('');
    }
  };

  const handleSelectChat = async (selectedChatId: number) => {
    setChatId(selectedChatId);
    await loadHistory(selectedChatId);
  };

  const renderMessage = (content: string, obj: string) => {
    if (obj === USER_ROLE) {
      return content;
    }
    return (
      <div className="chat-markdown">
        <ReactMarkdown
          remarkPlugins={[remarkGfm]}
          rehypePlugins={[rehypeRaw]}
          components={{
            p: ({ children }) => <p style={{ margin: '8px 0' }}>{children}</p>,
            pre: ({ children }) => (
              <pre style={{ 
                background: '#f6f8fa', 
                padding: '12px',
                borderRadius: '4px',
                overflowX: 'auto',
                whiteSpace: 'pre-wrap',
                wordBreak: 'break-word'
              }}>
                {children}
              </pre>
            ),
            code: ({ inline, children }) => (
              inline ? (
                <code style={{ 
                  background: '#f6f8fa',
                  padding: '2px 4px',
                  borderRadius: '3px',
                  fontSize: '0.9em'
                }}>
                  {children}
                </code>
              ) : (
                <code style={{ 
                  display: 'block',
                  fontSize: '0.9em',
                  fontFamily: 'monospace'
                }}>
                  {children}
                </code>
              )
            ),
            ul: ({ children }) => (
              <ul style={{ paddingLeft: '20px', margin: '8px 0' }}>
                {children}
              </ul>
            ),
            ol: ({ children }) => (
              <ol style={{ paddingLeft: '20px', margin: '8px 0' }}>
                {children}
              </ol>
            ),
            blockquote: ({ children }) => (
              <blockquote style={{
                borderLeft: '4px solid #ddd',
                paddingLeft: '16px',
                margin: '8px 0',
                color: '#666'
              }}>
                {children}
              </blockquote>
            ),
            strong: ({ children }) => (
              <strong style={{ fontWeight: 600 }}>
                {children}
              </strong>
            )
          } as MarkdownComponents}
        >
          {content}
        </ReactMarkdown>
      </div>
    );
  };

  return (
    <Modal
      title="AI 面试官"
      open={open}
      onCancel={onClose}
      footer={null}
      className="chat-dialog-modal"
    >
      <Layout style={{ height: '100%' }}>
        <Sider width={280} className="chat-list-sider">
          <div className="new-chat-button-container">
            <Button 
              type="primary" 
              icon={<PlusOutlined />} 
              onClick={initChat}
              style={{ width: '100%' }}
            >
              新对话
            </Button>
          </div>
          <div style={{ 
            flex: 1, 
            overflowY: 'auto',
            overflowX: 'hidden'
          }}>
            <Menu
              mode="inline"
              selectedKeys={chatId ? [chatId.toString()] : []}
              style={{ 
                borderRight: 'none',
                height: '100%'
              }}
              className="chat-list-menu"
            >
              {chatList.map((chat) => (
                <Menu.Item 
                  key={chat.chatId}
                  onClick={() => handleSelectChat(chat.chatId)}
                  style={{ 
                    height: 'auto',
                    padding: '8px 16px',
                    whiteSpace: 'normal',
                    lineHeight: '1.5'
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 500 }}>{chat.title}</div>
                    <div style={{ fontSize: '12px', color: '#999' }}>
                      {new Date(chat.createTime).toLocaleString()}
                    </div>
                  </div>
                </Menu.Item>
              ))}
            </Menu>
          </div>
        </Sider>
        <Content className="chat-content-area">
          <div className="messages-container">
            <List
              dataSource={messages}
              renderItem={(msg) => (
                <List.Item className="message-item">
                  <div className={`message-bubble ${msg.obj === USER_ROLE ? 'user-message' : 'ai-message'}`}>
                    {renderMessage(msg.content, msg.obj)}
                    {loading && msg === messages[messages.length - 1] && msg.obj === USER_ROLE && (
                      <div style={{ 
                        fontSize: '12px', 
                        color: '#999', 
                        marginTop: '8px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px'
                      }}>
                        <span>AI正在思考</span>
                        <span className="loading-dots">...</span>
                      </div>
                    )}
                  </div>
                </List.Item>
              )}
            />
          </div>
          <div className="chat-input-area">
            <Input
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onPressEnter={!loading ? handleSend : undefined}
              placeholder="输入您的问题..."
              disabled={loading || !chatId}
            />
            <Button 
              type="primary" 
              onClick={handleSend} 
              loading={loading}
              disabled={!inputValue.trim() || loading || !chatId}
            >
              发送
            </Button>
          </div>
        </Content>
      </Layout>
    </Modal>
  );
};

export default ChatDialog; 