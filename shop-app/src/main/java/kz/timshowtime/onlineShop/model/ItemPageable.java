package kz.timshowtime.onlineShop.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ItemPageable {
    private int pageNumber;
    private int pageSize;
    private long totalPages;

    public int pageNumber() {
        return pageNumber;
    }

    public int pageSize() {
        return pageSize;
    }

    public boolean hasPrevious() {
        return pageNumber + 1 > 1;
    }

    public boolean hasNext() {
        int nextPage = (pageNumber + 1) * pageSize;
        return nextPage < totalPages;
    }

    public static ItemPageable getPageable(Integer pageNumber, Integer pageSize, Long count) {
        return new ItemPageable(pageNumber, pageSize, count);
    }
}
