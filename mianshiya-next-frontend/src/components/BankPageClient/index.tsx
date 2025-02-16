'use client';

import React, { useState } from 'react';
import { Button } from 'antd';
import ChatDialog from "@/components/ChatDialog";

interface BankPageClientProps {
  title: string;
  questionBankId: string;
  firstQuestionId?: string;
}

const BankPageClient: React.FC<BankPageClientProps> = ({
  title,
  questionBankId,
  firstQuestionId,
}) => {
  const [chatOpen, setChatOpen] = useState(false);

  return (
    <>
      <div style={{ display: 'flex', gap: '8px' }}>
        <Button
          type="primary"
          shape="round"
          href={`/bank/${questionBankId}/question/${firstQuestionId}`}
          target="_blank"
          disabled={!firstQuestionId}
        >
          开始刷题
        </Button>
        <Button
          type="default"
          shape="round"
          onClick={() => setChatOpen(true)}
        >
          AI 助手
        </Button>
      </div>
      <ChatDialog 
        open={chatOpen}
        onClose={() => setChatOpen(false)}
        category={title}
      />
    </>
  );
};

export default BankPageClient; 