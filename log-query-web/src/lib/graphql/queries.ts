import { gql } from '@apollo/client';

export const GET_LOGS = gql`
  query GetLogs(
    $serviceName: String
    $status: String
    $responseStatus: String
    $startTime: String
    $endTime: String
    $page: Int = 0
    $size: Int = 10
  ) {
    logs(
      serviceName: $serviceName
      status: $status
      responseStatus: $responseStatus
      startTime: $startTime
      endTime: $endTime
      page: $page
      size: $size
    ) {
      content {
        id
        serviceName
        instanceId
        processId
        requestTime
        status
        errorMessage
        response
        responseMessage
        responseStatus
      }
      totalElements
      number
      size
      totalPages
    }
  }
`;

export const GET_UNIQUE_SERVICE_NAMES = gql`
  query GetUniqueServiceNames {
    uniqueServiceNames
  }
`;
