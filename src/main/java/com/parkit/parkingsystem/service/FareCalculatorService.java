package com.parkit.parkingsystem.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
	
    private static DataBaseConfig dataBaseConfig = new DataBaseConfig();
    private static TicketDAO ticketDAO = new TicketDAO();

    public void calculateFare(Ticket ticket) throws ClassNotFoundException, SQLException{

        Connection con = null;
        PreparedStatement ps;
        ResultSet rs;
        
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        double duration = ((float)(outHour - inHour))/3600000.0;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration  * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration  * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        } 
        if (duration <= 0.5)
        	ticket.setPrice(0);

        String vehicleRegNumber = ticket.getVehicleRegNumber();
        ticketDAO.dataBaseConfig = dataBaseConfig;
        con = ticketDAO.dataBaseConfig.getConnection();
        ps = con.prepareStatement("select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE, p.AVAILABLE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=? and t.OUT_TIME IS NOT NULL order by t.IN_TIME desc limit 1");
        ps.setString(1, vehicleRegNumber);
        rs = ps.executeQuery();
        if(rs.next())
        	ticket.setPrice(ticket.getPrice()*0.95);
        ticket.setPrice((Math.round(ticket.getPrice() * 100)) / 100.0);
    }
}