package com.agenthub.domain.model;

import java.util.List;

/**
 * 分页结果封装。
 *
 * <p>封装分页查询的返回数据，包括当前页内容、总记录数、当前页码和每页大小。</p>
 *
 * @param <T> 数据元素类型
 * @param content       当前页数据列表
 * @param totalElements 总记录数
 * @param page          当前页码（从0开始）
 * @param size          每页大小
 */
public record PageResult<T>(
        List<T> content,
        long totalElements,
        int page,
        int size
) {
    /**
     * 计算总页数。
     *
     * @return 总页数
     */
    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    /**
     * 判断是否有下一页。
     *
     * @return 有下一页返回 true
     */
    public boolean hasNext() {
        return page + 1 < totalPages();
    }

    /**
     * 判断是否有上一页。
     *
     * @return 有上一页返回 true
     */
    public boolean hasPrevious() {
        return page > 0;
    }
}
