package br.com.gabrielleone.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.gabrielleone.todolist.user.IUserRepository;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    var path = request.getServletPath();
    if (path.startsWith("/tasks/")) {
      var authorization = request.getHeader("Authorization");

      var authEncoded = authorization.substring("Basic".length()).trim();

      byte[] authDecode = Base64.getDecoder().decode(authEncoded);

      var authDecoded = new String(authDecode);

      String[] credentials = authDecoded.split(":");

      var username = credentials[0];
      var password = credentials[1];

      var user = this.userRepository.findByUsername(username);
      if (user == null) {
        response.sendError(401);
      } else {
        var passwordHashed = user.getPassword();
        var passwordMatch = BCrypt.verifyer().verify(password.toCharArray(), passwordHashed);
        if (!passwordMatch.verified) {
          response.sendError(401);
        }
        request.setAttribute("userId", user.getId());
      }
    }

    filterChain.doFilter(request, response);

  }
}
