package Trabalho.Transporte;

import Trabalho.Car.Car;
import Trabalho.Rotas.Route;
import de.tudresden.sumo.cmd.Vehicle;
import it.polito.appeal.traci.SumoTraciConnection;

public class Transporte extends Thread {

    private String idTransportService;
    private boolean on_off;
    private SumoTraciConnection sumo;
    private Car car;
    private Route route;
   // private boolean terminado;
    private boolean sumoInit;

    public Transporte(boolean _on_off, String _idTransportService, Route _route, Car _car, SumoTraciConnection _sumo) {
        this.on_off = _on_off;
        this.idTransportService = _idTransportService;
        this.route = _route;
        this.car = _car;
        this.sumo = _sumo;
    //    this.terminado = false;
        this.sumoInit = false;
    }

    @Override
    public void run() {
        System.out.println("Iniciando Transport para o Carro: " + this.car.getIdCar());

        initializeRoutes();

        System.out.println(this.car.getIdCar() + "Rota: " + route.parseEdges() + " adicionada!");

        String edgeFinal = route.parseEdges().get(route.parseEdges().size() - 1);
        System.out.println(this.car.getIdCar() + "- Edge final: " + edgeFinal);

        try {
            sleep(this.car.getAcquisitionRate());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Encerrando Transport.");
    }

    private void initializeRoutes() {
        try {
            sumo.do_job_set(de.tudresden.sumo.cmd.Route.add(this.route.getID(), this.route.parseEdges()));
            sumo.do_job_set(Vehicle.Atual(this.car.getIdCar(),
                                                    this.route.getID(),
                                                    "DEFAULT_VEHTYPE", 
                                                    "now",
                                                    "0",
                                                    "0",
                                                    "0",
                                                    "current", 
                                                    "max", 
                                                    "current",
                                                    "",
                                                    "",
                                                    ""));
            sumo.do_job_set(Vehicle.setColor(this.car.getIdCar(), this.car.getColorCar()));
            this.sumo.do_job_set(Vehicle.setSpeed(this.car.getIdCar(), 50));
            this.sumo.do_job_set(Vehicle.setSpeedMode(this.car.getIdCar(), 31));
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        this.sumoInit = true;
    }
    
    public boolean isOn_off() {
        return on_off;
    }

   
    public void setOn_off(boolean _on_off) {
        this.on_off = _on_off;
    }

    //
   // public void setTerminado(boolean _terminado) {
  //      this.terminado = _terminado;
  //  }

    
    public String getIdTransportService() {
        return this.idTransportService;
    }

    
    public SumoTraciConnection getSumo() {
        return this.sumo;
    }

   
    public Car getCar() {
        return this.car;
    }

    
    
    public Route getRota() {
        return this.route;
    }

    //Rota.
     
    public void setRoute(Route _route) {
        this.route = _route;
    }
}

