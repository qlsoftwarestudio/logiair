package com.sportflow.gestor_reservas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {
	@Test
	void simpleTest() {
		HealthController controller = new HealthController();
		assertEquals("OK", controller.health());
	}

}
