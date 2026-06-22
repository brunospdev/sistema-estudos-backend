package com.planejamais.service;

import com.planejamais.domain.LayoutMode;
import com.planejamais.domain.StatusEstudo;
import com.planejamais.domain.TipoItem;
import com.planejamais.dto.*;
import com.planejamais.entity.*;
import com.planejamais.exception.ForbiddenException;
import com.planejamais.exception.ResourceNotFoundException;
import com.planejamais.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DisciplinaService extends BaseService {

    private final DisciplinaRepository disciplinaRepository;
    private final AssuntoRepository assuntoRepository;
    private final AssuntoDescricaoRepository assuntoDescricaoRepository;
    private final EventoStatusRepository eventoStatusRepository;
    private final PreferenciasService preferenciasService;
    private final RecorrenciaService recorrenciaService;

    public DisciplinaService(UsuarioRepository usuarioRepository,
                             DisciplinaRepository disciplinaRepository,
                             AssuntoRepository assuntoRepository,
                             AssuntoDescricaoRepository assuntoDescricaoRepository,
                             EventoStatusRepository eventoStatusRepository,
                             PreferenciasService preferenciasService,
                             RecorrenciaService recorrenciaService) {
        super(usuarioRepository);
        this.disciplinaRepository = disciplinaRepository;
        this.assuntoRepository = assuntoRepository;
        this.assuntoDescricaoRepository = assuntoDescricaoRepository;
        this.eventoStatusRepository = eventoStatusRepository;
        this.preferenciasService = preferenciasService;
        this.recorrenciaService = recorrenciaService;
    }

    public List<DisciplinaResponse> listar(String email) {
        Usuario usuario = getUsuario(email);
        PreferenciasUsuario prefs = preferenciasService.getPreferencias(usuario);
        List<Disciplina> todas = disciplinaRepository.findByUsuarioWithUsuario(usuario);

        if (prefs.getLayoutMode() == LayoutMode.LISTA) {
            Disciplina plano = preferenciasService.ensurePlanoOculto(usuario);
            Map<Long, List<Assunto>> porDisciplina = carregarAssuntosHierarquicos(List.of(plano.getId()));
            List<Assunto> assuntos = porDisciplina.getOrDefault(plano.getId(), List.of());
            Map<Long, List<AssuntoDescricao>> descricoesMap = carregarDescricoesMap(coletarIdsAssuntos(assuntos));
            return List.of(toResponse(plano, assuntos, List.of(), descricoesMap));
        }

        List<Disciplina> raizes = todas.stream()
                .filter(d -> !d.isOculta() && d.getParent() == null)
                .toList();

        if (raizes.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Disciplina>> filhosPorPai = todas.stream()
                .filter(d -> d.getParent() != null && !d.isOculta())
                .collect(Collectors.groupingBy(d -> d.getParent().getId()));

        List<Long> idsVisiveis = todas.stream()
                .filter(d -> !d.isOculta())
                .map(Disciplina::getId)
                .toList();
        Map<Long, List<Assunto>> assuntosPorDisciplina = carregarAssuntosHierarquicos(idsVisiveis);
        Map<Long, List<AssuntoDescricao>> descricoesMap = carregarDescricoesMap(
                coletarIdsAssuntos(assuntosPorDisciplina.values().stream().flatMap(Collection::stream).toList())
        );

        return raizes.stream()
                .map(d -> toResponseComSubgrupos(d, filhosPorPai, assuntosPorDisciplina, descricoesMap))
                .toList();
    }

    private Set<Long> coletarIdsAssuntos(List<Assunto> raiz) {
        Set<Long> ids = new HashSet<>();
        if (raiz.isEmpty()) {
            return ids;
        }
        raiz.forEach(a -> ids.add(a.getId()));
        List<Long> raizIds = raiz.stream().map(Assunto::getId).toList();
        assuntoRepository.findByParentItem_IdInOrderBySortOrderAscIdAsc(raizIds)
                .forEach(a -> ids.add(a.getId()));
        return ids;
    }

    private Map<Long, List<AssuntoDescricao>> carregarDescricoesMap(Set<Long> assuntoIds) {
        if (assuntoIds.isEmpty()) {
            return Map.of();
        }
        return assuntoDescricaoRepository.findByAssunto_IdInOrderBySortOrderAscIdAsc(new ArrayList<>(assuntoIds))
                .stream()
                .collect(Collectors.groupingBy(d -> d.getAssunto().getId()));
    }

    private Map<Long, List<Assunto>> carregarAssuntosHierarquicos(List<Long> disciplinaIds) {
        if (disciplinaIds.isEmpty()) {
            return Map.of();
        }
        List<Assunto> raiz = assuntoRepository.findByDisciplinaIdInAndParentItemIsNullOrderBySortOrderAscIdAsc(disciplinaIds);
        return raiz.stream().collect(Collectors.groupingBy(a -> a.getDisciplina().getId()));
    }

    public DisciplinaResponse criar(DisciplinaRequest request, String email) {
        Usuario usuario = getUsuario(email);
        PreferenciasUsuario prefs = preferenciasService.getPreferencias(usuario);
        if (prefs.getLayoutMode() == LayoutMode.LISTA) {
            throw new ForbiddenException("No modo lista, adicione itens diretamente.");
        }

        Disciplina parent = null;
        if (request.getParentId() != null) {
            parent = findDisciplinaOwned(request.getParentId(), usuario);
        }
        final Disciplina parentFinal = parent;

        int maxOrder = disciplinaRepository.findByUsuarioWithUsuario(usuario).stream()
                .filter(d -> parentFinal == null ? d.getParent() == null : d.getParent() != null && d.getParent().getId().equals(parentFinal.getId()))
                .mapToInt(Disciplina::getSortOrder)
                .max()
                .orElse(-1);

        Disciplina disciplina = Disciplina.builder()
                .nome(request.getNome())
                .usuario(usuario)
                .parent(parent)
                .sortOrder(maxOrder + 1)
                .build();
        disciplinaRepository.save(disciplina);
        return toResponse(disciplina, List.of(), List.of(), Map.of());
    }

    public DisciplinaResponse editar(Long id, DisciplinaRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = findDisciplinaOwned(id, usuario);
        disciplina.setNome(request.getNome());
        disciplinaRepository.save(disciplina);
        List<Assunto> assuntos = assuntoRepository.findByDisciplinaOrderBySortOrderAscIdAsc(disciplina).stream()
                .filter(a -> a.getParentItem() == null)
                .toList();
        List<Disciplina> filhos = disciplinaRepository.findByUsuarioWithUsuario(usuario).stream()
                .filter(d -> d.getParent() != null && d.getParent().getId().equals(disciplina.getId()))
                .toList();
        List<DisciplinaResponse> subgrupos = filhos.stream()
                .map(f -> toResponse(f, List.of(), List.of(), Map.of()))
                .toList();
        Map<Long, List<AssuntoDescricao>> descricoesMap = carregarDescricoesMap(coletarIdsAssuntos(assuntos));
        return toResponse(disciplina, assuntos, subgrupos, descricoesMap);
    }

    @Transactional
    public void excluir(Long id, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = findDisciplinaOwned(id, usuario);
        if (disciplina.isOculta()) {
            throw new ForbiddenException("Não é possível excluir a disciplina do plano.");
        }
        List<Disciplina> todas = disciplinaRepository.findByUsuarioWithUsuario(usuario);
        List<Long> idsParaExcluir = new ArrayList<>();
        coletarIdsSubarvore(disciplina.getId(), todas, idsParaExcluir);
        Collections.reverse(idsParaExcluir);
        idsParaExcluir.forEach(did -> {
            Disciplina d = todas.stream().filter(x -> x.getId().equals(did)).findFirst().orElseThrow();
            assuntoRepository.deleteAll(assuntoRepository.findByDisciplinaOrderBySortOrderAscIdAsc(d));
        });
        idsParaExcluir.forEach(did -> disciplinaRepository.deleteById(did));
    }

    private void coletarIdsSubarvore(Long id, List<Disciplina> todas, List<Long> out) {
        out.add(id);
        todas.stream()
                .filter(d -> d.getParent() != null && d.getParent().getId().equals(id))
                .forEach(f -> coletarIdsSubarvore(f.getId(), todas, out));
    }

    @Transactional
    public void reordenar(ReorderRequest request, String email) {
        Usuario usuario = getUsuario(email);
        List<Long> ids = request.getIds();
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(id, usuario)
                    .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada: " + id));
            disciplina.setSortOrder(i);
            disciplinaRepository.save(disciplina);
        }
    }

    @Transactional
    public DisciplinaResponse.TopicoResponse adicionarTopico(Long disciplinaId, TopicoCreateRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = resolveDisciplinaParaTopico(disciplinaId, usuario);
        TipoItem tipo = request.getTipo() != null ? request.getTipo() : TipoItem.CONTEUDO;

        Assunto parentItem = null;
        if (request.getParentItemId() != null) {
            parentItem = assuntoRepository.findById(request.getParentItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item pai não encontrado."));
            if (!parentItem.getDisciplina().getId().equals(disciplina.getId())) {
                throw new ForbiddenException("Item pai não pertence a esta matéria.");
            }
        }
        final Assunto parentItemFinal = parentItem;

        int maxOrder = assuntoRepository.findByDisciplinaOrderBySortOrderAscIdAsc(disciplina).stream()
                .filter(a -> parentItemFinal == null
                        ? a.getParentItem() == null
                        : a.getParentItem() != null && a.getParentItem().getId().equals(parentItemFinal.getId()))
                .mapToInt(Assunto::getSortOrder)
                .max()
                .orElse(-1);

        LocalDate dataEntrega = request.getDataEntrega() != null
                ? request.getDataEntrega()
                : (isTipoComEntrega(tipo) ? request.getDataProgramada() : null);

        Assunto assunto = Assunto.builder()
                .titulo(request.getNome())
                .tipo(tipo)
                .statusEstudo(StatusEstudo.NAO_INICIADO)
                .dataProgramada(request.getDataProgramada())
                .dataEntrega(dataEntrega)
                .notaMaxima(request.getNotaMaxima())
                .sortOrder(maxOrder + 1)
                .parentItem(parentItem)
                .disciplina(disciplina)
                .build();
        assuntoRepository.save(assunto);
        if (request.getDescricao() != null && !request.getDescricao().isBlank()) {
            assuntoDescricaoRepository.save(AssuntoDescricao.builder()
                    .assunto(assunto)
                    .texto(request.getDescricao().trim())
                    .sortOrder(0)
                    .build());
        }

        if (request.getRecorrencia() != null) {
            LocalDate dataBase = dataEntrega != null ? dataEntrega : request.getDataProgramada();
            Recorrencia recorrencia = recorrenciaService.criarRecorrencia(usuario, request.getRecorrencia(), dataBase);
            assunto.setRecorrencia(recorrencia);
            assunto.setIndiceOcorrencia(0);
            assuntoRepository.save(assunto);
            recorrenciaService.gerarOcorrencias(recorrencia, assunto, 1);
        }

        Map<Long, List<AssuntoDescricao>> descricoesMap = carregarDescricoesMap(Set.of(assunto.getId()));
        return toTopicoResponse(assunto, List.of(), descricoesMap);
    }

    @Transactional
    public DisciplinaResponse.TopicoResponse editarTopico(Long disciplinaId, Long topicoId, TopicoUpdateRequest request, String email) {
        Assunto assunto = findTopicoOwned(disciplinaId, topicoId, email);
        assunto.setTitulo(request.getNome());
        if (request.getDataProgramada() != null) {
            assunto.setDataProgramada(request.getDataProgramada());
        }
        assuntoRepository.save(assunto);
        List<Assunto> subitens = carregarSubitens(topicoId);
        return toTopicoResponse(assunto, subitens, descricoesMapParaTopico(topicoId, subitens));
    }

    @Transactional
    public DisciplinaResponse.TopicoResponse patchAvaliacao(Long disciplinaId, Long topicoId, TopicoAvaliacaoRequest request, String email) {
        Assunto assunto = findTopicoOwned(disciplinaId, topicoId, email);
        if (request.getNota() != null) {
            assunto.setNota(request.getNota());
        }
        if (request.getNotaMaxima() != null) {
            assunto.setNotaMaxima(request.getNotaMaxima());
        }
        if (request.getDataRealizada() != null) {
            assunto.setDataRealizada(request.getDataRealizada());
        }
        if (request.getEntregaConcluida() != null) {
            assunto.setEntregaConcluida(request.getEntregaConcluida());
            if (request.getEntregaConcluida()) {
                assunto.setDataRealizada(request.getDataRealizada() != null ? request.getDataRealizada() : LocalDate.now());
            } else {
                assunto.setDataRealizada(null);
            }
        }
        assuntoRepository.save(assunto);
        List<Assunto> subitens = carregarSubitens(topicoId);
        return toTopicoResponse(assunto, subitens, descricoesMapParaTopico(topicoId, subitens));
    }

    @Transactional
    public DescricaoResponse adicionarDescricao(Long disciplinaId, Long topicoId, DescricaoRequest request, String email) {
        Assunto assunto = findTopicoOwned(disciplinaId, topicoId, email);
        int maxOrder = assuntoDescricaoRepository.findByAssunto_IdOrderBySortOrderAscIdAsc(topicoId).stream()
                .mapToInt(AssuntoDescricao::getSortOrder)
                .max()
                .orElse(-1);
        AssuntoDescricao descricao = assuntoDescricaoRepository.save(AssuntoDescricao.builder()
                .assunto(assunto)
                .texto(request.getTexto().trim())
                .sortOrder(maxOrder + 1)
                .build());
        return toDescricaoResponse(descricao);
    }

    @Transactional
    public DescricaoResponse editarDescricao(Long disciplinaId, Long topicoId, Long descricaoId,
                                             DescricaoRequest request, String email) {
        findTopicoOwned(disciplinaId, topicoId, email);
        AssuntoDescricao descricao = assuntoDescricaoRepository.findById(descricaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Descrição não encontrada."));
        if (!descricao.getAssunto().getId().equals(topicoId)) {
            throw new ForbiddenException("Descrição não pertence a este item.");
        }
        descricao.setTexto(request.getTexto().trim());
        assuntoDescricaoRepository.save(descricao);
        return toDescricaoResponse(descricao);
    }

    @Transactional
    public void excluirDescricao(Long disciplinaId, Long topicoId, Long descricaoId, String email) {
        findTopicoOwned(disciplinaId, topicoId, email);
        AssuntoDescricao descricao = assuntoDescricaoRepository.findById(descricaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Descrição não encontrada."));
        if (!descricao.getAssunto().getId().equals(topicoId)) {
            throw new ForbiddenException("Descrição não pertence a este item.");
        }
        assuntoDescricaoRepository.delete(descricao);
    }

    @Transactional
    public void excluirTopico(Long disciplinaId, Long topicoId, String email) {
        Assunto assunto = findTopicoOwned(disciplinaId, topicoId, email);
        List<Assunto> filhos = assuntoRepository.findByParentItem_IdInOrderBySortOrderAscIdAsc(List.of(topicoId));
        assuntoRepository.deleteAll(filhos);
        assuntoRepository.delete(assunto);
    }

    @Transactional
    public DisciplinaResponse.TopicoResponse patchStatus(Long disciplinaId, Long topicoId, TopicoStatusRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Assunto assunto = findTopicoOwned(disciplinaId, topicoId, email);

        StatusEstudo anterior = assunto.getStatusEstudo();
        StatusEstudo novo = request.getStatus();

        if (anterior != novo) {
            assunto.setStatusEstudo(novo);
            if (novo == StatusEstudo.DOMINADO) {
                assunto.setDataConclusao(LocalDate.now());
            } else {
                assunto.setDataConclusao(null);
            }
            assuntoRepository.save(assunto);

            eventoStatusRepository.save(EventoStatus.builder()
                    .usuario(usuario)
                    .assunto(assunto)
                    .statusAnterior(anterior)
                    .statusNovo(novo)
                    .build());
        }

        return toTopicoResponse(assunto, carregarSubitens(topicoId),
                descricoesMapParaTopico(topicoId, carregarSubitens(topicoId)));
    }

    @Transactional
    public DisciplinaResponse.TopicoResponse patchRecorrencia(Long disciplinaId, Long topicoId,
                                                              RecorrenciaRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Assunto assunto = findTopicoOwned(disciplinaId, topicoId, email);
        recorrenciaService.aplicarRecorrenciaEmAssunto(assunto, request, usuario);
        List<Assunto> subitens = carregarSubitens(topicoId);
        return toTopicoResponse(assunto, subitens, descricoesMapParaTopico(topicoId, subitens));
    }

    @Transactional
    public void excluirRecorrencia(Long disciplinaId, Long topicoId, String escopo, String email) {
        Usuario usuario = getUsuario(email);
        findTopicoOwned(disciplinaId, topicoId, email);
        recorrenciaService.removerRecorrencia(disciplinaId, topicoId, escopo, usuario);
    }

    @Transactional
    public DisciplinaResponse.TopicoResponse patchAgenda(Long disciplinaId, Long topicoId, TopicoAgendaRequest request, String email) {
        Assunto assunto = findTopicoOwned(disciplinaId, topicoId, email);
        LocalDate data = request.getDataProgramada();
        if (data != null) {
            if (isTipoEvento(assunto.getTipo())) {
                assunto.setDataEntrega(data);
                assunto.setDataProgramada(null);
            } else {
                assunto.setDataProgramada(data);
                if (isTipoComEntrega(assunto.getTipo())) {
                    assunto.setDataEntrega(data);
                }
            }
        } else {
            assunto.setDataProgramada(null);
            if (isTipoEvento(assunto.getTipo()) || isTipoComEntrega(assunto.getTipo())) {
                assunto.setDataEntrega(null);
            }
        }
        assuntoRepository.save(assunto);
        List<Assunto> subitens = carregarSubitens(topicoId);
        return toTopicoResponse(assunto, subitens, descricoesMapParaTopico(topicoId, subitens));
    }

    @Transactional
    public DisciplinaResponse.TopicoResponse toggleTopico(Long disciplinaId, Long topicoId, String email) {
        Assunto assunto = findTopicoOwned(disciplinaId, topicoId, email);
        StatusEstudo novo = assunto.getStatusEstudo() == StatusEstudo.DOMINADO
                ? StatusEstudo.NAO_INICIADO
                : StatusEstudo.DOMINADO;
        TopicoStatusRequest statusRequest = new TopicoStatusRequest();
        statusRequest.setStatus(novo);
        return patchStatus(disciplinaId, topicoId, statusRequest, email);
    }

    @Transactional
    public void reordenarTopicos(Long disciplinaId, ReorderRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = findDisciplinaOwned(disciplinaId, usuario);
        List<Long> ids = request.getIds();
        for (int i = 0; i < ids.size(); i++) {
            Long topicoId = ids.get(i);
            Assunto assunto = assuntoRepository.findById(topicoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tópico não encontrado."));
            if (!assunto.getDisciplina().getId().equals(disciplina.getId())) {
                throw new ForbiddenException("Tópico não pertence a esta disciplina.");
            }
            assunto.setSortOrder(i);
            assuntoRepository.save(assunto);
        }
    }

    private List<Assunto> carregarSubitens(Long parentId) {
        return assuntoRepository.findByParentItem_IdInOrderBySortOrderAscIdAsc(List.of(parentId));
    }

    private boolean isTipoComEntrega(TipoItem tipo) {
        return tipo == TipoItem.SIMULADO || tipo == TipoItem.PROVA
                || tipo == TipoItem.ATIVIDADE || tipo == TipoItem.ENTREGA || tipo == TipoItem.PRATICA;
    }

    private boolean isTipoEvento(TipoItem tipo) {
        return tipo == TipoItem.SIMULADO || tipo == TipoItem.PROVA
                || tipo == TipoItem.ATIVIDADE || tipo == TipoItem.ENTREGA
                || tipo == TipoItem.PRESENCIAL || tipo == TipoItem.OUTRO;
    }

    private Disciplina resolveDisciplinaParaTopico(Long disciplinaId, Usuario usuario) {
        PreferenciasUsuario prefs = preferenciasService.getPreferencias(usuario);
        if (prefs.getLayoutMode() == LayoutMode.LISTA) {
            return preferenciasService.ensurePlanoOculto(usuario);
        }
        return findDisciplinaOwned(disciplinaId, usuario);
    }

    private Disciplina findDisciplinaOwned(Long id, Usuario usuario) {
        return disciplinaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada."));
    }

    private Assunto findTopicoOwned(Long disciplinaId, Long topicoId, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = findDisciplinaOwned(disciplinaId, usuario);
        Assunto assunto = assuntoRepository.findById(topicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Tópico não encontrado."));
        if (!assunto.getDisciplina().getId().equals(disciplina.getId())) {
            throw new ForbiddenException("Tópico não pertence a esta disciplina.");
        }
        return assunto;
    }

    private DisciplinaResponse toResponseComSubgrupos(Disciplina d,
                                                      Map<Long, List<Disciplina>> filhosPorPai,
                                                      Map<Long, List<Assunto>> assuntosPorDisciplina,
                                                      Map<Long, List<AssuntoDescricao>> descricoesMap) {
        List<Disciplina> filhos = filhosPorPai.getOrDefault(d.getId(), List.of());
        List<DisciplinaResponse> subgrupos = filhos.stream()
                .map(f -> toResponse(f, assuntosPorDisciplina.getOrDefault(f.getId(), List.of()), List.of(), descricoesMap))
                .toList();
        return toResponse(d, assuntosPorDisciplina.getOrDefault(d.getId(), List.of()), subgrupos, descricoesMap);
    }

    private DisciplinaResponse toResponse(Disciplina d, List<Assunto> assuntosRaiz,
                                          List<DisciplinaResponse> subgrupos,
                                          Map<Long, List<AssuntoDescricao>> descricoesMap) {
        List<Long> raizIds = assuntosRaiz.stream().map(Assunto::getId).toList();
        Map<Long, List<Assunto>> subitensMap = raizIds.isEmpty()
                ? Map.of()
                : assuntoRepository.findByParentItem_IdInOrderBySortOrderAscIdAsc(raizIds).stream()
                .collect(Collectors.groupingBy(a -> a.getParentItem().getId()));

        List<DisciplinaResponse.TopicoResponse> topicos = assuntosRaiz.stream()
                .map(a -> toTopicoResponse(a, subitensMap.getOrDefault(a.getId(), List.of()), descricoesMap))
                .toList();

        Long parentId = d.getParent() != null ? d.getParent().getId() : null;
        return new DisciplinaResponse(
                d.getId(), d.getNome(), d.getDataCriacao(), d.getSortOrder(), d.isOculta(),
                parentId, topicos, subgrupos
        );
    }

    private Map<Long, List<AssuntoDescricao>> descricoesMapParaTopico(Long topicoId, List<Assunto> subitens) {
        Set<Long> ids = new HashSet<>();
        ids.add(topicoId);
        subitens.forEach(s -> ids.add(s.getId()));
        return carregarDescricoesMap(ids);
    }

    private DescricaoResponse toDescricaoResponse(AssuntoDescricao d) {
        return new DescricaoResponse(d.getId(), d.getTexto());
    }

    private List<DescricaoResponse> toDescricaoResponses(List<AssuntoDescricao> descricoes) {
        if (descricoes == null || descricoes.isEmpty()) {
            return List.of();
        }
        return descricoes.stream().map(this::toDescricaoResponse).toList();
    }

    private DisciplinaResponse.TopicoResponse toTopicoResponse(Assunto a, List<Assunto> subitens,
                                                               Map<Long, List<AssuntoDescricao>> descricoesMap) {
        List<DisciplinaResponse.TopicoResponse> subResponses = subitens.stream()
                .map(s -> toTopicoResponse(s, List.of(), descricoesMap))
                .toList();
        Long parentItemId = a.getParentItem() != null ? a.getParentItem().getId() : null;
        List<DescricaoResponse> descricoes = toDescricaoResponses(descricoesMap.getOrDefault(a.getId(), List.of()));
        return new DisciplinaResponse.TopicoResponse(
                a.getId(),
                a.getTitulo(),
                a.getTipo(),
                a.getStatusEstudo(),
                a.getDataProgramada(),
                a.getDataConclusao(),
                a.getDataEntrega(),
                a.getDataRealizada(),
                a.getNota(),
                a.getNotaMaxima(),
                a.isEntregaConcluida(),
                a.getHorasAcumuladas(),
                a.getSortOrder(),
                a.getUltimaSessaoEm(),
                parentItemId,
                descricoes,
                subResponses,
                recorrenciaService.toResponse(a.getRecorrencia(), a.getIndiceOcorrencia())
        );
    }
}
