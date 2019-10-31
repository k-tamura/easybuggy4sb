package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.client.auth.openidconnect.IdTokenResponse;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;

@Controller
public class VulnerableOIDCRPController extends AbstractController {
	
	private static boolean isSettingsReady = false;

	protected String authzEndpoint;

	protected String tokenEndpoint;

	protected String userinfoEndpoint;

	protected String endSessionEndpoint;

	protected String jwksUri;

	protected String issuer;

	@Value("${oidc.client.id}")
	protected String clientId;

	@Value("${oidc.client.secret}")
	protected String clientSecret;

	@Value("${oidc.redirect.uri}")
	protected String redirectUri;

	@Value("${oidc.op.name}")
	protected String opName;

	@Value("${oidc.configuration.endpoint}")
	public void setOPConfig(String configEndpoint) {
		try {
			HttpRequestFactory requestFactory = (new NetHttpTransport()).createRequestFactory();
			HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(configEndpoint));
			HttpResponse response = request.execute();
			Map<?, ?> opConfig = new Gson().fromJson(response.parseAsString(), Map.class);
			authzEndpoint = (String) opConfig.get("authorization_endpoint");
			tokenEndpoint = (String) opConfig.get("token_endpoint");
			userinfoEndpoint = (String) opConfig.get("userinfo_endpoint");
			endSessionEndpoint = (String) opConfig.get("end_session_endpoint");
			issuer = (String) opConfig.get("issuer");
			jwksUri = (String) opConfig.get("jwks_uri");
			if (!(StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret) || StringUtils.isEmpty(redirectUri)
					|| StringUtils.isEmpty(opName))) {
				isSettingsReady = true;
			}
		} catch (IOException e) {
			log.debug("OP configuration request failed.", e);
		}
	}

	@RequestMapping(value = "/vulnerabileoidcrp")
	public ModelAndView index(ModelAndView mav, HttpServletRequest req, HttpSession ses, Locale locale) {

		setViewAndCommonObjects(mav, locale, "vulnerabileoidcrp");

		if (ses != null) {
			Map<?, ?> userInfo = getUserInfo(ses);
			if (userInfo != null) {
				changeNextPageToUserInfo(mav, locale, userInfo);
				return mav;
			}
			ses.invalidate();
		}
		ses = req.getSession(true);

		ses.setAttribute("state", UUID.randomUUID().toString());
		ses.setAttribute("nonce", UUID.randomUUID().toString());

		mav.addObject("loginMessage",
				msg.getMessage("msg.login.with.openid.provider", new Object[] { opName }, locale));
		mav.addObject("isSettingsReady", isSettingsReady);
		if (!isSettingsReady)
			mav.addObject("note", msg.getMessage("msg.note.oidc.invalid.config", null, locale));

		return mav;
	}

	@RequestMapping(value = "/start")
	public ModelAndView start(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, HttpSession ses,
			Locale locale) {

		setViewAndCommonObjects(mav, locale, "vulnerabileoidcrp");

		if (ses == null) {
			return index(mav, req, null, locale);
		}

		Map<?, ?> userInfo = getUserInfo(ses);
		if (userInfo != null) {
			changeNextPageToUserInfo(mav, locale, userInfo);
			return mav;
		}

		String state = (String) ses.getAttribute("state");
		String nonce = (String) ses.getAttribute("nonce");
		if (state == null || nonce == null || state.isEmpty() || nonce.isEmpty()) {
			return index(mav, req, null, locale);
		} else {
			try {
				AuthorizationCodeRequestUrl url = new AuthorizationCodeRequestUrl(authzEndpoint, clientId);
				url.setResponseTypes(Arrays.asList("code"));
				url.setScopes(Arrays.asList("openid", "profile"));
				url.setState(state);
				url.set("nonce", nonce);
				url.setRedirectUri(new GenericUrl(redirectUri).build());
				res.sendRedirect(url.build());
			} catch (IOException e) {
				log.error("Authorization code request failed.", e);
			}
		}
        return null;
	}

	@RequestMapping(value = "/callback")
	public ModelAndView callback(ModelAndView mav, HttpServletRequest req, HttpSession ses, Locale locale) {

		setViewAndCommonObjects(mav, locale, "vulnerabileoidcrp");

		if (ses == null) {
			return index(mav, req, null, locale);
		}

		Map<?, ?> userInfo = getUserInfo(ses);
		if (userInfo != null) {
			changeNextPageToUserInfo(mav, locale, userInfo);
			return mav;
		}

		String state = (String) ses.getAttribute("state");
		String nonce = (String) ses.getAttribute("nonce");
		if (state == null || nonce == null || state.isEmpty() || nonce.isEmpty()) {
			return index(mav, req, null, locale);
		}

		// Verify authz code
		String code = req.getParameter("code");
		if (code == null || code.isEmpty()) {
			log.warn("Invalid code"); // Error handling should be Implemented
			return index(mav, req, null, locale);
		}

		// Verify state
		if (!state.equals(req.getParameter("state"))) {
			log.warn("Invalid state"); // Error handling should be Implemented
		}

		try {
			/* Access the token endpoint and get ID and access token */
			AuthorizationCodeTokenRequest authzReq = new AuthorizationCodeTokenRequest(new NetHttpTransport(),
					new JacksonFactory(), new GenericUrl(tokenEndpoint), code);
			authzReq.setRedirectUri(redirectUri)
					.setClientAuthentication(new BasicAuthentication(clientId, clientSecret));
			HttpResponse httpRes = authzReq.executeUnparsed();
			IdTokenResponse idTokenRes = httpRes.parseAs(IdTokenResponse.class);
			String accessToken = idTokenRes.getAccessToken();
			IdToken idToken = IdToken.parse(idTokenRes.getFactory(), idTokenRes.getIdToken());

			// Verify nonce
			if (!nonce.equals(idToken.getPayload().getNonce())) {
				log.warn("Invalid nonce"); // Error handling should be Implemented
			}
			// Verify signature
			if (!idToken.verifySignature(getJwkPublicKey(idToken.getHeader().getKeyId()))) {
				log.warn("Invalid signature"); // Error handling should be Implemented
			}
			// Verify iss
			if (!idToken.verifyIssuer(Arrays.asList(issuer))) {
				log.warn("Invalid issuer"); // Error handling should be Implemented
			}
			// Verify aud
			if (!idToken.verifyAudience(Arrays.asList(clientId))) {
				log.warn("Invalid audience"); // Error handling should be Implemented
			}
			// Verify at_hath
			if (!getAtHash(accessToken).equals(idToken.getPayload().getAccessTokenHash())) {
				log.warn("Invalid at_hash"); // Error handling should be Implemented
			}
			// Verify exp
			if (!idToken.verifyExpirationTime(System.currentTimeMillis(), 0)) {
				log.warn("Invalid exp"); // Error handling should be Implemented
			}
			// Verify iat
			if (!idToken.verifyIssuedAtTime(System.currentTimeMillis(), 600)) {
				log.warn("Invalid iat"); // Error handling should be Implemented
			}

			ses.setAttribute("accessToken", accessToken);
			ses.setAttribute("refreshToken", idTokenRes.getRefreshToken());
			userInfo = getUserInfo(ses);
			changeNextPageToUserInfo(mav, locale, userInfo);
			ses.setAttribute("sub", userInfo.get("sub"));
			return mav;
		} catch (TokenResponseException e) {
			log.debug("Invalid token request", e);
			return index(mav, req, null, locale);
		} catch (Exception e) {
			log.error("Error occur", e);
			return index(mav, req, null, locale);
		}
	}

	@RequestMapping(value = "/oidclogout")
	public String logout(HttpSession ses) {
		try {
			HttpRequestFactory requestFactory = (new NetHttpTransport()).createRequestFactory();
			HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(endSessionEndpoint));
			request.setRequestMethod(HttpMethods.POST);
			Map<String, String> params = new HashMap<>();
			params.put("client_id", clientId);
			params.put("client_secret", clientSecret);
			params.put("refresh_token", (String) ses.getAttribute("refreshToken"));
			HttpContent content = new UrlEncodedContent(params);
			request.setContent(content);
			HttpResponse response = request.execute();
			if (response.isSuccessStatusCode()) {
				log.error("Logout request to OP failed. Response: ", response.parseAsString());
			}
		} catch (IOException e) {
			log.error("Logout request to OP failed.", e);
		}
		ses.invalidate();
		return "redirect:/";
	}

	private void changeNextPageToUserInfo(ModelAndView mav, Locale locale, Map<?, ?> userInfo) {
		mav.addObject("title", msg.getMessage("title.userinfo.page", null, locale));
		mav.addObject("userInfo", userInfo);
	}

	private Map<?, ?> getUserInfo(HttpSession ses) {
		
		String accessToken = (String) ses.getAttribute("accessToken");
		if (accessToken == null) {
			return null;
		}
		try {
			HttpRequestFactory requestFactory = (new NetHttpTransport()).createRequestFactory();
			HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(userinfoEndpoint));
			HttpHeaders headers = new HttpHeaders();
			headers.setAuthorization("bearer " + accessToken);
			request.setHeaders(headers);
			HttpResponse response = request.execute();
			return new Gson().fromJson(response.parseAsString(), Map.class);
		} catch (HttpResponseException e) {
			Map<?, ?> fromJson = new Gson().fromJson(e.getContent(), Map.class);
			if (e.getStatusCode() == 401 && fromJson != null && "invalid_token".equals(fromJson.get("error"))) {
				TokenResponse tokenRes = refreshTokens((String) ses.getAttribute("refreshToken"));
				ses.setAttribute("accessToken", tokenRes.getAccessToken());
				ses.setAttribute("refreshToken", tokenRes.getRefreshToken());
				return getUserInfo(ses);
			} else {
				log.error("Userinfo request failed.", e);
			}
		} catch (IOException e) {
			log.error("Userinfo request failed.", e);
		}
		return null;
	}

	private TokenResponse refreshTokens(String refreshToken) {
		if (refreshToken != null) {
			/* Access the token endpoint and refresh tokens */
			RefreshTokenRequest tokenReq = new RefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
					new GenericUrl(tokenEndpoint), refreshToken);
			tokenReq.setClientAuthentication(new BasicAuthentication(clientId, clientSecret));
			try {
				HttpResponse httpRes = tokenReq.executeUnparsed();
				return httpRes.parseAs(TokenResponse.class);
			} catch (IOException ioe) {
				log.error("Refresh token request failed.", ioe);
			}
		}
		return null;
	}

	private String getAtHash(String accessToken) {
		byte[] hashedbytes = DigestUtils.sha256(accessToken);
		byte[] hashedbyteshalf = new byte[hashedbytes.length / 2];
		System.arraycopy(hashedbytes, 0, hashedbyteshalf, 0, hashedbyteshalf.length);
		return Base64.encodeBase64URLSafeString(hashedbyteshalf);
	}

	private PublicKey getJwkPublicKey(String keyId) {
		if (keyId == null) {
			return null;
		}
		try {
			HttpRequestFactory requestFactory = (new NetHttpTransport()).createRequestFactory();
			HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(jwksUri));
			HttpResponse response = request.execute();
			Map<?, ?> fromJson = new Gson().fromJson(response.parseAsString(), Map.class);
			List<Map<?, ?>> keys = (List<Map<?, ?>>) fromJson.get("keys");
			for (Map<?, ?> key : keys) {
				String use = key.get("use").toString();
				String kty = key.get("kty").toString();
				String n = key.get("n").toString();
				String e = key.get("e").toString();
				String kid = key.get("kid").toString();

				if ("sig".equals(use) && keyId.equals(kid)) {
					Base64 base64 = new Base64();
					BigInteger modulus = new BigInteger(1, base64.decode(n));
					BigInteger publicExponent = new BigInteger(1, base64.decode(e));
					PublicKey publicKey = KeyFactory.getInstance(kty)
							.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
					if (publicKey != null) {
						return publicKey;
					}
				}
			}
		} catch (Exception e) {
			log.error("Cannot get JWK public key.", e);
		}
		return null;
	}
}
