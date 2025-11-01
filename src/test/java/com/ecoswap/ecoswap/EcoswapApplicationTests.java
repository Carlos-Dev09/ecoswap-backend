package com.ecoswap.ecoswap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ecoswap.ecoswap.user.models.dto.UserDTO;
import com.ecoswap.ecoswap.user.services.UserService;

@SpringBootTest
class EcoswapApplicationTests {

	private final UserService userService;

	@Autowired
	public EcoswapApplicationTests(UserService userService) {
		this.userService = userService;
	}

	@Test
	void contextLoads() {
		assertNotNull(userService);
	}

	@Test
	void shouldReturnAllUsers(){
		List<UserDTO> usersDtos = userService.findAll();
		assertNotNull(usersDtos);
		assertTrue(usersDtos.size() >= 0);
	}

}
 