import React from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { LogEntry } from '@/lib/graphql/types';

interface LogTableProps {
  logs: LogEntry[];
}

export function LogTable({ logs }: LogTableProps) {
  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Service Name</TableHead>
            <TableHead>Instance ID</TableHead>
            <TableHead>Process ID</TableHead>
            <TableHead>Request Time</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Error Message</TableHead>
            <TableHead>Response Message</TableHead>
            <TableHead>Response Status</TableHead>
            <TableHead>Response</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {logs.map((log) => (
            <TableRow key={log.id}>
              <TableCell>{log.serviceName}</TableCell>
              <TableCell>{log.instanceId}</TableCell>
              <TableCell>{log.processId}</TableCell>
              <TableCell>{new Date(log.requestTime).toLocaleString()}</TableCell>
              <TableCell>{log.status}</TableCell>
              <TableCell>{log.errorMessage}</TableCell>
              <TableCell>{log.responseMessage}</TableCell>
              <TableCell>{log.responseStatus}</TableCell>
              <TableCell className="max-w-md truncate">
                {log.response}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
