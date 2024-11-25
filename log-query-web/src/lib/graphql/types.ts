export interface PingLog {
  id: string;
  serviceName: string;
  instanceId: string;
  processId: string;
  requestTime: string;
  status: string;
  errorMessage?: string;
  response?: string;
  responseMessage?: string;
  responseStatus?: string;
}

export interface LogQueryResponse {
  logs: {
    content: PingLog[];
    totalElements: number;
    number: number;
    size: number;
    totalPages: number;
  };
}

export interface LogQueryVariables {
  serviceName?: string;
  status?: string;
  startTime?: string;
  endTime?: string;
  page?: number;
  size?: number;
}

export interface UniqueServiceNamesResponse {
  uniqueServiceNames: string[];
}
