package kh.edu.istad.istadcommonwebflux.dto;

import java.util.Map;

public record ErrorApiResponse(
        String message,
        int status,
        Map<String, Object> model
) {
}
