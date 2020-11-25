// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa;

import lombok.Builder;
import lombok.Getter;
import software.amazon.awssdk.services.mwaa.MwaaClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

/**
 * Wraps all types of proxies for easier passing around.
 */
@Builder
@Getter
public class Proxies {
    private final AmazonWebServicesClientProxy awsClientProxy;
    private final ProxyClient<MwaaClient> mwaaClientProxy;
}
