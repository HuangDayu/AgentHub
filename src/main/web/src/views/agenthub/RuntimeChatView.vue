<template>
  <section class="chat-page">
    <!-- 错误提示 -->
    <div v-if="error" class="error-toast fade-in">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/>
        <line x1="15" y1="9" x2="9" y2="15"/>
        <line x1="9" y1="9" x2="15" y2="15"/>
      </svg>
      <span>{{ error }}</span>
      <button @click="error = ''">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12"/>
        </svg>
      </button>
    </div>

    <div v-if="!selectionReady" class="empty-state scale-in">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
      </div>
      <p>请先选择工作区</p>
      <button class="primary" @click="goToWorkspace">前往设置</button>
    </div>
    
    <div v-else class="chat-layout">
      <!-- 左侧：Agent和会话管理 -->
      <aside :class="['sidebar', { 'collapsed': !sidebarExpanded }]">
        <div v-if="sidebarExpanded" class="sidebar-content">
          <div class="sidebar-header">
            <h3>会话管理</h3>
          </div>

          <!-- Agent 选择 -->
          <div class="sidebar-section">
            <div class="agent-selector">
              <CustomSelect v-model="selectedAgentId" @change="onAgentChange" :options="agentOptions" placeholder="请选择 Agent" />
              <button class="refresh-btn" @click="loadAgents" title="刷新Agent列表">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M23 4v6h-6M1 20v-6h6"/>
                  <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
                </svg>
              </button>
            </div>
            <div v-if="agents.length === 0 && !loadingAgents" class="no-agents">
              <p>暂无Agent</p>
              <button class="link-btn" @click="goToAgents">创建Agent</button>
            </div>
          </div>

          <!-- 会话列表 -->
          <div class="sidebar-section flex-grow">
            <div class="section-header">
              <label class="section-label">会话列表</label>
            </div>
            <div class="session-list">
              <template v-for="session in sessions" :key="session.sessionId">
                <div
                  :class="['session-item', { 'active': session.sessionId === selectedSessionId }]"
                >
                  <div class="session-icon" @click="selectSession(session.sessionId)">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                    </svg>
                  </div>
                  <div class="session-info" @click="selectSession(session.sessionId)">
                    <div class="session-name">{{ session.name || session.sessionId.slice(0, 8) + '...' }}</div>
                    <div class="session-time">{{ formatDateTime(session.createdAt) }}</div>
                  </div>
                  <!-- 正在处理的图标 -->
                  <div v-if="isSessionStreaming(session.sessionId)" class="streaming-indicator" title="正在处理中...">
                    <svg class="spinner-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
                    </svg>
                  </div>
                  <button
                    class="session-runtime-btn"
                    :disabled="isTempSession(session.sessionId)"
                    @click.stop="showSessionRuntime(session.sessionId)"
                    title="查看运行视图"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M4 19h16"/>
                      <path d="M7 16V9"/>
                      <path d="M12 16V5"/>
                      <path d="M17 16v-4"/>
                    </svg>
                  </button>
                  <button class="delete-btn" @click.stop="handleDeleteSession(session.sessionId)" title="删除会话">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                  </svg>
                </button>
              </div>
              <!-- Subsession 子节点 -->
              <div v-if="subsessionMap.get(session.sessionId)?.length" class="subsession-children">
                <div
                  v-for="ss in subsessionMap.get(session.sessionId)"
                  :key="ss.id"
                  class="subsession-item"
                  @click.stop="selectSubsession(ss)"
                >
                  <svg class="sub-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                    <path d="M2 17l10 5 10-5"/>
                    <path d="M2 12l10 5 10-5"/>
                  </svg>
                  <span class="sub-name">{{ ss.name || ss.subagentId.slice(0, 8) + '...' }}</span>
                  <span :class="['sub-status', ss.status.toLowerCase()]">{{ ss.status }}</span>
                </div>
              </div>
            </template>
            <div v-if="!sessions.length && selectedAgentId" class="empty-sessions">
                <p>暂无会话</p>
                <p class="hint">发送消息将自动创建新会话</p>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 左侧：运行视图 -->
      <aside :class="['runtime-sidebar', { 'collapsed': !runtimeExpanded }]">
        <div v-if="runtimeExpanded" class="runtime-content">
          <div class="runtime-header">
            <h3>运行视图</h3>
            <button class="runtime-refresh-btn" @click="loadRuntimeData" :disabled="runtimeLoading" title="刷新运行时数据">
              <svg :class="{ spinning: runtimeLoading }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M23 4v6h-6"/>
                <path d="M1 20v-6h6"/>
                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
              </svg>
            </button>
          </div>

          <div class="runtime-tabs">
            <button :class="['runtime-tab', { active: activeRuntimeTab === 'run' }]" @click="activeRuntimeTab = 'run'">运行</button>
            <button :class="['runtime-tab', { active: activeRuntimeTab === 'trace' }]" @click="activeRuntimeTab = 'trace'">追踪</button>
            <button :class="['runtime-tab', { active: activeRuntimeTab === 'subagent' }]" @click="activeRuntimeTab = 'subagent'; loadSubagents()">子Agent</button>
          </div>

          <div v-if="runtimeError" class="runtime-error">{{ runtimeError }}</div>

          <div class="runtime-body">
            <div v-if="!runtimeHasTarget" class="runtime-placeholder">
              <div class="placeholder-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
              </div>
              <p>选择会话查看运行时数据</p>
              <p class="hint">发送消息后会自动关联当前运行</p>
            </div>

            <template v-else-if="activeRuntimeTab === 'run'">
              <div class="runtime-section">
                <div class="runtime-section-title">
                  <span>运行信息</span>
                  <small>{{ runtimeData.selectedRun?.status || runtimeTrace.status || 'PENDING' }}</small>
                </div>
                <table class="run-info-table">
                  <tbody>
                    <tr>
                      <th>Run ID</th>
                      <td>{{ shortId(runtimeData.selectedRun?.id) }}</td>
                    </tr>
                    <tr>
                      <th>名称</th>
                      <td>{{ runtimeData.selectedRun?.name || '-' }}</td>
                    </tr>
                    <tr>
                      <th>状态</th>
                      <td>{{ runtimeData.selectedRun?.status || runtimeTrace.status || 'PENDING' }}</td>
                    </tr>
                    <tr>
                      <th>开始时间</th>
                      <td>{{ formatDateTime(runtimeData.selectedRun?.timestamp || '') }}</td>
                    </tr>
                    <tr>
                      <th>耗时</th>
                      <td>{{ formatDuration(runtimeTrace.latencyNs) }}</td>
                    </tr>
                    <tr>
                      <th>Spans</th>
                      <td>{{ runtimeTrace.spanCount }}</td>
                    </tr>
                    <tr>
                      <th>PID</th>
                      <td>{{ runtimeData.selectedRun?.pid || '-' }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div class="runtime-section">
                <div class="runtime-section-title">
                  <span>Token 信息</span>
                  <small>{{ formatNumber(runtimeTotalTokens) }}</small>
                </div>
                <table class="run-info-table">
                  <tbody>
                    <tr>
                      <th>总计</th>
                      <td>{{ formatNumber(runtimeTotalTokens) }}</td>
                    </tr>
                    <tr>
                      <th>提示词</th>
                      <td>{{ formatNumber(runtimeChatStats.totalTokens.promptTokens) }}</td>
                    </tr>
                    <tr>
                      <th>生成内容</th>
                      <td>{{ formatNumber(runtimeChatStats.totalTokens.completionTokens) }}</td>
                    </tr>
                    <tr>
                      <th>总计(Avg)</th>
                      <td>{{ formatNumber(runtimeChatStats.avgTokens.totalTokens) }}</td>
                    </tr>
                    <tr>
                      <th>提示词(Avg)</th>
                      <td>{{ formatNumber(runtimeChatStats.avgTokens.promptTokens) }}</td>
                    </tr>
                    <tr>
                      <th>生成内容(Avg)</th>
                      <td>{{ formatNumber(runtimeChatStats.avgTokens.completionTokens) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div class="runtime-section">
                <div class="runtime-section-title">
                  <span>模型调用</span>
                  <small>{{ runtimeChatStats.modelInvocations }} 次</small>
                </div>
                <div v-if="modelStats.length === 0" class="runtime-empty">暂无模型调用数据</div>
                <div v-for="item in modelStats" :key="item.model" class="model-row">
                  <div class="model-row-main">
                    <strong>{{ item.model }}</strong>
                    <span>{{ item.calls }} 次</span>
                  </div>
                  <div class="model-meter"><i :style="{ width: item.percent + '%' }"></i></div>
                  <small>{{ formatNumber(item.tokens) }} tokens · {{ formatDuration(item.avgLatencyNs) }}</small>
                </div>
              </div>
            </template>

            <template v-else-if="activeRuntimeTab === 'trace'">
              <div class="runtime-section trace-section">
                <div class="runtime-section-title">
                  <span>调用链</span>
                  <small>{{ flatSpanCount }} spans</small>
                </div>
                <input
                  v-if="flatSpanCount > 0"
                  v-model="traceSearchText"
                  class="trace-search"
                  type="search"
                  placeholder="搜索 Span"
                />
                <div v-if="runtimeData.spanTree.length === 0" class="runtime-empty">暂无追踪数据</div>
                <div v-if="traceTreeRows.length > 0" class="trace-tree" role="tree">
                  <div v-for="node in traceTreeRows" :key="node.spanId" class="trace-node">
                    <div
                      :class="['span-row', { selected: selectedSpan?.spanId === node.spanId, error: node.statusCode === 2 }]"
                      role="treeitem"
                      tabindex="0"
                      :aria-expanded="node.children.length ? isSpanExpanded(node.spanId) : undefined"
                      @click="selectSpanNode(node)"
                      @keydown.enter.prevent="selectSpanNode(node)"
                      @keydown.space.prevent="selectSpanNode(node)"
                    >
                      <span class="span-prefix">{{ node.treePrefix }}</span>
                      <button
                        class="span-toggle"
                        type="button"
                        :disabled="node.children.length === 0"
                        :title="isSpanExpanded(node.spanId) ? '收起' : '展开'"
                        @click.stop="toggleSpanNode(node.spanId)"
                      >
                        <svg :class="{ expanded: isSpanExpanded(node.spanId) }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M9 18l6-6-6-6"/>
                        </svg>
                      </button>
                      <div class="span-row-content">
                        <div class="span-row-main">
                          <strong>{{ spanDisplayName(node) }}</strong>
                          <span class="span-duration">{{ formatDuration(node.latencyNs) }}</span>
                        </div>
                        <div class="span-row-sub">
                          <span>{{ spanDisplayKind(node) }}</span>
                          <span>{{ formatSpanTime(node.startTimeUnixNano) }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else-if="runtimeData.spanTree.length > 0" class="runtime-empty">没有匹配的 Span</div>
              </div>
            </template>

            <template v-if="activeRuntimeTab === 'subagent'">
              <div class="runtime-section">
                <div class="runtime-section-title">
                  <span>子Agent</span>
                  <small>{{ subagents.length }} 个</small>
                </div>
                <div v-if="subagents.length === 0" class="runtime-empty">暂无子Agent</div>
                <div v-for="sa in subagents" :key="sa.id"
                     :class="['subagent-item', { selected: selectedSubagentId === sa.id }]"
                     @click="onSelectSubagent(sa)">
                  <div class="subagent-header">
                    <strong>{{ sa.name }}</strong>
                    <span :class="['status-dot', sa.status.toLowerCase()]">{{ sa.status }}</span>
                  </div>
                  <div class="subagent-meta">{{ sa.description || '无描述' }}</div>
                  <div class="subagent-meta" v-if="sa.systemPrompt">📝 {{ sa.systemPrompt.slice(0, 60) }}...</div>
                </div>
              </div>

              <!-- 当前选中Subagent的Subsession + 消息 -->
              <template v-if="selectedSubsession">
                <div class="runtime-section">
                  <div class="runtime-section-title">
                    <span>子会话</span>
                    <small>{{ selectedSubsession.name || selectedSubsession.id.slice(0,8) }}</small>
                  </div>
                  <div class="subsession-info">
                    <span>状态: {{ selectedSubsession.status }}</span>
                    <span>创建: {{ formatDateTime(selectedSubsession.createdAt) }}</span>
                  </div>
                </div>
                <div class="runtime-section">
                  <div class="runtime-section-title">
                    <span>对话记录</span>
                    <small>{{ subagentMessages.length }} 条</small>
                  </div>
                  <div v-if="subagentMessages.length === 0" class="runtime-empty">暂无对话</div>
                  <div v-for="msg in subagentMessages" :key="msg.messageId" :class="['subagent-msg', msg.role.toLowerCase(), msg.messageType?.toLowerCase()]">
                    <div class="msg-role">{{ getMessageRoleLabel(msg) }}</div>
                    <div class="msg-content">
                      <template v-if="msg.role === 'USER'">{{ msg.content }}</template>
                      <template v-else-if="msg.role === 'SYSTEM'">
                        <div class="system-message">{{ msg.content }}</div>
                      </template>
                      <template v-else-if="msg.role === 'ASSISTANT' && (!msg.messageType || msg.messageType === 'ASSISTANT')">
                        <MarkdownRenderer v-if="msg.content" :content="msg.content" />
                        <ToolCallMessage v-for="toolCall in msg.toolCalls || []" :key="toolCall.id" :tool-call="toolCall" />
                      </template>
                      <template v-else-if="msg.role === 'TOOL' || msg.messageType === 'TOOL'">
                        <ToolResultMessage v-for="response in msg.toolResponses || []" :key="response.id" :response="response" />
                      </template>
                      <template v-else-if="msg.messageType === 'SKILL'">
                        <SkillMessage v-for="response in msg.toolResponses || []" :key="response.id" :response="response" />
                      </template>
                    </div>
                  </div>
                </div>
              </template>
            </template>
          </div>
        </div>
      </aside>

      <!-- 右侧：对话区 -->
      <article class="chat-panel">
        <div class="chat-messages" ref="messagesContainer">
          <div v-if="!messages.length && !selectedSessionId" class="empty-chat">
            <div class="empty-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <path d="M8 14s1.5 2 4 2 4-2 4-2"/>
                <line x1="9" y1="9" x2="9.01" y2="9"/>
                <line x1="15" y1="9" x2="15.01" y2="9"/>
              </svg>
            </div>
            <p>选择或创建会话开始对话</p>
          </div>
          <div v-else-if="!messages.length" class="empty-chat">
            <div class="empty-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
              </svg>
            </div>
            <p>发送一条消息开始对话</p>
          </div>
          <div
            v-for="(msg, index) in messages"
            :key="msg.messageId"
            :class="['message', msg.role.toLowerCase(), msg.messageType?.toLowerCase(), {'fade-in': index === messages.length - 1}]"
          >
            <div class="message-avatar" :class="msg.role.toLowerCase()">
              <svg v-if="msg.role === 'USER'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
            </div>
            <div class="message-body">
              <div class="message-header">
                <span class="message-role">{{ getMessageRoleLabel(msg) }}</span>
                <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <div class="message-content">
                <!-- 用户消息 -->
                <template v-if="msg.role === 'USER'">{{ msg.content }}</template>
                
                <!-- 系统消息（错误消息） -->
                <template v-else-if="msg.role === 'SYSTEM'">
                  <div class="system-message">{{ msg.content }}</div>
                </template>
                
                <!-- 助手文本消息 -->
                <template v-else-if="msg.role === 'ASSISTANT' && (!msg.messageType || msg.messageType === 'ASSISTANT')">
                  <MarkdownRenderer v-if="msg.content" :content="msg.content" />
                  <!-- 工具调用 -->
                  <div v-if="msg.toolCalls && msg.toolCalls.length > 0" class="tool-calls-container">
                    <ToolCallMessage
                      v-for="toolCall in msg.toolCalls"
                      :key="toolCall.id"
                      :tool-call="toolCall"
                    />
                  </div>
                </template>
                
                <!-- 工具结果消息 -->
                <template v-else-if="msg.role === 'TOOL' || msg.messageType === 'TOOL'">
                  <div v-if="msg.toolResponses && msg.toolResponses.length > 0" class="tool-results-container">
                    <ToolResultMessage
                      v-for="response in msg.toolResponses"
                      :key="response.id"
                      :response="response"
                    />
                  </div>
                </template>
                
                <!-- 技能消息 -->
                <template v-else-if="msg.messageType === 'SKILL'">
                  <div v-if="msg.toolResponses && msg.toolResponses.length > 0" class="skill-container">
                    <SkillMessage
                      v-for="response in msg.toolResponses"
                      :key="response.id"
                      :response="response"
                    />
                  </div>
                </template>
              </div>
            </div>
          </div>
          <!-- 流式输出实时显示 -->
          <div v-if="streamingContent" class="message assistant fade-in">
            <div class="message-avatar assistant">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
            </div>
            <div class="message-body">
              <div class="message-header">
                <span class="message-role">助手</span>
                <span class="typing-indicator">正在输入...</span>
              </div>
              <div class="message-content">
                <MarkdownRenderer :content="streamingContent" />
                <span class="cursor">▊</span>
              </div>
            </div>
          </div>
        </div>
        <!-- 输入区 -->
        <form :class="['chat-input', { expanded: inputExpanded }]" @submit.prevent="handleSend">
          <input
            ref="fileInputRef"
            class="file-input"
            type="file"
            multiple
            :disabled="!selectedAgentId || sending || uploadingFiles"
            @change="handleFileSelected"
          />
          <div class="input-container">
            <div v-if="uploadedAttachments.length" class="attachment-list">
              <div v-for="file in uploadedAttachments" :key="file.path" class="attachment-chip">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
                </svg>
                <span>{{ file.fileName }}</span>
                <small>{{ formatFileSize(file.size) }}</small>
                <button type="button" title="移除附件" @click="removeAttachment(file.path)">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M18 6L6 18M6 6l12 12"/>
                  </svg>
                </button>
              </div>
            </div>
            <button class="input-expand-btn" type="button" :title="inputExpanded ? '缩小输入框' : '放大输入框'" @click="toggleInputExpanded">
              <svg v-if="inputExpanded" class="input-tool-icon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M8 3v5H3"/>
                <path d="M16 3v5h5"/>
                <path d="M21 16h-5v5"/>
                <path d="M3 16h5v5"/>
                <path d="M9 9L4 4"/>
                <path d="M15 9l5-5"/>
                <path d="M15 15l5 5"/>
                <path d="M9 15l-5 5"/>
              </svg>
              <svg v-else class="input-tool-icon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M15 3h6v6"/>
                <path d="M9 21H3v-6"/>
                <path d="M21 3l-7 7"/>
                <path d="M3 21l7-7"/>
              </svg>
            </button>
            <textarea
              v-model="inputContent"
              :rows="inputExpanded ? 8 : 3"
              placeholder="输入消息，或先上传文件..."
              :disabled="!selectedAgentId || sending"
              @keydown="handleKeydown"
            ></textarea>
            <div class="input-actions">
              <button class="attach-btn" type="button" :disabled="!selectedAgentId || sending || uploadingFiles" title="上传附件" @click="openFilePicker">
                <svg v-if="!uploadingFiles" class="input-tool-icon" viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M12 3v12"/>
                  <path d="M7 8l5-5 5 5"/>
                  <path d="M5 15v3a3 3 0 0 0 3 3h8a3 3 0 0 0 3-3v-3"/>
                </svg>
                <span v-else class="mini-spinner"></span>
              </button>
              <button class="primary send-btn" type="submit" :disabled="!canSend">
                <svg v-if="!sending" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="btn-icon">
                  <line x1="22" y1="2" x2="11" y2="13"/>
                  <polygon points="22 2 15 22 11 13 2 9 22 2"/>
                </svg>
                <span v-else class="spinner"></span>
                <span>{{ sending ? '发送中' : '发送' }}</span>
              </button>
            </div>
          </div>
        </form>
      </article>
    </div>

    <!-- 左下角：运行视图展开/收起按钮 -->
    <button class="toggle-runtime-fab" @click="toggleRuntime" :title="runtimeExpanded ? '收起运行视图' : '展开运行视图'">
      <svg v-if="runtimeExpanded" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M15 18l-6-6 6-6"/>
      </svg>
      <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M9 18l6-6-6-6"/>
      </svg>
    </button>

    <!-- 左下角：会话管理展开/收起按钮 -->
    <button class="toggle-sidebar-fab" @click="toggleSidebar" :title="sidebarExpanded ? '收起会话管理' : '展开会话管理'">
      <svg v-if="sidebarExpanded" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M15 18l-6-6 6-6"/>
      </svg>
      <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M9 18l6-6-6-6"/>
      </svg>
    </button>

    <!-- 右下角：新增会话按钮 -->
    <button class="new-session-fab" @click="createNewSession" :disabled="!selectedAgentId" title="新建会话">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 5v14M5 12h14"/>
      </svg>
    </button>

    <ModalDialog
      :visible="spanDetailVisible"
      :title="selectedSpan ? spanDisplayName(selectedSpan) : 'Span 详情'"
      size="xlarge"
      :show-footer="false"
      @close="closeSpanDetail"
    >
      <div v-if="selectedSpan" class="span-detail-modal">
        <div class="span-detail-summary">
          <div>
            <span>状态</span>
            <strong>{{ statusLabel(selectedSpan.statusCode) }}</strong>
          </div>
          <div>
            <span>操作类型</span>
            <strong>{{ spanDisplayKind(selectedSpan) }}</strong>
          </div>
          <div>
            <span>耗时</span>
            <strong>{{ formatDuration(selectedSpan.latencyNs) }}</strong>
          </div>
        </div>
        <dl class="span-meta-grid">
          <dt>Trace ID</dt>
          <dd>{{ selectedSpan.traceId || '-' }}</dd>
          <dt>Span ID</dt>
          <dd>{{ selectedSpan.spanId || '-' }}</dd>
          <dt>Parent ID</dt>
          <dd>{{ selectedSpan.parentSpanId || '-' }}</dd>
          <dt>开始时间</dt>
          <dd>{{ formatSpanDateTime(selectedSpan.startTimeUnixNano) }}</dd>
          <dt>结束时间</dt>
          <dd>{{ formatSpanDateTime(selectedSpan.endTimeUnixNano) }}</dd>
        </dl>
        <div class="span-json-grid">
          <div class="span-json-section">
            <strong>输入</strong>
            <pre>{{ formatJson(spanFunctionPayload(selectedSpan, 'input')) }}</pre>
          </div>
          <div class="span-json-section">
            <strong>输出</strong>
            <pre>{{ formatJson(spanFunctionPayload(selectedSpan, 'output')) }}</pre>
          </div>
        </div>
        <div class="span-json-section">
          <strong>Attributes</strong>
          <pre>{{ formatJson(selectedSpan.attributes || {}) }}</pre>
        </div>
        <div class="span-json-section" v-if="selectedSpan.events?.length">
          <strong>Events</strong>
          <pre>{{ formatJson(selectedSpan.events) }}</pre>
        </div>
      </div>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { showConfirm } from '@/utils/confirm'
import { useRouter } from 'vue-router'
import { listAgents } from '@/api/agent-api'
import { listSubagents, listSubsessions } from '@/api/subagent-api'
import {
  createSession,
  deleteSession,
  listMessages,
  listSessions,
  listSubsessionMessages,
  sendMessageStream,
  uploadChatAttachments,
  type ChatAttachment,
} from '@/api/runtime-api'
import { emptyRuntimeDataView, loadRuntimeDataView, type RuntimeDataView } from '@/api/runtime-data-view-api'
import { formatDateTime } from '@/common/format'
import type { ChatMessage, ChatSession, StreamMessage } from '@/domain/types'
import { useWorkspaceStore } from '@/store/workspace-store'
import type { Agent } from '@/types/agent'
import type { SpanTreeNode } from '@/api/runtime-data-view-api'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import ToolCallMessage from '@/components/ToolCallMessage.vue'
import ToolResultMessage from '@/components/ToolResultMessage.vue'
import SkillMessage from '@/components/SkillMessage.vue'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'

const router = useRouter()
const store = useWorkspaceStore()
const error = ref('')

// Sidebar state
const sidebarExpanded = ref(true)
const runtimeExpanded = ref(false)

// Agent
const agents = ref<Agent[]>([])
const selectedAgentId = ref('')
const loadingAgents = ref(false)
const agentOptions = computed(() => agents.value.map(a => ({ value: a.id, label: a.name })))

// Session
const sessions = ref<ChatSession[]>([])
const selectedSessionId = ref('')
const pendingSessionName = ref('') // 临时会话的名称，用于创建时使用

// Messages - 为每个会话维护独立的消息列表
const sessionMessages = ref(new Map<string, ChatMessage[]>())
const messages = ref<ChatMessage[]>([])
const messagesContainer = ref<HTMLElement | null>(null)

// Input
const inputContent = ref('')
const sending = ref(false)
const streamingContent = ref('')
const inputExpanded = ref(false)
const uploadingFiles = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const uploadedAttachments = ref<ChatAttachment[]>([])

// Runtime data view
const activeRuntimeTab = ref<'run' | 'trace' | 'subagent'>('run')
const runtimeLoading = ref(false)
const runtimeError = ref('')
const selectedSpan = ref<SpanTreeNode | null>(null)
const runtimeData = ref<RuntimeDataView>(emptyRuntimeDataView())
const traceSearchText = ref('')
const expandedSpanIds = ref(new Set<string>())
const spanDetailVisible = ref(false)
const skipRuntimeLoad = ref(false)

// Subagent runtime view
const subagents = ref<import('@/types/subagent').Subagent[]>([])
const selectedSubagentId = ref('')
const selectedSubsessionId = ref('')
const subagentMessages = ref<ChatMessage[]>([])
const subsessions = ref<import('@/types/subagent').Subsession[]>([])
const subsessionMap = ref(new Map<string, import('@/types/subagent').Subsession[]>())

// 根据选中的Subagent或Subsession ID找到对应的Subsession
const selectedSubsession = computed(() => {
  const id = selectedSubsessionId.value || selectedSubagentId.value
  if (!id) return null
  for (const subs of subsessionMap.value.values()) {
    const found = subs.find(s => s.id === id || s.subagentId === id)
    if (found) return found
  }
  return null
})

// 判断当前选中的是否是Subsession
const isSubsessionView = computed(() => {
  return !!selectedSubsessionId.value && selectedSessionId.value === selectedSubsessionId.value
})

// 当前Subsession的父Session ID
const currentSubsessionParentId = computed(() => {
  const ss = selectedSubsession.value
  return ss ? ss.parentSessionId : ''
})

// 流消息状态管理 - 为每个会话维护独立的流消息状态
const sessionStreamingStates = new Map<string, {
  content: string
  isStreaming: boolean
  pendingMessages: ChatMessage[] // 暂存正在进行的stream的消息（用户消息 + 助手消息）
  abortController?: AbortController
}>()

// 获取当前会话的流消息状态
function getCurrentStreamingState() {
  const sessionId = selectedSessionId.value
  if (!sessionId) return null
  return ensureStreamingState(sessionId)
}

function ensureStreamingState(sessionId: string) {
  if (!sessionStreamingStates.has(sessionId)) sessionStreamingStates.set(sessionId, { content: '', isStreaming: false, pendingMessages: [] })
  return sessionStreamingStates.get(sessionId)!
}

// 检查某个会话是否正在流式处理
function isSessionStreaming(sessionId: string): boolean {
  const state = sessionStreamingStates.get(sessionId)
  return state?.isStreaming || false
}

// 监听selectedSessionId变化，恢复流消息状态和消息列表
watch(selectedSessionId, (newSessionId) => {
  if (!newSessionId) {
    clearSessionState()
    return
  }
  restoreSessionMessages(newSessionId)
  restoreStreamingState(newSessionId)
})

function clearSessionState() {
  messages.value = []
  streamingContent.value = ''
  sending.value = false
}

function restoreSessionMessages(sessionId: string) {
  const msgs = sessionMessages.value.get(sessionId) || []
  messages.value = [...msgs]
}

function restoreStreamingState(sessionId: string) {
  const state = sessionStreamingStates.get(sessionId)
  if (state) {
    streamingContent.value = state.content
    sending.value = state.isStreaming
  } else {
    streamingContent.value = ''
    sending.value = false
  }
}

// Selection state
const selectionReady = computed(() => store.tenantId && store.workspaceId)
const runtimeHasTarget = computed(() => Boolean(selectedAgentId.value && selectedSessionId.value))
const currentSession = computed(() => sessions.value.find((item) => item.sessionId === selectedSessionId.value))
const runtimeCanLoad = computed(() => Boolean(runtimeHasTarget.value && !isTempSession(selectedSessionId.value)))
const runtimeTrace = computed(() => runtimeData.value.trace)
const runtimeChatStats = computed(() => runtimeData.value.modelInvocationData.chat)
const runtimeTotalTokens = computed(() => runtimeChatStats.value.totalTokens.totalTokens || 0)
const flatSpanCount = computed(() => {
  let count = 0
  function walk(nodes: SpanTreeNode[]) {
    for (const n of nodes) { count++; walk(n.children || []) }
  }
  walk(runtimeData.value.spanTree)
  return count
})
const traceTreeRows = computed(() => flattenSpanTree(runtimeData.value.spanTree))
const modelStats = computed(() => buildModelStats())
const canSend = computed(() => Boolean(selectedAgentId.value && hasSendContent() && !sending.value && !uploadingFiles.value))

// Get selection object
function getSelection() {
  return {
    tenantId: store.tenantId!,
    workspaceId: store.workspaceId!
  }
}

async function loadRuntimeData() {
  if (!runtimeCanLoad.value) { resetRuntimeData(); return }
  runtimeError.value = ''; runtimeLoading.value = true
  try { await performLoadRuntimeData() } catch (e: any) { runtimeError.value = e.message || '加载运行时数据失败' } finally { runtimeLoading.value = false }
}

async function performLoadRuntimeData(): Promise<void> {
  runtimeData.value = await loadRuntimeDataView(getSelection(), selectedAgentId.value, selectedSessionId.value)
  expandRootSpans()
  selectedSpan.value = traceTreeRows.value[0] || null
}

function scheduleRuntimeRefresh(runId: string) {
  if (selectedSessionId.value !== runId) return
  if (isTempSession(runId)) return
  window.setTimeout(loadRuntimeData, 800)
}

async function ensureActiveSession(seed: string) {
  let currentSessionId = selectedSessionId.value
  if (currentSessionId && !currentSessionId.startsWith('temp-')) return currentSessionId
  const session = await createSession(getSelection(), selectedAgentId.value, sessionName(seed))
  replaceTempSession(currentSessionId, session)
  selectedSessionId.value = session.sessionId
  pendingSessionName.value = ''
  return session.sessionId
}

function sessionName(seed: string) {
  const text = seed.trim() || '附件会话'
  return text.slice(0, 20) + (text.length > 20 ? '...' : '')
}

function replaceTempSession(tempSessionId: string, session: ChatSession) {
  const index = sessions.value.findIndex(s => s.sessionId === tempSessionId)
  index === -1 ? sessions.value.unshift(session) : sessions.value.splice(index, 1, session)
}

// Toggle sidebar
function toggleSidebar() {
  sidebarExpanded.value = !sidebarExpanded.value
  // 如果展开会话管理，则收起运行视图
  if (sidebarExpanded.value) {
    runtimeExpanded.value = false
  }
}

// Toggle runtime panel
function toggleRuntime() {
  runtimeExpanded.value = !runtimeExpanded.value
  // 如果展开运行视图，则收起会话管理
  if (runtimeExpanded.value) {
    sidebarExpanded.value = false
  }
}

function showSessionRuntime(sessionId: string) {
  if (isTempSession(sessionId)) return
  selectedSessionId.value = sessionId
  sidebarExpanded.value = false
  runtimeExpanded.value = true
  loadMessages()
  loadRuntimeData()
}

// 选择Subagent，加载对应的Subsession消息
async function onSelectSubagent(sa: import('@/types/subagent').Subagent) {
  selectedSubagentId.value = sa.id
  const ss = selectedSubsession.value
  if (!ss) { subagentMessages.value = []; return }
  try {
    const msgs = await listSubsessionMessages(getSelection(), selectedAgentId.value, ss.parentSessionId, ss.id)
    subagentMessages.value = parseChatMessages(msgs)
  } catch { subagentMessages.value = [] }
}

// 选择Subsession，直接在对话区显示历史消息
async function selectSubsession(ss: import('@/types/subagent').Subsession) {
  skipRuntimeLoad.value = true
  selectedSubsessionId.value = ss.id
  selectedSessionId.value = ss.id
  try {
    const msgs = parseChatMessages(await listSubsessionMessages(getSelection(), selectedAgentId.value, ss.parentSessionId, ss.id))
    messages.value = msgs
    sessionMessages.value.set(ss.id, msgs)
  } catch { messages.value = [] }
}

// Load subagents for runtime view
async function loadSubagents() {
  if (!selectedAgentId.value) return
  try { subagents.value = await listSubagents(getSelection(), selectedAgentId.value); subsessions.value = await fetchSubsessionsForSelected() } catch (e: any) { console.error('加载子Agent失败:', e) }
}

async function fetchSubsessionsForSelected() {
  return selectedSessionId.value ? listSubsessions(getSelection(), selectedAgentId.value, selectedSessionId.value) : []
}

// Load agents
async function loadAgents() {
  if (!selectionReady.value) return
  error.value = ''; loadingAgents.value = true
  try { await performLoadAgents() } catch (e: any) { error.value = e.message || '加载 Agent 失败' } finally { loadingAgents.value = false }
}

async function performLoadAgents(): Promise<void> {
  agents.value = await listAgents(getSelection())
  console.log('Loaded agents:', agents.value)
  if (agents.value.length && !selectedAgentId.value) {
    selectedAgentId.value = agents.value[0].id
    await loadSessions()
  }
}

// Load sessions
async function loadSessions() {
  if (!selectedAgentId.value) return
  error.value = ''
  try {
    sessions.value = await listSessions(getSelection(), selectedAgentId.value)
    await applySessionsAndLoad()
  } catch (e: any) {
    error.value = e.message || '加载会话失败'
  }
}

async function applySessionsAndLoad(): Promise<void> {
  ensureSelectedSession()
  if (sessions.value.length > 0 && !selectedSessionId.value) {
    selectedSessionId.value = sessions.value[0].sessionId
    await loadMessages()
  }
  await loadSubsessionsForSelected()
}

// 只加载当前选中Session的Subsession
async function loadSubsessionsForSelected() {
  if (!selectedSessionId.value) return
  const map = new Map<string, import('@/types/subagent').Subsession[]>()
  try {
    const subs = await listSubsessions(getSelection(), selectedAgentId.value, selectedSessionId.value)
    if (subs.length) map.set(selectedSessionId.value, subs)
  } catch { /* 忽略 */ }
  subsessionMap.value = map
}

// Create new session
function createNewSession() {
  if (!selectedAgentId.value) return
  error.value = ''
  uploadedAttachments.value = []
  sessions.value.unshift(buildTempSession())
  selectedSessionId.value = sessions.value[0].sessionId
  resetRuntimeData()
  pendingSessionName.value = '新会话'
  messages.value = []
}

function buildTempSession(): ChatSession {
  return { sessionId: 'temp-' + Date.now(), agentId: selectedAgentId.value!, name: '新会话', createdAt: new Date().toISOString() }
}

// Delete session
async function handleDeleteSession(sessionId: string) {
  if (!canDeleteSession()) return
  if (!await showConfirm('确定要删除这个会话吗？')) return
  await performDeleteSession(sessionId)
}

function canDeleteSession(): boolean {
  return Boolean(selectedAgentId.value)
}

async function performDeleteSession(sessionId: string): Promise<void> {
  error.value = ''
  try {
    if (isTempSession(sessionId)) removeTempSession(sessionId)
    else await deleteServerSession(sessionId)
  } catch (e: any) {
    error.value = e.message || '删除会话失败'
  }
}

function isTempSession(sessionId: string): boolean {
  return sessionId.startsWith('temp-')
}

function removeTempSession(sessionId: string): void {
  sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
  if (isSelectedSession(sessionId)) clearSelectedTempSession()
}

async function deleteServerSession(sessionId: string): Promise<void> {
  await deleteSession(getSelection(), selectedAgentId.value!, sessionId)
  sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
  if (isSelectedSession(sessionId)) clearSelectedSession()
}

function isSelectedSession(sessionId: string): boolean {
  return selectedSessionId.value === sessionId
}

function clearSelectedSession(): void {
  selectedSessionId.value = ''
  messages.value = []
}

function clearSelectedTempSession(): void {
  selectedSessionId.value = ''
  pendingSessionName.value = ''
  messages.value = []
}

// Select session
function selectSession(sessionId: string) {
  selectedSessionId.value = sessionId
  uploadedAttachments.value = []
  loadMessages()
}

// Load messages
async function loadMessages() {
  if (!canLoadMessages()) return
  error.value = ''
  try {
    await fetchAndApplyMessages()
  } catch (e: any) {
    error.value = e.message || '加载消息失败'
  }
}

function canLoadMessages(): boolean {
  return Boolean(selectedSessionId.value && selectedAgentId.value)
}

async function fetchAndApplyMessages(): Promise<void> {
  const rawMessages = await listMessages(getSelection(), selectedAgentId.value!, selectedSessionId.value!)
  const parsed = parseChatMessages(rawMessages)
  applyMessagesToSession(parsed)
}

function applyMessagesToSession(parsed: ChatMessage[]): void {
  const allMsgs = [...parsed]
  appendPendingMessages(allMsgs, parsed)
  const sessionId = selectedSessionId.value!
  sessionMessages.value.set(sessionId, allMsgs)
  messages.value = [...allMsgs]
  scrollToBottom()
}

function appendPendingMessages(allMsgs: ChatMessage[], parsed: ChatMessage[]): void {
  const state = sessionStreamingStates.get(selectedSessionId.value)
  if (!state || state.pendingMessages.length === 0) return
  const backendIds = new Set(parsed.map(m => m.messageId))
  const pending = state.pendingMessages.filter(m => !backendIds.has(m.messageId))
  allMsgs.push(...pending)
}

function parseChatMessages(rawMessages: ChatMessage[]) {
  return rawMessages.map(parseChatMessage)
}

function parseChatMessage(message: ChatMessage): ChatMessage {
  const parsedMessage: ChatMessage = { ...message }
  parseToolCalls(parsedMessage)
  parseToolResponses(parsedMessage)
  return parsedMessage
}

function parseToolCalls(message: ChatMessage) {
  if (message.role !== 'ASSISTANT' || !message.content?.startsWith('[{')) return
  try { message.toolCalls = JSON.parse(message.content); message.content = '' }
  catch (e) { console.error('Failed to parse tool calls:', e) }
}

function parseToolResponses(message: ChatMessage) {
  if (message.role !== 'TOOL' || !message.content?.startsWith('[{')) return
  try { message.toolResponses = JSON.parse(message.content); message.content = '' }
  catch (e) { console.error('Failed to parse tool responses:', e) }
}

// Send message
async function handleSend() {
  if (!canSend.value) return

  const content = inputContent.value.trim() || '请阅读我上传的文件，并基于文件内容回复。'
  const attachments = [...uploadedAttachments.value]
  inputContent.value = ''
  sending.value = true
  error.value = ''

  let currentSessionId = ''
  try {
    currentSessionId = await ensureActiveSession(content)
  } catch (e: any) {
    error.value = e.message || '创建会话失败'
    sending.value = false
    return
  }

  // 立即添加用户消息
  const userMessage: ChatMessage = {
    messageId: Date.now().toString(),
    sessionId: currentSessionId,
    role: 'USER',
    content,
    createdAt: new Date().toISOString()
  }

  // 确保该会话的消息列表存在
  if (!sessionMessages.value.has(currentSessionId)) {
    sessionMessages.value.set(currentSessionId, [])
  }

  // 添加用户消息到sessionMessages
  const sessionMsgs = sessionMessages.value.get(currentSessionId)!
  sessionMsgs.push(userMessage)

  try {
    uploadedAttachments.value = []
    startStreamState(currentSessionId, userMessage)
    scrollToBottom()
    const subsessionId = isSubsessionView.value ? currentSessionId : undefined
    await sendMessageStream(getSelection(), selectedAgentId.value,
      isSubsessionView.value ? currentSubsessionParentId.value : currentSessionId,
      content, attachments.map(file => file.path), {
        onMessage: (streamMsg: StreamMessage) => {
          handleStreamMessage(streamMsg, currentSessionId)
          scrollToBottom()
        },
        onDone: () => finishStream(currentSessionId),
        onError: (err) => {
          uploadedAttachments.value = attachments
          handleStreamError(currentSessionId, userMessage, err)
        },
      }, subsessionId)
  } catch (e: any) {
    uploadedAttachments.value = attachments
    error.value = e.message || '发送消息失败'
    clearStreamState(currentSessionId)
  } finally {
    sending.value = false
  }
}

function startStreamState(sessionId: string, userMessage: ChatMessage) {
  const streamState = sessionStreamingStates.get(sessionId) || { content: '', isStreaming: false, pendingMessages: [] }
  streamState.content = ''
  streamState.isStreaming = true
  streamState.pendingMessages = [userMessage]
  sessionStreamingStates.set(sessionId, streamState)
  if (selectedSessionId.value === sessionId) {
    messages.value = [...(sessionMessages.value.get(sessionId) || [])]
    streamingContent.value = ''
  }
}

function finishStream(sessionId: string) {
  const state = sessionStreamingStates.get(sessionId)
  const finalContent = state?.content || ''
  if (finalContent.trim()) addAssistantMessage(sessionId, finalContent)
  clearStreamState(sessionId)
  scheduleRuntimeRefresh(sessionId)
  scrollToBottom()
}

function addAssistantMessage(sessionId: string, content: string) {
  const message: ChatMessage = assistantChatMessage(sessionId, content)
  const sessionMsgs = sessionMessages.value.get(sessionId) || []
  sessionMsgs.push(message)
  sessionMessages.value.set(sessionId, sessionMsgs)
  if (selectedSessionId.value === sessionId) messages.value = [...sessionMsgs]
}

function assistantChatMessage(sessionId: string, content: string): ChatMessage {
  return {
    messageId: (Date.now() + 1).toString(),
    sessionId,
    role: 'ASSISTANT',
    content,
    createdAt: new Date().toISOString(),
    messageType: 'ASSISTANT'
  }
}

function handleStreamError(sessionId: string, userMessage: ChatMessage, err: Error) {
  error.value = err.message || '发送消息失败'
  clearStreamState(sessionId)
  removeMessage(sessionId, userMessage.messageId)
}

function removeMessage(sessionId: string, messageId: string) {
  const sessionMsgs = sessionMessages.value.get(sessionId)
  if (!sessionMsgs) return
  const index = sessionMsgs.findIndex(m => m.messageId === messageId)
  if (index !== -1) sessionMsgs.splice(index, 1)
  if (selectedSessionId.value === sessionId) messages.value = [...sessionMsgs]
}

function clearStreamState(sessionId: string) {
  const state = sessionStreamingStates.get(sessionId)
  if (state) {
    state.content = ''
    state.isStreaming = false
    state.pendingMessages = []
  }
  if (selectedSessionId.value === sessionId) streamingContent.value = ''
}

function openFilePicker() {
  fileInputRef.value?.click()
}

async function handleFileSelected(event: Event) {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files || [])
  input.value = ''
  if (!files.length || !selectedAgentId.value) return
  await uploadSelectedFiles(files)
}

async function uploadSelectedFiles(files: File[]) {
  uploadingFiles.value = true
  error.value = ''
  try {
    const sessionId = await ensureActiveSession(inputContent.value || files[0]?.name || '')
    const uploaded = await uploadChatAttachments(getSelection(), selectedAgentId.value, sessionId, files)
    uploadedAttachments.value.push(...uploaded)
  } catch (e: any) {
    error.value = e.message || '上传附件失败'
  } finally {
    uploadingFiles.value = false
  }
}

function removeAttachment(path: string) {
  uploadedAttachments.value = uploadedAttachments.value.filter(file => file.path !== path)
}

function toggleInputExpanded() {
  inputExpanded.value = !inputExpanded.value
}

function formatFileSize(size: number) {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function hasSendContent() {
  return Boolean(inputContent.value.trim() || uploadedAttachments.value.length)
}

// 处理流式消息
function handleStreamMessage(streamMsg: StreamMessage, targetSessionId?: string) {
  const currentSessionId = targetSessionId || selectedSessionId.value
  if (!currentSessionId) return

  // 确保sessionMessages存在
  if (!sessionMessages.value.has(currentSessionId)) {
    sessionMessages.value.set(currentSessionId, [])
  }
  const sessionMsgs = sessionMessages.value.get(currentSessionId)!

  // 获取流状态
  const state = sessionStreamingStates.get(currentSessionId)

  if (streamMsg.messageType === 'ASSISTANT') {
    // 助手消息：累积文本内容
    if (streamMsg.text) {
      // 更新流状态中的内容
      if (state) {
        state.content += streamMsg.text
      }
      // 如果是当前会话，也更新显示用的streamingContent
      if (currentSessionId === selectedSessionId.value) {
        streamingContent.value = state?.content || ''
      }
    }
    // 如果有工具调用，先保存当前的助手消息，然后添加工具调用消息
    if (streamMsg.toolCalls && streamMsg.toolCalls.length > 0) {
      // 先保存累积的助手消息
      const currentContent = state?.content || ''
      if (currentContent.trim()) {
        const assistantMessage: ChatMessage = {
          messageId: `assistant-${Date.now()}`,
          sessionId: currentSessionId,
          role: 'ASSISTANT',
          content: currentContent,
          createdAt: new Date().toISOString(),
          messageType: 'ASSISTANT'
        }
        sessionMsgs.push(assistantMessage)
        // 如果是当前会话，更新显示
        if (currentSessionId === selectedSessionId.value) {
          messages.value.push(assistantMessage)
        }

        // 添加到pendingMessages
        if (state && state.pendingMessages) {
          state.pendingMessages.push(assistantMessage)
        }

        // 清空累积内容
        if (state) {
          state.content = ''
        }
        if (currentSessionId === selectedSessionId.value) {
          streamingContent.value = ''
        }
      }

      // 然后保存工具调用消息
      const toolCallMessage: ChatMessage = {
        messageId: `toolcall-${Date.now()}`,
        sessionId: currentSessionId,
        role: 'ASSISTANT',
        content: '',
        createdAt: new Date().toISOString(),
        messageType: 'ASSISTANT',
        toolCalls: streamMsg.toolCalls
      }
      sessionMsgs.push(toolCallMessage)
      // 如果是当前会话，更新显示
      if (currentSessionId === selectedSessionId.value) {
        messages.value.push(toolCallMessage)
      }

      // 添加到pendingMessages
      if (state && state.pendingMessages) {
        state.pendingMessages.push(toolCallMessage)
      }
    }
  } else if (streamMsg.messageType === 'TOOL') {
    // 工具消息：处理工具响应
    if (streamMsg.responses && streamMsg.responses.length > 0) {
      for (const response of streamMsg.responses) {
        // 判断是否是技能读取
        const isSkill = response.name === 'read_skill' || response.name === 'apply_skill'

        const toolResultMessage: ChatMessage = {
          messageId: `toolresult-${Date.now()}-${response.id}`,
          sessionId: currentSessionId,
          role: 'TOOL',
          content: '',
          createdAt: new Date().toISOString(),
          messageType: isSkill ? 'SKILL' : 'TOOL',
          toolResponses: [response]
        }
        sessionMsgs.push(toolResultMessage)
        // 如果是当前会话，更新显示
        if (currentSessionId === selectedSessionId.value) {
          messages.value.push(toolResultMessage)
        }

        // 添加到pendingMessages
        if (state && state.pendingMessages) {
          state.pendingMessages.push(toolResultMessage)
        }
      }
    }
  } else if (streamMsg.messageType === 'SYSTEM') {
    // 系统消息：通常是错误消息
    const systemMessage: ChatMessage = {
      messageId: `system-${Date.now()}`,
      sessionId: currentSessionId,
      role: 'SYSTEM',
      content: streamMsg.text || '',
      createdAt: new Date().toISOString(),
      messageType: 'SYSTEM'
    }
    sessionMsgs.push(systemMessage)
    // 如果是当前会话，更新显示
    if (currentSessionId === selectedSessionId.value) {
      messages.value.push(systemMessage)
    }

    // 添加到pendingMessages
    if (state && state.pendingMessages) {
      state.pendingMessages.push(systemMessage)
    }
  }
}

// Handle keydown
function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}



// Scroll to bottom
function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

type TraceRow = SpanTreeNode & { depth: number; treePrefix?: string }

function flatAllSpans(nodes: SpanTreeNode[]): SpanTreeNode[] {
  const result: SpanTreeNode[] = []
  function walk(list: SpanTreeNode[]) {
    for (const n of list) { result.push(n); walk(n.children || []) }
  }
  walk(nodes)
  return result
}

function flattenSpanTree(nodes: SpanTreeNode[]): TraceRow[] {
  const keyword = traceSearchText.value.trim().toLowerCase()
  return nodes.flatMap((node, index) => flattenSpanNode(node, index, nodes.length, keyword))
}

function flattenSpanNode(node: SpanTreeNode, index: number, total: number, keyword: string, depth = 0, ancestors: boolean[] = []): TraceRow[] {
  const children = node.children || []
  // 搜索过滤
  if (keyword) {
    const filteredChildren = children
      .flatMap((child, i) => flattenSpanNode(child, i, children.length, keyword, depth + 1, [...ancestors, total - 1 === index]))
    if (!spanMatchesKeyword(node, keyword) && filteredChildren.length === 0) return []
    const row: TraceRow = { ...node, depth, treePrefix: treePrefix(ancestors, total - 1 === index) }
    return [row, ...filteredChildren]
  }
  // 无搜索时按展开状态渲染
  const row: TraceRow = { ...node, depth, treePrefix: treePrefix(ancestors, total - 1 === index) }
  if (!isSpanExpanded(node.spanId)) return [row]
  return [row, ...children.flatMap((child, i) => flattenSpanNode(child, i, children.length, keyword, depth + 1, [...ancestors, total - 1 === index]))]
}

function spanMatchesKeyword(node: SpanTreeNode, keyword: string) {
  return [spanDisplayName(node), node.name, spanDisplayKind(node)].some((text) => text.toLowerCase().includes(keyword))
}

function treePrefix(ancestors: boolean[], isLast: boolean) {
  const prefix = ancestors.map((last) => (last ? '   ' : '│  ')).join('')
  return `${prefix}${isLast ? '└─' : '├─'}`
}

function expandRootSpans() {
  expandedSpanIds.value = new Set(runtimeData.value.spanTree.map((node) => node.spanId))
}

function toggleSpanNode(spanId: string) {
  const next = new Set(expandedSpanIds.value)
  next.has(spanId) ? next.delete(spanId) : next.add(spanId)
  expandedSpanIds.value = next
}

function isSpanExpanded(spanId: string) {
  return expandedSpanIds.value.has(spanId)
}

function selectSpanNode(node: SpanTreeNode) {
  selectedSpan.value = node
  spanDetailVisible.value = true
}

function closeSpanDetail() {
  spanDetailVisible.value = false
}

function spanDisplayName(span: SpanTreeNode) {
  return stringAttr(span, 'agentscope.function.name') || span.name || shortId(span.spanId)
}

function spanDisplayKind(span: SpanTreeNode) {
  return invokeDisplayKind(span) || modelDisplayKind(span) || stringAttr(span, 'gen_ai.operation.name') || span.kind || 'Unknown'
}

function invokeDisplayKind(span: SpanTreeNode) {
  const operation = stringAttr(span, 'gen_ai.operation.name')
  if (operation === 'invoke_agent') return withName(operation, stringAttr(span, 'gen_ai.agent.name'))
  if (operation === 'execute_tool') return withName(operation, stringAttr(span, 'gen_ai.tool.name'))
  return operation === 'format' ? withName(operation, stringAttr(span, 'agentscope.format.target')) : ''
}

function modelDisplayKind(span: SpanTreeNode) {
  const operation = stringAttr(span, 'gen_ai.operation.name')
  if (!['chat', 'chat_model', 'embeddings'].includes(operation || '')) return ''
  return withName(operation!, span.model || stringAttr(span, 'gen_ai.request.model'))
}

function withName(operation: string, name?: string) {
  return name ? `${operation}: ${name}` : operation
}

function stringAttr(span: SpanTreeNode, path: string) {
  const value = span.attributes?.[path] ?? pathValue(span.attributes, path.split('.'))
  return value === undefined || value === null ? '' : String(value)
}

function spanFunctionPayload(span: SpanTreeNode, key: 'input' | 'output') {
  return pathValue(span.attributes, ['agentscope', 'function', key]) ?? pathValue(span.attributes, [`agentscope.function.${key}`]) ?? {}
}

function pathValue(source: any, path: string[]) {
  return path.reduce<any>((obj, key) => obj?.[key], source)
}

function formatJson(value: unknown) {
  if (typeof value === 'string') return formatJsonString(value)
  return JSON.stringify(value ?? {}, null, 2)
}

function formatJsonString(value: string) {
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

function formatSpanTime(value?: string) {
  return formatSpanDateTime(value, true)
}

function formatSpanDateTime(value?: string, timeOnly = false) {
  const numericValue = Number(value)
  if (!numericValue) return '-'
  const date = new Date(numericValue / 1000000)
  return timeOnly ? date.toLocaleTimeString('zh-CN') : date.toLocaleString('zh-CN')
}

function buildModelStats() {
  const tokenStats = runtimeChatStats.value.totalTokensByModel || []
  const callStats = runtimeChatStats.value.modelInvocationsByModel || []
  if (tokenStats.length === 0) return buildSpanModelStats()
  const maxTokens = Math.max(...tokenStats.map((item) => item.totalTokens), 1)
  return tokenStats.map((item) => ({
    model: item.modelName,
    calls: callStats.find((call) => call.modelName === item.modelName)?.invocations || 0,
    tokens: item.totalTokens,
    percent: Math.max(6, Math.round((item.totalTokens / maxTokens) * 100)),
    avgLatencyNs: 0,
  }))
}

function buildSpanModelStats() {
  const grouped = new Map<string, { calls: number; tokens: number; latencyNs: number }>()
  flatAllSpans(runtimeData.value.spanTree).filter((span) => span.model).forEach((span) => {
    const current = grouped.get(span.model!) || { calls: 0, tokens: 0, latencyNs: 0 }
    current.calls += 1
    current.tokens += span.totalTokens || 0
    current.latencyNs += span.latencyNs || 0
    grouped.set(span.model!, current)
  })
  const maxTokens = Math.max(...[...grouped.values()].map((item) => item.tokens), 1)
  return [...grouped.entries()].map(([model, item]) => ({
    model,
    calls: item.calls,
    tokens: item.tokens,
    percent: Math.max(6, Math.round((item.tokens / maxTokens) * 100)),
    avgLatencyNs: item.calls ? item.latencyNs / item.calls : 0,
  }))
}

function formatDuration(nanoseconds?: number) {
  if (!nanoseconds) return '-'
  if (nanoseconds < 1000000) return `${(nanoseconds / 1000).toFixed(1)} us`
  if (nanoseconds < 1000000000) return `${(nanoseconds / 1000000).toFixed(1)} ms`
  return `${(nanoseconds / 1000000000).toFixed(2)} s`
}

function formatNumber(value?: number) {
  return new Intl.NumberFormat('zh-CN').format(value || 0)
}

function shortId(id?: string) {
  if (!id) return '-'
  return id.length > 12 ? `${id.slice(0, 8)}...` : id
}

function statusLabel(statusCode?: number) {
  if (statusCode === undefined || statusCode === null) return 'UNSET'
  if (statusCode === 2) return 'ERROR'
  return statusCode === 1 ? 'OK' : 'UNSET'
}

// Format time
function formatTime(dateStr: string) {
  const date = new Date(dateStr)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// Get message role label
function getMessageRoleLabel(msg: ChatMessage): string {
  if (msg.role === 'USER') return '用户'
  if (msg.role === 'SYSTEM') return '系统'
  
  // 根据消息类型返回不同的标签
  switch (msg.role) {
    case 'TOOL':
      // 显示工具名称
      if (msg.toolResponses && msg.toolResponses.length > 0) {
        return `工具: ${msg.toolResponses[0].name}`
      }
      return '工具'
    case 'ASSISTANT':
      // 如果有工具调用，显示工具名称
      if (msg.toolCalls && msg.toolCalls.length > 0) {
        return `调用: ${msg.toolCalls[0].name}`
      }
      return '助手'
    default:
      // 检查messageType
      if (msg.messageType === 'SKILL') {
        if (msg.toolResponses && msg.toolResponses.length > 0) {
          const skillName = msg.toolResponses[0].name === 'read_skill' ? '读取技能' : 
                           msg.toolResponses[0].name === 'apply_skill' ? '应用技能' : 
                           msg.toolResponses[0].name
          return `技能: ${skillName}`
        }
        return '技能'
      }
      return '助手'
  }
}
// Agent change
function onAgentChange() {
  selectedSessionId.value = ''
  messages.value = []
  resetRuntimeData()
  loadSessions()
}

// Go to workspace
function goToWorkspace() {
  router.push('/agenthub/workspace')
}

// Go to agents
function goToAgents() {
  router.push('/agenthub/agents')
}

// Watch selection
watch(() => [store.tenantId, store.workspaceId], () => {
  selectedAgentId.value = ''
  selectedSessionId.value = ''
  messages.value = []
  sessions.value = []
  resetRuntimeData()
  loadAgents()
})

watch([selectedAgentId, selectedSessionId], () => {
  if (skipRuntimeLoad.value) { skipRuntimeLoad.value = false; return }
  loadRuntimeData()
  loadSubsessionsForSelected()
})

function ensureSelectedSession() {
  if (!selectedSessionId.value) return
  if (sessions.value.some((session) => session.sessionId === selectedSessionId.value)) return
  selectedSessionId.value = ''
  resetRuntimeData()
}

function resetRuntimeData() {
  runtimeError.value = ''
  runtimeData.value = emptyRuntimeDataView()
  selectedSpan.value = null
  spanDetailVisible.value = false
}

// Initialize
onMounted(() => {
  if (selectionReady.value) {
    loadAgents()
  }
})
</script>

<style scoped>
.chat-page {
  max-width: 1400px;
  margin: 0 auto;
  height: calc(100vh - 120px);
  position: relative;
}

/* Error Toast */
.error-toast {
  position: fixed;
  top: 80px;
  right: 24px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--color-error); color: var(--color-text-inverse);
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(201, 74, 53, 0.3);
  backdrop-filter: blur(12px);
  animation: slide-in-right 0.3s ease;
}

.error-toast svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.error-toast button {
  padding: 4px;
  background: transparent;
  border: none; color: var(--color-text-inverse);
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.error-toast button:hover {
  opacity: 1;
}

.error-toast button svg {
  width: 16px;
  height: 16px;
}

@keyframes slide-in-right {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Empty State */
.empty-state {
  border-radius: 20px;
  padding: 60px 40px;
  background: var(--bg-card);
  color: var(--color-text-muted);
  text-align: center;
  box-shadow: 0 12px 24px rgba(32, 44, 68, 0.08);
  backdrop-filter: blur(12px);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-icon {
  width: 80px;
  height: 80px;
  padding: 20px;
  background: linear-gradient(135deg, var(--color-primary-subtle), var(--color-primary-subtle));
  border-radius: 20px;
  color: var(--color-primary);
}

.empty-icon svg {
  width: 100%;
  height: 100%;
}

/* Chat Layout */
.chat-layout {
  display: flex;
  height: 100%;
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  width: 360px;
  min-width: 360px;
  background: var(--bg-card);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  box-shadow: var(--shadow-md);
  backdrop-filter: blur(12px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1),
              min-width 0.25s cubic-bezier(0.4, 0, 0.2, 1),
              opacity 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar.collapsed {
  width: 0;
  min-width: 0;
  border: none;
  box-shadow: none;
  opacity: 0;
}

.sidebar-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--color-heading);
}

.sidebar-section {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-section.flex-grow {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-bottom: none;
  overflow: hidden;
}

.section-label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text-muted);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.section-header .section-label {
  margin-bottom: 0;
}

/* Agent Selector */
.agent-selector {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.agent-selector select {
  flex: 1;
  padding: 10px 14px;
  border-radius: 12px;
  border: 1px solid var(--color-border);
  background: var(--bg-card-solid);
  font: inherit;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.25s ease;
  color: var(--color-text);
}

.agent-selector select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(58, 123, 213, 0.15);
}

.agent-selector select option {
  color: var(--color-text);
  background: var(--bg-card-solid);
  padding: 10px;
}

.refresh-btn {
  width: 40px;
  height: 40px;
  border: 1px solid var(--color-border);
  background: var(--bg-card-solid);
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s ease;
  color: var(--color-primary-dark);
  flex-shrink: 0;
}

.refresh-btn svg {
  width: 18px;
  height: 18px;
}

.refresh-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--bg-hover);
}

.no-agents {
  text-align: center;
  padding: 12px;
  background: var(--bg-hover);
  border-radius: 10px;
}

.no-agents p {
  margin: 0 0 8px;
  font-size: 0.85rem;
  color: var(--color-text-light);
}

.link-btn {
  padding: 6px 12px;
  background: transparent;
  border: none;
  color: var(--color-primary);
  font: inherit;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  text-decoration: underline;
}

.link-btn:hover {
  color: var(--color-primary-dark);
}

.new-session-btn {
  padding: 6px 12px;
  border: none;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--color-text-inverse);
  border-radius: 8px;
  font: inherit;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.25s ease;
}

.new-session-btn svg {
  width: 14px;
  height: 14px;
}

.new-session-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(58, 138, 214, 0.3);
}

.new-session-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Session List */
.session-list {
  flex: 1;
  overflow-y: auto;
  margin-top: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 4px;
}

.session-item:hover {
  background: var(--bg-hover);
}

.session-item.active {
  background: var(--bg-active);
  box-shadow: inset 0 0 0 1px rgba(58, 123, 213, 0.2);
}

.session-icon {
  width: 28px;
  height: 28px;
  padding: 6px;
  background: var(--color-primary-subtle);
  border-radius: 8px;
  color: var(--color-primary);
  flex-shrink: 0;
}

.session-icon svg {
  width: 100%;
  height: 100%;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-name {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 2px;
}

.session-time {
  font-size: 0.7rem;
  color: var(--color-text-light);
}

.session-runtime-btn,
.delete-btn {
  width: 28px;
  height: 28px;
  padding: 6px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-light);
  opacity: 0;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.session-runtime-btn svg,
.delete-btn svg {
  width: 100%;
  height: 100%;
}

.session-runtime-btn:hover:not(:disabled) {
  color: var(--color-primary);
  background: var(--color-primary-subtle);
}

.session-runtime-btn:disabled {
  cursor: not-allowed;
  opacity: 0;
}

.delete-btn:hover {
  color: var(--color-error);
  background: rgba(201, 74, 53, 0.08);
}

.session-item:hover .session-runtime-btn,
.session-item:hover .delete-btn {
  opacity: 1;
}

.streaming-indicator {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 4px;
  flex-shrink: 0;
}

.spinner-icon {
  width: 18px;
  height: 18px;
  color: var(--color-primary);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.delete-btn:hover {
  background: var(--color-error-subtle);
  color: var(--color-error);
}

.empty-sessions {
  text-align: center;
  padding: 20px;
  color: var(--color-text-light);
}

.empty-sessions p {
  margin: 0;
  font-size: 0.9rem;
}

.empty-sessions .hint {
  margin-top: 4px;
  font-size: 0.8rem;
}

/* Chat Panel */
.chat-panel {
  flex: 1;
  min-width: 0;
  background: var(--bg-card);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  box-shadow: var(--shadow-md);
  backdrop-filter: blur(12px);
  display: grid;
  grid-template-rows: 1fr auto;
  overflow: hidden;
}

.chat-messages {
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Empty Chat */
.empty-chat {
  text-align: center;
  padding: 60px 20px;
  color: var(--color-text-muted);
}

.empty-chat .empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  padding: 16px;
}

/* Messages */
.message {
  display: flex;
  gap: 10px;
  max-width: 85%;
  animation: message-in 0.3s ease;
}

@keyframes message-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message.assistant {
  align-self: flex-start;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-avatar svg {
  width: 16px;
  height: 16px;
}

.message-avatar.user {
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--color-text-inverse);
  box-shadow: 0 3px 8px rgba(58, 138, 214, 0.3);
}

.message-avatar.assistant {
  background: linear-gradient(135deg, var(--color-warning-subtle), transparent);
  color: var(--color-warning);
}

.message-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.message.user .message-header {
  flex-direction: row-reverse;
}

.message-role {
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  color: var(--color-primary-dark);
}

.message-time {
  font-size: 0.65rem;
  color: var(--color-text-light);
}

.typing-indicator {
  font-size: 0.65rem;
  color: var(--color-primary);
  font-style: italic;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.message-content {
  padding: 10px 14px;
  border-radius: 12px;
  word-break: break-word;
  font-size: 0.85rem;
  display: inline-block;
  width: fit-content;
  max-width: 100%;
}

.message.user .message-content {
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--color-text-inverse);
  box-shadow: 0 3px 8px rgba(58, 123, 213, 0.2);
}

.message.assistant .message-content {
  background: var(--bg-card-solid);
  border: 1px solid var(--color-border);
}

.message.system .message-content {
  background: var(--color-error-subtle);
  border: 1px solid var(--color-error);
  color: var(--color-error);
}

.system-message {
  font-weight: 500;
}

/* Tool and Skill message styles */
.message.tool .message-content,
.message.skill .message-content {
  background: transparent;
  border: none;
  padding: 0;
  width: 100%;
}

.tool-calls-container,
.tool-results-container,
.skill-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.cursor {
  animation: blink 1s step-end infinite;
  color: var(--color-primary);
  font-weight: bold;
}

@keyframes blink {
  50% { opacity: 0; }
}

/* Chat Input */
.chat-input {
  padding: 14px 18px 16px;
  border-top: 1px solid var(--color-border);
  background: transparent;
}

.input-container {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: var(--bg-card-solid);
  box-shadow: 0 10px 28px var(--bg-overlay);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.input-container:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 12px 30px rgba(58, 138, 214, 0.16);
}

.file-input {
  display: none;
}

.attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-right: 44px;
}

.attachment-chip {
  min-width: 0;
  max-width: min(360px, 100%);
  height: 32px;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 0 7px 0 9px;
  border: 1px solid var(--color-success-subtle);
  border-radius: 8px;
  background: var(--color-success-subtle);
  color: var(--color-success-dark);
  font-size: 0.78rem;
}

.attachment-chip svg {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.attachment-chip span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-chip small {
  color: var(--color-success-dark);
  flex-shrink: 0;
  font-size: 0.68rem;
}

.attachment-chip button {
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--color-success-dark);
  cursor: pointer;
  padding: 3px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.attachment-chip button:hover {
  background: var(--color-success-subtle);
}

.input-expand-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 32px;
  height: 32px;
  border: 1px solid var(--color-border);
  background: var(--bg-elevated);
  color: var(--color-primary-dark);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.input-expand-btn:hover {
  border-color: rgba(58, 138, 214, 0.42);
  color: var(--color-primary);
}

.input-tool-icon {
  width: 18px;
  height: 18px;
  display: block;
  fill: none;
  stroke: currentColor;
  stroke-width: 2.2;
  stroke-linecap: round;
  stroke-linejoin: round;
  flex-shrink: 0;
}

.chat-input textarea {
  width: 100%;
  resize: none;
  min-height: 84px;
  padding: 6px 42px 6px 4px;
  border: none;
  background: transparent;
  font: inherit;
  font-size: 0.9rem;
  line-height: 1.5;
  transition: all 0.25s ease;
}

.chat-input.expanded textarea {
  min-height: 220px;
}

.chat-input textarea:focus {
  outline: none;
}

.chat-input textarea::placeholder {
  color: var(--color-text-light);
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-height: 38px;
}

.attach-btn {
  width: 38px;
  height: 38px;
  border: 1px solid var(--color-border);
  background: var(--bg-elevated);
  color: var(--color-primary-dark);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.attach-btn:hover:not(:disabled) {
  border-color: rgba(58, 138, 214, 0.42);
  color: var(--color-primary);
  transform: translateY(-1px);
}

.attach-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.primary {
  padding: 10px 16px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--color-text-inverse);
  border: none;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 3px 8px rgba(58, 123, 213, 0.2);
  transition: all 0.25s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 0.85rem;
}

.primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(58, 138, 214, 0.3);
}

.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn .btn-icon {
  width: 16px;
  height: 16px;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: var(--color-text-inverse);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.mini-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Toggle Sidebar FAB */
.toggle-runtime-fab {
  position: fixed;
  bottom: 80px;
  left: 24px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--color-text-inverse);
  box-shadow: var(--shadow-glow);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 100;
}

.toggle-runtime-fab svg {
  width: 24px;
  height: 24px;
  transition: transform 0.3s ease;
}

.toggle-runtime-fab:hover {
  transform: translateY(-4px) scale(1.1);
  box-shadow: 0 8px 24px color-mix(in srgb, var(--color-primary) 50%, transparent);
}

.toggle-runtime-fab:active {
  transform: translateY(-2px) scale(1.05);
}

.toggle-sidebar-fab {
  position: fixed;
  bottom: 24px;
  left: 24px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--color-text-inverse);
  box-shadow: 0 4px 12px rgba(58, 138, 214, 0.3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 100;
}

.toggle-sidebar-fab svg {
  width: 24px;
  height: 24px;
  transition: transform 0.3s ease;
}

.toggle-sidebar-fab:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(58, 138, 214, 0.4);
}

.toggle-sidebar-fab:active {
  transform: translateY(0);
}

.new-session-fab {
  position: fixed;
  bottom: 140px;
  right: 24px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--color-text-inverse);
  box-shadow: var(--shadow-glow);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 100;
}

.new-session-fab svg {
  width: 24px;
  height: 24px;
  transition: transform 0.3s ease;
}

.new-session-fab:hover:not(:disabled) {
  transform: translateY(-4px) scale(1.1);
  box-shadow: 0 8px 24px color-mix(in srgb, var(--color-primary) 50%, transparent);
}

.new-session-fab:active:not(:disabled) {
  transform: translateY(-2px) scale(1.05);
}

.new-session-fab:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.runtime-sidebar {
  width: 360px;
  min-width: 360px;
  background: var(--bg-card);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  box-shadow: var(--shadow-md);
  backdrop-filter: blur(12px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1),
              min-width 0.25s cubic-bezier(0.4, 0, 0.2, 1),
              opacity 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.runtime-sidebar.collapsed {
  width: 0;
  min-width: 0;
  border: none;
  box-shadow: none;
  opacity: 0;
}

.runtime-content {
  width: 360px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.runtime-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--text-muted, var(--color-text-muted));
}

.runtime-placeholder .placeholder-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.runtime-placeholder .placeholder-icon svg {
  width: 100%;
  height: 100%;
}

.runtime-placeholder p {
  margin: 0.5rem 0;
}

.runtime-placeholder .hint {
  font-size: 0.875rem;
  opacity: 0.7;
}

.runtime-refresh-btn {
  min-height: 34px;
  border: 1px solid var(--color-border);
  background: var(--bg-card-solid);
  border-radius: 10px;
  color: var(--color-primary-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
}

.runtime-refresh-btn svg {
  width: 17px;
  height: 17px;
}

.runtime-refresh-btn:disabled {
  opacity: 0.6;
  cursor: wait;
}

.spinning {
  animation: spin 1s linear infinite;
}

.runtime-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  padding: 12px 18px;
}

.runtime-tab {
  min-height: 34px;
  border: 1px solid var(--color-border);
  background: var(--bg-card-solid);
  border-radius: 10px;
  color: var(--color-text-muted);
  font: inherit;
  font-size: 0.82rem;
  cursor: pointer;
}

.runtime-tab.active {
  background: var(--color-primary-dark);
  border-color: var(--color-primary-dark); color: var(--color-text-inverse);
}

.runtime-error {
  margin: 0 18px 10px;
  padding: 9px 10px;
  border-radius: 10px;
  color: var(--color-error);
  background: var(--color-error-subtle);
  font-size: 0.82rem;
}

.runtime-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0 18px 18px;
}

