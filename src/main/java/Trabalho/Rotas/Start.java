package Trabalho.Rotas;

import it.polito.appeal.traci.SumoTraciConnection;

public class Start extends Thread {
    private SumoTraciConnection sumo;
    private long acquisitionRate;

    public Start(SumoTraciConnection _sumo, long _acquisitionRate) {
        this.sumo = _sumo;
        this.acquisitionRate = _acquisitionRate;
    }

    @Override
    public void run() {
        // Loop que executa continuamente
        while (true) {
            try {
                // Avança a simulação para o próximo passo de tempo
                this.sumo.do_timestep();
                
                // Pausa a thread pelo tempo especificado em acquisitionRate (milissegundos)
                sleep(acquisitionRate);
            } catch (Exception e) {
                // Captura e trata exceções que possam ocorrer durante a execução
                e.printStackTrace();
                break; // Encerra o loop em caso de exceção
            }
        }
    }
}