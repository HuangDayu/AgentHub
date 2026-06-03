export interface Memory {
  id: string
  tenantId: string
  workspaceId: string
  agentId: string
  name?: string
  memoryType: string
  content: string
  metadata: string
  importance: number
  expiresAt?: string
  createdAt: string
  updatedAt: string
}

export interface Skill {
  id: string
  tenantId: string
  workspaceId: string
  skillCode: string
  name: string
  description: string
  skillType: string
  skillPath: string
  skillFilesTree: string
  source: string
  sourcePath: string
  zipStoragePath: string
  configId: string
  fileCount: number
  totalSize: number
  enabled: boolean
  createdAt: string
  updatedAt: string
  lastSyncAt: string
}

export interface SkillConfig {
  id: string
  tenantId: string
  workspaceId: string
  name: string
  description: string
  skillPaths: string[]
  syncEnabled: boolean
  syncInterval: number
  autoSync: boolean
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface SkillFile {
  id: string
  skillId: string
  tenantId: string
  workspaceId: string
  filePath: string
  fileName: string
  fileExt: string
  fileSize: number
  fileType: string
  encoding: string
  storagePath: string
  checksum: string
  isDirectory: boolean
  metadata: string
  version: number
  createdAt: string
  updatedAt: string
}

/** 市场信息 */
export interface MarketInfo {
  marketId: string
  marketName: string
}

/** 市场技能搜索结果摘要 */
export interface MarketSkillSummary {
  marketId: string
  skillId: string
  skillCode: string
  name: string
  description: string
  author: string
  version: string
  downloadCount: number
  starCount: number
  thumbnailUrl: string
  updatedAt: string
}

/** 市场技能详情 */
export interface MarketSkillDetail {
  marketId: string
  skillId: string
  skillCode: string
  name: string
  description: string
  author: string
  version: string
  license: string
  homepage: string
  downloadUrl: string
  tags: string[]
  downloadCount: number
  starCount: number
  updatedAt: string
  readmeContent: string
}

export interface Workflow {
  id: string
  tenantId: string
  workspaceId: string
  workflowCode: string
  name: string
  description: string
  graphDefinition: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface AgentTeam {
  id: string
  tenantId: string
  workspaceId: string
  teamCode: string
  name: string
  description: string
  coordinationMode: string
  memberConfig: string
  status: string
  createdAt: string
  updatedAt: string
}
