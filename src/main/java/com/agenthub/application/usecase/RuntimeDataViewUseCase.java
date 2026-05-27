package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.multi.RowKeyTable;
import cn.hutool.core.map.multi.Table;
import com.agenthub.application.dto.*;
import com.agenthub.application.port.out.repositories.RunRegistrationRepository;
import com.agenthub.application.port.out.repositories.SessionRepository;
import com.agenthub.application.port.out.repositories.SpanRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.agent.Session;
import com.agenthub.domain.model.studio.RunRegistration;
import com.agenthub.domain.model.trace.Span;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RuntimeDataViewUseCase {
    private final SessionRepository sessionRepository;
    private final RunRegistrationRepository runRepository;
    private final SpanRepository spanRepository;
    private final AgentUseCase agentUseCase;

    public RuntimeDataViewOutput get(String agentId, String sessionId) {
        agentUseCase.get(agentId);
        Session selected = findSession(agentId, sessionId);
        List<Span> spans = sortedSpans(agentId, sessionId);
        return buildOutput(agentId, selected, spans);
    }

    private Session findSession(String agentId, String sessionId) {
        try {
            return sessionRepository.existSession(sessionId, agentId);
        } catch (NotFoundException e) {
            return emptySession(agentId, sessionId);
        }
    }

    private Session emptySession(String agentId, String sessionId) {
        return new Session(sessionId, agentId, sessionId, null, null, Instant.now());
    }

    private RuntimeDataViewOutput buildOutput(String agentId, Session selected, List<Span> spans) {
        List<RuntimeRunOutput> runs = runs(agentId);
        RuntimeRunOutput selectedRun = run(selected);
        return new RuntimeDataViewOutput(runs, selectedRun, trace(selectedRun.getId(), spans),
                buildSpanTree(spans), errorSpans(spans), slowSpans(spans), modelData(spans));
    }

    private List<RuntimeRunOutput> runs(String agentId) {
        return sessionRepository.findByAgentId(agentId).stream()
                .map(this::run)
                .sorted(Comparator.comparing(RuntimeRunOutput::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private RuntimeRunOutput run(Session session) {
        return runRepository.findById(session.getId())
                .map(r -> registeredRun(session, r))
                .orElseGet(() -> sessionRun(session));
    }

    private RuntimeRunOutput registeredRun(Session session, RunRegistration run) {
        return new RuntimeRunOutput(run.getId(), session.getAgentId(), run.getProject(), run.getName(),
                run.getTimestamp(), run.getPid(), run.getStatus(), run.getRunDir());
    }

    private RuntimeRunOutput sessionRun(Session session) {
        return new RuntimeRunOutput(session.getId(), session.getAgentId(), session.getAgentId(), name(session),
                session.getCreatedAt(), null, "RUNNING", null);
    }

    private String name(Session session) {
        return session.getName() == null || session.getName().isBlank() ? session.getId() : session.getName();
    }

    private List<Span> sortedSpans(String agentId, String runId) {
        return spanRepository.findByRunId(runId).stream()
                .filter(span -> agentId.equals(span.getAgentId()))
                .sorted(Comparator.comparing(this::startNanos).reversed())
                .toList();
    }

    private Long startNanos(Span span) {
        return nanos(span.getStartTimeUnixNano());
    }

    private RuntimeTraceOutput trace(String runId, List<Span> spans) {
        return new RuntimeTraceOutput(runId, firstStart(spans), lastEnd(spans),
                latency(spans), status(spans), spans.size(), totalTokens(spans),
                errorCount(spans), slowestSpanId(spans), slowestSpanName(spans), slowestLatency(spans));
    }


    private String firstStart(List<Span> spans) {
        return nanoString(minStart(spans));
    }

    private String lastEnd(List<Span> spans) {
        return nanoString(maxEnd(spans));
    }

    private String nanoString(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long latency(List<Span> spans) {
        Long start = minStart(spans);
        Long end = maxEnd(spans);
        return start == null || end == null ? 0L : end - start;
    }

    private Long minStart(List<Span> spans) {
        return spans.stream().map(this::startNanos).filter(v -> v > 0).min(Long::compareTo).orElse(null);
    }

    private Long maxEnd(List<Span> spans) {
        return spans.stream().map(s -> nanos(s.getEndTimeUnixNano())).filter(v -> v > 0).max(Long::compareTo).orElse(null);
    }

    private String status(List<Span> spans) {
        return spans.stream().anyMatch(this::errorSpan) ? "ERROR" : "OK";
    }

    private boolean errorSpan(Span span) {
        return span.hasError() || "ERROR".equalsIgnoreCase(span.getStatus());
    }

    private Integer errorCount(List<Span> spans) {
        return (int) spans.stream().filter(this::errorSpan).count();
    }

    private String slowestSpanId(List<Span> spans) {
        return slowest(spans).map(Span::getSpanId).orElse(null);
    }

    private String slowestSpanName(List<Span> spans) {
        return slowest(spans).map(Span::getName).orElse(null);
    }

    private Long slowestLatency(List<Span> spans) {
        return slowest(spans).map(Span::getLatencyNs).orElse(0L);
    }

    private Optional<Span> slowest(List<Span> spans) {
        return spans.stream().max(Comparator.comparing(this::latencyNanos));
    }

    private Long latencyNanos(Span span) {
        return span.getLatencyNs() == null ? 0L : span.getLatencyNs();
    }

    private List<RuntimeSpanSummaryOutput> errorSpans(List<Span> spans) {
        return spans.stream().filter(this::errorSpan).map(this::summary).toList();
    }

    private List<RuntimeSpanSummaryOutput> slowSpans(List<Span> spans) {
        return spans.stream().sorted(Comparator.comparing(this::latencyNanos).reversed()).limit(5).map(this::summary).toList();
    }

    private RuntimeSpanSummaryOutput summary(Span span) {
        return new RuntimeSpanSummaryOutput(span.getSpanId(), span.getParentSpanId(), span.getName(),
                latencyNanos(span), span.getStatusCode(), span.getStatus(), span.getModel());
    }

    private Long totalTokens(List<Span> spans) {
        return spans.stream().map(Span::getTotalTokens).filter(Objects::nonNull).reduce(0L, Long::sum);
    }

    private ModelInvocationDataOutput modelData(List<Span> spans) {
        List<Span> invocations = invocationSpans(spans);
        return new ModelInvocationDataOutput(invocations.size(), chatStats(invocations));
    }

    private List<Span> invocationSpans(List<Span> spans) {
        return spans.stream().filter(this::modelInvocation).toList();
    }

    private boolean modelInvocation(Span span) {
        return span.getModel() != null || attr(span, "gen_ai.request.model") != null || span.getTotalTokens() != null;
    }

    private ChatInvocationStatsOutput chatStats(List<Span> spans) {
        return new ChatInvocationStatsOutput(spans.size(), avg(spans), total(spans),
                callsByModel(spans), avgByModel(spans), totalByModel(spans));
    }

    private TokenStatsOutput avg(List<Span> spans) {
        int count = Math.max(spans.size(), 1);
        TokenStatsOutput total = total(spans);
        return new TokenStatsOutput(total.getPromptTokens() / count, total.getCompletionTokens() / count, total.getTotalTokens() / count);
    }

    private TokenStatsOutput total(List<Span> spans) {
        return new TokenStatsOutput(sumInput(spans), sumOutput(spans), sumTotal(spans));
    }

    private double sumInput(List<Span> spans) {
        return spans.stream().map(Span::getInputTokens).filter(Objects::nonNull).mapToDouble(Long::doubleValue).sum();
    }

    private double sumOutput(List<Span> spans) {
        return spans.stream().map(Span::getOutputTokens).filter(Objects::nonNull).mapToDouble(Long::doubleValue).sum();
    }

    private double sumTotal(List<Span> spans) {
        return spans.stream().map(Span::getTotalTokens).filter(Objects::nonNull).mapToDouble(Long::doubleValue).sum();
    }

    private List<ModelInvocationByModelOutput> callsByModel(List<Span> spans) {
        return groups(spans).entrySet().stream()
                .map(e -> new ModelInvocationByModelOutput(e.getKey(), e.getValue().size()))
                .toList();
    }

    private List<ModelTokenStatsOutput> avgByModel(List<Span> spans) {
        return groups(spans).entrySet().stream()
                .map(e -> avgModel(e.getKey(), e.getValue()))
                .toList();
    }

    private List<ModelTokenStatsOutput> totalByModel(List<Span> spans) {
        return groups(spans).entrySet().stream()
                .map(e -> totalModel(e.getKey(), e.getValue()))
                .toList();
    }

    private ModelTokenStatsOutput avgModel(String model, List<Span> spans) {
        TokenStatsOutput avg = avg(spans);
        return new ModelTokenStatsOutput(model, avg.getPromptTokens(), avg.getCompletionTokens(), avg.getTotalTokens());
    }

    private ModelTokenStatsOutput totalModel(String model, List<Span> spans) {
        TokenStatsOutput total = total(spans);
        return new ModelTokenStatsOutput(model, total.getPromptTokens(), total.getCompletionTokens(), total.getTotalTokens());
    }

    private Map<String, List<Span>> groups(List<Span> spans) {
        Map<String, List<Span>> groups = new LinkedHashMap<>();
        spans.forEach(span -> groups.computeIfAbsent(modelName(span), k -> new ArrayList<>()).add(span));
        return groups;
    }

    private String modelName(Span span) {
        Object model = span.getModel() == null ? attr(span, "gen_ai.request.model") : span.getModel();
        return model == null ? "unknown" : String.valueOf(model);
    }

    private Object attr(Span span, String key) {
        return span.getAttributes() == null ? null : span.getAttributes().get(key);
    }


    private List<SpanTreeNodeOutput> buildSpanTree(List<Span> spans) {
        List<SpanTreeNodeOutput> roots = new ArrayList<>();
        for (List<Span> traceSpans : spansByTrace(spans).values()) {
            roots.addAll(buildTraceTree(traceSpans));
        }
        sortNodes(roots);
        return roots;
    }

    private Map<String, List<Span>> spansByTrace(List<Span> spans) {
        Map<String, List<Span>> result = new HashMap<>();
        for (Span span : spans) {
            result.computeIfAbsent(span.getTraceId(), key -> new ArrayList<>()).add(span);
        }
        return result;
    }

    private List<SpanTreeNodeOutput> buildTraceTree(List<Span> spans) {
        List<SpanTreeNodeOutput> nodes = spans.stream().map(this::treeNode).toList();
        Map<String, List<SpanTreeNodeOutput>> children = childrenByParent(nodes);
        Set<String> spanIds = spanIds(nodes);
        nodes.forEach(node -> node.setChildren(sortedChildren(node, children)));
        return nodes.stream().filter(node -> rootNode(node, spanIds)).sorted(nodeComparator()).toList();
    }

    private SpanTreeNodeOutput treeNode(Span span) {
        SpanTreeNodeOutput node = new SpanTreeNodeOutput();
        BeanUtil.copyProperties(span, node);
        return node;
    }

    private Map<String, List<SpanTreeNodeOutput>> childrenByParent(List<SpanTreeNodeOutput> nodes) {
        Map<String, List<SpanTreeNodeOutput>> result = new HashMap<>();
        for (SpanTreeNodeOutput node : nodes) {
            if (!blank(node.getParentSpanId())) {
                result.computeIfAbsent(node.getParentSpanId(), key -> new ArrayList<>()).add(node);
            }
        }
        return result;
    }

    private Set<String> spanIds(List<SpanTreeNodeOutput> nodes) {
        Set<String> result = new HashSet<>();
        for (SpanTreeNodeOutput node : nodes) {
            if (!blank(node.getSpanId())) {
                result.add(node.getSpanId());
            }
        }
        return result;
    }

    private List<SpanTreeNodeOutput> sortedChildren(SpanTreeNodeOutput node, Map<String, List<SpanTreeNodeOutput>> children) {
        if (blank(node.getSpanId())) {
            return new ArrayList<>();
        }
        List<SpanTreeNodeOutput> result = new ArrayList<>(children.getOrDefault(node.getSpanId(), Collections.emptyList()));
        sortNodes(result);
        return result;
    }

    private boolean rootNode(SpanTreeNodeOutput node, Set<String> spanIds) {
        return blank(node.getParentSpanId()) || !spanIds.contains(node.getParentSpanId());
    }

    private void sortNodes(List<SpanTreeNodeOutput> nodes) {
        nodes.sort(nodeComparator());
    }

    private Comparator<SpanTreeNodeOutput> nodeComparator() {
        return Comparator.comparing(node -> nanos(node.getStartTimeUnixNano()), Comparator.reverseOrder());
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private long nanos(String value) {
        return value == null || value.isBlank() ? 0L : Long.parseLong(value);
    }
}
