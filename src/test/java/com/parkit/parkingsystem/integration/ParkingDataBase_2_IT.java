package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author SOUE
 *
 */
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
        FareCalculatorService.setTicketDAO(ticketDAO);
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
    
    /**
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    @Test
    
    public void testRecuringUser_5PercentDiscount() throws ClassNotFoundException, SQLException{
        Connection con = null;
        PreparedStatement ps;

        con = ticketDAO.dataBaseConfig.getConnection();
        assertEquals(ticketDAO.verifyRecuring("ABCDEF"),false);
        
        int id = 1;
        int parking_number = 1; 
        String vehicle_reg_number = "ABCDEF"; 
        int price = 0;
        int in_time_min = 240;
        int out_time_min = 120;

        insertDBTicketField(con, id, parking_number, vehicle_reg_number, price, in_time_min, out_time_min);
        
        assertEquals(ticketDAO.verifyRecuring("ABCDEF"),true);
        
        id = 2;
        parking_number = 2; 
        vehicle_reg_number = "ABCDEF"; 
        price = 0;
        in_time_min = 60;
        out_time_min = 0;
        
        insertDBTicketField(con, id, parking_number, vehicle_reg_number, price, in_time_min, out_time_min);
     
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
    
    @Test
    public void testAlreadyExistingVehicleRegNumber_Error() throws ClassNotFoundException, SQLException{
    	
        when(inputReaderUtil.readSelection()).thenReturn(1);
   	
        ParkingService parkingService_1 = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService_1.processIncomingVehicle();

        ParkingService parkingService_2 = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService_2.processIncomingVehicle();

        Connection con = null;
        PreparedStatement ps;

        con = ticketDAO.dataBaseConfig.getConnection();

        ps = con.prepareStatement("select ID from ticket where VEHICLE_REG_NUMBER = \"ABCDEF\" and OUT_TIME is null");
        ResultSet rs = ps.executeQuery();
        int rsCount = 0;
        while(rs.next()){
        	rsCount ++;
        } 
    	assertEquals(rsCount, 1);
    }

	/**
	 * @param con
	 * @param id
	 * @param parking_number
	 * @param vehicle_reg_number
	 * @param price
	 * @param in_time_min
	 * @param out_time_min
	 * @throws SQLException
	 */
	private void insertDBTicketField(Connection con, int id, int parking_number, String vehicle_reg_number, int price,
			int in_time_min, int out_time_min) throws SQLException {
		PreparedStatement ps;
		ps = con.prepareStatement("insert into ticket(ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?,?)");
        ps.setInt(1, id);
        ps.setInt(2, parking_number );
        ps.setString(3, vehicle_reg_number);
        ps.setInt(4, price);
        ps.setTimestamp(5,Timestamp.from(Instant.now().minus(in_time_min,ChronoUnit.MINUTES)));
        ps.setTimestamp(6,Timestamp.from(Instant.now().minus(out_time_min,ChronoUnit.MINUTES)));
        if (out_time_min == 0) {
            ps.setTimestamp(6,null);
        }
        ps.executeUpdate();
	}
}
