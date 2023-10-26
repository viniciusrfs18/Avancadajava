package Trabalho.Car;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.json.JSONObject;

import Trabalho.Rotas.Route;
import Trabalho.Car.Car;


public class AtualizaCar extends Thread {
    private Socket carSocket;       // Socket para comunicação com o carro
    private DataInputStream input;  // Stream de entrada para receber dados do carro
    private DataOutputStream output; // Stream de saída para enviar dados ao carro
    private Company company;        // Instância da empresa de mobilidade
    private double initialOdometerReading = -1; // Adicione este atributo

    // Atributo para sincronização
    private Object sync = new Object();

    public AtualizaCar(Socket _carSocket, Company _company) {
        this.company = _company;
        this.carSocket = _carSocket;
        try {
            // Variáveis de input e output do servidor
            input = new DataInputStream(carSocket.getInputStream());
            output = new DataOutputStream(carSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.sync = new Object();
    }

    @Override
    public void run() {
        try {
            String status = "";
            
            while (!status.equals("encerrado")) {

                DrivingData com = extractDrivingData(input.readUTF());
                status = com.getCarStatus(); 

                Company.sendCommunication(com);

               
                
                 double distanceTravelled = com.getOdometer() - initialOdometerReading;

                // Use distanceTravelled para suas lógicas, por exemplo:
                if (distanceTravelled > (distance + 1000)) {
                    distance += 1000;
                    String driverID = com.getDriverID();
                    company.oneKmPay(driverID);
                }

              
                if (currentDistance > (distance + 1000)) {
                    distance += 1000;
                    String driverID = com.getDriverID();
                    company.oneKmPay(driverID);
                }

                if (status.equals("aguardando")) {

                    if (!Company.routesAvaliable()) {
                        System.out.println("Sem mais rotas disponíveis");
                        Rota rota = new Rota("-1", "00000");
                        output.writeUTF(routeJSON(rota));
                        break;
                    }

                    if (Company.routesAvaliable()) {
                        synchronized (sync) {
                            Rota response = company.execRoute();
                            output.writeUTF(routeJSON(response));
                        }
                    }

                } else if (status.equals("finalizado")) {

                    String routeID = com.getRouteIDSUMO();
                    company.endRoute(routeID);
                    distance = 0;

               
            }

           
            input.close();
            output.close();
            carSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    
    
    
}
