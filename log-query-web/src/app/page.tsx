'use client';

import { useQuery } from '@apollo/client';
import { useState } from 'react';
import { LogQueryForm } from '@/components/log-query-form';
import { LogTable } from '@/components/log-table';
import { Pagination } from '@/components/pagination';
import { GET_LOGS } from '@/lib/graphql/queries';
import type { LogQueryResponse, LogQueryVariables } from '@/lib/graphql/types';

export default function Home() {
  const [queryVariables, setQueryVariables] = useState<LogQueryVariables>({
    page: 0,
    size: 10,
  });

  const { data, loading, error } = useQuery<
    { logs: LogQueryResponse['logs'] },
    LogQueryVariables
  >(GET_LOGS, {
    variables: queryVariables,
  });

  const handleQuerySubmit = (formData: {
    serviceName: string;
    status: string;
    startTime: string;
    endTime: string;
  }) => {
    setQueryVariables({
      ...queryVariables,
      ...formData,
      startTime: formData.startTime ? new Date(formData.startTime).toISOString() : undefined,
      endTime: formData.endTime ? new Date(formData.endTime).toISOString() : undefined,
      page: 0,
    });
  };

  const handlePageChange = (page: number) => {
    console.log('Page changed to:', page);
    setQueryVariables({
      ...queryVariables,
      page: page - 1,
    });
  };

  const handlePageSizeChange = (size: number) => {
    console.log('Page size changed to:', size);
    setQueryVariables({
      ...queryVariables,
      size,
      page: 0,
    });
  };

  return (
    <main className="container mx-auto py-8 space-y-8">
      <h1 className="text-3xl font-bold">Log Query</h1>
      <LogQueryForm onSubmit={handleQuerySubmit} />
      {loading && <div>Loading...</div>}
      {error && <div className="text-red-500">Error: {error.message}</div>}
      {data?.logs && (
        <>
          <LogTable logs={data.logs.content} />
          <Pagination
            currentPage={data.logs.number + 1}
            totalPages={data.logs.totalPages}
            pageSize={data.logs.size}
            onPageChange={handlePageChange}
            onPageSizeChange={handlePageSizeChange}
          />
        </>
      )}
    </main>
  );
}
