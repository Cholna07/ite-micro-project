package kh.edu.istad.platform.restapi.dto.response;

public record PageMetadata(
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {
}
