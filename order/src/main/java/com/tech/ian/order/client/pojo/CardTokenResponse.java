package com.tech.ian.order.client.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CardTokenResponse(String id) {
}
