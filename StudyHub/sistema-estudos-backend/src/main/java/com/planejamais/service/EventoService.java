package com.planejamais.service;

import com.planejamais.domain.TipoItem;
import com.planejamais.dto.EventoResponse;
import com.planejamais.entity.Assunto;
import com.planejamais.entity.AssuntoDescricao;
import com.planejamais.entity.Usuario;
import com.planejamais.repository.AssuntoDescricaoRepository;
import com.planejamais.repository.AssuntoRepository;
import com.planejamais.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventoService extends BaseService {

    private static final Set<TipoItem> TIPOS_EVENTO = EnumSet.of(
            TipoItem.SIMULADO, TipoItem.PROVA, TipoItem.ATIVIDADE,
            TipoItem.ENTREGA, TipoItem.PRESENCIAL, TipoItem.OUTRO
    );

    private final AssuntoRepository assuntoRepository;
    private final AssuntoDescricaoRepository assuntoDescricaoRepository;

    public EventoService(UsuarioRepository usuarioRepository,
                         AssuntoRepository assuntoRepository,
                         AssuntoDescricaoRepository assuntoDescricaoRepository) {
        super(usuarioRepository);
        this.assuntoRepository = assuntoRepository;
        this.assuntoDescricaoRepository = assuntoDescricaoRepository;
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listar(String email, String filtro) {
        Usuario usuario = getUsuario(email);
        List<Assunto> eventos = assuntoRepository.findEventosByUsuario(usuario.getId());
        List<Assunto> filtrados = eventos.stream()
                .filter(a -> TIPOS_EVENTO.contains(a.getTipo()))
                .filter(a -> aplicarFiltro(a, filtro))
                .toList();

        List<Long> ids = filtrados.stream().map(Assunto::getId).toList();
        Map<Long, List<String>> descricoesPorAssunto = ids.isEmpty()
                ? Map.of()
                : assuntoDescricaoRepository.findByAssunto_IdInOrderBySortOrderAscIdAsc(ids).stream()
                .collect(Collectors.groupingBy(
                        d -> d.getAssunto().getId(),
                        Collectors.mapping(AssuntoDescricao::getTexto, Collectors.toList())
                ));

        return filtrados.stream()
                .map(a -> toResponse(a, descricoesPorAssunto.getOrDefault(a.getId(), List.of())))
                .toList();
    }

    private boolean aplicarFiltro(Assunto a, String filtro) {
        if (filtro == null || filtro.isBlank() || "todos".equalsIgnoreCase(filtro)) {
            return true;
        }
        return switch (filtro.toLowerCase()) {
            case "nota" -> a.getNota() != null;
            case "concluidos" -> a.isEntregaConcluida();
            case "pendentes" -> !a.isEntregaConcluida();
            default -> true;
        };
    }

    private EventoResponse toResponse(Assunto a, List<String> descricoes) {
        return new EventoResponse(
                a.getId(),
                a.getTitulo(),
                a.getTipo(),
                descricoes,
                a.getDataEntrega(),
                a.getDataRealizada(),
                a.getNota(),
                a.getNotaMaxima(),
                a.isEntregaConcluida(),
                a.getDisciplina().getId(),
                a.getDisciplina().getNome()
        );
    }
}
