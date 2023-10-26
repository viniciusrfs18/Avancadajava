package Trabalho.Rotas;


import de.tudresden.sumo.objects.SumoStringList;
import java.io.File;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Route {

    private String id;
    private String edges;

    public Route(String id, String edges) {
        this.id = id;
        this.edges = edges;
    }

    public String getID() {
        return id;
    }

    public String getEdges() {
        return edges;
    }

    public SumoStringList parseEdges() {
        SumoStringList edgeList = new SumoStringList();
        for (String e : this.edges.split(" ")) {
            edgeList.add(e);
        }
        return edgeList;
    }

    public static ArrayList<Route> criaRotas(String xmlFilePath) {
                ArrayList<Route> ArRoutes = new ArrayList<>();
        
        try {
            
            File xmlFile = new File(xmlFilePath);
            
           
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
          
            Document doc = builder.parse(xmlFile);
            
            
            NodeList vehicleList = doc.getElementsByTagName("vehicle");
            
          
            for (int i = 0; i < vehicleList.getLength(); i++) {
                Element vehicleElement = (Element) vehicleList.item(i);                
                
                String idRouteAux = vehicleElement.getAttribute("id");                
                
                NodeList routeList = vehicleElement.getElementsByTagName("route");
                
                for (int j = 0; j < routeList.getLength(); j++) {
                    Element routeElement = (Element) routeList.item(j);
                    
                    String edges = routeElement.getAttribute("edges");
                    
                    Route route = new Route(idRouteAux, edges);
                    
                    ArRoutes.add(route);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Retorna a lista de rotas do arquivo XML
        return ArRoutes;
    }
}


