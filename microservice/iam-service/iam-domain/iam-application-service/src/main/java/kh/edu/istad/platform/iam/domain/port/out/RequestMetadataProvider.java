package kh.edu.istad.platform.iam.domain.port.out;

public interface RequestMetadataProvider {
    String remoteAddress();

    String userAgent();
}
