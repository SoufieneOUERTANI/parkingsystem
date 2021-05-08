package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
        FareCalculatorService.setTicketDAO(ticketDAO);
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar_TicketIsGenerated_AND_ParkingSlotLocked() throws ClassNotFoundException, SQLException{
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
    
        Connection con = null;
        con = ticketDAO.dataBaseConfig.getConnection();
        PreparedStatement ps = con.prepareStatement("select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE, p.AVAILABLE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=\"ABCDEF\" order by t.IN_TIME  limit 1");
        ResultSet rs = ps.executeQuery();
        if(rs.next()){
        	assertEquals(rs.getInt(1),1);
        	assertEquals(rs.getInt(7),0);
        }
        
    }

    @Test
    public void testParkingExit_TicketGenerated_AND_ParkingSlotAgainAvailable() throws ClassNotFoundException, SQLException{
        ParkingService parkingService_1 = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService_1.processIncomingVehicle();
        ParkingService parkingService_2 = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService_2.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database
        
        Connection con = null;
        con = ticketDAO.dataBaseConfig.getConnection();
        PreparedStatement ps = con.prepareStatement("select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE, p.AVAILABLE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=\"ABCDEF\" order by t.IN_TIME  limit 1");
        ResultSet rs = ps.executeQuery();
        if(rs.next()){
        	assertNotEquals(rs.getTimestamp(4),null);
        	assertEquals(rs.getInt(7),1);
        	if ((rs.getTimestamp(5).getTime() - rs.getTimestamp(4).getTime()) > 1800000){
            	assertNotEquals(rs.getTimestamp(3),0);
        	}        
        }
    }

}
