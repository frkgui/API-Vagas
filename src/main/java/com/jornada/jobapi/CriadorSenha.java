package com.jornada.jobapi;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CriadorSenha {
    public static void main(String[] args) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

//          -- Criptografia de senha --
           String senhaCriptografada = bCryptPasswordEncoder.encode("123");
          System.out.println(senhaCriptografada);

//          -- Senhas salvas no banco  --
//        $2a$10$VcztnROIeRStkOrXhqMm5.duTTttVAD2TM/cXnJMcOBZPFJPjBizi (123)
//        $10$v6lphh0tajjyYKj.Tflxge3fKPWDrwjP2AOlvA3wkMoGQoIrauZFO (12345)


//         -- Verificar se a senha é igual a senha criptografada --
//        boolean senhaCorreta =bCryptPasswordEncoder.matches("123", "$2a$10$VcztnROIeRStkOrXhqMm5.duTTttVAD2TM/cXnJMcOBZPFJPjBizi");
//        System.out.println(senhaCorreta);
    }
}
