package com.jornada.jobapi.service;

import com.jornada.jobapi.dto.AutenticacaoDTO;
import com.jornada.jobapi.dto.UsuarioDTO;
import com.jornada.jobapi.entity.UsuarioEntity;
import com.jornada.jobapi.exception.RegraDeNegocioException;
import com.jornada.jobapi.mapper.UsuarioMapper;
import com.jornada.jobapi.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UsuarioService(@Lazy UsuarioRepository usuarioRepository,
                       @Lazy AuthenticationManager authenticationManager,
                       @Lazy UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.usuarioMapper = usuarioMapper;
    }

    @Value("${jwt.validade.token}")
    private String validadeJWT;

    @Value("${jwt.secret}")
    private String secret;

    public String fazerLogin(AutenticacaoDTO autenticacaoDTO) throws RegraDeNegocioException {

        String senha = autenticacaoDTO.getSenha();

        // Verifica se a senha atende aos critérios
//        if (!senha.matches(".*[A-Z].*") || // Pelo menos uma letra maiúscula
//                !senha.matches(".*[a-z].*") || // Pelo menos uma letra minúscula
//                !senha.matches(".*\\d.*") ||   // Pelo menos um número
//                !senha.matches(".*[!@#$%^&*()].*")) { // Pelo menos um caractere especial
//            throw new RegraDeNegocioException("A senha não atende aos critérios de segurança.");
//        }
        UsernamePasswordAuthenticationToken dtoDoSpring = new UsernamePasswordAuthenticationToken(
                autenticacaoDTO.getEmail(),
                autenticacaoDTO.getSenha()
        );
        try {
            Authentication autenticacao = authenticationManager.authenticate(dtoDoSpring);

            Object usuarioAutenticado = autenticacao.getPrincipal();
            UsuarioEntity usuarioEntity = (UsuarioEntity) usuarioAutenticado;

//            List<String> nomeDosCargos= usuarioEntity.getCargos().stream()
//                    .map(cargo -> cargo.getNome()).toList();

            Date dataAtual = new Date();
            Date dataExpiracao = new Date(dataAtual.getTime() + Long.parseLong(validadeJWT.trim()));

            String jwtGerado = Jwts.builder()
                    .setIssuer("jornada-job-api")
//                    .claim("CARGOS", nomeDosCargos)
                    .setSubject(usuarioEntity.getIdUsuario().toString())
                    .setIssuedAt(dataAtual)
                    .setExpiration(dataExpiracao)
                    .signWith(SignatureAlgorithm.HS256, secret)
                    .compact();

            return jwtGerado;

        } catch (AuthenticationException ex) {
            ex.printStackTrace();
            throw new RegraDeNegocioException("E-mail e/ou senha inválidos");
        }
    }

    public UsernamePasswordAuthenticationToken validarToken(String token) {
        if (token == null) {
            return null;
        }
        String tokenLimpo = token.replace("Bearer ", "");

        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(tokenLimpo)
                .getBody();

        String idUser = claims.getSubject();
        List<String> cargos = claims.get("CARGOS", List.class);

        List<SimpleGrantedAuthority> listaDeCargos = cargos.stream()
                .map(cargoStr -> new SimpleGrantedAuthority(cargoStr))
                .toList();

        UsernamePasswordAuthenticationToken tokenSpring
                = new UsernamePasswordAuthenticationToken(idUser, null, listaDeCargos);

        return tokenSpring;
    }


    public UsuarioDTO salvarUsuario(UsuarioDTO usuarioDTO) throws RegraDeNegocioException {
        String senhaSegura = usuarioDTO.getSenha();

        if (!senhaSegura.matches(".*[A-Z].*") || // Pelo menos uma letra maiúscula
                !senhaSegura.matches(".*[a-z].*") || // Pelo menos uma letra minúscula
                !senhaSegura.matches(".*\\d.*") ||   // Pelo menos um número
                !senhaSegura.matches(".*[!@#$%^&*()].*")) { // Pelo menos um caractere especial
            throw new RegraDeNegocioException("A senha não atende aos critérios de segurança.");
        }

        validarUser(usuarioDTO);
        UsuarioEntity entidade = usuarioMapper.paraEntity(usuarioDTO);

        String senha = entidade.getSenha();
        String senhaCriptografada = geradorDeSenha(senha);
        entidade.setSenha(senhaCriptografada);

//        entidade.setEnabled(true);

        UsuarioEntity salvo = usuarioRepository.save(entidade);
        UsuarioDTO dtoSalvo = usuarioMapper.paraDTO(salvo);
        return dtoSalvo;
    }

    public UsuarioDTO atualizarUsuario(@RequestBody UsuarioDTO usuarioDTO) throws RegraDeNegocioException {
        String senhaSegura = usuarioDTO.getSenha();

        if (!senhaSegura.matches(".*[A-Z].*") || // Pelo menos uma letra maiúscula
                !senhaSegura.matches(".*[a-z].*") || // Pelo menos uma letra minúscula
                !senhaSegura.matches(".*\\d.*") ||   // Pelo menos um número
                !senhaSegura.matches(".*[!@#$%^&*()].*")) { // Pelo menos um caractere especial
            throw new RegraDeNegocioException("A senha não atende aos critérios de segurança.");
        }

        validarUser(usuarioDTO);
        UsuarioEntity entidade = usuarioMapper.paraEntity(usuarioDTO);

        String senha = entidade.getSenha();
        String senhaCriptografada = geradorDeSenha(senha);
        entidade.setSenha(senhaCriptografada);

//        entidade.setEnabled(true);

        UsuarioEntity salvo = usuarioRepository.save(entidade);
        UsuarioDTO dtoSalvo = usuarioMapper.paraDTO(salvo);
        return dtoSalvo;    }

    public String geradorDeSenha(String senha) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String senhaCriptografada = bCryptPasswordEncoder.encode(senha);
        return senhaCriptografada;
    }

    public void validarUser(UsuarioDTO usuario) throws RegraDeNegocioException {
        if (usuario.getEmail().contains("@gmail")
                || usuario.getEmail().contains("@hotmail")
                || usuario.getEmail().contains("@outlook")) {
        } else {
            throw new RegraDeNegocioException("Precisa ser @gmail, @hotmail ou @outlook");
        }
    }

    public boolean validarIdUser(Integer id) throws RegraDeNegocioException {
        if (usuarioRepository.findById(id).isEmpty()) {
            throw new RegraDeNegocioException("ID inválido, usuário não existe!");
        }
        return true;
    }

    public void remover(Integer id) throws RegraDeNegocioException {
        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado"));

        usuarioRepository.delete(usuario);
    }

    public Optional<UsuarioEntity> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public void desativarUsuario(Integer idInformado) throws RegraDeNegocioException {
        UsuarioEntity entity = usuarioRepository.findById(idInformado)
                .orElseThrow(() -> new RegraDeNegocioException("Usuario não encontrado"));
//        entity.setEnabled(false);
        usuarioRepository.save(entity);
    }
}
