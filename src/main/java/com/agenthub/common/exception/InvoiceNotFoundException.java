package com.agenthub.common.exception;

/**
 * 发票未找到异常。
 *
 * <p>当根据发票 ID 查询不到对应记录时抛出。</p>
 */
public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(String invoiceId) {
        super("Invoice not found: " + invoiceId);
    }
}