.collector-grid span,
.model-row small {
  display: block;
  color: var(--color-text-light);
  font-size: 0.72rem;
}

.runtime-section {
  padding: 12px;
  margin-bottom: 12px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: var(--bg-card-solid);
}

.runtime-section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.runtime-section-title span {
  color: var(--color-primary-dark);
  font-size: 0.88rem;
  font-weight: 700;
}

.runtime-section-title small {
  color: var(--color-text-light);
  font-size: 0.72rem;
}

.collector-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.collector-grid div {
  padding: 10px 8px;
  background: var(--bg-stripe);
  border-radius: 10px;
  text-align: center;
}

.collector-grid strong {
  display: block;
  color: var(--color-primary-dark);
  font-size: 1.05rem;
}

.runtime-empty {
  padding: 18px 8px;
  color: var(--color-text-light);
  font-size: 0.82rem;
  text-align: center;
}

.model-row {
  border-top: 1px solid var(--color-border);
  padding-top: 10px;
  margin-top: 10px;
}

.model-row strong {
  display: block;
  color: var(--color-text);
  font-size: 0.84rem;
}

.run-info-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 0.76rem;
}

.run-info-table tr + tr {
  border-top: 1px solid var(--color-border);
}

.run-info-table th,
.run-info-table td {
  padding: 8px 0;
  text-align: left;
  vertical-align: top;
}

