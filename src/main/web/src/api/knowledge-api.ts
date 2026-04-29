import {runtimeConfig} from '@/common/runtime-config'
import type {Document, KnowledgeBase, SelectionState} from '@/domain/types'
import {scopedHeaders} from '@/services/workspace-service'
import {requestJson} from './http'

// ── Knowledge Bases ──────────────────────────────────────

export async function listKnowledgeBases(selection: SelectionState) {
    const response = await requestJson<{
        items: KnowledgeBase[]
    }>(`/api/v1/workspaces/${selection.workspaceId}/knowledge-bases`, {
        baseUrl: runtimeConfig.retrievalApiBase,
        method: 'GET',
        headers: scopedHeaders(selection),
    })
    // Map backend 'id' to frontend 'kbId'
    return response.items.map(item => ({
        id: item.id,
        name: item.name,
        description: item.description,
        indexVersions: item.indexVersions,
        activeIndexVersion: item.activeIndexVersion,
        vectorStoreConfigId: item.vectorStoreConfigId,
        embeddingModelConfigId: item.embeddingModelConfigId,
        chatModelConfigId: item.chatModelConfigId,
        createdAt: item.createdAt,
        updatedAt: item.updatedAt,
    }))
}

export function createKnowledgeBase(payload: {
    selection: SelectionState
    kbId: string
    name: string
    description: string
    indexVersions: string[]
    activeIndexVersion: string
    vectorStoreConfigId?: string
    embeddingModelConfigId?: string
    chatModelConfigId?: string
}) {
    return requestJson<KnowledgeBase>(`/api/v1/workspaces/${payload.selection.workspaceId}/knowledge-bases`, {
        baseUrl: runtimeConfig.retrievalApiBase,
        method: 'POST',
        headers: scopedHeaders(payload.selection),
        bodyJson: {
            kbId: payload.kbId,
            name: payload.name,
            description: payload.description,
            indexVersions: payload.indexVersions,
            activeIndexVersion: payload.activeIndexVersion,
            vectorStoreConfigId: payload.vectorStoreConfigId,
            embeddingModelConfigId: payload.embeddingModelConfigId,
            chatModelConfigId: payload.chatModelConfigId,
        },
    })
}

export function updateKnowledgeBase(payload: {
    selection: SelectionState
    kbId: string
    name?: string
    description?: string
    indexVersions?: string[]
    activeIndexVersion?: string
    vectorStoreConfigId?: string
    embeddingModelConfigId?: string
    chatModelConfigId?: string
}) {
    return requestJson<KnowledgeBase>(`/api/v1/workspaces/${payload.selection.workspaceId}/knowledge-bases/${payload.kbId}`, {
        baseUrl: runtimeConfig.retrievalApiBase,
        method: 'PATCH',
        headers: scopedHeaders(payload.selection),
        bodyJson: {
            name: payload.name,
            description: payload.description,
            indexVersions: payload.indexVersions,
            activeIndexVersion: payload.activeIndexVersion,
            vectorStoreConfigId: payload.vectorStoreConfigId,
            embeddingModelConfigId: payload.embeddingModelConfigId,
            chatModelConfigId: payload.chatModelConfigId,
        },
    })
}

export function deleteKnowledgeBase(selection: SelectionState, kbId: string) {
    return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/knowledge-bases/${kbId}`, {
        baseUrl: runtimeConfig.retrievalApiBase,
        method: 'DELETE',
        headers: scopedHeaders(selection),
    })
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
