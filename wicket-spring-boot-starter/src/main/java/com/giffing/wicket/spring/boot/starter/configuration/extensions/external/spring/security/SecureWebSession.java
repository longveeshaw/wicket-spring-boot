package com.giffing.wicket.spring.boot.starter.configuration.extensions.external.spring.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.Request;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

/**
 * Spring Security Implementation of Wickets {@link AuthenticatedWebSession}. 
 * 
 * @author Marc Giffing
 *
 */
public class SecureWebSession extends AuthenticatedWebSession {

	private static final long serialVersionUID = 1L;

	private static final String SPRING_SECURITY_CONTEXT_KEY = HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
	

	@SpringBean(name = "authenticationManager")
	private AuthenticationManager authenticationManager;

	private final HttpSession httpSession;
	
	public SecureWebSession(Request request) {
		super(request);
		this.httpSession = ((HttpServletRequest) request.getContainerRequest()).getSession(false);
		Injector.get().inject(this);
	}

	@Override
	public boolean authenticate(String username, String password) {
		try {
			Authentication auth = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(username, password));
			if (auth.isAuthenticated()) {
				SecurityContextHolder.getContext().setAuthentication(auth);
				if(httpSession != null) {
					httpSession.setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
				}
				return true;
			}
			return false;
		} catch (AuthenticationException e) {
			return false;
		}
	}

	@Override
	public Roles getRoles() {
		Roles roles = new Roles();
		if (isSignedIn()) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			for (GrantedAuthority authority : authentication.getAuthorities()) {
				roles.add(authority.getAuthority());
			}
		}
		return roles;
	}

}
