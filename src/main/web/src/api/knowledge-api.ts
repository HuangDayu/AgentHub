import {runtimeConfig} from '@/common/runtime-config'
import type {Document, KnowledgeBase, SelectionState} from '@/domain/types'
import {scopedHeaders} from '@/services/workspace-service'
import {requestJson} from './http'

// ── Knowledge Bases ──────────────────────────────────────

export interface CreateKnowledgeBasePayload {
    selection: SelectionState
    kbCode: string
    name: string
    description: string
    indexVersions: string[]
    activeIndexVersion: string
    vectorStoreConfigId?: string
    embeddingModelConfigId?: string
    chatModelConfigId?: string
}

export interface UpdateKnowledgeBasePayload {
    selection: SelectionState
    kbId: string
    name?: string
    description?: string
    indexVersions?: string[]
    activeIndexVersion?: string
    vectorStoreConfigId?: string
    embeddingModelConfigId?: string
    chatModelConfigId?: string
}

export async function listKnowledgeBases(selection: SelectionState): Promise<KnowledgeBase[]> {
    const response = await requestJson<{ items: KnowledgeBase[] }>(knowledgeBaseUrl(selection), {
        baseUrl: runtimeConfig.retrievalApiBase,
        method: 'GET',
        headers: scopedHeaders(selection),
    })
    return response.items
}

export function createKnowledgeBase(payload: CreateKnowledgeBasePayload) {
    return requestJson<KnowledgeBase>(knowledgeBaseUrl(payload.selection), {
        baseUrl: runtimeConfig.retrievalApiBase,
        method: 'POST',
        headers: scopedHeaders(payload.selection),
        bodyJson: buildCreateBody(payload),
    })
}

export function updateKnowledgeBase(payload: UpdateKnowledgeBasePayload) {
    return requestJson<KnowledgeBase>(knowledgeBaseItemUrl(payload.selection, payload.kbId), {
        baseUrl: runtimeConfig.retrievalApiBase,
        method: 'PATCH',
        headers: scopedHeaders(payload.selection),
        bodyJson: buildUpdateBody(payload),
    })
}

export function deleteKnowledgeBase(selection: SelectionState, kbId: string) {
    return requestJson<void>(knowledgeBaseItemUrl(selection, kbId), {
        baseUrl: runtimeConfig.retrievalApiBase,
        method: 'DELETE',
        headers: scopedHeaders(selection),
    })
}

function knowledgeBaseUrl(selection: SelectionState): string {
    return `/api/v1/workspaces/${selection.workspaceId}/knowledge-bases`
}

function knowledgeBaseItemUrl(selection: SelectionState, kbId: string): string {
    return `${knowledgeBaseUrl(selection)}/${kbId}`
}

function buildCreateBody(payload: CreateKnowledgeBasePayload) {
    return { ...buildCreateIdentity(payload), ...buildCreateOptions(payload) }
}

function buildCreateIdentity(payload: CreateKnowledgeBasePayload) {
    return {
        kbCode: payload.kbCode,
        name: payload.name,
        description: payload.description,
        indexVersions: payload.indexVersions,
        activeIndexVersion: payload.activeIndexVersion,
    }
}

function buildCreateOptions(payload: CreateKnowledgeBasePayload) {
    return {
        vectorStoreConfigId: payload.vectorStoreConfigId,
        embeddingModelConfigId: payload.embeddingModelConfigId,
        chatModelConfigId: payload.chatModelConfigId,
    }
}

function buildUpdateBody(payload: UpdateKnowledgeBasePayload) {
    return { ...buildUpdateIdentity(payload), ...buildUpdateOptions(payload) }
}

function buildUpdateIdentity(payload: UpdateKnowledgeBasePayload) {
    return {
        name: payload.name,
        description: payload.description,
        indexVersions: payload.indexVersions,
        activeIndexVersion: payload.activeIndexVersion,
    }
}

function buildUpdateOptions(payload: UpdateKnowledgeBasePayload) {
    return {
        vectorStoreConfigId: payload.vectorStoreConfigId,
        embeddingModelConfigId: payload.embeddingModelConfigId,
        chatModelConfigId: payload.chatModelConfigId,
    }
}

// ── Documents ────────────────────────────────────────────

/**
 * 列出知识库文档。
 * 后端端点：GET /api/v1/workspaces/${selection.workspaceId}/knowledge-bases/{kbId}/documents
 */
export async function listDocuments(selection: SelectionState, kbId: string): Promise<Document[]> {
    const url = new URL(`/api/v1/workspaces/${selection.workspaceId}/knowledge-bases/${kbId}/documents`, runtimeConfig.retrievalApiBase)
    const response = await fetch(url, {method: 'GET', headers: scopedHeaders(selection)})
    if (!response.ok) return []

    const data = await response.json()
    return Array.isArray(data) ? data : []
}

export async function uploadDocument(selection: SelectionState, kbId: string, file: File) {
    const formData = new FormData()
    formData.append('file', file)
    const response = await fetch(
        new URL(`/api/v1/workspaces/${selection.workspaceId}/knowledge-bases/${kbId}/documents`, runtimeConfig.retrievalApiBase),
        {method: 'POST', headers: scopedHeaders(selection), body: formData}
    )
    if (!response.ok) throw new Error(await response.text())
}

export function deleteDocument(selection: SelectionState, kbId: string, docId: string) {
    return requestJson<void>(
        `/api/v1/workspaces/${selection.workspaceId}/knowledge-bases/${kbId}/documents/${docId}`,
        {
            baseUrl: runtimeConfig.retrievalApiBase,
            method: 'DELETE',
            headers: scopedHeaders(selection),
        }
    )
}

/**
 * 向量化文档。
 * 后端端点：POST /api/v1/workspaces/${selection.workspaceId}/knowledge-bases/{kbId}/documents/{docId}
 */
export async function vectorizeDocument(selection: SelectionState, kbId: string, docId: string) {
    return requestJson<void>(
        `/api/v1/workspaces/${selection.workspaceId}/knowledge-bases/${kbId}/documents/${docId}`,
        {
            baseUrl: runtimeConfig.retrievalApiBase,
            method: 'PUT',
            headers: scopedHeaders(selection),
        }
    )
}
