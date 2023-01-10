package org.t246osslab.easybuggy4sb.vulnerabilities;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.client.auth.openidconnect.IdTokenResponse;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;
import org.t246osslab.easybuggy4sb.core.model.Forum;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Controller
public class VulnerableOIDCRPController extends AbstractController {
	
	private static boolean isSettingsReady = false;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	protected String authzEndpoint;

	protected String tokenEndpoint;

	protected String userinfoEndpoint;

	protected String endSessionEndpoint;

	protected String registration_endpoint;

	protected String jwksUri;

	protected String issuer;

	@Value("${attacker.app.url}")
	protected String attackerAppUrl;

	@Value("${manage.account.page.url}")
	protected String manageAccountPageUrl;

	@Value("${oidc.client.id}")
	protected String clientId;

	@Value("${oidc.client.secret}")
	protected String clientSecret;

	@Value("${oidc.dynamic.client.registration.enabled}")
	protected boolean clientRegistrationEnabled = false;

	@Value("${oidc.configuration.endpoint}")
	public void setOPConfig(String configEndpoint) {
		if (configEndpoint != null && !configEndpoint.isEmpty()) {
			log.debug("OP Config Endpoint: " + configEndpoint);
			try {
				HttpRequestFactory requestFactory = (new NetHttpTransport()).createRequestFactory();
				HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(configEndpoint));
				HttpResponse response = request.execute();
				Map<?, ?> opConfig = new Gson().fromJson(response.parseAsString(), Map.class);
				log.debug("OP Config: " + opConfig.toString());
				authzEndpoint = (String) opConfig.get("authorization_endpoint");
				tokenEndpoint = (String) opConfig.get("token_endpoint");
				userinfoEndpoint = (String) opConfig.get("userinfo_endpoint");
				endSessionEndpoint = (String) opConfig.get("end_session_endpoint");
				registration_endpoint = (String) opConfig.get("registration_endpoint");
				issuer = (String) opConfig.get("issuer");
				jwksUri = (String) opConfig.get("jwks_uri");
				if (clientRegistrationEnabled) {
					tryRegisterClient();
				}
				if (!(StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret))) {
					isSettingsReady = true;
				}
			} catch (IOException e) {
				log.error("OP configuration request failed.", e);
			}
		}
	}

	@RequestMapping(value = "/vulnerabileoidcrp")
	public ModelAndView index(ModelAndView mav, HttpServletRequest req, HttpSession ses, Locale locale) {

		String type = req.getParameter("type");
		String[] placeholders = null;
		if (type == null) {
			placeholders = new String[]{ attackerAppUrl };
		} else if ("2".equals(type)) {
			placeholders = new String[]{ attackerAppUrl + "/picture", req.getRequestURL().toString() };
		}
		setViewAndCommonObjects(mav, locale, "vulnerabileoidcrp2");
		mav.addObject("note", msg.getMessage("msg.note.vulnerabileoidcrp" + (type == null ? "" : type), placeholders, locale));
		searchMessages(mav, locale);

		if (ses != null) {
			Map<?, ?> userInfo = getUserInfo(ses);
			if (userInfo != null) {
				mav.addObject("userInfo", userInfo);
				return mav;
			}
			ses.invalidate();
		}
		ses = req.getSession(true);

		mav.addObject("loginMessage",
				msg.getMessage("msg.login.with.openid.provider", null, locale));
		mav.addObject("isSettingsReady", isSettingsReady);
		if (!isSettingsReady)
			mav.addObject("note", msg.getMessage("msg.note.oidc.invalid.config", null, locale));

		return mav;
	}

	@RequestMapping(value = "/start")
	public ModelAndView start(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, HttpSession ses,
			Locale locale) {

		if (ses == null) {
			return index(mav, req, null, locale);
		}
		Map<?, ?> userInfo = getUserInfo(ses);
		if (userInfo != null) {
			mav.addObject("userInfo", userInfo);
			return index(mav, req, null, locale);
		}

		String state = UUID.randomUUID().toString();
		String nonce = UUID.randomUUID().toString();
		try {
			AuthorizationCodeRequestUrl url = new AuthorizationCodeRequestUrl(authzEndpoint, clientId);
			url.setResponseTypes(Arrays.asList("code"));
			url.setScopes(Arrays.asList("openid", "profile"));
			url.setState(state);
			url.set("nonce", nonce);
			url.setRedirectUri(new GenericUrl(req.getRequestURL().toString()
					.replace("/start", "/callback")).build());
			res.sendRedirect(url.build());
			ses.setAttribute("state", state);
			ses.setAttribute("nonce", nonce);
		} catch (IOException e) {
			log.error("Authorization code request failed.", e);
		}
        return null;
	}

	@RequestMapping(value = "/callback")
	public ModelAndView callback(ModelAndView mav, HttpServletRequest req, HttpSession ses, Locale locale) {

		if (ses == null) {
			return index(mav, req, null, locale);
		}

		setViewAndCommonObjects(mav, locale, "vulnerabileoidcrp");
		mav.addObject("manageAccountPageUrl", manageAccountPageUrl);

		Map<?, ?> userInfo = getUserInfo(ses);
		if (userInfo != null) {
			mav.addObject("userInfo", userInfo);
			return mav;
		}

		// Verify authz code
		String code = req.getParameter("code");
		if (code == null || code.isEmpty()) {
			log.warn("code is required");
			return index(mav, req, null, locale);
		}

		// Verify state
		String state = (String) ses.getAttribute("state");
		if (state == null || state.isEmpty() || !state.equals(req.getParameter("state"))) {
			log.warn("Invalid state"); // Error handling should be Implemented
		}

		try {
			/* Access the token endpoint and get ID and access token */
			AuthorizationCodeTokenRequest authzReq = new AuthorizationCodeTokenRequest(new NetHttpTransport(),
					new JacksonFactory(), new GenericUrl(tokenEndpoint), code);
			authzReq.setRedirectUri(req.getRequestURL().toString())
					.setClientAuthentication(new BasicAuthentication(clientId, clientSecret));
			HttpResponse httpRes = authzReq.executeUnparsed();
			IdTokenResponse idTokenRes = httpRes.parseAs(IdTokenResponse.class);
			String accessToken = idTokenRes.getAccessToken();
			String idTokenStr = idTokenRes.getIdToken();
			IdToken idToken = IdToken.parse(idTokenRes.getFactory(), idTokenStr);

			// Verify nonce
			String nonce = (String) ses.getAttribute("nonce");
			if (nonce == null || nonce.isEmpty() || !nonce.equals(idToken.getPayload().getNonce())) {
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
			ses.setAttribute("idToken", idTokenStr);
			ses.setAttribute("refreshToken", idTokenRes.getRefreshToken());
			userInfo = getUserInfo(ses);
			mav.addObject("userInfo", userInfo);
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
	public void logout(HttpServletRequest req, HttpServletResponse res, HttpSession ses) {
		String state = UUID.randomUUID().toString();
		try {
			GenericUrl url = new GenericUrl(endSessionEndpoint);
			url.set("id_token_hint", (String) ses.getAttribute("idToken"));
			url.set("post_logout_redirect_uri", req.getRequestURL().toString().replace("/oidclogout", "/"));
			url.set("state", state);
			res.sendRedirect(url.build());
			ses.setAttribute("state", state);
		} catch (IOException e) {
			log.error("Logout request to OP failed.", e);
		}
		ses.invalidate();
	}

	@RequestMapping(value = "/addMessage")
	public ModelAndView forum(ModelAndView mav, HttpServletRequest req, HttpSession ses, Locale locale) {
		if (ses == null) {
			return index(mav, req, null, locale);
		}
		Map<?, ?> userInfo = getUserInfo(ses);
		if (userInfo == null) {
			return index(mav, req, null, locale);
		} else {
			mav.addObject("userInfo", userInfo);
		}
		setViewAndCommonObjects(mav, locale, "vulnerabileoidcrp2");
		String message = req.getParameter("message");
		if (message != null && !message.isEmpty()) {
			String username = (String) userInfo.get("name");
			if (username == null || message.isEmpty()) username = (String) userInfo.get("preferred_username");
			String picture = (String) userInfo.get("picture");
			if (picture == null || picture.isEmpty()) picture = "images/avatar_man.png";
			insertMessage(username, picture, message, mav, locale);
		}
		searchMessages(mav, locale);
		return mav;
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
				if (tokenRes != null) {
					ses.setAttribute("accessToken", tokenRes.getAccessToken());
					ses.setAttribute("refreshToken", tokenRes.getRefreshToken());
					return getUserInfo(ses);
				}
			} else {
				log.error("Userinfo request failed.", e);
			}
		} catch (IOException e) {
			log.error("Userinfo request failed.", e);
		}
		return null;
	}
	private void tryRegisterClient() {
		log.info("Try Register Client.");

		try {
			TokenRequest tokenReq = new TokenRequest(new NetHttpTransport(), new JacksonFactory(),
					new GenericUrl(tokenEndpoint), "password");
			tokenReq.put("client_id", "admin-cli");
			tokenReq.put("username", "admin");
			tokenReq.put("password", "admin");
			TokenResponse tokenResponse = tokenReq.execute();

			HttpRequestFactory requestFactory = (new NetHttpTransport()).createRequestFactory();
			HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(registration_endpoint));
			request.setRequestMethod(HttpMethods.POST);
			HttpHeaders headers = new HttpHeaders();
			headers.setAuthorization("bearer " + tokenResponse.getAccessToken());
			headers.setContentType("application/json");
			headers.setAccept("application/json");
			request.setHeaders(headers);
			Map<String, Object> params = new HashMap<>();
			params.put("redirect_uris", Arrays.asList("*"));
			params.put("post_logout_redirect_uris", Arrays.asList("*"));
			HttpContent content = new JsonHttpContent(new JacksonFactory(), params);
			request.setContent(content);
			HttpResponse response = request.execute();
			Map map = new Gson().fromJson(response.parseAsString(), Map.class);
			clientId = (String) map.get("client_id");
			clientSecret = (String) map.get("client_secret");
			log.info("Client {} is registered.", clientId);

		} catch (IOException e) {
			log.error("Registration request to OP failed.", e);
		}
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

	private List<Forum> searchMessages(ModelAndView mav, Locale locale) {
		List<Forum> messages = null;
		try {
			messages = jdbcTemplate.query("select * from forum order by time desc", new RowMapper<Forum>() {
				public Forum mapRow(ResultSet rs, int rowNum) throws SQLException {
					Forum forum = new Forum();
					forum.setTime(rs.getTime("time"));
					forum.setUsername(rs.getString("username"));
					forum.setPicture(rs.getString("picture"));
					forum.setMessage(rs.getString("message"));
					return forum;
				}
			});
			mav.addObject("messages", messages);
		} catch (DataAccessException e) {
			mav.addObject("errmsg",
					msg.getMessage("msg.db.access.error.occur", new String[] { e.getMessage() }, null, locale));
			log.error("DataAccessException occurs: ", e);
		} catch (Exception e) {
			mav.addObject("errmsg",
					msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));
			log.error("Exception occurs: ", e);
		}
		return messages;
	}

	private void insertMessage(String username, String picture, String message, ModelAndView mav, Locale locale) {
		String resultMessage = null;
		try {
			int insertCount = jdbcTemplate.update("insert into forum values (CURRENT_TIMESTAMP, ?, ?, ?)",
					username, picture, message);
			if (insertCount != 1) {
				resultMessage = msg.getMessage("msg.user.already.exist", null, locale);
			}
		} catch (DataAccessException e) {
			resultMessage = msg.getMessage("msg.db.access.error.occur", new String[] { e.getMessage() }, locale);
			log.error("DataAccessException occurs: ", e);
		} catch (Exception e) {
			resultMessage = msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, locale);
			log.error("Exception occurs: ", e);
		}
		mav.addObject("errmsg", resultMessage);
	}

}
