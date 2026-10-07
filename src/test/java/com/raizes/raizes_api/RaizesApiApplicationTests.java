package com.raizes.raizes_api;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest(properties = {
    "app.security.jwt.secret=teste-segredo-com-pelo-menos-trinta-e-dois-bytes",
    "app.admin-inicial.nome=Administrador Teste",
    "app.admin-inicial.email=admin@teste.local",
    "app.admin-inicial.senha=SenhaTeste123"
})
class RaizesApiApplicationTests {
    @Test void contextLoads() {}
}
