'use client';

import { Pagination } from 'antd';
import { useRouter, useSearchParams } from 'next/navigation';

interface QuestionBankPaginationProps {
  current: number;
  total: number;
  pageSize: number;
}

export default function QuestionBankPagination({ current, total, pageSize }: QuestionBankPaginationProps) {
  const router = useRouter();
  const searchParams = useSearchParams();

  return (
    <div style={{ padding: "20px", textAlign: "center" }}>
      <Pagination 
        current={current}
        total={total}
        pageSize={pageSize}
        onChange={(page) => {
          const params = new URLSearchParams(searchParams.toString());
          params.set('page', page.toString());
          router.replace(`?${params.toString()}`, { scroll: false });
        }}
        size="small"
        showSizeChanger={false}
      />
    </div>
  );
} 