package com.planejamais.service;

import com.planejamais.domain.StatusEstudo;
import com.planejamais.domain.TipoItem;
import com.planejamais.dto.PainelHojeItemResponse;
import com.planejamais.entity.Assunto;
import com.planejamais.entity.Usuario;
import com.planejamais.repository.AssuntoRepository;
import com.planejamais.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PainelHojeService extends BaseService {

    private final AssuntoRepository assuntoRepository;

    public PainelHojeService(UsuarioRepository usuarioRepository,
                             AssuntoRepository assuntoRepository) {
        super(usuarioRepository);
        this.assuntoRepository = assuntoRepository;
    }

    @Transactional(readOnly = true)
    public List<PainelHojeItemResponse> getPainelHoje(String email) {
        Usuario usuario = getUsuario(email);
        LocalDate hoje = LocalDate.now();

        List<Assunto> programados = assuntoRepository.findProgramadosParaHoje(
                usuario.getId(), hoje, StatusEstudo.DOMINADO);
        List<Assunto> atrasados = assuntoRepository.findAtrasados(
                usuario.getId(), hoje, StatusEstudo.DOMINADO);
        List<Assunto> entregasHoje = assuntoRepository.findEntregasParaHoje(usuario.getId(), hoje);

        Map<Long, PainelHojeItemResponse> itens = new LinkedHashMap<>();

        for (Assunto a : atrasados) {
            itens.put(a.getId(), toItem(a, true, false));
        }
        for (Assunto a : programados) {
            itens.putIfAbsent(a.getId(), toItem(a, false, false));
        }
        for (Assunto a : entregasHoje) {
            itens.putIfAbsent(a.getId(), toItem(a, false, true));
        }

        return new ArrayList<>(itens.values());
    }

    private PainelHojeItemResponse toItem(Assunto a, boolean atrasado, boolean entrega) {
        return new PainelHojeItemResponse(
                a.getId(),
                a.getTitulo(),
                a.getTipo(),
                a.getStatusEstudo(),
                a.getDataProgramada(),
                a.getDataEntrega(),
                a.getDisciplina().getId(),
                a.getDisciplina().getNome(),
                atrasado,
                entrega,
                a.getUltimaSessaoEm()
        );
    }
}