.run-info-table th {
  width: 78px;
  color: var(--color-text-light);
  font-weight: 500;
}

.run-info-table td {
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-row-main {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
}

.model-row-main span {
  color: var(--color-text-light);
  font-size: 0.76rem;
}

.model-meter {
  height: 7px;
  margin: 8px 0 6px;
  border-radius: 999px;
  background: var(--bg-stripe);
  overflow: hidden;
}

.model-meter i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--color-primary), var(--color-success));
}

.trace-section {
  padding-bottom: 8px;
}

.trace-tree {
  display: flex;
  flex-direction: column;
  gap: 1px;
  padding: 4px 0;
}

.trace-node {
  width: 100%;
}

.span-row {
  width: 100%;
  min-height: 34px;
  display: flex;
  align-items: center;
  gap: 5px;
  border: none;
  background: transparent;
  padding: 3px 4px;
  border-radius: 6px;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: background 0.12s ease;
}

.span-row:hover {
  background: var(--bg-hover);
}

.span-row.selected {
  background: var(--color-primary-subtle);
  box-shadow: inset 2px 0 0 var(--color-primary);
}

.span-row.error strong {
  color: var(--color-error);
}

.span-prefix {
  color: var(--color-text-light);
  flex-shrink: 0;
  font-family: Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 0.72rem;
  line-height: 1;
  white-space: pre;
}

