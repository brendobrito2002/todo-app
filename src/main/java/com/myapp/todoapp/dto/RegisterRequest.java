package com.myapp.todoapp.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(	
		@NotBlank(message = "O nome é obrigatório")
	    @Size(min = 3, max = 50, message = "O nome deve ter entre 3 e 50 caracteres")
	    String name,

	    @NotBlank(message = "O email é obrigatório")
	    @Email(message = "O email precisa ser válido")
	    String email,

	    @NotBlank(message = "A senha é obrigatória")
	    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
	    String password
) {}