package Trabalho.Relatorio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import Trabalho.Driver.Driver;
import Trabalho.Car.Car;

public class Relatorio {

    // Método responsável pela criação da Planilha que irá conter os dados dos Motoristas,
	// Cria uma Sheet para cada Driver criado
	private void createSheet(ArrayList<Driver> drivers){
		String nomeDoArquivo = "DadosCarro.xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            for (Driver driver : drivers) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet(driver.getCar().getIdCar());
                
				Row headerRow = sheet.createRow(0);
				headerRow.createCell(0).setCellValue("Timestamp");
				headerRow.createCell(1).setCellValue("ID Car");
				headerRow.createCell(2).setCellValue("ID Route");
				headerRow.createCell(3).setCellValue("Speed");
				headerRow.createCell(4).setCellValue("Distance");
				headerRow.createCell(5).setCellValue("FuelConsumption");
				headerRow.createCell(6).setCellValue("FuelType");
				headerRow.createCell(7).setCellValue("CO2Emission");
				headerRow.createCell(8).setCellValue("Longitude (Lon)");
				headerRow.createCell(9).setCellValue("Latitude (Lat)");
            }

            // Salva o arquivo .xlsx após criar todas as abas de planilha.
            try (FileOutputStream outputStream = new FileOutputStream(nomeDoArquivo)) {
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

// atualiza a tabela dos carros
//
//
//




public class AtualizaCarrro extends Thread{
    private Company company;

    public AtualizaCarro(Company _company) {
        this.company = _company;
    }

    @Override
    public void run() {
        try {
            while (Company.routesAvaliable()) {
                Thread.sleep(10);
                if (!company.temReport()) {
                    updateSheetCar(company.pegacom());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateSheetCar(DrivingData data){
        
        String nomeDoArquivo = "carData.xlsx";

        try (FileInputStream inputStream = new FileInputStream(nomeDoArquivo);
        Workbook workbook = WorkbookFactory.create(inputStream);
        FileOutputStream outputStream = new FileOutputStream(nomeDoArquivo)) {
            
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(data.getCarID());    
            
        int lastRowNum = sheet.getLastRowNum();
        Row newRow = sheet.createRow(lastRowNum + 1);

                // Preencha as células da nova linha com os dados da classe TransferData
        newRow.createCell(0).setCellValue(data.getTimeStamp());
        newRow.createCell(1).setCellValue(data.getCarID());
        newRow.createCell(2).setCellValue(data.getRouteIDSUMO());
        newRow.createCell(3).setCellValue(data.getSpeed());
        newRow.createCell(4).setCellValue(data.getOdometer()); 
        newRow.createCell(5).setCellValue(data.getFuelConsumption());
        newRow.createCell(6).setCellValue(data.getFuelType());
        newRow.createCell(7).setCellValue(data.getCo2Emission());
        newRow.createCell(8).setCellValue(data.getLonAtual());
        newRow.createCell(9).setCellValue(data.getLatAtual());
            
        // Salve as alterações na planilha
        workbook.write(outputStream);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}