package com.lifequest.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.entity.User;
import com.lifequest.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public boolean existsByUsername(String username) {
		return userRepository.existsByUsername(username);
	}

	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Transactional
	public User create(String username, String email, UUID innerId, String userIcon) {
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setInnerId(innerId);
		user.setUserIcon(userIcon);
		return userRepository.save(user);
	}

	@Transactional
	public User linkCharacter(User user, CharacterStats characterStats) {
		user.setPlayerName(characterStats.getPlayerName());
		user.setPlayerId(characterStats.getId());
		return userRepository.save(user);
	}
}
