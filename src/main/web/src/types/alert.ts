/**
 * Alert 类型定义
 */
export interface Alert {
  id?: string;
  alertLevel: string;
  alertType: string;

  title: string;
  message: string;

  runId?: string;
  agentId?: string;
  traceId?: string;

  metadata?: Record<string, any>;

  resolved?: boolean;
  resolvedAt?: string;
  resolvedBy?: string;

  createdAt?: string;
}

/**
 * Alert 级别枚举
 */
export enum AlertLevel {
  INFO = 'INFO',
  WARNING = 'WARNING',
  ERROR = 'ERROR',
  CRITICAL = 'CRITICAL',
}

/**
 * Alert 类型枚举
 */
export enum AlertType {
  PERFORMANCE = 'PERFORMANCE',
  RESOURCE = 'RESOURCE',
  ERROR = 'ERROR',
  SECURITY = 'SECURITY',
}
