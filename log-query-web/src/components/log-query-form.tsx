import React from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { useQuery } from "@apollo/client";
import { GET_UNIQUE_SERVICE_NAMES } from "@/lib/graphql/queries";
import { UniqueServiceNamesResponse } from "@/lib/graphql/types";

interface LogQueryFormProps {
  onSubmit: (data: {
    serviceName: string;
    status: string;
    responseStatus: string;
    startTime: string;
    endTime: string;
  }) => void;
}

const defaultFormData = {
  serviceName: 'ALL',
  status: 'ALL',
  responseStatus: 'ALL',
  startTime: '',
  endTime: '',
};

export function LogQueryForm({ onSubmit }: LogQueryFormProps) {
  const [formData, setFormData] = React.useState(defaultFormData);

  const { data: serviceNamesData, loading: serviceNamesLoading, error: serviceNamesError } = useQuery<UniqueServiceNamesResponse>(
    GET_UNIQUE_SERVICE_NAMES
  );

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // 处理时间范围
    const formatDateTime = (dateTimeStr: string, isEndTime: boolean) => {
      if (!dateTimeStr) return '';

      // 解析日期字符串为Date对象
      const date = new Date(dateTimeStr);
      
      // 获取年月日
      const year = date.getFullYear();
      const month = date.getMonth(); // 0-11
      const day = date.getDate();

      // 创建上海时区的日期（使用年月日）
      const shanghaiDate = new Date();
      shanghaiDate.setFullYear(year);
      shanghaiDate.setMonth(month);
      shanghaiDate.setDate(day);

      // 设置时分秒
      if (isEndTime) {
        shanghaiDate.setHours(23);
        shanghaiDate.setMinutes(59);
        shanghaiDate.setSeconds(59);
        shanghaiDate.setMilliseconds(999);
      } else {
        shanghaiDate.setHours(0);
        shanghaiDate.setMinutes(0);
        shanghaiDate.setSeconds(0);
        shanghaiDate.setMilliseconds(0);
      }

      // 格式化为 yyyy-MM-dd HH:mm:ss
      const pad = (num: number) => String(num).padStart(2, '0');
      const formattedDate = `${year}-${pad(month + 1)}-${pad(day)} ${
        pad(shanghaiDate.getHours())}:${
        pad(shanghaiDate.getMinutes())}:${
        pad(shanghaiDate.getSeconds())}`;

      console.log(`Formatting ${isEndTime ? 'end' : 'start'} time:`, {
        input: dateTimeStr,
        date: date.toString(),
        shanghaiDate: shanghaiDate.toString(),
        formatted: formattedDate
      });

      return formattedDate;
    };

    const submittedData = {
      ...formData,
      serviceName: formData.serviceName === 'ALL' ? '' : formData.serviceName,
      status: formData.status === 'ALL' ? '' : formData.status,
      responseStatus: formData.responseStatus === 'ALL' ? '' : formData.responseStatus,
      startTime: formatDateTime(formData.startTime, false),
      endTime: formatDateTime(formData.endTime, true)
    };

    console.log('Submitting query with data:', submittedData);
    onSubmit(submittedData);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 border p-4 rounded-lg bg-card">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div className="space-y-2">
          <Label htmlFor="serviceName">Service Name</Label>
          <Select
            value={formData.serviceName}
            onValueChange={(value) => setFormData({ ...formData, serviceName: value })}
          >
            <SelectTrigger>
              <SelectValue placeholder={serviceNamesLoading ? "Loading..." : "Select service"} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Services</SelectItem>
              {serviceNamesError && (
                <SelectItem value="error" disabled>Error loading services</SelectItem>
              )}
              {!serviceNamesError && serviceNamesData?.uniqueServiceNames?.map((service) => (
                <SelectItem key={service} value={service}>
                  {service}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {serviceNamesError && (
            <div className="text-sm text-red-500">Failed to load service names</div>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="status">Status</Label>
          <Select
            value={formData.status}
            onValueChange={(value) => setFormData({ ...formData, status: value })}
          >
            <SelectTrigger>
              <SelectValue placeholder="Select status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">全部</SelectItem>
              <SelectItem value="已发送">已发送</SelectItem>
              <SelectItem value="未发送">未发送</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="responseStatus">Response Status</Label>
          <Select
            value={formData.responseStatus}
            onValueChange={(value) => setFormData({ ...formData, responseStatus: value })}
          >
            <SelectTrigger>
              <SelectValue placeholder="Select response status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All</SelectItem>
              <SelectItem value="SUCCESS">Success</SelectItem>
              <SelectItem value="RATE_LIMITED">Rate Limited</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="startTime">Start Time</Label>
          <Input
            type="datetime-local"
            id="startTime"
            value={formData.startTime}
            onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="endTime">End Time</Label>
          <Input
            type="datetime-local"
            id="endTime"
            value={formData.endTime}
            onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
          />
        </div>
      </div>

      <Button type="submit" className="w-full">
        Search
      </Button>
    </form>
  );
}