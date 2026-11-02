package com.yz.minded.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.exception.BusinessException;

import java.util.List;

public class PageUtils {
    public static <T> Page<T> of(List<T> list, long pageNum, long pageSize) {
        if (list == null) list = List.of();
        if (pageNum < 1 || pageSize < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页参数非法");
        }
        int total = list.size();
        int totalPage = (int) Math.ceil((double) total / pageSize);
        // 空列表时返回空页，不再抛异常（比原来友好）
        List<T> records;
        if (total == 0 || pageNum > totalPage) {
            records = List.of();
        } else {
            int start = (int) ((pageNum - 1) * pageSize);
            int end = (int) Math.min(pageNum * pageSize, total);
            records = list.subList(start, end);
        }
        Page<T> page = new Page<>(pageNum, pageSize, total);
        page.setPages(totalPage);
        page.setRecords(records);
        return page;
    }
}