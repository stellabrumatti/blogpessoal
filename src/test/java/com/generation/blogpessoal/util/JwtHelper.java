package com.generation.blogpessoal.util;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.model.UsuarioLogin;

public class JwtHelper {

	private JwtHelper() {}
	
	public static String obterToken(TestRestTemplate testRestTemplate, String usuario, String senha) {
		
		UsuarioLogin usuarioLogin = TestBuilder.criarUsuarioLogin(usuario, senha);
		
		// Cria o Corpo da Requisição HTTP
		HttpEntity<UsuarioLogin> requisicao = new HttpEntity<UsuarioLogin>(usuarioLogin);
		
		// Enviar a Requisição
		ResponseEntity<UsuarioLogin> resposta = testRestTemplate
				.exchange("/usuarios/logar", HttpMethod.POST, requisicao, UsuarioLogin.class);
		
		UsuarioLogin corpoResposta = resposta.getBody();
		
		if(corpoResposta != null && corpoResposta.getToken() != null) {
			return corpoResposta.getToken();
		}
		
		throw new RuntimeException("Falha no login: " + usuario);
		
	}
	
	public static <T> HttpEntity<T> criarRequisicaoComToken(T corpo, String token){
		HttpHeaders cabecalho = new HttpHeaders();
		String tokenLimpo = token.startsWith("Bearer ") ? token.substring(7): token;
		cabecalho.setBearerAuth(tokenLimpo);
		return new HttpEntity<>(corpo,cabecalho);
	}
	
	public static HttpEntity<Void> criarRequisicaoComToken(String token){
		return criarRequisicaoComToken(null, token);
	}
}
