package kh.edu.istad.platform.restapi.dto.response;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        PageMetadata page
) {
}
