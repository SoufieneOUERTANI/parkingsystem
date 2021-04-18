package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBase_2_IT {

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
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }
    
    @AfterAll
    private static void tearDown(){
        //dataBasePrepareService.clearDataBaseEntries();
    }
    
    @Test
    
    public void testRecuringUser5PercentDiscount() throws ClassNotFoundException, SQLException{
        Connection con = null;
        con = ticketDAO.dataBaseConfig.getConnection();
        PreparedStatement ps;
        int qr;
        
        assertEquals(ticketDAO.verifyRecuring("ABCDEF"),false);
        
        ps = con.prepareStatement("insert into ticket(ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?,?)");
        ps.setInt(1, 1);
        ps.setInt(2, 1);
        ps.setString(3, "ABCDEF");
        ps.setInt(4, 0);
        ps.setTimestamp(5,Timestamp.from(Instant.now().minus(4,ChronoUnit.HOURS)));
        ps.setTimestamp(6,Timestamp.from(Instant.now().minus(2,ChronoUnit.HOURS)));
        qr = ps.executeUpdate();
        
        ps = con.prepareStatement("insert into ticket(ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?,?)");
        ps.setInt(1, 2);
        ps.setInt(2, 2);
        ps.setString(3, "ABCDEF");
        ps.setInt(4, 0);
        ps.setTimestamp(5,Timestamp.from(Instant.now().minus(1,ChronoUnit.HOURS)));
        ps.setTimestamp(6,null);
        qr = ps.executeUpdate();
        
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        assertEquals(ticketDAO.verifyRecuring("ABCDEF"),true);
        parkingService.processExitingVehicle();

        
        ps = con.prepareStatement("select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE, p.AVAILABLE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=\"ABCDEF\" order by t.IN_TIME desc limit 1");
        ResultSet rs = ps.executeQuery();

        if(rs.next()){
            double duration = ((float)(rs.getTimestamp(5).getTime() - rs.getTimestamp(4).getTime()))/3600000.0;
            float prix = (float) (duration * Fare.CAR_RATE_PER_HOUR * 0.95);
        
        	assertEquals(
        			(Math.round(rs.getFloat(3) * 100))/100.0, (Math.round(prix * 100))/100.0);
        }

    }
}
