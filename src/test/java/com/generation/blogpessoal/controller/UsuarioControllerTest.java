package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.UsuarioService;
import com.generation.blogpessoal.util.JwtHelper;
import com.generation.blogpessoal.util.TestBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class UsuarioControllerTest {

	@Autowired
	private TestRestTemplate testRestTemplate;
	
	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	private static final String BASE_URL = "/usuarios";
	private static final String USUARIO = "root@root.com";
	private static final String SENHA = "rootroot";
	
	@BeforeAll
	void inicio() {
		usuarioRepository.deleteAll();
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Root", USUARIO, SENHA));
	}
	
	@Test
	@DisplayName("01 - Deve Cadastrar um novo usuário com sucesso")
	void deveCadastrarUsuario() {
		//Given
		Usuario usuario = TestBuilder.criarUsuario(null, "Thuany", "thuany@email.com.br", "12345678");
		
		// When
		HttpEntity<Usuario> requisicao = new HttpEntity<Usuario>(usuario);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
				BASE_URL + "/cadastrar", HttpMethod.POST, requisicao, Usuario.class);
		
		// Then
		assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
		
	}
	
	@Test
	@DisplayName("02 - Não Deve Cadastrar usuário duplicado")
	void naoDeveCadastrarUsuarioDuplicado() {
		//Given
		Usuario usuario = TestBuilder.criarUsuario(null, "Rafaela Lemes", "rafa_lemes@email.com.br", "12345678");
		usuarioService.cadastrarUsuario(usuario);
		
		// When
		HttpEntity<Usuario> requisicao = new HttpEntity<Usuario>(usuario);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
				BASE_URL + "/cadastrar", HttpMethod.POST, requisicao, Usuario.class);
		
		// Then
		assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
		assertNull(resposta.getBody());
		
	}
	
	@Test
	@DisplayName("03 - Deve Atualizar os dados do usuário com sucesso")
	void DeveAtualizarUmUsuario() {
		//Given
		Usuario usuario = TestBuilder.criarUsuario(null, "Nadia", "nadia@email.com.br", "12345678");
		Optional<Usuario> usuarioCadastrado = usuarioService.cadastrarUsuario(usuario);
		
		Usuario usuarioUpdate = TestBuilder.criarUsuario(usuarioCadastrado.get().getId(), "Nadia Caricatto", 
				"nadia@email.com.br", "abc12345");
		
		// When
		
		// Gerar o Token
		String token = JwtHelper.obterToken(testRestTemplate, USUARIO, SENHA);
		
		// Criar a Requisição com o Token
		HttpEntity<Usuario> requisicao = JwtHelper.criarRequisicaoComToken(usuarioUpdate, token);
		
		// Enviar a Requisição PUT
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
				BASE_URL + "/atualizar", HttpMethod.PUT, requisicao, Usuario.class);
		
		// Then
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
		
	}
	
	@Test
	@DisplayName("04 - Deve listar todos os usuários")
	void deveListarTodosUsuarios() {
		// Given
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Ana Marques", 
				"ana_marques@email.com.br", "12345678"));
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Carlos Moura", 
				"carlos_moura@email.com.br", "12345678"));
		
		// When
		String token = JwtHelper.obterToken(testRestTemplate, USUARIO, SENHA);
		HttpEntity<Void> requisicao = JwtHelper.criarRequisicaoComToken(token);
		ResponseEntity<Usuario[]> resposta = testRestTemplate.exchange(
				BASE_URL + "/all", HttpMethod.GET, requisicao, Usuario[].class);
		
		// Then
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
	}
	
	@Test
	@DisplayName("05 - Deve buscar um usuário por ID")
	void deveBuscarUsuarioPorId() {
		// Given
		Optional<Usuario> usuario = usuarioService.cadastrarUsuario(
				TestBuilder.criarUsuario(null, "Fabiana Moraes", 
						"fabi_moraes@email.com.br", "12345678")
		);
		
		// When
		String token = JwtHelper.obterToken(testRestTemplate, USUARIO, SENHA);
		HttpEntity<Void> requisicao = JwtHelper.criarRequisicaoComToken(token);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
				BASE_URL + "/" + usuario.get().getId(), HttpMethod.GET, requisicao, Usuario.class);
		
		// Then
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
		assertEquals("Fabiana Moraes", resposta.getBody().getNome());
	}
}
