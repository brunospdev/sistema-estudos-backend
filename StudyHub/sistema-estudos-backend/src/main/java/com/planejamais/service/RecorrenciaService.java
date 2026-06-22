package com.planejamais.service;

import com.planejamais.domain.FrequenciaRecorrencia;
import com.planejamais.domain.StatusEstudo;
import com.planejamais.domain.TipoItem;
import com.planejamais.dto.RecorrenciaRequest;
import com.planejamais.dto.RecorrenciaResponse;
import com.planejamais.entity.Assunto;
import com.planejamais.entity.Recorrencia;
import com.planejamais.entity.Usuario;
import com.planejamais.exception.ForbiddenException;
import com.planejamais.exception.ResourceNotFoundException;
import com.planejamais.repository.AssuntoRepository;
import com.planejamais.repository.RecorrenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecorrenciaService {

    private static final int LIMITE_PADRAO_OCORRENCIAS = 52;
    private static final int LIMITE_MESES_FRENTE = 12;

    private final RecorrenciaRepository recorrenciaRepository;
    private final AssuntoRepository assuntoRepository;

    public RecorrenciaService(RecorrenciaRepository recorrenciaRepository,
                              AssuntoRepository assuntoRepository) {
        this.recorrenciaRepository = recorrenciaRepository;
        this.assuntoRepository = assuntoRepository;
    }

    @Transactional
    public Recorrencia criarRecorrencia(Usuario usuario, RecorrenciaRequest request, LocalDate dataInicio) {
        LocalDate inicio = request.getDataInicio() != null ? request.getDataInicio() : dataInicio;
        if (inicio == null) {
            throw new IllegalArgumentException("Data de início é obrigatória para recorrência.");
        }

        Recorrencia recorrencia = Recorrencia.builder()
                .usuario(usuario)
                .frequencia(request.getFrequencia())
                .intervalo(request.getIntervalo() != null ? request.getIntervalo() : 1)
                .diasSemana(serializarDiasSemana(request.getDiasSemana(), inicio, request.getFrequencia()))
                .diaMes(request.getDiaMes() != null ? request.getDiaMes() : inicio.getDayOfMonth())
                .dataInicio(inicio)
                .dataFim(request.getDataFim())
                .maxOcorrencias(request.getMaxOcorrencias())
                .ativa(true)
                .build();

        return recorrenciaRepository.save(recorrencia);
    }

    @Transactional
    public List<Assunto> gerarOcorrencias(Recorrencia recorrencia, Assunto modelo, int indiceInicial) {
        List<LocalDate> datas = calcularDatas(recorrencia);
        List<Assunto> criados = new ArrayList<>();

        for (int i = indiceInicial; i < datas.size(); i++) {
            LocalDate data = datas.get(i);
            Assunto ocorrencia = clonarAssunto(modelo, data, recorrencia, i);
            criados.add(assuntoRepository.save(ocorrencia));
        }
        return criados;
    }

    @Transactional
    public RecorrenciaResponse aplicarRecorrenciaEmAssunto(Assunto assunto, RecorrenciaRequest request, Usuario usuario) {
        LocalDate dataBase = dataBaseDoAssunto(assunto);
        if (dataBase == null) {
            throw new IllegalArgumentException("Informe uma data antes de configurar a recorrência.");
        }

        if (assunto.getRecorrencia() != null) {
            return atualizarRecorrenciaExistente(assunto, request, usuario);
        }

        Recorrencia recorrencia = criarRecorrencia(usuario, request, dataBase);
        assunto.setRecorrencia(recorrencia);
        assunto.setIndiceOcorrencia(0);
        aplicarDataNoAssunto(assunto, dataBase);
        assuntoRepository.save(assunto);

        gerarOcorrencias(recorrencia, assunto, 1);
        return toResponse(recorrencia, 0);
    }

    @Transactional
    public RecorrenciaResponse atualizarRecorrenciaExistente(Assunto assunto, RecorrenciaRequest request, Usuario usuario) {
        Recorrencia recorrencia = assunto.getRecorrencia();
        if (recorrencia == null || !recorrencia.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Recorrência não pertence ao usuário.");
        }

        LocalDate dataBase = request.getDataInicio() != null ? request.getDataInicio() : dataBaseDoAssunto(assunto);
        recorrencia.setFrequencia(request.getFrequencia());
        recorrencia.setIntervalo(request.getIntervalo() != null ? request.getIntervalo() : 1);
        recorrencia.setDiasSemana(serializarDiasSemana(request.getDiasSemana(), dataBase, request.getFrequencia()));
        recorrencia.setDiaMes(request.getDiaMes() != null ? request.getDiaMes() : dataBase.getDayOfMonth());
        recorrencia.setDataInicio(dataBase);
        recorrencia.setDataFim(request.getDataFim());
        recorrencia.setMaxOcorrencias(request.getMaxOcorrencias());
        recorrencia.setAtiva(true);
        recorrenciaRepository.save(recorrencia);

        removerOcorrenciasFuturas(recorrencia, assunto.getIndiceOcorrencia() != null ? assunto.getIndiceOcorrencia() : 0);
        gerarOcorrencias(recorrencia, assunto, (assunto.getIndiceOcorrencia() != null ? assunto.getIndiceOcorrencia() : 0) + 1);

        return toResponse(recorrencia, assunto.getIndiceOcorrencia());
    }

    @Transactional
    public void removerRecorrencia(Long disciplinaId, Long topicoId, String escopo, Usuario usuario) {
        Assunto assunto = assuntoRepository.findById(topicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Tópico não encontrado."));
        if (!assunto.getDisciplina().getId().equals(disciplinaId)) {
            throw new ForbiddenException("Tópico não pertence a esta disciplina.");
        }
        if (assunto.getRecorrencia() == null) {
            return;
        }

        Recorrencia recorrencia = assunto.getRecorrencia();
        if (!recorrencia.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Recorrência não pertence ao usuário.");
        }

        String escopoNorm = escopo != null ? escopo.toUpperCase() : "ESTA";

        switch (escopoNorm) {
            case "TODAS" -> {
                List<Assunto> ocorrencias = assuntoRepository.findByRecorrencia_IdOrderByIndiceOcorrenciaAsc(recorrencia.getId());
                assuntoRepository.deleteAll(ocorrencias);
                recorrenciaRepository.delete(recorrencia);
            }
            case "FUTURAS" -> {
                int indiceAtual = assunto.getIndiceOcorrencia() != null ? assunto.getIndiceOcorrencia() : 0;
                removerOcorrenciasFuturas(recorrencia, indiceAtual);
                assunto.setRecorrencia(null);
                assunto.setIndiceOcorrencia(null);
                assuntoRepository.save(assunto);
                recorrencia.setAtiva(false);
                recorrenciaRepository.save(recorrencia);
            }
            default -> {
                assunto.setRecorrencia(null);
                assunto.setIndiceOcorrencia(null);
                assuntoRepository.save(assunto);
            }
        }
    }

    public RecorrenciaResponse toResponse(Recorrencia recorrencia, Integer indiceOcorrencia) {
        if (recorrencia == null) {
            return null;
        }
        return new RecorrenciaResponse(
                recorrencia.getId(),
                recorrencia.getFrequencia(),
                recorrencia.getIntervalo(),
                deserializarDiasSemana(recorrencia.getDiasSemana()),
                recorrencia.getDiaMes(),
                recorrencia.getDataInicio(),
                recorrencia.getDataFim(),
                recorrencia.getMaxOcorrencias(),
                recorrencia.isAtiva(),
                indiceOcorrencia
        );
    }

    private void removerOcorrenciasFuturas(Recorrencia recorrencia, int indiceAtual) {
        List<Assunto> ocorrencias = assuntoRepository.findByRecorrencia_IdOrderByIndiceOcorrenciaAsc(recorrencia.getId());
        List<Assunto> futuras = ocorrencias.stream()
                .filter(a -> a.getIndiceOcorrencia() != null && a.getIndiceOcorrencia() > indiceAtual)
                .filter(a -> !a.isEntregaConcluida())
                .toList();
        assuntoRepository.deleteAll(futuras);
    }

    private Assunto clonarAssunto(Assunto modelo, LocalDate data, Recorrencia recorrencia, int indice) {
        Assunto clone = Assunto.builder()
                .titulo(modelo.getTitulo())
                .tipo(modelo.getTipo())
                .statusEstudo(StatusEstudo.NAO_INICIADO)
                .dataProgramada(isTipoEvento(modelo.getTipo()) ? null : data)
                .dataEntrega(isTipoEvento(modelo.getTipo()) || isTipoComEntrega(modelo.getTipo()) ? data : null)
                .notaMaxima(modelo.getNotaMaxima())
                .sortOrder(modelo.getSortOrder())
                .parentItem(modelo.getParentItem())
                .disciplina(modelo.getDisciplina())
                .recorrencia(recorrencia)
                .indiceOcorrencia(indice)
                .build();
        return clone;
    }

    private void aplicarDataNoAssunto(Assunto assunto, LocalDate data) {
        if (isTipoEvento(assunto.getTipo()) || isTipoComEntrega(assunto.getTipo())) {
            assunto.setDataEntrega(data);
            if (!isTipoEvento(assunto.getTipo())) {
                assunto.setDataProgramada(data);
            }
        } else {
            assunto.setDataProgramada(data);
        }
    }

    private LocalDate dataBaseDoAssunto(Assunto assunto) {
        if (isTipoEvento(assunto.getTipo())) {
            return assunto.getDataEntrega() != null ? assunto.getDataEntrega() : assunto.getDataProgramada();
        }
        return assunto.getDataProgramada() != null ? assunto.getDataProgramada() : assunto.getDataEntrega();
    }

    List<LocalDate> calcularDatas(Recorrencia recorrencia) {
        LocalDate limite = recorrencia.getDataFim() != null
                ? recorrencia.getDataFim()
                : recorrencia.getDataInicio().plusMonths(LIMITE_MESES_FRENTE);

        int maxTotal = recorrencia.getMaxOcorrencias() != null
                ? recorrencia.getMaxOcorrencias()
                : LIMITE_PADRAO_OCORRENCIAS;

        return switch (recorrencia.getFrequencia()) {
            case DIARIA -> gerarDiaria(recorrencia.getDataInicio(), limite, recorrencia.getIntervalo(), maxTotal);
            case SEMANAL -> gerarSemanal(recorrencia, limite, maxTotal);
            case QUINZENAL -> gerarDiaria(recorrencia.getDataInicio(), limite, 14 * recorrencia.getIntervalo(), maxTotal);
            case MENSAL -> gerarMensal(recorrencia, limite, maxTotal);
            case PERSONALIZADA -> gerarDiaria(recorrencia.getDataInicio(), limite, recorrencia.getIntervalo(), maxTotal);
        };
    }

    private List<LocalDate> gerarDiaria(LocalDate inicio, LocalDate limite, int intervalo, int maxTotal) {
        List<LocalDate> datas = new ArrayList<>();
        LocalDate atual = inicio;
        while (!atual.isAfter(limite) && datas.size() < maxTotal) {
            datas.add(atual);
            atual = atual.plusDays(intervalo);
        }
        return datas;
    }

    private List<LocalDate> gerarSemanal(Recorrencia recorrencia, LocalDate limite, int maxTotal) {
        List<DayOfWeek> dias = deserializarDiasSemana(recorrencia.getDiasSemana()).stream()
                .map(d -> DayOfWeek.of(d))
                .sorted()
                .toList();

        if (dias.isEmpty()) {
            dias = List.of(recorrencia.getDataInicio().getDayOfWeek());
        }

        List<LocalDate> datas = new ArrayList<>();
        LocalDate semanaBase = recorrencia.getDataInicio().minusDays(recorrencia.getDataInicio().getDayOfWeek().getValue() - 1L);

        while (datas.size() < maxTotal) {
            for (DayOfWeek dia : dias) {
                LocalDate candidata = semanaBase.plusDays(dia.getValue() - 1L);
                if (candidata.isBefore(recorrencia.getDataInicio())) {
                    continue;
                }
                if (candidata.isAfter(limite)) {
                    return datas;
                }
                datas.add(candidata);
                if (datas.size() >= maxTotal) {
                    return datas;
                }
            }
            semanaBase = semanaBase.plusWeeks(recorrencia.getIntervalo());
        }
        return datas;
    }

    private List<LocalDate> gerarMensal(Recorrencia recorrencia, LocalDate limite, int maxTotal) {
        List<LocalDate> datas = new ArrayList<>();
        int diaMes = recorrencia.getDiaMes() != null ? recorrencia.getDiaMes() : recorrencia.getDataInicio().getDayOfMonth();
        LocalDate atual = recorrencia.getDataInicio();

        while (!atual.isAfter(limite) && datas.size() < maxTotal) {
            int diaAjustado = Math.min(diaMes, atual.lengthOfMonth());
            LocalDate candidata = LocalDate.of(atual.getYear(), atual.getMonth(), diaAjustado);
            if (!candidata.isBefore(recorrencia.getDataInicio())) {
                datas.add(candidata);
            }
            atual = atual.plusMonths(recorrencia.getIntervalo());
        }
        return datas;
    }

    private String serializarDiasSemana(List<Integer> dias, LocalDate dataInicio, FrequenciaRecorrencia frequencia) {
        if (dias == null || dias.isEmpty()) {
            if (frequencia == FrequenciaRecorrencia.SEMANAL) {
                return String.valueOf(dataInicio.getDayOfWeek().getValue());
            }
            return null;
        }
        return dias.stream()
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private List<Integer> deserializarDiasSemana(String valor) {
        if (valor == null || valor.isBlank()) {
            return List.of();
        }
        return Arrays.stream(valor.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();
    }

    private boolean isTipoEvento(TipoItem tipo) {
        return tipo == TipoItem.SIMULADO || tipo == TipoItem.PROVA
                || tipo == TipoItem.ATIVIDADE || tipo == TipoItem.ENTREGA
                || tipo == TipoItem.PRESENCIAL || tipo == TipoItem.OUTRO;
    }

    private boolean isTipoComEntrega(TipoItem tipo) {
        return tipo == TipoItem.SIMULADO || tipo == TipoItem.PROVA
                || tipo == TipoItem.ATIVIDADE || tipo == TipoItem.ENTREGA || tipo == TipoItem.PRATICA;
    }
}