.span-prefix:empty {
  width: 0;
}

.span-toggle {
  width: 14px;
  height: 14px;
  border: none;
  background: transparent;
  color: var(--color-text-muted);
  padding: 0;
  flex-shrink: 0;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.span-toggle:disabled {
  opacity: 0;
  cursor: default;
}

.span-toggle svg {
  width: 12px;
  height: 12px;
  transition: transform 0.18s ease;
}

.span-toggle svg.expanded {
  transform: rotate(90deg);
}

.span-row-content {
  flex: 1;
  min-width: 0;
}

.span-row-main {
  display: flex;
  justify-content: space-between;
  gap: 6px;
  align-items: center;
}

.span-row-main strong {
  display: block;
  color: var(--color-text);
  font-size: 0.76rem;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.25;
}

.span-duration {
  color: var(--color-text-light);
  font-size: 0.68rem;
  flex-shrink: 0;
  font-variant-numeric: tabular-nums;
}

.span-row-sub {
  display: flex;
  justify-content: space-between;
  gap: 6px;
  margin-top: 2px;
  color: var(--color-text-light);
  font-size: 0.68rem;
  min-width: 0;
}

.span-row-sub span:first-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.span-row-sub span:last-child {
  flex-shrink: 0;
  font-variant-numeric: tabular-nums;
}

.span-detail-modal {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.span-detail-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.span-detail-summary div {
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--bg-page);
}

.span-detail-summary span {
  display: block;
  color: var(--color-text-light);
  font-size: 0.72rem;
  margin-bottom: 6px;
}

.span-detail-summary strong {
  color: var(--color-text);
  font-size: 0.88rem;
  word-break: break-word;
}

.span-meta-grid {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr);
  gap: 10px 12px;
  margin: 0;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
}

