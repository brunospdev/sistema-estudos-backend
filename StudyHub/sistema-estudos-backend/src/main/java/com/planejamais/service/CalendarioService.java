package com.planejamais.service;

import com.planejamais.domain.StatusEstudo;
import com.planejamais.domain.TipoItem;
import com.planejamais.dto.CalendarioResponse;
import com.planejamais.dto.MarcoResumoResponse;
import com.planejamais.entity.Assunto;
import com.planejamais.entity.MarcoUsuario;
import com.planejamais.entity.PreferenciasUsuario;
import com.planejamais.entity.Usuario;
import com.planejamais.repository.AssuntoRepository;
import com.planejamais.repository.MarcoUsuarioRepository;
import com.planejamais.repository.PreferenciasUsuarioRepository;
import com.planejamais.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalendarioService extends BaseService {

    private final AssuntoRepository assuntoRepository;
    private final MarcoUsuarioRepository marcoRepository;
    private final PreferenciasUsuarioRepository preferenciasRepository;

    public CalendarioService(UsuarioRepository usuarioRepository,
                             AssuntoRepository assuntoRepository,
                             MarcoUsuarioRepository marcoRepository,
                             PreferenciasUsuarioRepository preferenciasRepository) {
        super(usuarioRepository);
        this.assuntoRepository = assuntoRepository;
        this.marcoRepository = marcoRepository;
        this.preferenciasRepository = preferenciasRepository;
    }

    @Transactional(readOnly = true)
    public CalendarioResponse obter(String email, LocalDate de, LocalDate ate) {
        Usuario usuario = getUsuario(email);

        List<Assunto> assuntos = assuntoRepository.findByUsuarioAndDataProgramadaBetween(usuario.getId(), de, ate);
        List<Assunto> entregas = assuntoRepository.findByUsuarioAndDataEntregaBetween(usuario.getId(), de, ate);
        List<MarcoUsuario> marcos = marcoRepository.findByUsuarioAndDataBetween(usuario.getId(), de, ate);

        Map<LocalDate, CalendarioResponse.DiaCalendario> diasMap = new LinkedHashMap<>();

        for (Assunto assunto : assuntos) {
            adicionarItemCalendario(diasMap, assunto.getDataProgramada(), assunto);
        }

        for (Assunto assunto : entregas) {
            if (assunto.getDataEntrega() != null
                    && (assunto.getDataProgramada() == null || !assunto.getDataEntrega().equals(assunto.getDataProgramada()))) {
                adicionarItemCalendario(diasMap, assunto.getDataEntrega(), assunto);
            }
        }

        for (MarcoUsuario marco : marcos) {
            LocalDate data = marco.getData();
            diasMap.computeIfAbsent(data, d -> new CalendarioResponse.DiaCalendario(d, new ArrayList<>(), new ArrayList<>()));
            diasMap.get(data).getMarcos().add(new CalendarioResponse.MarcoCalendario(
                    marco.getId(),
                    marco.getTipo(),
                    marco.getTitulo(),
                    marco.isEhPrincipal()
            ));
        }

        MarcoResumoResponse marcoPrincipal = preferenciasRepository.findByUsuario(usuario)
                .map(PreferenciasUsuario::getMarcoPrincipal)
                .filter(m -> m != null)
                .map(PreferenciasService::toMarcoResumo)
                .orElse(null);

        return new CalendarioResponse(new ArrayList<>(diasMap.values()), marcoPrincipal);
    }

    private void adicionarItemCalendario(Map<LocalDate, CalendarioResponse.DiaCalendario> diasMap,
                                         LocalDate data,
                                         Assunto assunto) {
        diasMap.computeIfAbsent(data, d -> new CalendarioResponse.DiaCalendario(d, new ArrayList<>(), new ArrayList<>()));
        boolean jaExiste = diasMap.get(data).getItens().stream()
                .anyMatch(i -> i.getId().equals(assunto.getId()));
        if (!jaExiste) {
            diasMap.get(data).getItens().add(new CalendarioResponse.ItemCalendario(
                    assunto.getId(),
                    assunto.getTitulo(),
                    assunto.getDisciplina().getNome(),
                    assunto.getStatusEstudo(),
                    assunto.getTipo()
            ));
        }
    }
}
