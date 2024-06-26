/* WeSmooth! 2024 */
package com.wesmooth.service.sdk.security.filters;

import com.wesmooth.service.sdk.security.Jwt;
import com.wesmooth.service.sdk.security.JwtUtility;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * The JWT Authentication Filter.
 *
 * @author Boris Georgiev
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component(value = "jwtAuthenticationFilter")
public class JwtAuthenticationFilter extends BaseJwtFilter {

  @Autowired
  public JwtAuthenticationFilter(JwtUtility jwtUtility) {
    super(jwtUtility);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    if (!request.getRequestURI().contains("/users/oauth2")
        && !request.getRequestURI().contains("/users/register")) {
      final String authHeader = request.getHeader("Authorization");
      try {
        trueOrThrow(authHeader != null && !authHeader.isBlank());
        String jwtString = authHeader.substring(7); // Remove "Bearer "
        Jwt jwt = jwtUtility.decrypt(jwtString);
        long currentMillis = System.currentTimeMillis();
        trueOrThrow(currentMillis < jwt.getPayload().getExp());
      } catch (Exception exception) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT is malformed or expired.");
      }
    }
    filterChain.doFilter(request, response);
  }

  /**
   * Configures and registers the JwtAuthenticationFilter.
   *
   * @return the bean.
   */
  @Bean
  public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistrationBean() {
    FilterRegistrationBean<JwtAuthenticationFilter> bean = new FilterRegistrationBean<>();
    bean.setFilter(this);
    return bean;
  }
}