.span-meta-grid dt {
  color: var(--color-text-light);
  font-size: 0.74rem;
}

.span-meta-grid dd {
  margin: 0;
  color: var(--color-text);
  font-size: 0.76rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.span-json-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.span-json-section {
  min-width: 0;
}

.span-json-section strong {
  display: block;
  margin-bottom: 6px;
  color: var(--color-primary-dark);
  font-size: 0.8rem;
}

.span-json-section pre {
  max-height: 280px;
  overflow: auto;
  margin: 0;
  padding: 10px;
  border-radius: 8px;
  background: var(--bg-codeblock);
  color: var(--color-text);
  font-size: 0.72rem;
  line-height: 1.45;
  white-space: pre-wrap;
  word-break: break-word;
}

.trace-search {
  margin-bottom: 10px;
}

/* Runtime header with refresh button */
.runtime-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 12px 4px;
}
.runtime-header h3 {
  margin: 0;
  font-size: 14px;
}

/* Subsession children */
.subsession-children {
  padding-left: 36px;
  margin-top: 2px;
  margin-bottom: 4px;
}
.subsession-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  color: var(--color-text-light);
  transition: background 0.15s;
}
.subsession-item:hover {
  background: var(--color-bg-hover, #f0f0f0);
}
.subsession-item .sub-icon {
  width: 12px;
  height: 12px;
  flex-shrink: 0;
}
.subsession-item .sub-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.subsession-item .sub-status {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
}
.subsession-item .sub-status.active { background: #e8f5e9; color: #2e7d32; }
.subsession-item .sub-status.closed { background: #f5f5f5; color: #999; }

.subsession-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-light, #666);
}
.status-dot {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 3px;
  white-space: nowrap;
}
.status-dot.running { background: #fff3e0; color: #e65100; }
.status-dot.completed { background: #e8f5e9; color: #2e7d32; }
.status-dot.failed { background: #fbe9e7; color: #c62828; }
.status-dot.interrupted { background: #fce4ec; color: #c62828; }
.status-dot.active { background: #e8f5e9; color: #2e7d32; }
.subagent-item {
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background 0.15s;
}
.subagent-item:hover { background: var(--color-bg-hover, #f5f5f5); }
.subagent-item.selected { background: var(--color-bg-selected, #e3f2fd); }
.subagent-msg {
  padding: 6px 8px;
  margin-bottom: 4px;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.4;
}
.subagent-msg.user { background: #f5f5f5; }
.subagent-msg.assistant { background: #e8f5e9; }
.msg-role { font-size: 11px; color: #999; margin-bottom: 2px; }
.msg-content { white-space: pre-wrap; word-break: break-word; }

/* Animations */
.fade-in {
  animation: fade-in 0.4s ease forwards;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* Scrollbar */
.chat-messages::-webkit-scrollbar,
.session-list::-webkit-scrollbar {
  width: 8px;
}

.chat-messages::-webkit-scrollbar-track,
.session-list::-webkit-scrollbar-track {
  background: var(--bg-stripe);
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb,
.session-list::-webkit-scrollbar-thumb {
  background: var(--color-border-strong);
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb:hover,
.session-list::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-light);
}

/* Responsive */
@media (max-width: 900px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: calc(100vh - 120px);
    z-index: 50;
  }
  
  .sidebar.collapsed {
    left: -360px;
  }

  .runtime-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: calc(100vh - 120px);
    z-index: 50;
  }

  .runtime-sidebar.collapsed {
    left: -360px;
  }
}

@media (max-width: 768px) {
  .chat-page {
    height: calc(100vh - 100px);
  }
  
  .message {
    max-width: 95%;
  }
  
  .toggle-sidebar-fab {
    width: 48px;
    height: 48px;
    bottom: 20px;
    left: 20px;
  }
  
  .toggle-sidebar-fab svg {
    width: 20px;
    height: 20px;
  }
}
</style>
