package org.tassoni.quizexample.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tassoni.quizexample.model.User;
import org.tassoni.quizexample.repository.UserRepository;

//See https://github.com/rwinch/gs-spring-security-3.2/blob/master/src/main/java/sample/security/UserRepositoryUserDetailsService.java
@Service("userDetailsService")
public class AppUserDetailsService implements UserDetailsService{

	@Autowired
	private UserRepository userRepository;
	
	@Override
	/*@Transactional(readOnly = true)*/
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username);
		//TODO  throw a UsernameNotFoundException if not found.  What does userRepository do if it doesn't find an entity?
		//TODO Implement UserDetails.  At first just give everyone ROLE_USER, since that's what we allow into the api.
		//Should we cache here, or let DaoAuthenticationProvider or something do that
		//TODO encryption??
		if(user == null)
			throw new UsernameNotFoundException(username + " not found");
		
		return new AppUserDetails(user);
	}

}
