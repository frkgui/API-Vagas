package com.jornada.jobapi.controller;

import com.jornada.jobapi.dto.AtualizarUsuarioDTO;
import com.jornada.jobapi.dto.UsuarioCandidatoRecrutadorDTO;
import com.jornada.jobapi.dto.UsuarioDTO;
import com.jornada.jobapi.dto.VagasDTO;
import com.jornada.jobapi.exception.RegraDeNegocioException;
import com.jornada.jobapi.service.EmailService;
import com.jornada.jobapi.service.UsuarioService;
import com.jornada.jobapi.service.VagasService;
import com.jornada.jobapi.service.VagasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/recrutador")
@RequiredArgsConstructor
@Slf4j
public class RecrutadorController {
    private final UsuarioService usuarioService;
    private final VagasService vagasService;
    private final EmailService emailService;

    @Operation(summary = "Ver dados", description = "Lista todos os usuarios na base de dados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Deu certo!"),
            @ApiResponse(responseCode = "400",description = "Erro na validação de dados"),
            @ApiResponse(responseCode = "500",description = "Erro do servidor")
    })
    @GetMapping
    public Optional<UsuarioDTO> listarDadosDoCandidato() throws RegraDeNegocioException {
        return usuarioService.listarDadosDoRecrutadorLogado();
    }

    @Operation(summary = "Atualizar nome e senha", description = "Atualiza de acordo com a base de dados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Deu certo!"),
            @ApiResponse(responseCode = "400",description = "Erro na validação de dados"),
            @ApiResponse(responseCode = "500",description = "Erro do servidor")
    })
    @PutMapping
    public AtualizarUsuarioDTO atualizarRecrutador(@RequestBody @Valid AtualizarUsuarioDTO dto) throws RegraDeNegocioException {
        return usuarioService.atualizarUsuario(dto);
    }
    @PostMapping("/criar-vaga")
    public VagasDTO criarVaga(@RequestBody @Valid VagasDTO vagasDTO) throws RegraDeNegocioException {
        log.info("Vaga Criada com Sucesso");
        return vagasService.criarVaga(vagasDTO);
    }

    @Operation(summary = "Envia email para os candidatos aprovados", description = "Este processo realiza o envio de um email automático")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Deu certo!"),
            @ApiResponse(responseCode = "400",description = "Erro na validação de dados"),
            @ApiResponse(responseCode = "500",description = "Erro do servidor")
    })
    @PostMapping("/EnviarEmailAprovado")
    @PreAuthorize("hasRole('RECRUTADOR')")
    public void EmailAprovados(String para, String assunto, String nome) throws MessagingException {
        this.emailService.enviarEmailComTemplateAprovado(para, assunto, nome);
    }

    @Operation(summary = "Envia email para os candidatos reprovados", description = "Este processo realiza o envio de um email automático")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Deu certo!"),
            @ApiResponse(responseCode = "400",description = "Erro na validação de dados"),
            @ApiResponse(responseCode = "500",description = "Erro do servidor")
    })
    @PostMapping("/EnviarEmailReprovado")
    @PreAuthorize("hasRole('RECRUTADOR')")
    public void EmailReprovados(String para, String assunto, String nome) throws MessagingException {
        this.emailService.enviarEmailComTemplateReprovado(para, assunto, nome);
    }

    @Operation(summary = "Deletar um recrutador", description = "Este processo realiza a remoção de um Recrutador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Deu certo!"),
            @ApiResponse(responseCode = "400",description = "Erro na validação de dados"),
            @ApiResponse(responseCode = "500",description = "Erro do servidor")
    })
    @DeleteMapping
    public void deletarRecrutador() throws RegraDeNegocioException{
        usuarioService.remover();
    }

    //Analisar Candidatos - Recrutador
    @GetMapping("/analisar-candidatos")
    public List<VagasDTO> analisarVaga() throws SQLException, RegraDeNegocioException {
        List<VagasDTO> lista = vagasService.analisarVaga();
        return lista;
    }

}
