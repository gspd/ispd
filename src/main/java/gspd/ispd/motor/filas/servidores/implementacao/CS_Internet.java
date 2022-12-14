/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gspd.ispd.motor.filas.servidores.implementacao;

import gspd.ispd.motor.Simulation;
import gspd.ispd.motor.EventoFuturo;
import gspd.ispd.motor.filas.Mensagem;
import gspd.ispd.motor.filas.Tarefa;
import gspd.ispd.motor.filas.servidores.CS_Comunicacao;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author denison_usuario
 */
public class CS_Internet extends CS_Comunicacao implements Vertice {

    private List<CS_Link> conexoesEntrada;
    private List<CS_Link> conexoesSaida;
    private Integer pacotes = 0;

    public CS_Internet(String id, double LarguraBanda, double Ocupacao, double Latencia) {
        super(id, LarguraBanda, Ocupacao, Latencia);
        this.conexoesEntrada = new ArrayList<CS_Link>();
        this.conexoesSaida = new ArrayList<CS_Link>();
    }

    public List<CS_Link> getConexoesEntrada() {
        return conexoesEntrada;
    }

    @Override
    public List<CS_Link> getConexoesSaida() {
        return conexoesSaida;
    }

    @Override
    public void addConexoesEntrada(CS_Link conexao) {
        this.conexoesEntrada.add(conexao);
    }

    @Override
    public void addConexoesSaida(CS_Link conexao) {
        this.conexoesSaida.add(conexao);
    }

    @Override
    public void chegadaDeCliente(Simulation simulacao, Tarefa cliente) {
        //cria evento para iniciar o atendimento imediatamente
        EventoFuturo novoEvt = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.ATENDIMENTO,
                this,
                cliente);
        simulacao.addEventoFuturo(novoEvt);
    }

    @Override
    public void atendimento(Simulation simulacao, Tarefa cliente) {
        pacotes++;
        cliente.iniciarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para atender proximo cliente da lista
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this) + tempoTransmitir(cliente.getTamComunicacao()),
                EventoFuturo.SAIDA,
                this, cliente);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    @Override
    public void saidaDeCliente(Simulation simulacao, Tarefa cliente) {
        pacotes--;
        //Incrementa o n??mero de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        //Incrementa o tempo de transmiss??o
        double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Incrementa o tempo de transmiss??o no pacote
        cliente.finalizarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para chegada da tarefa no proximo servidor
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.CHEGADA,
                cliente.getCaminho().remove(0), cliente);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    @Override
    public void requisicao(Simulation simulacao, Mensagem cliente, int tipo) {
        //Incrementa o n??mero de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        //Incrementa o tempo de transmiss??o
        double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Gera evento para chegada da tarefa no proximo servidor
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this) + tempoTrans,
                EventoFuturo.MENSAGEM,
                cliente.getCaminho().remove(0), cliente);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }
    
    @Override
    public Integer getCargaTarefas() {
        return pacotes;
    }
}
