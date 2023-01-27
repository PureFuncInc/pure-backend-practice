package net.purefunc.practice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/*
    {
        "content": [
            {
                "username": "string",
                "type": "SIGNUP",
                "createdBy": "anonymousUser",
                "createdDateStr": "2023-01-27T16:51:26.792+08:00"
            },
            {
                "username": "string",
                "type": "LOGIN",
                "createdBy": "anonymousUser",
                "createdDateStr": "2023-01-27T16:51:29.692+08:00"
            }
        ],
        "pageable": {
            "sort": {
                "empty": true,
                "sorted": false,
                "unsorted": true
            },
            "offset": 0,
            "pageNumber": 0,
            "pageSize": 10,
            "paged": true,
            "unpaged": false
        },
        "last": true,
        "totalPages": 1,
        "totalElements": 2,
        "size": 10,
        "number": 0,
        "first": true,
        "sort": {
            "empty": true,
            "sorted": false,
            "unsorted": true
        },
        "numberOfElements": 2,
        "empty": false
}
 */
public class CustomPageImpl<T> extends PageImpl<T> {

    private static final long serialVersionUID = -6080041821611776363L;
    public JsonNode pageable;
    public boolean last;
    public int totalPages;
    public boolean first;
    public JsonNode sort;
    public int numberOfElements;
    public boolean empty;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CustomPageImpl(@JsonProperty("content") List<T> content,
                          @JsonProperty("pageable") JsonNode pageable,
                          @JsonProperty("last") boolean last,
                          @JsonProperty("totalPages") int totalPages,
                          @JsonProperty("totalElements") Long totalElements,
                          @JsonProperty("size") int size,
                          @JsonProperty("number") int number,
                          @JsonProperty("first") boolean first,
                          @JsonProperty("sort") JsonNode sort,
                          @JsonProperty("numberOfElements") int numberOfElements,
                          @JsonProperty("empty") boolean empty) {
        super(content, PageRequest.of(number, size), totalElements);
        this.pageable = pageable;
        this.last = last;
        this.totalPages = totalPages;
        this.first = first;
        this.sort = sort;
        this.numberOfElements = numberOfElements;
        this.empty = empty;
    }

    public CustomPageImpl(List<T> content, PageRequest pageRequest, long total) {
        super(content, pageRequest, total);
    }

    public CustomPageImpl(List<T> content) {
        super(content);
    }
}
