package com.example.lazier.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OAuthTokenDto {

	@JsonProperty("access_token")
	private String accessToken;
	private String scope;

	@JsonProperty("token_type")
	private String tokenType;

}
