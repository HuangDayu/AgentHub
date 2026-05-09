package com.agenthub.infrastructure.web;

import com.agenthub.api.dto.ErrorResponse;
import com.agenthub.common.exception.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 全局API异常处理器。
 * <p>
 * 统一处理各类异常并返回标准错误响应格式。
 * </p>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * 日志记录器
     */
    private static final Log log = LogFactory.getLog(ApiExceptionHandler.class);

    /**
     * 核心异常处理方法，生成错误响应并记录日志。
     *
     * @param exception 异常对象
     * @param status    HTTP状态码
     * @return 错误响应对象
     */
    private ErrorResponse handlerException(Exception exception, int status) {
        String messageId = randomId();
        String requestPath = getRequestPath();
        logException(exception, status, messageId, requestPath);
        return createErrorResponse(status, exception.getMessage(), messageId);
    }

    /**
     * 记录异常日志信息。
     */
    private void logException(Exception exception, int status, String messageId, String requestPath) {
        String message = exception.getMessage();
        String logMsg = String.format("Api [%s] exception, status: %s, messageId: %s, message: %s, stackTrace:\n",
                requestPath, status, messageId, message);
        log.error(logMsg, exception);
    }

    /**
     * 创建错误响应对象。
     */
    private ErrorResponse createErrorResponse(int status, String message, String messageId) {
        return new ErrorResponse(Instant.now(), status, message, messageId);
    }

    /**
     * 获取当前请求的URI路径。
     *
     * @return 请求URI路径，若无请求上下文则返回null
     */
    public static String getRequestPath() {
        ServletRequestAttributes attributes = getServletRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest().getRequestURI();
    }

    /**
     * 获取Servlet请求属性。
     */
    private static ServletRequestAttributes getServletRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    /**
     * 处理非法状态异常，返回409冲突响应。
     *
     * @param exception 非法状态异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIllegalState(IllegalStateException exception) {
        return handlerException(exception, HttpStatus.CONFLICT.value());
    }

    /**
     * 处理不支持操作异常，返回400错误请求响应。
     *
     * @param exception 不支持操作异常
     * @return 错误响应
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnsupported(UnsupportedOperationException exception) {
        return handlerException(exception, HttpStatus.BAD_REQUEST.value());
    }

    /**
     * 处理通用异常，返回500内部服务器错误响应。
     *
     * @param exception 异常对象
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleRuntime(Exception exception) {
        if (exception.getClass().getName().startsWith("io.jsonwebtoken")) {
            ErrorResponse errorResponse = handlerException(exception, HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        ErrorResponse errorResponse = handlerException(exception, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理资源未找到异常，返回404响应。
     *
     * @param exception 未找到异常
     * @return 错误响应实体
     */
    @ExceptionHandler({NotFoundException.class, AuditExportJobNotFoundException.class, InvoiceNotFoundException.class,
            JobNotFoundException.class, ModelNotFoundException.class, NotificationNotFoundException.class,
            PolicyNotFoundException.class, ToolNotFoundException.class, RouteNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException exception) {
        ErrorResponse body = handlerException(exception, HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * 处理ResponseStatusException，返回对应的状态码。
     *
     * @param exception ResponseStatusException
     * @return 错误响应实体
     */
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            org.springframework.web.server.ResponseStatusException exception) {
        HttpStatusCode status = exception.getStatusCode();
        String message = getResponseStatusMessage(exception);
        ErrorResponse body = handlerException(new RuntimeException(message), status.value());
        return new ResponseEntity<>(body, status);
    }

    /**
     * 获取ResponseStatusException的消息。
     */
    private String getResponseStatusMessage(org.springframework.web.server.ResponseStatusException exception) {
        return exception.getReason() != null ? exception.getReason() : exception.getMessage();
    }

    /**
     * 处理资源冲突异常，返回409响应。
     *
     * @param exception 冲突异常
     * @return 错误响应实体
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException exception) {
        ErrorResponse body = handlerException(exception, HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * 处理参数校验异常，返回400响应。
     *
     * @param exception 校验异常
     * @return 错误响应实体
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException exception) {
        ErrorResponse body = handlerException(exception, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 处理模型回退异常，返回400响应。
     *
     * @param exception 模型回退异常
     * @return 错误响应实体
     */
    @ExceptionHandler(ModelFallbackException.class)
    public ResponseEntity<ErrorResponse> handleModelFallbackException(ModelFallbackException exception) {
        ErrorResponse body = handlerException(exception, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 处理非法参数异常，根据消息内容返回404或400响应。
     *
     * @param exception 非法参数异常
     * @return 错误响应实体
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        if (isNotFoundMessage(exception.getMessage())) {
            return createNotFoundResponse(exception);
        }
        return createBadRequestResponse(exception);
    }

    /**
     * 判断消息是否表示资源未找到。
     */
    private boolean isNotFoundMessage(String msg) {
        return msg != null && (msg.contains("not found") || msg.contains("Not found"));
    }

    /**
     * 创建404未找到响应。
     */
    private ResponseEntity<ErrorResponse> createNotFoundResponse(IllegalArgumentException exception) {
        ErrorResponse body = handlerException(exception, HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * 创建400错误请求响应。
     */
    private ResponseEntity<ErrorResponse> createBadRequestResponse(IllegalArgumentException exception) {
        ErrorResponse body = handlerException(exception, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 处理无效凭据异常，返回401未授权响应。
     *
     * @return 401未授权响应
     */
    @ExceptionHandler({InvalidCredentialsException.class, io.jsonwebtoken.MalformedJwtException.class})
    public ResponseEntity<Void> handleInvalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * 处理无效刷新令牌异常，返回401未授权响应。
     *
     * @return 401未授权响应
     */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<Void> handleInvalidRefreshToken() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
